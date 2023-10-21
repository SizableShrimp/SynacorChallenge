import java.nio.file.Files
import java.nio.file.Paths

@OptIn(ExperimentalUnsignedTypes::class)
fun main() {
    val vm = ConsoleVirtualMachine(Files.readAllBytes(Paths.get("challenge.bin")))
    val load = true
    if (load) {
        VirtualMachineIO.readVM(vm, Paths.get("headquarters.txt"))
        // Testing with teleporter
        vm.registers[7] = 1U
    } else {
        vm.startingLines.addAll(AdventurePathFinder.startingActions)
    }

    vm.run()
}