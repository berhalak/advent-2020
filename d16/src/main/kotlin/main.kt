import java.io.File
import java.lang.Error
import java.lang.reflect.Field
import java.util.BitSet
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

class FieldDef(val text: String) {
    fun contains(n: Int): Boolean {
        val ranges = text.split(": ")[1].split(" or ")
        for (r in ranges) {
            val limits = r.split("-").map { it.toInt() }
            if (n in limits[0]..limits[1]) return true
        }
        return false
    }

    fun matchesAll(numbersAtPosition: List<Int>): Boolean {
        val ok = numbersAtPosition.all { contains(it) }
        return ok
    }

    fun name() = text.split(": ")[0]
}

class Ticket(val fields: String) {
    fun invalidNumbers(fields: List<FieldDef>): Iterable<Int> {
        val s = sequence<Int> {
            for (n in myNumbers()) {
                val hasAny = fields.any { it.contains(n) }
                if (!hasAny) {
                    yield(n)
                }
            }
        }

        return s.asIterable()
    }

    fun isValid(fields: List<FieldDef>): Boolean {
        return !invalidNumbers(fields).any()
    }

    fun take(index: Int): Int {
        return myNumbers().elementAt(index)
    }

    private fun myNumbers(): Iterable<Int> {
        return fields.split(",").map { it.toInt() }
    }

    fun fieldCount(): Int {
        return myNumbers().count()
    }
}

fun main() {

    val file = File("input.txt").readLines()

    val fields = mutableListOf<FieldDef>()
    var myTicket: Ticket? = null
    val nearby = mutableListOf<Ticket>()

    for (line in file) {
        if (line.isBlank()) break
        fields.add(FieldDef(line))
    }

    var ok = false
    for (line in file) {
        if (!ok) {
            if (line == "your ticket:") {
                ok = true;
            }
            continue
        }
        myTicket = Ticket(line)
        break
    }

    ok = false

    for (line in file) {
        if (!ok) {
            if (line == "nearby tickets:") {
                ok = true;
            }
            continue
        }

        nearby.add(Ticket(line))
    }

    val sum = nearby.flatMap { it.invalidNumbers(fields) }.sum()

    println(sum)

    nearby.add(myTicket!!)

    val valid = nearby.filter { it.isValid(fields) }

    var map = mutableMapOf<Int, MutableList<FieldDef>>()
    for (i in 0 until myTicket.fieldCount()) {
        val numbersAtPosition = valid.map { it.take(i) }

        val valid = fields.filter { it.matchesAll(numbersAtPosition) }

//        println("For $i we can have")
//        valid.forEach { println(it.name()) }

        map[i] = valid.toMutableList()
    }

    var g = 9000
    // now rotate
    while (map.values.any { it.count() > 1 }) {
        val ones = map.filter { it.value.count() == 1 }

        for (single in ones) {
            for(entry in map) {
                if (entry.key == single.key) continue
                entry.value.remove(single.value[0])
            }
        }

        if (ones.isEmpty()) {
            throw Error("No more ones")
        }

        g--
        if (g == 0) {
            throw Error("Guard")
        }
    }

    val dep = map.filter { it.value[0].name().startsWith("departure") }.map { myTicket.take(it.key)}

    val product = dep.map{ it.toULong() }.reduce { a, b -> a * b}

    println(product)
}