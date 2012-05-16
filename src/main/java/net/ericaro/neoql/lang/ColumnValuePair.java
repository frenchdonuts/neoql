package net.ericaro.neoql.lang;

public class ColumnValuePair<T, V> {

	Column<T, V> column;
	V value;

	ColumnValuePair(Column<T, V> column, V value) {
		super();
		this.column = column;
		this.value = value;
	}

	public Column<T, V> getColumn() {
		return column;
	}

	public V getValue() {
		return value;
	}
	
	public void set(T row) {
		column.set(row, value ) ;
	}

}
