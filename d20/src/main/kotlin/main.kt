import java.io.File
import kotlin.Error
import kotlin.math.sqrt


typealias  Array2d<T> = Array<Array<T>>

class Tile {
    val _id: Long;
    val _map: Array2d<Boolean>;

    override fun toString(): String {
        return id().toString()
    }

    constructor(lines: List<String>) {
        var firstLine = lines.first()
        val number = firstLine.replace("Tile ", "").replace(":", "").toLong()
        _id = number
        _map = Array<Array<Boolean>>(
            lines[1].length,
            { y -> Array<Boolean>(lines[1].length, { x -> lines[y + 1][x] == '#' }) })
    }

    constructor(id: Long, map: Array2d<Boolean>) {
        _id = id
        val size = map.size
        _map = Array<Array<Boolean>>(size, { Array<Boolean>(size, { false }) })

    }


    fun id(): Long {
        return _id
    }

    fun rotate(): Tile {

        val clone = Tile(_id, _map)

        val cloned = clone._map
        val orig = this._map

        val size = cloned.size

        for (row in cloned.indices) {
            for (column in cloned[row].indices) {
                val value = orig[row][column]
                cloned[column][size - row - 1] = value
            }
        }

        return clone
    }

    fun row(n: Int): Array<Boolean> {
        return if (n >= 0) _map[n] else _map[_map.size + n]
    }

    fun column(n: Int): Array<Boolean> {
        val a = Array<Boolean>(_map.size, { false })

        var z = n
        if (z < 0) z = _map.size + z

        for (rowIndex in _map.indices) {
            a[rowIndex] = _map[rowIndex][z]
        }

        return a
    }


    fun match(other: Tile, dir: Direction): Boolean {
        if (dir == Direction.NORTH) {
            return row(0) contentEquals other.row(-1)
        }
        if (dir == Direction.SOUTH) {
            return row(-1) contentEquals other.row(0)
        }
        if (dir == Direction.WEST) {
            return column(0) contentEquals other.column(-1)
        }

        if (dir == Direction.EAST) {
            return column(-1) contentEquals other.column(0)
        }

        return false
    }

    fun flip(dir: Direction): Tile {
        if (dir == Direction.NORTH) {
            val result = Tile(_id, _map)

            var from = _map
            var to = result._map
            val size = from.size

            for (rowIndex in from.indices) {
                for (colIndex in from[rowIndex].indices) {
                    to[size - rowIndex - 1][colIndex] = from[rowIndex][colIndex]
                }
            }

            return result
        }

        val result = Tile(_id, _map)

        var from = _map
        var to = result._map
        val size = from.size

        for (rowIndex in from.indices) {
            for (colIndex in from[rowIndex].indices) {
                to[rowIndex][size - colIndex - 1] = from[rowIndex][colIndex]
            }
        }
        return result
    }

    fun sides(): Iterable<Tile> {
        var last = this
        return sequence<Tile> {
            yield(last)
            yield(last.flip(Direction.NORTH))
            yield(last.flip(Direction.WEST))

        }.asIterable()
    }

    fun modify(): Iterable<Tile> {
        val self = this
        val seq = sequence<Tile> {
            for (side in self.sides()) {
                for (dir in side.directions()) {
                    yield(dir)
                }
            }
        }
        return seq.asIterable()
    }

    fun directions(): Iterable<Tile> {
        var last = this
        return sequence<Tile> {
            yield(last)
            for (i in 1..3) {
                val next = last.rotate()
                yield(next)
                last = next
            }
        }.asIterable()
    }

    fun print() {
        for (r in 0 until _map.size) {
            for (c in row(r)) {
                print(if (c) "#" else ".")
            }
            println()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Tile) return false
        return other.id() == this.id()
    }
}

class Image(val size: Int) {

    val map = mutableMapOf<Position, Tile>()

    fun start(): Position {
        return Position(0, 0, this)
    }

    fun corners(): Sequence<Tile> {
        val self = this
        val last = size - 1

        return sequence {
            yield(map[Position(0, 0, self)]!!)
            yield(map[Position(last, 0, self)]!!)
            yield(map[Position(0, last, self)]!!)
            yield(map[Position(last, last, self)]!!)
        }
    }

