@file:OptIn(ExperimentalUnsignedTypes::class)

import java.nio.file.Paths

fun AdventureVirtualMachine.toNode(): AdventurePathFinder.Node {
    return AdventurePathFinder.Node(this.actionHistory.size, this.inv.toSet(), this.currentRoom!!)
}

class AdventurePathFinder(private val data: ByteArray) {
    private val visited = mutableMapOf<Node, Int>()
    private val vms = mutableListOf<AdventureVirtualMachine>()

    private fun getStartingActions(): List<AdventureVirtualMachine.Action> = headquartersStartingActions.map {
        AdventureVirtualMachine.Action(
            AdventureVirtualMachine.ActionType.BY_NAME[it.substringBefore(' ')]
                ?: AdventureVirtualMachine.ActionType.GO,
            it.substringAfter(' ')
        )
    }

    fun run() {
        val startingVm = AdventureVirtualMachine(this.data)
        VirtualMachineIO.readVM(startingVm, Paths.get("headquarters.txt"))
        startingVm.registers[7] = 25_734U
        val startingActions = this.getStartingActions().toMutableList()
        var start = false
        while (startingActions.isNotEmpty()) {
            if (!start) {
                start = true
            } else {
                startingVm.actionQueue.add(startingActions.removeFirst())
            }
            startingVm.lines.clear()
            startingVm.run()
            startingVm.updateCurrentRoom()
        }
        this.vms.add(startingVm)

        while (vms.isNotEmpty()) {
            runTick()
        }

        // Place a debug point on the line below
        val b = false
    }

    private fun runTick() {
        val newVms = mutableListOf<AdventureVirtualMachine>()

        this.vms.removeIf {
            it.updateCurrentRoom()
            val node = it.toNode()

            if (this.visited.merge(node, 1, Integer::sum)!! >= 10)
                return@removeIf true

            val actions = listActions(it)
            if (actions.isEmpty())
                return@removeIf true

            if (actions.size > 1) {
                for (i in 1..<actions.size) {
                    newVms.add(it.copy().apply { this.actionQueue.add(actions[i]) })
                }
            }
            // We have to add this after so the action queue isn't improperly copied
            it.actionQueue.add(actions[0])

            false
        }

        this.vms.addAll(newVms)

        this.vms.removeIf { it.run() }
    }

    private fun listActions(vm: AdventureVirtualMachine): List<AdventureVirtualMachine.Action> {
        val currentRoom = vm.currentRoom!!
        val actions = mutableListOf<AdventureVirtualMachine.Action>()

        // look
        //   You may merely 'look' to examine the room, or you may 'look <subject>' (such as 'look chair') to examine something specific.
        // go
        //   You may 'go <exit>' to travel in that direction (such as 'go west'), or you may merely '<exit>' (such as 'west').
        // inv
        //   To see the contents of your inventory, merely 'inv'.
        // take
        //   You may 'take <item>' (such as 'take large rock').
        // drop
        //   To drop something in your inventory, you may 'drop <item>'.
        // use
        //   You may activate or otherwise apply an item with 'use <item>'.

        if (currentRoom.exits.isNotEmpty()) {
            currentRoom.exits.forEach {
                actions.add(AdventureVirtualMachine.Action(AdventureVirtualMachine.ActionType.GO, it))
            }
        }

        if (currentRoom.things.isNotEmpty()) {
            currentRoom.things.forEach { actions.add(AdventureVirtualMachine.Action(AdventureVirtualMachine.ActionType.TAKE, it)) }
        }

        // TODO Possibly implement LOOK and DROP
        if (vm.inv.isNotEmpty()) {
            vm.inv.forEach { actions.add(AdventureVirtualMachine.Action(AdventureVirtualMachine.ActionType.USE, it)) }
        }

        vm.lines.clear()

        return actions
    }

    companion object {
        internal val startingActions: List<String> = listOf(
            "take tablet",
            "doorway",
            "north",
            "north",
            "bridge",
            "continue",
            "down",
            "east",
            "take empty lantern",
            "west",
            "west",
            "passage",
            // Grue time
            // "darkness", // We need the lantern for this one
            "ladder",
            // To get to the can
            "west",
            "south",
            "north",
            "take can",
            "use can", // Fill the lantern with oil
            "use lantern", // Light the lantern
            "west",
            // Go back to ladder and go up it
            "ladder",
            // Go through darkness with lit lantern
            "darkness",
            "continue",
            "west",
            "west",
            "west",
            "west", // Entrance to Ruins
            "north", // Main Foyer
            "take red coin",
            "north", // Ruins Central Hall
            "west",
            "take blue coin",
            "up",
            "take shiny coin",
            "down",
            "east",
            "east",
            "take concave coin",
            "down",
            "take corroded coin",
            "up",
            "west",
            // Put in coins in Monument in Ruins Central Hall
            "use blue coin",
            "use red coin",
            "use shiny coin",
            "use concave coin",
            "use corroded coin",
            // Go through unlocked Foyer door
            "north",
            "take teleporter",
            "use teleporter"
        )
        internal val headquartersStartingActions: List<String> = listOf(
            "use teleporter"
        )
    }

    data class Node(val steps: Int, val inv: Set<String>, val currentRoom: AdventureVirtualMachine.Room) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Node

            if (inv != other.inv) return false
            if (currentRoom != other.currentRoom) return false

            return true
        }

        override fun hashCode(): Int {
            var result = inv.hashCode()
            result = 31 * result + currentRoom.hashCode()
            return result
        }
    }
}