package javavm.classfile;

/**
 * CONSTANT_Class_info {
 *     u1 tag;
 *     u2 name_index;
 * }
 */
public class ConstantClassInfo extends ConstantInfo {
    private final int nameIndex;

    public ConstantClassInfo(int nameIndex) {
        this.nameIndex = nameIndex;
    }

    public int getNameIndex() {
        return nameIndex;
    }

    @Override
    public int getTag() {
        return CONSTANT_Class;
    }
}