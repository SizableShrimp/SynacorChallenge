import java.util.Scanner

@OptIn(ExperimentalUnsignedTypes::class)
class ConsoleVirtualMachine : VirtualMachine {
    constructor(data: UShortArray) : super(data)

    constructor(data: ByteArray) : super(data)

    private val scanner by lazy {
        Scanner(System.`in`)
    }
    private val startingLines: MutableList<String> = AdventurePathFinder.startingActions.toMutableList()
    override fun printChar(char: Char) = print(char)

    override fun readLine(): String = if (this.startingLines.isEmpty()) this.scanner.nextLine() else this.startingLines.removeFirst()
}