import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    val vm = VirtualMachine(Files.readAllBytes(Paths.get("challenge.bin")))

    vm.run()
}