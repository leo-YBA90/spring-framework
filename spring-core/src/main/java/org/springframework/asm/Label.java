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
 * 方法字节码中的位置。标签用于跳转、转到和切换指令，也用于try-catch块。标签指定后面的指令。
 * 但是，请注意，在标签和它指定的指令之间可以有其他元素（例如其他标签、堆栈映射帧、行号等）。
 *
 * A position in the bytecode of a method. Labels are used for jump, goto, and switch instructions,
 * and for try catch blocks. A label designates the <i>instruction</i> that is just after. Note
 * however that there can be other elements between a label and the instruction it designates (such
 * as other labels, stack map frames, line numbers, etc.).
 *
 * @author Eric Bruneton
 */
public class Label {

  /**
   * 指示标签仅用于调试属性的标志。这样的标签不是基本块的开始、跳转指令的目标或异常处理程序。
   * 在控制流图分析算法中可以安全地忽略它（用于优化目的）。
   *
   * A flag indicating that a label is only used for debug attributes. Such a label is not the start
   * of a basic block, the target of a jump instruction, or an exception handler. It can be safely
   * ignored in control flow graph analysis algorithms (for optimization purposes).
   */
  static final int FLAG_DEBUG_ONLY = 1;

  /**
   * 指示标签是跳转指令目标或异常处理程序开始的标志。
   *
   * A flag indicating that a label is the target of a jump instruction, or the start of an
   * exception handler.
   */
  static final int FLAG_JUMP_TARGET = 2;

  /** 指示标签字节码偏移量已知的标志。 */
  /** A flag indicating that the bytecode offset of a label is known. */
  static final int FLAG_RESOLVED = 4;

  /** 一种标志，表示一个标签对应于一个可到达的基本块。 */
  /** A flag indicating that a label corresponds to a reachable basic block. */
  static final int FLAG_REACHABLE = 8;

  /**
   * 表示与标签相对应的基本块以子例程调用结束的标志。通过在{@link MethodWriter#visitJumpInsn}中构造，
   * 具有此标志集的标签至少有两条传出边：
   * 第一条对应于字节码中jsr指令后面的指令，即当执行从jsr调用返回时继续执行的指令。
   * 这是一个虚拟的控制流边缘，因为执行从不直接从jsr转到下一条指令。
   * 相反，它转到子例程并最终返回jsr之后的指令。这个虚拟边用于计算以ret指令结尾的基本块的实际传出边，
   * 在{@link #addSubroutineRetSuccessors}。
   * 第二个对应于jsr指令的目标，
   *
   * A flag indicating that the basic block corresponding to a label ends with a subroutine call. By
   * construction in {@link MethodWriter#visitJumpInsn}, labels with this flag set have at least two
   * outgoing edges:
   *
   * <ul>
   *   <li>the first one corresponds to the instruction that follows the jsr instruction in the
   *       bytecode, i.e. where execution continues when it returns from the jsr call. This is a
   *       virtual control flow edge, since execution never goes directly from the jsr to the next
   *       instruction. Instead, it goes to the subroutine and eventually returns to the instruction
   *       following the jsr. This virtual edge is used to compute the real outgoing edges of the
   *       basic blocks ending with a ret instruction, in {@link #addSubroutineRetSuccessors}.
   *   <li>the second one corresponds to the target of the jsr instruction,
   * </ul>
   */
  static final int FLAG_SUBROUTINE_CALLER = 16;

  /**
   * 表示与标签相对应的基本块是子例程的开始的标志。
   * A flag indicating that the basic block corresponding to a label is the start of a subroutine.
   */
  static final int FLAG_SUBROUTINE_START = 32;

  /** 一种标志，表示与标签相对应的基本块是子例程的结尾。 */
  /** A flag indicating that the basic block corresponding to a label is the end of a subroutine. */
  static final int FLAG_SUBROUTINE_END = 64;

  /**
   * 当{@link #otherLineNumbers}数组需要调整大小以存储新源行号时，要添加到该数组的元素数。
   *
   * The number of elements to add to the {@link #otherLineNumbers} array when it needs to be
   * resized to store a new source line number.
   */
  static final int LINE_NUMBERS_CAPACITY_INCREMENT = 4;

  /**
   * 当需要调整大小以存储新的转发引用时，要添加到{@link #forwardReferences}数组中的元素数。
   *
   * The number of elements to add to the {@link #forwardReferences} array when it needs to be
   * resized to store a new forward reference.
   */
  static final int FORWARD_REFERENCES_CAPACITY_INCREMENT = 6;

