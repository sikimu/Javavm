package javavm.classfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ClassFileReader implements AutoCloseable {
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    // TODO: マジックナンバー(0xCAFEBABE)を読み取り、クラスファイルが正しいフォーマットかを検証する
    // - readMagic()メソッドを作成し、4バイトを読み取る
    // - 読み取った値が0xCAFEBABEと一致しない場合は例外をスローする
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

    /**
     * クラスファイルのマジックナンバーを読み取り、検証します。
     * @throws IOException 入出力エラーが発生した場合
     * @throws ClassFormatError マジックナンバーが不正な場合
     */
    public void readMagic() throws IOException {
        int magic = readBigEndianInt();
        if (magic != MAGIC_NUMBER) {
            throw new ClassFormatError("マジックナンバーが不正です: 0x" + Integer.toHexString(magic).toUpperCase());
        }
    }

    /**
     * 入力ストリームから4バイトを読み取り、ビッグエンディアンの整数として返します。
     * @return 読み取った4バイトから変換された整数
     * @throws IOException 入出力エラーが発生した場合
     * @throws ClassFormatError ファイルの末尾に達した場合
     */
    private int readBigEndianInt() throws IOException {
        int b1 = inputStream.read();
        int b2 = inputStream.read();
        int b3 = inputStream.read();
        int b4 = inputStream.read();

        if (b1 < 0 || b2 < 0 || b3 < 0 || b4 < 0) {
            throw new ClassFormatError("予期せぬファイルの終わりに到達しました");
        }

        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }
}