    fun can(p: Position, tile: Tile): Boolean {
        // need to fit to up down left right
        for (dir in Direction.values()) {
            if (!p.can(dir)) continue
            if (!has(p.move(dir))) continue
            val tileUp = look(p.move(dir))
            if (!tile.match(tileUp, dir)) return false
        }
        return true
    }

    fun put(x: Int, y: Int, t: Tile) {
        put(Position(x, y, this), t)
    }

    fun put(p: Position, tile: Tile) {
        if (!can(p, tile)) throw Error("Can't put here")
        // need to fit to up down left right
        for (dir in Direction.values()) {
            if (!p.can(dir)) continue
            if (!has(p.move(dir))) continue
            val tileUp = look(p.move(dir))
            if (!tile.match(tileUp, dir)) return
        }
        map[p] = tile
    }

    fun has(p: Position): Boolean {
        return map.containsKey(p)
    }

    fun look(p: Position): Tile {
        return map[p]!!
    }

    fun clone(): Image {
        val c = Image(size)
        for (e in map) {
            c.map[e.key] = e.value
        }
        return c
    }

    fun placed(): Int {
        return map.size
    }

    fun id(x: Int, y: Int): Long {
        if (map.containsKey(Position(x, y, this))) {
            return map[Position(x, y, this)]!!.id()
        }
        return 0
    }
}


class Position(val x: Int, val y: Int, val i: Image) {
    fun next(): Position {
        val maxIndex = i.size - 1
        if (x == maxIndex) return Position(0, y + 1, i)
        return Position(x + 1, y, i)
    }

    override fun toString(): String {
        return "$x, $y"
    }

    override fun equals(other: Any?): Boolean {
        return other is Position && other.x == x && other.y == y
    }

    override fun hashCode(): Int {
        return (x * 19 + y).hashCode()
    }

    fun can(dir: Direction): Boolean {
        return when (dir) {
            Direction.NORTH -> y > 0
            Direction.EAST -> x < i.size - 1
            Direction.SOUTH -> y < i.size - 1
            Direction.WEST -> x > 0
        }
    }

    fun move(dir: Direction): Position {
        return when (dir) {
            Direction.NORTH -> Position(x, y - 1, i)
            Direction.EAST -> Position(x + 1, y, i)
            Direction.SOUTH -> Position(x, y + 1, i)
            Direction.WEST -> Position(x - 1, y, i)
        }
    }

    fun prev(): Position {
        if (x == 0) return Position(i.size - 1, y - 1, i)
        return Position(x - 1, y, i)
    }

    fun hasNext(): Boolean {
        val maxIndex = i.size - 1
        return x != maxIndex && y != maxIndex
    }

    fun hasPrev(): Boolean {
        return y > 0 || x > 0
    }
}


fun main() {

    val file = File("input.txt").readLines()
    val tiles = mutableListOf<Tile>()

    for (lines in file.splitByEmpty()) {
        tiles.add(Tile(lines))
    }

    var a = tiles.first()
    var b = tiles[1]

    val i = Image(3)
    b.print()

    println()
    b = b.flip(Direction.NORTH)
    b.print()
    println()
    b = b.flip(Direction.NORTH)
    b.print()
    i.put(0,0, b)
    i.put(1, 0, a)


    // first get the size of the squere
    val squereSize = sqrt(tiles.size.toDouble()).toInt()
    val img = Image(squereSize)


    val solved = solve(tiles.toList(), img, img.start())

    println("Attempts $counter")

    if (solved != null) {
        println("Solved")
        val corners = solved.corners().map { it.id() }.product()
        println(corners)
    } else {
        println("Failed")
    }
}

var counter = 0
fun solve(tiles: Iterable<Tile>, img: Image, p: Position): Image? {
    counter++

    if (img.id(0, 0) == 1951L &&
        img.id(1, 0) == 2311L &&
        img.id(2, 0) == 3079L
    ) {
        println("ok")
    }

    // if upper left corner
    for (t in tiles) {
        println("${img.placed()} ${t.id()}")
        for (dir in t.modify()) {
            // try to put puzzle in place
            if (!img.can(p, dir)) continue

            val cloned = img.clone()
            cloned.put(p, dir)

            // if this is the end break
            if (!p.hasNext()) break

            // if not, put next puzzl in next place
            val solved = solve(tiles.except(t), cloned, p.next())
            if (solved != null) {
                println("Solved")
                return solved
            }
        }
    }

    // if tiles are left
    if (tiles.any()) return null

    // no tiles, super return final image
    return img
}

