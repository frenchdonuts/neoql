package net.ericaro.neoql;

import java.util.Arrays;


/**
 * INSERT INTO table set column = value, column= value
 * 
 * @author eric
 * 
 */
public class InsertInto<T> implements Statement {

	private ClassTableDef<T> table;
	private ColumnValue<T, ?>[] columnValuePairs = new ColumnValue[0];
	T row;

	InsertInto(ClassTableDef<T> type) {
		super();
		this.table = type;
	}

	public <V> InsertInto<T> set(Column<T, V> col, V value) {
		int l = columnValuePairs.length;
		columnValuePairs = Arrays.copyOf(columnValuePairs, l + 1);
		columnValuePairs[l] = new ColumnValue<T, V>((ColumnImpl<T, V>) col, value);
		return this;
	}

	public ClassTableDef<T> getTable() {
		return table;
	}

	ColumnValue<T, ?>[] getColumnValuePairs() {
		return columnValuePairs;
	}

	public T build() {
			row = table.newInstance();
			for (ColumnValue s : columnValuePairs)
				s.set(row);
			return row;
		

	}

	/**
	 * Return the row that will be inserted. The insertion is made actually when
	 * this statement is executed on a database
	 * 
	 * @return
	 */
	public T getRow() {
		if (row == null)
			build();
		return row;
	}

	@Override
	public void executeOn(Database database) {
		database.execute(this);
	}
}