  /**
   * 用于提取对此标签的正向引用类型的位掩码。提取的类型为{@link #FORWARD_REFERENCE_TYPE_SHORT}
   * 或{@link #FORWARD_REFERENCE_TYPE_WIDE}。
   *
   * The bit mask to extract the type of a forward reference to this label. The extracted type is
   * either {@link #FORWARD_REFERENCE_TYPE_SHORT} or {@link #FORWARD_REFERENCE_TYPE_WIDE}.
   *
   * @see #forwardReferences
   */
  static final int FORWARD_REFERENCE_TYPE_MASK = 0xF0000000;

  /**
   * 字节码中以两个字节存储的前向引用的类型。例如，这是ifnull指令的前向引用。
   *
   * The type of forward references stored with two bytes in the bytecode. This is the case, for
   * instance, of a forward reference from an ifnull instruction.
   */
  static final int FORWARD_REFERENCE_TYPE_SHORT = 0x10000000;

  /**
   * 字节码中以四个字节存储的转发引用的类型。例如，lookupswitch指令的前向引用就是这种情况。
   *
   * The type of forward references stored in four bytes in the bytecode. This is the case, for
   * instance, of a forward reference from a lookupswitch instruction.
   */
  static final int FORWARD_REFERENCE_TYPE_WIDE = 0x20000000;

  /**
   * 提取对此标签的正向引用的“句柄”的位掩码。提取的句柄是存储前向引用值的字节码偏移量（使用2或4个字节，
   * 如{@link #FORWARD_REFERENCE_TYPE_MASK}所示）。
   *
   * The bit mask to extract the 'handle' of a forward reference to this label. The extracted handle
   * is the bytecode offset where the forward reference value is stored (using either 2 or 4 bytes,
   * as indicated by the {@link #FORWARD_REFERENCE_TYPE_MASK}).
   *
   * @see #forwardReferences
   */
  static final int FORWARD_REFERENCE_HANDLE_MASK = 0x0FFFFFFF;

  /**
   * 用于指示标签列表结尾的sentinel元素。
   *
   * A sentinel element used to indicate the end of a list of labels.
   *
   * @see #nextListElement
   */
  static final Label EMPTY_LIST = new Label();

  /**
   * 与此标签关联的用户管理的状态。警告：此字段由ASM树包使用。要将其与ASM树包一起使用，
   * 必须重写MethodNode中的getLabelNode方法。
   *
   * A user managed state associated with this label. Warning: this field is used by the ASM tree
   * package. In order to use it with the ASM tree package you must override the getLabelNode method
   * in MethodNode.
   */
  public Object info;

  /**
   * 此标签或其相应基本块的类型和状态。必须是{@link #FLAG_DEBUG_ONLY}、{@link #FLAG_JUMP_TARGET}、{@link #FLAG_RESOLVED}、
   * {@link #FLAG_REACHABLE}、{@link #FLAG_SUBROUTINE_CALLER}、{@link #FLAG_SUBROUTINE_START}、{@link #FLAG_SUBROUTINE_END}中的零或多个。
   *
   * The type and status of this label or its corresponding basic block. Must be zero or more of
   * {@link #FLAG_DEBUG_ONLY}, {@link #FLAG_JUMP_TARGET}, {@link #FLAG_RESOLVED}, {@link
   * #FLAG_REACHABLE}, {@link #FLAG_SUBROUTINE_CALLER}, {@link #FLAG_SUBROUTINE_START}, {@link
   * #FLAG_SUBROUTINE_END}.
   */
  short flags;

  /**
   * 与此标签对应的源行号，或0。如果此标签对应多个源行号，则第一个源行号存储在此字段中，
   * 其余的源行号存储在{@link #otherLineNumbers}。
   *
   * The source line number corresponding to this label, or 0. If there are several source line
   * numbers corresponding to this label, the first one is stored in this field, and the remaining
   * ones are stored in {@link #otherLineNumbers}.
   */
  private short lineNumber;

  /**
   * The source line numbers corresponding to this label, in addition to {@link #lineNumber}, or
   * null. The first element of this array is the number n of source line numbers it contains, which
   * are stored between indices 1 and n (inclusive).
   */
  private int[] otherLineNumbers;

  /**
   * 此标签在其方法字节码中的偏移量（字节）。仅当设置了{@link #FLAG_RESOLVED}标志时才设置此值。
   *
   * The offset of this label in the bytecode of its method, in bytes. This value is set if and only
   * if the {@link #FLAG_RESOLVED} flag is set.
   */
  int bytecodeOffset;

