package controllers.mmjb;

import controllers.mmjb.neuralnetwork.Connection;
import controllers.mmjb.neuralnetwork.NeuralNetwork;
import controllers.mmjb.neuralnetwork.Node;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;


public class FileHandler {
	
	public static NeuralNetwork loadNN(String path) {
		
		BufferedReader reader = null;
		Node[][] nodes;
		
		//God fornøjelse Matthias :p
		try{
			reader = new BufferedReader(new InputStreamReader (new FileInputStream(path)));
			String line = reader.readLine();
			nodes = new Node[Integer.parseInt(line)][];
			line = reader.readLine();
			
			//Hvert lag i det neurale netværk 
			for(int i = 0; line != null; i++) {
				String[] parts = line.split(",");
				nodes[i] = new Node[Integer.parseInt(parts[0])];
				int cur = 1;
				
				//Hver node i laget
				for(int j = 0; j < nodes[i].length; j++) {
					ArrayList<Connection> list = new ArrayList<Connection>(nodes[i].length);
					Node node = new Node(Double.longBitsToDouble(Long.parseLong((parts[cur++]))), list);
					int amount = Integer.parseInt(parts[cur++]);
					
					for(int k = 0; k < amount; k++) {
						Dimension dim = new Dimension(Integer.parseInt(parts[cur++]), Integer.parseInt(parts[cur++]));
						list.add(new Connection(Double.longBitsToDouble(Long.parseLong((parts[cur++]))), dim));
						
					}
					nodes[i][j] = node;
				}
				
				line = reader.readLine();
			}
			
		} catch (IOException ex) {
			  return null;
		} finally {
		   try {reader.close();} catch (Exception ex) {return null;}
		}
		
		return new NeuralNetwork(nodes);
	}
	
	
	public static void saveNN(NeuralNetwork n, String path) {
		
		Writer writer = null;

		File f = new File(path);
		if(f.exists() && !f.isDirectory()) {f.delete(); }

		try {
		    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "utf-8"));
		    
		    Node[][] nodes = n.getNodes();
		    
		    writer.write(nodes.length + "\n");
		    
		    //Don't try to read or understand this
		    for(int i = 0; i < nodes.length; i++) {
		    	writer.write(nodes[i].length + ",");
		    	for(int j = 0; j < nodes[i].length; j++) {
		    		ArrayList<Connection> cons = nodes[i][j].weights;
		    		writer.write(Double.doubleToLongBits(nodes[i][j].threshold) + "," + cons.size());
		    		for(int k = 0; k < cons.size(); k++){
		    			Connection c = cons.get(k);
		    			writer.write("," + c.target.width + "," + c.target.height + "," + Double.doubleToLongBits(c.weight));
		    		}
		    		writer.write(",");
		    	}
		    	writer.write("\n");
		    }
		    
		} catch (IOException ex) {
		  //Throw exception
		} finally {
		   try {writer.close();} catch (Exception ex) {}
		}
	}
	
	
	public static int[][] loadInputFiles(String path) {
		
		BufferedReader reader = null;
		ArrayList<int[]> list = new ArrayList<int[]>();
		
		try{
			reader = new BufferedReader(new InputStreamReader (new FileInputStream(path)));
			String line = reader.readLine();
			line = reader.readLine();
			
			for(int i = 0; line != null; i++, line = reader.readLine()) {
				String[] ints = line.split(" ");
				int[] convInts = new int[ints.length];
				for(int j = 0; j < ints.length; j++) {
					convInts[j] = Integer.parseInt(ints[j]);
				}
				list.add(convInts);
			}
		
			
		} catch (IOException ex) {
			  ex.printStackTrace();
		} finally {
		   try {reader.close();} catch (Exception ex) {ex.printStackTrace();}
		}
		
		int[][] result = new int[list.size()][];
		for(int i = 0; i < list.size(); i++) {
			result[i] = list.get(i);
		}
		
		return result;
	}

}
