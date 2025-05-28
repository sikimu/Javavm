package javavm.classfile;

/**
 * CONSTANT_Fieldref_info {
 *     u1 tag;
 *     u2 class_index;
 *     u2 name_and_type_index;
 * }
 */
public class ConstantFieldrefInfo extends ConstantInfo {
    private final int classIndex;
    private final int nameAndTypeIndex;

    public ConstantFieldrefInfo(int classIndex, int nameAndTypeIndex) {
        this.classIndex = classIndex;
        this.nameAndTypeIndex = nameAndTypeIndex;
    }

    public int getClassIndex() {
        return classIndex;
    }

    public int getNameAndTypeIndex() {
        return nameAndTypeIndex;
    }

    @Override
    public int getTag() {
        return CONSTANT_Fieldref;
    }
}