// ASM: a very small and fast Java bytecode manipulation framework
// Copyright (c) 2000-2011 INRIA, France Telecom
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
// 3. Neither the name of the copyright holders nor the names of its
//    contributors may be used to endorse or promote products derived from
//    this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
// THE POSSIBILITY OF SUCH DAMAGE.
package org.springframework.asm;

/**
 * Java虚拟机规范(JVMS)中定义的非标准类、字段、方法或代码属性。
 *
 * A non standard class, field, method or Code attribute, as defined in the Java Virtual Machine
 * Specification (JVMS).
 *
 * @see <a href= "https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.7">JVMS
 *     4.7</a>
 * @see <a href= "https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.7.3">JVMS
 *     4.7.3</a>
 * @author Eric Bruneton
 * @author Eugene Kuleshov
 */
public class Attribute {

  /** 此属性的类型，在jvm中也称为其名称。 */
  /** The type of this attribute, also called its name in the JVMS. */
  public final String type;

  /**
   * The raw content of this attribute, only used for unknown attributes (see {@link #isUnknown()}).
   * The 6 header bytes of the attribute (attribute_name_index and attribute_length) are <i>not</i>
   * included.
   */
  private byte[] content;

  /**
   * 此属性列表中的下一个属性(可以通过此字段链接属性实例来存储类、字段、方法或代码属性的列表)。可以是{@literal null}。
   *
   * The next attribute in this attribute list (Attribute instances can be linked via this field to
   * store a list of class, field, method or Code attributes). May be {@literal null}.
   */
  Attribute nextAttribute;

  /**
   * Constructs a new empty attribute.
   *
   * @param type the type of the attribute.
   */
  protected Attribute(final String type) {
    this.type = type;
  }

  /**
   * 如果属性类型未知，则返回{@literal true}。这意味着不能解析属性内容来提取常量池引用、标签等。
   * 相反，属性内容作为不透明的字节数组读取，并按原样写回。如果内容实际上包含常量池引用、标签或其他需要在常量池、
   * 方法字节码等发生更改时更新的符号引用，则可能导致无效的属性。这个方法的默认实现总是返回{@literal true}。
   *
   * Returns {@literal true} if this type of attribute is unknown. This means that the attribute
   * content can't be parsed to extract constant pool references, labels, etc. Instead, the
   * attribute content is read as an opaque byte array, and written back as is. This can lead to
   * invalid attributes, if the content actually contains constant pool references, labels, or other
   * symbolic references that need to be updated when there are changes to the constant pool, the
   * method bytecode, etc. The default implementation of this method always returns {@literal true}.
   *
   * @return {@literal true} if this type of attribute is unknown.
   */
  public boolean isUnknown() {
    return true;
  }

  /**
   * 如果此类型的属性是代码属性，则返回{@literal true}。
   *
   * Returns {@literal true} if this type of attribute is a Code attribute.
   *
   * @return {@literal true} if this type of attribute is a Code attribute.
   */
  public boolean isCodeAttribute() {
    return false;
  }

  /**
   * 返回与此属性对应的标签。
   *
   * Returns the labels corresponding to this attribute.
   *
   * @return the labels corresponding to this attribute, or {@literal null} if this attribute is not
   *     a Code attribute that contains labels.
   */
  protected Label[] getLabels() {
    return new Label[0];
  }

