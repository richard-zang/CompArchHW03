package cis501.submission;

import cis501.Direction;
import cis501.IDirectionPredictor;

public class DirPredTournament extends DirPredBimodal {

    IDirectionPredictor predictorNT;
    IDirectionPredictor predictorT;

    public DirPredTournament(int chooserIndexBits, IDirectionPredictor predictorNT, IDirectionPredictor predictorT) {
        super(chooserIndexBits); // re-use DirPredBimodal as the chooser table
        this.predictorNT = predictorNT;
        this.predictorT = predictorT;
    }

    @Override
    public Direction predict(long pc) {
        return (counterTable[maskedPC(pc)] > f) ? predictorT.predict(pc) :
                                                  predictorNT.predict(pc);
    }

    @Override
    public void train(long pc, Direction actual) {
        // If predictors guessed different things, update Tournament to bias the correct.
        // Child predictor.
        if(predictorT.predict(pc) != predictorNT.predict(pc)){
            if(predictorT.predict(pc) == actual){
                if(counterTable[maskedPC(pc)] != T) counterTable[maskedPC(pc)]++;
            }
            else {
                if(counterTable[maskedPC(pc)] != F) counterTable[maskedPC(pc)]--;
            }
        }
        // Train children predictors!
        predictorNT.train(pc, actual);
        predictorT.train(pc, actual);
    }
}
