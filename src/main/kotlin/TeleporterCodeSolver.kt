fun main() {
    TeleporterCodeSolver.main()
}

object TeleporterCodeSolver {
    fun main() {
        val cache = mutableMapOf<Pair<UShort, UShort>, Pair<UShort, UShort>>()
        for (teleporterCode in 1..32768) {
            cache.clear()
            for (a in 0..4) {
                for (b in 0..teleporterCode) {
                    teleporterCalibration(a.toUShort(), b.toUShort(), teleporterCode.toUShort(), cache)
                }
            }
            val (a) = teleporterCalibration(4.toUShort(), 1.toUShort(), teleporterCode.toUShort(), cache)
            if (a == 6U.toUShort()) {
                println(teleporterCode)
                break
            }
        }
    }

    private fun teleporterCalibration(a: UShort, b: UShort, teleporterCode: UShort, cache: MutableMap<Pair<UShort, UShort>, Pair<UShort, UShort>>): Pair<UShort, UShort> {
        val startPair = a to b
        cache[startPair]?.apply { return this }

        if (a == 0U.toUShort()) {
            val resultPair = ((b + 1U) % 32768U).toUShort() to b
            cache[startPair] = resultPair
            return resultPair
        }

        if (b == 0U.toUShort()) {
            val resultPair = teleporterCalibration(((a + 32767U) % 32768U).toUShort(), teleporterCode, teleporterCode, cache)
            cache[startPair] = resultPair
            return resultPair
        }

        var newPair = teleporterCalibration(a, ((b + 32767U) % 32768U).toUShort(), teleporterCode, cache)
        newPair = teleporterCalibration(((a + 32767U) % 32768U).toUShort(), newPair.first, teleporterCode, cache)
        cache[startPair] = newPair
        return newPair
    }
}