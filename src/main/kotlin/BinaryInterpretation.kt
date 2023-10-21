var a = 4
var b = 1
val teleporter = 1

fun innerMost() {
    if (a == 0) {
        // At its core when we first return here,
        // b will be teleporter, and a will be teleporter+1
        a = b + 1
        return // 3
    }

    if (b == 0) {
        // We reach this if statement first
        a--
        b = teleporter
        innerMost() // 2
        return
    }

    val temp = a
    b--
    innerMost() // 1
    // First time we escape, b = teleporter and a = teleporter + 1
    b = a // Now b = a = teleporter + 1
    a = temp - 1 // Now a = original a - 1

    // When we get here, a is just our original a - 1, and b is
    // just the number of loops we have to do.
    innerMost() // 4

    // What is set here at the end is what is returned.
    // We only care about what 'a' becomes
    // I think it becomes teleporter + 1
}

fun main() {
    innerMost()
    println(b)
}