import java.nio.file.Files
import java.nio.file.Paths

@OptIn(ExperimentalUnsignedTypes::class)
fun main() {
    val vm = ConsoleVirtualMachine(Files.readAllBytes(Paths.get("challenge.bin")))
    val load = true
    if (load) {
        VirtualMachineIO.readVM(vm, Paths.get("headquarters.txt"))
        // After investigating the code, we simply want the teleporter value to be "5"
        // The function at line 2740, after removing the nearly-infinite recursion, simply
        // sets reg 0 to the teleporter value + 1. Reg 1 is overwritten right after calling
        // the function, so we can ignore what happens to it inside the function.
        // However, it can be seen that reg 1 goes up to infinity very quickly inside the function.
        // Given we want reg 0 to be equal to 6 at the end of the function, we want the
        // teleporter value aka reg 7 to be set to 5, so that 5 + 1 = 6.
        vm.registers[7] = 5U
    } else {
        vm.startingLines.addAll(AdventurePathFinder.startingActions)
    }

    vm.run()
}