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
 * 有关正在{@link ClassReader}中分析的类的信息。
 *
 * Information about a class being parsed in a {@link ClassReader}.
 *
 * @author Eric Bruneton
 */
final class Context {

  /** 必须在此类中分析的属性的原型。 */
  /** The prototypes of the attributes that must be parsed in this class. */
  Attribute[] attributePrototypes;

  /**
   * 用于分析此类的选项。一个或多个{@link ClassReader#SKIP_CODE}、{@link ClassReader#SKIP_DEBUG}、
   * {@link ClassReader#SKIP_FRAMES}、{@link ClassReader#EXPAND_FRAMES}或{@link ClassReader#EXPAND_ASM_INSNS}。
   *
   * The options used to parse this class. One or more of {@link ClassReader#SKIP_CODE}, {@link
   * ClassReader#SKIP_DEBUG}, {@link ClassReader#SKIP_FRAMES}, {@link ClassReader#EXPAND_FRAMES} or
   * {@link ClassReader#EXPAND_ASM_INSNS}.
   */
  int parsingOptions;

  /** 用于读取常量池中字符串的缓冲区。 */
  /** The buffer used to read strings in the constant pool. */
  char[] charBuffer;

  // Information about the current method, i.e. the one read in the current (or latest) call
  // to {@link ClassReader#readMethod()}.

  /** 当前方法的访问标志。 */
  /** The access flags of the current method. */
  int currentMethodAccessFlags;

  /** 当前方法的名称。 */
  /** The name of the current method. */
  String currentMethodName;

  /** 当前方法的描述符。 */
  /** The descriptor of the current method. */
  String currentMethodDescriptor;

  /**
   * 按字节码偏移量索引的当前方法的标签（只有需要标签的字节码偏移量才具有非空关联标签）。
   *
   * The labels of the current method, indexed by bytecode offset (only bytecode offsets for which a
   * label is needed have a non null associated Label).
   */
  Label[] currentMethodLabels;

  // Information about the current type annotation target, i.e. the one read in the current
  // (or latest) call to {@link ClassReader#readAnnotationTarget()}.

  /**
   * 当前类型批注目标的目标类型和目标信息，编码如{@link TypeReference}中所述。
   *
   * The target_type and target_info of the current type annotation target, encoded as described in
   * {@link TypeReference}.
   */
  int currentTypeAnnotationTarget;

  /** 当前类型批注目标的目标路径。 */
  /** The target_path of the current type annotation target. */
  TypePath currentTypeAnnotationTargetPath;

  /** 当前局部变量批注中每个局部变量范围的开始。 */
  /** The start of each local variable range in the current local variable annotation. */
  Label[] currentLocalVariableAnnotationRangeStarts;

  /** 当前局部变量批注中每个局部变量范围的结束。 */
  /** The end of each local variable range in the current local variable annotation. */
  Label[] currentLocalVariableAnnotationRangeEnds;

  /**
   * 当前局部变量批注中每个局部变量范围的局部变量索引。
   * The local variable index of each local variable range in the current local variable annotation.
   */
  int[] currentLocalVariableAnnotationRangeIndices;

  // Information about the current stack map frame, i.e. the one read in the current (or latest)
  // call to {@link ClassReader#readFrame()}.

  /** 当前堆栈映射帧的字节码偏移量。 */
  /** The bytecode offset of the current stack map frame. */
  int currentFrameOffset;

  /**
   * 当前堆栈映射帧的类型。{@link Opcodes#F_FULL}、{@link Opcodes#F_APPEND}、{@link Opcodes#F_CHOP}、
   * {@link Opcodes#F_SAME}或{@link Opcodes#F_SAME1}中的一个。
   *
   * The type of the current stack map frame. One of {@link Opcodes#F_FULL}, {@link
   * Opcodes#F_APPEND}, {@link Opcodes#F_CHOP}, {@link Opcodes#F_SAME} or {@link Opcodes#F_SAME1}.
   */
  int currentFrameType;

  /**
   * 当前堆栈映射帧中本地变量类型的数目。每种类型都用一个数组元素表示（甚至是long数组和double数组）。
   *
   * The number of local variable types in the current stack map frame. Each type is represented
   * with a single array element (even long and double).
   */
  int currentFrameLocalCount;

  /**
   * 当前堆栈映射帧中本地变量类型的增量数（每种类型都用单个数组元素表示，甚至是长数组和双数组元素）。这是此帧中的局部变量类型数，减去前一帧中的局部变量类型数。
   *
   * The delta number of local variable types in the current stack map frame (each type is
   * represented with a single array element - even long and double). This is the number of local
   * variable types in this frame, minus the number of local variable types in the previous frame.
   */
  int currentFrameLocalCountDelta;

  /**
   * 当前堆栈映射帧中本地变量的类型。每种类型都用一个数组元素（甚至是长数组和双数组）表示，
   * 使用{@link MethodVisitor#visitFrame}中描述的格式。根据{@link #currentFrameType}，这包含所有局部变量的类型，
   * 或仅包含附加变量的类型（与前一帧相比）。
   *
   * The types of the local variables in the current stack map frame. Each type is represented with
   * a single array element (even long and double), using the format described in {@link
   * MethodVisitor#visitFrame}. Depending on {@link #currentFrameType}, this contains the types of
   * all the local variables, or only those of the additional ones (compared to the previous frame).
   */
  Object[] currentFrameLocalTypes;

  /**
   * 当前堆栈映射帧中的数字堆栈元素类型。每种类型都用一个数组元素表示（甚至是长数组和双数组）。
   *
   * The number stack element types in the current stack map frame. Each type is represented with a
   * single array element (even long and double).
   */
  int currentFrameStackCount;

  /**
   * 当前堆栈映射帧中堆栈元素的类型。每种类型都用一个数组元素（甚至是长数组和双数组）表示，使用{@link MethodVisitor#visitFrame}中描述的格式。
   *
   * The types of the stack elements in the current stack map frame. Each type is represented with a
   * single array element (even long and double), using the format described in {@link
   * MethodVisitor#visitFrame}.
   */
  Object[] currentFrameStackTypes;
}
