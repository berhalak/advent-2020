import java.beans.IntrospectionException
import java.io.File
import java.math.BigInteger

interface Group {
    fun add(line: String);
    fun sum() : Int;
}

class Group1 : Group {
    val hash = HashSet<Char>()

    override fun add(line: String) {
        for(c in line) {
            hash.add(c)
        }
    }

    override fun sum() : Int {
        return hash.size
    }
}

class Group2  : Group {
    val hash = HashSet<Char>()
    var initied = false

    override fun add(line: String) {
        if (!initied) {
            for(c in line) {
                hash.add(c)
            }
            initied = true
        } else {
            for(c in hash) {
                if (!line.contains(c)) {
                    hash.remove(c)
                }
            }
        }
    }

    override fun sum() : Int {
        return hash.size
    }
}


fun main(args: Array<String>) {
    val file = File("input.txt").readLines() + "";
//
//    val groups = mutableListOf<Group>()
//
//    var last = Group2()
//
//    for(line in file) {
//        if (line.isBlank()) {
//            groups.add(last);
//            last = Group2();
//            continue;
//        }
//        last.add(line)
//    }
//
//    val sum = groups.map { it.sum() }.sum();
//
//    println(sum)
}