package controllers.mmjb.neuralnetwork;

import java.util.ArrayList;

public class Node {
	
	public double inputValue;
	public double threshold;
	public ArrayList<Connection> weights;
	
	public Node(double threshold, ArrayList<Connection> weights) {
		this.threshold = threshold;
		this.weights = weights;
	}
	
	public Node(double threshold) {
		this.threshold = threshold;
		weights = new ArrayList<Connection>();
	}
}
