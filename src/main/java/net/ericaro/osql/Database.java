package net.ericaro.osql;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;



public class Database {
	Map<Class, TableData> tables = new HashMap<Class, TableData>(); // real
																	// class ->
																	// table
																	// mapping

	protected <T> TableData<T> tableFor(Class<T> table) {
		return tables.get(table);
	}
	protected <T> Table<T> tableFor(TableDef<T> table) {
		return table.asTable(this);
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

	public <T> Iterable<T> select(Class<T> table) {
		return select(table, DQL.True);
	}
		
	public <T> Iterable<T> select(Class<T> table, Predicate<? super T> where) {
		return select(new Select<T>(table, where) );
	}
	
	public <T> Iterable<T> select(final Select<T> select) {
		return new Iterable<T>() {
			@Override public Iterator<T> iterator() {
				return new SelectIterator<T>(select, tableFor(select.getTable()));
			}};
	}
	
	public <T> Table<T> table(Select<T> select) {
		return new SelectTable<T>(select, tableFor(select.getTable()));
	}
	public <T> Table<T> table(Class<T> table) {
		return tableFor(table);
	}
	public <L,R> Table<Pair<L,R>> table(InnerJoin<L,R> innerjoin) {
		Table<L> left = innerjoin.getLeftTable().asTable(this);
		Table<R> right = innerjoin.getRightTable().asTable(this);
		return new InnerJoinTable<L,R>(left,right, innerjoin.getOn());
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
