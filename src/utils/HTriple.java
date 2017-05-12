package utils;

// pair of any two things, i.e., <a, b>
public class HTriple<V1, V2, V3> {

	private V1 a;
	
	private V2 b;
	
	private V3 c;
	
	public HTriple(V1 a, V2 b, V3 c){
		this.setA(a);
		this.setB(b);
		this.setC(c);
	}

	public V1 getA() {
		return a;
	}

	public void setA(V1 a) {
		this.a = a;
	}

	public V2 getB() {
		return b;
	}

	public void setB(V2 b) {
		this.b = b;
	}

	public V3 getC() {
		return c;
	}

	public void setC(V3 c) {
		this.c = c;
	}
}
