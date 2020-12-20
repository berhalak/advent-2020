import java.io.File
import java.lang.Error
import kotlin.math.sqrt

class Tile(val lines: List<String>) {
    fun id() : Long {
        var firstLine = lines.first()
        val number = firstLine.replace("Tile ", "").replace(":", "").toLong()
        return number
    }
}

class Place(val x: Int, val y: Int, val size: Int) {
    fun findTile(tiles: List<Tile>) {

    }

    fun isCorner() : Boolean {
        return when {
            x == 0 && y == 0 -> true
            x == 0 && y == size - 1 -> true
            x == size - 1 && y == 0 -> true
            x == size - 1 && x == size - 1 -> true
            else -> false
        }
    }

    fun tile() : Tile {

    }
}

class Image(val size: Int) : Iterable<Place> {
    override fun iterator(): Iterator<Place> {
        TODO("Not yet implemented")
    }

    fun corners() : Sequence<Tile> {
        return this.filter { it.isCorner() }.map { it.tile() }.asSequence()
    }
}

fun main() {

    val file = File("input.txt").readLines()
    val tiles = mutableListOf<Tile>()

    for (lines in file.splitByEmpty()) {
        tiles.add(Tile(lines))
    }

    // first get the size of the squere
    val squereSize = sqrt(tiles.size.toDouble()).toInt()

    var image = Image(squereSize)

    for (place in image) {
        place.findTile(tiles)
    }

    val corners = image.corners()

    val product = corners.map { it.id() }.product()

    println(product)

}