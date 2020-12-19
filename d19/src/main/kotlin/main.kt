import java.io.File


interface Fragment {
    fun eat(rules: List<Rule1>, message: String): String?;
}

class CharFragment(val text: String) : Fragment {
    override fun eat(rules: List<Rule1>, message: String): String? {
        if (message.startsWith(text))
            return message.substring(text.length)
        return null
    }

    override fun toString(): String {
        return text
    }
}

class ConcatFragment(val text: String) : Fragment {
    override fun eat(rules: List<Rule1>, message: String): String? {
        val ids = text.split(" ").map { it.toInt() }
        val myRules = ids.map { rules.find { r -> r.id() == it }!! }.map { it.fragment() }
        var start: String? = message
        for(r in myRules) {
            start = r.eat(rules, start!!)
            if (start == null) return null
        }
        return start
    }

    override fun toString(): String {
        return text
    }
}

class OrFragment(val nodes: Iterable<Fragment>) : Fragment {
    override fun eat(rules: List<Rule1>, message: String): String? {
        for(r in nodes.sortedByDescending { it.toString().length } ) {
            val start = r.eat(rules, message)
            if (start != null) return start
        }
        return null
    }

    override fun toString(): String {
        return nodes.map { it.toString() }.joinToString(" | ")
    }
}

class Validator(val rules: List<Rule1>, val start: Fragment) {
    fun validate(msg: String): Boolean {
        val result = start.eat(rules, msg)
        if (result != null && result.isBlank()) return true
        return false
    }
}

open class Rule1(val def: String) {
    fun id(): Int {
        return def.split(":")[0].toInt()
    }

    override fun toString(): String {
        return def
    }

    fun fragment(): Fragment {
        var text = def.split(":")[1].trim()

        if (text.startsWith("\"")) {
            return CharFragment(text.replace("\"", ""))
        } else if (text.contains('|')) {
            val parts = text.split('|').map { it.trim() }
            val fragments = parts.map { ConcatFragment(it) }
            return OrFragment(fragments)
        } else {
            return ConcatFragment(text)
        }
    }

    fun build(rules: List<Rule1>): Validator {
        return Validator(rules, fragment())
    }
}

class Rule2 : Rule1 {
    constructor(def: String) : super(when {
        def.startsWith("8:") -> "8: 42 | 42 8"
        def.startsWith("11:") -> "11: 42 31 | 42 11 31"
        else -> def
    })
}

fun main() {

    val file = File("input.txt").readLines()

    fun part(factory : (String) -> Rule1) {
        val rules = file.takeWhile { !it.isBlank() }.map { factory(it) }
        if (rules.size == file.size) {
            println("No messages")
            return
        }
        val messages = file.takeLastWhile { !it.isBlank() }.reversed().map { it }
        val validator = rules.find { it.id() == 0 }!!.build(rules)
        val valid = messages.filter { validator.validate(it) }.count()

        println("Valid messages are $valid")
    }

//    part(::Rule1)
    part(::Rule2)
}