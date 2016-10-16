package cis501.submission;

import cis501.ICache;
import java.util.*;

public class Cache implements ICache {
    protected int indexBits;
    private int ways;
    protected int blockOffsetBits;
    protected int hitLatency;
    protected int cleanMissLatency;
    protected int dirtyMissLatency;
    protected int rows;

    protected CacheSet[] arrayCacheSets;
    //==========================================================================
    public Cache(int indexBits, int ways, int blockOffsetBits,
                 final int hitLatency, final int cleanMissLatency,
                 final int dirtyMissLatency) {
        checkAsserts(indexBits, ways, blockOffsetBits, hitLatency, cleanMissLatency,
                     dirtyMissLatency);

        this.indexBits = indexBits;
        this.ways = ways;
        this.blockOffsetBits = blockOffsetBits;
        this.hitLatency = hitLatency;
        this.cleanMissLatency = cleanMissLatency;
        this.dirtyMissLatency = dirtyMissLatency;
        rows = 1 << indexBits;
        arrayCacheSets = new CacheSet[rows];
        for(int i = 0; i < arrayCacheSets.length; i++){
            arrayCacheSets[i] =
                new CacheSet(ways, blockOffsetBits, indexBits,
                             hitLatency, cleanMissLatency, dirtyMissLatency);
        }
    }
    //==========================================================================
    public int getIndexBits(long address){
        int indexBitsMask = (1 << indexBits) - 1;
        return (int)(address >> blockOffsetBits) & indexBitsMask;
    }
    //==========================================================================
    @Override
    public int access(boolean load, long address) {

        //If no blocks exist, then we must always do a clean fetch.
        if(blockOffsetBits == 0){
            return cleanMissLatency;
        }
        CacheSet set = arrayCacheSets[getIndexBits(address)];
        if(load){
            return set.load(address);
        }
        else {
            return set.store(address);
        }
    }
    //==========================================================================
    public void checkAsserts(int indexBits, int ways, int blockOffsetBits,
                             final int hitLatency, final int cleanMissLatency,
                             final int dirtyMissLatency) {
        assert indexBits >= 0;
        assert indexBits <= 20;
        assert ways > 0;
        assert ways <= 128;
        assert blockOffsetBits >= 0;
        assert blockOffsetBits <= 20;
        assert indexBits + blockOffsetBits < 32;
        assert indexBits + blockOffsetBits < 64;
        assert hitLatency >= 0;
        assert cleanMissLatency >= 0;
        assert dirtyMissLatency >= 0;

        return;
    }
    //==========================================================================
}
