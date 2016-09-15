package utils;

// pair of any two things, i.e., <a, b>
public class Pair<V> {

	private V a;
	
	private V b;
	
	public Pair(V a, V b){
		this.setA(a);
		this.setB(b);
	}

	public V getA() {
		return a;
	}

	public void setA(V a) {
		this.a = a;
	}

	public V getB() {
		return b;
	}

	public void setB(V b) {
		this.b = b;
	}
}
