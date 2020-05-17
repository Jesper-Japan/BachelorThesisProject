package controllers.mmjb.evolve;

import controllers.mmjb.neuralnetwork.NeuralNetwork;

/**
 * Created by Matt on 24-04-2015.
 */
public interface FitnessSpace {
    void playOneGame(int times, NeuralNetwork n);
}
