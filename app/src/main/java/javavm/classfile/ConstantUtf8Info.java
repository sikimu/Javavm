package javavm.classfile;

/**
 * CONSTANT_Utf8型の定数プールエントリ
 */
public class ConstantUtf8Info extends ConstantInfo {
    private final String value;

    public ConstantUtf8Info(String value) {
        this.value = value;
    }

    @Override
    public int getTag() {
        return CONSTANT_Utf8;
    }

    /**
     * UTF-8文字列の値を取得します。
     * @return UTF-8文字列
     */
    public String getValue() {
        return value;
    }
}