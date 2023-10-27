import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.concurrent.getOrSet
import kotlin.system.measureNanoTime

suspend fun main() {
    val duration = measureNanoTime {
        TeleporterCodeSolver.main()
    }

    println("Time: ${duration / (1_000_000)} ms")
}

object TeleporterCodeSolver {
    private val threadCache = ThreadLocal<IntArray>()
    private var exit: Boolean = false

    suspend fun main() {
        coroutineScope {
            val isAPresent = 1.shl(16)
            val baseCache = IntArray(0b1000.shl(16) - 1) {
                if (it >= isAPresent)
                    return@IntArray -1

                // a is 0 here
                val b = it and 0xFFFF
                (b + 1) % 32768
            }

            for (teleporterCode in 1..<32768) {
                launch {
                    if (exit)
                        return@launch

                    // Reusing the same arrays for each thread and copying the data back in
                    // seems to save ~7 seconds on my hardware.
                    val cache = threadCache.getOrSet { IntArray(baseCache.size) }
                    System.arraycopy(baseCache, 0, cache, 0, cache.size)
                    // val cache = baseCache.copyOf()

                    val a = teleporterCalibration(4, 1, teleporterCode, cache)
                    if (a == 6) {
                        println(teleporterCode)
                        exit = true
                    }
                }
            }
        }
    }

    private fun teleporterCalibration(a: Int, b: Int, teleporterCode: Int, cache: IntArray): Int {
        if (exit)
            throw CancellationException()
        val startPair = a to b
        cache[startPair].apply {
            if (this > 0)
                return this
        }

        if (a == 0) {
            val result = (b + 1) % 32768
            cache[startPair] = result
            return result
        }

        if (b == 0) {
            val resultPair = teleporterCalibration((a - 1) % 32768, teleporterCode, teleporterCode, cache)
            cache[startPair] = resultPair
            return resultPair
        }

        var newA = teleporterCalibration(a, b - 1, teleporterCode, cache)
        newA = teleporterCalibration(a - 1, newA, teleporterCode, cache)
        cache[startPair] = newA
        return newA
    }

    private infix fun Int.to(other: Int) = this.shl(16) or other
}