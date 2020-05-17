package controllers.mmjb;

public class ConvenientMethods {
	
	public static double[][] convertArrayIntToDouble(int[][] ints) {
		double[][] doubles = new double[ints.length][];
		
		for(int i = 0; i < ints.length; i++) {
			doubles[i] = new double[ints[i].length];
			for(int j = 0; j < ints[i].length; j++) {
				doubles[i][j] = ints[i][j] * 1.0;
			}
		}
		
		return doubles;
	}
	
}
