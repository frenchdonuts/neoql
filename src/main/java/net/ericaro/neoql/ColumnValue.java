package net.ericaro.neoql;




/** Simple column-value pair
 * 
 * @author eric
 *
 * @param <T>
 * @param <V>
 */
public class ColumnValue<T, V> {

	ColumnDef<T, V> column;
	V value;

	public ColumnValue(Column<T, V> column, V value) {
		super();
		this.column = (ColumnDef<T, V>) column;
		this.value = value;
	}

	public Column<T, V> getColumn() {
		return column;
	}

	public V getValue() {
		return value;
	}

	 /** set the value to the given row. Kept package because this is a backdoor to edit uneditable objects. 
	 * @param row
	 */
	void set(T row) {
		column.set(row, value);
	}
}
