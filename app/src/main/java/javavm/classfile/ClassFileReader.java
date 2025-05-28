package javavm.classfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ClassFileReader implements AutoCloseable {
    private final InputStream inputStream;

    public ClassFileReader(String path) throws ClassNotFoundException {
        File file = new File(path);
        if (!file.exists()) {
            throw new ClassNotFoundException("クラスファイルが見つかりません: " + path);
        }

        try {
            inputStream = new FileInputStream(file);
        } catch (IOException e) {
            throw new ClassNotFoundException("クラスファイルを開けません: " + path, e);
        }
    }

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
    }
}