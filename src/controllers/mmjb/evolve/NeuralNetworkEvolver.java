package controllers.mmjb.evolve;

import controllers.mmjb.FileHandler;
import controllers.mmjb.Stash;
import controllers.mmjb.neuralnetwork.CompositeNN;
import controllers.mmjb.neuralnetwork.Connection;
import controllers.mmjb.neuralnetwork.NeuralNetwork;
import controllers.mmjb.neuralnetwork.Node;
import core.competition.CompetitionParameters;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.*;

/**
 * Created by Matt on 07-03-2015.
 */
public class NeuralNetworkEvolver{
    private static final boolean SEVERAL_GAMES = false;
            ;
    /* Variables for the neural networks */
    private NeuralNetwork[] neuralNetworks;
    private CompositeNN[] composites;
    private int loop;
    private final String id;
    
    private Writer writer;
    private long startTime;
    
    private static final boolean COMPOSITE_NEURALNETWORKS = true;
    private static final boolean CROSS_OVER = true;


    /* Variables for evolving */
    //private static final boolean ABSOLUTE_MUTATION = false;

    private int populationSize;
    private int[] nnSize;

    private static double mutationRate = 0.4;
    private static double mutationChance = 0.05;

    /* Internal variables */
    private static Random rand = new Random();

    public NeuralNetworkEvolver(int populationSize, int[] nnSize, String id) throws UnsupportedEncodingException, FileNotFoundException {
        this.populationSize = populationSize;
        this.nnSize = nnSize;
        loop = 0;
        this.id = id;
        
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Results_" + id + ".csv"), "utf-8"));
        
        neuralNetworks = new NeuralNetwork[populationSize];
        for (int i = 0; i < populationSize; i++) {
        	neuralNetworks[i] = NeuralNetwork.generateRandom(nnSize);
        }

        if(COMPOSITE_NEURALNETWORKS) {
        	composites = new CompositeNN[populationSize];
        	for(int i = 0; i < populationSize; i++) {
        		composites[i] = new CompositeNN(neuralNetworks[i]);
        	}
        	neuralNetworks = null;
        }
        
        startTime = System.currentTimeMillis();
    }
    
    public NeuralNetworkEvolver(int populationSize, int[] nnSize, CompositeNN[] keepers, String id) throws UnsupportedEncodingException, FileNotFoundException {
    	this(populationSize, nnSize, id);
    	for(int i = 0; i < keepers.length; i++) {
    		composites[i] = keepers[i];
    	}
    }

    public static void main(String[] args) throws IOException
    {
        //GVGFitnessSpace.playGameWithVisuals(FileHandler.loadNN("highestFitness_Generation516"));

        final int COMPRESSION_GENERATIONS = 50;
        final int COMPRESSION_RUNS = 1000;
        final int BEHAVIOR_GENERATIONS = 50;
        final int BEHAVIOR_RUNS = 3;
        final int LOOPS = 6;

        NeuralNetworkEvolver nne = new NeuralNetworkEvolver(40, new int[]{Stash.INPUTSIZE, 10, 10, 4}, args[0]);
    	//Writing out the stats of the run.
    	nne.writer.write("Loop;Generation;Time;High Score;;" +
                "PopulationSize: " + nne.populationSize +
                ";MutationRate: " + mutationRate +
                ";MutationChance: " + mutationChance +
                ";Compression Generations: " + COMPRESSION_GENERATIONS +
                ";Compression Runs: " + COMPRESSION_RUNS +
                ";Behavior Generations: " + BEHAVIOR_GENERATIONS +
                ";Behavior Runs: " + BEHAVIOR_RUNS +
                ";Loops: " + LOOPS +
                ";Max Timesteps: " + CompetitionParameters.MAX_TIMESTEPS +
                ";Neural Network Size: (" + nne.nnSize[0]);

    	for(int i = 1; i < nne.nnSize.length; i++) nne.writer.write("," + nne.nnSize[i]);
    	nne.writer.write(")\n");
    	nne.writer.flush();

        Stash.setSpan(COMPRESSION_RUNS);
    	
        if(COMPOSITE_NEURALNETWORKS) {
        	CompositeNN comp = nne.surviveComposite(1, 1, false); //generating input for training the first real
        	System.out.println("\n\n\n\n\n\n\n\n\n"); //Clearing console
        	while(nne.loop++ < LOOPS) {
	        	System.out.println("\n\nTeaching first part of the neural networks\n");
	        	comp = nne.surviveComposite(COMPRESSION_GENERATIONS, COMPRESSION_RUNS, true);
	        	Stash.clear();
	        	nne.prepareSecondIteration(comp);
	        	System.out.println("\n\nTeaching second part of the neural networks\n");
	        	comp = nne.surviveComposite(BEHAVIOR_GENERATIONS, BEHAVIOR_RUNS, false);
	        	nne.saveNeuralNetworks();
        	}
        	
            System.in.read();
            GVGFitnessSpace.playGameWithVisuals(comp.getCombined());
        } else {
        	NeuralNetwork evolvednn = nne.survive(BEHAVIOR_GENERATIONS * LOOPS,BEHAVIOR_RUNS);
        	FileHandler.saveNN(evolvednn, "FinalNeuralNetwork");
    	}
    	nne.writer.close();

    }
    
