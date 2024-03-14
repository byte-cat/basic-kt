package com.github.bytecat.file;

import java.io.InputStream;
import java.io.OutputStream;

public interface IFile {
    String getName();
    long length();
    InputStream openReadStream();
    OutputStream openWriteStream();
}
