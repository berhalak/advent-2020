import java.io.File
import kotlin.concurrent.thread
import kotlin.math.abs

class Bus(val id: Int) {
    var _time = -1
    var _dep = false

    fun tick() {
        _time++

        if (_time % id == 0) {
            _dep = true
        } else {
            _dep = false;
        }
    }

    fun departed(): Boolean {
        return _dep
    }
}

fun main() {

    val file = File("input.txt").readLines()

    val timestamp = file[0].toInt()

    val busesIds = file[1].split(",").filter { it != "x" }.map { it.toInt() }

    val buses = busesIds.map { Bus(it) }

    var time = 0

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