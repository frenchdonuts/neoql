package net.ericaro.neoql.lang;

import java.util.Arrays;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.NeoQLException;

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

	InsertInto(Class<T> type) {
		super();
		this.table = type;
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

	ColumnValuePair<T, ?>[] getColumnValuePairs() {
		return columnValuePairs;
	}

	public T build() {
		try {
			row = table.newInstance();
			for (ColumnValuePair s : columnValuePairs)
				s.getColumn().set(row, s.getValue());
			return row;
		} catch (Exception e) {
			throw new NeoQLException(
					"Exception while instanciating row for table " + table, e);
		}

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
