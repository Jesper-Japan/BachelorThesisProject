package controllers.mmjb.neuralnetwork;

import java.awt.Dimension;

public class Connection {
	public double weight;
	public Dimension target;
	public Connection(double weight, Dimension target) {
		this.weight = weight;
		this.target = target;
	}
}
