import java.io.File
import java.math.BigInteger

class Passport(val lines : Array<String>) {
    fun isValid() : Boolean {
        val joined = lines.joinToString(" ");
        val pairs = joined.split(" ");

        val keys = arrayOf("byr", "iyr", "eyr", "hgt", "hcl", "ecl", "pid", "cid");
        val optional = arrayOf("cid");

        fun validateHeight(value: String) : Boolean {
            if (!value.matches(Regex("\\d+(cm|in)"))) return false;
            if (value.endsWith("cm")) {
                return value.replace("cm", "").toInt() in 150..193
            } else {
                return value.replace("in", "").toInt() in 59..76
            }
        }

        main@for(field in keys) {
            if (field in optional) continue;

            for(pair in pairs) {
                val (key, value) = pair.split(":");
                if (key.equals(field)) {

                    val ok = when (key) {
                        "byr" -> value.matches(Regex("""\d{4}""")) && value.toInt() in 1920..2002
                        "iyr" -> value.matches(Regex("""\d{4}""")) && value.toInt() in 2010..2020
                        "eyr" -> value.matches(Regex("""\d{4}""")) && value.toInt() in 2020..2030
                        "hgt" -> validateHeight(value)
                        "hcl" -> value.matches(Regex("""#[0-9a-f]{6}"""))
                        "ecl" -> value.matches(Regex("(amb|blu|brn|gry|grn|hzl|oth)"))
                        "pid" -> value.matches(Regex("\\d{9}"))
                        else -> true
                    }

                    if (ok)
                        continue@main;
                    else {
                        val ok2 = when (key) {
                            "byr" -> value.matches(Regex("""\d{4}""")) && value.toInt() in 1920..2002
                            "iyr" -> value.matches(Regex("""\d{4}""")) && value.toInt() in 2010..2020
                            "eyr" -> value.matches(Regex("""\d{4}""")) && value.toInt() in 2020..2030
                            "hgt" -> validateHeight(value)
                            "hcl" -> value.matches(Regex("""#[0-9a-f]{6}"""))
                            "ecl" -> value.matches(Regex("(amb|blu|brn|gry|hzl|oth)"))
                            "pid" -> value.matches(Regex("\\d{9}"))
                            else -> true
                        }
                    }
                }
            }

            return false;
        }

        return true;
    }
}

fun main(args: Array<String>) {
    val file = File("input.txt").readLines();

    val passports = mutableListOf<Passport>();
    val buffer = mutableListOf<String>();

    for(line in file) {
        if (line.isEmpty()) {
            passports.add(Passport(buffer.toTypedArray()));
            buffer.clear();
            continue;
        }
        buffer.add(line);
    }

    passports.add(Passport(buffer.toTypedArray()));

    val valid = passports.filter { it.isValid() }.count();

    println(valid);

}