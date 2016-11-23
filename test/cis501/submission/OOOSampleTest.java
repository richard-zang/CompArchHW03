package cis501.submission;

import cis501.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.Collection;

import static cis501.IOOORegisterRenamer.NUM_ARCH_REGS;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class OOOSampleTest {

    private final static int LOADQ_ENTRIES = 6;
    private final static int STOREQ_ENTRIES = 6;

    public static class RegisterRenamerTests {

        private static final int PREGS = 60;
        private IOOORegisterRenamer rr;

        /** Runs before each test...() method */
        @Before
        public void setup() {
            rr = new OOORegisterRenamer(PREGS);
        }

        @Test
        public void testInitialFreelist() {
            assertEquals(PREGS - NUM_ARCH_REGS, rr.availablePhysRegs());
        }

        @Test
        public void testInitialMapping() {
            assertEquals(1, rr.a2p(1).get());
            assertEquals(10, rr.a2p(10).get());
        }

        @Test
        public void testAllocate() {
            assertEquals(NUM_ARCH_REGS, rr.allocateReg(1).get());
            assertEquals(NUM_ARCH_REGS+1, rr.allocateReg(2).get());
        }

        @Test
        public void testFreeReallocate() {
            rr.freeReg(new PhysReg(1)); // goes to back of free list
            for (int i = 0; i < (PREGS - NUM_ARCH_REGS); i++) { // empty the free list, except for p1
                assertEquals(NUM_ARCH_REGS + i, rr.allocateReg(i).get());
            }
            assertEquals(1, rr.allocateReg(PREGS - NUM_ARCH_REGS).get()); // p1 gets reused
        }
    }

    public static class LSQSingleByteTests {

        private IOOOLoadStoreQueue lsq;

        @Before
        public void setup() {
            lsq = new OOOLoadStoreQueue(LOADQ_ENTRIES, STOREQ_ENTRIES);
        }

        @Test
        public void testCapacity() {
            lsq.commitOldest(); // clear out preamble insn, if there was one
            for (int i = 0; i < LOADQ_ENTRIES; i++) {
                assertTrue(lsq.roomForLoad());
                lsq.dispatchLoad(1);
            }
            assertFalse(lsq.roomForLoad());

            for (int i = 0; i < STOREQ_ENTRIES; i++) {
                assertTrue(lsq.roomForStore());
                lsq.dispatchStore(1);
            }
            assertFalse(lsq.roomForStore());
        }

        @Test
        public void test1() {
            StoreHandle s = lsq.dispatchStore(1);
            assertTrue(lsq.executeStore(s, 0xA, 100).isEmpty());
            LoadHandle l = lsq.dispatchLoad(1);
            assertEquals(0, lsq.executeLoad(l, 0xB));
        }

        @Test
        public void test2() {
            LoadHandle l = lsq.dispatchLoad(1);
            StoreHandle s = lsq.dispatchStore(1);

            assertTrue(lsq.executeStore(s, 0xA, 100).isEmpty());
            assertEquals(0, lsq.executeLoad(l, 0xB));
        }

        @Test
        public void test3() {
            StoreHandle s = lsq.dispatchStore(1);
            assertTrue(lsq.executeStore(s, 0xA, 100).isEmpty());
            LoadHandle l = lsq.dispatchLoad(1);
            assertEquals(100, lsq.executeLoad(l, 0xA));
        }

        @Test
        public void test4() {
            StoreHandle s0 = lsq.dispatchStore(1);
            StoreHandle s1 = lsq.dispatchStore(1);
            LoadHandle l = lsq.dispatchLoad(1);

            assertTrue(lsq.executeStore(s0, 0xA, 100).isEmpty());
            assertTrue(lsq.executeStore(s1, 0xA, 200).isEmpty());
            assertEquals(200, lsq.executeLoad(l, 0xA));
        }

        @Test
        public void test5() {
            StoreHandle s0 = lsq.dispatchStore(1);
            StoreHandle s1 = lsq.dispatchStore(1);
            LoadHandle l = lsq.dispatchLoad(1);

            assertTrue(lsq.executeStore(s1, 0xA, 200).isEmpty());
            assertTrue(lsq.executeStore(s0, 0xA, 100).isEmpty());
            assertEquals(200, lsq.executeLoad(l, 0xA));
        }

        @Test
        public void test6() {
            StoreHandle s0 = lsq.dispatchStore(1);
            StoreHandle s1 = lsq.dispatchStore(1);
            LoadHandle l = lsq.dispatchLoad(1);

            assertTrue(lsq.executeStore(s1, 0xA, 200).isEmpty());
            assertEquals(200, lsq.executeLoad(l, 0xA));
            assertTrue(lsq.executeStore(s0, 0xA, 100).isEmpty());
        }

        @Test
        public void test7() {
            StoreHandle s0 = lsq.dispatchStore(1);
            StoreHandle s1 = lsq.dispatchStore(1);
            LoadHandle l = lsq.dispatchLoad(1);

            assertTrue(lsq.executeStore(s0, 0xA, 100).isEmpty());
            assertEquals(100, lsq.executeLoad(l, 0xA));
            Collection<? extends LoadHandle> squashed = lsq.executeStore(s1, 0xA, 200);
            assertTrue(squashed.contains(l));
            assertEquals(1, squashed.size());
        }

        @Test
        public void test8() {
            StoreHandle s0 = lsq.dispatchStore(1);
            LoadHandle l = lsq.dispatchLoad(1);
            StoreHandle s1 = lsq.dispatchStore(1);

            assertTrue(lsq.executeStore(s0, 0xA, 100).isEmpty());
            assertEquals(100, lsq.executeLoad(l, 0xA));
            assertTrue(lsq.executeStore(s1, 0xA, 200).isEmpty());
        }

        @Test
        public void test9() {
            StoreHandle s0 = lsq.dispatchStore(1);
            LoadHandle l = lsq.dispatchLoad(1);
            StoreHandle s1 = lsq.dispatchStore(1);

            assertTrue(lsq.executeStore(s0, 0xA, 100).isEmpty());
            assertTrue(lsq.executeStore(s1, 0xA, 200).isEmpty());
            assertEquals(100, lsq.executeLoad(l, 0xA));
        }

        @Test
        public void test10() {
            StoreHandle s0 = lsq.dispatchStore(1);
            LoadHandle l = lsq.dispatchLoad(1);
            StoreHandle s1 = lsq.dispatchStore(1);

            assertTrue(lsq.executeStore(s1, 0xA, 200).isEmpty());
            assertTrue(lsq.executeStore(s0, 0xA, 100).isEmpty());
            assertEquals(100, lsq.executeLoad(l, 0xA));
        }

        @Test
        public void test25() {
            StoreHandle s = lsq.dispatchStore(1);
            LoadHandle l = lsq.dispatchLoad(1);

            assertTrue(lsq.executeStore(s, 0xA, 100).isEmpty());
            lsq.commitOldest(); // commit store
            assertEquals(0, lsq.executeLoad(l, 0xA));
        }

        @Test
        public void test26() {
            StoreHandle s1 = lsq.dispatchStore(1);
            LoadHandle l = lsq.dispatchLoad(1);
            StoreHandle s2 = lsq.dispatchStore(1);

            assertTrue(lsq.executeStore(s1, 0xA, 100).isEmpty());
            assertTrue(lsq.executeStore(s2, 0xA, 200).isEmpty());
            assertEquals(100, lsq.executeLoad(l, 0xA));
            lsq.commitOldest(); // commit store
        }

        @Test
        public void test27() {
            lsq.commitOldest(); // should be a NOP
            StoreHandle s1 = lsq.dispatchStore(1);
            LoadHandle l = lsq.dispatchLoad(1);
            StoreHandle s2 = lsq.dispatchStore(1);

            assertTrue(lsq.executeStore(s1, 0xA, 100).isEmpty());
            assertTrue(lsq.executeStore(s2, 0xA, 200).isEmpty());
            lsq.commitOldest(); // commit store
            assertEquals(0, lsq.executeLoad(l, 0xA));
        }


        // MULTI-BYTE TESTS

        @Test
        public void testMultiByte0() {
            assertTrue(lsq.executeStore(lsq.dispatchStore(2), 0xA, 0x1122).isEmpty());
            assertEquals(0x1122, lsq.executeLoad(lsq.dispatchLoad(2), 0xA));
        }

        @Test
        public void testMultiByte1() {
            assertTrue(lsq.executeStore(lsq.dispatchStore(1), 0xA, 0x11).isEmpty());
            assertTrue(lsq.executeStore(lsq.dispatchStore(1), 0xB, 0x22).isEmpty());
            assertEquals(0x1122, lsq.executeLoad(lsq.dispatchLoad(2), 0xA));
        }

        @Test
        public void testMultiByte2() {
            assertTrue(lsq.executeStore(lsq.dispatchStore(1), 0x8, 0x11).isEmpty());
            assertTrue(lsq.executeStore(lsq.dispatchStore(1), 0x9, 0x22).isEmpty());
            assertTrue(lsq.executeStore(lsq.dispatchStore(1), 0xA, 0x33).isEmpty());
            assertTrue(lsq.executeStore(lsq.dispatchStore(1), 0xB, 0x44).isEmpty());
            assertEquals(0x11223344, lsq.executeLoad(lsq.dispatchLoad(4), 0x8));
        }

        @Test
        public void testMultiByte3() {
            assertTrue(lsq.executeStore(lsq.dispatchStore(2), 0x8, 0x1122).isEmpty());
            assertTrue(lsq.executeStore(lsq.dispatchStore(2), 0xA, 0x3344).isEmpty());
            assertEquals(0x11223344, lsq.executeLoad(lsq.dispatchLoad(4), 0x8));
        }

        @Test
        public void testMultiByte4() {
            assertTrue(lsq.executeStore(lsq.dispatchStore(2), 0xA, 0x1122).isEmpty());
            assertEquals(0x22, lsq.executeLoad(lsq.dispatchLoad(1), 0xB));
        }

        @Test
        public void testMultiByte5() {
            assertTrue(lsq.executeStore(lsq.dispatchStore(1), 0x8, 0x11).isEmpty());
            assertTrue(lsq.executeStore(lsq.dispatchStore(1), 0x9, 0x22).isEmpty());
            assertTrue(lsq.executeStore(lsq.dispatchStore(1), 0xB, 0x44).isEmpty());
            assertEquals(0x11220044, lsq.executeLoad(lsq.dispatchLoad(4), 0x8));
        }
    }
}
