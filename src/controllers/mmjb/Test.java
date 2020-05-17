package controllers.mmjb;

import controllers.mmjb.neuralnetwork.CompositeNN;
import controllers.mmjb.neuralnetwork.Connection;
import controllers.mmjb.neuralnetwork.NeuralNetwork;
import controllers.mmjb.neuralnetwork.Node;

import java.util.ArrayList;


public class Test {

	public static void main(String[] args) {

		NeuralNetwork n = NeuralNetwork.generateRandom(new int[] {5,2,2,3});
//		int i = n.calcOutput(new double[] {0.6, 0.7, 0.8});
//		testResultSpread();
//		print1(n);
		print1(n);
		System.out.println("new one");
		print1(new CompositeNN(n).getCombined());
		testNNEquality(n, new CompositeNN(n).getCombined());
	}
	
	private static void print1(NeuralNetwork n) {
		Node[][] nodes = n.getNodes();
		System.out.println("***********************");
		
		for(int i = 0; i < nodes.length; i++) {
			System.out.println("Layer: " + i);
			
			for(int j = 0; j < nodes[i].length; j ++) {
				System.out.println("Node: " + j);
				ArrayList<Connection> list = nodes[i][j].weights;
				for(int k = 0; k < list.size(); k++) {
					System.out.println("Coords: " + list.get(k).target + ", Weight: " + list.get(k).weight);
				}
				System.out.println();
			}
			System.out.println();
		}
		System.out.println();
		System.out.println("***********************");
	}
	
	private static void testSaveLoad() {
		NeuralNetwork n = NeuralNetwork.generateRandom(new int[] {3,4,2});
		FileHandler.saveNN(n, "test2.txt");
		NeuralNetwork n2 = FileHandler.loadNN("test2.txt");
		FileHandler.saveNN(n2, "test3.txt");
		NeuralNetwork n3 = FileHandler.loadNN("test3.txt");
		FileHandler.saveNN(n3, "test4.txt");
		
	}
	
	private static void testReliableOutput() {
		NeuralNetwork n = NeuralNetwork.generateRandom(new int[] {8,200,200,200,200,30});

		System.out.println("start");
		
		double[] input = {0.6, 0.7, 0.8, 0.5, 0.3, 0.6, 0.4, 0.4};
		for(int i = 0; i < 1000; i ++) {
			int res = n.calcOutput(input, 0);
			for(int j = 0; j < 100; j++) {
				if(res != n.calcOutput(input, 0))System.out.println("mis");
			}
			n = NeuralNetwork.generateRandom(new int[] {8,200,200,200,200,30});

			//System.out.println(i);
		}
		System.out.println("Done");
		
	}
	
	
	private static void testResultSpread() {
		double[] input = {0.4, 0.5, 0.6, 0.4, 0.8};
		NeuralNetwork n;
		int[] results = new int[8];
		for (int i = 0; i < 10000; i++) {
			n = NeuralNetwork.generateRandom(new int[] {5,20, 100, 20, 8});
			results[n.calcOutput(input, 0)]++;
		}
		for(int i = 0;  i< results.length; i++) {
			System.out.println(i+ ": " + results[i]);
		}
		
	}
	
	private static double sigmoid(double d) {
		return 1/(1+Math.pow(Math.E, -d));
	}
	
	private static double normalizedSigmoid(double d, double c) {
//		if (1+c <= d) return 1.0;
//		if (-(1+c) >= d) return -1.0;
		return (d * c) / (1 + c - Math.abs(d));
	}
	
	private static double logisticSigmoid(double x, double k) {
		double a = 1.0/ (1.0 + Math.exp(0-((x-0.5)*2*k)));
		double b = 1.0/ (1.0+Math.exp(k));
		double c = 1.0/ (1.0+Math.exp(0-k));
		return (a-b)/(c-b);
	}
	
	private static void testNNEquality(NeuralNetwork nn1, NeuralNetwork nn2) {
		Node[][] nodes1 = nn1.getNodes();
		Node[][] nodes2 = nn2.getNodes();
		
		
		if(nodes1.length != nodes2.length) {
			System.out.println("the neural networks don't have the same amount of layers. N1: " + nodes1.length + ", N2:" + nodes2.length);
			return;
		}
		
		for(int i = 0; i < nodes1.length; i++) {
			if(nodes1[i].length != nodes2[i].length) {
				System.out.println("the layer " + i + "isn't the same size. N1: " + nodes1[i].length + ", N2:" + nodes2[i].length);
				return;
			}
			for(int j = 0; j < nodes1[i].length; j++) {
				Node n1 = nodes1[i][j];
				Node n2 = nodes2[i][j];
				
				if(n1.threshold != n2.threshold) System.out.println("(" + i + "," + j + ") doesn't have the same threshold");
				if(n1.weights.size() != n2.weights.size()) System.out.println("(" + i + "," + j + ") doesn't have the same amount of weights");
				else {
					for(Connection c : n1.weights) {
						for(int k = 0; k < n2.weights.size(); k++) {
							Connection c2 = n2.weights.get(k);
							if(c.weight == c2.weight && c.target == c2.target) break;
							else if(k == n2.weights.size()-1) {
								System.out.println("(" + i + "," + j + ") has a connection, that doesn't have a dublicate in the other Neural Network");
//								
//								System.out.println("Coords: " + c.target + ", Weight: " + c.weight);
//								ArrayList<Connection> list = nodes2[i][j].weights;
//								for(int k1 = 0; k1 < list.size(); k1++) {
//									System.out.println("Coords: " + list.get(k1).target + ", Weight: " + list.get(k1).weight);
//								}
//								break;
							}
						} //her et sted er jeg kommet til.. Det er ikke færdigt, fordi fejlen blev fundet.
					}
				}
			}
		}
		System.out.println("done");
	}

}
