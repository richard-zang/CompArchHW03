package cis501.submission;

import cis501.*;

import java.io.IOException;

public class TraceRunner {

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
        runPipe(0, insnLimit, args[0]);
        runPipe(1, insnLimit, args[0]);
        runPipe(2, insnLimit, args[0]);
        runPipe(3, insnLimit, args[0]);
        runPipe(4, insnLimit, args[0]);
        runPipe(5, insnLimit, args[0]);
    }

    public static void runPipe(int latency,int insnLimit, String fileName){
        InorderPipeline pipe = new InorderPipeline(latency, Bypass.FULL_BYPASS);
        InsnIterator uiter = new InsnIterator(fileName, insnLimit);
        pipe.run(uiter);
        System.out.println("Cycles [" + latency + "]: " + pipe.getCycles());
        System.out.println("Insn [" + latency + "]: " + pipe.getInsns());
        float ipc = pipe.getCycles() /(float) pipe.getInsns();
        System.out.println("IPC [" + latency + "]: " + ipc);
    }
}


