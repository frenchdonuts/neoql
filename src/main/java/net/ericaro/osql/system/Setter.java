package net.ericaro.osql.system;


class Setter<T,V> {

	V value ;
	private Column<V> f;
	
	Setter(Class<T> table, Column<V> col, V value) {
		this.value = value;
		f = col;
	}
	
	
	void set(T row) {
			f.set(row, value);
	}

}
