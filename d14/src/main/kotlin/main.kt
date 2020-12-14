import java.io.File
import java.util.BitSet
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

fun String.toInt36(): Int36 {
    return Int36.fromDecimalString(this)
}

class Int36 {
    private var bits = BitSet(36)

    override fun toString(): String {
        return "${toLong()}"
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

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Int36) return false
        return (other.toLong().equals(toLong()))
    }

    override fun hashCode(): Int {
        return bits.hashCode()
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

fun String.removeLeading(prefix: String): String {
    var result = this

    while (result.startsWith(prefix))
        result = result.removePrefix(prefix)

    return result
}
fun String.replaceSubByMask(character: Char, sub: String): String {
    val sb = StringBuilder(this)
    var j = 0
    for (i in sb.indices) {

        if (sb[i] == character) {
            sb[i] = sub[j++]
        }
    }
    return sb.toString()
}

data class MaskInstruction(val value: String);
data class AssignmentInstruction(val address: Int36, val value: Int36)

interface Memory {
    fun set(a: Int36, value: Int36)
}

class RawMemory : Memory, Iterable<Int36> {
    val mem = mutableMapOf<Int36, Int36>()

    override fun iterator(): Iterator<Int36> {
        return mem.values.iterator()
    }

    override fun set(a: Int36, value: Int36) {
        mem[a] = value
    }
}

class MaskedMemory(val inner: Memory, val mask: Mask) : Memory {
    override fun set(a: Int36, value: Int36) {
        inner.set(a, mask.change(value))
    }
}

class FlickerMemory(val inner: Memory, val mask: Mask) : Memory {
    override fun set(a: Int36, value: Int36) {
        val generator = MaskGenerator(mask.value);
        for (mask in generator) {
            var changed = mask.change(a)
            inner.set(changed, value)
        }
    }
}

fun main() {

    val file = File("input.txt").readLines()

    fun factory(line: String): Any {
        if (line.startsWith("mask = ")) {
            return MaskInstruction(line.replace("mask = ", ""))
        } else {
            val reg = Regex("""mem\[(?<a>\d+)\] = (?<val>\d+)""")
            val m = reg.matchEntire(line)!!
            return AssignmentInstruction(m.groups["a"]!!.value.toInt36(), m.groups["val"]!!.value.toInt36());
        }
    }

    fun part1() {

        val mem = RawMemory()
        var current : Memory = mem;
        val inst = file.map { factory(it) }

        for (i in inst) {
            if (i is MaskInstruction) {
                current = MaskedMemory(mem, Mask(i.value))
            } else if (i is AssignmentInstruction) {
                current.set(i.address, i.value)
            }
        }

        val result = mem.map { it.toLong() }.sum()

        println(result.toLong())
    }


    fun part2() {

        val mem = RawMemory()
        var current : Memory = mem;
        val inst = file.map { factory(it) }

        for (i in inst) {
            if (i is MaskInstruction) {
                current = FlickerMemory(mem, Mask(i.value))
            } else if (i is AssignmentInstruction) {
                current.set(i.address, i.value)
            }
        }

        val result = mem.map { it.toLong() }.sum()

        println(result.toLong())
    }
    part1();
    part2();
}