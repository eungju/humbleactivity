@file:JvmName("ListExtensions")
package humbleactivity.app.data

fun <T> List<T>.removeAt(at: Int): List<T> = subList(0, at) + subList(at + 1, size)

fun <T> List<T>.swap(from: Int, to: Int): List<T> = if (from == to)
    this
else if (from > to)
    swap(to, from)
else
    subList(0, from) + this[to] + subList(from + 1, to) + this[from] + subList(to + 1, size)
