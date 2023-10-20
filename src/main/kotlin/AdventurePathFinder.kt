fun AdventureVirtualMachine.toNode(): AdventurePathFinder.Node {
    return AdventurePathFinder.Node(this.actionHistory.size, this.inv.toList(), this.currentRoom!!)
}

class AdventurePathFinder(private val data: ByteArray) {
    private val visited = mutableMapOf<Node, Int>()
    private val vms = mutableListOf<AdventureVirtualMachine>()

    fun run() {
        val startingVm = AdventureVirtualMachine(this.data)
        startingVm.run() // Run until it asks for input
        this.vms.add(startingVm)

        while (vms.isNotEmpty()) {
            runTick()
        }

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

        // TODO Implement LOOK and USE and maybe DROP

        vm.lines.clear()

        return actions
    }

    data class Node(val steps: Int, val inv: List<String>, val currentRoom: AdventureVirtualMachine.Room) {
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