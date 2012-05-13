package net.ericaro.osql.system;

import java.util.HashMap;
import java.util.Map;

import net.ericaro.osql.lang.ColumnValuePair;
import net.ericaro.osql.lang.CreateTable;
import net.ericaro.osql.lang.DeleteFrom;
import net.ericaro.osql.lang.InsertInto;
import net.ericaro.osql.lang.Predicate;
import net.ericaro.osql.lang.Select;
import net.ericaro.osql.lang.Update;

public class Database {
	Map<Class, TableData> tables = new HashMap<Class, TableData>(); // real
																	// class ->
																	// table
																	// mapping

	protected <T> TableData<T> tableFor(Class<T> table) {
		return tables.get(table);
	}

	public <T> void addTableListener(Class<T> table, TableListener<T> listener) {
		tableFor(table).addTableListener(listener);
	}

	public <T> void removeTableListener(Class<T> table,
			TableListener<T> listener) {
		tableFor(table).removeTableListener(listener);
	}

	public <T> void execute(CreateTable<T> createTable) {

		Class<T> table = createTable.getTable();
		TableData<T> data = new TableData<T>(this, table);
		this.tables.put(table, data);
	}

	public <T> void execute(DeleteFrom<T> deleteFrom) {
		TableData<T> data = tableFor(deleteFrom.getTable());
		data.delete(deleteFrom.getWhere());
	}

	public <T> T execute(InsertInto<T> insertInto) {
		TableData<T> data = tableFor(insertInto.getTable());
		return data.insert(insertInto.getRow());
		
	}

	public <T> SelectList<T> select(Select<T> select) {
		return new SelectList<T>(select, tableFor(select.getTable()));
	}

	public <T> void execute(Update<T> update) {
		Class<T> table = update.getType();
		TableData<T> data = tableFor(table);

		Predicate<? super T> where = update.getWhere();
		ColumnValuePair<T, ?>[] setters = update.getColumnValuePairs();
		for (T row : data)
			if (where.eval(row)) {
				T clone = data.clone(row);
				for (ColumnValuePair<T, ?> s : setters) {
					s.set(clone);
				}
				data.update(row, clone);
			}
	}

}
