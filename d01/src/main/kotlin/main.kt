import java.io.File

fun main(args: Array<String>) {
    val file = File("input.txt").readLines();
    val numbers = file.map { it.toBigInteger() }.toList();

    fun next(value: Int) = value + 1;

    loop@ for (i in 0..numbers.size - 3) {
        for (j in next(i)..numbers.size - 2) {
            for (z in next(j) until numbers.size) {
                val a = numbers[i];
                val b = numbers[j];
                val c = numbers[z];
                if (a + b + c == 2020.toBigInteger()) {
                    println("$a * $b * $c = ${a * b * c}")
                    break@loop
                }
            }
        }
    }
}