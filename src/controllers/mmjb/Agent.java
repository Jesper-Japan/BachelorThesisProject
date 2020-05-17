package controllers.mmjb;


import controllers.mmjb.evolve.GVGFitnessSpace;
import controllers.mmjb.neuralnetwork.CompositeNN;
import controllers.mmjb.neuralnetwork.NeuralNetwork;
import core.competition.CompetitionParameters;
import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.util.*;

public class Agent extends AbstractPlayer {

    public NeuralNetwork n;
    private CompositeNN comp;
    public double nFitness;
    private HashSet<Vector2d> movement;
    private Vector2d lastLocation;
    private double[] previousLocations;
    private Vector2d nearestGem;
    private double nearestGemDistance;

    private int gameLength;

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {

        //Size of the game grid
        int xSize = so.getObservationGrid().length;
        int ySize = so.getObservationGrid()[0].length;

        //Setup fields
        movement = new HashSet<Vector2d>();
        previousLocations = new double[xSize * ySize];
        nearestGem = new Vector2d(xSize+1, ySize+1); //Initialize to value larger than max possible length

        //Load the neural network, either from file or generate a new one
        if(GVGFitnessSpace.composite) {
        	comp = GVGFitnessSpace.curComp;
        } else {
        	n = GVGFitnessSpace.getNeuralNetwork();
        }
        //nn = NeuralNetwork.generateRandom(new int[]{2, 20, 20,so.getAvailableActions().size()});
    }

    @Override
    public void OnGameEnd() {
        if (gameLength < CompetitionParameters.MAX_TIMESTEPS)
            nFitness -= CompetitionParameters.MAX_TIMESTEPS - gameLength;
        nFitness += movement.size();
        
        if(GVGFitnessSpace.composite) {
        	comp.fitness += nFitness + CompetitionParameters.MAX_TIMESTEPS;
        } else {
        	n.fitness += nFitness + CompetitionParameters.MAX_TIMESTEPS;
        }
    }

    @Override
    /**
     *
     * @param stateObs     state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        /* Evaluate the actions of the neural network so far */
        Vector2d pos = stateObs.getAvatarPosition();
        gameLength = stateObs.getGameTick();

        movement.add(stateObs.getAvatarPosition());


        //Fitness of finding gems (specific to boulderdash)
//        if (lastLocation != null && (Math.abs(pos.x - nearestGem.x) < Math.abs(lastLocation.x - nearestGem.x) || Math.abs(pos.y - nearestGem.y) < Math.abs(lastLocation.y - nearestGem.y))){
//            ennFitness += 20;
//        }
//        else if (lastLocation != null && (Math.abs(pos.x - nearestGem.x) > Math.abs(lastLocation.x - nearestGem.x) || Math.abs(pos.y - nearestGem.y) > Math.abs(lastLocation.y - nearestGem.y))){
//            ennFitness -= 20;
//        }

        /* Retrieve information about current state and transform it into input for neural network */
        ArrayList<Observation>[][] obs = stateObs.getObservationGrid();

        double[] input = new double[Stash.INPUTSIZE];

        for(int i = 0; i < obs.length; i++ ) {
            for (int j = 0; j < obs[i].length; j++) {
                input[obs.length * obs[0].length + j*i + j] = previousLocations[j*i+j];
                for(Observation o : obs[i][j]) {
                    if(o.category == Types.TYPE_AVATAR)
                    {
                        input[j * i + j] = 1.0;
                        if (previousLocations[j * i + j] == 1.0)
                            nFitness -= 1; //Penalize for being in an area it has already been in
                        else
                            previousLocations[j * i + j] = 1.0;
                        input[obs.length * obs[0].length + j*i + j] = previousLocations[j * i + j];
                    }
                    if(o.category == Types.TYPE_RESOURCE) {
                        input[obs.length * obs[0].length * 2 + j * i + j] = 1.0;
                    }
                    if(o.category == Types.TYPE_NPC)
                        input[obs.length * obs[0].length * 3 + j*i + j] = 1.0;
                    if(o.category == Types.TYPE_STATIC)
                        input[obs.length * obs[0].length * 4 + j*i + j] = 1.0;
                }
            }
        }
        input[input.length - 1] = stateObs.getGameTick() * 0.1;
        input[input.length - 2] = Stash.gameIdx;

        Stash.add(input);

        /* Feed input into neural network */
        int action = GVGFitnessSpace.composite ? comp.calcOutput(input, false) : n.calcOutput(input, 0);
        /* Return the action chosen by neural network */
        //System.out.println(stateObs.getAvailableActions().get(action));

        if (action < 0){
            if (getLastAction() != Types.ACTIONS.ACTION_NIL)
                System.out.println("*********OUTPUTTING NIL**********");
            return Types.ACTIONS.ACTION_NIL;
        }

        Types.ACTIONS chosenAction = stateObs.getAvailableActions().get(action);
        lastLocation = pos;
        return chosenAction;
    }


}
