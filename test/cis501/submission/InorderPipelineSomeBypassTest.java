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
    public void test4a(){
        List<Insn> insns = new LinkedList<>();
        sim = new InorderPipeline(2, set);
        insns.add(makeInsn(3, 1, 2, MemoryOp.Load));
        insns.add(makeInsn(4, 5, 3, MemoryOp.Store));
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
        // 123456789abcdefg
        // fdxm--w       |
        //  fd---xm--w   |
        //   f---dx--m--w|
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

    @Test
    public void test10(){
        List<Insn> insns = new LinkedList<>();

        EnumSet<Bypass> mySet = EnumSet.noneOf(Bypass.class);
        mySet.add(Bypass.WM);
        sim = new InorderPipeline(0, mySet);

        insns.add(makeInsn(3, 1, 2, MemoryOp.Store));
        insns.add(makeInsn(6, 4, 3, null));
        sim.run(insns);

        assertEquals(2, sim.getInsns());
        // 123456789abcdef
        // fdxmw   |
        //  fd--xmw|
        assertEquals(9, sim.getCycles());
    }

    @Test
    public void test11(){
        List<Insn> insns = new LinkedList<>();

        EnumSet<Bypass> mySet = EnumSet.noneOf(Bypass.class);
        mySet.add(Bypass.WM);
        sim = new InorderPipeline(0, mySet);

        insns.add(makeInsn(3, 1, 2, MemoryOp.Store));
        insns.add(makeInsn(6, 4, 3, null));
        insns.add(makeInsn(7, 4, 6, MemoryOp.Load));
        sim.run(insns);

        assertEquals(3, sim.getInsns());
        // 123456789abcdef
        // fdxmw      |
        //  fd--xmw   |
        //   fd----xmw|
        assertEquals(12, sim.getCycles());
    }

    @Test
    public void test12a(){
        List<Insn> insns = new LinkedList<>();

        EnumSet<Bypass> mySet = EnumSet.noneOf(Bypass.class);
        mySet.add(Bypass.WM);
        sim = new InorderPipeline(0, mySet);

        insns.add(makeInsn(6, 4, 3, null));
        insns.add(makeInsn(7, 6, 4, MemoryOp.Load)); //Reg1 no stall
        sim.run(insns);

        assertEquals(2, sim.getInsns());
        // 123456789abcdef
        // fdxmw   |
        //  fd--xmw| //No MX or WX
        assertEquals(9, sim.getCycles());
    }

    @Test
    public void test12b(){
        List<Insn> insns = new LinkedList<>();

        EnumSet<Bypass> mySet = EnumSet.noneOf(Bypass.class);
        mySet.add(Bypass.WM);
        sim = new InorderPipeline(0, mySet);

        insns.add(makeInsn(6, 4, 3, null));
        insns.add(makeInsn(7, 4, 6, MemoryOp.Load));
        sim.run(insns);

        assertEquals(2, sim.getInsns());
        // 123456789abcdef
        // fdxmw   |
        //  fd--xmw|
        assertEquals(9, sim.getCycles());
    }

    @Test
    public void test12c(){
        List<Insn> insns = new LinkedList<>();

        EnumSet<Bypass> mySet = EnumSet.noneOf(Bypass.class);
        mySet.add(Bypass.WX);
        sim = new InorderPipeline(0, mySet);

        insns.add(makeInsn(6, 4, 3, null));
        insns.add(makeInsn(7, 4, 6, MemoryOp.Load));
        sim.run(insns);

        assertEquals(2, sim.getInsns());
        // 123456789abcdef
        // fdxmw  |
        //  fd-xmw|
        assertEquals(8, sim.getCycles());
    }

    @Test
    public void test13(){
        List<Insn> insns = new LinkedList<>();

        EnumSet<Bypass> mySet = EnumSet.noneOf(Bypass.class);
        mySet.add(Bypass.WX);
        mySet.add(Bypass.WM);
        mySet.add(Bypass.MX);
        sim = new InorderPipeline(0, mySet);

        insns.add(makeInsn(6, 4, 3, MemoryOp.Load));
        insns.add(makeInsn(6, 6, 6, MemoryOp.Store));
        sim.run(insns);

        assertEquals(2, sim.getInsns());
        // 123456789abcdef
        // fdxmw  |
        //  fd-xmw|
        assertEquals(8, sim.getCycles());
    }

    @Test
    public void test14(){
        List<Insn> insns = new LinkedList<>();

        EnumSet<Bypass> mySet = EnumSet.noneOf(Bypass.class);
        mySet.add(Bypass.WM);
        mySet.add(Bypass.MX);
        sim = new InorderPipeline(0, mySet);

        insns.add(makeInsn(6, 4, 3, MemoryOp.Load));
        insns.add(makeInsn(6, 6, 6, MemoryOp.Store));
        sim.run(insns);

        assertEquals(2, sim.getInsns());
        // 123456789abcdef
        // fdxmw   |
        //  fd--xmw|
        assertEquals(9, sim.getCycles());
    }

    @Test
    public void test15(){
        List<Insn> insns = new LinkedList<>();

        EnumSet<Bypass> mySet = EnumSet.noneOf(Bypass.class);
        mySet.add(Bypass.WM);
        mySet.add(Bypass.MX);
        sim = new InorderPipeline(0, mySet);

        insns.add(makeInsn(6, 4, 3, MemoryOp.Load));
        insns.add(makeInsn(6, 6, 5, MemoryOp.Store));
        sim.run(insns);

        assertEquals(2, sim.getInsns());
        // 123456789abcdef
        // fdxmw |
        //  fdxmw|
        assertEquals(7, sim.getCycles());
    }

    @Test
    public void test16(){
        List<Insn> insns = new LinkedList<>();

        EnumSet<Bypass> mySet = EnumSet.noneOf(Bypass.class);
        mySet.add(Bypass.WM);
        mySet.add(Bypass.MX);
        sim = new InorderPipeline(0, mySet);

        insns.add(makeInsn(6, 4, 3, MemoryOp.Load));
        insns.add(makeInsn(6, 5, 6, MemoryOp.Store));
        sim.run(insns);

        assertEquals(2, sim.getInsns());
        // 123456789abcdef
        // fdxmw   |
        //  fd--xmw|
        assertEquals(9, sim.getCycles());
    }

    @Test
    public void test17(){
        List<Insn> insns = new LinkedList<>();

        EnumSet<Bypass> mySet = EnumSet.noneOf(Bypass.class);
        mySet.add(Bypass.WX);
        sim = new InorderPipeline(0, mySet);

        insns.add(makeInsn(6, 4, 3, MemoryOp.Load));
        insns.add(makeInsn(6, 5, 6, MemoryOp.Store));
        sim.run(insns);

        assertEquals(2, sim.getInsns());
        // 123456789abcdef
        // fdxmw  |
        //  fd-xmw|
        assertEquals(8, sim.getCycles());
    }

    @Test
    public void test18(){
        List<Insn> insns = new LinkedList<>();

        EnumSet<Bypass> mySet = EnumSet.noneOf(Bypass.class);
        sim = new InorderPipeline(0, mySet);

        insns.add(makeInsn(3, 4, 3, MemoryOp.Load));
        insns.add(makeInsn(4, 4, 4, MemoryOp.Store));
        sim.run(insns);

        assertEquals(2, sim.getInsns());
        // 123456789abcdef
        // fdxmw |
        //  fdxmw|
        assertEquals(7, sim.getCycles());
    }

    @Test
    public void test19(){
        List<Insn> insns = new LinkedList<>();

        EnumSet<Bypass> mySet = EnumSet.noneOf(Bypass.class);
        sim = new InorderPipeline(3, mySet);

        insns.add(makeInsn(3, 4, 3, MemoryOp.Load));
        insns.add(makeInsn(4, 4, 4, MemoryOp.Store));
        sim.run(insns);

        assertEquals(2, sim.getInsns());
        // 123456789abcdef
        // fdxm---w    |
        //  fdx---m---w|
        assertEquals(13, sim.getCycles());
    }

    @Test
    public void test20(){
        List<Insn> insns = new LinkedList<>();

        EnumSet<Bypass> mySet = EnumSet.noneOf(Bypass.class);
        sim = new InorderPipeline(3, mySet);

        insns.add(makeInsn(3, 4, 3, MemoryOp.Load));
        insns.add(makeInsn(4, 4, 4, null));
        sim.run(insns);

        assertEquals(2, sim.getInsns());
        // 123456789abcdef
        // fdxm---w |
        //  fdx---mw|
        assertEquals(10, sim.getCycles());
    }

        @Test
    public void testALUtoStoreValue3(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(3,1,2, null));
        insns.add(makeInsn(0,3,4, MemoryOp.Store));
        sim = new InorderPipeline(0, set);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdefgh
        // FDXMW |
        //  FDXMW|
        final long expected = 7;
        assertEquals(expected, sim.getCycles());
    }

            @Test
    public void testALUtoStoreValue4(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(3,1,2, null));
        insns.add(makeInsn(0,3,4, MemoryOp.Store));
        sim = new InorderPipeline(1, set);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdefgh
        // FDXM-W |
        //  FDX MW|
        final long expected = 8;
        assertEquals(expected, sim.getCycles());
    }

            @Test
    public void testALUtoStoreValue5(){
        List<Insn> insns = new LinkedList<>();
        insns.add(makeInsn(3,1,2, null));
        insns.add(makeInsn(0,3,4, MemoryOp.Store));
        sim = new InorderPipeline(3, set);
        sim.run(insns);
        assertEquals(insns.size(), sim.getInsns());
        // 123456789abcdefgh
        // FDXM---W |
        //  FDX---MW|
        final long expected = 10;
        assertEquals(expected, sim.getCycles());
    }

}
