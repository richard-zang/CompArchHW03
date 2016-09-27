package cis501;

import java.util.StringTokenizer;

/** Class representing a single micro-op. */
public class Insn {

    /** Destination register. Value will be in range [-1,15]. */
    public final short dstReg;

    /** Input 1 for ALU ops, or address for loads, value-to-be-stored for stores. Value will be in range [-1,15]. */
    public final short srcReg1;

    /** Input 2 for ALU ops, or address for stores. Value will be in range [-1,15]. */
    public final short srcReg2;

    public final long pc;
    public final short insnSizeBytes;

    public final Direction branch;
    public final long branchTarget;
    public final CondCodes condCode;

    public final MemoryOp mem;
    public final long memAddress;
    public final short memAccessBytes;

    public final String asm;

    public long fallthroughPC() {
        return this.pc + this.insnSizeBytes;
    }

    private CondCodes CondCodeOfChar(char c) {
        switch (c) {
            case 'R':
                return CondCodes.ReadCC;
            case 'W':
                return CondCodes.WriteCC;
            case 'B':
                return CondCodes.ReadWriteCC;
            case '_':
                return null; // ignore condition codes
            default:
                throw new IllegalArgumentException("Invalid cond code type: " + c);
        }
    }

    private Direction BranchOfChar(char c) {
        switch (c) {
            case 'T':
                return Direction.Taken;
            case 'N':
                return Direction.NotTaken;
            case '_':
                return null;
            default:
                throw new IllegalArgumentException("Invalid branch type: " + c);
        }
    }

    private MemoryOp MemOfChar(char c) {
        switch (c) {
            case 'L':
                return MemoryOp.Load;
            case 'S':
                return MemoryOp.Store;
            case '_':
                return null;
            default:
                throw new IllegalArgumentException("Invalid mem op: " + c);
        }
    }

    private short parseReg(String r) {
        switch (r) {
            case "  _": return -1;
            case " r0": return 0;
            case " r1": return 1;
            case " r2": return 2;
            case " r3": return 3;
            case " r4": return 4;
            case " r5": return 5;
            case " r6": return 6;
            case " r7": return 7;
            case " r8": return 8;
            case " r9": return 9; // sb
            case "r10": return 10; // sl
            case "r11": return 11; // fp
            case "r12": return 12; // ip
            case " sp": return 13;
            case " lr": return 14;
            case " pc": return 15;
            default:
                throw new IllegalArgumentException("Invalid reg: " + r);
        }
    }

    /** Parse an insn from a line in the trace file */
    public Insn(String line) {
        StringTokenizer st = new StringTokenizer(line,"\t");
        // asm string may contain commas and be split into multiple tokens
        assert st.countTokens() == 12;

        try {
            this.pc = Long.parseLong(st.nextToken(), 16);
            this.insnSizeBytes = Short.parseShort(st.nextToken());
            this.branch = BranchOfChar(st.nextToken().charAt(0));
            this.branchTarget = Long.parseLong(st.nextToken(), 16);
            this.mem = MemOfChar(st.nextToken().charAt(0));
            this.memAddress = Long.parseLong(st.nextToken(), 16);
            this.memAccessBytes = Short.parseShort(st.nextToken());
            this.condCode = CondCodeOfChar(st.nextToken().charAt(0));
            this.dstReg = parseReg(st.nextToken());
            this.srcReg1 = parseReg(st.nextToken());
            this.srcReg2 = parseReg(st.nextToken());
            this.asm = st.nextToken();
        } catch (Exception e) {
            System.out.println("Error parsing insn: " + line);
            throw e;
        }
    }

    /** Create an insn directly. Used for testing. */
    public Insn(int dr, int sr1, int sr2,
                long pc, int isize,
                Direction dir, long branchTarget, CondCodes cc,
                MemoryOp mop, long memAddr, int msize,
                String asm) {
        this.dstReg = (short) dr;
        this.srcReg1 = (short) sr1;
        this.srcReg2 = (short) sr2;
        this.pc = pc;
        this.insnSizeBytes = (short)isize;
        this.branch = dir;
        this.branchTarget = branchTarget;
        this.condCode = cc;
        this.mem = mop;
        this.memAddress = memAddr;
        this.memAccessBytes = (short) msize;
        this.asm = asm;
    }

    @Override
    public String toString() {
        return String.format("dst:%d src1:%d src2:%d pc:%x isize:%d branch:%s targ:%x cc:%s mem:%s maddr:%x msize:%d %s",
                dstReg, srcReg1, srcReg2,
                pc, insnSizeBytes,
                branch, branchTarget, condCode,
                mem, memAddress, memAccessBytes,
                asm);
    }

}
