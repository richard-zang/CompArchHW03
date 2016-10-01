
package cis501.submission;

import cis501.Direction;

public class DirPredGshare extends DirPredBimodal {
    //Branch History Register
    protected int BHR;
    protected int historyBits;

    public DirPredGshare(int indexBits, int historyBits) {
        super(indexBits);
        BHR = 0;
        this.historyBits = historyBits;
    }

    @Override
    public Direction predict(long pc) {
        //Plug in the XORed PC into the Bimodal predictor.
        return super.predict(hashedIndex(pc));
    }

    protected int hashedIndex(long pc){
        return maskedPC(BHR ^ (int)pc);
    }

    @Override
    public void train(long pc, Direction actual) {
        if(actual == Direction.Taken){
            if(counterTable[hashedIndex(pc)] != T) counterTable[hashedIndex(pc)]++;
        }
        else {
            if(counterTable[hashedIndex(pc)] != F) counterTable[hashedIndex(pc)]--;
        }
        //Clear out the nth bit.
        //For example, assume historyBits == 5
        //and BHR = 0b00010101
        //and the new branch is taken.
        //Shift BHR by 1 so BHR = 0b00101010
        //AND BHR by 0b00011111 to retain only the lowest historyBits(5) bits.
        //0b00011111 = (1 << historyBits) - 1;
        //We get 0b00001010.
        //Then by ORing the new branch information, we get 0b00001011.
        BHR = (BHR << 1) & ((1 << historyBits) - 1);
        BHR |= (actual == Direction.Taken) ? 1 : 0;
    }
}
