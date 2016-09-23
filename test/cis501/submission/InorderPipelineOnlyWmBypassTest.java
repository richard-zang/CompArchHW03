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

public class InorderPipelineOnlyWmBypassTest {

	EnumSet<Bypass> onlyWmBypass = EnumSet.of(Bypass.WM);

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
    public void testOnlyWmBypass1(){
        List<Insn> insns = new LinkedList<>();
        sim = new InorderPipeline(0, onlyWmBypass);
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
    public void testOnlyWmBypass2(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, null));
        sim = new InorderPipeline(0, onlyWmBypass);
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
    public void testOnlyWmBypass3(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        sim = new InorderPipeline(0, onlyWmBypass);
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
    public void testOnlyWmBypass4(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,5,6, null));
        sim = new InorderPipeline(0, onlyWmBypass);
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
    public void testOnlyWmBypass5(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, null));
        insns.add(makeInsn(4,5,6, MemoryOp.Store));
        sim = new InorderPipeline(0, onlyWmBypass);
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
      Dependency
    */
    @Test
    public void testOnlyWmBypass6(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, null));
        insns.add(makeInsn(4,1,2, null));
        sim = new InorderPipeline(0, onlyWmBypass);
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
      Add r4 <- r1 + r2
      Load to use dependency solved by stalling
    */
    @Test
    public void testOnlyWmBypass7(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,1,2, null));
        sim = new InorderPipeline(0, onlyWmBypass);
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
    public void testOnlyWmBypass8(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(2,5,4, null));
        insns.add(makeInsn(4,1,2, null));
        sim = new InorderPipeline(0, onlyWmBypass);
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
    public void testOnlyWmBypass9(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(1,2,3, MemoryOp.Store));
        sim = new InorderPipeline(0, onlyWmBypass);
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
      Load r1 <- Offset[r2]
      Store r2 <- Offset[r1]
      Load to store dependency
    */
    @Test
    public void testOnlyWmBypass10(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(2,3,1, MemoryOp.Store));
        sim = new InorderPipeline(0, onlyWmBypass);
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
      Load to use dependency
    */
    @Test
    public void testOnlyWmBypass11(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(2,1,3, MemoryOp.Store));
        sim = new InorderPipeline(0, onlyWmBypass);
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
    public void testOnlyWmBypass12(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,6,2, null));
        insns.add(makeInsn(5,6,3, MemoryOp.Load));
        insns.add(makeInsn(2,2,2, null));
        sim = new InorderPipeline(0, onlyWmBypass);
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
    public void testOnlyWmBypass13(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,2,1, MemoryOp.Load));
        insns.add(makeInsn(5,3,4, MemoryOp.Load));
        insns.add(makeInsn(2,5,2, null));
        sim = new InorderPipeline(0, onlyWmBypass);
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
    public void testOnlyWmBypass14(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,2,1, MemoryOp.Load));
        insns.add(makeInsn(5,3,4, MemoryOp.Load));
        insns.add(makeInsn(2,5,2, null));
        sim = new InorderPipeline(0, onlyWmBypass);
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
    public void testOnlyWmBypass15(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,2,1, null));
        insns.add(makeInsn(5,3,4, null));
        insns.add(makeInsn(2,5,5, MemoryOp.Store));
        sim = new InorderPipeline(0, onlyWmBypass);
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
      Same Tests with 1 Mem Latency




    */
    /*
      No Instructions, 1 MemLatency 
    */
    @Test
    public void testOnlyWmBypass16(){
        List<Insn> insns = new LinkedList<>();
        sim = new InorderPipeline(1, onlyWmBypass);
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
    public void testOnlyWmBypass17(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, null));
        sim = new InorderPipeline(1, onlyWmBypass);
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
    public void testOnlyWmBypass18(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        sim = new InorderPipeline(1, onlyWmBypass);
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
    public void testOnlyWmBypass19(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,5,6, null));
        sim = new InorderPipeline(1, onlyWmBypass);
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
    public void testOnlyWmBypass20(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, null));
        insns.add(makeInsn(4,5,6, MemoryOp.Store));
        sim = new InorderPipeline(1, onlyWmBypass);
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
      Dependency solved by stalling
    */
    @Test
    public void testOnlyWmBypass21(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, null));
        insns.add(makeInsn(4,1,2, null));
        sim = new InorderPipeline(1, onlyWmBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXMW   |
        //  FD--XMW|
        final long expected = 9;
        assertEquals(expected, sim.getCycles());
    }
    /*
      1 MemLatency
      Load r1 <- Offset[r2]
      Add r4 <- r1 + r2
      Load to use dependency solved by stalling
    */
    @Test
    public void testOnlyWmBypass22(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,1,2, null));
        sim = new InorderPipeline(1, onlyWmBypass);
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
    public void testOnlyWmBypass23(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(2,5,4, null));
        insns.add(makeInsn(4,1,2, null));
        sim = new InorderPipeline(1, onlyWmBypass);
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
    public void testOnlyWmBypass24(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(1,2,3, MemoryOp.Store));
        sim = new InorderPipeline(1, onlyWmBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdef
        // FDXM-W  |
        //  FD-XM-W|
        final long expected = 9;
        assertEquals(expected, sim.getCycles());
    }

    /*
      1 MemLatency
      Load r1 <- Offset[r2]
      Store r2 -> Offset[r1]
      Load to store dependency
    */
    @Test
    public void testOnlyWmBypass25(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(2,3,1, MemoryOp.Store));
        sim = new InorderPipeline(1, onlyWmBypass);
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
    public void testOnlyWmBypass26(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(2,1,3, null));
        sim = new InorderPipeline(1, onlyWmBypass);
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
    public void testOnlyWmBypass26Prime(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(2,1,3, MemoryOp.Load));
        sim = new InorderPipeline(1, onlyWmBypass);
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
    public void testOnlyWmBypass27(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,6,2, null));
        insns.add(makeInsn(5,6,3, MemoryOp.Load));
        insns.add(makeInsn(2,2,2, null));
        sim = new InorderPipeline(1, onlyWmBypass);
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
    public void testOnlyWmBypass28(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,2,1, MemoryOp.Load));
        insns.add(makeInsn(5,3,4, MemoryOp.Load));
        insns.add(makeInsn(2,5,2, null));
        sim = new InorderPipeline(1, onlyWmBypass);
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
    public void testOnlyWmBypass29a(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,2,1, MemoryOp.Load));
        sim = new InorderPipeline(1, onlyWmBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdefg
        // FDXM-W    |
        //  FD---XM-W|
        final long expected = 11;
        assertEquals(expected, sim.getCycles());
    }

        @Test
    public void testOnlyWmBypass29b(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,2,1, MemoryOp.Load));
        insns.add(makeInsn(5,3,4, MemoryOp.Load));
        sim = new InorderPipeline(1, onlyWmBypass);
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
    public void testOnlyWmBypass29(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,2,1, MemoryOp.Load));
        insns.add(makeInsn(5,3,4, MemoryOp.Load));
        insns.add(makeInsn(2,5,2, null));
        sim = new InorderPipeline(1, onlyWmBypass);
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

    /*
      1 MemLatency
      All dependent
    */
    @Test
    public void testOnlyWmBypass30(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(1,2,3, MemoryOp.Load));
        insns.add(makeInsn(4,2,1, null));
        insns.add(makeInsn(5,3,4, null));
        insns.add(makeInsn(2,5,5, MemoryOp.Store));
        sim = new InorderPipeline(1, onlyWmBypass);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdefgh
        // FDXM-W          |
        //  FD---XMW       |
        //   F---D--XMW    |
        //       F--D--XM-W|
        final long expected = 17;
        assertEquals(expected, sim.getCycles());
    }
}
