import java.io.File

val EMPTY = 1
val FLOOR = 0
val TAKEN = 2

open class Seat(val row: Int, val column: Int, val room: Room) {

    open fun adjecntLimit() = 4

    fun isEmpty(): Boolean {
        return room.at(row, column) == EMPTY;
    }

    fun shift(other: Room) {
        val seat = this;
        val copy = other

        if (seat.isEmpty() && seat.adjectCount() == 0) {
            seat.makeOcupy(copy)
        } else if (seat.isOccupied() && seat.adjectCount() >= adjecntLimit()) {
            seat.makeEmpty(copy)
        }
    }

    open fun adjectCount(): Int {
        fun at(row: Int, column: Int) : Int {
            if (row < 0) return 0
            if (column < 0) return 0
            if (row > room.rows.lastIndex) return 0
            if (column > room.rows[0].lastIndex) return 0
            return if (room.at(row,column) == TAKEN) 1 else 0;
        }

        val count = at(row - 1, column - 1) + at(row - 1, column) + at(row - 1, column + 1) +
                    at(row, column - 1) + at(row, column + 1) +
                    at(row + 1, column - 1) + at(row + 1, column) + at(row + 1, column + 1)

        return count
    }

    fun makeOcupy(copy: Room) {
        copy.rows[row][column] = TAKEN;
    }

    fun isOccupied() : Boolean {
        return room.rows[row][column] == TAKEN
    }

    fun makeEmpty(copy: Room) {
        copy.rows[row][column] = EMPTY;
    }

}

class Seat2(row: Int, column: Int, room: Room) : Seat(row, column, room) {
    override fun adjecntLimit(): Int {
        return 5
    }

    override fun adjectCount(): Int {
        fun point(row: Int, column: Int) : Int {
            if (row < 0) return 0
            if (column < 0) return 0
            if (row > room.rows.lastIndex) return 0
            if (column > room.rows[0].lastIndex) return 0
            return if (room.at(row,column) == TAKEN) 1 else 0;
        }

        fun at(dRow: Int, dColumn: Int) : Int {
            var startRow = row;
            var startColumn = column;

            while(true) {
                startRow = startRow + dRow
                startColumn = startColumn + dColumn

                if (startRow < 0) break;
                if (startColumn < 0) break;
                if (startRow > room.rows.lastIndex) break;
                if (startColumn > room.rows[0].lastIndex) break;
                if (room.at(startRow,startColumn) == TAKEN) return 1;
                if (room.at(startRow, startColumn) != FLOOR) return 0;
            }
             return 0
        }


        val count = at( - 1,  - 1) + at( - 1, 0) + at( - 1,  1) +
                at(0, 0 - 1) + at(0, 0 + 1) +
                at(0 + 1, 0 - 1) + at(0 + 1, 0) + at(0 + 1, 0 + 1)

        return count
    }
}

class Room : Iterable<Seat> {
    val rows : Array<IntArray>;

    var gen : (Int, Int, Room) -> Seat = { x, y, r -> Seat(x, y, r)}

    constructor(input: List<String>, generator: (Int, Int, Room) -> Seat) {
        val rows = input.size;
        gen = generator

        val columns = input[0].length
        this.rows = Array<IntArray>(rows) { IntArray(columns) { 0 } }

        for(index in input.indices) {
            val line = input[index]
            for(charIndex in line.indices) {
                val ch = line[charIndex]
                this.rows[index][charIndex] = when (ch) {
                    'L' -> EMPTY
                    '.' -> FLOOR
                    else -> TAKEN
                }
            }
        }
    }

    constructor(copy: Room, generator: (Int, Int, Room) -> Seat) : this(copy.rows, generator) {

    }

    constructor(other: Array<IntArray>, generator: (Int, Int, Room) -> Seat) {
        gen = generator
        rows = Array(other.size) { IntArray(other[0].size) { 0 } }
        for(index in other.indices) {
            val line = other[index]
            for(charIndex in line.indices) {
                rows[index][charIndex] = line[charIndex]
            }
        }
    }

    fun print() {

        println("------------------")

        for(rowIndex in rows.indices) {
            val row = rows[rowIndex]
            for(colIndex in row.indices) {
                val col = row[colIndex]

                print(when(col) {
                    TAKEN -> '#'
                    FLOOR -> '.'
                    else -> 'L'
                })
            }
            println()
        }

        println("------------------")
    }

    fun ocupy() : Room {
        val copy = Room(this, gen)
        for(seat in this) {
            seat.shift(copy);
        }
        return copy
    }

    fun ocupied() : Int {
        val count = this.filter { it.isOccupied() }.count()
        return count;
    }

    fun at(x: Int, y: Int) = rows[x][y];

    override fun iterator(): Iterator<Seat> {
        val room = this
        val s = sequence<Seat> {
            for(index in rows.indices) {
                val line = rows[index]
                for(charIndex in line.indices) {
                    yield(gen(index, charIndex, room))
                }
            }
        }

        return s.iterator()
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is Room) return false
        val room = other as Room

        for(row in rows.indices) {
            for(column in rows[row].indices) {
                val my = rows[row][column]
                val yours = room.rows[row][column]
                if (my != yours) return false;
            }
        }

        return true
    }
}


fun main(args: Array<String>) {
    val file = File("input.txt").readLines()

    fun test(generator: (Int, Int, Room) -> Seat) {
        var room = Room(file, generator)
        var last = room;
        var counter = 1
        room = room.ocupy();


     //   room.print()

        while (!last.equals(room)) {
            last = room;
            room = room.ocupy();

           // room.print()

            counter++
        }



        println("Iterations ${counter} occupied seets ${room.ocupied()}")
    }

    // first
    test { x, y, r -> Seat(x, y, r) }
    // second
    test { x, y, r -> Seat2(x, y, r) }

}