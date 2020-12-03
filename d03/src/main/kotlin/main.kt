import java.io.File
import java.math.BigInteger

data class Cords(val dx: Int, val dy: Int) {}

fun main(args: Array<String>) {
    val file = File("input.txt").readLines();


    fun test(dx: Int, dy : Int) : BigInteger {
        var x = 0;
        var y = 0;
        var encouters = 0;

        while (y < file.size - 1) {
            x += dx;
            y += dy;
            if (y > file.size - 1) break;
            if (x >= file[y].length) {
                x -= file[y].length;
            }
            val sign = file[y][x];
            if (sign == '#') {
                encouters++;
            }
        }
        return encouters.toBigInteger();
    }

    val cords = mutableListOf(Cords(1,1), Cords(3, 1), Cords(5,1), Cords(7,1), Cords(1, 2));

    var product : BigInteger = 1.toBigInteger();

    for(c in cords) {
        product *= test(c.dx, c.dy);
    }

    println(product)

}