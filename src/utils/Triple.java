package utils;

// triple <a, b, c> of anything
public class Triple<V> {
	
	private V a;
	
	private V b;
	
	private V c;
	
	public Triple(V a, V b, V c){
		this.setA(a);
		this.setB(b);
		this.setC(c);
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

	public V getC() {
		return c;
	}

	public void setC(V c) {
		this.c = c;
	}

}
