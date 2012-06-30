package net.ericaro.neoql;




/** Simple column-value pair
 * 
 * @author eric
 *
 * @param <T>
 * @param <V>
 */
public class ColumnSetter<T, V> {

	MyColumn<T, V> column;
	V value;

	public ColumnSetter(Column<T, V> column, V value) {
		super();
		this.column = (MyColumn<T, V>) column;
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
	boolean set(T row) {
		return column.set(row, value);
	}
	
}
