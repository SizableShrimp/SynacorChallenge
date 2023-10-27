import java.util.Deque
import java.util.LinkedList
import java.util.stream.Collectors

@OptIn(ExperimentalUnsignedTypes::class)
class AdventureVirtualMachine : OutputStoringVirtualMachine {
    private val mutableInv: MutableSet<String> = mutableSetOf()
    private val mutableActionHistory: MutableList<Action> = mutableListOf()
    val inv: Set<String> = this.mutableInv
    val actionHistory: List<Action> = this.mutableActionHistory
    internal val actionQueue: Deque<Action> = LinkedList()
    var currentRoom: Room? = null
        private set

    constructor(data: UShortArray) : super(data.copyOf())

    constructor(data: ByteArray) : super(data)

    override fun readLine(): String? {
        if (this.actionQueue.isEmpty())
            return null

        val action = this.actionQueue.pop()

        this.mutableActionHistory.add(action)

        return action.toString()
    }

    internal fun updateCurrentRoom() {
        // The first 9 lines of the program are always the same; skip them
        var idx = if (this.outputLines[0] == "Welcome to the Synacor OSCON 2012 Challenge!" || this.outputLines[0] == "Welcome to the Synacor Challenge!") 9 else 0

        while (this.outputLines[idx].isBlank())
            idx++

        val equalsIdx = this.outputLines.withIndex().firstOrNull { it.index >= idx && it.value.startsWith("==") }?.index ?: -1
        if (equalsIdx == -1) {
            val firstLine = this.outputLines[idx]
            val currentRoom = this.currentRoom!!
            val lastAction = this.mutableActionHistory[this.mutableActionHistory.lastIndex]
            when (lastAction.type) {
                ActionType.TAKE -> {
                    if (firstLine == "Taken.") {
                        val things = currentRoom.things.toMutableList().apply { remove(lastAction.subject!!) }
                        this.mutableInv.add(lastAction.subject!!)
                        this.currentRoom = Room(currentRoom.name, currentRoom.description, things, currentRoom.exits)
                    } else {
                        pauseDebugger()
                    }
                }
                ActionType.USE -> {
                    logInterestingThing(idx, this.outputLines.size - 3)
                    this.actionQueue.add(Action(ActionType.INV))
                    this.clearOutputLines()
                    this.run()
                    var invIdx = 0
                    while (this.outputLines[invIdx].isBlank())
                        invIdx++
                    invIdx++ // Skip "Your inventory:" line
                    this.mutableInv.clear()
                    for (i in invIdx..<this.outputLines.size) {
                        val line = this.outputLines[i]
                        if (line.startsWith("- "))
                            this.mutableInv.add(line.substring(2))
                    }
                }
                ActionType.GO -> {
                    if (firstLine != "That door is locked.") {
                        pauseDebugger()
                    }
                }
                else -> {
                    pauseDebugger()
                }
            }

            return // We did an action or something, return early
        }

        if (equalsIdx != idx) {
            // Something interesting happened when we entered this room
            logInterestingThing(idx, equalsIdx)

            idx = equalsIdx
        }

        val roomName = this.outputLines[idx++].run { this.substring(3, this.length - 3) }
        val description = mutableListOf<String>()
        var toAdd: List<String>
        do {
            toAdd = this.readUntilBlank(idx, this.outputLines)
            idx += toAdd.size + 1
            description.addAll(toAdd)

            val currentLine = this.outputLines[idx]
            if (currentLine == "Things of interest here:" || currentLine.endsWith(" exits:") || currentLine.endsWith(" exit:"))
                break
        } while (idx < this.outputLines.size)

        var things = this.readUntilBlank(idx, this.outputLines)
        idx += things.size + 1

        val exits = if (things[0] == "Things of interest here:") {
            this.readUntilBlank(idx, this.outputLines)
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

    private fun logInterestingThing(startIdx: Int, endIdx: Int) {
        // Something interesting happened, log it if we haven't already
        val interestingList = this.outputLines.subList(startIdx, endIdx).joinToString("\n")
        if (interestingThings.add(interestingList))
            println(interestingList)
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

    companion object {
        private val interestingThings: MutableSet<String> = mutableSetOf()
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
        USE;

        companion object {
            val BY_NAME = entries.stream().collect(Collectors.toMap({ it.name.lowercase() }, { it }))
        }
    }
}