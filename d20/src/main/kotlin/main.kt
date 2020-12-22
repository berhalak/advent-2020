import java.io.File
import kotlin.Error
import kotlin.math.sqrt


typealias  Array2d<T> = Array<Array<T>>

class Bitmap {

    lateinit var data: Array2d<Boolean>;

    constructor(map: Array2d<Boolean>) {
        data = Array(map.size) { Array<Boolean>(map[0].size) { false } }

        for(i in map.indices) {
            for(j in map[i].indices) {
                data[i][j] = map[i][j]
            }
        }
    }

    fun print() {
        for (r in 0 until data.size) {
            for (c in data[r]) {
                print(if (c) "#" else ".")
            }
            println()
        }
    }

    constructor(width: Int, height: Int) {
        data = Array(height) { Array<Boolean>(width) { false } }
    }

    fun pixels(): Int {
        val all = data.flatMap { it.asIterable() }.filter { it }.count()
        return all
    }

    fun clear(dRow: Int, dCol: Int, mask: Bitmap) {
        for (y in dRow until mask.height + dRow) {
            if (y > height - 1) return
            for (x in dCol until mask.width + dCol) {
                if (x > width - 1) return

                val maskY = y - dRow
                val maskX = x - dCol

                if (!mask.data[maskY][maskX]) {
                    continue
                }

                data[y][x] = false
            }
        }
    }

    fun match(dRow: Int, dCol: Int, mask: Bitmap): Boolean {
        for (y in dRow until mask.height + dRow) {
            if (y > height - 1) return false
            for (x in dCol until mask.width + dCol) {
                if (x > width - 1) return false

                val maskY = y - dRow
                val maskX = x - dCol

                if (!mask.data[maskY][maskX]) {
                    continue
                }

                if (data[y][x]) {
                    continue
                }

                return false

            }
        }

        return true;
    }

    val height
        get() = data.size

    val width
        get() = data[0].size
}

class Tile {
    val _id: Long;
    val _map: Array2d<Boolean>;

