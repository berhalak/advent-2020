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
