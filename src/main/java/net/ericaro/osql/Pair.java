package net.ericaro.osql;

public class Pair<L,R> {

	L left;
	R right;
	public Pair(L left, R right) {
		super();
		this.left = left;
		this.right = right;
	}
	public L getLeft() {
		return left;
	}
	public R getRight() {
		return right;
	}
	@Override
	public String toString() {
		return "(" + left + ", " + right + ")";
	}
	
	
	
	
	
}
