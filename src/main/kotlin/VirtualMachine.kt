import VirtualMachine.Operation
import java.util.Deque
import java.util.LinkedList
import java.util.stream.Collectors

@OptIn(ExperimentalUnsignedTypes::class)
class VirtualMachine(private val data: UShortArray) {
    constructor(data: ByteArray) : this(getUShortArray(data))

    fun run() {
        var idx = 0
        val registers = UShortArray(8)
        val stack = LinkedList<UShort>()

        while (idx < data.size) {
            val opcode = Opcode.LOOKUP_TABLE.getValue(this.data[idx].toByte())
            if (opcode == Opcode.HALT)
                break

            idx = opcode.execute(registers, this.data, stack, idx)
            if (idx == -1)
                break
        }
    }

    enum class Opcode(private val id: Byte, private val numArgs: Int, private val operation: Operation = Operation { _, _, _, _, _ -> }) {
        HALT(0, 0),
        SET(1, 2, { _, get, set, _, _ -> set(0, get(1)) }),
        PUSH(2, 1, { _, get, _, _, stack -> stack.add(get(0)) }),
        POP(3, 1, { _, _, set, _, stack ->
            check(stack.isNotEmpty())
            set(0, stack.removeLast())
        }),
        EQ(4, 3, { _, get, set, _, _ -> set(0, if (get(1) == get(2)) 1.toUShort() else 0.toUShort()) }),
        GT(5, 3, { _, get, set, _, _ -> set(0, if (get(1) > get(2)) 1.toUShort() else 0.toUShort()) }),
        JMP(6, 1) {
            override fun getOffset(offset: Int, get: (Int) -> UShort, set: (Int, UShort) -> Unit, stack: Deque<UShort>) =
                get(0).toInt()
        },
        JT(7, 2) {
            override fun getOffset(offset: Int, get: (Int) -> UShort, set: (Int, UShort) -> Unit, stack: Deque<UShort>) =
                if (get(0) != 0.toUShort()) get(1).toInt() else super.getOffset(offset, get, set, stack)
        },
        JF(8, 2) {
            override fun getOffset(offset: Int, get: (Int) -> UShort, set: (Int, UShort) -> Unit, stack: Deque<UShort>) =
                if (get(0) == 0.toUShort()) get(1).toInt() else super.getOffset(offset, get, set, stack)
        },
        ADD(9, 3, { _, get, set, _, _ -> set(0, (get(1).toInt() + get(2).toInt()).mod(32768).toUShort()) }),
        MULT(10, 3, { _, get, set, _, _ -> set(0, (get(1).toInt() * get(2).toInt()).mod(32768).toUShort()) }),
        MOD(11, 3, { _, get, set, _, _ -> set(0, get(1).toInt().mod(get(2).toInt()).mod(32768).toUShort()) }),
        AND(12, 3, { _, get, set, _, _ -> set(0, get(1) and get(2)) }),
        OR(13, 3, { _, get, set, _, _ -> set(0, get(1) or get(2)) }),
        NOT(14, 2, { _, get, set, _, _ -> set(0, get(1).inv() xor (Short.MAX_VALUE + 1).toUShort()) }),
        RMEM(15, 2, { offset, get, set, _, _ -> set(0, get(get(1).toInt() - offset - 1)) }),
        WMEM(16, 2, { _, get, _, setDirect, _ -> setDirect(get(0).toInt(), get(1)) }),
        CALL(17, 1, { offset, _, _, _, stack -> stack.add((offset + 2).toUShort()) }) {
            override fun getOffset(offset: Int, get: (Int) -> UShort, set: (Int, UShort) -> Unit, stack: Deque<UShort>) =
                get(0).toInt()
        },
        RET(18, 0) {
            override fun getOffset(offset: Int, get: (Int) -> UShort, set: (Int, UShort) -> Unit, stack: Deque<UShort>): Int {
                return if (stack.isEmpty()) -1 else stack.removeLast().toInt()
            }
        },
        OUT(19, 1, { _, get, _, _, _ -> print(get(0).toInt().toChar()) }),
        IN(20, 1),
        NOOP(21, 0);

        fun execute(registers: UShortArray, data: UShortArray, stack: Deque<UShort>, offset: Int): Int {
            val get: (Int) -> UShort = { idx ->
                val arg = data[offset + 1 + idx]
                if (arg > 32767U) {
                    registers[arg.toInt() - 32768]
                } else {
                    arg
                }
            }
            val set: (Int, UShort) -> Unit = { idx, value ->
                val arg = data[offset + 1 + idx]
                if (arg > 32767U) {
                    registers[arg.toInt() - 32768] = value
                } else {
                    data[arg.toInt()] = value
                }
            }
            val setDirect: (Int, UShort) -> Unit = { idx, value -> data[idx] = value }

            this.operation.operate(offset, get, set, setDirect, stack)
            return getOffset(offset, get, set, stack)
        }

        protected open fun getOffset(offset: Int, get: (Int) -> UShort, set: (Int, UShort) -> Unit, stack: Deque<UShort>) =
            offset + 1 + this.numArgs

        companion object {
            internal val LOOKUP_TABLE = entries.stream().collect(Collectors.toMap({ it.id }, { it }))
        }
    }

    fun interface Operation {
        fun operate(offset: Int, get: (Int) -> UShort, set: (Int, UShort) -> Unit, setDirect: (Int, UShort) -> Unit, stack: Deque<UShort>)
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
