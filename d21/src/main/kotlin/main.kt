import java.io.File
import kotlin.math.sin

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

    fun removeIngredients(ingNames: List<String>) {
        list = list.except(ingNames).sorted()
    }

    fun foodList(): String {
        return list.sorted().joinToString(",")
    }

    fun empty(): Boolean {
        return ingredients() == 0 || alergenns() == 0
    }
}

class Alergens(aList: Iterable<String>) {
    var list = aList.sorted()

    override fun toString(): String {
        return "(contains ${list.joinToString(", ")})"
    }

    fun product(other: Alergens): Alergens {
        val common = list.intersect(other.list)
        return Alergens(common)
    }

    fun remove(al: Alergens) {
        if (al.list.any())
            list = list.except(al.list.first()).toList()
    }
}

typealias CookBook = Map2d<String, Int>

fun main() {
    val file = File("input.txt").readLines()
    var foods = file.map {
        var (left, right) = it.split(" (contains ")
        right = right.replace(")", "")
        return@map Food(left.split(" "), Alergens(right.split(", ")))
    }

    val foodMap = CookBook()
    for (f in foods) {
        for (i in f.list) {
            for (a in f.al.list) {
                foodMap[i][a] = (foodMap[i][a] ?: 0) + 1
            }
        }
    }

    fun noneHas(book: CookBook): Boolean {
        for ((food, map) in book) {
            if (map.any { it.second > 0 }) {
                return false
            }
        }
        return true
    }

    fun mostPropoble(book: CookBook): Pair<String, String> {
        var current: Pair<String, String>? = null
        var max = 0

        for ((food, data) in book) {
            for ((ing, total) in data) {
                if (total > max) {
                    max = total
                    current = food to ing
                }
            }
        }

        return current!!
    }

    fun removeIngredient(name: String, book: CookBook) {
        for ((food, data) in book) {
            data[name] = 0
        }
    }

    fun removeFood(name: String, book: CookBook) {
        book.remove(name)
    }

    fun printMap(book: CookBook) {
        for ((food, data) in book) {
            for ((ing, total) in data) {
                println("$food : $ing = $total")
            }
        }
    }


    printMap(foodMap)

    println("Solving")

    while (!noneHas(foodMap)) {
        // take the most propoble
        val (food, ingredient) = mostPropoble(foodMap);
        removeIngredient(ingredient, foodMap)
        removeFood(food, foodMap)
    }

    val ingNames = foodMap.map { it.first }.distinct()

    val counts = ingNames.map { ing ->
        ing to foods.filter { it.list.contains(ing) }.count()
    }

    val total = counts.map { it.second }.sum()

    println(total)

    println("Removing $ingNames")

    // remove ingredients from food list now
    foods.forEach { it.removeIngredients(ingNames) }

    println("Solving part 2")


    foods.forEach { it.print() }

    var result = mutableListOf<Food>()

    while (foods.any()) {
        start@ for (i in 0..foods.lastIndex - 1) {
            var c = foods[i]
            for (j in i + 1..foods.lastIndex) {
                val z = c.product(foods[j])
                if (z == null) {
                    break
                } else if (z.alergenns() > 0) {
                    c = z!!
                }
                if (c.isSingle()) {
                    println(c)
                    foods.forEach { it.removeFood(c) }
                    foods = foods.filter { !it.empty() }
                    result.add(c)
                    break@start
                }
            }
        }

        foods = foods.distinctBy { it.toString() }
        for (c in foods.filter { it.isSingle() }) {
            println(c)
            foods = foods.filter { c != it }
            foods.forEach { it.removeFood(c) }
            result.add(c)
        }
    }

    val resultList = result.distinctBy { it.toString() }.toMutableList().sortedBy { it.al.list[0] }

    println(resultList.map { it.foodList() }.joinToString(","))
}

