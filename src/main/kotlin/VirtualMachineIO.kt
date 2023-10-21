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
            val opcodeId = data[idx]
            val opcode = if (opcodeId > 256U) null else VirtualMachine.Opcode.LOOKUP_TABLE[opcodeId.toByte()]
            if (opcode == null) {
                lines.add(data[idx].toString())
                idx++
                continue
            }

            val lineBuilder = StringBuilder(opcode.name.lowercase())
            for (i in 0..<opcode.numArgs) {
                val value = data[idx + 1 + i]
                lineBuilder.append(' ')
                if (value > 32767U) {
                    lineBuilder.append('[').append(value - 32768U).append(']')
                } else {
                    lineBuilder.append(value)
                }
            }

            lines.add(lineBuilder.toString())
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

    fun readVM(vm: VirtualMachine, path: Path) {
        val lines = Files.readAllLines(path).map {
            val commentIdx = it.indexOf('#')
            if (commentIdx != -1) {
                it.substring(0, commentIdx).trim()
            } else {
                it
            }
        }.filter { it.isNotEmpty() }
        var idx = 0
        var breakIdx = 0

        // Pseudocode
        for ((lineIdx, line) in lines.withIndex()) {
            if (line[0].isUpperCase()) {
                breakIdx = lineIdx
                break
            }

            if (line[0].isDigit()) {
                vm.data[idx++] = line.toUShort()
                continue
            }

            val split = line.split(' ')
            val opcode = VirtualMachine.Opcode.valueOf(split[0].uppercase())
            vm.data[idx++] = opcode.id.toUShort()

            for (i in 0..<opcode.numArgs) {
                val valueStr = split[i + 1]
                vm.data[idx++] = if (valueStr[0] == '[') {
                    (valueStr[1].digitToInt().toUInt() + 32768U).toUShort()
                } else {
                    valueStr.toUShort()
                }
            }
        }

        // Offset
        vm.offset = lines[breakIdx].substring(8).toInt()

        // Registers
        for (i in 0..<8) {
            vm.registers[i] = lines[i + breakIdx + 2].toUShort()
        }

        // Stack
        for (i in (breakIdx + 2 + 8 + 1)..<lines.size) {
            val line = lines[i]
            if (line.isNotBlank())
                vm.stack.addLast(line.toUShort())
        }
    }

    /**
     * @return The 1-based line number based on the current VM's state and offset
     */
    fun getLineNumber(vm: VirtualMachine, offset: Int): Int {
        var line = 1
        var idx = 0
        val data = vm.data

        while (idx < offset) {
            val opcodeId = data[idx].toByte()
            val opcode = VirtualMachine.Opcode.LOOKUP_TABLE[opcodeId]
            if (opcode == null) {
                idx++
                line++
                continue
            } else {
                idx += 1 + opcode.numArgs
                line++
                continue
            }
        }

        return line
    }
}