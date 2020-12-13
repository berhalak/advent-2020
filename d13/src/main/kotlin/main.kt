import java.io.File
import kotlin.concurrent.thread
import kotlin.math.abs

class Bus(val id: Int, val index: Long = 0) {
    var _time = -1L
    var _dep = false
    var last = 0L

    fun tick() {
        _time++

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

    fun willDepart(index: Long) : Boolean {
        return (_time + index) % id == 0L
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
        val input = file[1].split(",").mapIndexed{ n, id -> n to id}
        val buses = input.filter { it.second != "x" }.map { Bus(it.second.toInt(), it.first.toLong()) }

        var time = 0L

        while (true) {
            buses.forEach { it.tick() }

            if (buses[0].departed()) {
                // ask each if it will depart after n minutes
                val all = buses.drop(1).all { it.willDepart(it.index) }

                if (all) {
                    println(time)
                    break
                }
            }


            time++;

        }
    }

    test2()
}