package net.ericaro.neoql;

 class ColumnValuePair<T,V> {

	Column<T,V> column;
	V value;
	 ColumnValuePair(Column<T, V> column, V value) {
		super();
		this.column = column;
		this.value = value;
	}
	 Column<T, V> getColumn() {
		return column;
	}
	 void setColumn(Column<T, V> column) {
		this.column = column;
	}
	 V getValue() {
		return value;
	}
	 void setValue(V value) {
		this.value = value;
	}
	
	
	
}
