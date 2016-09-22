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
        //  f---dxmw|
        final long expected = 7 + 3;
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
        //  f---dxmw|
        final long expected = 7 + 3;
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
        //    f-dxmw|
        final long expected = 7 + 3;
        assertEquals(expected, sim.getCycles());
    }

}
