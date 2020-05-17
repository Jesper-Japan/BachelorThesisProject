package controllers.mmjb.evolve;

import controllers.mmjb.Stash;
import controllers.mmjb.neuralnetwork.CompositeNN;
import controllers.mmjb.neuralnetwork.NeuralNetwork;
import core.ArcadeMachine;
import tools.StatSummary;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Created by Matt on 24-04-2015.
 */
public class GVGFitnessSpace {

    private static Queue<NeuralNetwork> runQueue = new LinkedList<NeuralNetwork>(); //Queue used for when the agent wants a neural network.
    public static CompositeNN curComp;
    public static boolean composite;
    
    /* Variables for playing the games */
    static String gamesPath = "examples/gridphysics/";
    static String neuralNetworkController = "controllers.mmjb.Agent";

    //CIG 2014 Training Set Games
    static String games[] = new String[]{"aliens", "boulderdash", "butterflies", "chase", "frogs",
            "missilecommand", "portals", "sokoban", "survivezombies", "zelda"};

    //CIG 2014 Validation Set Games
    //String games[] = new String[]{"camelRace", "digdug", "firestorms", "infection", "firecaster",
    //        "overload", "pacman", "seaquest", "whackamole", "eggomania"};

    
    static int levelIdx = 0; //level names from 0 to 4 (game_lvlN.txt).
    static String game = gamesPath + games[Stash.gameIdx] + ".txt";
    static String level = gamesPath + games[Stash.gameIdx] + "_lvl" + levelIdx +".txt";
    static String level2 = gamesPath + games[Stash.gameIdx] + "_lvl" + 2 +".txt";
    static String level3 = gamesPath + games[Stash.gameIdx] + "_lvl" + 3 +".txt";
    static String level4 = gamesPath + games[Stash.gameIdx] + "_lvl" + 4 +".txt";
    static String[] levels = new String[]{level};
    static int[] gamesToPlayIDs = new int[] {3};


    public static void playGame(int times, NeuralNetwork n) {
        n.fitness = 0;

        //Throw the neural network onto the queue so the agent will fetch it
        for (int i = 0; i < times; i++) {
            runQueue.add(n);
        }

        StatSummary scores;

        for (int i = 0; i < gamesToPlayIDs.length; i++) {
            changeGame(gamesToPlayIDs[i]);
            scores = executeGame(times);
            n.fitness += scores.sum() * 10;
            n.fitness += scores.timesWon * 1000;
        }

        n.fitness /= gamesToPlayIDs.length;

    }
    
    public static void playGame(int times, CompositeNN comp) {
        comp.fitness = 0;
        curComp = comp;
        composite = true;

        StatSummary scores;
        for (int i = 0; i < gamesToPlayIDs.length; i++) {
            changeGame(gamesToPlayIDs[i]);
            scores = executeGame(times);
            comp.fitness += scores.sum() * 10;
            comp.fitness += scores.timesWon * 1000;
        }

        comp.fitness /= gamesToPlayIDs.length;
    }

    private static StatSummary executeGame(int times) {
        return ArcadeMachine.runGames(game, levels, times, neuralNetworkController, null);
    }

    public static void playGameWithVisuals(NeuralNetwork nn){
        int seed = new Random().nextInt();
        runQueue.add(nn);
        changeGame(gamesToPlayIDs[0]);
        ArcadeMachine.runOneGame(game, levels[0], true, neuralNetworkController, null, seed);
    }

    public static NeuralNetwork getNeuralNetwork(){
        if (runQueue.peek() != null){
            return runQueue.poll();
        }
        throw new NullPointerException("no neural network was found in the run queue!");
    }

    private static void changeGame(int gameId){
        Stash.gameIdx = gameId;
        game = gamesPath + games[gameId] + ".txt";

        for (int i = 0; i < levels.length; i++) {
            levels[i] = gamesPath + games[Stash.gameIdx] + "_lvl" + i +".txt";
        }
    }
}
