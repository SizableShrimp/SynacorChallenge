import java.util.LinkedList

fun main() {
    VaultSolver.main()
}

object VaultSolver {
    private val visited = mutableMapOf<Node, Node>()
    private val toCheck = LinkedList<Node>()
    private val grid = arrayOf(
        arrayOf(
            "*",
            "4",
            "+",
            "22"
        ),
        arrayOf(
            "8",
            "*",
            "4",
            "-"
        ),
        arrayOf(
            "-",
            "11",
            "-",
            "9"
        ),
        arrayOf(
            "1",
            "*",
            "18",
            "*"
        )
    )

    private var minNode: Node? = null
    private val startPos = 0 to 3
    private val targetOrbValue = 30
    private val targetPos = 3 to 0

    fun main() {
        // Pos is (x, y)
        toCheck.add(Node(0, startPos, 22))
        val toAdd = mutableListOf<Node>()

        while (toCheck.isNotEmpty()) {
            toCheck.forEach { runNode(it, toAdd) }
            toCheck.clear()
            toCheck.addAll(toAdd)
            toAdd.clear()
        }

        val minNode = minNode!!
        val steps = ArrayList<Pair<Int, Int>>(minNode.steps)
        var node: Node? = minNode
        while (node != null) {
            steps.add(0, node.pos)
            node = node.prev
        }

        for (i in 1..<steps.size) {
            val prevPos = steps[i - 1]
            val pos = steps[i]
            val offsetPos = (pos.first - prevPos.first) to (pos.second - prevPos.second)

            println(directionNames[directions.indexOf(offsetPos)])
        }
    }

    private fun runNode(node: Node, toAdd: MutableList<Node>) {
        val validNext = getValidNextPositions(node.pos)

        validNext.forEach { newPos ->
            val op = grid[newPos.first][newPos.second]
            val newOrbValue = updateOrbValue(node, op)
            if (newOrbValue <= 0 || newOrbValue > 100)
                return@forEach

            val atTarget = newPos == targetPos
            if (atTarget && newOrbValue != targetOrbValue)
                return@forEach

            val nextOperation = if (op[0].isDigit()) null else op
            val newNode = Node(node.steps + 1, newPos, newOrbValue, nextOperation, node)
            if ((visited[newNode]?.steps ?: Int.MAX_VALUE) <= newNode.steps)
                return@forEach

            visited[newNode] = newNode
            if (atTarget) {
                if (minNode == null || newNode.steps < minNode!!.steps)
                    minNode = newNode
            } else {
                toAdd.add(newNode)
            }
        }
    }

    private fun updateOrbValue(node: Node, op: String) =
        if (node.nextOperation == null) {
            node.value
        } else {
            when (node.nextOperation[0]) {
                '+' -> node.value + op.toInt()
                '*' -> node.value * op.toInt()
                '-' -> node.value - op.toInt()
                else -> error("Should never get here")
            }
        }

    private val directions = listOf(0 to -1, 1 to 0, 0 to 1, -1 to 0)
    private val directionNames = listOf("north", "east", "south", "west")
    private val coordRange = 0..3

    private fun getValidNextPositions(pos: Pair<Int, Int>): List<Pair<Int, Int>> =
        directions
            .map { (pos.first + it.first) to (pos.second + it.second) }
            .filter { coordRange.contains(it.first) && coordRange.contains(it.second) && it != startPos }

    private data class Node(val steps: Int, val pos: Pair<Int, Int>, val value: Int, val nextOperation: String? = null, val prev: Node? = null) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Node

            if (pos != other.pos) return false
            if (value != other.value) return false
            if (nextOperation != other.nextOperation) return false

            return true
        }

        override fun hashCode(): Int {
            var result = pos.hashCode()
            result = 31 * result + value
            result = 31 * result + (nextOperation?.hashCode() ?: 0)
            return result
        }
    }
}