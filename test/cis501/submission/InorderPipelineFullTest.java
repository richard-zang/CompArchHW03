package cis501.submission;

import cis501.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static cis501.Bypass.FULL_BYPASS;
import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class InorderPipelineFullTest {

    private static Insn makeInsn(int dst, int src1, int src2, MemoryOp mop) {
        return new Insn(dst, src1, src2,
                1, 4,
                null, 0, null,
                mop, 1, 1,
                "synthetic");
    }

    @RunWith(Parameterized.class)
    public static class FullBypassTests {

        @Rule
        public final Timeout globalTimeout = Timeout.seconds(2);

        private final int ADDL_MEM_LAT;
        private final String MSG;
        private final IInorderPipeline sim;

        public FullBypassTests(int additionalMemLat) {
            super();
            ADDL_MEM_LAT = additionalMemLat;
            MSG = "[Add'l memory latency = " + ADDL_MEM_LAT + "]";
            final Set<Bypass> fullbp = FULL_BYPASS;

            sim = new cis501.submission.InorderPipeline(additionalMemLat, fullbp);
        }

        /** The memory latencies to test. */
        @Parameterized.Parameters
        public static Collection memLatencies() {
            return new CtorParams(0).p(1).p(3);
        }

        @Test
        public void test1Insn() {
            List<Insn> insns = new LinkedList<>();
            insns.add(makeInsn(3, 1, 2, null));
            sim.run(insns);
            assertEquals(MSG, 1, sim.getInsns());
            // 123456
            // fdxmw|
            assertEquals(MSG, 6, sim.getCycles());
        }

        @Test
        public void testManyInsns() {
            List<Insn> insns = new LinkedList<>();
            final int COUNT = 10;
            for (int i = 0; i < COUNT; i++) {
                insns.add(makeInsn(3, 1, 2, null));
            }
            sim.run(insns);
            assertEquals(MSG, COUNT, sim.getInsns());
            assertEquals(MSG, 5 + COUNT, sim.getCycles());
        }

        @Test
        public void test1MemInsn() {
            List<Insn> insns = new LinkedList<>();
            insns.add(makeInsn(3, 1, 2, MemoryOp.Load));
            sim.run(insns);
            assertEquals(MSG, 1, sim.getInsns());
            // 123456789abcdef
            // fdxmmmmw|
            assertEquals(MSG, 6 + ADDL_MEM_LAT, sim.getCycles());
        }

        @Test
        public void testManyMemInsns() {
            List<Insn> insns = new LinkedList<>();
            final int COUNT = 10;
            for (int i = 0; i < COUNT; i++) {
                insns.add(makeInsn(3, 1, 2, MemoryOp.Store)); // no load-use dependencies
            }
            sim.run(insns);
            assertEquals(MSG, COUNT, sim.getInsns());
            // 123456789abcdefghi
            // fdxmmmmw        |
            //  fdx   mmmmw    |
            //   fd   x   mmmmw|
            assertEquals(MSG, 5 + COUNT + (COUNT * ADDL_MEM_LAT), sim.getCycles());
        }

        @Test
        public void testLoadUse1() {
            List<Insn> insns = new LinkedList<>();
            insns.add(makeInsn(3, 1, 2, MemoryOp.Load));
            insns.add(makeInsn(5, 3, 4, null)); // load to src reg 1
            sim.run(insns);
            assertEquals(MSG, 2, sim.getInsns());
            // 123456789abcdef
            // fdxmmmmw  |
            //  fd    xmw|
            assertEquals(MSG, 6 + ADDL_MEM_LAT + 2, sim.getCycles());
        }

        @Test
        public void testLoadUse2() {
            List<Insn> insns = new LinkedList<>();
            insns.add(makeInsn(3, 1, 2, MemoryOp.Load));
            insns.add(makeInsn(5, 4, 3, null)); // load to src reg 2
            sim.run(insns);
            assertEquals(MSG, 2, sim.getInsns());
            // 123456789abcdef
            // fdxmmmmw  |
            //  fd    xmw|
            assertEquals(MSG, 6 + ADDL_MEM_LAT + 2, sim.getCycles());
        }

        @Test
        public void testLoadUseStoreAddress() {
            List<Insn> insns = new LinkedList<>();
            insns.add(makeInsn(3, 1, 2, MemoryOp.Load));
            insns.add(makeInsn(5, 4, 3, MemoryOp.Store)); // load to src reg 2 (store address), so we stall
            sim.run(insns);
            assertEquals(MSG, 2, sim.getInsns());
            // 123456789abcdef
            // fdxmmmmw     |
            //  fd    xmmmmw|
            final long expected = 6 + (2 * ADDL_MEM_LAT) + 2;
            assertEquals(MSG, expected, sim.getCycles());
        }

        @Test
        public void testLoadUseStoreValue() {
            List<Insn> insns = new LinkedList<>();
            insns.add(makeInsn(3, 1, 2, MemoryOp.Load));
            insns.add(makeInsn(5, 3, 4, MemoryOp.Store)); // load to src reg 1 (store value), so no stall
            sim.run(insns);
            assertEquals(MSG, 2, sim.getInsns());
            // 123456789abcdef
            // fdxmmmmw    |
            // fdx    mmmmw|
            final long expected = 6 + (2 * ADDL_MEM_LAT) + 1;
            assertEquals(MSG, expected, sim.getCycles());
        }

        @Test
        public void testLoadUseStoreAddress2() {
            List<Insn> insns = new LinkedList<>();
            insns.add(makeInsn(3, 1, 2, MemoryOp.Load));
            insns.add(makeInsn(5, 4, 3, MemoryOp.Store)); // load to src reg 2 (store address), so we stall
            insns.add(makeInsn(8, 6, 7, null)); // independent insn
            sim.run(insns);
            assertEquals(MSG, 3, sim.getInsns());
            // 123456789abcdef
            // fdxmmmmw      |
            //  fd    xmmmmw |
            //   f    dx   mw|
            final long expected = 6 + (2 * ADDL_MEM_LAT) + 3;
            assertEquals(MSG, expected, sim.getCycles());
        }

        @Test
        public void testLoadUseStoreValue2() {
            List<Insn> insns = new LinkedList<>();
            insns.add(makeInsn(3, 1, 2, MemoryOp.Load));
            insns.add(makeInsn(5, 3, 4, MemoryOp.Store)); // load to src reg 1 (store value), so no stall
            insns.add(makeInsn(8, 6, 7, null)); // independent insn
            sim.run(insns);
            assertEquals(MSG, 3, sim.getInsns());
            // 123456789abcdef
            // fdxmmmmw     |
            //  fdx   mmmmw |
            //   fd   x   mw|
            final long expected = 6 + (2 * ADDL_MEM_LAT) + 2;
            assertEquals(MSG, expected, sim.getCycles());
        }
    }

}
