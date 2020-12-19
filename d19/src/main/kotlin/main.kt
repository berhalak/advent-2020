import java.io.File
import java.lang.Error

open class Rule(val def: String) {
    fun id(): Int {
        return def.split(":")[0].toInt()
    }

    override fun toString(): String {
        return def
    }

    fun body(): String {
        return def.split(":")[1].trim()
    }
}

class Rule2 : Rule {
    constructor(def: String) : super(
        when {
            def.startsWith("8:") -> "8: 42 | 42 8"
            def.startsWith("11:") -> "11: 42 31 | 42 11 31"
            else -> def
        }
    )
}

fun isValid(msg: String): Boolean {
    val zeroRule = table.find { it.id() == 0 }!!

    return test(zeroRule.body(), msg)
}

fun eat(def: String, msg: String): String {

    if (def.startsWith("\"")) {
        val content = def.replace("\"", "")
        if (msg.startsWith(content)) return msg.substring(content.length)
        return msg
    }

    if (def.contains("|")) {
        val parts = def.split(" | ")
        return parts.any { test(it, msg) }
    }

    if (def.contains(" ")) {
        var parts = def.split(" ")
        var last = parts.last()
        var notLast = parts.dropLast(1)
        var start = msg
        notLast.forEach {
            start = eat(it, start)
        }
        return test(last, start)
    }

    throw Error("Shouldn't happen")
}


fun test(def: String, msg: String): Boolean {

    if (def.startsWith("\"")) {
        val content = def.replace("\"", "")
        return msg == content
    }

    if (def.contains("|")) {
        val parts = def.split(" | ")
        return parts.any { test(it, msg) }
    }

    if (def.contains(" ")) {
        var parts = def.split(" ")
        var last = parts.last()
        var notLast = parts.dropLast(1)
        var start = msg
        notLast.forEach {
            start = eat(it, start)
        }
        return test(last, start)
    }

    throw Error("Shouldn't happen")
}

val table = mutableListOf<Rule>()

fun main() {

    val file = File("input.txt").readLines()

    fun part(factory: (String) -> Rule) {
        val rules = file.takeWhile { !it.isBlank() }.map { factory(it) }
        if (rules.size == file.size) {
            println("No messages")
            return
        }
        val messages = file.takeLastWhile { !it.isBlank() }.reversed().map { it }

        table.clear()
        table.addAll(rules)

        val valid = messages.filter { isValid(it) }.count()


        println("Valid messages are $valid")
    }

//    part(::Rule1)
    part(::Rule2)
}