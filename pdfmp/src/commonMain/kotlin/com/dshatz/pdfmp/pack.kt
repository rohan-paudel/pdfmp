package com.dshatz.pdfmp

import kotlinx.io.*

internal fun <K, V> LinkedHashMap<K, V>.packMap(to: Buffer, packKey: Buffer.(K) -> Unit, packValue: Buffer.(V) -> Unit) {
    to.writeInt(size)
    forEach {
        packKey(to, it.key)
        packValue(to, it.value)
    }
}

internal fun <K, V> unpackMap(buffer: Buffer, unpackKey: Buffer.() -> K, unpackValue: Buffer.() -> V): LinkedHashMap<K, V> {
    val size = buffer.readInt()
    return LinkedHashMap<K, V>(size).also { map ->
        repeat(size) {
            map[unpackKey(buffer)] = unpackValue(buffer)
        }
    }
}

internal fun <T> List<T>.packList(to: Buffer, packItem: T.(Buffer) -> Unit) {
    to.writeInt(size)
    forEach {
        it.packItem(to)
    }
}

internal fun <T> unpackList(buffer: Buffer, unpackItem: Buffer.() -> T): List<T> {
    val size = buffer.readInt()
    return buildList {
        repeat(size) {
            add(buffer.unpackItem())
        }
    }
}