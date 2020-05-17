package controllers.mmjb;

import controllers.mmjb.evolve.GVGFitnessSpace;
import core.competition.CompetitionParameters;

import java.io.IOException;

/**
 * Created by Matt on 19-04-2015.
 */
public class ShowGame {
    public static void main(String[] args) throws IOException{
        System.out.println("Press to see results of the saved neural network with highest fitness");
        System.in.read();
        CompetitionParameters.MAX_TIMESTEPS = 2000;
        GVGFitnessSpace.playGameWithVisuals(FileHandler.loadNN(args[0]));
    }
}
