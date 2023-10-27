@file:OptIn(ExperimentalUnsignedTypes::class)

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.lang.StringBuilder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.GZIPInputStream

fun main(args: Array<String>) {
    val (archSpecPath, challengeBinPath) = getInputPaths(args)
    val vm = SolverVirtualMachine(Files.readAllBytes(challengeBinPath))
    vm.addInputs(AdventurePathFinder.startingActions)

    vm.run() // Run up to and including the first teleport

    modifyTeleporterCalibration(vm) // Modify the VM's teleporter calibration code to not take 1 billion years

    // Set the teleporter register to the value we found in TeleporterCodeSolver.kt
    vm.registers[7] = 25734U
    vm.addInputs(AdventurePathFinder.headquartersStartingActions)

    vm.run() // Run to the end

    // Now we need to extract the codes
    val extractedCodes = extractCodes(vm.outputLines, Files.readAllLines(archSpecPath))

    // Print each code
    extractedCodes.forEachIndexed { idx, code -> println("Code ${idx + 1}: $code") }
}

private fun getInputPaths(args: Array<String>): Pair<Path, Path> {
    if (args.size == 2)
        return Paths.get(args[0]) to Paths.get(args[1])

    if (args.isEmpty())
        error("Input must be either a path to synacor-challenge.tgz OR arch-spec and challenge.bin separately in that order")

    // Now we know the size is 1 here
    val zipPath = Paths.get(args[0])
    val tempDir = Files.createTempDirectory(null)
    tempDir.toFile().deleteOnExit()
    val archSpecPath = tempDir.resolve("arch-spec")
    val challengeBinPath = tempDir.resolve("challenge.bin")

    with(Files.newInputStream(zipPath)) {
        with(GZIPInputStream(this)) {
            with(TarArchiveInputStream(this)) {
                this.nextTarEntry
                Files.copy(this, archSpecPath)
                this.nextTarEntry
                Files.copy(this, challengeBinPath)
            }
        }
    }

    return archSpecPath to challengeBinPath
}

private fun modifyTeleporterCalibration(vm: VirtualMachine) {
    val data = vm.data
    var idx = 0
    val jfId = VirtualMachine.Opcode.JF.id.toUShort()
    val teleporterRegisterId = getRegisterId(7)

    while (idx < data.size) {
        if (data[idx] == jfId && data[idx + 1] == teleporterRegisterId) {
            // We found the surrounding code that executes the calibration
            break
        }

        idx++
    }

    if (idx == data.size)
        error("The VM should always have a JF instruction with an argument referring to register 7")

    val callId = VirtualMachine.Opcode.CALL.id.toUShort()
    var foundFirstCall = false

    while (idx < data.size) {
        if (data[idx] == callId) {
            if (foundFirstCall) {
                // The second CALL instruction after JF [7] is what calls the calibration code
                break
            }

            foundFirstCall = true
        }
        idx++
    }

    if (idx == data.size)
        error("The VM should always have at least 2 CALL instructions after the JF [7] instruction")

    val noopId = VirtualMachine.Opcode.NOOP.id.toUShort()

    // Cancel the call to the calibration code
    data[idx] = noopId
    data[idx + 1] = noopId

    // We want to replace eq [1] [0] 6 with set [0] [6] and a following NOOP to keep all the memory addresses the same
    data[idx + 2] = VirtualMachine.Opcode.SET.id.toUShort()
    data[idx + 3] = getRegisterId(1)
    data[idx + 4] = 6U
    data[idx + 5] = VirtualMachine.Opcode.NOOP.id.toUShort()

    // At this point, we have successfully modified the teleporter code such that calibration is skipped but still counted as successful.
}

private fun getRegisterId(id: Int) = (id + 32768).toUShort()

private fun extractCodes(outputLines: List<String>, archSpecLines: List<String>): List<String> {
    val result = mutableListOf<String>()

    // Add code 1 from the arch-spec
    result.add(findCode(archSpecLines, "- Here's a code for the challenge website: "))

    // Add code 2 from the first part of the self-test
    result.add(findCode(outputLines, "this one into the challenge website: "))

    // Add code 3 from the second part of the self-test
    result.add(findCode(outputLines, "The self-test completion code is: "))

    // Add code 4 from reading the tablet
    result.add(findCode(outputLines, "You find yourself writing \""))

    // Add code 5 from a wall in the twisty passages
    result.add(findMultiLineCode(outputLines, "Chiseled on the wall of one of the passageways, you see:"))

    // Add code 6 from using the teleporter for the first time
    result.add(findMultiLineCode(outputLines, "You activate the teleporter!  As you spiral through time and space, you think you see a pattern in the stars..."))

    // Add code 7 from using the teleporter for the second time
    result.add(findMultiLineCode(outputLines, "You wake up on a sandy beach with a slight headache.  The last thing you remember is activating that teleporter... "
            + "but now you can't find it anywhere in your pack.  Someone seems to have drawn a message in the sand here:")
    )

    // Add code 8 from using the mirror and reversing the code
    result.add(reverseMirrorCode(findCode(outputLines, "You gaze into the mirror, and you see yourself gazing back.  But wait!  "
            + "It looks like someone wrote on your face while you were unconscious on the beach!  Through the mirror, you see \"")))

    return result
}

private fun findLine(outputLines: List<String>, prefix: String): String {
    for (line in outputLines) {
        if (line.startsWith(prefix))
            return line
    }

    error("Could not find line matching prefix \"$prefix\" in output")
}

private fun findCode(outputLines: List<String>, prefix: String) = findLine(outputLines, prefix).substring(prefix.length).take(12)

private fun findMultiLineCode(outputLines: List<String>, line: String) = outputLines[outputLines.indexOf(line) + 2].substring(4)

private fun reverseMirrorCode(inputCode: String): String {
    val builder = StringBuilder()

    for (i in inputCode.indices) {
        val resultChar = when (val char = inputCode[inputCode.length - 1 - i]) {
            'b' -> 'd'
            'd' -> 'b'
            'p' -> 'q'
            'q' -> 'p'
            else -> char
        }

        builder.append(resultChar)
    }

    return builder.toString()
}