  /**
   * todo
   *
   * The forward references to this label. The first element is the number of forward references,
   * times 2 (this corresponds to the index of the last element actually used in this array). Then,
   * each forward reference is described with two consecutive integers noted
   * 'sourceInsnBytecodeOffset' and 'reference':
   *
   * <ul>
   *   <li>'sourceInsnBytecodeOffset' is the bytecode offset of the instruction that contains the
   *       forward reference,
   *   <li>'reference' contains the type and the offset in the bytecode where the forward reference
   *       value must be stored, which can be extracted with {@link #FORWARD_REFERENCE_TYPE_MASK}
   *       and {@link #FORWARD_REFERENCE_HANDLE_MASK}.
   * </ul>
   *
   * <p>For instance, for an ifnull instruction at bytecode offset x, 'sourceInsnBytecodeOffset' is
   * equal to x, and 'reference' is of type {@link #FORWARD_REFERENCE_TYPE_SHORT} with value x + 1
   * (because the ifnull instruction uses a 2 bytes bytecode offset operand stored one byte after
   * the start of the instruction itself). For the default case of a lookupswitch instruction at
   * bytecode offset x, 'sourceInsnBytecodeOffset' is equal to x, and 'reference' is of type {@link
   * #FORWARD_REFERENCE_TYPE_WIDE} with value between x + 1 and x + 4 (because the lookupswitch
   * instruction uses a 4 bytes bytecode offset operand stored one to four bytes after the start of
   * the instruction itself).
   */
  private int[] forwardReferences;

  // -----------------------------------------------------------------------------------------------

  // Fields for the control flow and data flow graph analysis algorithms (used to compute the
  // maximum stack size or the stack map frames). A control flow graph contains one node per "basic
  // block", and one edge per "jump" from one basic block to another. Each node (i.e., each basic
  // block) is represented with the Label object that corresponds to the first instruction of this
  // basic block. Each node also stores the list of its successors in the graph, as a linked list of
  // Edge objects.
  //
  // The control flow analysis algorithms used to compute the maximum stack size or the stack map
  // frames are similar and use two steps. The first step, during the visit of each instruction,
  // builds information about the state of the local variables and the operand stack at the end of
  // each basic block, called the "output frame", <i>relatively</i> to the frame state at the
  // beginning of the basic block, which is called the "input frame", and which is <i>unknown</i>
  // during this step. The second step, in {@link MethodWriter#computeAllFrames} and {@link
  // MethodWriter#computeMaxStackAndLocal}, is a fix point algorithm
  // that computes information about the input frame of each basic block, from the input state of
  // the first basic block (known from the method signature), and by the using the previously
  // computed relative output frames.
  //
  // The algorithm used to compute the maximum stack size only computes the relative output and
  // absolute input stack heights, while the algorithm used to compute stack map frames computes
  // relative output frames and absolute input frames.

  /**
   * 与此标签对应的基本块输入堆栈中的元素数。此字段在{@link MethodWriter#computeMaxStackAndLocal}中计算。
   *
   * The number of elements in the input stack of the basic block corresponding to this label. This
   * field is computed in {@link MethodWriter#computeMaxStackAndLocal}.
   */
  short inputStackSize;

  /**
   * 输出堆栈中与此标签对应的基本块末尾的元素数。此字段仅为以RET指令结尾的基本块计算。
   *
   * The number of elements in the output stack, at the end of the basic block corresponding to this
   * label. This field is only computed for basic blocks that end with a RET instruction.
   */
  short outputStackSize;

  /**
   * 输出堆栈所达到的最大高度，相对于输入堆栈的顶部，在与此标签相对应的基本块中。这一最大值总是正的，或者是{@literal null}。
   *
   * The maximum height reached by the output stack, relatively to the top of the input stack, in
   * the basic block corresponding to this label. This maximum is always positive or {@literal
   * null}.
   */
  short outputStackMax;

  /**
   * 此基本块所属的子例程的id，或0。如果基本块属于多个子例程，则这是包含它的“最旧”子例程的id（按照调用另一个子例程
   * 的子例程比被调用方“旧”的约定）。如果方法包含JSR指令，则在{@link MethodWriter#computeMaxStackAndLocal}中计算此字段。
   *
   * The id of the subroutine to which this basic block belongs, or 0. If the basic block belongs to
   * several subroutines, this is the id of the "oldest" subroutine that contains it (with the
   * convention that a subroutine calling another one is "older" than the callee). This field is
   * computed in {@link MethodWriter#computeMaxStackAndLocal}, if the method contains JSR
   * instructions.
   */
  short subroutineId;

