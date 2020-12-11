import java.io.File


interface Device : Comparable<Device>, Iterable<Device> {
    fun can(prev: Device) : Boolean {
        return false;
    }

    fun output(): Long {
        return 0
    }

    fun addPossible(def: List<Device>);

    fun total() : Long {
        return 0
    }

    fun setInput(i: Device) {

    }

    fun getInput() : Device? {
        return null;
    }

    fun diffrence() : Long {
        if (getInput() == null) return 0;
        return output() - getInput()!!.output()
    }

    override fun compareTo(other: Device): Int {
        return this.output().compareTo(other.output())
    }

    override fun iterator(): Iterator<Device> {
        return sequence<Device> {  }.iterator()
    }


}



class Adapter(val rating: Long) : Device {
    override fun output(): Long {
        return rating;
    }

    override fun toString(): String {
        return output().toString()
    }

    var children: List<Device> = listOf()

    override fun addPossible(def: List<Device>) {
        children = def
    }

    var _total = -1L

    override fun total(): Long {
        if (children.size == 0) return 1;

        if (_total != -1L) return _total

        _total = children.map { it.total() }.sum()

        return _total
    }

    private var _input: Device? = null;

    override fun setInput(i: Device) {
        _input = i;
    }

    override fun getInput() : Device? {
        return _input;
    }

    override fun iterator(): Iterator<Device> {
        var start : Device? = this as Device
        val s = sequence<Device> {
            while(start != null) {
                yield(start as Device)
                start = start!!.getInput()
            }
        }

        return s.iterator()
    }


    override fun can(prev: Device): Boolean {
        return prev.output() + 3 >= rating && prev.output() <= rating
    }
}

class Outlet : Device {
    var children: List<Device> = listOf()
    override fun addPossible(def: List<Device>) {
        children = def
    }

    override fun total(): Long {
        if (children.size == 0) return 1;
        return children.map { it.total() }.sum()
    }
}

fun main(args: Array<String>) {
    val file = File("input.txt").readLines()

    val adapters = file.map { it.toLong() }.map { Adapter(it) as Device }
    val left = adapters.toMutableList();

    // find hihest
    val highest = left.sorted().last()

    val computer = Adapter(highest.output() + 3)

    // add adapter in my device
    left.add(computer)

    var current : Device = Outlet();

    while(!left.isEmpty()) {
        val next = left.filter { it.can(current) }.sorted().first();
        left.remove(next)
        next.setInput(current);
        current = next;
    }

    var map = mutableMapOf<Long, Int>()

    for(a in current) {
        val diff = a.diffrence()
        if (!map.containsKey(diff)) map.set(diff, 0)
        map[diff] = map[diff]!! + 1;
    }

    val onses = map[1]!!
    val threes = map[3]!!

    println("Fist answer ${onses * threes}")

    // now built tree

    val zero = Outlet()
    val all = mutableListOf<Device>()
    all.add(zero)
    all.addAll(adapters)
    all.add(computer)

    for(d in all) {
        val possible = all.filter { it.can(d) && it != d }
        d.addPossible(possible)
    }

    val size = zero.total()

    println("Second is ${size}")
}