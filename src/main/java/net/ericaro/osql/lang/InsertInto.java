package net.ericaro.osql.lang;

import java.util.Arrays;

import net.ericaro.osql.DQLException;
import net.ericaro.osql.system.Database;

/**
 * INSERT INTO table set column = value, column= value
 * 
 * @author eric
 * 
 */
public class InsertInto<T> implements Statement {

	private Class<T> table;
	private ColumnValuePair<T, ?>[] columnValuePairs = new ColumnValuePair[0];
	T row;

	public InsertInto(Class<T> type) {
		super();
		this.table = type;
		build();
	}

	public <V> InsertInto<T> set(Column<T, V> col, V value) {
		int l = columnValuePairs.length;
		columnValuePairs = Arrays.copyOf(columnValuePairs, l + 1);
		columnValuePairs[l] = new ColumnValuePair<T, V>(col, value);
		return this;
	}

	public Class<T> getTable() {
		return table;
	}

	public ColumnValuePair<T, ?>[] getColumnValuePairs() {
		return columnValuePairs;
	}

	T build() {
		try {
			row = table.newInstance();
			return row;
		} catch (Exception e) {
			throw new DQLException(
					"Exception while instanciating row for table " + table, e);
		}

	}

	/** Return the row that will be inserted. The insertion is made actually when this statement is executed on a database
	 * 
	 * @return
	 */
	public T getRow() {
		for (ColumnValuePair<T, ?> s : columnValuePairs)
			s.set(row);
		return row;
	}
	
	@Override
	public void executeOn(Database database) {
		database.execute(this);
	}
}
