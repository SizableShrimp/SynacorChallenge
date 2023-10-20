import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    val data = Files.readAllBytes(Paths.get("challenge.bin"))
    val pathFinder = AdventurePathFinder(data)

    pathFinder.run()
}