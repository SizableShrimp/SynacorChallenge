@OptIn(ExperimentalUnsignedTypes::class)
abstract class OutputStoringVirtualMachine : VirtualMachine {
    private val lineBuilder: StringBuilder = StringBuilder()
    private val mutableOutputLines: MutableList<String> = mutableListOf()
    val outputLines: List<String> = mutableOutputLines

    constructor(data: UShortArray) : super(data)

    constructor(data: ByteArray) : super(data)

    override fun printChar(char: Char) {
        if (char == '\n') {
            this.mutableOutputLines.add(this.lineBuilder.toString())
            this.lineBuilder.clear()
            return
        }

        this.lineBuilder.append(char)
    }

    internal fun clearOutputLines() = this.mutableOutputLines.clear()
}