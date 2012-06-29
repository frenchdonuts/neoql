package net.ericaro.neoql;

import net.ericaro.neoql.tables.Mapper;

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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair other = (Pair) obj;
		if (left == null) {
			if (other.left != null)
				return false;
		} else if (!left.equals(other.left))
			return false;
		if (right == null) {
			if (other.right != null)
				return false;
		} else if (!right.equals(other.right))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "(" + left + ", " + right + ")";
	}
	
	
	public static <L,R> Mapper<Pair<L,R>, L> left(){
		return new Mapper<Pair<L,R>, L>() {
			@Override
			public L map(Pair<L, R> source) {
				return source.getLeft();
			}
			
		};
	}
	public static <L,R> Mapper<Pair<L,R>, R> right(){
		return new Mapper<Pair<L,R>, R>() {
			@Override
			public R map(Pair<L, R> source) {
				return source.getRight();
			}
			
		};
	}
}
