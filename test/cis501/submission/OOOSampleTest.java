package cis501.submission;

import cis501.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
            assertEquals(NUM_ARCH_REGS + 1, rr.allocateReg(2).get());
        }

        @Test
        public void testFreeReallocate() {
            rr.freeReg(new PhysReg(1)); // goes to back of free list
            int i = 0;
            for (; i < (PREGS - NUM_ARCH_REGS); i++) { // empty the free list, except for p1
                assertEquals(NUM_ARCH_REGS + i, rr.allocateReg(i % NUM_ARCH_REGS).get());
            }
            assertEquals(1, rr.allocateReg(i % NUM_ARCH_REGS).get()); // p1 gets reused
        }

        @Test
        public void testTrace500() {
            /* Note: Each element in this array contains the correct input physical registers for
            each of the first 500 insns of the streamcluster trace file (output physical registers
            are in a separate array, below). These renamed registers assume the use of 60 physical
            registers. The code after the arrays uses them to validate your register renaming
            implementation. */
            final int[][] inputPregs = new int[][]{{3}, // 0: cmp r3, #1
                    {17}, // 1: bne.w #0x4006e74
                    {}, // 2: mov.w r8, #1
                    {13}, // 3: ldr r3, [sp, #0x9c]
                    {19}, // 4: cmp r3, #0
                    {20}, // 5: ite eq
                    {20}, // 6: moveq r3, #0
                    {20, 18}, // 7: andne r3, r8, #1
                    {22}, // 8: cbz r3, #0x4006e98
                    {13}, // 9: ldr r3, [sp, #0x9c]
                    {23}, // 10: ldr r0, [r3, #0xc]
                    {24}, // 11: cbz r0, #0x4006e98
                    {}, // 12: adds r5, #1
                    {25, 11}, // 13: cmp r11, r5
                    {27}, // 14: bhi #0x4006df6
                    {6}, // 15: ldr r3, [r6], #4
                    {28}, // 16: ldr r4, [r3, #0x14]
                    {29, 9}, // 17: cmp r4, r9
                    {30}, // 18: beq #0x4006e98
                    {7}, // 19: ands r10, r7, #2
                    {32}, // 20: beq #0x4006e0e
                    {29}, // 21: ldrb.w r3, [r4, #0x195]
                    {33}, // 22: lsls r2, r3, #0x1a
                    {35}, // 23: bmi #0x4006e98
                    {13}, // 24: ldr r3, [sp, #0x20]
                    {36}, // 25: ldr r3, [r3]
                    {37}, // 26: lsls r3, r3, #0x1c
                    {39}, // 27: bmi #0x4006ea6
                    {29}, // 28: ldr.w r1, [r4, #0x178]
                    {40}, // 29: cmp r1, #0
                    {41}, // 30: beq #0x4006e98
                    {29}, // 31: ldr r2, [r4, #0x38]
                    {}, // 32: movs r0, #0
                    {29}, // 33: ldr r3, [r4, #0x34]
                    {29}, // 34: ldr.w lr, [r4, #0x184]
                    {42}, // 35: ldr r2, [r2, #4]
                    {45}, // 36: ldr r3, [r3, #4]
                    {43, 13}, // 37: str r0, [sp, #0x60]
                    {47, 13}, // 38: str r2, [sp, #0x38]
                    {48, 13}, // 39: str r3, [sp, #0x24]
                    {43, 13}, // 40: str r0, [sp, #0x64]
                    {46}, // 41: cmp.w lr, #0
                    {49}, // 42: beq #0x4006ed2
                    {29}, // 43: ldr.w r3, [r4, #0x180]
                    {29}, // 44: ldr.w r0, [r4, #0x17c]
                    {13}, // 45: ldr r2, [sp, #0x28]
                    {50, 13}, // 46: str r3, [sp, #0x2c]
                    {13}, // 47: ldr r3, [sp, #0x30]
                    {52}, // 48: and r12, r2, #0x1f
                    {53}, // 49: ands r0, r3
                    {13}, // 50: ldr r3, [sp, #0x2c]
                    {55, 46}, // 51: ldr.w r0, [lr, r0, lsl #2]
                    {52, 57}, // 52: lsr.w r3, r2, r3
                    {59}, // 53: and r3, r3, #0x1f
                    {54}, // 54: mov r2, r12
                    {58, 16}, // 55: lsr.w r3, r0, r3
                    {8}, // 56: lsrs r0, r2
                    {17}, // 57: ands r3, r0
                    {21}, // 58: lsls r0, r3, #0x1f
                    {5}, // 59: bmi.w #0x4007180
                    {}, // 60: mov.w r8, #1
                    {13}, // 61: ldr r3, [sp, #0x9c]
                    {26}, // 62: cmp r3, #0
                    {23}, // 63: ite eq
                    {23}, // 64: moveq r3, #0
                    {23, 20}, // 65: andne r3, r8, #1
                    {27}, // 66: cbz r3, #0x4006e98
                    {13}, // 67: ldr r3, [sp, #0x9c]
                    {10}, // 68: ldr r0, [r3, #0xc]
                    {30}, // 69: cbz r0, #0x4006e98
                    {}, // 70: adds r5, #1
                    {28, 11}, // 71: cmp r11, r5
                    {32}, // 72: bhi #0x4006df6
                    {6}, // 73: ldr r3, [r6], #4
                    {33}, // 74: ldr r4, [r3, #0x14]
                    {36, 9}, // 75: cmp r4, r9
                    {37}, // 76: beq #0x4006e98
                    {7}, // 77: ands r10, r7, #2
                    {1}, // 78: beq #0x4006e0e
                    {36}, // 79: ldrb.w r3, [r4, #0x195]
                    {39}, // 80: lsls r2, r3, #0x1a
                    {24}, // 81: bmi #0x4006e98
                    {13}, // 82: ldr r3, [sp, #0x20]
                    {41}, // 83: ldr r3, [r3]
                    {38}, // 84: lsls r3, r3, #0x1c
                    {42}, // 85: bmi #0x4006ea6
                    {36}, // 86: ldr.w r1, [r4, #0x178]
                    {45}, // 87: cmp r1, #0
                    {44}, // 88: beq #0x4006e98
                    {36}, // 89: ldr r2, [r4, #0x38]
                    {}, // 90: movs r0, #0
                    {36}, // 91: ldr r3, [r4, #0x34]
                    {36}, // 92: ldr.w lr, [r4, #0x184]
                    {48}, // 93: ldr r2, [r2, #4]
                    {50}, // 94: ldr r3, [r3, #4]
                    {43, 13}, // 95: str r0, [sp, #0x60]
                    {51, 13}, // 96: str r2, [sp, #0x38]
                    {49, 13}, // 97: str r3, [sp, #0x24]
                    {43, 13}, // 98: str r0, [sp, #0x64]
                    {12}, // 99: cmp.w lr, #0
                    {53}, // 100: beq #0x4006ed2
                    {36}, // 101: ldr.w r3, [r4, #0x180]
                    {36}, // 102: ldr.w r0, [r4, #0x17c]
                    {13}, // 103: ldr r2, [sp, #0x28]
                    {55, 13}, // 104: str r3, [sp, #0x2c]
                    {13}, // 105: ldr r3, [sp, #0x30]
                    {59}, // 106: and r12, r2, #0x1f
                    {52}, // 107: ands r0, r3
                    {13}, // 108: ldr r3, [sp, #0x2c]
                    {58, 12}, // 109: ldr.w r0, [lr, r0, lsl #2]
                    {59, 3}, // 110: lsr.w r3, r2, r3
                    {17}, // 111: and r3, r3, #0x1f
                    {16}, // 112: mov r2, r12
                    {19, 22}, // 113: lsr.w r3, r0, r3
                    {18}, // 114: lsrs r0, r2
                    {5}, // 115: ands r3, r0
                    {4}, // 116: lsls r0, r3, #0x1f
                    {25}, // 117: bmi.w #0x4007180
                    {13}, // 118: ldr.w r8, [sp, #0x28]
                    {23}, // 119: mov r0, r8
                    {}, // 120: bl #0x40135a4
                    {45}, // 121: cmp r1, #0
                    {29}, // 122: beq #0x401359a
                    {2, 45}, // 123: push.w {r0, r1, lr}
                    {}, // 124: bl #0x4013348
                    {45}, // 125: subs r2, r1, #1
                    {37}, // 126: it eq
                    {37, 32}, // 127: bxeq lr
                    {37}, // 128: blo.w #0x401359a
                    {2, 45}, // 129: cmp r0, r1
                    {33}, // 130: bls.w #0x4013584
                    {45, 31}, // 131: tst r1, r2
                    {8}, // 132: beq.w #0x401358c
                    {2}, // 133: clz r3, r0
                    {45}, // 134: clz r2, r1
                    {39, 1}, // 135: sub.w r3, r2, r3
                    {41}, // 136: rsb.w r3, r3, #0x1f
                    {}, // 137: adr r2, #0x10
                    {24, 38}, // 138: add.w r3, r2, r3, lsl #4
                    {}, // 139: mov.w r2, #0
                    {40}, // 140: mov pc, r3
                    {2, 45}, // 141: cmp.w r0, r1, lsl #18
                    {}, // 142: nop
                    {42}, // 143: adc.w r2, r2, r2
                    {34}, // 144: it hs
                    {2, 34, 45}, // 145: subhs.w r0, r0, r1, lsl #18
                    {44, 45}, // 146: cmp.w r0, r1, lsl #17
                    {}, // 147: nop
                    {30}, // 148: adc.w r2, r2, r2
                    {14}, // 149: it hs
                    {44, 14, 45}, // 150: subhs.w r0, r0, r1, lsl #17
                    {48, 45}, // 151: cmp.w r0, r1, lsl #16
                    {}, // 152: nop
                    {46}, // 153: adc.w r2, r2, r2
                    {50}, // 154: it hs
                    {48, 50, 45}, // 155: subhs.w r0, r0, r1, lsl #16
                    {49, 45}, // 156: cmp.w r0, r1, lsl #15
                    {}, // 157: nop
                    {47}, // 158: adc.w r2, r2, r2
                    {43}, // 159: it hs
                    {49, 43, 45}, // 160: subhs.w r0, r0, r1, lsl #15
                    {55, 45}, // 161: cmp.w r0, r1, lsl #14
                    {}, // 162: nop
                    {51}, // 163: adc.w r2, r2, r2
                    {54}, // 164: it hs
                    {55, 54, 45}, // 165: subhs.w r0, r0, r1, lsl #14
                    {53, 45}, // 166: cmp.w r0, r1, lsl #13
                    {}, // 167: nop
                    {57}, // 168: adc.w r2, r2, r2
                    {52}, // 169: it hs
                    {53, 52, 45}, // 170: subhs.w r0, r0, r1, lsl #13
                    {3, 45}, // 171: cmp.w r0, r1, lsl #12
                    {}, // 172: nop
                    {58}, // 173: adc.w r2, r2, r2
                    {17}, // 174: it hs
                    {3, 17, 45}, // 175: subhs.w r0, r0, r1, lsl #12
                    {22, 45}, // 176: cmp.w r0, r1, lsl #11
                    {}, // 177: nop
                    {59}, // 178: adc.w r2, r2, r2
                    {19}, // 179: it hs
                    {22, 19, 45}, // 180: subhs.w r0, r0, r1, lsl #11
                    {21, 45}, // 181: cmp.w r0, r1, lsl #10
                    {}, // 182: nop
                    {56}, // 183: adc.w r2, r2, r2
                    {26}, // 184: it hs
                    {21, 26, 45}, // 185: subhs.w r0, r0, r1, lsl #10
                    {27, 45}, // 186: cmp.w r0, r1, lsl #9
                    {}, // 187: nop
                    {5}, // 188: adc.w r2, r2, r2
                    {20}, // 189: it hs
                    {27, 20, 45}, // 190: subhs.w r0, r0, r1, lsl #9
                    {12, 45}, // 191: cmp.w r0, r1, lsl #8
                    {}, // 192: nop
                    {0}, // 193: adc.w r2, r2, r2
                    {25}, // 194: it hs
                    {12, 25, 45}, // 195: subhs.w r0, r0, r1, lsl #8
                    {18, 45}, // 196: cmp.w r0, r1, lsl #7
                    {}, // 197: nop
                    {10}, // 198: adc.w r2, r2, r2
                    {29}, // 199: it hs
                    {18, 29, 45}, // 200: subhs.w r0, r0, r1, lsl #7
                    {33, 45}, // 201: cmp.w r0, r1, lsl #6
                    {}, // 202: nop
                    {37}, // 203: adc.w r2, r2, r2
                    {4}, // 204: it hs
                    {33, 4, 45}, // 205: subhs.w r0, r0, r1, lsl #6
                    {1, 45}, // 206: cmp.w r0, r1, lsl #5
                    {}, // 207: nop
                    {31}, // 208: adc.w r2, r2, r2
                    {41}, // 209: it hs
                    {1, 41, 45}, // 210: subhs.w r0, r0, r1, lsl #5
                    {38, 45}, // 211: cmp.w r0, r1, lsl #4
                    {}, // 212: nop
                    {39}, // 213: adc.w r2, r2, r2
                    {24}, // 214: it hs
                    {38, 24, 45}, // 215: subhs.w r0, r0, r1, lsl #4
                    {42, 45}, // 216: cmp.w r0, r1, lsl #3
                    {}, // 217: nop
                    {8}, // 218: adc.w r2, r2, r2
                    {2}, // 219: it hs
                    {42, 2, 45}, // 220: subhs.w r0, r0, r1, lsl #3
                    {30, 45}, // 221: cmp.w r0, r1, lsl #2
                    {}, // 222: nop
                    {34}, // 223: adc.w r2, r2, r2
                    {44}, // 224: it hs
                    {30, 44, 45}, // 225: subhs.w r0, r0, r1, lsl #2
                    {46, 45}, // 226: cmp.w r0, r1, lsl #1
                    {}, // 227: nop
                    {14}, // 228: adc.w r2, r2, r2
                    {48}, // 229: it hs
                    {46, 48, 45}, // 230: subhs.w r0, r0, r1, lsl #1
                    {47, 45}, // 231: cmp.w r0, r1
                    {}, // 232: nop
                    {50}, // 233: adc.w r2, r2, r2
                    {49}, // 234: it hs
                    {47, 49, 45}, // 235: subhs.w r0, r0, r1
                    {43}, // 236: mov r0, r2
                    {32}, // 237: bx lr
                    {43, 32}, // 238: pop.w {r1, r2, lr}
                    {55, 43}, // 239: mul r3, r2, r0
                    {54, 57}, // 240: sub.w r1, r1, r3
                    {32}, // 241: bx lr
                    {36}, // 242: ldr.w r3, [r4, #0x188]
                    {53, 52}, // 243: ldr.w r3, [r3, r1, lsl #2]
                    {58}, // 244: cmp r3, #0
                    {3}, // 245: beq.w #0x4006e74
                    {36}, // 246: ldr.w r2, [r4, #0x18c]
                    {13}, // 247: add r1, sp, #0x60
                    {35, 13}, // 248: str.w r10, [sp, #0x54]
                    {17, 58}, // 249: add.w r3, r2, r3, lsl #2
                    {59, 13}, // 250: str r1, [sp, #0x2c]
                    {6, 13}, // 251: str r6, [sp, #0x58]
                    {13}, // 252: add r1, sp, #0x64
                    {28, 13}, // 253: str r5, [sp, #0x98]
                    {22}, // 254: mov r6, r3
                    {9, 13}, // 255: str.w r9, [sp, #0xa4]
                    {36}, // 256: mov r5, r4
                    {11, 13}, // 257: str.w r11, [sp, #0x5c]
                    {19, 13}, // 258: str r1, [sp, #0x50]
                    {13}, // 259: ldr.w r10, [sp, #0x3c]
                    {13}, // 260: ldr.w r9, [sp, #0x38]
                    {13}, // 261: ldr.w r11, [sp, #0x34]
                    {}, // 262: b #0x40071d4
                    {56}, // 263: ldr r3, [r6]
                    {20, 23}, // 264: eor.w r2, r8, r3
                    {0}, // 265: lsrs r2, r2, #1
                    {25}, // 266: bne #0x40071ca
                    {20}, // 267: lsls r1, r3, #0x1f
                    {56}, // 268: add.w r6, r6, #4
                    {18}, // 269: bmi.w #0x4007382
                    {29}, // 270: ldr r3, [r6]
                    {37, 23}, // 271: eor.w r2, r8, r3
                    {33}, // 272: lsrs r2, r2, #1
                    {31}, // 273: bne #0x40071ca
                    {37}, // 274: lsls r1, r3, #0x1f
                    {29}, // 275: add.w r6, r6, #4
                    {41}, // 276: bmi.w #0x4007382
                    {39}, // 277: ldr r3, [r6]
                    {38, 23}, // 278: eor.w r2, r8, r3
                    {24}, // 279: lsrs r2, r2, #1
                    {42}, // 280: bne #0x40071ca
                    {21}, // 281: ldr.w r4, [r5, #0x18c]
                    {13}, // 282: ldr r2, [sp, #0x2c]
                    {2, 39}, // 283: subs r4, r6, r4
                    {13}, // 284: ldr r1, [sp, #0x50]
                    {13}, // 285: ldr r0, [sp, #0x24]
                    {30}, // 286: asrs r4, r4, #2
                    {34, 13}, // 287: str r2, [sp, #0x18]
                    {14, 13}, // 288: str r1, [sp, #0x14]
                    {26}, // 289: mov r1, r10
                    {46, 13}, // 290: str r0, [sp, #0xc]
                    {48, 5}, // 291: add.w lr, r9, r4, lsl #4
                    {13}, // 292: ldr r3, [sp, #0xa0]
                    {27}, // 293: mov r0, r11
                    {13}, // 294: ldr r2, [sp, #0x9c]
                    {21, 13}, // 295: str r5, [sp, #0x10]
                    {48, 13}, // 296: str r4, [sp, #8]
                    {7, 13}, // 297: str r7, [sp]
                    {13, 51}, // 298: str.w lr, [sp, #4]
                    {}, // 299: bl #0x4006c98
                    {45, 48}, // 300: push.w {r3, r4, r5, r6, r7, r8, r9, lr}
                    {13}, // 301: ldr r4, [sp, #0x24]
                    {13}, // 302: ldr.w r9, [sp, #0x30]
                    {52}, // 303: ldrb r5, [r4, #0xc]
                    {52}, // 304: ldr r6, [r4, #4]
                    {43}, // 305: and r5, r5, #0xf
                    {53}, // 306: clz r6, r6
                    {58}, // 307: cmp r5, #6
                    {59}, // 308: lsr.w r6, r6, #5
                    {6}, // 309: it eq
                    {6}, // 310: moveq r6, #0
                    {35}, // 311: cmp r6, #0
                    {9}, // 312: bne #0x4006d5a
                    {52}, // 313: ldrh r6, [r4, #0xe]
                    {13}, // 314: ldr r7, [sp, #0x20]
                    {11}, // 315: cmp r6, #0
                    {17}, // 316: ite ne
                    {17}, // 317: movne r7, #0
                    {17, 0}, // 318: andeq r7, r7, #1
                    {3}, // 319: cmp r7, #0
                    {19}, // 320: bne #0x4006d5a
                    {}, // 321: movw r6, #0x467
                    {58, 25}, // 322: asr.w r5, r6, r5
                    {56}, // 323: lsls r5, r5, #0x1f
                    {12}, // 324: bpl #0x4006d5a
                    {45}, // 325: mov r6, r3
                    {47}, // 326: mov r3, r1
                    {18, 52}, // 327: cmp r4, r3
                    {54}, // 328: mov r5, r2
                    {40}, // 329: mov r1, r0
                    {10}, // 330: beq #0x4006cf4
                    {49}, // 331: ldr.w r3, [r9, #0x1a4]
                    {31}, // 332: cbz r5, #0x4006d2e
                    {37}, // 333: cmp r3, #0
                    {4}, // 334: beq #0x4006d6e
                    {13}, // 335: ldr r2, [sp, #0x28]
                    {31}, // 336: ldr r1, [r5, #4]
                    {24, 37}, // 337: ldrh.w r8, [r3, r2, lsl #1]
                    {49}, // 338: ldr.w r2, [r9, #0x170]
                    {36}, // 339: ubfx r3, r8, #0, #0xf
                    {2}, // 340: lsls r3, r3, #4
                    {8, 42}, // 341: adds r0, r2, r3
                    {55}, // 342: ldr r6, [r0, #4]
                    {41, 44}, // 343: cmp r6, r1
                    {14}, // 344: beq #0x4006d60
                    {8, 42}, // 345: ldr r0, [r2, r3]
                    {31}, // 346: ldr r1, [r5]
                    {}, // 347: bl #0x4012090
                    {32}, // 348: ldrb r2, [r0]
                    {38}, // 349: ldrb r3, [r1]
                    {34}, // 350: cmp r2, #1
                    {48}, // 351: it hs
                    {48, 34, 51}, // 352: cmphs r2, r3
                    {5}, // 353: bne #0x4012080
                    {52, 31}, // 354: strd r4, r5, [sp, #-0x10]!
                    {32, 38}, // 355: orr.w r4, r0, r1
                    {44, 3}, // 356: strd r6, r7, [sp, #8]
                    {}, // 357: mvn r12, #0
                    {21}, // 358: lsl.w r2, r4, #0x1d
                    {43}, // 359: cbz r2, #0x4012100
                    {32, 38}, // 360: eor.w r4, r0, r1
                    {53}, // 361: tst.w r4, #7
                    {50}, // 362: bne #0x401219a
                    {32}, // 363: and r4, r0, #7
                    {32}, // 364: bic r0, r0, #7
                    {59}, // 365: and r5, r4, #3
                    {38}, // 366: bic r1, r1, #7
                    {6}, // 367: lsl.w r5, r5, #3
                    {28, 51}, // 368: ldrd r2, r3, [r0], #0x10
                    {59}, // 369: tst.w r4, #4
                    {35, 3}, // 370: ldrd r6, r7, [r1], #0x10
                    {7, 39}, // 371: lsl.w r4, r12, r5
                    {9, 17}, // 372: orn r2, r2, r4
                    {17, 0}, // 373: orn r6, r6, r4
                    {22}, // 374: beq #0x4012108
                    {11, 39}, // 375: uadd8 r5, r2, r12
                    {11, 58}, // 376: eor.w r4, r2, r6
                    {19, 39}, // 377: sel r4, r4, r12
                    {25}, // 378: cbnz r4, #0x4012172
                    {51, 39}, // 379: uadd8 r5, r3, r12
                    {51, 3}, // 380: eor.w r5, r3, r7
                    {12, 39}, // 381: sel r5, r5, r12
                    {20}, // 382: cbnz r5, #0x401214a
                    {28, 51}, // 383: ldrd r2, r3, [r0, #-0x8]
                    {35, 3}, // 384: ldrd r6, r7, [r1, #-0x8]
                    {47, 39}, // 385: uadd8 r5, r2, r12
                    {47, 18}, // 386: eor.w r4, r2, r6
                    {54, 39}, // 387: sel r4, r4, r12
                    {51, 39}, // 388: uadd8 r5, r3, r12
                    {51, 3}, // 389: eor.w r5, r3, r7
                    {24, 39}, // 390: sel r5, r5, r12
                    {29}, // 391: orrs r5, r4
                    {4}, // 392: beq #0x4012100
                    {29}, // 393: cbnz r4, #0x4012172
                    {2}, // 394: rev r5, r5
                    {40}, // 395: clz r4, r5
                    {1}, // 396: bic r4, r4, #7
                    {33, 3}, // 397: lsr.w r1, r7, r4
                    {3, 13}, // 398: ldrd r6, r7, [sp, #8]
                    {51, 33}, // 399: lsr.w r3, r3, r4
                    {41}, // 400: and r0, r3, #0xff
                    {30}, // 401: and r1, r1, #0xff
                    {40, 13}, // 402: ldrd r4, r5, [sp], #0x10
                    {57, 8}, // 403: sub.w r0, r0, r1
                    {46}, // 404: bx lr
                    {14}, // 405: cmp r0, #0
                    {48}, // 406: bne #0x4006d18
                    {}, // 407: b #0x4006d28
                    {42}, // 408: mov r0, r4
                    {42, 40}, // 409: pop.w {r3, r4, r5, r6, r7, r8, r9, pc}
                    {52}, // 410: cmp r0, #0
                    {34}, // 411: bne.w #0x40074e6
                    {42}, // 412: mov r8, r4
                    {52}, // 413: mov r3, r0
                    {40}, // 414: mov r4, r5
                    {13}, // 415: ldr.w r10, [sp, #0x54]
                    {13}, // 416: ldr r6, [sp, #0x58]
                    {13}, // 417: ldr r5, [sp, #0x98]
                    {13}, // 418: ldr.w r9, [sp, #0xa4]
                    {13}, // 419: ldr.w r11, [sp, #0x5c]
                    {}, // 420: b #0x4006f60
                    {13}, // 421: ldr r2, [sp, #0xac]
                    {50}, // 422: cmp r2, #0
                    {44}, // 423: beq.w #0x40070da
                    {5}, // 424: ldrb r2, [r3, #0xc]
                    {59}, // 425: lsrs r2, r2, #4
                    {9}, // 426: cmp r2, #2
                    {7}, // 427: beq #0x4006f82
                    {13}, // 428: ldr r2, [sp, #0x40]
                    {17}, // 429: ldr r2, [r2, #0x34]
                    {19}, // 430: cmp r2, #0
                    {56}, // 431: bne.w #0x400735c
                    {13}, // 432: ldr r2, [sp, #0x90]
                    {}, // 433: movs r0, #1
                    {45, 5}, // 434: str r3, [r2]
                    {45, 53}, // 435: str r4, [r2, #4]
                    {}, // 436: add sp, #0x6c
                    {38, 31}, // 437: pop.w {r4, r5, r6, r7, r8, r9, r10, r11, pc}
                    {12}, // 438: subs r2, r0, #0
                    {54}, // 439: ble #0x400763a
                    {3}, // 440: ldr r3, [r7, #0x28]
                    {20, 3}, // 441: str.w r4, [r7, #0x8c]
                    {21}, // 442: mov r4, r8
                    {3}, // 443: ldr.w r8, [r7, #0x8c]
                    {10}, // 444: cmp r3, #0
                    {37}, // 445: beq.w #0x40078f8
                    {32}, // 446: ldr.w r3, [r10]
                    {22}, // 447: cbz r3, #0x40076b0
                    {22}, // 448: ldrb r2, [r3, #0xd]
                    {2}, // 449: and r2, r2, #3
                    {29}, // 450: cmp r2, #3
                    {1}, // 451: beq #0x40076ec
                    {3}, // 452: ldr r4, [r7, #0x2c]
                    {}, // 453: movs r5, #0
                    {35}, // 454: ldrb.w r3, [r4, #0x194]
                    {28}, // 455: and r3, r3, #3
                    {30}, // 456: cmp r3, #2
                    {33}, // 457: beq #0x40077a8
                    {35}, // 458: mov r0, r4
                    {57}, // 459: ldr.w r3, [r0, #0x1fc]
                    {4}, // 460: cmp r3, #0
                    {14}, // 461: beq.w #0x4007a54
                    {}, // 462: ldr.w r3, [pc, #0xa54]
                    {}, // 463: movw r2, #0x804
                    {}, // 464: add r3, pc
                    {36}, // 465: ldr r3, [r3]
                    {48, 16}, // 466: tst r3, r2
                    {42}, // 467: bne.w #0x400791e
                    {3}, // 468: ldr r1, [r7, #0x28]
                    {26, 32}, // 469: str.w r1, [r10]
                    {}, // 470: adds r7, #0x5c
                    {55}, // 471: mov sp, r7
                    {18, 31}, // 472: pop.w {r4, r5, r6, r7, r8, r9, r10, r11, pc}
                    {55}, // 473: ldr r3, [r7, #0x54]
                    {47, 31}, // 474: str.w r3, [r6, #0x224]
                    {57}, // 475: mov r2, r0
                    {57, 31}, // 476: str.w r0, [r6, #0x220]
                    {34}, // 477: cmp r2, #0
                    {50}, // 478: beq.w #0x4008e00
                    {34}, // 479: ldr r0, [r2]
                    {47}, // 480: ldr r1, [r3, #4]
                    {59, 44}, // 481: adds r1, r0, r1
                    {0, 55}, // 482: str r1, [r7, #0x3c]
                    {47}, // 483: ldrb r1, [r3, #0xc]
                    {17}, // 484: and r1, r1, #0xf
                    {7}, // 485: cmp r1, #0xa
                    {19}, // 486: beq.w #0x4009094
                    {18}, // 487: cmp r5, #0x12
                    {52}, // 488: beq.w #0x400907a
                    {52}, // 489: bhi #0x4008d1a
                    {18}, // 490: cmp r5, #0x16
                    {56}, // 491: bhi.w #0x4008e42
                    {18}, // 492: cmp r5, #0x15
                    {13}, // 493: bhs.w #0x4009024
                    {55}, // 494: ldr r3, [r7, #0x3c]
                    {32}, // 495: add.w r10, r10, #8
                    {53, 6}, // 496: str.w r3, [r9, r8]
                    {55}, // 497: ldr r3, [r7, #0x2c]
                    {11, 45}, // 498: cmp r3, r10
                    {5}, // 499: bhi.w #0x4008c0a
            };
            final int[][] outputPregs = new int[][]{{17}, // 0: cmp r3, #1
                    {}, // 1: bne.w #0x4006e74
                    {18}, // 2: mov.w r8, #1
                    {19}, // 3: ldr r3, [sp, #0x9c]
                    {20}, // 4: cmp r3, #0
                    {}, // 5: ite eq
                    {21}, // 6: moveq r3, #0
                    {22}, // 7: andne r3, r8, #1
                    {}, // 8: cbz r3, #0x4006e98
                    {23}, // 9: ldr r3, [sp, #0x9c]
                    {24}, // 10: ldr r0, [r3, #0xc]
                    {}, // 11: cbz r0, #0x4006e98
                    {26, 25}, // 12: adds r5, #1
                    {27}, // 13: cmp r11, r5
                    {}, // 14: bhi #0x4006df6
                    {28}, // 15: ldr r3, [r6], #4
                    {29}, // 16: ldr r4, [r3, #0x14]
                    {30}, // 17: cmp r4, r9
                    {}, // 18: beq #0x4006e98
                    {32, 31}, // 19: ands r10, r7, #2
                    {}, // 20: beq #0x4006e0e
                    {33}, // 21: ldrb.w r3, [r4, #0x195]
                    {35, 34}, // 22: lsls r2, r3, #0x1a
                    {}, // 23: bmi #0x4006e98
                    {36}, // 24: ldr r3, [sp, #0x20]
                    {37}, // 25: ldr r3, [r3]
                    {39, 38}, // 26: lsls r3, r3, #0x1c
                    {}, // 27: bmi #0x4006ea6
                    {40}, // 28: ldr.w r1, [r4, #0x178]
                    {41}, // 29: cmp r1, #0
                    {}, // 30: beq #0x4006e98
                    {42}, // 31: ldr r2, [r4, #0x38]
                    {43, 44}, // 32: movs r0, #0
                    {45}, // 33: ldr r3, [r4, #0x34]
                    {46}, // 34: ldr.w lr, [r4, #0x184]
                    {47}, // 35: ldr r2, [r2, #4]
                    {48}, // 36: ldr r3, [r3, #4]
                    {}, // 37: str r0, [sp, #0x60]
                    {}, // 38: str r2, [sp, #0x38]
                    {}, // 39: str r3, [sp, #0x24]
                    {}, // 40: str r0, [sp, #0x64]
                    {49}, // 41: cmp.w lr, #0
                    {}, // 42: beq #0x4006ed2
                    {50}, // 43: ldr.w r3, [r4, #0x180]
                    {51}, // 44: ldr.w r0, [r4, #0x17c]
                    {52}, // 45: ldr r2, [sp, #0x28]
                    {}, // 46: str r3, [sp, #0x2c]
                    {53}, // 47: ldr r3, [sp, #0x30]
                    {54}, // 48: and r12, r2, #0x1f
                    {55, 56}, // 49: ands r0, r3
                    {57}, // 50: ldr r3, [sp, #0x2c]
                    {58}, // 51: ldr.w r0, [lr, r0, lsl #2]
                    {59}, // 52: lsr.w r3, r2, r3
                    {16}, // 53: and r3, r3, #0x1f
                    {8}, // 54: mov r2, r12
                    {3}, // 55: lsr.w r3, r0, r3
                    {17, 19}, // 56: lsrs r0, r2
                    {22, 21}, // 57: ands r3, r0
                    {0, 5}, // 58: lsls r0, r3, #0x1f
                    {}, // 59: bmi.w #0x4007180
                    {20}, // 60: mov.w r8, #1
                    {26}, // 61: ldr r3, [sp, #0x9c]
                    {23}, // 62: cmp r3, #0
                    {}, // 63: ite eq
                    {4}, // 64: moveq r3, #0
                    {27}, // 65: andne r3, r8, #1
                    {}, // 66: cbz r3, #0x4006e98
                    {10}, // 67: ldr r3, [sp, #0x9c]
                    {30}, // 68: ldr r0, [r3, #0xc]
                    {}, // 69: cbz r0, #0x4006e98
                    {2, 28}, // 70: adds r5, #1
                    {32}, // 71: cmp r11, r5
                    {}, // 72: bhi #0x4006df6
                    {33}, // 73: ldr r3, [r6], #4
                    {36}, // 74: ldr r4, [r3, #0x14]
                    {37}, // 75: cmp r4, r9
                    {}, // 76: beq #0x4006e98
                    {1, 35}, // 77: ands r10, r7, #2
                    {}, // 78: beq #0x4006e0e
                    {39}, // 79: ldrb.w r3, [r4, #0x195]
                    {24, 34}, // 80: lsls r2, r3, #0x1a
                    {}, // 81: bmi #0x4006e98
                    {41}, // 82: ldr r3, [sp, #0x20]
                    {38}, // 83: ldr r3, [r3]
                    {42, 14}, // 84: lsls r3, r3, #0x1c
                    {}, // 85: bmi #0x4006ea6
                    {45}, // 86: ldr.w r1, [r4, #0x178]
                    {44}, // 87: cmp r1, #0
                    {}, // 88: beq #0x4006e98
                    {48}, // 89: ldr r2, [r4, #0x38]
                    {43, 47}, // 90: movs r0, #0
                    {50}, // 91: ldr r3, [r4, #0x34]
                    {12}, // 92: ldr.w lr, [r4, #0x184]
                    {51}, // 93: ldr r2, [r2, #4]
                    {49}, // 94: ldr r3, [r3, #4]
                    {}, // 95: str r0, [sp, #0x60]
                    {}, // 96: str r2, [sp, #0x38]
                    {}, // 97: str r3, [sp, #0x24]
                    {}, // 98: str r0, [sp, #0x64]
                    {53}, // 99: cmp.w lr, #0
                    {}, // 100: beq #0x4006ed2
                    {55}, // 101: ldr.w r3, [r4, #0x180]
                    {57}, // 102: ldr.w r0, [r4, #0x17c]
                    {59}, // 103: ldr r2, [sp, #0x28]
                    {}, // 104: str r3, [sp, #0x2c]
                    {52}, // 105: ldr r3, [sp, #0x30]
                    {16}, // 106: and r12, r2, #0x1f
                    {58, 56}, // 107: ands r0, r3
                    {3}, // 108: ldr r3, [sp, #0x2c]
                    {19}, // 109: ldr.w r0, [lr, r0, lsl #2]
                    {17}, // 110: lsr.w r3, r2, r3
                    {22}, // 111: and r3, r3, #0x1f
                    {18}, // 112: mov r2, r12
                    {21}, // 113: lsr.w r3, r0, r3
                    {5, 26}, // 114: lsrs r0, r2
                    {27, 4}, // 115: ands r3, r0
                    {0, 25}, // 116: lsls r0, r3, #0x1f
                    {}, // 117: bmi.w #0x4007180
                    {23}, // 118: ldr.w r8, [sp, #0x28]
                    {2}, // 119: mov r0, r8
                    {10}, // 120: bl #0x40135a4
                    {29}, // 121: cmp r1, #0
                    {}, // 122: beq #0x401359a
                    {}, // 123: push.w {r0, r1, lr}
                    {32}, // 124: bl #0x4013348
                    {37, 31}, // 125: subs r2, r1, #1
                    {}, // 126: it eq
                    {}, // 127: bxeq lr
                    {}, // 128: blo.w #0x401359a
                    {33}, // 129: cmp r0, r1
                    {}, // 130: bls.w #0x4013584
                    {8}, // 131: tst r1, r2
                    {}, // 132: beq.w #0x401358c
                    {1}, // 133: clz r3, r0
                    {39}, // 134: clz r2, r1
                    {41}, // 135: sub.w r3, r2, r3
                    {38}, // 136: rsb.w r3, r3, #0x1f
                    {24}, // 137: adr r2, #0x10
                    {40}, // 138: add.w r3, r2, r3, lsl #4
                    {42}, // 139: mov.w r2, #0
                    {}, // 140: mov pc, r3
                    {34}, // 141: cmp.w r0, r1, lsl #18
                    {}, // 142: nop
                    {30}, // 143: adc.w r2, r2, r2
                    {}, // 144: it hs
                    {44}, // 145: subhs.w r0, r0, r1, lsl #18
                    {14}, // 146: cmp.w r0, r1, lsl #17
                    {}, // 147: nop
                    {46}, // 148: adc.w r2, r2, r2
                    {}, // 149: it hs
                    {48}, // 150: subhs.w r0, r0, r1, lsl #17
                    {50}, // 151: cmp.w r0, r1, lsl #16
                    {}, // 152: nop
                    {47}, // 153: adc.w r2, r2, r2
                    {}, // 154: it hs
                    {49}, // 155: subhs.w r0, r0, r1, lsl #16
                    {43}, // 156: cmp.w r0, r1, lsl #15
                    {}, // 157: nop
                    {51}, // 158: adc.w r2, r2, r2
                    {}, // 159: it hs
                    {55}, // 160: subhs.w r0, r0, r1, lsl #15
                    {54}, // 161: cmp.w r0, r1, lsl #14
                    {}, // 162: nop
                    {57}, // 163: adc.w r2, r2, r2
                    {}, // 164: it hs
                    {53}, // 165: subhs.w r0, r0, r1, lsl #14
                    {52}, // 166: cmp.w r0, r1, lsl #13
                    {}, // 167: nop
                    {58}, // 168: adc.w r2, r2, r2
                    {}, // 169: it hs
                    {3}, // 170: subhs.w r0, r0, r1, lsl #13
                    {17}, // 171: cmp.w r0, r1, lsl #12
                    {}, // 172: nop
                    {59}, // 173: adc.w r2, r2, r2
                    {}, // 174: it hs
                    {22}, // 175: subhs.w r0, r0, r1, lsl #12
                    {19}, // 176: cmp.w r0, r1, lsl #11
                    {}, // 177: nop
                    {56}, // 178: adc.w r2, r2, r2
                    {}, // 179: it hs
                    {21}, // 180: subhs.w r0, r0, r1, lsl #11
                    {26}, // 181: cmp.w r0, r1, lsl #10
                    {}, // 182: nop
                    {5}, // 183: adc.w r2, r2, r2
                    {}, // 184: it hs
                    {27}, // 185: subhs.w r0, r0, r1, lsl #10
                    {20}, // 186: cmp.w r0, r1, lsl #9
                    {}, // 187: nop
                    {0}, // 188: adc.w r2, r2, r2
                    {}, // 189: it hs
                    {12}, // 190: subhs.w r0, r0, r1, lsl #9
                    {25}, // 191: cmp.w r0, r1, lsl #8
                    {}, // 192: nop
                    {10}, // 193: adc.w r2, r2, r2
                    {}, // 194: it hs
                    {18}, // 195: subhs.w r0, r0, r1, lsl #8
                    {29}, // 196: cmp.w r0, r1, lsl #7
                    {}, // 197: nop
                    {37}, // 198: adc.w r2, r2, r2
                    {}, // 199: it hs
                    {33}, // 200: subhs.w r0, r0, r1, lsl #7
                    {4}, // 201: cmp.w r0, r1, lsl #6
                    {}, // 202: nop
                    {31}, // 203: adc.w r2, r2, r2
                    {}, // 204: it hs
                    {1}, // 205: subhs.w r0, r0, r1, lsl #6
                    {41}, // 206: cmp.w r0, r1, lsl #5
                    {}, // 207: nop
                    {39}, // 208: adc.w r2, r2, r2
                    {}, // 209: it hs
                    {38}, // 210: subhs.w r0, r0, r1, lsl #5
                    {24}, // 211: cmp.w r0, r1, lsl #4
                    {}, // 212: nop
                    {8}, // 213: adc.w r2, r2, r2
                    {}, // 214: it hs
                    {42}, // 215: subhs.w r0, r0, r1, lsl #4
                    {2}, // 216: cmp.w r0, r1, lsl #3
                    {}, // 217: nop
                    {34}, // 218: adc.w r2, r2, r2
                    {}, // 219: it hs
                    {30}, // 220: subhs.w r0, r0, r1, lsl #3
                    {44}, // 221: cmp.w r0, r1, lsl #2
                    {}, // 222: nop
                    {14}, // 223: adc.w r2, r2, r2
                    {}, // 224: it hs
                    {46}, // 225: subhs.w r0, r0, r1, lsl #2
                    {48}, // 226: cmp.w r0, r1, lsl #1
                    {}, // 227: nop
                    {50}, // 228: adc.w r2, r2, r2
                    {}, // 229: it hs
                    {47}, // 230: subhs.w r0, r0, r1, lsl #1
                    {49}, // 231: cmp.w r0, r1
                    {}, // 232: nop
                    {43}, // 233: adc.w r2, r2, r2
                    {}, // 234: it hs
                    {51}, // 235: subhs.w r0, r0, r1
                    {55}, // 236: mov r0, r2
                    {}, // 237: bx lr
                    {54}, // 238: pop.w {r1, r2, lr}
                    {57}, // 239: mul r3, r2, r0
                    {53}, // 240: sub.w r1, r1, r3
                    {}, // 241: bx lr
                    {52}, // 242: ldr.w r3, [r4, #0x188]
                    {58}, // 243: ldr.w r3, [r3, r1, lsl #2]
                    {3}, // 244: cmp r3, #0
                    {}, // 245: beq.w #0x4006e74
                    {17}, // 246: ldr.w r2, [r4, #0x18c]
                    {59}, // 247: add r1, sp, #0x60
                    {}, // 248: str.w r10, [sp, #0x54]
                    {22}, // 249: add.w r3, r2, r3, lsl #2
                    {}, // 250: str r1, [sp, #0x2c]
                    {}, // 251: str r6, [sp, #0x58]
                    {19}, // 252: add r1, sp, #0x64
                    {}, // 253: str r5, [sp, #0x98]
                    {56}, // 254: mov r6, r3
                    {}, // 255: str.w r9, [sp, #0xa4]
                    {21}, // 256: mov r5, r4
                    {}, // 257: str.w r11, [sp, #0x5c]
                    {}, // 258: str r1, [sp, #0x50]
                    {26}, // 259: ldr.w r10, [sp, #0x3c]
                    {5}, // 260: ldr.w r9, [sp, #0x38]
                    {27}, // 261: ldr.w r11, [sp, #0x34]
                    {}, // 262: b #0x40071d4
                    {20}, // 263: ldr r3, [r6]
                    {0}, // 264: eor.w r2, r8, r3
                    {25, 12}, // 265: lsrs r2, r2, #1
                    {}, // 266: bne #0x40071ca
                    {18, 10}, // 267: lsls r1, r3, #0x1f
                    {29}, // 268: add.w r6, r6, #4
                    {}, // 269: bmi.w #0x4007382
                    {37}, // 270: ldr r3, [r6]
                    {33}, // 271: eor.w r2, r8, r3
                    {31, 4}, // 272: lsrs r2, r2, #1
                    {}, // 273: bne #0x40071ca
                    {41, 1}, // 274: lsls r1, r3, #0x1f
                    {39}, // 275: add.w r6, r6, #4
                    {}, // 276: bmi.w #0x4007382
                    {38}, // 277: ldr r3, [r6]
                    {24}, // 278: eor.w r2, r8, r3
                    {42, 8}, // 279: lsrs r2, r2, #1
                    {}, // 280: bne #0x40071ca
                    {2}, // 281: ldr.w r4, [r5, #0x18c]
                    {34}, // 282: ldr r2, [sp, #0x2c]
                    {44, 30}, // 283: subs r4, r6, r4
                    {14}, // 284: ldr r1, [sp, #0x50]
                    {46}, // 285: ldr r0, [sp, #0x24]
                    {50, 48}, // 286: asrs r4, r4, #2
                    {}, // 287: str r2, [sp, #0x18]
                    {}, // 288: str r1, [sp, #0x14]
                    {47}, // 289: mov r1, r10
                    {}, // 290: str r0, [sp, #0xc]
                    {51}, // 291: add.w lr, r9, r4, lsl #4
                    {45}, // 292: ldr r3, [sp, #0xa0]
                    {40}, // 293: mov r0, r11
                    {54}, // 294: ldr r2, [sp, #0x9c]
                    {}, // 295: str r5, [sp, #0x10]
                    {}, // 296: str r4, [sp, #8]
                    {}, // 297: str r7, [sp]
                    {}, // 298: str.w lr, [sp, #4]
                    {57}, // 299: bl #0x4006c98
                    {}, // 300: push.w {r3, r4, r5, r6, r7, r8, r9, lr}
                    {52}, // 301: ldr r4, [sp, #0x24]
                    {49}, // 302: ldr.w r9, [sp, #0x30]
                    {43}, // 303: ldrb r5, [r4, #0xc]
                    {53}, // 304: ldr r6, [r4, #4]
                    {58}, // 305: and r5, r5, #0xf
                    {59}, // 306: clz r6, r6
                    {6}, // 307: cmp r5, #6
                    {28}, // 308: lsr.w r6, r6, #5
                    {}, // 309: it eq
                    {35}, // 310: moveq r6, #0
                    {9}, // 311: cmp r6, #0
                    {}, // 312: bne #0x4006d5a
                    {11}, // 313: ldrh r6, [r4, #0xe]
                    {22}, // 314: ldr r7, [sp, #0x20]
                    {17}, // 315: cmp r6, #0
                    {}, // 316: ite ne
                    {0}, // 317: movne r7, #0
                    {3}, // 318: andeq r7, r7, #1
                    {19}, // 319: cmp r7, #0
                    {}, // 320: bne #0x4006d5a
                    {25}, // 321: movw r6, #0x467
                    {56}, // 322: asr.w r5, r6, r5
                    {12, 20}, // 323: lsls r5, r5, #0x1f
                    {}, // 324: bpl #0x4006d5a
                    {33}, // 325: mov r6, r3
                    {18}, // 326: mov r3, r1
                    {10}, // 327: cmp r4, r3
                    {31}, // 328: mov r5, r2
                    {29}, // 329: mov r1, r0
                    {}, // 330: beq #0x4006cf4
                    {37}, // 331: ldr.w r3, [r9, #0x1a4]
                    {}, // 332: cbz r5, #0x4006d2e
                    {4}, // 333: cmp r3, #0
                    {}, // 334: beq #0x4006d6e
                    {24}, // 335: ldr r2, [sp, #0x28]
                    {41}, // 336: ldr r1, [r5, #4]
                    {36}, // 337: ldrh.w r8, [r3, r2, lsl #1]
                    {8}, // 338: ldr.w r2, [r9, #0x170]
                    {2}, // 339: ubfx r3, r8, #0, #0xf
                    {1, 42}, // 340: lsls r3, r3, #4
                    {55, 30}, // 341: adds r0, r2, r3
                    {44}, // 342: ldr r6, [r0, #4]
                    {14}, // 343: cmp r6, r1
                    {}, // 344: beq #0x4006d60
                    {32}, // 345: ldr r0, [r2, r3]
                    {38}, // 346: ldr r1, [r5]
                    {46}, // 347: bl #0x4012090
                    {34}, // 348: ldrb r2, [r0]
                    {51}, // 349: ldrb r3, [r1]
                    {48}, // 350: cmp r2, #1
                    {}, // 351: it hs
                    {5}, // 352: cmphs r2, r3
                    {}, // 353: bne #0x4012080
                    {}, // 354: strd r4, r5, [sp, #-0x10]!
                    {21}, // 355: orr.w r4, r0, r1
                    {}, // 356: strd r6, r7, [sp, #8]
                    {39}, // 357: mvn r12, #0
                    {43}, // 358: lsl.w r2, r4, #0x1d
                    {}, // 359: cbz r2, #0x4012100
                    {53}, // 360: eor.w r4, r0, r1
                    {50}, // 361: tst.w r4, #7
                    {}, // 362: bne #0x401219a
                    {59}, // 363: and r4, r0, #7
                    {28}, // 364: bic r0, r0, #7
                    {6}, // 365: and r5, r4, #3
                    {35}, // 366: bic r1, r1, #7
                    {7}, // 367: lsl.w r5, r5, #3
                    {9}, // 368: ldrd r2, r3, [r0], #0x10
                    {22}, // 369: tst.w r4, #4
                    {0}, // 370: ldrd r6, r7, [r1], #0x10
                    {17}, // 371: lsl.w r4, r12, r5
                    {11}, // 372: orn r2, r2, r4
                    {58}, // 373: orn r6, r6, r4
                    {}, // 374: beq #0x4012108
                    {56}, // 375: uadd8 r5, r2, r12
                    {19}, // 376: eor.w r4, r2, r6
                    {25}, // 377: sel r4, r4, r12
                    {}, // 378: cbnz r4, #0x4012172
                    {45}, // 379: uadd8 r5, r3, r12
                    {12}, // 380: eor.w r5, r3, r7
                    {20}, // 381: sel r5, r5, r12
                    {}, // 382: cbnz r5, #0x401214a
                    {47}, // 383: ldrd r2, r3, [r0, #-0x8]
                    {18}, // 384: ldrd r6, r7, [r1, #-0x8]
                    {10}, // 385: uadd8 r5, r2, r12
                    {54}, // 386: eor.w r4, r2, r6
                    {29}, // 387: sel r4, r4, r12
                    {23}, // 388: uadd8 r5, r3, r12
                    {24}, // 389: eor.w r5, r3, r7
                    {37}, // 390: sel r5, r5, r12
                    {4, 2}, // 391: orrs r5, r4
                    {}, // 392: beq #0x4012100
                    {}, // 393: cbnz r4, #0x4012172
                    {40}, // 394: rev r5, r5
                    {1}, // 395: clz r4, r5
                    {33}, // 396: bic r4, r4, #7
                    {30}, // 397: lsr.w r1, r7, r4
                    {55}, // 398: ldrd r6, r7, [sp, #8]
                    {41}, // 399: lsr.w r3, r3, r4
                    {57}, // 400: and r0, r3, #0xff
                    {8}, // 401: and r1, r1, #0xff
                    {42}, // 402: ldrd r4, r5, [sp], #0x10
                    {14}, // 403: sub.w r0, r0, r1
                    {}, // 404: bx lr
                    {48}, // 405: cmp r0, #0
                    {}, // 406: bne #0x4006d18
                    {}, // 407: b #0x4006d28
                    {52}, // 408: mov r0, r4
                    {16}, // 409: pop.w {r3, r4, r5, r6, r7, r8, r9, pc}
                    {34}, // 410: cmp r0, #0
                    {}, // 411: bne.w #0x40074e6
                    {21}, // 412: mov r8, r4
                    {5}, // 413: mov r3, r0
                    {53}, // 414: mov r4, r5
                    {32}, // 415: ldr.w r10, [sp, #0x54]
                    {31}, // 416: ldr r6, [sp, #0x58]
                    {38}, // 417: ldr r5, [sp, #0x98]
                    {6}, // 418: ldr.w r9, [sp, #0xa4]
                    {43}, // 419: ldr.w r11, [sp, #0x5c]
                    {}, // 420: b #0x4006f60
                    {50}, // 421: ldr r2, [sp, #0xac]
                    {44}, // 422: cmp r2, #0
                    {}, // 423: beq.w #0x40070da
                    {59}, // 424: ldrb r2, [r3, #0xc]
                    {0, 9}, // 425: lsrs r2, r2, #4
                    {7}, // 426: cmp r2, #2
                    {}, // 427: beq #0x4006f82
                    {17}, // 428: ldr r2, [sp, #0x40]
                    {19}, // 429: ldr r2, [r2, #0x34]
                    {56}, // 430: cmp r2, #0
                    {}, // 431: bne.w #0x400735c
                    {45}, // 432: ldr r2, [sp, #0x90]
                    {12, 11}, // 433: movs r0, #1
                    {}, // 434: str r3, [r2]
                    {}, // 435: str r4, [r2, #4]
                    {58}, // 436: add sp, #0x6c
                    {20}, // 437: pop.w {r4, r5, r6, r7, r8, r9, r10, r11, pc}
                    {54, 25}, // 438: subs r2, r0, #0
                    {}, // 439: ble #0x400763a
                    {10}, // 440: ldr r3, [r7, #0x28]
                    {}, // 441: str.w r4, [r7, #0x8c]
                    {23}, // 442: mov r4, r8
                    {24}, // 443: ldr.w r8, [r7, #0x8c]
                    {37}, // 444: cmp r3, #0
                    {}, // 445: beq.w #0x40078f8
                    {22}, // 446: ldr.w r3, [r10]
                    {}, // 447: cbz r3, #0x40076b0
                    {2}, // 448: ldrb r2, [r3, #0xd]
                    {29}, // 449: and r2, r2, #3
                    {1}, // 450: cmp r2, #3
                    {}, // 451: beq #0x40076ec
                    {35}, // 452: ldr r4, [r7, #0x2c]
                    {51, 18}, // 453: movs r5, #0
                    {28}, // 454: ldrb.w r3, [r4, #0x194]
                    {30}, // 455: and r3, r3, #3
                    {33}, // 456: cmp r3, #2
                    {}, // 457: beq #0x40077a8
                    {57}, // 458: mov r0, r4
                    {4}, // 459: ldr.w r3, [r0, #0x1fc]
                    {14}, // 460: cmp r3, #0
                    {}, // 461: beq.w #0x4007a54
                    {41}, // 462: ldr.w r3, [pc, #0xa54]
                    {48}, // 463: movw r2, #0x804
                    {36}, // 464: add r3, pc
                    {16}, // 465: ldr r3, [r3]
                    {42}, // 466: tst r3, r2
                    {}, // 467: bne.w #0x400791e
                    {26}, // 468: ldr r1, [r7, #0x28]
                    {}, // 469: str.w r1, [r10]
                    {40, 55}, // 470: adds r7, #0x5c
                    {49}, // 471: mov sp, r7
                    {27}, // 472: pop.w {r4, r5, r6, r7, r8, r9, r10, r11, pc}
                    {47}, // 473: ldr r3, [r7, #0x54]
                    {}, // 474: str.w r3, [r6, #0x224]
                    {34}, // 475: mov r2, r0
                    {}, // 476: str.w r0, [r6, #0x220]
                    {50}, // 477: cmp r2, #0
                    {}, // 478: beq.w #0x4008e00
                    {59}, // 479: ldr r0, [r2]
                    {44}, // 480: ldr r1, [r3, #4]
                    {9, 0}, // 481: adds r1, r0, r1
                    {}, // 482: str r1, [r7, #0x3c]
                    {17}, // 483: ldrb r1, [r3, #0xc]
                    {7}, // 484: and r1, r1, #0xf
                    {19}, // 485: cmp r1, #0xa
                    {}, // 486: beq.w #0x4009094
                    {52}, // 487: cmp r5, #0x12
                    {}, // 488: beq.w #0x400907a
                    {}, // 489: bhi #0x4008d1a
                    {56}, // 490: cmp r5, #0x16
                    {}, // 491: bhi.w #0x4008e42
                    {13}, // 492: cmp r5, #0x15
                    {}, // 493: bhs.w #0x4009024
                    {53}, // 494: ldr r3, [r7, #0x3c]
                    {45}, // 495: add.w r10, r10, #8
                    {}, // 496: str.w r3, [r9, r8]
                    {11}, // 497: ldr r3, [r7, #0x2c]
                    {5}, // 498: cmp r3, r10
                    {}, // 499: bhi.w #0x4008c0a
            };

            // TODO: update this path
            String traceFile = "path/to/your/streamcluster-10M-v1.trace.gz";
            Map<Short, PhysReg> actualInPregs = new HashMap<>();
            Map<Short, PhysReg> actualOutPregs = new HashMap<>();
            int inum = 0;
            for (Insn i : new InsnIterator(traceFile, inputPregs.length)) {
                actualInPregs.clear();
                actualOutPregs.clear();
                rr.rename(i, actualInPregs, actualOutPregs);

                // check input pregs size
                assertEquals("insn " + inum + " has a different number of input pregs",
                        inputPregs[inum].length, actualInPregs.size());
                // check input pregs contents
                for (PhysReg pr : actualInPregs.values()) {
                    assertTrue("insn " + inum + " has different input pregs", arrayContains(inputPregs[inum], pr.get()));
                }

                // check output pregs size
                assertEquals("insn " + inum + " has a different number of output pregs",
                        outputPregs[inum].length, actualOutPregs.size());
                // check output pregs contents
                for (PhysReg pr : actualOutPregs.values()) {
                    assertTrue("insn " + inum + " has different output pregs", arrayContains(outputPregs[inum], pr.get()));
                }

                inum++;
            }
        }

        private boolean arrayContains(int[] haystack, int needle) {
            for (int i : haystack) {
                if (i == needle) return true;
            }
            return false;
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
