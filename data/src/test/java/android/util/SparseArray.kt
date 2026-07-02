package android.util

/**
 * Minimal JVM-test shadow used by Firebase SDK error-code enums during local unit tests.
 */
@Suppress("unused", "UNUSED_PARAMETER")
class SparseArray<E> {
    private val values = linkedMapOf<Int, E>()

    constructor()

    constructor(initialCapacity: Int)

    fun get(key: Int): E? = values[key]

    fun get(
        key: Int,
        valueIfKeyNotFound: E,
    ): E = values[key] ?: valueIfKeyNotFound

    fun put(
        key: Int,
        value: E,
    ) {
        values[key] = value
    }

    fun append(
        key: Int,
        value: E,
    ) {
        values[key] = value
    }

    fun delete(key: Int) {
        values.remove(key)
    }

    fun remove(key: Int) {
        values.remove(key)
    }

    fun clear() {
        values.clear()
    }

    fun size(): Int = values.size

    fun keyAt(index: Int): Int = values.keys.elementAt(index)

    fun valueAt(index: Int): E = values.values.elementAt(index)
}
