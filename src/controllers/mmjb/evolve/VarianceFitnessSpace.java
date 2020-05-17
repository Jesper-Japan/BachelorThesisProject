package controllers.mmjb.evolve;

import controllers.mmjb.ConvenientMethods;
import controllers.mmjb.FileHandler;
import controllers.mmjb.Stash;
import controllers.mmjb.neuralnetwork.CompositeNN;
import controllers.mmjb.neuralnetwork.NeuralNetwork;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by Matt on 24-04-2015.
 */
public class VarianceFitnessSpace {
	
	private static int place = 0;
    //double[][] inputs;

    public VarianceFitnessSpace() {
        //for (int i = 1; i < 103; i++) {
            //inputs = ConvenientMethods.convertArrayIntToDouble(FileHandler.loadInputFiles("Inputfiler2\\Inputfiler2\\input1.csv"));
        //}
    }

    public static void playOneGame(int times, NeuralNetwork n) {
        //Load input vectors
        double[][] outputVectors = new double[times][];
        for (int i = 0; i < times; i++) {
            outputVectors[i] = n.returnOutputVector(Stash.getStoredInput(), 0);
        }
        //Calculate output
        n.fitness = calculateFitness(outputVectors);
    }
    
    public static void playOneGame(int times, CompositeNN comp) {
    	playOneGame(times, comp.first);
    	comp.fitness = comp.first.fitness;
    	comp.first.fitness = 0;
    }

    private static double calculateFitness(double[][] outputs){
        ArrayList<Double> distances = new ArrayList<Double>();
        for (int i = 0; i < outputs.length; i++) {
            for (int j = i; j < outputs.length; j++) {
                if (i != j)
                    distances.add(distance(outputs[i], outputs[j]));
            }
        }

        double sum = 0;
        double min = 0;
        for (Double d : distances){
            if (d < min)
                min = (double)d;
            sum += d;
        }
        double avg = sum/distances.size();
        return min + avg;
    }

    private static double distance(double[] v1, double[] v2){
        if (v1.length != v2.length) return 0.0;
        double dif = 0;
        for (int i = 0; i < v1.length; i++) {
            dif += Math.pow(v1[i] - v2[i], 2);
        }
        return Math.sqrt(dif);
    }
}