    /*
     * Prepares the NeuralNetworkEvolver for training the second part of the neural networks
     * It does so by taking the best result from the training the first part of the neural networks
     * and copying it's 'first' component into all of the composites 'first' component.
     * 
     * Måske er dette en dårlig ide..
     */
    private void prepareSecondIteration(CompositeNN comp) {
    	for(int i = 0; i < composites.length; i++) {
    		composites[i].first = NeuralNetwork.copy(comp.first);
    	}
    }
    
    private void saveNeuralNetworks() {
    	for(int i = 0; i < composites.length; i++) {
    		FileHandler.saveNN(composites[i].getCombined(), "Loop" + loop + "NN" + i);
    	}
    }
    
    private void loadNeuralNetworks(String path) {
    	neuralNetworks = new NeuralNetwork[populationSize];
        for (int i = 0; i < populationSize; i++) {
        	neuralNetworks[i] = FileHandler.loadNN(path + "NN" + i);
        }

        if(COMPOSITE_NEURALNETWORKS) {
        	composites = new CompositeNN[populationSize];
        	for(int i = 0; i < populationSize; i++) {
        		composites[i] = new CompositeNN(neuralNetworks[i]);
        	}
        	neuralNetworks = null;
        }
    }
    
    private CompositeNN surviveComposite(int generations, int gameAmount, boolean first) {
    	//Det er her magien for alvor skal finde sted.. Resten er rimelig designet med undtagelse af Composite
    	
    	double highestFitness = 0;
        for (int i = 0; i < generations; i++) {
            System.out.println("********* LOOP " + loop + " - GENERATION " + i + " *********");
            for (CompositeNN comp : composites){
                // Hvad der sker afhænger af 'first' - train first or second part of the Composite
            	if(first) {
            		VarianceFitnessSpace.playOneGame(gameAmount, comp);
            	} else {
            		GVGFitnessSpace.playGame(gameAmount, comp);
            	}
                comp.fitness /= gameAmount;
                
                
                if (!first) {
                    System.out.println("Game(s) resulted in fitness: " + comp.fitness);
                	comp.fitness = (int) comp.fitness;
                }
                if (comp.fitness > highestFitness) {
                    if(!first) FileHandler.saveNN(comp.getCombined(), "highFitness" + comp.fitness + "loop" + loop);
                    highestFitness = comp.fitness;
                }

                Stash.offset(gameAmount);
            }

            Arrays.sort(composites);
            Collections.reverse(Arrays.asList(composites));
            
            if(!first || i%1 == 0) {
            	try {
            
	            	writer.write(loop + ";" + i + ";" + (System.currentTimeMillis() - startTime) + ";" + String.format("%f", highestFitness));
	            	for(int j = 0; j < populationSize; j++) {
	            		writer.write(";" + String.format("%f", composites[j].fitness));
	            	}
	            	writer.write("\n");
	    			writer.flush();
	    		} catch (IOException e) {
	    			e.printStackTrace();
	    		}
            }
            
            
            if (!first)
                FileHandler.saveNN(composites[0].getCombined(), "highestFitness_Generation" + i + "loop" + loop);

            for (CompositeNN c : composites){
                System.out.println(c.fitness);
            }
            
            
            //Evolving the collection of composites
            if (i < generations - 1) { //If this is the last run then don't evolve nn's
                if(first) {
                	//Extract the first part of all composites and evolve on them before returning them to their respective composites;
                	NeuralNetwork[] firsts = new NeuralNetwork[populationSize];
                	
                	for(int j = 0; j < populationSize; j++) {
                		firsts[j] = composites[j].first;
                	}
                	firsts = evolve(firsts);
                	
                	for(int j = 0; j < populationSize; j++) {
                		composites[j].first = firsts[j];
                		composites[j].fitness = 0.0;
                	}
                }
                else {
            		NeuralNetwork[] seconds = new NeuralNetwork[populationSize];
                	
                	for(int j = 0; j < populationSize; j++) {
                		seconds[j] = composites[j].second;
                	}
                	seconds = evolve(seconds);
                	
                	for(int j = 0; j < populationSize; j++) {
                		composites[j].second = seconds[j];
                		composites[j].fitness = 0.0;
                	}
                }
            }
        }
        
    	return composites[0];
    }
    

