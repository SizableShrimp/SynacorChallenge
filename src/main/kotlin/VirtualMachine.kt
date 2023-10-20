import java.util.Deque
import java.util.LinkedList
import java.util.stream.Collectors

@OptIn(ExperimentalUnsignedTypes::class)
abstract class VirtualMachine(private val data: UShortArray) {
    internal var offset: Int = 0
        private set
    private val registers: UShortArray = UShortArray(8)
    internal val stack: Deque<UShort> = LinkedList()
    private val inQueue: Deque<Char> = LinkedList()
    var paused: Boolean = false

    constructor(data: ByteArray) : this(getUShortArray(data))

    protected fun <T : VirtualMachine> copy(factory: (UShortArray) -> T): T {
        val vmCopy = factory(this.data.copyOf())
        vmCopy.offset = this.offset
        this.registers.forEachIndexed { idx, value -> vmCopy.registers[idx] = value }
        vmCopy.stack.addAll(this.stack)
        vmCopy.inQueue.addAll(this.inQueue)
        return vmCopy
    }

    /**
     * @return `true` if the program halted, `false` if the program was paused
     */
    fun run(): Boolean {
        this.paused = false

        while (this.offset < this.data.size) {
            val opcode = Opcode.LOOKUP_TABLE.getValue(this.data[this.offset].toByte())
            if (opcode == Opcode.HALT)
                return true

            this.offset = opcode.execute(this)
            if (this.paused)
                break
            if (this.offset == -1)
                return true
        }

        return false
    }

    internal fun set(idx: Int, value: UShort, relative: Boolean = true) {
        if (!relative) {
            this.data[idx] = value
            return
        }

        val arg = this.data[this.offset + 1 + idx]
        if (arg > 32767U) {
            this.registers[arg.toInt() - 32768] = value
        } else {
            error("I don't think we want this behavior!")
            // this.data[arg.toInt()] = value
        }
    }

    internal fun get(idx: Int, relative: Boolean = true): UShort {
        val arg = this.data[idx + if (relative) (1 + this.offset) else 0]
        return if (arg > 32767U) {
            this.registers[arg.toInt() - 32768]
        } else {
            arg
        }
    }

    protected abstract fun printChar(char: Char)

    protected abstract fun readLine(): String?

    internal fun queryCharIn(): UShort? {
        if (this.inQueue.isEmpty()) {
            val line = this.readLine() ?: return null.apply { this@VirtualMachine.paused = true }
            this.inQueue.addAll(line.toList())
            this.inQueue.add('\n')
        }

        return this.inQueue.removeFirst().code.toUShort()
    }

    enum class Opcode(private val id: Byte, private val numArgs: Int, private val operation: VirtualMachine.() -> Unit = { }) {
        HALT(0, 0),
        SET(1, 2, { set(0, get(1)) }),
        PUSH(2, 1, { this.stack.add(get(0)) }),
        POP(3, 1, {
            check(this.stack.isNotEmpty())
            set(0, this.stack.removeLast())
        }),
        EQ(4, 3, { set(0, if (get(1) == get(2)) 1.toUShort() else 0.toUShort()) }),
        GT(5, 3, { set(0, if (get(1) > get(2)) 1.toUShort() else 0.toUShort()) }),
        JMP(6, 1) {
            override fun getOffset(vm: VirtualMachine) = vm.get(0).toInt()
        },
        JT(7, 2) {
            override fun getOffset(vm: VirtualMachine) =
                if (vm.get(0) != 0.toUShort()) vm.get(1).toInt() else super.getOffset(vm)
        },
        JF(8, 2) {
            override fun getOffset(vm: VirtualMachine) =
                if (vm.get(0) == 0.toUShort()) vm.get(1).toInt() else super.getOffset(vm)
        },
        ADD(9, 3, { set(0, ((get(1).toInt() + get(2).toInt()) % 32768).toUShort()) }),
        MULT(10, 3, { set(0, ((get(1).toInt() * get(2).toInt()) % 32768).toUShort()) }),
        MOD(11, 3, { set(0, ((get(1).toInt() % get(2).toInt()) % 32768).toUShort()) }),
        AND(12, 3, { set(0, get(1) and get(2)) }),
        OR(13, 3, { set(0, get(1) or get(2)) }),
        NOT(14, 2, { set(0, get(1).inv().toUInt().shl(17).shr(17).toUShort()) }),
        RMEM(15, 2, { set(0, get(get(1).toInt(), relative = false)) }),
        WMEM(16, 2, { set(get(0).toInt(), get(1), relative = false) }),
        CALL(17, 1, { stack.add((offset + 2).toUShort()) }) {
            override fun getOffset(vm: VirtualMachine) = vm.get(0).toInt()
        },
        RET(18, 0) {
            override fun getOffset(vm: VirtualMachine): Int {
                return if (vm.stack.isEmpty()) -1 else vm.stack.removeLast().toInt()
            }
        },
        OUT(19, 1, { printChar(get(0).toInt().toChar()) }),
        IN(20, 1, {
            val char = queryCharIn()
            if (char != null)
                set(0, char)
        }),
        NOOP(21, 0);

        fun execute(vm: VirtualMachine): Int {
            this.operation(vm)

            return if (vm.paused) vm.offset else this.getOffset(vm)
        }

        protected open fun getOffset(vm: VirtualMachine) = vm.offset + 1 + this.numArgs

        companion object {
            internal val LOOKUP_TABLE = entries.stream().collect(Collectors.toMap({ it.id }, { it }))
        }
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
