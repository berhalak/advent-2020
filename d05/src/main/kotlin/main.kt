import java.beans.IntrospectionException
import java.io.File
import java.math.BigInteger


fun main(args: Array<String>) {
    val file = File("input.txt").readLines();
    val numbers = mutableListOf<Int>()

    fun first(r: IntRange) : IntRange {
        if (r.last == r.first) return r;
        return r.first..(r.first + (r.last - r.first) / 2)
    }

    fun last(r: IntRange) : IntRange {
        if (r.last == r.first) return r;
        return (first(r).last + 1)..r.last;
    }

    val all = mutableListOf<Int>()


    for(line in file) {
        var row = 0..127
        var col = 0..7;
        for(sign in line) {
            when (sign) {
                'F' -> row = first(row)
                'B' -> row = last(row)
                'L' -> col = first(col)
                'R' -> col = last(col)
            }
        }
        val seatId = row.first * 8 + col.first
        all.add(seatId)
    }

    all.sort();

    // day first
    println(all.last())

    var last = 0;

    for(i in all.indices) {
        val current = all[i];
        if (last != 0 && last != current - 1) {
            // day last
            println(current - 1)
            break;
        }
        last = current;
    }
}