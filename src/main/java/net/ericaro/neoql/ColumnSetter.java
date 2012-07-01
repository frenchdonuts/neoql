package net.ericaro.neoql;




/** Stores a (Column,Value) pairs in order to later be able to apply it on a row.
 * 
 * @author eric
 *
 * @param <T> Table type.
 * @param <V> Column Type
 */
public class ColumnSetter<T, V> {

	Column<T, V> column;
	Singleton<V> value;

	/** construct using this two parameters.
	 * 
	 * @param column
	 * @param value
	 */
	public ColumnSetter(Column<T, V> column, V value) {
		super();
		this.column = (Column<T, V>) column;
		this.value =  new FinalSingleton<V>(value);
	}

	/** Construct an association between a column, and a singleton. The singleton "get()" value will be always used.
	 * 
	 * @param column
	 * @param value
	 */
	public ColumnSetter(Column<T, V> column, Singleton<V> value) {
		this.column = column;
		this.value = value;
	}

	/** Return the current Column from this association
	 * 
	 * @return
	 */
	public Column<T, V> getColumn() {
		return column;
	}
	
	/** returns the current value from this association. 
	 * Note that this association, actually stores a singleton, hence the returned value may change,
	 *  if the singleton value changes.
	 * 
	 * @return
	 */
	public V getValue() {
		return value.get();
	}
	
	/** return the current singleton from this association.
	 * 
	 * @return
	 */
	public Singleton<V> getSingletonValue() {
		return value;
	}

	 /** set the value to the given row. Kept package because this is a backdoor to edit uneditable objects. 
	 * @param row
	 */
	boolean set(T row) {
		return column.set(row, value.get());
	}
	
}
