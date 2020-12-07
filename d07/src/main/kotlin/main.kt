import java.beans.IntrospectionException
import java.io.File
import java.math.BigInteger

class Rule(val text: String) {

    val color : String;
    val child : MutableMap<String, Int> = mutableMapOf<String, Int>();
    var _total = -1;

    init {
        val reg = Regex("""(?<color>\w+ \w+) bags contain (?<rest>.+)""")
        val m = reg.matchEntire(text)
        if (m == null) {
            throw Exception("error")
        }
        color = m.groups["color"]!!.value
        val rest = m.groups["rest"]!!.value.trim();

        val splited = rest.split(Regex("(,|\\.)"))

        for(part in splited) {
            if (part.trim().isBlank()) continue
            if (part.trim() == "no other bags") break;
            val r = Regex("""\s*(?<size>\d+) (?<color>\w+ \w+) bags?""")
            val subM = r.matchEntire(part.trim());
            if (subM == null) {
                break;
            }
            val subColor = subM.groups["color"]!!.value;
            val subCount = subM.groups["size"]!!.value.toInt()
            child.put(subColor, subCount)
        }
    }

    fun total(map: Map<String, Rule>) : Int {
        if (_total >= 0) return _total;

        // my total
        val myTotal = child.map { it.value }.sum();

        // total from my children
        val subTotal = child.map { map[it.key]!!.total(map) * it.value }.sum();

        _total = myTotal + subTotal;

        return _total;
    }

    fun canHold(color: String, map: Map<String, Rule>) : Boolean {
        if (child.containsKey(color)) return true;

        for(key in child.keys) {
            val subRule = map[key]
            val subCan = subRule!!.canHold(color, map);
            if (subCan) return true;
        }

        return false;
    }
}


fun main(args: Array<String>) {
    val file = File("input.txt").readLines() + "";
    val rules = file.filter { !it.isBlank() }.map { Rule(it) }
    var map = rules.associateBy  { it.color} ;
    val sum = rules.filter { it.canHold("shiny gold", map) }.count()
    println(sum)

    val shiny = map["shiny gold"]!!
    val total = shiny.total(map);
    println(total)

}