  /**
   * 读取{@link #type}属性。这个方法必须返回一个新的{@link Attribute}对象，类型为{@link #type}，
   * 对应于给定的ClassReader中从'offset'开始的'length'字节。
   *
   * Reads a {@link #type} attribute. This method must return a <i>new</i> {@link Attribute} object,
   * of type {@link #type}, corresponding to the 'length' bytes starting at 'offset', in the given
   * ClassReader.
   *
   * @param classReader the class that contains the attribute to be read.
   * @param offset index of the first byte of the attribute's content in {@link ClassReader}. The 6
   *     attribute header bytes (attribute_name_index and attribute_length) are not taken into
   *     account here.
   * @param length the length of the attribute's content (excluding the 6 attribute header bytes).
   * @param charBuffer the buffer to be used to call the ClassReader methods requiring a
   *     'charBuffer' parameter.
   * @param codeAttributeOffset index of the first byte of content of the enclosing Code attribute
   *     in {@link ClassReader}, or -1 if the attribute to be read is not a Code attribute. The 6
   *     attribute header bytes (attribute_name_index and attribute_length) are not taken into
   *     account here.
   * @param labels the labels of the method's code, or {@literal null} if the attribute to be read
   *     is not a Code attribute.
   * @return a <i>new</i> {@link Attribute} object corresponding to the specified bytes.
   */
  protected Attribute read(
      final ClassReader classReader,
      final int offset,
      final int length,
      final char[] charBuffer,
      final int codeAttributeOffset,
      final Label[] labels) {
    Attribute attribute = new Attribute(type);
    attribute.content = new byte[length];
    System.arraycopy(classReader.classFileBuffer, offset, attribute.content, 0, length);
    return attribute;
  }

  /**
   * 返回此属性内容的字节数组形式。不能在返回的ByteVector中添加6个头字节(attribute_name_index和attribute_length)。
   *
   * Returns the byte array form of the content of this attribute. The 6 header bytes
   * (attribute_name_index and attribute_length) must <i>not</i> be added in the returned
   * ByteVector.
   *
   * @param classWriter the class to which this attribute must be added. This parameter can be used
   *     to add the items that corresponds to this attribute to the constant pool of this class.
   * @param code the bytecode of the method corresponding to this Code attribute, or {@literal null}
   *     if this attribute is not a Code attribute. Corresponds to the 'code' field of the Code
   *     attribute.
   * @param codeLength the length of the bytecode of the method corresponding to this code
   *     attribute, or 0 if this attribute is not a Code attribute. Corresponds to the 'code_length'
   *     field of the Code attribute.
   * @param maxStack the maximum stack size of the method corresponding to this Code attribute, or
   *     -1 if this attribute is not a Code attribute.
   * @param maxLocals the maximum number of local variables of the method corresponding to this code
   *     attribute, or -1 if this attribute is not a Code attribute.
   * @return the byte array form of this attribute.
   */
  protected ByteVector write(
      final ClassWriter classWriter,
      final byte[] code,
      final int codeLength,
      final int maxStack,
      final int maxLocals) {
    return new ByteVector(content);
  }

  /**
   * 返回以该属性开头的属性列表的属性数。
   *
   * Returns the number of attributes of the attribute list that begins with this attribute.
   *
   * @return the number of attributes of the attribute list that begins with this attribute.
   */
  final int getAttributeCount() {
    int count = 0;
    Attribute attribute = this;
    while (attribute != null) {
      count += 1;
      attribute = attribute.nextAttribute;
    }
    return count;
  }

  /**
   * 返回以该属性开头的属性列表中所有属性的总大小(以字节为单位)。这个大小包括每个属性的6个头字节(attribute_name_index和attribute_length)。
   * 还将属性类型名称添加到常量池中。
   *
   * Returns the total size in bytes of all the attributes in the attribute list that begins with
   * this attribute. This size includes the 6 header bytes (attribute_name_index and
   * attribute_length) per attribute. Also adds the attribute type names to the constant pool.
   *
   * @param symbolTable where the constants used in the attributes must be stored.
   * @return the size of all the attributes in this attribute list. This size includes the size of
   *     the attribute headers.
   */
  final int computeAttributesSize(final SymbolTable symbolTable) {
    final byte[] code = null;
    final int codeLength = 0;
    final int maxStack = -1;
    final int maxLocals = -1;
    return computeAttributesSize(symbolTable, code, codeLength, maxStack, maxLocals);
  }

