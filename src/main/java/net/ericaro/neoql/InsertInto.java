package net.ericaro.neoql;

import java.util.Arrays;


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
		build();
	}

	 public <V> InsertInto<T> set(Column<T, V> col, V value) {
		int l = columnValuePairs.length;
		columnValuePairs = Arrays.copyOf(columnValuePairs, l + 1);
		columnValuePairs[l] = new ColumnValuePair<T, V>(col, value);
		return this;
	}

	 Class<T> getTable() {
		return table;
	}

	 ColumnValuePair<T, ?>[] getColumnValuePairs() {
		return columnValuePairs;
	}

	T build() {
		try {
			row = table.newInstance();
			return row;
		} catch (Exception e) {
			throw new NeoQLException(
					"Exception while instanciating row for table " + table, e);
		}

	}

	/** Return the row that will be inserted. The insertion is made actually when this statement is executed on a database
	 * 
	 * @return
	 */
	 T getRow() {
		for (ColumnValuePair s : columnValuePairs)
			s.getColumn().set(row, s.getValue());
		return row;
	}
	
	@Override
	public  void executeOn(Database database) {
		database.execute(this);
	}
}
