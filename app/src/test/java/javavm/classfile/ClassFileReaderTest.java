package javavm.classfile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

class ClassFileReaderTest {
    @TempDir
    Path tempDir;

    private String createTestClassFile(byte[] content) throws IOException {
        Path tempFile = tempDir.resolve("Test.class");
        try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
            fos.write(content);
        }
        return tempFile.toString();
    }

    private byte[] createValidMagicNumber() {
        return new byte[]{(byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE};
    }

    @Test
    void testFileNotFound() {
        String nonExistentPath = "non/existent/file.class";
        ClassNotFoundException exception = assertThrows(
            ClassNotFoundException.class,
            () -> new ClassFileReader(nonExistentPath),
            "ファイルが存在しない場合はClassNotFoundExceptionをスローする必要があります"
        );
        
        assertTrue(
            exception.getMessage().contains(nonExistentPath),
            "例外メッセージにファイルパスが含まれている必要があります"
        );
    }

    @Test
    void testValidMagicNumber() throws Exception {
        // 正しいマジックナンバー 0xCAFEBABE を持つクラスファイルを作成
        byte[] validMagic = {(byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE};
        String validClassPath = createTestClassFile(validMagic);

        try (ClassFileReader reader = new ClassFileReader(validClassPath)) {
            reader.readMagic(); // 例外が発生しないことを確認
        }
    }

    @Test
    void testInvalidMagicNumber() throws Exception {
        // 不正なマジックナンバーを持つクラスファイルを作成
        byte[] invalidMagic = {0x00, 0x00, 0x00, 0x00};
        String invalidClassPath = createTestClassFile(invalidMagic);

        try (ClassFileReader reader = new ClassFileReader(invalidClassPath)) {
            ClassFormatError exception = assertThrows(
                ClassFormatError.class,
                () -> reader.readMagic(),
                "不正なマジックナンバーの場合はClassFormatErrorをスローする必要があります"
            );

            assertTrue(
                exception.getMessage().contains("マジックナンバーが不正です"),
                "例外メッセージにマジックナンバーが不正である旨を含める必要があります"
            );
        }
    }

    @Test
    void testValidVersion() throws Exception {
        // 有効なマジックナンバーとバージョン情報を持つクラスファイルを作成
        byte[] content = new byte[8];
        System.arraycopy(createValidMagicNumber(), 0, content, 0, 4);
        // JDK 8のバージョン情報: minor_version=0, major_version=52
        content[4] = 0x00; // minor_version上位バイト
        content[5] = 0x00; // minor_version下位バイト
        content[6] = 0x00; // major_version上位バイト
        content[7] = 0x34; // major_version下位バイト (52 = 0x34)

        String validClassPath = createTestClassFile(content);

        try (ClassFileReader reader = new ClassFileReader(validClassPath)) {
            reader.readMagic();
            reader.readVersion(); // 例外が発生しないことを確認
        }
    }

    @Test
    void testInvalidVersion_UnexpectedEOF() throws Exception {
        // マジックナンバーのみで、バージョン情報が不完全なファイルを作成
        String invalidClassPath = createTestClassFile(createValidMagicNumber());

        try (ClassFileReader reader = new ClassFileReader(invalidClassPath)) {
            reader.readMagic();
            ClassFormatError exception = assertThrows(
                ClassFormatError.class,
                () -> reader.readVersion(),
                "不完全なバージョン情報の場合はClassFormatErrorをスローする必要があります"
            );

            assertTrue(
                exception.getMessage().contains("予期せぬファイルの終わり"),
                "例外メッセージにファイルが不完全である旨を含める必要があります"
            );
        }
    }

    @Test
    void testReadConstantPool() throws Exception {
        // マジックナンバー(4バイト) + バージョン情報(4バイト) + 定数プールカウント(2バイト) + UTF-8エントリ
        byte[] content = new byte[] {
            // マジックナンバー
            (byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE,
            // バージョン情報（Java 8）
            0x00, 0x00, 0x00, 0x34,
            // 定数プールカウント = 2（インデックス1のエントリのみ）
            0x00, 0x02,
            // CONSTANT_Utf8 {"Test"}
            0x01, // tag = 1
            0x00, 0x04, // length = 4
            0x54, 0x65, 0x73, 0x74 // "Test"
        };

        String validClassPath = createTestClassFile(content);

        try (ClassFileReader reader = new ClassFileReader(validClassPath)) {
            reader.readMagic();
            reader.readVersion();
            ConstantInfo[] constantPool = reader.readConstantPool();
            
            assertEquals(2, constantPool.length, "定数プールの長さは2であるべきです");
            assertNull(constantPool[0], "定数プールの0番目の要素はnullであるべきです");
            
            assertTrue(constantPool[1] instanceof ConstantUtf8Info, 
                "定数プールの1番目の要素はConstantUtf8Infoであるべきです");
            
            ConstantUtf8Info utf8Info = (ConstantUtf8Info)constantPool[1];
            assertEquals("Test", utf8Info.getValue(), "UTF-8文字列の値が一致しません");
        }
    }

    @Test
    void testInvalidConstantPoolCount() throws Exception {
        // 定数プールカウントが0のクラスファイル
        byte[] content = new byte[] {
            // マジックナンバー
            (byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE,
            // バージョン情報（Java 8）
            0x00, 0x00, 0x00, 0x34,
            // 定数プールカウント = 0（不正）
            0x00, 0x00
        };

        String invalidClassPath = createTestClassFile(content);

        try (ClassFileReader reader = new ClassFileReader(invalidClassPath)) {
            reader.readMagic();
            reader.readVersion();
            ClassFormatError exception = assertThrows(
                ClassFormatError.class,
                () -> reader.readConstantPool(),
                "不正な定数プールカウントの場合はClassFormatErrorをスローする必要があります"
            );

            assertTrue(
                exception.getMessage().contains("定数プールカウント"),
                "例外メッセージに定数プールカウントに関する説明を含める必要があります"
            );
        }
    }

    @Test
    void testInvalidConstantTag() throws Exception {
        // 不正なタグ値を持つ定数プールエントリ
        byte[] content = new byte[] {
            // マジックナンバー
            (byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE,
            // バージョン情報（Java 8）
            0x00, 0x00, 0x00, 0x34,
            // 定数プールカウント = 2
            0x00, 0x02,
            // 不正なタグ値 = 0
            0x00,
            0x00, 0x04,
            0x54, 0x65, 0x73, 0x74
        };

        String invalidClassPath = createTestClassFile(content);

        try (ClassFileReader reader = new ClassFileReader(invalidClassPath)) {
            reader.readMagic();
            reader.readVersion();
            ClassFormatError exception = assertThrows(
                ClassFormatError.class,
                () -> reader.readConstantPool(),
                "不正なタグ値の場合はClassFormatErrorをスローする必要があります"
            );

            assertTrue(
                exception.getMessage().contains("不正なタグ"),
                "例外メッセージにタグ値が不正である旨を含める必要があります"
            );
        }
    }

    @Test
    void testReadConstantClass() throws Exception {
        // マジックナンバー + バージョン情報 + 定数プール（UTF-8 "Test" + Class）
        byte[] content = new byte[] {
            // マジックナンバー
            (byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE,
            // バージョン情報（Java 8）
            0x00, 0x00, 0x00, 0x34,
            // 定数プールカウント = 3
            0x00, 0x03,
            // #1: CONSTANT_Utf8 {"Test"}
            0x01, // tag = 1
            0x00, 0x04, // length = 4
            0x54, 0x65, 0x73, 0x74, // "Test"
            // #2: CONSTANT_Class {name_index = 1}
            0x07, // tag = 7
            0x00, 0x01 // name_index = 1
        };

        String validClassPath = createTestClassFile(content);

        try (ClassFileReader reader = new ClassFileReader(validClassPath)) {
            reader.readMagic();
            reader.readVersion();
            ConstantInfo[] constantPool = reader.readConstantPool();
            
            assertEquals(3, constantPool.length, "定数プールの長さは3であるべきです");
            assertNull(constantPool[0], "定数プールの0番目の要素はnullであるべきです");
            
            assertTrue(constantPool[1] instanceof ConstantUtf8Info,
                "定数プールの1番目の要素はConstantUtf8Infoであるべきです");
            
            assertTrue(constantPool[2] instanceof ConstantClassInfo,
                "定数プールの2番目の要素はConstantClassInfoであるべきです");
            
            ConstantClassInfo classInfo = (ConstantClassInfo)constantPool[2];
            assertEquals(1, classInfo.getNameIndex(), "name_indexが一致しません");
        }
    }

    @Test
    void testReadConstantFieldref() throws Exception {
        // マジックナンバー + バージョン情報 + 定数プール（Fieldref）
        byte[] content = new byte[] {
            // マジックナンバー
            (byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE,
            // バージョン情報（Java 8）
            0x00, 0x00, 0x00, 0x34,
            // 定数プールカウント = 2
            0x00, 0x02,
            // #1: CONSTANT_Fieldref {class_index = 2, name_and_type_index = 3}
            0x09, // tag = 9
            0x00, 0x02, // class_index = 2
            0x00, 0x03  // name_and_type_index = 3
        };

        String validClassPath = createTestClassFile(content);

        try (ClassFileReader reader = new ClassFileReader(validClassPath)) {
            reader.readMagic();
            reader.readVersion();
            ConstantInfo[] constantPool = reader.readConstantPool();
            
            assertEquals(2, constantPool.length, "定数プールの長さは2であるべきです");
            assertNull(constantPool[0], "定数プールの0番目の要素はnullであるべきです");
            
            assertTrue(constantPool[1] instanceof ConstantFieldrefInfo,
                "定数プールの1番目の要素はConstantFieldrefInfoであるべきです");
            
            ConstantFieldrefInfo fieldrefInfo = (ConstantFieldrefInfo)constantPool[1];
            assertEquals(2, fieldrefInfo.getClassIndex(), "class_indexが一致しません");
            assertEquals(3, fieldrefInfo.getNameAndTypeIndex(), "name_and_type_indexが一致しません");
        }
    }

    @Test
    void testReadConstantNameAndType() throws Exception {
        // マジックナンバー + バージョン情報 + 定数プール（NameAndType）
        byte[] content = new byte[] {
            // マジックナンバー
            (byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE,
            // バージョン情報（Java 8）
            0x00, 0x00, 0x00, 0x34,
            // 定数プールカウント = 2
            0x00, 0x02,
            // #1: CONSTANT_NameAndType {name_index = 3, descriptor_index = 4}
            0x0C, // tag = 12
            0x00, 0x03, // name_index = 3
            0x00, 0x04  // descriptor_index = 4
        };

        String validClassPath = createTestClassFile(content);

        try (ClassFileReader reader = new ClassFileReader(validClassPath)) {
            reader.readMagic();
            reader.readVersion();
            ConstantInfo[] constantPool = reader.readConstantPool();
            
            assertEquals(2, constantPool.length, "定数プールの長さは2であるべきです");
            assertNull(constantPool[0], "定数プールの0番目の要素はnullであるべきです");
            
            assertTrue(constantPool[1] instanceof ConstantNameAndTypeInfo,
                "定数プールの1番目の要素はConstantNameAndTypeInfoであるべきです");
            
            ConstantNameAndTypeInfo nameAndTypeInfo = (ConstantNameAndTypeInfo)constantPool[1];
            assertEquals(3, nameAndTypeInfo.getNameIndex(), "name_indexが一致しません");
            assertEquals(4, nameAndTypeInfo.getDescriptorIndex(), "descriptor_indexが一致しません");
        }
    }
}