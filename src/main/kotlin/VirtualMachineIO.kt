import java.nio.file.Files
import java.nio.file.Path

@OptIn(ExperimentalUnsignedTypes::class)
object VirtualMachineIO {
    fun writeVM(vm: VirtualMachine, path: Path) {
        val lines = mutableListOf<String>()

        // Pseudocode
        var idx = 0
        val data = vm.data
        val size = data.size
        while (idx < size) {
            val opcode = getOpcode(data[idx])
            if (opcode == null) {
                lines.add(data[idx].toString())
                idx++
                continue
            }

            lines.add((lines.size + 1).toString() + ": " + writeOpcode(opcode, data, idx))
            idx += 1 + opcode.numArgs
        }

        lines.add("")

        // Offset
        lines.add("Offset: " + vm.offset)

        lines.add("")

        // Registers
        lines.add("Registers:")
        vm.registers.forEach {
            lines.add(it.toString())
        }

        lines.add("")

        // Stack
        lines.add("Stack:")
        vm.stack.forEach {
            lines.add(it.toString())
        }

        Files.write(path, lines, Charsets.UTF_8)
    }

    private fun writeOpcode(opcode: VirtualMachine.Opcode, data: UShortArray, offset: Int): String {
        val lineBuilder = StringBuilder(opcode.name.lowercase())
        val isJump = opcode == VirtualMachine.Opcode.JMP
                || opcode == VirtualMachine.Opcode.JT
                || opcode == VirtualMachine.Opcode.JF
                || opcode == VirtualMachine.Opcode.CALL

        for (i in 0..<opcode.numArgs) {
            val value = data[offset + 1 + i]
            lineBuilder.append(' ')
            if (value > 32767U) {
                lineBuilder.append('[').append(value - 32768U).append(']')
            } else {
                lineBuilder.append(value)
                if (isJump) {
                    if (i == opcode.numArgs - 1)
                        lineBuilder.append(" # ").append(getLineNumber(data, value.toInt()))
                } else if (opcode == VirtualMachine.Opcode.OUT) {
                    val char = value.toInt().toChar()
                    lineBuilder.append(" # '")
                    if (char == '\n') {
                        lineBuilder.append("\\n")
                    } else {
                        lineBuilder.append(char)
                    }
                    lineBuilder.append('\'')
                }
            }
        }

        return lineBuilder.toString()
    }

    fun readVM(vm: VirtualMachine, path: Path) {
        val lines = readVMLines(path)

        // Pseudocode
        val breakIdx = readCode(lines, vm.data)

        // Offset
        vm.offset = lines[breakIdx].substring(8).toInt()

        // Registers
        for (i in 0..<8) {
            vm.registers[i] = lines[i + breakIdx + 2].toUShort()
        }

        // Stack
        for (i in (breakIdx + 2 + 8 + 1)..<lines.size) {
            vm.stack.addLast(lines[i].toUShort())
        }
    }

    private fun readVMLines(path: Path) = Files.readAllLines(path).map {
        val commentIdx = it.indexOf('#')
        if (commentIdx != -1) {
            it.substring(0, commentIdx)
        } else {
            it
        }
    }.filter { it.isNotBlank() }.map { it.trim() }

    private fun readCode(lines: List<String>, data: UShortArray): Int {
        var idx = 0

        for ((lineIdx, line) in lines.withIndex()) {
            if (line[0].isUpperCase()) {
                return lineIdx
            }

            val colonIdx = line.indexOf(':')
            if (colonIdx == -1) {
                data[idx++] = line.toUShort()
                continue
            }

            val split = line.substring(colonIdx + 2).split(' ')
            val opcode = VirtualMachine.Opcode.valueOf(split[0].uppercase())
            data[idx++] = opcode.id.toUShort()

            for (i in 0..<opcode.numArgs) {
                val valueStr = split[i + 1]
                data[idx++] = if (valueStr[0] == '[') {
                    (valueStr[1].digitToInt().toUInt() + 32768U).toUShort()
                } else {
                    valueStr.toUShort()
                }
            }
        }

        error("Should not get here")
    }

    /**
     * @return The 1-based line number based on the current VM's state and offset
     */
    fun getLineNumber(data: UShortArray, offset: Int): Int {
        var line = 1
        var idx = 0

        while (idx < offset) {
            val opcode = getOpcode(data[idx])
            idx += if (opcode == null) 1 else 1 + opcode.numArgs
            line++
        }

        return line
    }

    val VirtualMachine.lineNumber
        get() = getLineNumber(this.data, this.offset)

    private fun getOpcode(opcodeId: UShort) = if (opcodeId > 256U) null else VirtualMachine.Opcode.LOOKUP_TABLE[opcodeId.toByte()]
}