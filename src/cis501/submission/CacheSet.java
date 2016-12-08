package cis501.submission;

import cis501.ICache;
import java.util.*;

public class CacheSet{
    private int ways;
    private long[] tag;
    protected long[] lru;
    private boolean[] valid;
    protected boolean[] dirty;
    private int nonTagBits;

    protected int hitLatency;
    protected int cleanMissLatency;
    protected int dirtyMissLatency;
    //==========================================================================
    public CacheSet(int ways, int blockOffsetBits, int indexBits,
                    int hitLatency, int cleanMissLatency, int dirtyMissLatency){
        this.ways = ways;
        tag = new long[ways];
        valid = new boolean[ways];
        dirty = new boolean[ways];
        lru = new long[ways];
        nonTagBits = blockOffsetBits + indexBits;

        this.hitLatency = hitLatency;
        this.cleanMissLatency = cleanMissLatency;
        this.dirtyMissLatency = dirtyMissLatency;

        return;
    }
    //==========================================================================
    private long getTag(long address){
        return address >> this.nonTagBits;
    }
    //==========================================================================
    protected int findBlock(long address){
        int block = -1;
        for(int i = 0; i < ways; i++){
            if((tag[i] == this.getTag(address)) && (valid[i] == true)){
                block = i;
                break;
            }
        }
        return block;
    }
    //==========================================================================
    protected void setTag(int way, long address){
        tag[way] = this.getTag(address);
    }
    //==========================================================================
    protected void setValid(int way, boolean isValid){
        valid[way] = isValid;
    }
    //==========================================================================
    protected void setDirty(int way, boolean isDirty){
        dirty[way] = isDirty;
    }
    //==========================================================================
    protected void updateLRU(int way){
        lru[way] = 0;
        for(int i = 0; i < ways; i++){
            lru[i]++;
        }
    }
    //==========================================================================
    protected boolean containsBlock(long address){
        //If the block index != -1, it means we have the block.
        //Otherwise we do not have that block.
        return (findBlock(address) != -1);
    }
    //==========================================================================
    protected int evictIfNeeded(long address){
        //Do we need to evict?
        //If not, all is good.
        for(int i = 0; i < ways; i++){
            if(!valid[i]){
                return cleanMissLatency;
            }
        }
        //If we have gotten to this point, we need to evict.
        //Which block do we evict?
        int costOfEviction = cleanMissLatency;
        int currentOldestBlock = 0;
        for(int i = 0; i < ways; i++){
            if(lru[currentOldestBlock] < lru[i]){
                currentOldestBlock = i;
            }
        }
        if(dirty[currentOldestBlock]){
            //The cost of eviction is writing back the dirty block.
            costOfEviction = dirtyMissLatency;
        }
        valid[currentOldestBlock] = false;
        dirty[currentOldestBlock] = false;
        return costOfEviction;
    }
    //==========================================================================
    protected int emptyWay(){
        for(int i = 0; i < ways; i++){
            if(!valid[i]){
                return i;
            }
        }
        return -1;
    }
    //==========================================================================
    /**
     * Return cache latency for a load.
     */
    protected int load(long address){
        return doStoreLoad(address, false);
    }
    //==========================================================================
    /**
     * Return cache latency for a store.
     */
    protected int store(long address){
        return doStoreLoad(address, true);
    }
    //==========================================================================
    /**
     * Stores have a bit of extra logic since we may have to write back data to higher
     * caches. Otherwise they are the same.
     */
    protected int doStoreLoad(long address, boolean store){
        if(this.containsBlock(address)){
            int way = this.findBlock(address);
            if(store) this.setDirty(way, true);
            this.updateLRU(way);
            return hitLatency;
        }
        else {
            int cost = this.evictIfNeeded(address);
            int way = this.emptyWay();
            this.setTag(way, address);
            this.setValid(way, true);
            if(store) this.setDirty(way, true);
            this.updateLRU(way);
            return cost;
        }
    }
    //==========================================================================
}
