@OptIn(ExperimentalUnsignedTypes::class)
class SolverVirtualMachine : OutputStoringVirtualMachine {
    private val inputLines: MutableList<String> = mutableListOf()

    constructor(data: UShortArray) : super(data)

    constructor(data: ByteArray) : super(data)

    override fun readLine() = this.inputLines.takeIf { it.isNotEmpty() }?.removeFirst()

    fun addInput(input: String) = this.inputLines.add(input)

    fun addInputs(inputs: List<String>) = this.inputLines.addAll(inputs)
}