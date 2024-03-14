package com.github.bytecat.file

import com.github.bytecat.ext.getMD5
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URI

class DefaultFile : File, IFile {
    constructor(pathname: String) : super(pathname)
    constructor(parent: String?, child: String) : super(parent, child)
    constructor(parent: File?, child: String) : super(parent, child)
    constructor(uri: URI) : super(uri)

    override fun openReadStream(): InputStream {
        return FileInputStream(this)
    }

    override fun openWriteStream(): OutputStream {
        return FileOutputStream(this)
    }
}