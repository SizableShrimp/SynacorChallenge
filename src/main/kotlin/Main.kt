import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    val vm = ConsoleVirtualMachine(Files.readAllBytes(Paths.get("challenge.bin")))
    val load = true
    if (load) {
        VirtualMachineIO.readVM(vm, Paths.get("headquarters.txt"))
    } else {
        vm.startingLines.addAll(AdventurePathFinder.startingActions)
    }

    vm.run()
}