  /**
   * 与此标签对应的基本块的输入和输出堆栈映射帧。此字段仅在使用{@link MethodWriter#COMPUTE_ALL_FRAMES}
   * 或{@link MethodWriter#COMPUTE_INSERTED_FRAMES}选项时使用。
   *
   * The input and output stack map frames of the basic block corresponding to this label. This
   * field is only used when the {@link MethodWriter#COMPUTE_ALL_FRAMES} or {@link
   * MethodWriter#COMPUTE_INSERTED_FRAMES} option is used.
   */
  Frame frame;

  /**
   * 此标签的继承者，按其在{@link MethodVisitor#visitLabel}中访问的顺序排列。此链接列表不包括仅用于调试信息的标签。
   * 如果使用{@link MethodWriter#COMPUTE_ALL_FRAMES}或{@link MethodWriter#COMPUTE_INSERTED_FRAMES}选项，
   * 则它不包含表示相同字节码偏移量的连续标签（在这种情况下，此列表中只显示第一个标签）。
   *
   * The successor of this label, in the order they are visited in {@link MethodVisitor#visitLabel}.
   * This linked list does not include labels used for debug info only. If the {@link
   * MethodWriter#COMPUTE_ALL_FRAMES} or {@link MethodWriter#COMPUTE_INSERTED_FRAMES} option is used
   * then it does not contain either successive labels that denote the same bytecode offset (in this
   * case only the first label appears in this list).
   */
  Label nextBasicBlock;

  /**
   * 在其方法的控制流图中，与此标签相对应的基本块的传出边。这些边存储在由{@link Edge}对象组成的链接列表中，
   * 这些对象通过它们的{@link Edge#nextEdge}字段彼此链接。
   *
   * The outgoing edges of the basic block corresponding to this label, in the control flow graph of
   * its method. These edges are stored in a linked list of {@link Edge} objects, linked to each
   * other by their {@link Edge#nextEdge} field.
   */
  Edge outgoingEdges;

  /**
   * 此标签所属标签列表中的下一个元素，如果不属于任何列表，则为{@literal null}.
   * 所有标签列表必须以{@link #EMPTY_LIST}sentinel结尾，以确保此字段在且仅当此标签不属于标签列表时为空。
   * 请注意，可以同时有多个标签列表，但一个标签一次最多只能属于一个列表（除非某些列表有一个共同的尾部，但这在实践中没有使用）。
   * 标签的列表分别用2×3和α3来计算堆栈映射帧和最大堆栈大小，以及在第4×5和α5中计算属于子程序和它们的输出边的基本块。
   * 在这些方法之外，此字段应为空（此属性是这些方法的前置条件和后置条件）。
   *
   * The next element in the list of labels to which this label belongs, or {@literal null} if it
   * does not belong to any list. All lists of labels must end with the {@link #EMPTY_LIST}
   * sentinel, in order to ensure that this field is null if and only if this label does not belong
   * to a list of labels. Note that there can be several lists of labels at the same time, but that
   * a label can belong to at most one list at a time (unless some lists share a common tail, but
   * this is not used in practice).
   *
   * <p>List of labels are used in {@link MethodWriter#computeAllFrames} and {@link
   * MethodWriter#computeMaxStackAndLocal} to compute stack map frames and the maximum stack size,
   * respectively, as well as in {@link #markSubroutine} and {@link #addSubroutineRetSuccessors} to
   * compute the basic blocks belonging to subroutines and their outgoing edges. Outside of these
   * methods, this field should be null (this property is a precondition and a postcondition of
   * these methods).
   */
  Label nextListElement;

  // -----------------------------------------------------------------------------------------------
  // Constructor and accessors
  // -----------------------------------------------------------------------------------------------

  /** Constructs a new label. */
  public Label() {
    // Nothing to do.
  }

  /**
   * 返回与此标签对应的字节码偏移量。此偏移量是从方法字节码的开头开始计算的。此方法用于{@link Attribute}子类，
   * 类生成器或适配器通常不需要此方法。
   *
   * Returns the bytecode offset corresponding to this label. This offset is computed from the start
   * of the method's bytecode. <i>This method is intended for {@link Attribute} sub classes, and is
   * normally not needed by class generators or adapters.</i>
   *
   * @return the bytecode offset corresponding to this label.
   * @throws IllegalStateException if this label is not resolved yet.
   */
  public int getOffset() {
    if ((flags & FLAG_RESOLVED) == 0) {
      throw new IllegalStateException("Label offset position has not been resolved yet");
    }
    return bytecodeOffset;
  }

