package utils;

import java.util.ArrayList;

public class Combination {
	
	public void combination(Object [] array, int n, ArrayList<ArrayList<Object>> combinations) {
		combination(array, new int[n], 0, n, combinations);
	}

	public void combination(Object [] array, int [] indexes, int start, int n, ArrayList<ArrayList<Object>> combinations) {
		if (n == 1) {
			ArrayList<Object> prefixValue = new ArrayList<Object>();
			String prefix = generatePrefix(array, indexes, prefixValue);
			
			for (int i = start; i < array.length; i++) {
				ArrayList<Object> combination = new ArrayList<Object>();
				for (int j = 0; j < prefixValue.size(); j ++)
					combination.add(prefixValue.get(j));
				
				combination.add(array[i]);
				//sum += prefixValue.get(j);
				//System.out.print(prefix);
				//System.out.print(array[i]);
				//System.out.println(']');
//				System.out.println(sum);
				if (!combinations.contains(combination))
					combinations.add(combination);
			}
		} else {
			for (int i = start; i <= array.length - n; i++) {
				indexes[indexes.length - n] = i;
				combination(array, indexes, i + 1, n - 1, combinations);
			}
		}
	}

	private String generatePrefix(Object [] array, int [] indexes, ArrayList<Object> prefix ) {
		StringBuilder prefixBuilder = new StringBuilder("[");
		for (int i = 0; i < indexes.length - 1; i++) {
			prefixBuilder.append(array[indexes[i]]).append(", ");
			prefix.add(array[indexes[i]]);
		}
		return prefixBuilder.toString();
	}

	public static void main(String[] args) {
		Combination c = new Combination();
		ArrayList<ArrayList<Object>> sum = new ArrayList<ArrayList<Object>>();
		c.combination(new Integer [] { 1, 4, 5, 6, 9, 10}, 4, sum);
		System.out.println(sum.toString());
	}

}