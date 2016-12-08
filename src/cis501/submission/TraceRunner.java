package cis501.submission;

import cis501.*;

import java.io.IOException;

public class TraceRunner{

    public static void main(String[] args) throws IOException {
        final int insnLimit;

        switch (args.length) {
        case 1:
            insnLimit = -1; // by default, run on entire trace
            break;
        case 2: // use user-provided limit
            insnLimit = Integer.parseInt(args[1]);
            break;
        default:
            System.err.println("Usage: path/to/trace-file [insn-limit]");
            return;
        }

        int[] levels = {1, 2, 4, 8, 16};
        /* for associativity levels 1, 2, 4, 8 ,16... */
        for(int way : levels){
            /* For cache sizes */
            for(int j = 9; j <= 18; j++){
                int cacheSize = 2 << (j - 1);
                System.out.println("associativity\t" + way);
                System.out.println("cache size\t" + cacheSize);

                IBranchTargetBuffer btb = new BranchTargetBuffer(18);
                IDirectionPredictor gshare = new DirPredGshare(18, 18);

                /* Compute indexBits. */
                BranchPredictor bp = new BranchPredictor(gshare, btb);
                double log10bits = Math.log(cacheSize / (32 * way));
                double indexBits = log10bits / Math.log(2);
                System.out.println("Index bits: " + indexBits);
                int blockBits = 5; //We want a fixed 32 byte blocks.

                Cache cacheI = new Cache((int)indexBits, way, blockBits, 0, 2, 3);
                Cache cacheD = new Cache((int)indexBits, way, blockBits, 0, 2, 3);
                IInorderPipeline pipe = new InorderPipeline(bp, cacheI, cacheD);

                /* Run pipe! */
                InsnIterator uiter = new InsnIterator(args[0], -1);
                pipe.run(uiter);
                System.out.println("Insn\t" + "\t" + pipe.getInsns());
                System.out.println("Cycles\t" + "\t" + pipe.getCycles());
                float ipc =  pipe.getInsns() / (float) pipe.getCycles();
                System.out.println("IPC\t" + "\t" + ipc);

            }
        }

        return;
    }
}