  /**
   * 返回以该属性开头的属性列表中所有属性的总大小(以字节为单位)。这个大小包括每个属性的6个头字节(attribute_name_index和attribute_length)。
   * 还将属性类型名称添加到常量池中。
   *
   * Returns the total size in bytes of all the attributes in the attribute list that begins with
   * this attribute. This size includes the 6 header bytes (attribute_name_index and
   * attribute_length) per attribute. Also adds the attribute type names to the constant pool.
   *
   * @param symbolTable where the constants used in the attributes must be stored.
   * @param code the bytecode of the method corresponding to these Code attributes, or {@literal
   *     null} if they are not Code attributes. Corresponds to the 'code' field of the Code
   *     attribute.
   * @param codeLength the length of the bytecode of the method corresponding to these code
   *     attributes, or 0 if they are not Code attributes. Corresponds to the 'code_length' field of
   *     the Code attribute.
   * @param maxStack the maximum stack size of the method corresponding to these Code attributes, or
   *     -1 if they are not Code attributes.
   * @param maxLocals the maximum number of local variables of the method corresponding to these
   *     Code attributes, or -1 if they are not Code attribute.
   * @return the size of all the attributes in this attribute list. This size includes the size of
   *     the attribute headers.
   */
  final int computeAttributesSize(
      final SymbolTable symbolTable,
      final byte[] code,
      final int codeLength,
      final int maxStack,
      final int maxLocals) {
    final ClassWriter classWriter = symbolTable.classWriter;
    int size = 0;
    Attribute attribute = this;
    while (attribute != null) {
      symbolTable.addConstantUtf8(attribute.type);
      size += 6 + attribute.write(classWriter, code, codeLength, maxStack, maxLocals).length;
      attribute = attribute.nextAttribute;
    }
    return size;
  }

  /**
   * 返回与给定字段、方法或类访问标志和签名对应的所有属性的总大小(以字节为单位)。
   * 这个大小包括每个属性的6个头字节(attribute_name_index和attribute_length)。还将属性类型名称添加到常量池中。
   *
   * Returns the total size in bytes of all the attributes that correspond to the given field,
   * method or class access flags and signature. This size includes the 6 header bytes
   * (attribute_name_index and attribute_length) per attribute. Also adds the attribute type names
   * to the constant pool.
   *
   * @param symbolTable where the constants used in the attributes must be stored.
   * @param accessFlags some field, method or class access flags.
   * @param signatureIndex the constant pool index of a field, method of class signature.
   * @return the size of all the attributes in bytes. This size includes the size of the attribute
   *     headers.
   */
  static int computeAttributesSize(
      final SymbolTable symbolTable, final int accessFlags, final int signatureIndex) {
    int size = 0;
    // Before Java 1.5, synthetic fields are represented with a Synthetic attribute.
    if ((accessFlags & Opcodes.ACC_SYNTHETIC) != 0
        && symbolTable.getMajorVersion() < Opcodes.V1_5) {
      // Synthetic attributes always use 6 bytes.
      symbolTable.addConstantUtf8(Constants.SYNTHETIC);
      size += 6;
    }
    if (signatureIndex != 0) {
      // Signature attributes always use 8 bytes.
      symbolTable.addConstantUtf8(Constants.SIGNATURE);
      size += 8;
    }
    // ACC_DEPRECATED is ASM specific, the ClassFile format uses a Deprecated attribute instead.
    if ((accessFlags & Opcodes.ACC_DEPRECATED) != 0) {
      // Deprecated attributes always use 6 bytes.
      symbolTable.addConstantUtf8(Constants.DEPRECATED);
      size += 6;
    }
    return size;
  }

  /**
   * 将以该属性开头的属性列表的所有属性放在给定的字节向量中。这包括每个属性的6个头字节(attribute_name_index和attribute_length)。
   *
   * Puts all the attributes of the attribute list that begins with this attribute, in the given
   * byte vector. This includes the 6 header bytes (attribute_name_index and attribute_length) per
   * attribute.
   *
   * @param symbolTable where the constants used in the attributes must be stored.
   * @param output where the attributes must be written.
   */
  final void putAttributes(final SymbolTable symbolTable, final ByteVector output) {
    final byte[] code = null;
    final int codeLength = 0;
    final int maxStack = -1;
    final int maxLocals = -1;
    putAttributes(symbolTable, code, codeLength, maxStack, maxLocals, output);
  }

