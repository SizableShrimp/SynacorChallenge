import java.util.Deque
import java.util.LinkedList

@OptIn(ExperimentalUnsignedTypes::class)
class AdventureVirtualMachine : VirtualMachine {
    private val lineBuilder: StringBuilder = StringBuilder()
    internal val lines: MutableList<String> = mutableListOf()
    private val mutableInv: MutableList<String> = mutableListOf()
    private val mutableActionHistory: MutableList<Action> = mutableListOf()
    val inv: List<String> = this.mutableInv
    val actionHistory: List<Action> = this.mutableActionHistory
    internal val actionQueue: Deque<Action> = LinkedList()
    var currentRoom: Room? = null
        private set

    constructor(data: UShortArray) : super(data.copyOf())

    constructor(data: ByteArray) : super(data)

    override fun printChar(char: Char) {
        if (char == '\n') {
            this.lines.add(this.lineBuilder.toString())
            this.lineBuilder.clear()
            return
        }

        this.lineBuilder.append(char)
    }

    override fun readLine(): String? {
        if (this.actionQueue.isEmpty())
            return null

        val action = this.actionQueue.pop()

        this.mutableActionHistory.add(action)

        return action.toString()
    }

    internal fun updateCurrentRoom() {
        // The first 9 lines of the program are always the same; skip them
        var idx = if (this.lines[0] == "Welcome to the Synacor OSCON 2012 Challenge!") 9 else 0

        while (this.lines[idx].isBlank())
            idx++

        val firstLine = this.lines[idx++]
        if (!firstLine.startsWith("==")) {
            val currentRoom = this.currentRoom!!
            val lastAction = this.mutableActionHistory[this.mutableActionHistory.lastIndex]
            if (lastAction.type == ActionType.TAKE) {
                if (firstLine == "Taken.") {
                    val things = currentRoom.things.toMutableList().apply { remove(lastAction.subject!!) }
                    this.mutableInv.add(lastAction.subject!!)
                    this.currentRoom = Room(currentRoom.name, currentRoom.description, things, currentRoom.exits)
                } else {
                    val b = false
                }
            } else {
                val b = false
            }

            return // We did an action or something, return early
        }

        val roomName = firstLine.run { this.substring(3, this.length - 3) }
        val description = mutableListOf<String>()
        var toAdd: List<String>
        do {
            toAdd = this.readUntilBlank(idx, this.lines)
            idx += toAdd.size + 1
            description.addAll(toAdd)

            val currentLine = this.lines[idx]
            if (currentLine == "Things of interest here:" || currentLine.endsWith(" exits:") || currentLine.endsWith(" exit:"))
                break
        } while (idx < this.lines.size)
        if (description.isNotEmpty() && description[0] == "You are in a twisty alike of little passages, all maze.") {
            val b = false
        }
        var things = this.readUntilBlank(idx, this.lines)
        idx += things.size + 1
        val exits = if (things[0] == "Things of interest here:") {
            this.readUntilBlank(idx, this.lines)
        } else {
            val temp = things
            things = listOf()
            temp
        }.run { subList(1, this.size).map { it.substring(2) } }
        if (things.isNotEmpty())
            things = things.run { subList(1, this.size) }.map { it.substring(2) }

        this.currentRoom = Room(roomName, description.toList(), things.toList(), exits.toList())
        return
    }

    private fun readUntilBlank(index: Int = 0, lines: List<String>): List<String> {
        val result = mutableListOf<String>()

        for (i in index..<lines.size) {
            val line = lines[i]
            if (line.isBlank())
                break

            result.add(line)
        }

        return result
    }

    internal fun copy(): AdventureVirtualMachine {
        val vmCopy = this.copy { AdventureVirtualMachine(it) }
        vmCopy.mutableInv.addAll(this.mutableInv)
        vmCopy.mutableActionHistory.addAll(this.mutableActionHistory)
        vmCopy.actionQueue.addAll(this.actionQueue)
        vmCopy.currentRoom = this.currentRoom
        return vmCopy
    }

    data class Room(val name: String, val description: List<String>, val things: List<String>, val exits: List<String>)

    data class Action(val type: ActionType, val subject: String? = null) {
        override fun toString() = this.type.name.lowercase() + if (this.subject === null) "" else " ${this.subject}"
    }

    enum class ActionType {
        LOOK,
        GO,
        INV,
        TAKE,
        DROP,
        USE
    }
}