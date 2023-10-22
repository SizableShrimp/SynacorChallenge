fun main() {
    val intToCoinMap = mapOf(
        2 to "red",
        3 to "corroded",
        9 to "blue",
        7 to "concave",
        5 to "shiny"
    )
    allPermutations(intToCoinMap.keys).forEach {
        val (a, b, c, d, e) = it
        // _ + _ * _^2 + _^3 - e = 399
        if (a + b * c * c + d * d * d - e == 399) {
            it.map(intToCoinMap::get).map { s -> "use $s coin" }.forEach(::println)
            return
        }
    }
}

private fun <T> allPermutations(set: Set<T>): Set<List<T>> {
    if (set.isEmpty()) return emptySet()

    fun <T> _allPermutations(list: List<T>): Set<List<T>> {
        if (list.isEmpty()) return setOf(emptyList())

        val result: MutableSet<List<T>> = mutableSetOf()
        for (i in list.indices) {
            _allPermutations(list - list[i]).forEach {
                item -> result.add(item + list[i])
            }
        }
        return result
    }

    return _allPermutations(set.toList())
}