package com.mqttsnet.thinglinks.common.core.dynamicCompilation.bytecode;

/**
 * Class文件修改器
 * @author thinglinks
 * @date 2022-07-04
 */
public class ClassModifier {
    private static final int CONSTANT_POOL_COUNT_INDEX = 8;
    private static final int CONSTANT_Utf8_info = 1;
    private static final int[] CONSTANT_ITEM_LENGTH = {-1, -1, -1, 5, 5, 9, 9, 3, 3, 5, 5, 5, 5};
    private static final int u1 = 1;
    private static final int u2 = 2;
    private byte[] classByte;

    public ClassModifier(byte[] classByte) {
        this.classByte = classByte;
    }
    
    public byte[] modifyUTF8Constant4Class(Class<?> oldClass, Class<?> newClass) {
        return modifyUTF8Constant4ClassPath(oldClass.getName(), newClass.getName());
    }
    
    public byte[] modifyUTF8Constant4ClassPath(String oldClassName, String newClassName) {
        String oldReference = oldClassName.replace(".", "/");
        String newReference = newClassName.replace(".", "/");
        return modifyUTF8Constant4Reference(oldReference, newReference);
    }

    /**
     * 修改字符串常量池的符号引用
     */
    public byte[] modifyUTF8Constant4Reference(String oldReference, String newReference) {
        int cpc = getConstantPoolCount();
        int offset = CONSTANT_POOL_COUNT_INDEX + u2;
        for (int i = 0; i < cpc; i++) {
            int tag = ByteUtils.bytes2Int(classByte, offset, u1);
            if (tag == CONSTANT_Utf8_info) {
                int len = ByteUtils.bytes2Int(classByte, offset + u1, u2);
                offset += (u1 + u2);
                String str = ByteUtils.bytes2String(classByte, offset, len);
                if (str.equalsIgnoreCase(oldReference)) {
                    byte[] strBytes = ByteUtils.string2Bytes(newReference);
                    byte[] strLen = ByteUtils.int2Bytes(newReference.length(), u2);
                    classByte = ByteUtils.bytesReplace(classByte, offset - u2, u2, strLen);
                    //这里不只是替换，应该是填充，把新的字节数据插入到原来的位置，然后存在后面字节的向前或者先后移动
                    classByte = ByteUtils.bytesReplace(classByte, offset, len, strBytes);
                    return classByte;
                }
                else {
                    offset += len;
                }
            }
            else {
                offset += CONSTANT_ITEM_LENGTH[tag];
            }
        }
        
        //没有找到需要注入的引用字符串，直接返回原始数据
        return classByte;
    }

    public int getConstantPoolCount() {
        return ByteUtils.bytes2Int(classByte, CONSTANT_POOL_COUNT_INDEX, u2);
    }
}
