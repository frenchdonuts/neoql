package net.ericaro.neoql;

/** Simple column-value pair
 * 
 * @author eric
 *
 * @param <T>
 * @param <V>
 */
class ColumnValue<T, V> {

	ColumnImpl<T, V> column;
	V value;

	ColumnValue(ColumnImpl<T, V> column, V value) {
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
		column.set(row, value);
	}

}
