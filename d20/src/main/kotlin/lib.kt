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