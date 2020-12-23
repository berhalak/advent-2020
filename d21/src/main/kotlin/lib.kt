import java.beans.IntrospectionException
import java.io.File
import java.math.BigInteger

fun <T> _combine(list: List<T>): Sequence<Pair<T, T>> = sequence {
    for (i in 0..list.lastIndex - 1) {
        for (j in (i + 1) .. list.lastIndex) {
            yield(list[i] to list[j])
        }
    }
}

fun <T> List<T>.combine() : Sequence<Pair<T, T>> = _combine(this)

fun <T> Iterable<T>.except(t: T) : Iterable<T> = this.filter { !it!!.equals(t) }

fun Sequence<Long>.product() : Long {
    return this.reduce { a, b -> a * b}
}

fun List<String>.splitByEmpty() : Sequence<List<String>> {
    val lines = this
    return sequence {
        val buffer = mutableListOf<String>()
        for(line in lines) {
            if (line.isBlank()) {
                yield(buffer.toList())
                buffer.clear()
                continue
            }
            buffer.add(line)
        }
        yield(buffer.toList())
    }
}


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



class Map1d<T, K> : Iterable<Pair<T, K>> {
    private val map = mutableMapOf<T, K>()
    operator fun get(i: T): K? {
        if (map.containsKey(i)) return map[i]!!
        return null
    }

    fun clone(): Map1d<T, K> {
        val clone = Map1d<T, K>()
        for (entry in map) {
            clone.map.set(entry.key, entry.value)
        }
        return clone
    }

    operator fun set(i: T, v: K) {
        map[i] = v
    }

    override fun iterator(): Iterator<Pair<T, K>> {
        return map.map { Pair(it.key, it.value) }.iterator()
    }
}

class Map2d<T,K> : Iterable<Pair<T, Map1d<T,K>>> {
    private val map = mutableMapOf<T, Map1d<T,K>>()
    operator fun get(i: T): Map1d<T,K> {
        if (map.containsKey(i)) return map[i]!!
        map[i] = Map1d()
        return map[i]!!
    }

    fun clone(): Map2d<T,K> {
        val clone = Map2d<T,K>()
        for (entry in map) {
            clone.map.set(entry.key, entry.value.clone())
        }
        return clone
    }

    override fun iterator(): Iterator<Pair<T, Map1d<T,K>>> {
        return map.map { Pair(it.key, it.value) }.iterator()
    }
}

class Map3d<T,K> : Iterable<Pair<T, Map2d<T,K>>> {
    private val map = mutableMapOf<T, Map2d<T,K>>()
    operator fun get(i: T): Map2d<T,K> {
        if (map.containsKey(i)) return map[i]!!
        map[i] = Map2d()
        return map[i]!!
    }

    fun clone(): Map3d<T,K> {
        val clone = Map3d<T,K>()
        for (entry in map) {
            clone.map.set(entry.key, entry.value.clone())
        }
        return clone
    }

    override fun iterator(): Iterator<Pair<T, Map2d<T,K>>> {
        return map.map { Pair(it.key, it.value) }.iterator()
    }
}


class Map4d<T,K> : Iterable<Pair<T, Map3d<T,K>>> {
    private val map = mutableMapOf<T, Map3d<T,K>>()
    operator fun get(i: T): Map3d<T,K> {
        if (map.containsKey(i)) return map[i]!!
        map[i] = Map3d<T,K>()
        return map[i]!!
    }

    fun clone(): Map4d<T,K> {
        val clone = Map4d<T,K>()
        for (entry in map) {
            clone.map.set(entry.key, entry.value.clone())
        }
        return clone
    }

    override fun iterator(): Iterator<Pair<T, Map3d<T,K>>> {
        return map.map { Pair(it.key, it.value) }.iterator()
    }
}