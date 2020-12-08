import java.beans.IntrospectionException
import java.io.File
import java.math.BigInteger


interface Debugger {
    fun inspect(p: Program, prev: Memory, cur: Memory): Boolean {
        return true
    }
}


class Memory(var aIndex: Int = 0, var aRegistry: Int = 0) {

    var mutations = 0

    var index: Int
        get() = aIndex
        set(value) {
            aIndex = value
            mutations++
        }

    var register: Int
        get() = aRegistry
        set(value) {
            aRegistry = value
        }

    fun copy(): Memory {
        return Memory(index, register)
    }
}

class Program(val ins: List<Instruction>) {
    var mem : Memory? = null


    fun run(d: Debugger) {
        val mem = Memory()
        this.mem = mem
        while (mem.index <= ins.lastIndex) {
            val prev = mem.copy()
            val current = ins[mem.index];
            mem.mutations = 0
            current.invoke(mem);
            if (mem.mutations == 0) {
                mem.index++
            }
            if (!d.inspect(this, prev, mem)) {
                break;
            }
        }
    }

    fun hasLoop(): Boolean {
        val deb = SingleInstructionDebugger()
        this.run(deb)
        return deb.hasLoop
    }
}

interface Instruction {
    fun invoke(p: Memory) {

    }

    fun toggle() : Instruction? {
        return null
    }

}

class Acum(val value: Int) : Instruction {

    override fun invoke(p: Memory) {
        p.register += value;

    }
}

class Jump(val value: Int) : Instruction {

    override fun invoke(p: Memory) {
        p.index += value;

    }
    override fun toggle(): Instruction? {
        return Nop(value)
    }
}

class Nop(val value: Int) : Instruction {

    override  fun toggle(): Instruction? {
        return Jump(value)
    }
}

class SingleInstructionDebugger : Debugger {
    var hasLoop = false
    var lastMem: Memory? = null;
    val visited = mutableMapOf<Int, Int>()
    override fun inspect(p: Program, prev: Memory, cur: Memory): Boolean {
        if (!visited.containsKey(prev.index)) {
            visited[prev.index] = 1
        } else {
            visited[prev.index] = visited[prev.index]!! + 1
        }

        if (visited[prev.index]!! > 1) {
            hasLoop = true
            lastMem = prev
            return false;
        }
        return true
    }
}


fun main(args: Array<String>) {
    val file = File("input.txt").readLines()

    fun factory(line: String): Instruction {
        if (line == null || line.isBlank()) throw Error("Line can't be blank")
        val reg = Regex("(?<name>\\w+) (?<val>(-|\\+)\\d+)")
        val matchResult = reg.matchEntire(line)!!;
        val instName = matchResult.groups["name"]!!.value;
        val argument = matchResult.groups["val"]!!.value.toInt();

        return when (instName) {
            "acc" -> Acum(argument)
            "jmp" -> Jump(argument)
            else -> Nop(argument)
        }
    }

    val instructions = file.map { factory(it) };
    var current = instructions.toMutableList();

    fun test(index: Int) {

        if (index > instructions.lastIndex)
            throw Error("Somthing is wrong")

        current = instructions.toMutableList();

        if (index >= 0) {
            val toggled = current[index].toggle()
            if (toggled != null) {
                current[index] = toggled
            }
        }

        val prog = Program(current)
        if (prog.hasLoop()) {
            return test(index + 1)
        }

        println(prog.mem!!.register);
    }

    test(0);
}