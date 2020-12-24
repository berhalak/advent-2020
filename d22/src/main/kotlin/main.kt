import java.io.File


class Card(val number: Int) {
    operator fun compareTo(b: Card): Int {
        return number.compareTo(b.number)
    }
}

class Winner(val a: Card, val b: Card) {
    fun givePrice(player1: Deck, player2: Deck) {
        if (a > b) player1.putLast(a, b)
        else player2.putLast(b, a)
    }
}

class Deck {
    val cards: MutableList<Card>

    constructor(lines: List<String>) {
        this.cards = lines.drop(1).map { Card(it.toInt()) }.toMutableList()
    }

    fun draw(): Card {
        val first = cards[0]
        cards.removeAt(0)
        return first
    }

    fun noCards() : Boolean {
        return cards.size == 0
    }

    fun putLast(a: Card, b:Card) {
        cards.add(a)
        cards.add(b)
    }

    fun score() : Long {
        var sum = 0L
        for(i in cards.indices) {
            val index = i + 1
            val card = cards[cards.size - 1 - i]
            val value = index * card.number.toLong()
            sum += value
        }
        return sum
    }
}

fun main() {
    val file = File("input.txt").readLines()
    val decks = file.splitByEmpty().map { Deck(it) }.toList()

    var round = 0

    val player1 = decks[0]!!
    val player2 = decks[1]!!

    while (round++ < 900) {
        if (player1.noCards() || player2.noCards()) break
        val a = player1.draw()
        val b = player2.draw()
        val winner = Winner(a, b)
        winner.givePrice(player1, player2)
    }

    val winner = if (player1.noCards()) player2 else player1

    println(winner.score())
}

