import VirtualMachine.Operation
import java.util.stream.Collectors

@OptIn(ExperimentalUnsignedTypes::class)
class VirtualMachine(private val data: UShortArray) {
    constructor(data: ByteArray) : this(getUShortArray(data))

    fun run() {
        var idx = 0
        val registers = UShortArray(8)

        while (idx < data.size) {
            val opcode = Opcode.LOOKUP_TABLE.getValue(this.data[idx].toByte())
            if (opcode == Opcode.HALT)
                return

            idx = opcode.execute(registers, this.data, idx)
        }
    }

    enum class Opcode(private val id: Byte, private val numArgs: Int, private val operation: Operation = Operation { _, _ -> }) {
        HALT(0, 0),
        SET(1, 2),
        PUSH(2, 1),
        POP(3, 1),
        EQ(4, 3),
        GT(5, 3),
        JMP(6, 1) {
            override fun getOffset(offset: Int, getter: (Int) -> UShort, setter: (Int, UShort) -> Unit) = getter(0).toInt()
        },
        JT(7, 2) {
            override fun getOffset(offset: Int, getter: (Int) -> UShort, setter: (Int, UShort) -> Unit) =
                if (getter(0) != 0.toUShort()) getter(1).toInt() else super.getOffset(offset, getter, setter)
        },
        JF(8, 2) {
            override fun getOffset(offset: Int, getter: (Int) -> UShort, setter: (Int, UShort) -> Unit) =
                if (getter(0) == 0.toUShort()) getter(1).toInt() else super.getOffset(offset, getter, setter)
        },
        ADD(9, 3),
        MULT(10, 3),
        MOD(11, 3),
        AND(12, 3),
        OR(13, 3),
        NOT(14, 2),
        RMEM(15, 2),
        WMEM(16, 2),
        CALL(17, 1),
        RET(18, 0),
        OUT(19, 1, { getter, _ -> print(getter(0).toInt().toChar()) }),
        IN(20, 1),
        NOOP(21, 0);

        fun execute(registers: UShortArray, data: UShortArray, offset: Int): Int {
            val getter: (Int) -> UShort = { idx ->
                val arg = data[offset + 1 + idx]
                if (arg > 32767U) {
                    registers[arg.toInt() - 32768]
                } else {
                    arg
                }
            }
            val setter: (Int, UShort) -> Unit = { idx, value ->
                val arg = data[offset + 1 + idx]
                if (arg > 32767U) {
                    registers[arg.toInt() - 32768] = value
                } else {
                    data[arg.toInt()] = value
                }
            }

            this.operation.operate(getter, setter)
            return getOffset(offset, getter, setter)
        }

        protected open fun getOffset(offset: Int, getter: (Int) -> UShort, setter: (Int, UShort) -> Unit) = offset + 1 + this.numArgs

        companion object {
            internal val LOOKUP_TABLE = entries.stream().collect(Collectors.toMap({ it.id }, { it }))
        }
    }

    fun interface Operation {
        fun operate(getter: (Int) -> UShort, setter: (Int, UShort) -> Unit)
    }

    fun interface OffsetGenerator {
        fun generate(offset: Int, getter: (Int) -> UShort, setter: (Int, UShort) -> Unit): Int
    }

    companion object {
        private fun getUShort(lower: Byte, upper: Byte) = (upper.toUByte().toInt().shl(8) or lower.toUByte().toInt()).toUShort()

        private fun getUShort(data: ByteArray, idx: Int) = getUShort(data[idx], data[idx + 1])

        private fun getUShortArray(data: ByteArray): UShortArray {
            val array = UShortArray(data.size / 2)

            for (idx in data.indices step 2) {
                array[idx / 2] = getUShort(data, idx)
            }

            return array
        }
    }
}
