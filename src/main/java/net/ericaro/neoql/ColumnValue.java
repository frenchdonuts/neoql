package net.ericaro.neoql;

 class ColumnValue<T, V> {

	AbstractColumn<T, V> column;
	V value;

	ColumnValue(AbstractColumn<T, V> column, V value) {
		super();
		this.column = column;
		this.value = value;
	}

	 Column<T, V> getColumn() {
		return column;
	}

	 V getValue() {
		return value;
	}
	
	 void set(T row) {
		column.set(row, value ) ;
	}

}
