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

        /*        for(int i = 4; i <= 18; i++){
            IDirectionPredictor bimodal = new DirPredBimodal(i);
            runPrediction(args[0], bimodal, i, "bimodal");
        }
        for(int i = 4; i <= 18; i++){
             IDirectionPredictor gshare = new DirPredGshare(i, i);
            runPrediction(args[0], gshare, i, "gshare");
            }*/

        for(int i = 4; i <= 18; i++){
            IDirectionPredictor bimodal = new DirPredBimodal(i - 2);
            IDirectionPredictor gshare = new DirPredGshare(i - 1 , i - 1);
            IDirectionPredictor tourn = new DirPredTournament(i - 2, bimodal, gshare);
            runPrediction(args[0], tourn, i, "tournament");
        }
    }

    public static void runPrediction(String fileName, IDirectionPredictor p, int i,
                                     String testName){
        IBranchTargetBuffer btb = new BranchTargetBuffer(i);
        InorderPipeline pipe = new InorderPipeline(1, new BranchPredictor(p, btb));
        InsnIterator uiter = new InsnIterator(fileName, -1);
        pipe.run(uiter);
        System.out.println("Insn\t" + i + "\t" + pipe.getInsns());
        System.out.println("Cycles\t" + i + "\t" + pipe.getCycles());
        float ipc =  pipe.getInsns() / (float) pipe.getCycles();
        System.out.println("IPC\t" + i + "\t" + ipc);
    }
}


