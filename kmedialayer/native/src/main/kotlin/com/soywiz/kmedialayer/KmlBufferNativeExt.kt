package com.soywiz.kmedialayer

import kotlinx.cinterop.*

fun KmlBuffer.unsafeAddress(): CPointer<ByteVar> = this.baseBuffer.unsafeAddress()
fun KmlBufferBase.unsafeAddress(): CPointer<ByteVar> = this.data.unsafeAddress()
fun ByteArray.unsafeAddress(): CPointer<ByteVar> {
    val pin = this.pin()
    val ptr = pin.addressOf(0)
    pin.unpin()
    return ptr
}
