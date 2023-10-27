import java.util.Scanner

@OptIn(ExperimentalUnsignedTypes::class)
class ConsoleVirtualMachine : VirtualMachine {
    private val scanner by lazy {
        Scanner(System.`in`)
    }

    constructor(data: UShortArray) : super(data)

    constructor(data: ByteArray) : super(data)

    override fun printChar(char: Char) = print(char)

    override fun readLine(): String = this.scanner.nextLine()
}