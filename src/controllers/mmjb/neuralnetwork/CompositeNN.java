package controllers.mmjb.neuralnetwork;

import java.awt.Dimension;
import java.util.ArrayList;

/**
 * Created by Matt on 24-04-2015.
 */
public class CompositeNN implements Comparable<CompositeNN>{
    public NeuralNetwork first;
    public NeuralNetwork second;
    public double fitness;

    public CompositeNN(NeuralNetwork first, NeuralNetwork second) {
    	this.first = first;
    	this.second = second;
    }
    
    public CompositeNN(NeuralNetwork n) {
        split(n);
    }
    
    private void split(NeuralNetwork n) {
    	//Når man opretter et nyt Composite, skal den kun have et NN, som den så splitter op
    	Node[][] nodes = n.getNodes();
    	Node[][] firstNodes = new Node[3][];
    	Node[][] secondNodes = new Node[nodes.length - 2][];
    	
    	//First contains input layer + 1. hidden layer.
    	for(int i = 0; i < 2; i++) {
    		firstNodes[i] = new Node[nodes[i].length];
    		for (int j = 0; j < nodes[i].length; j++) {
                ArrayList<Connection> cons = new ArrayList<Connection>();
                ArrayList<Connection> oldCons = nodes[i][j].weights;
                for (Connection con : oldCons){
                    cons.add(new Connection(con.weight, con.target));
                }
                firstNodes[i][j] = new Node(nodes[i][j].threshold, cons);
            }
    	}
    	//Populating the temporary outputlayer for the 'first' component of the Composite.
    	firstNodes[2] = new Node[nodes[2].length];
    	for(int i = 0; i < nodes[2].length; i++) {
    		firstNodes[2][i] = new Node(0.0);
    	}
    	
    	first = new NeuralNetwork(firstNodes);
    	
    	//Second contains the rest of the hidden layers + output layer.
    	for(int i = 2; i < nodes.length; i++) {
    		secondNodes[i-2] = new Node[nodes[i].length];
    		for (int j = 0; j < nodes[i].length; j++) {
                ArrayList<Connection> cons = new ArrayList<Connection>();
                ArrayList<Connection> oldCons = nodes[i][j].weights;
                for (Connection con : oldCons){
                	Dimension target = new Dimension(con.target.width-2, con.target.height); //Subtracting 2 for the dimensions to be correct when calling calcOutput and returnOutputVector.
                    cons.add(new Connection(con.weight, target));
                }
                secondNodes[i-2][j] = new Node(nodes[i][j].threshold, cons);
            }
    	}
    	second = new NeuralNetwork(secondNodes);
    }

    /*
     * Takes the input vector for the combined neural network.
     * If first then it just returns the output from the first half, 
     * else it returns the output from the entire neural network.
     * This is only expected to be called with first = true.
     */
    public int calcOutput(double[] input, boolean first) {
    	return first ? this.first.calcOutput(input, 0) : second.calcOutput(this.first.returnOutputVector(input, 0), 2);
    }

    /*
     * Takes the input vector for the combined neural network.
     * If first then it just returns the output vector from the first half, 
     * else it returns the output vector from the entire neural network.
     */
    public double[] returnOutputVector(double[] input, boolean first) {
    	return first ? this.first.returnOutputVector(input, 0) : second.returnOutputVector(this.first.returnOutputVector(input, 0), 2);
    }
    
    
    /*
     * Combines the first and second component into one whole neural network
     */
    public NeuralNetwork getCombined() {
    	Node[][] firstNodes = first.getNodes();
    	Node[][] secondNodes = second.getNodes();
    	Node[][] nodes = new Node[secondNodes.length+2][];
    	
    	//First contains input layer + 1. hidden layer.
    	for(int i = 0; i < 2; i++) {
    		nodes[i] = new Node[firstNodes[i].length];
    		for (int j = 0; j < firstNodes[i].length; j++) {
                ArrayList<Connection> cons = new ArrayList<Connection>();
                ArrayList<Connection> oldCons = firstNodes[i][j].weights;
                for (Connection con : oldCons){
                    cons.add(new Connection(con.weight, con.target));
                }
                nodes[i][j] = new Node(firstNodes[i][j].threshold, cons);
            }
    	}
    	
    	//Second contains the rest of the hidden layers + output layer.
    	for(int i = 0; i < secondNodes.length; i++) {
    		nodes[i+2] = new Node[secondNodes[i].length];
    		for (int j = 0; j < secondNodes[i].length; j++) {
                ArrayList<Connection> cons = new ArrayList<Connection>();
                ArrayList<Connection> oldCons = secondNodes[i][j].weights;
                for (Connection con : oldCons){
                	Dimension target = new Dimension(con.target.width+2, con.target.height); //Adding 2 because they were subtracted during split.
                    cons.add(new Connection(con.weight, target));
                }
                nodes[i+2][j] = new Node(secondNodes[i][j].threshold, cons);
            }
    	}
    	
    	return new NeuralNetwork(nodes);
    }

	@Override
	public int compareTo(CompositeNN o) {
		return Double.compare(fitness, o.fitness);
	}

//    public Node[][] getNodes() {
//        return super.getNodes();
//    }
}
