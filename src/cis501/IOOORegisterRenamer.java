package cis501;

import java.util.Map;

public interface IOOORegisterRenamer {

    /**
     * The number of architectural registers in ARMv7. 16 general-purpose registers, +1 for
     * condition codes.
     */
    final int NUM_ARCH_REGS = 17;

    /** Treat condition codes as architectural register 16. */
    final short COND_CODE_ARCH_REG = 16;

    int availablePhysRegs();

    /**
     * Allocate a new physical register for the given architectural register ar.
     *
     * @return the new physical register that maps to ar. Returns null if there are no more physical
     * registers available.
     */
    PhysReg allocateReg(int ar);

    /** Place physical register pr at the end of the free list */
    void freeReg(PhysReg pr);

    /**
     * @return the physical register that architectural register ar currently maps to.
     */
    PhysReg a2p(int ar);

    /**
     * Rename the registers in the given insn i. If there are insufficient free physical registers
     * to rename i, then the oldest insn(s) should be committed to free up enough physical registers
     * for i to be renamed.
     *
     * @param i       The insn whose register inputs and outputs should be renamed
     * @param inputs  This is an output parameter. An empty map will be passed in, and this function
     *                should return a map populated with the archreg-to-physreg mapping used for
     *                this insn's inputs. This map should only include mappings for the input
     *                registers of this insn. Be sure to include mapping(s) for the condition code
     *                register if needed.
     * @param outputs Same as the inputs map but for the output(s) of this register.
     */
    void rename(final Insn i, Map<Short, PhysReg> inputs, Map<Short, PhysReg> outputs);

}
