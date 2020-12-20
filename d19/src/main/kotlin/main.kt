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
    val zeroRule = table[0]!!

    cache.clear()
  //  println("Testing $msg")

    return test(zeroRule.body(), msg)
}

val cache = mutableMapOf<String, Boolean>()

fun test(ruleBody: String, msg: String): Boolean {

    if (msg.isBlank()) return false
    if (ruleBody.isBlank()) return false

    val key = ruleBody + "_" + msg

    if (cache.containsKey(key)) return cache[key]!!

    if (ruleBody.startsWith("\"")) {
        val content = ruleBody.replace("\"", "")
        val result = msg == content
        cache[key] = result
        return result
    }

    if (ruleBody.contains("|")) {
        val parts = ruleBody.split(" | ")
        val result = parts.any { test(it, msg) }
        cache[key] = result
        return result
    }

    if (ruleBody.contains(" ")) {
        val rules = ruleBody.split(" ")
        for(i in 1 until msg.length) {
            val first = msg.dropLast(i)
            val second = msg.drop(msg.length - i)
            val firstRule = rules[0]
            val restRule = rules.drop(1).joinToString(" ")
            if (test(firstRule, first) && test(restRule, second)) {
                cache[key] = true
                return true
            }
        }
        cache[key] = false
        return false
    }

    val refRule = table[ruleBody.toInt()]!!
    return test(refRule.body(), msg)
}

val table = mutableMapOf<Int, Rule>()

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
        rules.forEach { table[it.id()] = it }
        val valid = messages.filter { isValid(it) }.count()

        println("Valid messages are $valid")
    }

    part(::Rule)
    part(::Rule2)
}