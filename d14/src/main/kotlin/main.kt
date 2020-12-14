import java.io.File

interface Instruction {
    fun runOn(memory: Memory) {

    }
}

class MInt(val text: String) {
    operator fun plus(b: MInt) : MInt {
        return this
    }
}

class Mask(val value: String) : Instruction {
    override fun runOn(memory: Memory) {
        memory.setFilter { it -> this.change(it) }
    }

    fun change(value: MInt) : MInt {

    }
}

class Assignment(val address: MInt, val value: MInt) : Instruction {

}

class Memory : Iterable<MInt> {
    val mem = mutableMapOf<Int, MInt>()

    override fun iterator(): Iterator<MInt> {
        return mem.values.iterator()
    }

    fun setFilter(filter: (MInt) -> MInt) {

    }

}

fun main() {

    val file = File("input.txt").readLines()

    fun part1() {
        fun factory(line: String): Instruction {
            if (line.startsWith("mask = ")) {
                return Mask(line.replace("mask = ", ""))
            } else {
                val reg = Regex("""mem\[(?<a>\d+)\] = (?<val>\d+)""")
                val m = reg.matchEntire(line)!!
                return Assignment(MInt(m.groups["a"]!!.value), MInt(m.groups["val"]!!.value));
            }
        }

        val mem = Memory()

        val inst = file.map { factory(it) }

        inst.forEach { it.runOn(mem) }

        val result = mem.reduce { a, b -> a + b }
    }

    part1();
}