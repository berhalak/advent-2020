import java.io.File
import java.math.BigInteger

interface Reductor {
    fun reduce(text: String): String
}

class Parents(val expression: Reductor) : Reductor {
    override fun reduce(text: String): String {
        var reg = Regex("""\((?<e>[^\)\(]+)\)""")
        var start = text

        while (true) {
            var m = reg.find(start)
            if (m == null) break
            val e = m.groups["e"]!!.value
            val r = expression.reduce(e)
            start = start.replaceRange(m.range, r)

        }
        return start
    }
}

class Addition : Reductor {
    override fun reduce(text: String): String {
        var reg = Regex("(?<a>\\d+)\\s+(?<op>\\+)\\s+(?<b>\\d++)")
        val m = reg.find(text)
        if (m == null) return text
        val a = m.groups["a"]!!.value.toULong()
        val b = m.groups["b"]!!.value.toULong()
        val s = m.groups["op"]!!.value
        val result = (a + b).toString()

        return text.replaceRange(m.range, result)
    }
}

class Multi : Reductor {
    override fun reduce(text: String): String {
        var reg = Regex("(?<a>\\d+)\\s+(?<op>\\*)\\s+(?<b>\\d++)")
        val m = reg.find(text)
        if (m == null) return text
        val a = m.groups["a"]!!.value.toULong()
        val b = m.groups["b"]!!.value.toULong()
        val s = m.groups["op"]!!.value
        val result = (a * b).toString()


        return text.replaceRange(m.range, result)
    }
}


class Any : Reductor {
    override fun reduce(text: String): String {
        var reg = Regex("(?<a>\\d+)\\s+(?<op>(\\+|\\*))\\s+(?<b>\\d++)")
        val m = reg.find(text)
        if (m == null) return text

        val a = m.groups["a"]!!.value.toULong()
        val b = m.groups["b"]!!.value.toULong()
        val s = m.groups["op"]!!.value
        val result = if (s == "+") (a + b).toString() else (a * b).toString()

        return text.replaceRange(m.range, result)
    }
}

typealias PipeLine = Array<Reductor>

class Expression2(val text: String) : Reductor {
    var _print = false
    override fun reduce(text: String): String {
        var start = text
        var reductors: Array<Reductor> = arrayOf(Parents(this), Addition(), Multi())
        if (_print) println(start)

        for (r in reductors) {
            while (true) {
                val reduced = r.reduce(start)
                if (reduced != start) {
                    start = reduced
                    if (_print) println(start)
                } else {
                    break
                }
            }
        }

        if (_print) println("-----")
        return start
    }

    fun print() {
        _print = true
        eval()
        _print = false
    }

    fun eval(): ULong {
        val reduced = reduce(text)
        return reduced.toULong()
    }
}


class Expression(val text: String) : Reductor {
    override fun reduce(text: String): String {
        var start = text
        var reductors: Array<Reductor> = arrayOf(Parents(this), Any())

        for (r in reductors) {
            while (true) {
                val reduced = r.reduce(start)

                if (reduced != start) {
                    start = reduced
                } else {
                    break
                }
            }
        }


        return start
    }

    fun eval(): ULong {
        return reduce(text).toULong()
    }
}

fun main() {
    val file = File("input.txt").readLines()
//
//    Expression2("(8 * 4 * 4 + 6 + (8 + 9 * 4 * 7 * 3) * 4) * 7 * 4 * 8 + (4 * 3 * 6 * 5) + 5").print()
//
//    return

    val sum = file.map { Expression(it) }.map { it.eval() }.sum()
    println(sum)

    val sum2 = file.map { Expression2(it) }.map { it.eval() }.sum()
    println(sum2)

//    val sum2 = file.map { Expression2(it) }.map { it.eval() }.sum()
//
//    var s = 0.toULong()
//    for (i in file.map { Expression2(it) }) {
//        s += i.eval()
//        println("[$s] ${i.text} = ${i.eval()}")
//    }

}