  /**'
   * 返回与此标签的字节码偏移量（如果已知）对应的“规范”{@link Label} 实例，否则返回标签本身。
   * 规范实例是与此字节码偏移量相对应的第一个标签（按访问顺序为{@link MethodVisitor#visitLabel}）。
   * 它不可能以尚未被访问的标签而闻名。仅当使用{@link MethodWriter#COMPUTE_ALL_FRAMES}选项时才应使用此方法。
   *
   * Returns the "canonical" {@link Label} instance corresponding to this label's bytecode offset,
   * if known, otherwise the label itself. The canonical instance is the first label (in the order
   * of their visit by {@link MethodVisitor#visitLabel}) corresponding to this bytecode offset. It
   * cannot be known for labels which have not been visited yet.
   *
   * <p><i>This method should only be used when the {@link MethodWriter#COMPUTE_ALL_FRAMES} option
   * is used.</i>
   *
   * @return the label itself if {@link #frame} is null, otherwise the Label's frame owner. This
   *     corresponds to the "canonical" label instance described above thanks to the way the label
   *     frame is set in {@link MethodWriter#visitLabel}.
   */
  final Label getCanonicalInstance() {
    return frame == null ? this : frame.owner;
  }

  // -----------------------------------------------------------------------------------------------
  // Methods to manage line numbers
  // -----------------------------------------------------------------------------------------------

  /**
   * 添加与此标签对应的源行号。
   *
   * Adds a source line number corresponding to this label.
   *
   * @param lineNumber a source line number (which should be strictly positive).
   */
  final void addLineNumber(final int lineNumber) {
    if (this.lineNumber == 0) {
      this.lineNumber = (short) lineNumber;
    } else {
      if (otherLineNumbers == null) {
        otherLineNumbers = new int[LINE_NUMBERS_CAPACITY_INCREMENT];
      }
      int otherLineNumberIndex = ++otherLineNumbers[0];
      if (otherLineNumberIndex >= otherLineNumbers.length) {
        int[] newLineNumbers = new int[otherLineNumbers.length + LINE_NUMBERS_CAPACITY_INCREMENT];
        System.arraycopy(otherLineNumbers, 0, newLineNumbers, 0, otherLineNumbers.length);
        otherLineNumbers = newLineNumbers;
      }
      otherLineNumbers[otherLineNumberIndex] = lineNumber;
    }
  }

  /**
   * 使给定的访问者访问此标签及其源行号（如果适用）。
   *
   * Makes the given visitor visit this label and its source line numbers, if applicable.
   *
   * @param methodVisitor a method visitor.
   * @param visitLineNumbers whether to visit of the label's source line numbers, if any.
   */
  final void accept(final MethodVisitor methodVisitor, final boolean visitLineNumbers) {
    methodVisitor.visitLabel(this);
    if (visitLineNumbers && lineNumber != 0) {
      methodVisitor.visitLineNumber(lineNumber & 0xFFFF, this);
      if (otherLineNumbers != null) {
        for (int i = 1; i <= otherLineNumbers[0]; ++i) {
          methodVisitor.visitLineNumber(otherLineNumbers[i], this);
        }
      }
    }
  }

  // -----------------------------------------------------------------------------------------------
  // Methods to compute offsets and to manage forward references
  // -----------------------------------------------------------------------------------------------

  /**
   * 在方法的字节码中放置对此标签的引用。如果标签的字节码偏移量已知，则直接计算并写入标签与引用它的指令之间的相对字节码偏移量。
   * 否则，将写入空的相对偏移量，并为此标签声明新的正向引用。
   *
   * Puts a reference to this label in the bytecode of a method. If the bytecode offset of the label
   * is known, the relative bytecode offset between the label and the instruction referencing it is
   * computed and written directly. Otherwise, a null relative offset is written and a new forward
   * reference is declared for this label.
   *
   * @param code the bytecode of the method. This is where the reference is appended.
   * @param sourceInsnBytecodeOffset the bytecode offset of the instruction that contains the
   *     reference to be appended.
   * @param wideReference whether the reference must be stored in 4 bytes (instead of 2 bytes).
   */
  final void put(
      final ByteVector code, final int sourceInsnBytecodeOffset, final boolean wideReference) {
    if ((flags & FLAG_RESOLVED) == 0) {
      if (wideReference) {
        addForwardReference(sourceInsnBytecodeOffset, FORWARD_REFERENCE_TYPE_WIDE, code.length);
        code.putInt(-1);
      } else {
        addForwardReference(sourceInsnBytecodeOffset, FORWARD_REFERENCE_TYPE_SHORT, code.length);
        code.putShort(-1);
      }
    } else {
      if (wideReference) {
        code.putInt(bytecodeOffset - sourceInsnBytecodeOffset);
      } else {
        code.putShort(bytecodeOffset - sourceInsnBytecodeOffset);
      }
    }
  }

