package net.ericaro.osql.system;


class Setter<T,V> {

	V value ;
	private Column<T,V> f;
	
	Setter(Class<T> table, Column<T,V> col, V value) {
		this.value = value;
		f = col;
	}
	void set(T row) {
			f.set(row, value);
	}
	
	public Column<T,V> getColumn(){return f;}
	public V getValue(){return value;}

}