  /**
   * 将以该属性开头的属性列表的所有属性放在给定的字节向量中。这包括每个属性的6个头字节(attribute_name_index和attribute_length)。
   *
   * Puts all the attributes of the attribute list that begins with this attribute, in the given
   * byte vector. This includes the 6 header bytes (attribute_name_index and attribute_length) per
   * attribute.
   *
   * @param symbolTable where the constants used in the attributes must be stored.
   * @param code the bytecode of the method corresponding to these Code attributes, or {@literal
   *     null} if they are not Code attributes. Corresponds to the 'code' field of the Code
   *     attribute.
   * @param codeLength the length of the bytecode of the method corresponding to these code
   *     attributes, or 0 if they are not Code attributes. Corresponds to the 'code_length' field of
   *     the Code attribute.
   * @param maxStack the maximum stack size of the method corresponding to these Code attributes, or
   *     -1 if they are not Code attributes.
   * @param maxLocals the maximum number of local variables of the method corresponding to these
   *     Code attributes, or -1 if they are not Code attribute.
   * @param output where the attributes must be written.
   */
  final void putAttributes(
      final SymbolTable symbolTable,
      final byte[] code,
      final int codeLength,
      final int maxStack,
      final int maxLocals,
      final ByteVector output) {
    final ClassWriter classWriter = symbolTable.classWriter;
    Attribute attribute = this;
    while (attribute != null) {
      ByteVector attributeContent =
          attribute.write(classWriter, code, codeLength, maxStack, maxLocals);
      // Put attribute_name_index and attribute_length.
      output.putShort(symbolTable.addConstantUtf8(attribute.type)).putInt(attributeContent.length);
      output.putByteArray(attributeContent.data, 0, attributeContent.length);
      attribute = attribute.nextAttribute;
    }
  }

  /**
   * 将与给定字段、方法或类访问标志和签名对应的所有属性放在给定的字节向量中。
   * 这包括每个属性的6个头字节(attribute_name_index和attribute_length)。
   *
   * Puts all the attributes that correspond to the given field, method or class access flags and
   * signature, in the given byte vector. This includes the 6 header bytes (attribute_name_index and
   * attribute_length) per attribute.
   *
   * @param symbolTable where the constants used in the attributes must be stored.
   * @param accessFlags some field, method or class access flags.
   * @param signatureIndex the constant pool index of a field, method of class signature.
   * @param output where the attributes must be written.
   */
  static void putAttributes(
      final SymbolTable symbolTable,
      final int accessFlags,
      final int signatureIndex,
      final ByteVector output) {
    // Before Java 1.5, synthetic fields are represented with a Synthetic attribute.
    if ((accessFlags & Opcodes.ACC_SYNTHETIC) != 0
        && symbolTable.getMajorVersion() < Opcodes.V1_5) {
      output.putShort(symbolTable.addConstantUtf8(Constants.SYNTHETIC)).putInt(0);
    }
    if (signatureIndex != 0) {
      output
          .putShort(symbolTable.addConstantUtf8(Constants.SIGNATURE))
          .putInt(2)
          .putShort(signatureIndex);
    }
    if ((accessFlags & Opcodes.ACC_DEPRECATED) != 0) {
      output.putShort(symbolTable.addConstantUtf8(Constants.DEPRECATED)).putInt(0);
    }
  }

  /** 一组属性原型(具有相同类型的属性被认为是相等的)。 */
  /** A set of attribute prototypes (attributes with the same type are considered equal). */
  static final class Set {

    private static final int SIZE_INCREMENT = 6;

    private int size;
    private Attribute[] data = new Attribute[SIZE_INCREMENT];

    void addAttributes(final Attribute attributeList) {
      Attribute attribute = attributeList;
      while (attribute != null) {
        if (!contains(attribute)) {
          add(attribute);
        }
        attribute = attribute.nextAttribute;
      }
    }

    Attribute[] toArray() {
      Attribute[] result = new Attribute[size];
      System.arraycopy(data, 0, result, 0, size);
      return result;
    }

    private boolean contains(final Attribute attribute) {
      for (int i = 0; i < size; ++i) {
        if (data[i].type.equals(attribute.type)) {
          return true;
        }
      }
      return false;
    }

    private void add(final Attribute attribute) {
      if (size >= data.length) {
        Attribute[] newData = new Attribute[data.length + SIZE_INCREMENT];
        System.arraycopy(data, 0, newData, 0, size);
        data = newData;
      }
      data[size++] = attribute;
    }
  }
}
