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


fun main(args: Array<String>) {
    val file = File("input.txt").readLines()
    val numbers = file.map { it.toLong() }
    val prem = 25

    for (i in prem..numbers.lastIndex) {
        val current = numbers[i]
        val r = i - prem..i - 1

        val sliced = numbers.slice(r)
        if (sliced.size != prem) throw Error()

        val invalid = sliced.combine().filter { it.first + it.second == current }.firstOrNull();

        if (invalid == null) {

            println("Invalid is $current")

            for(start in 0..i-1) {
                var sum = 0L;
                var max = 0L
                var min = Long.MAX_VALUE;
                for(second in start..i-1) {
                    sum += numbers[second]
                    max = Math.max(numbers[second], max)
                    min = Math.min(numbers[second], min)
                    if (sum >= current) break
                }
                if (sum == current) {
                    println("Sum in ${max + min}")
                    break;
                }
            }


            break;
        }
    }
}