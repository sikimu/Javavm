package javavm.classfile;

/**
 * CONSTANT_NameAndType_info構造体を表現するクラス
 */
public class ConstantNameAndTypeInfo extends ConstantInfo {
    private final int nameIndex;
    private final int descriptorIndex;

    /**
     * コンストラクタ
     * @param nameIndex 名前を参照するための定数プールインデックス
     * @param descriptorIndex 記述子を参照するための定数プールインデックス
     */
    public ConstantNameAndTypeInfo(int nameIndex, int descriptorIndex) {
        this.nameIndex = nameIndex;
        this.descriptorIndex = descriptorIndex;
    }

    @Override
    public int getTag() {
        return CONSTANT_NameAndType;
    }

    /**
     * 名前のインデックスを取得します
     * @return 名前を参照するための定数プールインデックス
     */
    public int getNameIndex() {
        return nameIndex;
    }

    /**
     * 記述子のインデックスを取得します
     * @return 記述子を参照するための定数プールインデックス
     */
    public int getDescriptorIndex() {
        return descriptorIndex;
    }
}