  /**
   * 将正向引用添加到此标签。必须仅为真正的正向引用调用此方法，即仅当尚未解析此标签时才调用此方法。
   * 对于反向引用，引用的相对字节码偏移量可以而且必须直接计算和存储。
   *
   * Adds a forward reference to this label. This method must be called only for a true forward
   * reference, i.e. only if this label is not resolved yet. For backward references, the relative
   * bytecode offset of the reference can be, and must be, computed and stored directly.
   *
   * @param sourceInsnBytecodeOffset the bytecode offset of the instruction that contains the
   *     reference stored at referenceHandle.
   * @param referenceType either {@link #FORWARD_REFERENCE_TYPE_SHORT} or {@link
   *     #FORWARD_REFERENCE_TYPE_WIDE}.
   * @param referenceHandle the offset in the bytecode where the forward reference value must be
   *     stored.
   */
  private void addForwardReference(
      final int sourceInsnBytecodeOffset, final int referenceType, final int referenceHandle) {
    if (forwardReferences == null) {
      forwardReferences = new int[FORWARD_REFERENCES_CAPACITY_INCREMENT];
    }
    int lastElementIndex = forwardReferences[0];
    if (lastElementIndex + 2 >= forwardReferences.length) {
      int[] newValues = new int[forwardReferences.length + FORWARD_REFERENCES_CAPACITY_INCREMENT];
      System.arraycopy(forwardReferences, 0, newValues, 0, forwardReferences.length);
      forwardReferences = newValues;
    }
    forwardReferences[++lastElementIndex] = sourceInsnBytecodeOffset;
    forwardReferences[++lastElementIndex] = referenceType | referenceHandle;
    forwardReferences[0] = lastElementIndex;
  }

  /**
   * 将此标签的字节码偏移量设置为给定值，并解析对此标签的正向引用（如果有）。当此标签添加到方法的字节码时
   * （即当其字节码偏移量已知时），必须调用此方法。此方法将先前添加到该标签的前向引用填充在字节码中剩下的空白处。
   *
   * Sets the bytecode offset of this label to the given value and resolves the forward references
   * to this label, if any. This method must be called when this label is added to the bytecode of
   * the method, i.e. when its bytecode offset becomes known. This method fills in the blanks that
   * where left in the bytecode by each forward reference previously added to this label.
   *
   * @param code the bytecode of the method.
   * @param bytecodeOffset the bytecode offset of this label.
   * @return {@literal true} if a blank that was left for this label was too small to store the
   *     offset. In such a case the corresponding jump instruction is replaced with an equivalent
   *     ASM specific instruction using an unsigned two bytes offset. These ASM specific
   *     instructions are later replaced with standard bytecode instructions with wider offsets (4
   *     bytes instead of 2), in ClassReader.
   */
  final boolean resolve(final byte[] code, final int bytecodeOffset) {
    this.flags |= FLAG_RESOLVED;
    this.bytecodeOffset = bytecodeOffset;
    if (forwardReferences == null) {
      return false;
    }
    boolean hasAsmInstructions = false;
    for (int i = forwardReferences[0]; i > 0; i -= 2) {
      final int sourceInsnBytecodeOffset = forwardReferences[i - 1];
      final int reference = forwardReferences[i];
      final int relativeOffset = bytecodeOffset - sourceInsnBytecodeOffset;
      int handle = reference & FORWARD_REFERENCE_HANDLE_MASK;
      if ((reference & FORWARD_REFERENCE_TYPE_MASK) == FORWARD_REFERENCE_TYPE_SHORT) {
        if (relativeOffset < Short.MIN_VALUE || relativeOffset > Short.MAX_VALUE) {
          // Change the opcode of the jump instruction, in order to be able to find it later in
          // ClassReader. These ASM specific opcodes are similar to jump instruction opcodes, except
          // that the 2 bytes offset is unsigned (and can therefore represent values from 0 to
          // 65535, which is sufficient since the size of a method is limited to 65535 bytes).
          int opcode = code[sourceInsnBytecodeOffset] & 0xFF;
          if (opcode < Opcodes.IFNULL) {
            // Change IFEQ ... JSR to ASM_IFEQ ... ASM_JSR.
            code[sourceInsnBytecodeOffset] = (byte) (opcode + Constants.ASM_OPCODE_DELTA);
          } else {
            // Change IFNULL and IFNONNULL to ASM_IFNULL and ASM_IFNONNULL.
            code[sourceInsnBytecodeOffset] = (byte) (opcode + Constants.ASM_IFNULL_OPCODE_DELTA);
          }
          hasAsmInstructions = true;
        }
        code[handle++] = (byte) (relativeOffset >>> 8);
        code[handle] = (byte) relativeOffset;
      } else {
        code[handle++] = (byte) (relativeOffset >>> 24);
        code[handle++] = (byte) (relativeOffset >>> 16);
        code[handle++] = (byte) (relativeOffset >>> 8);
        code[handle] = (byte) relativeOffset;
      }
    }
    return hasAsmInstructions;
  }

