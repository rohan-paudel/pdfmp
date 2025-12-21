package com.dshatz.pdfmp

import kotlinx.io.Buffer
import kotlinx.io.readFloat
import kotlinx.io.writeFloat

fun packMap(buffer: Buffer, list: List<Float>) {
    list.forEach(buffer::writeFloat)
}

fun unpackFloats(buffer: Buffer): List<Float> {
    val result = mutableListOf<Float>()
    while (!buffer.exhausted()) {
        result += buffer.readFloat()
    }
    return result
}