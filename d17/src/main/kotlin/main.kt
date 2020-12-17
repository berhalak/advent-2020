import java.io.File


data class Cords(val x: Int, val y: Int, val z: Int)  {


}

data class Cube(val cord: Cords, val active: Boolean = true) {

}

class Map1d : Iterable<Pair<Int, Boolean>> {
    private val map = mutableMapOf<Int, Boolean>()
    operator fun get(i: Int): Boolean {
        if (map.containsKey(i)) return map[i]!!
        return false
    }

    fun clone(): Map1d {
        val clone = Map1d()
        for (entry in map) {
            clone.map.set(entry.key, entry.value)
        }
        return clone
    }

    operator fun set(i: Int, v: Boolean) {
        map[i] = v
    }

    override fun iterator(): Iterator<Pair<Int, Boolean>> {
        return map.map { Pair(it.key, it.value) }.iterator()
    }
}

class Map2d : Iterable<Pair<Int, Map1d>> {
    private val map = mutableMapOf<Int, Map1d>()
    operator fun get(i: Int): Map1d {
        if (map.containsKey(i)) return map[i]!!
        map[i] = Map1d()
        return map[i]!!
    }

    fun clone(): Map2d {
        val clone = Map2d()
        for (entry in map) {
            clone.map.set(entry.key, entry.value.clone())
        }
        return clone
    }

    override fun iterator(): Iterator<Pair<Int, Map1d>> {
        return map.map { Pair(it.key, it.value) }.iterator()
    }
}

class Map3d : Iterable<Pair<Int, Map2d>> {
    private val map = mutableMapOf<Int, Map2d>()
    operator fun get(i: Int): Map2d {
        if (map.containsKey(i)) return map[i]!!
        map[i] = Map2d()
        return map[i]!!
    }

    fun clone(): Map3d {
        val clone = Map3d()
        for (entry in map) {
            clone.map.set(entry.key, entry.value.clone())
        }
        return clone
    }

    override fun iterator(): Iterator<Pair<Int, Map2d>> {
        return map.map { Pair(it.key, it.value) }.iterator()
    }
}

class Space(val data: Map3d = Map3d()) : Iterable<Cube> {

    fun set(c: Cube) {
        val (x, y, z) = c.cord
        data[x][y][z] = c.active
    }

    fun clone(): Space {
        return Space(this.data.clone())
    }

    fun print()  {
        val all = this.toList()
        val xrange = all.map { it.cord.x }.min()!!..all.map { it.cord.x }.max()!!
        val yrange = all.map { it.cord.y }.min()!!..all.map { it.cord.y }.max()!!
        val zrange = all.map { it.cord.z }.min()!!..all.map { it.cord.z }.max()!!

        for(z in zrange) {
            println("z = $z")
            for(y in yrange) {
                for(x in xrange) {
                    if (data[x][y][z]) print("#")
                    else print(".")
                }
                println()
            }
        }
    }

    operator fun get(c: Cords): Cube {
        val (x, y, z) = c
        return Cube(c, data[x][y][z])
    }

    fun lookAround(c: Cube, include: Boolean = false): Sequence<Cube> {
        return sequence {
            val (x, y, z) = c.cord

            for (dx in -1..1) {
                for (dy in -1..1) {
                    for (dz in -1..1) {
                        if (!include)
                            if (dx == 0 && dy == 0 && dz == 0) continue
                        val status = data[x + dx][y + dy][z + dz]
                        yield(Cube(Cords(x + dx, y + dy, z + dz), status))
                    }
                }
            }
        }
    }

    fun mutate() {
        val old = clone()
        for (cube in old.flatMap { old.lookAround(it, true) }.distinctBy { it.cord }) {

            if (cube.active) {
                val activeNeighbors = old.lookAround(cube).filter { it.active }.count()
                if (activeNeighbors in 2..3) {
                    continue
                }
                this.turnOff(cube)
            } else {
                val activeNeighbors = old.lookAround(cube).filter { it.active }.count()
                if (activeNeighbors in 3..3) {
                    this.turnOn(cube)
                }
            }
        }
    }

    private fun turnOff(cube: Cube) {
        val (x, y, z) = cube.cord
        data[x][y][z] = false
    }

    private fun turnOn(cube: Cube) {
        val (x, y, z) = cube.cord
        data[x][y][z] = true
    }

    override fun iterator(): Iterator<Cube> {
        return sequence<Cube> {
            for ((x, xdata) in data) {
                for ((y, ydata) in xdata) {
                    for ((z, value) in ydata) {
                        yield(Cube(Cords(x, y, z), value))
                    }
                }
            }
        }.iterator()
    }


}

fun main() {
    val file = File("input.txt").readLines()

    val space = Space();

    for (y in file.indices) {
        val line = file[y]
        for (x in line.indices) {
            val c = line[x]
            if (c == '#') {
                space.set(Cube(Cords(x, y, 1), true))
            }
        }
    }

    println("Before")

    space.print();

    for (i in 1..6) {
        space.mutate()

        println("After $i cycle")

    }
    space.print()
    println(space.filter { it.active }.count())
}