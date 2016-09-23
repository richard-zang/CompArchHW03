package cis501.submission;

import cis501.Bypass;
import cis501.IInorderPipeline;
import cis501.Insn;
import cis501.MemoryOp;
import org.junit.Before;
import org.junit.Test;
import java.util.EnumSet;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class InorderPipelineSomeBypassTest{

    private static IInorderPipeline sim;

    private static Insn makeInsn(int dst, int src1, int src2, MemoryOp mop) {
        return new Insn(dst, src1, src2, 1, 4, null, 0, null,
                        mop, 1, 1, "synthetic");
    }
    private EnumSet<Bypass> set;
    @Before
    public void setup() {
        set = EnumSet.noneOf(Bypass.class);
        set.add(Bypass.WX);
        set.add(Bypass.WM);
        sim = new InorderPipeline(0/*no add'l memory latency*/, set);
    }

    @Test
    public void test1(){
   List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(3, 1, 2, null));
        insns.add(makeInsn(4, 3, 2, null));
        sim.run(insns);
        assertEquals(2, sim.getInsns());
        // 12345678
        // fdxmw  |
        //  fd-xmw|
        assertEquals(8, sim.getCycles());
    }

    @Test
    public void test2(){
        List<Insn> insns = new LinkedList<>();
        sim = new InorderPipeline(2, set);
        insns.add(makeInsn(3, 1, 2, MemoryOp.Load));
        insns.add(makeInsn(4, 5, 3, null));
        sim.run(insns);
        assertEquals(2, sim.getInsns());
        // 123456789abcdef
        // fdxm--w  |
        //  fd---xmw|
        assertEquals(10, sim.getCycles());
    }

    @Test
    public void test3(){
        List<Insn> insns = new LinkedList<>();
        sim = new InorderPipeline(2, set);
        insns.add(makeInsn(3, 1, 2, MemoryOp.Load));
        insns.add(makeInsn(4, 5, 3, MemoryOp.Store));
        //insns.add(makeInsn(4, 5, 3, MemoryOp.Store));
        sim.run(insns);
        assertEquals(2, sim.getInsns());
        // 123456789abcdef
        // fdxm--w    |
        //  fd---xm--w|

        assertEquals(12, sim.getCycles());
    }

    @Test
    public void test4(){
        List<Insn> insns = new LinkedList<>();
        sim = new InorderPipeline(2, set);
        insns.add(makeInsn(3, 1, 2, MemoryOp.Load));
        insns.add(makeInsn(4, 5, 3, MemoryOp.Store));
        insns.add(makeInsn(4, 5, 3, MemoryOp.Store));
        sim.run(insns);
        assertEquals(3, sim.getInsns());
        // 123456789abcdef
        // fdxm--w       |
        //  fd---xm--w   |
        //   fd---x--m--w|
        assertEquals(15, sim.getCycles());
    }

    @Test
    public void test5(){
        List<Insn> insns = new LinkedList<>();
        sim = new InorderPipeline(0, set);
        insns.add(makeInsn(3, 1, 2, null));
        insns.add(makeInsn(4, 5, 3, null));
        insns.add(makeInsn(6, 5, 4, null));
        sim.run(insns);
        assertEquals(3, sim.getInsns());
        // 123456789abcdef
        // fdxmw    |
        //  fd-xmw  |
        //   f-d-xmw|
        assertEquals(10, sim.getCycles());
    }

    @Test
    public void test5Prime(){
        List<Insn> insns = new LinkedList<>();
        EnumSet<Bypass> mySet = EnumSet.noneOf(Bypass.class);

        sim = new InorderPipeline(0, mySet);
        insns.add(makeInsn(3, 1, 2, null));
        insns.add(makeInsn(4, 5, 3, null));
        insns.add(makeInsn(6, 5, 4, null));
        sim.run(insns);
        assertEquals(3, sim.getInsns());
        // 123456789abcdef
        // fdxmw      |
        //  fd--xmw   |
        //   f-d--xmw |
        assertEquals(12, sim.getCycles());
    }

    // 10 because it has WX bypass on.
    @Test
    public void test6(){
        List<Insn> insns = new LinkedList<>();
        sim = new InorderPipeline(2, set);

        insns.add(makeInsn(3, 1, 2, MemoryOp.Load));
        insns.add(makeInsn(6, 5, 3, null));
        sim.run(insns);
        assertEquals(2, sim.getInsns());
        // 123456789abcdef
        // fdxm--w  |
        //  fd---xmw|
        assertEquals(10, sim.getCycles());
    }

    // 11 because it has no WX bypass on.
    @Test
    public void test7(){
        List<Insn> insns = new LinkedList<>();

        EnumSet<Bypass> mySet = EnumSet.noneOf(Bypass.class);

        sim = new InorderPipeline(2, mySet);
        insns.add(makeInsn(3, 1, 2, MemoryOp.Load));
        insns.add(makeInsn(6, 5, 3, null));
        sim.run(insns);

        assertEquals(2, sim.getInsns());
        // 123456789abcdef
        // fdxm--w   |
        //  fd----xmw|
        assertEquals(11, sim.getCycles());
    }

    // This would usually be done in MX, but we have none!
    @Test
    public void test8(){
        List<Insn> insns = new LinkedList<>();

        EnumSet<Bypass> mySet = EnumSet.noneOf(Bypass.class);
        mySet.add(Bypass.WX);

        sim = new InorderPipeline(0, mySet);
        insns.add(makeInsn(3, 1, 2, null));
        insns.add(makeInsn(6, 4, 3, null));
        sim.run(insns);

        assertEquals(2, sim.getInsns());
        // 123456789abcdef
        // fdxmw  |
        //  fd-xmw|
        assertEquals(8, sim.getCycles());
    }

    // Added MX, should be done one cycle earlier than test8().
    @Test
    public void test9(){
        List<Insn> insns = new LinkedList<>();

        EnumSet<Bypass> mySet = EnumSet.noneOf(Bypass.class);
        mySet.add(Bypass.WX);
        mySet.add(Bypass.MX);

        sim = new InorderPipeline(0, mySet);
        insns.add(makeInsn(3, 1, 2, null));
        insns.add(makeInsn(6, 4, 3, null));
        sim.run(insns);

        assertEquals(2, sim.getInsns());
        // 123456789abcdef
        // fdxmw |
        //  fdxmw|
        assertEquals(7, sim.getCycles());
    }
}
