package cis501.submission;

import cis501.Bypass;
import cis501.IInorderPipeline;
import cis501.Insn;
import cis501.MemoryOp;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class InorderPipelineNoBypassTest {

    private static IInorderPipeline sim;

    private static Insn makeInsn(int dst, int src1, int src2, MemoryOp mop) {
        return new Insn(dst, src1, src2, 1, 4, null, 0, null,
                mop, 1, 1, "synthetic");
    }

    @Before
    public void setup() {
        sim = new InorderPipeline(0/*no add'l memory latency*/, Bypass.NO_BYPASS);
    }

    /** Dependency and no MX so we stall. */
    @Test
    public void testMxBypass1(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(3, 2, 1, null));
        insns.add(makeInsn(4, 3, 2, MemoryOp.Store));
        sim.run(insns);
        assertEquals(2, sim.getInsns());
        // 123456789abcdef
        // fdxmw|
        //  fd--xmw|
        final long expected = 7 + 2;
        assertEquals(expected, sim.getCycles());
    }

    @Test
    public void testMxBypass2(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(3, 2, 1, null));
        insns.add(makeInsn(5, 6, 7, null)); //Arbitrary instruction.
        insns.add(makeInsn(4, 3, 2, MemoryOp.Store));
        sim.run(insns);

        assertEquals(3, sim.getInsns());
        // 123456789abcdef
        // fdxmw|
        //  fdxmw|
        //  f--dxmw|
        final long expected = 7 + 2;
        assertEquals(expected, sim.getCycles());
    }

    @Test
    public void testMxBypass3(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(3, 2, 1, null));
        insns.add(makeInsn(5, 6, 7, null)); //Arbitrary instruction.
        insns.add(makeInsn(5, 6, 7, null)); //Arbitrary instruction.
        insns.add(makeInsn(4, 3, 2, MemoryOp.Store));
        sim.run(insns);

        assertEquals(4, sim.getInsns());
        // 123456789abcdef
        // fdxmw|
        //  fdxmw|
        //   fdxmw|
        //    fdxmw|
        final long expected = 7 + 2;
        assertEquals(expected, sim.getCycles());
    }
    @Test
    public void testLoadToUse1(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(3, 8, 2, MemoryOp.Load));
        insns.add(makeInsn(4, 2, 3, null));
        sim.run(insns);

        assertEquals(2, sim.getInsns());
        // 123456789abcdef
        // fdxmw|
        //  fd--xmw|
        final long expected = 7 + 2;
        assertEquals(expected, sim.getCycles());
        }

    @Test
    public void testA(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(3, 8, 2, MemoryOp.Store));
        insns.add(makeInsn(4, 3, 2, MemoryOp.Load));
        sim.run(insns);

        assertEquals(2, sim.getInsns());
        // 123456789abcdef
        // fdxmw   |
        //  fd--xmw|
        final long expected = 9;
        assertEquals(expected, sim.getCycles());
    }

    @Test
    public void testB(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(3, 8, 2, MemoryOp.Store));
        insns.add(makeInsn(4, 3, 2, MemoryOp.Load));
        insns.add(makeInsn(5, 4, 2, null));
        sim.run(insns);

        assertEquals(3, sim.getInsns());
        // 123456789abcdef
        // fdxmw      |
        //  fd--xmw   |
        //  fd-----xmw|
        final long expected = 12;
        assertEquals(expected, sim.getCycles());
    }

    @Test
    public void testC(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(3, 8, 2, MemoryOp.Store));
        insns.add(makeInsn(5, 4, 2, null));
        insns.add(makeInsn(4, 3, 2, MemoryOp.Load));
        insns.add(makeInsn(5, 4, 2, null));
        sim.run(insns);

        assertEquals(4, sim.getInsns());
        // 123456789abcdef
        // fdxmw      |
        //  fdxmw     |
        //  fd--xmw   |
        //  fd-----xmw|
        final long expected = 12;
        assertEquals(expected, sim.getCycles());
    }

     @Test
    public void testD(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(3, 8, 2, MemoryOp.Store));
        insns.add(makeInsn(5, 4, 2, null));
        insns.add(makeInsn(5, 4, 2, null));
        insns.add(makeInsn(4, 3, 2, MemoryOp.Load));
        insns.add(makeInsn(5, 4, 2, null));
        sim.run(insns);

        assertEquals(5, sim.getInsns());
        // 123456789abcdef
        // fdxmw      |
        //  fdxmw     |
        //   fdxmw    |
        //    fd--xmw |
        //     fd- xmw|
        final long expected = 12;
        assertEquals(expected, sim.getCycles());
    }

    @Test
    public void testE(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(3, 8, 2, MemoryOp.Load));
        insns.add(makeInsn(5, 4, 3, null));
        sim.run(insns);

        assertEquals(2, sim.getInsns());
        // 123456789abcdef
        // fdxmw   |
        //  fd--xmw|
        final long expected = 9;
        assertEquals(expected, sim.getCycles());
    }

    @Test
    public void testF(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(3, 8, 2, null));
        insns.add(makeInsn(3, 4, 5, MemoryOp.Store));
        sim.run(insns);

        assertEquals(2, sim.getInsns());
        // 123456789abcdef
        // fdxmw   |
        //  fd--xmw|
        final long expected = 9;
        assertEquals(expected, sim.getCycles());
    }
}
