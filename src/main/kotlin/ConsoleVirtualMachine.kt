import java.util.Scanner

@OptIn(ExperimentalUnsignedTypes::class)
class ConsoleVirtualMachine : VirtualMachine {
    constructor(data: UShortArray) : super(data)

    constructor(data: ByteArray) : super(data)

    private val scanner by lazy {
        Scanner(System.`in`)
    }
    private val startingLines: MutableList<String> = mutableListOf("take tablet",
        "doorway",
        "north",
        "north",
        "bridge",
        "continue",
        "down",
        "east",
        "take empty lantern",
        "west",
        "west",
        "passage",
        // Grue time
        // "darkness", // We need the lantern for this one
        "ladder"
    )

    override fun printChar(char: Char) = print(char)

    override fun readLine(): String = if (this.startingLines.isEmpty()) this.scanner.nextLine() else this.startingLines.removeFirst()
}