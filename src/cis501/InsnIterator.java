package cis501;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

public class InsnIterator implements Iterator<Insn>, Iterable<Insn> {

    private final BufferedReader reader;
    private final int LIMIT;
    private int insnsProcessed = 0;

    /**
     * @param filename The path to the compressed trace file
     * @param limit    Stop after processing this many insns. If -1, process the entire trace.
     */
    public InsnIterator(String filename, int limit) {
        if (-1 == limit) {
            LIMIT = Integer.MAX_VALUE; // no limit
        } else {
            LIMIT = limit;
        }

        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(new GZIPInputStream(new FileInputStream(filename)), "US-ASCII");
        } catch (IOException e) {
            e.printStackTrace();
        }
        reader = new BufferedReader(isr);
    }

    public boolean hasNext() {
        try {
            return insnsProcessed < LIMIT && reader.ready();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Insn next() {
        try {
            String ln = reader.readLine();
            insnsProcessed++;
            return new Insn(ln);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Iterator<Insn> iterator() {
        return this;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}
