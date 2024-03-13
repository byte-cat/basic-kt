package com.github.bytecat.utils

interface FileRequestObserver {
    fun onParseStart()
    fun onParseEnd()
}