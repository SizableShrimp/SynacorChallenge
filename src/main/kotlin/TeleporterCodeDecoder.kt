import java.security.MessageDigest

private val data: IntArray by lazy {
    val data = IntArray(30_000)
    data[6147] = 3
    data[6148] = 14877
    data[6149] = 20284
    data[6150] = 10068
    data[6151] = 12

    val theAlphabet = intArrayOf(
        97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108,
        109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122,
        65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90
    )
    data[25880] = theAlphabet.size
    theAlphabet.forEachIndexed { idx, char -> data[25881 + idx] = char }

    data[29255] = 18404
    data[29256] = 20284
    data[29257] = 10068
    data
}

private fun decodeSynacorCode(binaryModifier: Int, moduloAddr: Int, exitComparison: Int, copyAddr: Int) {
    val targetLength = data[6147]

    for (i in 1..targetLength) {
        data[6147 + i] = data[copyAddr + i]
    }

    var exit = false // reg[3]

    while (!exit) {
        var currLength = 0 // reg[4]
        var idx: Int // reg[5]
        var c: Int // reg[6]

        do {
            idx = (currLength % data[6147] + 6147 + 1) % 32768
            c = ((data[idx] * 5249) % 32768 + 12345) % 32768
            data[idx] = c
            c = (x(binaryModifier, c) % data[moduloAddr]) + 1

            if (c <= exitComparison)
                exit = true

            currLength++
            data[currLength + 6151] = data[c + moduloAddr]
        } while (currLength != data[6151])
    }
}

fun main() {
    for (teleporterCode in 1..32768) {
        data[6148] = 14877
        data[6149] = 20284
        data[6150] = 10068

        decodeSynacorCode(teleporterCode, 25880, 32767, 29254)

        val synacorCode = data.slice(6152..6163).map { it.toChar() }.joinToString("")
        val md5Hash = md5(synacorCode).toHex()

        if (md5Hash == "4873cf6b76f62ac7d5a53605b2535a0c") {
            println(teleporterCode)
            println(synacorCode)
            return
        }
    }
}

private fun md5(str: String): ByteArray = MessageDigest.getInstance("MD5").digest(str.toByteArray(Charsets.UTF_8))
private fun ByteArray.toHex() = joinToString(separator = "") { byte -> "%02x".format(byte) }

private fun x(zeroIn: Int, oneIn: Int) =
    ((zeroIn or oneIn) and (zeroIn and oneIn).inv()).toUInt().shl(17).shr(17).toInt()