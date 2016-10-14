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


	protected CacheSet[] arrayCacheSets;


	protected class CacheSet {
		private int ways;
		private long[] tag;
		protected int[] lru;
		private boolean[] valid;
		protected boolean[] dirty;

		public CacheSet(int ways){
			this.ways = ways;
			tag = new long[ways];
			valid = new boolean[ways];
			dirty = new boolean[ways];
			lru = new int[ways];
		}
		protected int findBlock(long address){
			int block = -1;
			for(int i = 0; i < ways; i++){
				if((tag[i] == address) && (valid[i] == true)){
					block = i;
				}
			}
			return block;
		}
		protected void setTag(int way, long address){
			tag[way] = address;
		}
		protected void setValid(int way, boolean isValid){
			valid[way] = isValid;
		}
		protected void setDirty(int way, boolean isDirty){
			dirty[way] = isDirty;
		}
		protected void updateLRU(int way){
			lru[way] = 0;
			for(int i = 0; i < ways; i++){
				lru[i]++;
			}
		}
		protected boolean containsBlock(long address){
			//If the block index != -1, it means we have the block.
			//Otherwise we do not have that block.
			return (findBlock(address) != -1);
		}
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
		protected int emptyWay(){
			for(int i = 0; i < ways; i++){
				if(!valid[i]){
					return i;
				}
			}
		return -1;
		}
	}

    public Cache(int indexBits, int ways, int blockOffsetBits,
                 final int hitLatency, final int cleanMissLatency, final int dirtyMissLatency) {
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

		this.indexBits = indexBits;
		this.ways = ways;
		this.blockOffsetBits = blockOffsetBits;
		this.hitLatency = hitLatency;
		this.cleanMissLatency = cleanMissLatency;
		this.dirtyMissLatency = dirtyMissLatency;

		int numRows = 1 << indexBits;
		arrayCacheSets = new CacheSet[numRows];
		for(int i = 0; i < arrayCacheSets.length; i++){
			arrayCacheSets[i] = new CacheSet(ways);
		}
    }

	public int getIndexBits(long address){
		int indexBitsMask = (2 << indexBits) - 1;
		return (int)(address >> blockOffsetBits) & indexBitsMask;
	}
	//investigate what happens when blockoffsetbits = 0
	//it should always fail and force memory fetch
    @Override
    public int access(boolean load, long address) {
		CacheSet set = arrayCacheSets[getIndexBits(address)];
		if(load){
			if(set.containsBlock(address)){
				int way = set.findBlock(address);
				set.updateLRU(way);
				return hitLatency;
			}
			else {
				int cost = set.evictIfNeeded(address);
				int way = set.findBlock(address);
				set.setTag(way, address);
				set.setValid(way, true);
				set.updateLRU(way);
				return cost;
			}
		}
		else {
			if(set.containsBlock(address)){
				int way = set.findBlock(address);
				set.setDirty(way, true);
				set.updateLRU(way);
				return hitLatency;
			}
			else {
				int cost = set.evictIfNeeded(address);
				int way = set.findBlock(address);
				set.setTag(way, address);
				set.setValid(way, true);
				set.setDirty(way, true);
				set.updateLRU(way);
				return cost;
			}
		}
    }
}