  // -----------------------------------------------------------------------------------------------
  // Methods related to subroutines
  // -----------------------------------------------------------------------------------------------

  /**
   * 查找从与此标签对应的基本块开始的属于子例程的基本块，并将这些块标记为属于此子例程。此方法遵循控制流图，
   * 查找当前基本块中可访问的所有块，而不遵循任何jsr目标。
   * 注意：此方法的先决条件和后决条件是，所有标签都必须具有空{@link #nextListElement}。
   *
   * Finds the basic blocks that belong to the subroutine starting with the basic block
   * corresponding to this label, and marks these blocks as belonging to this subroutine. This
   * method follows the control flow graph to find all the blocks that are reachable from the
   * current basic block WITHOUT following any jsr target.
   *
   * <p>Note: a precondition and postcondition of this method is that all labels must have a null
   * {@link #nextListElement}.
   *
   * @param subroutineId the id of the subroutine starting with the basic block corresponding to
   *     this label.
   */
  final void markSubroutine(final short subroutineId) {
    // Data flow algorithm: put this basic block in a list of blocks to process (which are blocks
    // belonging to subroutine subroutineId) and, while there are blocks to process, remove one from
    // the list, mark it as belonging to the subroutine, and add its successor basic blocks in the
    // control flow graph to the list of blocks to process (if not already done).
    Label listOfBlocksToProcess = this;
    listOfBlocksToProcess.nextListElement = EMPTY_LIST;
    while (listOfBlocksToProcess != EMPTY_LIST) {
      // Remove a basic block from the list of blocks to process.
      Label basicBlock = listOfBlocksToProcess;
      listOfBlocksToProcess = listOfBlocksToProcess.nextListElement;
      basicBlock.nextListElement = null;

      // If it is not already marked as belonging to a subroutine, mark it as belonging to
      // subroutineId and add its successors to the list of blocks to process (unless already done).
      if (basicBlock.subroutineId == 0) {
        basicBlock.subroutineId = subroutineId;
        listOfBlocksToProcess = basicBlock.pushSuccessors(listOfBlocksToProcess);
      }
    }
  }

