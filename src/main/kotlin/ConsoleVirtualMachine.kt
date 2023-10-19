import java.util.Scanner

@OptIn(ExperimentalUnsignedTypes::class)
class ConsoleVirtualMachine : VirtualMachine {
    constructor(data: UShortArray) : super(data)

    constructor(data: ByteArray) : super(data)

    private val scanner by lazy {
        Scanner(System.`in`)
    }

    override fun printChar(char: Char) = print(char)

    override fun readLine(): String = this.scanner.nextLine()
}