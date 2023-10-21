var a = 4
var b = 1
val teleporter = 1

fun innerMost() {
    if (a == 0) {
        // At its core when we first return here,
        // b will be teleporter, and a will be teleporter+1
        a = b + 1
        return
    }

    if (b == 0) {
        // We reach this if statement first
        a--
        b = teleporter
        innerMost()
        return
    }

    val temp = a
    b--
    innerMost()
    // First time we escape, b = teleporter and a = teleporter + 1
    b = a
    a = temp - 1
    innerMost()

    // What is set here at the end is what is returned
}

fun main() {
    innerMost()
    println(b)
}