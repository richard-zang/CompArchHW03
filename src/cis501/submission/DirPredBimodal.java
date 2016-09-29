package cis501.submission;

import cis501.Direction;
import cis501.IDirectionPredictor;

public class DirPredBimodal implements IDirectionPredictor {
	protected final byte T = 3;
	protected final byte t = 2;
	protected final byte f = 1;
	protected final byte F = 0;

	protected byte[] counterTable;

	protected int indexBits;

    public DirPredBimodal(int indexBits) {
		counterTable = new byte[1 << indexBits];
		this.indexBits = indexBits;
    }

    @Override
    public Direction predict(long pc) {
        return (counterTable[maskedPC(pc)] > f) ? Direction.Taken : Direction.NotTaken;
    }

	//The mask should contain indexBits number of 1s.
	//For example, if indexBits is 5, the mask is (1 << 5) - 1
	//which equals 0b00011111.
	public int maskedPC(long pc){
		int mask = (1 << indexBits) - 1;
		return (int)pc & mask;
	}

    @Override
    public void train(long pc, Direction actual) {
		if(actual == Direction.Taken){
			if(counterTable[maskedPC(pc)] != T) counterTable[maskedPC(pc)]++;
		}
		else {
			if(counterTable[maskedPC(pc)] != F) counterTable[maskedPC(pc)]--;
		}
    }
}
