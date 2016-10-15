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


	protected class CacheSet {
		private int ways;
		private long[] tag;
		protected int[] lru;
		private boolean[] valid;
		protected boolean[] dirty;
		private int nonTagBits;

		public CacheSet(int ways, int blockOffsetBits, int indexBits){
			this.ways = ways;
			tag = new long[ways];
			valid = new boolean[ways];
			dirty = new boolean[ways];
			lru = new int[ways];
			nonTagBits = blockOffsetBits + indexBits;
		}
		private long getTag(long address){
			return address >> this.nonTagBits;
		}
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
		protected void setTag(int way, long address){
			tag[way] = this.getTag(address);
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
		protected int load(long address){
			if(this.containsBlock(address)){
				int way = this.findBlock(address);
				this.updateLRU(way);
				return hitLatency;
			}
			else {
				int cost = this.evictIfNeeded(address);
				int way = this.emptyWay();
				this.setTag(way, address);
				this.setValid(way, true);
				this.updateLRU(way);
				return cost;
			}
		}
		protected int store(long address){
			if(this.containsBlock(address)){
				int way = this.findBlock(address);
				this.setDirty(way, true);
				this.updateLRU(way);
				return hitLatency;
			}
			else {
				int cost = this.evictIfNeeded(address);
				int way = this.emptyWay();
				this.setTag(way, address);
				this.setValid(way, true);
				this.setDirty(way, true);
				this.updateLRU(way);
				return cost;
			}
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
		rows = 1 << indexBits;
		arrayCacheSets = new CacheSet[rows];
		for(int i = 0; i < arrayCacheSets.length; i++){
			arrayCacheSets[i] = new CacheSet(ways, blockOffsetBits, indexBits);
		}
    }

	public int getIndexBits(long address){
		int indexBitsMask = (1 << indexBits) - 1;
		return (int)(address >> blockOffsetBits) & indexBitsMask;
	}
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
}
