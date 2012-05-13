package net.ericaro.osql;

public class ColumnValuePair<T,V> {

	Column<T,V> column;
	V value;
	public ColumnValuePair(Column<T, V> column, V value) {
		super();
		this.column = column;
		this.value = value;
	}
	public Column<T, V> getColumn() {
		return column;
	}
	public void setColumn(Column<T, V> column) {
		this.column = column;
	}
	public V getValue() {
		return value;
	}
	public void setValue(V value) {
		this.value = value;
	}
	
	public void set(T row) {
		column.set(row, value);
	}
	
}
