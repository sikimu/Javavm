package javavm.classfile;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClassFileReaderTest {
    
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
}