import java.io.File
import java.util.BitSet


interface Instruction {
    fun runOn(memory: Memory) {

    }
}

fun String.toInt36(): Int36 {
    return Int36.fromDecimalString(this)
}

class Int36 {
    private var bits = BitSet(36)

    override fun toString(): String {
        return "${toLong()} (${toBitString()})"
    }

    companion object {
        fun fromDecimalString(text: String): Int36 {
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
                var r = Int36()
                r.bits = bits

                return r
            }
            return Int36()
        }

        fun fromBinaryString(text: String): Int36 {
            var index = 0;
            val result = Int36()
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

    operator fun plus(other: Int36): Int36 {
        val along = this.toLong()
        val blong = other.toLong()
        var result = along + blong;

        val max = 1L.shl(36) - 1;

        if (result > max) {
            result -= max
        };

        return result.toString().toInt36()
    }
}

typealias Filter = (Memory, Int36, Int36) -> Unit

class Mask(val value: String) {
    fun change(value: Int36): Int36 {
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

        return Int36.fromBinaryString(result.toString())
    }
}

fun String.removeLeading(prefix: String): String {
    var result = this

    while (result.startsWith(prefix))
        result = result.removePrefix(prefix)

    return result
}

fun String.replaceSubByMask(character: Char, sub: String): String {
    val sb = StringBuilder(this)
    var j = 0
    for(i in sb.indices) {

        if (sb[i] == character) {
            sb[i] = sub[j++]
        }
    }
    return sb.toString()
}

class MaskGenerator(val value: String) : Iterable<Mask> {

    override fun iterator(): Iterator<Mask> {
        var s = sequence<Mask> {
            val mask = value.replace("X", "F").replace("0", "X");
            val size = mask.count { it == 'F' }

            for (i in 0..1L.shl(size) - 1) {
                val iBinary = i.toString().toInt36().toBitString().substring((36 - size))

                val final = mask.replaceSubByMask('F', iBinary);
                yield(Mask(final))
            }
        }

        return s.iterator()
    }
}

class MaskInstruction(val value: String) : Instruction {
    override fun runOn(memory: Memory) {
        memory.filter { m, a, v -> m.set(a.toLong(), Mask(value).change(v)) }
    }
}

class Mask2Instruction(val value: String) : Instruction {
    override fun runOn(memory: Memory) {
        memory.filter { m, a, v -> this.make(m, a, v) }
    }

    fun make(memory: Memory, a: Int36, value: Int36) {
        val generator = MaskGenerator(this.value);


        for (mask in generator) {
            var changed = mask.change(a).toLong()
            memory.set(changed, value)
        }

    }
}

class Assignment(val address: Long, val value: Int36) : Instruction {
    override fun runOn(memory: Memory) {
        memory.assign(address, value);
    }
}

class Memory : Iterable<Int36> {
    val mem = mutableMapOf<Long, Int36>()
    var _filter: Filter? = null

    override fun iterator(): Iterator<Int36> {
        return mem.values.iterator()
    }

    fun set(a: Long, value: Int36) {
        mem[a] = value
    }

    fun assign(a: Long, value: Int36) {
        if (_filter != null) {
            _filter!!(this, a.toString().toInt36(), value)
        } else {
            set(a, value)
        }
    }

    fun filter(filter: Filter) {
        this._filter = filter
    }
}

fun main() {

    val file = File("input.txt").readLines()

    fun part1() {
        fun factory(line: String): Instruction {
            if (line.startsWith("mask = ")) {
                return MaskInstruction(line.replace("mask = ", ""))
            } else {
                val reg = Regex("""mem\[(?<a>\d+)\] = (?<val>\d+)""")
                val m = reg.matchEntire(line)!!
                return Assignment(m.groups["a"]!!.value.toLong(), m.groups["val"]!!.value.toInt36());
            }
        }

        val mem = Memory()

        val inst = file.map { factory(it) }

        for (i in inst) {
            i.runOn(mem)
        }

        val result = mem.map { it.toLong() }.sum()

        println(result.toLong())
    }


    fun part2() {

        val mem = Memory()

        fun factory(line: String): Instruction {
            if (line.startsWith("mask = ")) {
                return Mask2Instruction(line.replace("mask = ", ""))
            } else {
                val reg = Regex("""mem\[(?<a>\d+)\] = (?<val>\d+)""")
                val m = reg.matchEntire(line)!!
                return Assignment(m.groups["a"]!!.value.toLong(), m.groups["val"]!!.value.toInt36());
            }
        }

        val inst = file.map { factory(it) }

        for (i in inst) {
            i.runOn(mem)
        }

        val result = mem.map { it.toLong() }.sum()

        println(result.toLong())
    }
   // part1();
    part2();
}