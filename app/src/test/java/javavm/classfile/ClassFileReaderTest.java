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

    private String createTestClassFile(byte[] magicNumber) throws IOException {
        Path tempFile = tempDir.resolve("Test.class");
        try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
            fos.write(magicNumber);
        }
        return tempFile.toString();
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
}