package controllers.mmjb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class Stash {
	private static final int MAX_STASH = 20000;
	private static int maxsStashPerGame = MAX_STASH / 10;
	private static int place = 0;
	private static int to;
	private static int span = 10;



	public static int gameIdx = 2;
	public static final int GRIDSIZE = 32 * 14;
	public static final int INPUTSIZE = GRIDSIZE * 5 + 2;

	private static LinkedHashMap<Integer, ArrayList<double[]>> storedInputs = new LinkedHashMap<Integer,ArrayList<double[]>>();
	private static Iterator<ArrayList<double[]>> storedInputsIte;
	//public static ArrayList<double[]> storedInput = new ArrayList<double[]>();

	public static double[] getStoredInput()
	{
		if (storedInputsIte == null || !storedInputsIte.hasNext())
			storedInputsIte = storedInputs.values().iterator();

		ArrayList<double[]> storedInput = storedInputsIte.next();

		if (place >= to || place >= storedInput.size())
			resetPosition();
		return storedInput.get(place++);
	}

	public static void setSpan(int span){
		Stash.span = span;
	}
	private static void resetPosition(){
		place = 0;
		to = span;
	}

	public static void offset(int i){
		place += i;
		to += i;
		if (to >= MAX_STASH){
			place = 0;
			to = i;
		}
	}

	public static void add(double[] input){
		if (storedInputs.get(gameIdx) == null)
			storedInputs.put(gameIdx, new ArrayList<double[]>());

		ArrayList<double[]> inputs = storedInputs.get(gameIdx);

		if (inputs.size() < maxsStashPerGame)
			inputs.add(input);
	}

	public static void clear(){
		storedInputs.clear();
		storedInputsIte = null;
	}

}
