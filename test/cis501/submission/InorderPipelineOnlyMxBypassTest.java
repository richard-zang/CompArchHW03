package cis501.submission;

import cis501.Bypass;
import cis501.IInorderPipeline;
import cis501.Insn;
import cis501.MemoryOp;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class InorderPipelineOnlyMxBypassTest {

	EnumSet<Bypass> onlyMxBypass = EnumSet.of(Bypass.MX);

    private static IInorderPipeline sim;

    private static Insn makeInsn(int dst, int src1, int src2, MemoryOp mop) {
        return new Insn(dst, src1, src2, 1, 4, null, 0, null,
                        mop, 1, 1, "synthetic");
    }

    @Before
    public void setup() {
        sim = null;
    }

    /*
      No Instructions, No MemLatency 
    */
    @Test
    public void testOnlyMxBypass1(){
        List<Insn> insns = new LinkedList<>();
        sim = new InorderPipeline(0, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // 
        //
        final long expected = 0;
        assertEquals(expected, sim.getCycles());
    }

    /*
      1 Non-memory Instruction, No MemLatency
    */
    @Test
    public void testOnlyMxBypass2(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, null));
        sim = new InorderPipeline(0, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXMW|
        //
        final long expected = 6;
        assertEquals(expected, sim.getCycles());
    }

    /*
      1 Memory Instruction, No MemLatency
    */
    @Test
    public void testOnlyMxBypass3(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        sim = new InorderPipeline(0, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXMW
        //
        final long expected = 6;
        assertEquals(expected, sim.getCycles());
    }
    /*
      1 Memory Instruction, 1 Non-Memory Instruction, No dependency, No MemLatency
    */
    @Test
    public void testOnlyMxBypass4(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,5,6, null));
        sim = new InorderPipeline(0, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXMW |
        //  FDXMW|
        final long expected = 6 + 1;
        assertEquals(expected, sim.getCycles());
    }

    /*
      1 Non-Memory Instruction, 1 Memory Instruction, No dependency, No MemLatency
    */
    @Test
    public void testOnlyMxBypass5(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, null));
        insns.add(makeInsn(4,5,6, MemoryOp.Store));
        sim = new InorderPipeline(0, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXMW |
        //  FDXMW|
        final long expected = 6 + 1;
        assertEquals(expected, sim.getCycles());
    }

    /*
      No MemLatency
      Add r1 <- r2 + r3
      Add r4 <- r1 + r2
      Dependency solved by MX bypass
    */
    @Test
    public void testOnlyMxBypass6(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, null));
        insns.add(makeInsn(4,1,2, null));
        sim = new InorderPipeline(0, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXMW |
        //  FDXMW|
        final long expected = 6 + 1;
        assertEquals(expected, sim.getCycles());
    }
    /*
      No MemLatency
      Load r1 <- Offset[r2]
      Add r4 <- r1 + r2
      Load to use dependency solved by stalling
    */
    @Test
    public void testOnlyMxBypass7(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,1,2, null));
        sim = new InorderPipeline(0, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXMW   |
        //  FD--XMW|
        final long expected = 9;
        assertEquals(expected, sim.getCycles());
    }

    /*
      No MemLatency
      Load r1 <- Offset[r2]
      Sub r2 <- r5 + r4
      Add r4 <- r1 + r2
      Load to use dependency solved by stalling
    */
    @Test
    public void testOnlyMxBypass8(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(2,5,4, null));
        insns.add(makeInsn(4,1,2, null));
        sim = new InorderPipeline(0, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXMW    |
        //  FDXMW   |
        //   FD--XMW|
        final long expected = 10;
        assertEquals(expected, sim.getCycles());
    }

    /*
      No MemLatency
      Load r1 <- Offset[r2]
      Store r1 -> Offset[r3]
      Load to store dependency
    */
    @Test
    public void testOnlyMxBypass9(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(1,2,3, MemoryOp.Store));
        sim = new InorderPipeline(0, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXMW  |
        //  FD-XMW|
        final long expected = 8;
        assertEquals(expected, sim.getCycles());
    }

    /*
      No MemLatency
      Load r1 <- Offset[r2]
      Store r2 <- Offset[r1]
      Load to store dependency
    */
    @Test
    public void testOnlyMxBypass10(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(2,3,1, MemoryOp.Store));
        sim = new InorderPipeline(0, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXMW   |
        //  FD--XMW|
        final long expected = 9;
        assertEquals(expected, sim.getCycles());
    }


    /*
      No MemLatency
      Load r1 <- Offset[r2]
      Store r2 <- Offset[r3]
      Load to use no dependency
    */
    @Test
    public void testOnlyMxBypass11(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(2,1,3, MemoryOp.Store));
        sim = new InorderPipeline(0, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXMW |
        //  FDXMW|
        final long expected = 7;
        assertEquals(expected, sim.getCycles());
    }

    /*
      No MemLatency
      No dependencies
    */
    @Test
    public void testOnlyMxBypass12(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,6,2, null));
        insns.add(makeInsn(5,6,3, MemoryOp.Load));
        insns.add(makeInsn(2,2,2, null));
        sim = new InorderPipeline(0, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXMW   |
        //  FDXMW  |
        //   FDXMW |
        //    FDXMW|
        final long expected = 6 + 3;
        assertEquals(expected, sim.getCycles());
    }

    /*
      No MemLatency
      All dependent
    */
    @Test
    public void testOnlyMxBypass13(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,2,1, MemoryOp.Load));
        insns.add(makeInsn(5,3,4, MemoryOp.Load));
        insns.add(makeInsn(2,5,2, null));
        sim = new InorderPipeline(0, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXMW         |
        //  FD--XMW      |
        //   F--D--XMW   |
        //      F--D--XMW|
        final long expected = 15;
        assertEquals(expected, sim.getCycles());
    }

    /*
      No MemLatency
      All dependent
    */
    @Test
    public void testOnlyMxBypass14(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,2,1, MemoryOp.Load));
        insns.add(makeInsn(5,3,4, MemoryOp.Load));
        insns.add(makeInsn(2,5,2, null));
        sim = new InorderPipeline(0, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXMW         |
        //  FD--XMW      |
        //   F--D--XMW   |
        //      F--D--XMW|
        final long expected = 15;
        assertEquals(expected, sim.getCycles());
    }

    /*
      No MemLatency
      All dependent
    */
    @Test
    public void testOnlyMxBypass15(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,2,1, null));
        insns.add(makeInsn(5,3,4, null));
        insns.add(makeInsn(2,5,5, MemoryOp.Store));
        sim = new InorderPipeline(0, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXMW     |
        //  FD--XMW  |
        //   F--DXMW |
        //      FDXMW|
        final long expected = 11;
        assertEquals(expected, sim.getCycles());
    }

    /*
      Same Tests with 1 Mem Latency




    */
    /*
      No Instructions, 1 MemLatency 
    */
    @Test
    public void testOnlyMxBypass16(){
        List<Insn> insns = new LinkedList<>();
        sim = new InorderPipeline(1, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // 
        //
        final long expected = 0;
        assertEquals(expected, sim.getCycles());
    }

    /*
      1 Non-memory Instruction, 1 MemLatency
    */
    @Test
    public void testOnlyMxBypass17(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, null));
        sim = new InorderPipeline(1, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXMW|
        //
        final long expected = 6;
        assertEquals(expected, sim.getCycles());
    }

    /*
      1 Memory Instruction, 1 MemLatency
    */
    @Test
    public void testOnlyMxBypass18(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        sim = new InorderPipeline(1, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXM-W|
        //
        final long expected = 7;
        assertEquals(expected, sim.getCycles());
    }
    /*
      1 Memory Instruction, 1 Non-Memory Instruction, No dependency, 1 MemLatency
    */
    @Test
    public void testOnlyMxBypass19(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,5,6, null));
        sim = new InorderPipeline(1, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXM-W |
        //  FDX-MW|
        final long expected = 8;
        assertEquals(expected, sim.getCycles());
    }

    /*
      1 Non-Memory Instruction, 1 Memory Instruction, No dependency, 1 MemLatency
    */
    @Test
    public void testOnlyMxBypass20(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, null));
        insns.add(makeInsn(4,5,6, MemoryOp.Store));
        sim = new InorderPipeline(1, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXMW  |
        //  FDXM-W|
        final long expected = 8;
        assertEquals(expected, sim.getCycles());
    }

    /*
      1 MemLatency
      Add r1 <- r2 + r3
      Add r4 <- r1 + r2
      Dependency solved by MX bypass
    */
    @Test
    public void testOnlyMxBypass21(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, null));
        insns.add(makeInsn(4,1,2, null));
        sim = new InorderPipeline(1, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXMW |
        //  FDXMW|
        final long expected = 7;
        assertEquals(expected, sim.getCycles());
    }
    /*
      1 MemLatency
      Load r1 <- Offset[r2]
      Add r4 <- r1 + r2
      Load to use dependency solved by stalling
    */
    @Test
    public void testOnlyMxBypass22(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,1,2, null));
        sim = new InorderPipeline(1, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXM-W   |
        //  FD---XMW|
        final long expected = 10;
        assertEquals(expected, sim.getCycles());
    }

    /*
      1 MemLatency
      Load r1 <- Offset[r2]
      Sub r2 <- r5 + r4
      Add r4 <- r1 + r2
      Load to use dependency solved by stalling
    */
    @Test
    public void testOnlyMxBypass23(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(2,5,4, null));
        insns.add(makeInsn(4,1,2, null));
        sim = new InorderPipeline(1, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXM-W    |
        //  FDX-MW   |
        //   FD---XMW|
        final long expected = 11;
        assertEquals(expected, sim.getCycles());
    }

    /*
      1 MemLatency
      Load r1 <- Offset[r2]
      Store r1 -> Offset[r3]
      Load to store dependency
    */
    @Test
    public void testOnlyMxBypass24(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(1,2,3, MemoryOp.Store));
        sim = new InorderPipeline(1, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXM-W   |
        //  FDX--M-W|
        final long expected = 10;
        assertEquals(expected, sim.getCycles());
    }

    /*
      1 MemLatency
      Load r1 <- Offset[r2]
      Store r2 -> Offset[r1]
      Load to store dependency
    */
    @Test
    public void testOnlyMxBypass25(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(2,3,1, MemoryOp.Store));
        sim = new InorderPipeline(1, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXM-W    |
        //  FD---XM-W|
        final long expected = 11;
        assertEquals(expected, sim.getCycles());
    }


    /*
      1 MemLatency
      Load r1 <- Offset[r2]
      add r2 <- r1, r3
      Load to use dependency solved by stalling
    */
    @Test
    public void testOnlyMxBypass26(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(2,1,3, null));
        sim = new InorderPipeline(1, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXM-W   |
        //  FD---XMW|
        final long expected = 10;
        assertEquals(expected, sim.getCycles());
    }

    /*
      1 MemLatency
      Load r1 <- Offset[r2]
      load r2 <- r1, r3
      Load to use dependency solved by stalling
    */
    @Test
    public void testOnlyMxBypass26Prime(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(2,1,3, MemoryOp.Load));
        sim = new InorderPipeline(1, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXM-W    |
        //  FD---XM-W|
        final long expected = 11;
        assertEquals(expected, sim.getCycles());
    }

    /*
      1 MemLatency
      No dependencies
    */
    @Test
    public void testOnlyMxBypass27(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,6,2, null));
        insns.add(makeInsn(5,6,3, MemoryOp.Load));
        insns.add(makeInsn(2,2,2, null));
        sim = new InorderPipeline(1, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXM-W    |
        //  FDX-MW   |
        //   FD-XM-W |
        //    F-DX-MW|
        final long expected = 11;
        assertEquals(expected, sim.getCycles());
    }

    /*
      1 MemLatency
      All dependent
    */
    @Test
    public void testOnlyMxBypass28(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,2,1, MemoryOp.Load));
        insns.add(makeInsn(5,3,4, MemoryOp.Load));
        insns.add(makeInsn(2,5,2, null));
        sim = new InorderPipeline(1, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdefghi
        // FDXM-W           |
        //  FD---XM-W       |
        //   F---D---XM-W   |
        //       F---D---XMW|
        final long expected = 18;
        assertEquals(expected, sim.getCycles());
    }

    @Test
    public void testOnlyMxBypass29a(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,2,1, MemoryOp.Load));
        sim = new InorderPipeline(1, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdefg
        // FDXM-W    |
        //  FD---XM-W|
        final long expected = 11;
        assertEquals(expected, sim.getCycles());
    }

        @Test
    public void testOnlyMxBypass29b(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,2,1, MemoryOp.Load));
        insns.add(makeInsn(5,3,4, MemoryOp.Load));
        sim = new InorderPipeline(1, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdefg
        // FDXM-W        |
        //  FD---XM-W    |
        //   F---D---XM-W|
        final long expected = 15;
        assertEquals(expected, sim.getCycles());
    }

    /*
      1 MemLatency
      All dependent
    */
    @Test
    public void testOnlyMxBypass29(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,2,1, MemoryOp.Load));
        insns.add(makeInsn(5,3,4, MemoryOp.Load));
        insns.add(makeInsn(2,5,2, null));
        sim = new InorderPipeline(1, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdefghi
        // FDXM-W           |
        //  FD---XM-W       |
        //   F---D---XM-W   |
        //      F----D---XMW|
        final long expected = 18;
        assertEquals(expected, sim.getCycles());
    }

    /*
      1 MemLatency
      All dependent
    */
    @Test
    public void testOnlyMxBypass30(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,2,1, null));
        insns.add(makeInsn(5,3,4, null));
        insns.add(makeInsn(2,5,5, MemoryOp.Store));
        sim = new InorderPipeline(1, onlyMxBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXM-W      |
        //  FD---XMW   |
        //   F---DXMW  |
        //       FDXM-W|
        final long expected = 13;
        assertEquals(expected, sim.getCycles());
    }
}
