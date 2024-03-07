package com.github.bytecat

import org.junit.jupiter.api.Test

class TestByteCat {
    @Test
    fun testSetup() {
        val byteCat = ByteCat()
        byteCat.startup()
    }
}