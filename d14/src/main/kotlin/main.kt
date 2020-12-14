import java.io.File
import java.util.*
import java.util.BitSet


interface Instruction {
    fun runOn(memory: Memory) {

    }
}

fun String.toMInt(): MInt {
    return MInt.fromDecimalString(this)
}

class MInt {
    private var bits = BitSet(36)

    companion object {
        fun fromDecimalString(text: String): MInt {
            if (!text.isBlank()) {
                var value = text.toLong();

                val bits = BitSet(36)
                var index = 0
                while (value !== 0L && index < 36) {
                    if (value % 2L !== 0L) {
                        bits.set(index)
                    }
                    ++index
                    value = value ushr 1
                }
                var r = MInt()
                r.bits = bits

                return r
            }
            return MInt()
        }

        fun fromBinaryString(text: String): MInt {
            var index = 0;
            val result = MInt()
            val bits = result.bits

            for (c in text.reversed()) {
                if (c == '1') bits.set(index)
                index++
            }

            return result
        }
    }


    fun toBitString(): String {
        val builder = StringBuilder()
        for (i in 0..35) {
            if (bits[i]) builder.append("1")
            else builder.append("0")
        }

        return builder.reverse().toString();
    }

    fun toLong(): Long {
        var r = 0L

        for (i in 0..35) {
            if (bits[i])
                r += Math.pow(2.0, i.toDouble()).toLong()
        }
        return r
    }

    operator fun plus(other: MInt): MInt {
        val r = BitSet(36);
        val along = this.toLong()
        val blong = other.toLong()
        val a = this.bits
        val b = other.bits;
        var m = false

        for (i in 0..35) {
            if (a[i] && b[i]) {
                if (m) {

                } else {
                    m = true
                }
            } else if (a[i] || b[i]) {
                if (m) {

                } else {
                    m = false
                    r[i] = true
                }
            } else {
                if (m) {
                    r[i] = true
                    m = false;
                }
            }
        }
        if (m) {
            r.clear();
            r[0] = true
        }

        val result = MInt()
        result.bits = r;
        return result
    }
}

typealias Filter = (MInt) -> MInt

class Mask(val value: String) : Instruction {
    override fun runOn(memory: Memory) {
        memory.setFilter { it -> this.change(it) }
    }

    fun change(value: MInt): MInt {
        val mask = this.value
        val numb = value.toBitString()
        val result = StringBuilder()

        for (i in mask.indices) {
            val m = mask[i]
            val v = numb[i]

            if (m == 'X') {
                result.append(v)
            } else {
                result.append(m)
            }
        }

        return MInt.fromBinaryString(result.toString())
    }
}

class Assignment(val address: Long, val value: MInt) : Instruction {
    override fun runOn(memory: Memory) {
        memory.set(address, value);
    }
}

class Memory : Iterable<MInt> {
    val mem = mutableMapOf<Long, MInt>()
    var _filter: Filter = { it }

    override fun iterator(): Iterator<MInt> {
        return mem.values.iterator()
    }

    fun set(a: Long, value: MInt) {
        mem[a] = _filter(value)
    }

    fun setFilter(filter: Filter) {
        this._filter = filter
    }

}

fun main() {

    val a = "2".toMInt()
    val b = "4".toMInt()
    val c = a + b
    val z = c.toLong()
    println(z)


    val file = File("input.txt").readLines()

    fun part1() {
        fun factory(line: String): Instruction {
            if (line.startsWith("mask = ")) {
                return Mask(line.replace("mask = ", ""))
            } else {
                val reg = Regex("""mem\[(?<a>\d+)\] = (?<val>\d+)""")
                val m = reg.matchEntire(line)!!
                return Assignment(m.groups["a"]!!.value.toLong(), m.groups["val"]!!.value.toMInt());
            }
        }

        val mem = Memory()

        val inst = file.map { factory(it) }

        for (i in inst) {
            i.runOn(mem)
        }

        val result = mem.reduce { a, b -> a + b }

        println(result.toLong())
    }

    part1();
}