    public NeuralNetwork survive(int generations, int gameAmount) {
    	
        double highestFitness = 0;
        for (int i = 0; i < generations; i++) {
            System.out.println("********* GENERATION " + i + " *********");
            for (NeuralNetwork n : neuralNetworks){
                GVGFitnessSpace.playGame(gameAmount, n);
                n.fitness /= gameAmount;
                System.out.println("Game(s) resulted in fitness: " + n.fitness);
                if (n.fitness > highestFitness) {
                    FileHandler.saveNN(n, "highFitness" + n.fitness);
                    highestFitness = n.fitness;
                }
            }

            Arrays.sort(neuralNetworks);
            Collections.reverse(Arrays.asList(neuralNetworks));
            FileHandler.saveNN(neuralNetworks[0], "highestFitness_Generation" + i);

            try {
                
            	writer.write(loop + ";" + i + ";" + (System.currentTimeMillis() - startTime) + ";" + String.format("%f", highestFitness));
            	for(int j = 0; j < populationSize; j++) {
            		writer.write(";" + String.format("%f", neuralNetworks[j].fitness));
            	}
            	writer.write("\n");
    			writer.flush();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}

            for (NeuralNetwork n : neuralNetworks){
                System.out.println(n.fitness);
            }
            
            
            if (i < generations - 1) { //If this is the last run then don't evolve nn's
                neuralNetworks = evolve(neuralNetworks);
            }
        }

        //return the highest scoring neural network
        System.out.println("Fitness of returning neural network: " + neuralNetworks[0].fitness);
        return neuralNetworks[0];
    }


    private static NeuralNetwork[] evolve(NeuralNetwork[] oldies){
        NeuralNetwork[] newNNs = new NeuralNetwork[oldies.length];
        float[] probabilities = new float[oldies.length];
        int nnCount = 0;

        //Keep the top third
        for (; nnCount < oldies.length/3 ; nnCount++) {
            newNNs[nnCount] = oldies[nnCount];
        }

        //Create probabilities
        for(int i = 1; i <= oldies.length; i++){
            probabilities[i-1] = 1.0f/i;
        }

        for (NeuralNetwork n : oldies) {
            float randValue = rand.nextFloat();
            int index;
            for (index = probabilities.length - 1; index > 0; index--) {
                if (randValue < probabilities[index])
                    break;
            }

            if (nnCount >= oldies.length)
                break;

            newNNs[nnCount++] = evolveNN(n, oldies[index]);
        }

        return newNNs;
    }

    private static NeuralNetwork evolveNN(NeuralNetwork n1, NeuralNetwork n2){
        Node[][] sourceNodes = NeuralNetwork.copy(n1).getNodes();
        Node[][] otherNodes = NeuralNetwork.copy(n2).getNodes();

        Node[][] nn;
        if (CROSS_OVER)
            nn = crossOver(sourceNodes, otherNodes);
        else
            nn = sourceNodes;

        nn = mutate(nn);
        return new NeuralNetwork(nn);
    }

    private static Node[][] crossOver(Node[][] f1, Node[][] f2){
        Random rand = new Random();

        if (rand.nextFloat() > 0.5){
            Node[][] tmp;
            tmp = f1;
            f1 = f2;
            f2 = tmp;
        }

        /* Pick a random layer in the arrays */
        int randLayer = rand.nextInt(f2.length); //picked from the second array, since it's the one who will decide the output nodes.

        if (randLayer == 0) //if the layer picked is 0 it's the input nodes, which it does not make sense to split on
            return f1;
//            return rand.nextFloat() < 0.5 ? f1 : f2;

        if (randLayer == f1.length - 1 || randLayer == f2.length - 1)
            return f1;
//            return rand.nextFloat() < 0.5 ? f1 : f2; //TODO: Should probably implement a better handling for when the resulting layer is the output nodes of either network

        /* Cross over where the splitting point is the layer */
        Node[][] newArr = new Node[f2.length][];
        for (int i = 0; i < f2.length; i++){
            if (i < randLayer)
                newArr[i] = f1[i].clone();
            else
                newArr[i] = f2[i].clone();
        }

        return newArr;
    }

    private static Node[][] mutate(Node[][] f1){
        Random rand = new Random();

        for (int i = 0; i < f1.length; i++) {
            for (int j = 0; j < f1[i].length; j++) {
                for (Connection con : f1[i][j].weights){
                    if (rand.nextFloat() < mutationChance){
                        con.weight += ((rand.nextFloat()-0.5) * mutationRate);
                    }
                }

                if (rand.nextFloat() < mutationChance)
                    f1[i][j].threshold += ((rand.nextFloat()-0.5) * mutationRate);
            }
        }
        return f1;
    }

    /* GENERIC GETTERS AND SETTERS */

    public NeuralNetwork[] getNeuralNetworks() {
        return neuralNetworks;
    }

}
