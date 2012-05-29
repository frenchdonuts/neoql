package net.ericaro.neoql.lang;

import net.ericaro.neoql.system.Column;


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

	/** already deprecated, because I need to separated the "object" management from the rest of the world.
	 * the lang package should be isolated. this violate the isolation, but I need to do it for now
	 * 
	 * @param row
	 */
	@Deprecated
	public void set(T row) {
		column.set(row, value);
	}
}