  /**
   * 查找以与此标签对应的基本块开始的子例程结尾的基本块，并为每个基本块在给定子例程调用之后向基本块添加一个传出边。
   * 换句话说，当从给定的调用方基本块调用时，通过添加与此子例程的返回相对应的边来完成控制流图。
   * 注意：此方法的先决条件和后决条件是，所有标签都必须具有空{@link #nextListElement}.
   *
   * Finds the basic blocks that end a subroutine starting with the basic block corresponding to
   * this label and, for each one of them, adds an outgoing edge to the basic block following the
   * given subroutine call. In other words, completes the control flow graph by adding the edges
   * corresponding to the return from this subroutine, when called from the given caller basic
   * block.
   *
   * <p>Note: a precondition and postcondition of this method is that all labels must have a null
   * {@link #nextListElement}.
   *
   * @param subroutineCaller a basic block that ends with a jsr to the basic block corresponding to
   *     this label. This label is supposed to correspond to the start of a subroutine.
   */
  final void addSubroutineRetSuccessors(final Label subroutineCaller) {
    // Data flow algorithm: put this basic block in a list blocks to process (which are blocks
    // belonging to a subroutine starting with this label) and, while there are blocks to process,
    // remove one from the list, put it in a list of blocks that have been processed, add a return
    // edge to the successor of subroutineCaller if applicable, and add its successor basic blocks
    // in the control flow graph to the list of blocks to process (if not already done).
    Label listOfProcessedBlocks = EMPTY_LIST;
    Label listOfBlocksToProcess = this;
    listOfBlocksToProcess.nextListElement = EMPTY_LIST;
    while (listOfBlocksToProcess != EMPTY_LIST) {
      // Move a basic block from the list of blocks to process to the list of processed blocks.
      Label basicBlock = listOfBlocksToProcess;
      listOfBlocksToProcess = basicBlock.nextListElement;
      basicBlock.nextListElement = listOfProcessedBlocks;
      listOfProcessedBlocks = basicBlock;

      // Add an edge from this block to the successor of the caller basic block, if this block is
      // the end of a subroutine and if this block and subroutineCaller do not belong to the same
      // subroutine.
      if ((basicBlock.flags & FLAG_SUBROUTINE_END) != 0
          && basicBlock.subroutineId != subroutineCaller.subroutineId) {
        basicBlock.outgoingEdges =
            new Edge(
                basicBlock.outputStackSize,
                // By construction, the first outgoing edge of a basic block that ends with a jsr
                // instruction leads to the jsr continuation block, i.e. where execution continues
                // when ret is called (see {@link #FLAG_SUBROUTINE_CALLER}).
                subroutineCaller.outgoingEdges.successor,
                basicBlock.outgoingEdges);
      }
      // Add its successors to the list of blocks to process. Note that {@link #pushSuccessors} does
      // not push basic blocks which are already in a list. Here this means either in the list of
      // blocks to process, or in the list of already processed blocks. This second list is
      // important to make sure we don't reprocess an already processed block.
      listOfBlocksToProcess = basicBlock.pushSuccessors(listOfBlocksToProcess);
    }
    // Reset the {@link #nextListElement} of all the basic blocks that have been processed to null,
    // so that this method can be called again with a different subroutine or subroutine caller.
    while (listOfProcessedBlocks != EMPTY_LIST) {
      Label newListOfProcessedBlocks = listOfProcessedBlocks.nextListElement;
      listOfProcessedBlocks.nextListElement = null;
      listOfProcessedBlocks = newListOfProcessedBlocks;
    }
  }

  /**
   * 将该标签在方法的控制流图中的后续项（与jsr目标相对应的后续项和已在标签列表中的后续项除外）添加到要处理的给定块列表中，
   * 并返回新列表。
   *
   * Adds the successors of this label in the method's control flow graph (except those
   * corresponding to a jsr target, and those already in a list of labels) to the given list of
   * blocks to process, and returns the new list.
   *
   * @param listOfLabelsToProcess a list of basic blocks to process, linked together with their
   *     {@link #nextListElement} field.
   * @return the new list of blocks to process.
   */
  private Label pushSuccessors(final Label listOfLabelsToProcess) {
    Label newListOfLabelsToProcess = listOfLabelsToProcess;
    Edge outgoingEdge = outgoingEdges;
    while (outgoingEdge != null) {
      // By construction, the second outgoing edge of a basic block that ends with a jsr instruction
      // leads to the jsr target (see {@link #FLAG_SUBROUTINE_CALLER}).
      boolean isJsrTarget =
          (flags & Label.FLAG_SUBROUTINE_CALLER) != 0 && outgoingEdge == outgoingEdges.nextEdge;
      if (!isJsrTarget && outgoingEdge.successor.nextListElement == null) {
        // Add this successor to the list of blocks to process, if it does not already belong to a
        // list of labels.
        outgoingEdge.successor.nextListElement = newListOfLabelsToProcess;
        newListOfLabelsToProcess = outgoingEdge.successor;
      }
      outgoingEdge = outgoingEdge.nextEdge;
    }
    return newListOfLabelsToProcess;
  }

  // -----------------------------------------------------------------------------------------------
  // Overridden Object methods
  // -----------------------------------------------------------------------------------------------

  /**
   * 返回此标签的字符串表示形式。
   *
   * Returns a string representation of this label.
   *
   * @return a string representation of this label.
   */
  @Override
  public String toString() {
    return "L" + System.identityHashCode(this);
  }
}
