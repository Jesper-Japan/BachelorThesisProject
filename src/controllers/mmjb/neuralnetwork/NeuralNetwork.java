package controllers.mmjb.neuralnetwork;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Random;


public class NeuralNetwork implements Comparable<NeuralNetwork>{
	
	private Node[][] nodes;
	public double fitness;
	
	public double[] second = null;
	public double[] third = null;
	public double[] output = null;
	private long K = 0;
	
	public NeuralNetwork(Node[][] nodes) {
		this.nodes = nodes;
	}

    public static NeuralNetwork copy(NeuralNetwork nn){
        Node[][] oldNodes = nn.getNodes();
        Node[][] nodes = nn.getNodes().clone();

        for (int i = 0; i < oldNodes.length; i++) {
            nodes[i] = oldNodes[i].clone();
            for (int j = 0; j < oldNodes[i].length; j++) {
                ArrayList<Connection> cons = new ArrayList<Connection>();
                ArrayList<Connection> oldCons = oldNodes[i][j].weights;
                for (Connection con : oldCons){
                    cons.add(new Connection(con.weight, con.target));
                }
                Node oldNode = oldNodes[i][j];
                nodes[i][j] = new Node(oldNode.threshold, cons);
            }
        }

        return new NeuralNetwork(nodes);
    }
	
	public Node[][] getNodes() {
		return nodes;
	}
	
	@Override
    public int compareTo(NeuralNetwork o) {
        return Double.compare(fitness, o.fitness);
    }
	
	
	
	
	public int calcOutput(double[] input, int layerStart) {
        return findGreatest(returnOutputVector(input, layerStart));
	}
	
	
	public double[] returnOutputVector(double[] inputVector, int layerStart) {
		
		int layer = 0;
		double inputVal;
		
		if(layerStart == 0){
			//Input layer
			for(int i = 0; i < nodes[layer].length; i++) {
				if(inputVector[i] != 0.0) {
					for (Connection c : nodes[layer][i].weights) {
						//input[c.target.height] = inputVector[j] * c.weight;
						nodes[c.target.width][c.target.height].inputValue += inputVector[i] * c.weight;
					}
				}
			}
			layer++;
		} else {
			for(int i = 0; i < nodes[0].length; i++) {
				nodes[0][i].inputValue = inputVector[i];
			}
		}
		
		if(layerStart <= 1) {
			//First hidden layer
			for(int i = 0; i < nodes[layer].length; i++) {
				inputVal = nodes[layer][i].inputValue;
				for (Connection c : nodes[layer][i].weights) {
					nodes[c.target.width][c.target.height].inputValue += sigmoid(inputVal + nodes[layer][i].threshold) * c.weight;
				}
				nodes[layer][i].inputValue = 0;
			}
			layer++;
		}
		
		
		//Subsequent hidden layers
		for( ; layer < nodes.length - 1; layer++) {
			for(int i = 0; i < nodes[layer].length; i++) {
				inputVal = nodes[layer][i].inputValue;
				if(inputVal > nodes[layer][i].threshold) {
					for (Connection c : nodes[layer][i].weights) {
						nodes[c.target.width][c.target.height].inputValue += inputVal * c.weight;
					}
				}

                nodes[layer][i].inputValue = 0;

			}
		}
		
		//Output layer
		double[] output = new double[nodes[nodes.length - 1].length];
		for (layer = 0; layer < nodes[nodes.length - 1].length; layer++) {
            output[layer] = nodes[nodes.length - 1][layer].inputValue;
            nodes[nodes.length - 1][layer].inputValue = 0;
		}

        return output;
	}

	private int findGreatest(double[] input) {
		int greatest = -1;
		for(int i = 0; i < input.length; i++) {
			if(input[i] != 0.0) {
				if(greatest == -1) {
					greatest = i;
				}
				else if(input[i] > input[greatest]) {
					greatest = i;
				}
			}
		}
		return greatest;
	}
	
	private static double sigmoid(double d) {
		return 1/(1+Math.pow(Math.E, 0-d));
	}
	
	private static double normalizedSigmoid(double d, double c) {
		if (1+c <= d) return 1.0;
		if (-(1+c) >= d) return -1.0;
		return (d * c) / (1 + c - Math.abs(d));
	}
	
	private static double logisticSigmoid(double x, double k) {
		double a = 1.0/ (1.0 + Math.exp(0-((x-0.5)*2*k)));
		double b = 1.0/ (1.0+Math.exp(k));
		double c = 1.0/ (1.0+Math.exp(0-k));
		return (a-b)/(c-b);
	}
	
	
	public static NeuralNetwork generateRandom(int[] layers) {
		Node[][] arr = new Node[layers.length][];
		Random r = new Random();
		
		for(int i = 0; i < layers.length; i++) {
			arr[i] = new Node[layers[i]];
			
			for(int j = 0; j < layers[i]; j++) {

				double threshold = 0.0;
				
				//output layer
				if(i == layers.length-1) {
					arr[i][j] = new Node(threshold);
				} else {
					
					int c = layers[i];
					if(i != 0) {
						c += layers[i-1];
					}
					
					ArrayList<Connection> list = new ArrayList<Connection>();
					for (int k = 0; k < layers[i+1]; k++) {
						list.add(new Connection(((r.nextDouble() * 2.0) - 1.0)/c, new Dimension(i+1, k)));
					}
					
					threshold = ((r.nextDouble() * 2.0) - 1.0)/c; //Skift til mellem -1 og 1 og layers[i] + layers[i-1]
					
					arr[i][j] = new Node(threshold, list);
 				}
			}
		}
		
		
		return new NeuralNetwork(arr);
	}
	 
	
}