import java.io.File

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

    fun removeIngredients(ingNames: List<String>) {
        list = list.except(ingNames).sorted()
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

typealias CookBook = Map2d<String, Int>

fun main() {
    val file = File("input.txt").readLines()
    val foods = file.map {
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

    fun noneHas(book: CookBook) : Boolean {
        for((food, map) in book) {
            if (map.any { it.second > 0}) {
                return false
            }
        }
        return true
    }

    fun mostPropoble(book: CookBook) : Pair<String, String> {
        var current: Pair<String, String>? = null
        var max = 0

        for((food, data) in book) {
            for((ing, total) in data) {
                if (total > max) {
                    max = total
                    current = food to ing
                }
            }
        }

        return current!!
    }

    fun removeIngredient(name: String, book: CookBook) {
        for((food, data) in book) {
            data[name] = 0
        }
    }

    fun removeFood(name: String, book: CookBook) {
        book.remove(name)
    }

    fun printMap(book: CookBook) {
        for((food, data) in book) {
            for((ing, total) in data) {
                println("$food : $ing = $total")
            }
        }
    }


    printMap(foodMap)

    println("Solving")

    while(!noneHas(foodMap)) {
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

    // remove ingredients from food list now
    foods.forEach { it.removeIngredients(ingNames)}

    foods.forEach { it.print() }
}

