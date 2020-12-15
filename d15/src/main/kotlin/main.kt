import java.io.File
import java.util.BitSet
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType


fun main() {

    val file = File("input.txt").readLines()

    fun test(max: Int) : Int {

        val numbers = file[0].split(",").map { it.toInt() }.toList()

        var index = numbers.lastIndex
        val indexes = mutableMapOf<Int, Int>()
        numbers.forEachIndexed { i, x -> indexes[x] = i }
        indexes.remove(numbers.last())

        var last = numbers.last()

        fun decide(value: Int): Int {
            if (indexes.containsKey(value))  return index - indexes[value]!!
            return 0
        }

        while (index + 1 < max) {
            val prev = last
            val lastId = index
            last = decide(last)
            index++
            indexes[prev] = lastId
        }

        return last
    }

    println("Part1 ${test(2020)}")
    println("Part2 ${test(30_000_000)}")
}