    fun size() = _map.size

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
        _map = map
    }

    constructor(id: Long, size: Int) {
        _id = id
        _map = Array<Array<Boolean>>(size, { Array<Boolean>(size, { false }) })
    }

    fun id(): Long {
        return _id
    }

    fun noBorder(): Tile {
        val clone = Tile(id(), size() - 2)

        for (row in 1 until _map.lastIndex) {
            for (col in 1 until _map.lastIndex) {
                clone._map[row - 1][col - 1] = _map[row][col]
            }
        }

        return clone
    }

    fun rotate(): Tile {
        val clone = Tile(_id, _map.size)
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

    fun swap(): Tile {
        return flip(Direction.WEST)
    }

    fun toBitmap() = Bitmap(_map)

    fun flip(dir: Direction = Direction.NORTH): Tile {
        if (dir == Direction.NORTH) {
            val result = Tile(_id, _map.size)

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

        val result = Tile(_id, _map.size)

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

    fun adjent(t: Tile): Boolean {
        for (dir in Direction.values()) {
            for (morph in t.modify()) {
                if (this.match(morph, dir)) return true
            }
        }
        return false
    }
}


class Monster {
    val printed = """
                  # 
#    ##    ##    ###
 #  #  #  #  #  #   
    """

    fun toBitmap(): Bitmap {
        val lines = printed.lines().subList(1, 4)
        val result = Bitmap(lines[0].length, 3)
        for (i in lines.indices) {
            val line = lines[i]
            for (j in line.indices) {
                val c = line[j]
                if (c == '#') result.data[i][j] = true
            }
        }
        return result
    }
}

class Image(val size: Int) {

    private val map = mutableMapOf<Position, Tile>()

    fun toBorderLessTile(): Tile {
        // get first tile
        val tileSize = map.values.first().noBorder().size()
        val memory = Array<Array<Boolean>>(size * tileSize, { Array<Boolean>(size * tileSize, { false }) })
        for (row in 0 until size) {
            for (col in 0 until size) {
                val pos = Position(col, row, this)
                val tile = look(pos).noBorder()
                for (y in tile._map.indices) {
                    for (x in tile._map[y].indices) {
                        val properY = y + row * tileSize
                        val properX = x + col * tileSize
                        memory[properY][properX] = tile._map[y][x]
                    }
                }
            }
        }

        return Tile(0, memory)
    }

    fun toTile(): Tile {
        // get first tile
        val tileSize = map.values.first().size()
        val memory = Array<Array<Boolean>>(size * tileSize, { Array<Boolean>(size * tileSize, { false }) })
        for (row in 0 until size) {
            for (col in 0 until size) {
                val pos = Position(col, row, this)
                val tile = look(pos)
                for (y in tile._map.indices) {
                    for (x in tile._map[y].indices) {
                        val properY = y + row * tileSize
                        val properX = x + col * tileSize
                        memory[properY][properX] = tile._map[y][x]
                    }
                }
            }
        }

        return Tile(0, memory)
    }

    fun printIds() {
        for (row in 0 until size) {
            for (col in 0 until size) {
                val pos = Position(col, row, this)
                if (has(pos)) {
                    print(look(pos).id())
                } else {
                    print("....")
                }
                print(" ")
            }
            println()
        }
    }

    fun start(): Position {
        return Position(0, 0, this)
    }

    fun filled() = map.size

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

    fun force(x: Int, y: Int, t: Tile) {
        if (!solve(x, y, t)) throw Error("Can't solve $t in $x, $y")
    }

    fun solve(x: Int, y: Int, t: Tile): Boolean {
        val p = Position(x, y, this)
        for (face in t.modify()) {
            if (can(p, face)) {
                put(p, face)
                return true
            }
        }
        return false
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
        return x != maxIndex || y != maxIndex
    }

    fun hasPrev(): Boolean {
        return y > 0 || x > 0
    }
}

fun sort(list: List<Tile>): Sequence<Tile> {
    val result = mutableMapOf<Tile, Int>();

    for (t in list) {
        val adjent = list.filter { !it.equals(t) && it.adjent(t) }.count()
        result[t] = adjent
    }

    return result.toList().sortedBy { it.second }.map { it.first }.asSequence()
}


fun main() {

    val file = File("input.txt").readLines()
    val tiles = mutableListOf<Tile>()

    for (lines in file.splitByEmpty()) {
        tiles.add(Tile(lines))
    }

    val squereSize = sqrt(tiles.size.toDouble()).toInt()
    val img = Image(squereSize)

    val solved = solve(sort(tiles).toList(), img, img.start())

    if (solved != null) {
        println("Solved")

        println("Product is " + solved.corners().map { it.id() }.product())

        var image = solved.toBorderLessTile()

        println("Water roughnes is " + image.toBitmap().pixels())

        val m = Monster().toBitmap()
        println("Monster size is " + m.pixels())

        for (i in image.modify()) {
            val map = i.toBitmap()
            var count = 0

            for (r in 0 until map.height) {
                for (c in 0 until map.height) {
                    if (map.match(r, c, m)) {
                        map.clear(r, c, m)
                        count++
                    }
                }
            }

            if (count > 0) {
                println("Found $count monsters, water roughness is ${map.pixels()}")
                map.print()
            }
        }
    } else {
        println("Failed")
    }
}

fun solve(tiles: Iterable<Tile>, img: Image, p: Position): Image? {

    var cloned = img


    for (t in tiles) {
        if (t.id() == 1951L) {
            if (img.filled() == 0) {
                println("Started")
            }
        }
        for (dir in t.modify()) {


            // try to put puzzle in place
            if (!img.can(p, dir)) continue

            cloned = img.clone()
            cloned.put(p, dir)

            // if this is the end break
            if (!p.hasNext()) break

            // if not, put next puzzl in next place
            val solved = solve(tiles.except(t), cloned, p.next())
            if (solved != null) {
                return solved
            }
        }
    }

    if (cloned.filled() == cloned.size * cloned.size) return cloned

    return null
}

