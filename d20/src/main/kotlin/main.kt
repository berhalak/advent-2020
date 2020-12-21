import java.io.File
import kotlin.math.sqrt


typealias  Array2d<T> = Array<Array<T>>

class Tile {
    val _id: Int;
    val _map: Array2d<Boolean>;

    constructor(lines: List<String>) {
        var firstLine = lines.first()
        val number = firstLine.replace("Tile ", "").replace(":", "").toInt()
        _id = number
        _map = Array<Array<Boolean>>(
            lines[1].length,
            { y -> Array<Boolean>(lines[1].length, { x -> lines[y + 1][x] == '#' }) })
    }

    constructor(id: Int, map: Array2d<Boolean>) {
        _id = id
        _map = map.clone()
    }


    fun id(): Int {
        return _id
    }

    fun rotate() : Tile {
        val clone = Tile(_id, _map)


        val cloned = clone._map
        val orig = this._map

        val size = map.size

        for(y in map.indices) {
            for(x in map[y].indices) {
                val source_x = x
                val source_y = y

                val dest_y = source_x
                val dest_x = size - source_y

                cloned[dest_y][size - dest_x] = orig[source_y][source_x]
            }
        }

        return clone
    }

    fun directions(): Iterable<Tile> {
        var last = this
        return sequence<Tile> {
            for (i in 1..4) {
                val next = last.rotate()
                yield(next)
                last = next
            }
        }.asIterable()
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Tile) return false
        return other.id() == this.id()
    }
}

class Image(val size: Int) {

    fun start(): Position {
        return Position(0, 0, this)
    }

    fun put(p: Position, tile: Tile): Boolean {
        if (p.hasPrev()) {
            val prevTile = pull(p.prev())

        }
    }

    fun clone(): Image {

    }
}


class Position(val x: Int, val y: Int, val i: Image) {
    fun next(): Position {
        val maxIndex = i.size - 1
        if (x == maxIndex) return Position(0, y + 1, i)
        return Position(x + 1, y, i)
    }

    fun hasNext(): Boolean {
        val maxIndex = i.size - 1
        return x != maxIndex && y != maxIndex
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
    val img = Image(squereSize)


    solve(tiles.toList(), img, img.start())
}

fun solve(tiles: Iterable<Tile>, img: Image, p: Position): Boolean {
    // if upper left corner
    for (t in tiles) {
        for (dir in t.directions()) {
            if (!img.put(p, dir)) continue
            if (!p.hasNext()) break
            if (solve(tiles.except(t), img.clone(), p.next())) {
                println("Solved")
                return true
            }
        }
    }
    if (tiles.any()) return false
    return true
}

