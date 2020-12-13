import java.io.File

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

class Transmission {
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

        // in 17, x, 13, 19
        // first we will start moving by 1
        // but after some t, 1st and 3rd bus will arrive as we want (with 2 minutes delay)
        // this situation will be happening in cycles
        // so after it will occur second time, we will now how big is this cycle
        // and we can accelerate
        // and actually we can test now the situation when 3 buses arrive as we want
        // calculate the cycle, and accelerate
        val inc = Transmission()

        var start = 0;

        var counter = -1L

        while (true) {

            counter++

            buses.forEach { it.tick(time) }

            // inform transmission that two gears are next to each other
            // so we can accelerate
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

            // if all buses will depart along their index
            if (start == buses.lastIndex) {
                println("After $time, calculated in $counter loops")
                break
            }


            time = inc.next()
        }
    }

    test2();

}