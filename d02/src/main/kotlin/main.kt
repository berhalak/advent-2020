import java.io.File

fun main(args: Array<String>) {
    val file = File("input.txt").readLines();
    val regex = Regex("""(\d+)-(\d+)\s+(\w):\s+(.+)""")

    fun part1() {
        var validPasswords = 0;

        for (line in file) {
            if (line.isEmpty()) continue;
            val match = regex.find(line)!!;
            val (fText, tText, sign, text) = match.destructured;

            val from = fText.toInt();
            val to = tText.toInt();

            val howMany = text.filter { it.equals(sign[0]) }.count();

            if (howMany in from..to) {
                validPasswords++;
            }
        }

        println("Number of valid passowords are: $validPasswords");
    }

    fun part2() {
        var validPasswords = 0;

        for (line in file) {
            if (line.isEmpty()) continue;
            val match = regex.find(line)!!;
            val (fText, tText, sign, text) = match.destructured;

            val first = fText.toInt();
            val second = tText.toInt();

            var hit = if (text[first - 1].equals(sign[0])) 1 else 0;
            hit += if (text[second - 1].equals(sign[0])) 1 else 0;

            if (hit == 1) {
                validPasswords++;
            }
        }

        println("Number of valid passowords are: $validPasswords");
    }

    part2();
}