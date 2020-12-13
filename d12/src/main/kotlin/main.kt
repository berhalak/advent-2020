import java.io.File
import kotlin.concurrent.thread
import kotlin.math.abs

open class Step(val dir: Direction, val value: Int) {
    operator fun times(delta: Int): Step {
        return Step(dir, value * delta)
    }

    fun normal(): Int {
        return when (dir) {
            Direction.WEST -> -value
            Direction.SOUTH -> -value
            else -> value
        }
    }
}

class North(value: Int) : Step(Direction.NORTH, value)

class South(value: Int) : Step(Direction.SOUTH, value)

class East(value: Int) : Step(Direction.EAST, value)

class West(value: Int) : Step(Direction.WEST, value)

class Degree(private val value: Int) {
    fun toInt(): Int {
        var normal = value
        while (normal < 0) normal += 360
        while (normal >= 360) normal -= 360
        return normal
    }
}


enum class Direction {
    NORTH,
    EAST,
    SOUTH,
    WEST;

    fun left(d: Int): Direction {
        val delta = d / 90
        var current = this.ordinal - delta
        while (current < 0) current += 4
        while (current > 3) current -= 4
        return values()[current]
    }

    fun right(d: Int): Direction {
        val delta = d / 90
        var current = this.ordinal + delta
        while (current < 0) current += 4
        while (current > 3) current -= 4
        return values()[current]
    }
}

class Action2(private val text: String) {
    fun move(
        waypoint: Vector,
        ship: Point
    ) {
        val match = Regex("(?<a>[A-Z])(?<val>\\d+)").matchEntire(text)!!

        val action = match.groups["a"]!!.value
        val delta = match.groups["val"]!!.value.toInt()

        when (action) {
            "N" -> waypoint.move(North(delta))
            "S" -> waypoint.move(South(delta))
            "E" -> waypoint.move(East(delta))
            "W" -> waypoint.move(West(delta))
            "L" -> waypoint.left(delta)
            "R" -> waypoint.right(delta)
            "F" -> ship.move(waypoint.hor() * delta).also { ship.move(waypoint.ver() * delta) }
        }
    }
}

open class Point {

    protected val position = mutableMapOf<Direction, Int>()

    constructor()

    constructor(hor: Step, ver: Step) {
        this.move(hor)
        this.move(ver)
    }

    fun ver(): Step {
        fun value(d: Direction): Int {
            if (position.containsKey(d)) return position[d]!!
            return 0
        }

        val south = value(Direction.SOUTH) - value(Direction.NORTH)

        return if (south > 0) Step(Direction.SOUTH, abs(south)) else Step(Direction.NORTH, abs(south))
    }

    fun hor(): Step {
        fun value(d: Direction): Int {
            if (position.containsKey(d)) return position[d]!!
            return 0
        }

        val east = value(Direction.EAST) - value(Direction.WEST)

        return if (east > 0) Step(Direction.EAST, abs(east)) else Step(Direction.WEST, abs(east))
    }

    override fun toString(): String {
        fun value(d: Direction): Int {
            if (position.containsKey(d)) return position[d]!!
            return 0
        }

        val south = value(Direction.SOUTH) - value(Direction.NORTH)
        val east = value(Direction.EAST) - value(Direction.WEST)

        val hor = if (east > 0) "east" else "west"
        val ver = if (south > 0) "south" else "north"

        return "$hor ${abs(east)}, $ver ${abs(south)}"
    }

    fun manhattan(): Int {
        fun value(d: Direction): Int {
            if (position.containsKey(d)) return position[d]!!
            return 0
        }

        val ver = value(Direction.SOUTH) - value(Direction.NORTH)
        val hor = value(Direction.EAST) - value(Direction.WEST)

        return abs(ver) + abs(hor)
    }

    fun move(step: Step) {
        val direction = step.dir
        val value = step.value
        if (!position.containsKey(direction)) position[direction] = 0
        position[direction] = position[direction]!! + value
    }
}

class Vector(hor: Step, ver: Step) : Point(hor, ver) {
    fun left(deg: Int) {
        right(360 - deg)
    }

    fun right(deg: Int) {
        val correct = Degree(deg).toInt()

        if (correct == 0) return

        val x = hor()
        val y = ver()

        position.clear()

        // turn right 90
        move(East(y.normal()))
        move(South(x.normal()))

        // keep until zero
        right(correct - 90)
    }
}


class Action1(private val text: String) {

    fun move(
        ship: Point,
        face: Direction
    ): Direction {
        val match = Regex("(?<a>[A-Z])(?<val>\\d+)").matchEntire(text)!!

        val action = match.groups["a"]!!.value
        val delta = match.groups["val"]!!.value.toInt()

        when (action) {
            "N" -> ship.move(North(delta))
            "S" -> ship.move(South(delta))
            "E" -> ship.move(East(delta))
            "W" -> ship.move(West(delta))
            "F" -> ship.move(Step(face, delta))
        }

        return when (action) {
            "L" -> face.left(delta)
            "R" -> face.right(delta)
            else -> face
        }
    }
}

fun main() {

    val file = File("input.txt").readLines()

    fun first() {
        val actions = file.map { Action1(it) }
        val ship = Point()
        var face = Direction.EAST
        for (a in actions) {
            face = a.move(ship, face)
        }
        val pos = ship.manhattan()
        println("First $pos")
    }

    fun second() {
        val actions = file.map { Action2(it) }
        val ship = Point()
        val waypoint = Vector(East(10), North(1))
        for (a in actions) {
            a.move(waypoint, ship)
        }
        val pos = ship.manhattan()
        println("Second $pos")
    }

    thread { first() }
    thread { second() }
}