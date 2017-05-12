package utils;

// pair of any two things, i.e., <a, b>
public class HPair<V1, V2> {

	private V1 a;
	
	private V2 b;
	
	public HPair(V1 a, V2 b){
		this.setA(a);
		this.setB(b);
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
}
