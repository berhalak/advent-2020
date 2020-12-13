import java.io.File
import kotlin.concurrent.thread
import kotlin.math.abs

class Bus(val id: Int, val index: Long = 0) {
    var _time = -1L
    var _dep = false
    var last = 0L

    fun tick(time: Long = -1) {
        _time++

        if (time >= 0) _time = time

        if (_time % id.toLong() == 0L) {
            _dep = true
        } else {
            if (_dep) last = 0
            _dep = false;
            last++
        }
    }

    fun departed(): Boolean {
        return _dep
    }

    fun willDepart(index: Long): Boolean {
        return (_time + index) % id == 0L
    }
}

class Inc {
    var time = 0L
    val map = mutableMapOf<String, Long>()
    val done = mutableSetOf<String>()
    var max = 1L

    fun next(): Long {
        time += max

        return time
    }

    fun inform(a: Bus, b: Bus, t: Long) {
        val key = "${a.index}_${b.index}"
        if (done.contains(key)) return
        if (map.containsKey(key)) {
            val delta = t - map[key]!!
            val nMax = maxOf(max, delta)
            if (max != nMax) {
                println("Changing gear to $nMax")
            }
            max = nMax
            done.add(key)
        } else {
            map[key] = t;
        }
    }
}

fun main() {

    val file = File("input.txt").readLines()

    fun test1() {
        val timestamp = file[0].toInt()

        val busesIds = file[1].split(",").filter { it != "x" }.map { it.toInt() }

        val buses = busesIds.map { Bus(it) }

        var time = 0L

        while (true) {
            buses.forEach { it.tick() }
            if (time >= timestamp) {
                val departed = buses.find { it.departed() }
                if (departed != null) {
                    print((time - timestamp) * departed.id)
                    return
                }
            }
            time++;

        }
    }


    fun test2() {
        val busesIds = file[1].split(",").mapIndexed { n, x -> n to x }
        val buses = busesIds.filter { it.second != "x" }.map { Bus(it.second.toInt(), it.first.toLong()) }

        var time = 0L;

        val inc = Inc()

        var start = 0;

        while (true) {

            buses.forEach { it.tick(time) }

            var start = 0;
            while (start < buses.lastIndex) {
                val first = buses[start]
                val next = buses[start + 1];
                if (first.willDepart(first.index) && next.willDepart(next.index)) {
                    inc.inform(first, next, time)
                    start++
                } else {
                    break
                }
            }

            if (start == buses.lastIndex) {
                println(time)
                break
            }


            time = inc.next()
        }
    }

    test2();

}