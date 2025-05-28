package javavm.classfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ClassFileReader implements AutoCloseable {
    private static final int MAGIC_NUMBER = 0xCAFEBABE;
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

    // TODO: 定数プールの読み取りを実装する
    // - readConstantPool()メソッドを作成
    // - constant_pool_countを読み取る(2バイト)
    // - constant_pool_count - 1個の定数プールエントリを読み取る
    // - 各エントリはタグ(1バイト)と可変長のデータで構成される
    private final InputStream inputStream;

    public ClassFileReader(String path) throws ClassNotFoundException {
        File file = new File(path);
        if (!file.exists()) {
            throw new ClassNotFoundException("クラスファイルが見つかりません: " + path);
        }

        if (file.isDirectory()) {
            throw new ClassNotFoundException("ディレクトリはクラスファイルとして読み込めません: " + path);
        }

        if (file.length() == 0) {
            throw new ClassFormatError("空のクラスファイルです: " + path);
        }

        if (file.length() > MAX_FILE_SIZE) {
            throw new ClassFormatError("ファイルサイズが最大許容サイズを超えています: " + file.length() + " > " + MAX_FILE_SIZE + " bytes");
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
     * クラスファイルのバージョン情報を読み取り、検証します。
     * @throws IOException 入出力エラーが発生した場合
     * @throws ClassFormatError バージョン情報の読み取りに失敗した場合
     */
    public void readVersion() throws IOException {
        int minorVersion = readBigEndianShort();
        int majorVersion = readBigEndianShort();

        // JDK 8以降をサポート（major_version >= 52）
        if (majorVersion < 52) {
            throw new ClassFormatError("サポートされていないクラスファイルバージョンです: " + majorVersion + "." + minorVersion);
        }
    }

    /**
     * 定数プールを読み取ります。
     * @return 読み取った定数プールの配列。インデックス0は未使用でnullが格納されます。
     * @throws IOException 入出力エラーが発生した場合
     * @throws ClassFormatError 定数プールの読み取りに失敗した場合
     */
    public ConstantInfo[] readConstantPool() throws IOException {
        int count = readBigEndianShort();
        if (count <= 0) {
            throw new ClassFormatError("定数プールカウントが不正です: " + count);
        }

        ConstantInfo[] constantPool = new ConstantInfo[count];
        // インデックス0は未使用
        constantPool[0] = null;

        // インデックス1から定数プールエントリを読み取り
        for (int i = 1; i < count; i++) {
            int tag = inputStream.read();
            if (tag < 0) {
                throw new ClassFormatError("予期せぬファイルの終わりに到達しました");
            }

            // TODO: CONSTANT_Class(7)の実装
            // - name_indexを読み取る(2バイト)
            // - ConstantClassInfoクラスを作成して格納

            // TODO: CONSTANT_Fieldref(9)の実装
            // - class_indexを読み取る(2バイト)
            // - name_and_type_indexを読み取る(2バイト)
            // - ConstantFieldrefInfoクラスを作成して格納

            switch (tag) {
                case ConstantInfo.CONSTANT_Class:
                    int nameIndex = readBigEndianShort();
                    constantPool[i] = new ConstantClassInfo(nameIndex);
                    break;

                case ConstantInfo.CONSTANT_Fieldref:
                    int classIndex = readBigEndianShort();
                    int nameAndTypeIndex = readBigEndianShort();
                    constantPool[i] = new ConstantFieldrefInfo(classIndex, nameAndTypeIndex);
                    break;

                case ConstantInfo.CONSTANT_NameAndType:
                    int nameIdx = readBigEndianShort();
                    int descriptorIndex = readBigEndianShort();
                    constantPool[i] = new ConstantNameAndTypeInfo(nameIdx, descriptorIndex);
                    break;

                case ConstantInfo.CONSTANT_Utf8:
                    // 文字列の長さを読み取り
                    int length = readBigEndianShort();
                    // 文字列のバイト列を読み取り
                    byte[] bytes = new byte[length];
                    int n = inputStream.read(bytes);
                    if (n != length) {
                        throw new ClassFormatError("予期せぬファイルの終わりに到達しました");
                    }
                    // UTF-8文字列に変換
                    constantPool[i] = new ConstantUtf8Info(new String(bytes));
                    break;

                default:
                    throw new ClassFormatError("不正なタグ値です: " + tag);
            }
        }

        return constantPool;
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

    /**
     * 入力ストリームから2バイトを読み取り、ビッグエンディアンの整数として返します。
     * @return 読み取った2バイトから変換された整数
     * @throws IOException 入出力エラーが発生した場合
     * @throws ClassFormatError ファイルの末尾に達した場合
     */
    private int readBigEndianShort() throws IOException {
        int b1 = inputStream.read();
        int b2 = inputStream.read();

        if (b1 < 0 || b2 < 0) {
            throw new ClassFormatError("予期せぬファイルの終わりに到達しました");
        }

        return (b1 << 8) | b2;
    }
}