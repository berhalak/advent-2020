import java.io.File
import kotlin.Error
import kotlin.math.sqrt

class Food(aList: Iterable<String>, val al: Alergens) {

    var list = aList.sorted()

    fun print() {
        println("$this")
    }

    override fun equals(other: Any?): Boolean {
        return other is Food && other.toString().equals(toString())
    }

    fun product(food: Food): Food? {
        if (food.equals(this)) return null
        val myList = list
        val otherList = food.list
        val commonList = myList.intersect(otherList)
        if (commonList.size > 0) {
            val commonAl = al.product(food.al)
            if (commonAl != null)
                return Food(commonList, commonAl)
        }
        return null
    }

    fun ingredients() = list.size

    fun alergenns() = al.list.size

    override fun toString(): String {
        return "${list.joinToString(" ")} $al"
    }

    fun isSingle(): Boolean {
        return ingredients() == 1 && alergenns() == 1
    }

    fun removeFood(single: Food) {
        if (this.equals(single)) return

        if (single.list.any()) {
            val foodList = list.except(single.list.first()).toList()
            list = foodList
            al.remove(single.al)
        }
    }
}

class Alergens(aList: Iterable<String>) {
    var list = aList.sorted()

    override fun toString(): String {
        return "(contains ${list.joinToString(", ")})"
    }

    fun product(other: Alergens): Alergens? {
        val common = list.intersect(other.list)
        if (!common.any()) return null
        return Alergens(common)
    }

    fun remove(al: Alergens) {
        if (al.list.any())
            list = list.except(al.list.first()).toList()
    }
}

fun main() {
    val file = File("input.txt").readLines()
    val foods = file.map {
        var (left, right) = it.split(" (contains ")
        right = right.replace(")", "")
        return@map Food(left.split(" "), Alergens(right.split(", ")))
    }

    val foodMap = Map2d<String, Int>()
    for (f in foods) {
        for (i in f.list) {
            for (a in f.al.list) {
                foodMap[i][a] = (foodMap[i][a] ?: 0) + 1
            }
        }
    }

    for((food, map) in foodMap) {
        val alergen = map.first().first

    }

}

