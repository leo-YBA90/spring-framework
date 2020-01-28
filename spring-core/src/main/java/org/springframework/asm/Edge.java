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
 *
 * 方法的控制流图。此图的每个节点都是一个基本块，用与其第一条指令对应的标签表示。每条边从一个节点到另一个节点，
 * 即从一个基本块到另一个基本块（分别称为前置块和后继块）。边缘对应于跳转或ret指令或异常处理程序。
 * @see Label
 * An edge in the control flow graph of a method. Each node of this graph is a basic block,
 * represented with the Label corresponding to its first instruction. Each edge goes from one node
 * to another, i.e. from one basic block to another (called the predecessor and successor blocks,
 * respectively). An edge corresponds either to a jump or ret instruction or to an exception
 * handler.
 *
 * @see Label
 * @author Eric Bruneton
 */
final class Edge {

	/**
	 * 与跳转或ret指令相对应的控制流图边。仅与{@link ClassWriter#COMPUTE_FRAMES}一起使用。
	 *
	 * A control flow graph edge corresponding to a jump or ret instruction. Only used with {@link
	 * ClassWriter#COMPUTE_FRAMES}.
	 */
	static final int JUMP = 0;

	/**
	 * 对应于异常处理程序的控制流图边缘。仅与{@link ClassWriter#COMPUTE_MAXS}一起使用。
	 *
	 * A control flow graph edge corresponding to an exception handler. Only used with {@link
	 * ClassWriter#COMPUTE_MAXS}.
	 */
	static final int EXCEPTION = 0x7FFFFFFF;

	/**
	 * 有关此控制流图边缘的信息。
	 * 如果使用{@link ClassWriter#COMPUTE_MAXS}，则此字段包含堆栈大小增量（对应于跳转指令的边）
	 * 或值异常（对应于异常处理程序的边）。堆栈大小增量是跳转指令之后的堆栈大小，
	 * 减去前置基本块（即包含跳转指令的块）开头的堆栈大小。
	 * 如果使用{@link ClassWriter#COMPUTE_FRAMES}，则此字段包含值跳转（对应于跳转指令的边）
	 * 或索引（对应于异常处理程序的边），该索引位于{@link ClassWriter}类型表中。
	 *
	 * Information about this control flow graph edge.
	 *
	 * <ul>
	 *   <li>If {@link ClassWriter#COMPUTE_MAXS} is used, this field contains either a stack size
	 *       delta (for an edge corresponding to a jump instruction), or the value EXCEPTION (for an
	 *       edge corresponding to an exception handler). The stack size delta is the stack size just
	 *       after the jump instruction, minus the stack size at the beginning of the predecessor
	 *       basic block, i.e. the one containing the jump instruction.
	 *   <li>If {@link ClassWriter#COMPUTE_FRAMES} is used, this field contains either the value JUMP
	 *       (for an edge corresponding to a jump instruction), or the index, in the {@link
	 *       ClassWriter} type table, of the exception type that is handled (for an edge corresponding
	 *       to an exception handler).
	 * </ul>
	 */
	final int info;

	/** 此控制流图边缘的后续块。 */
	/** The successor block of this control flow graph edge. */
	final Label successor;

	/**
	 * 基本块传出边列表中的下一条边。见{@link Label#outgoingEdges}。
	 *
	 * The next edge in the list of outgoing edges of a basic block. See {@link Label#outgoingEdges}.
	 */
	Edge nextEdge;

	/**
	 * Constructs a new Edge.
	 *
	 * @param info see {@link #info}.
	 * @param successor see {@link #successor}.
	 * @param nextEdge see {@link #nextEdge}.
	 */
	Edge(final int info, final Label successor, final Edge nextEdge) {
		this.info = info;
		this.successor = successor;
		this.nextEdge = nextEdge;
	}
}
