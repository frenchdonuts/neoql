package net.ericaro.neoql;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Database {
	// real class -> table mapping
	private Map<Class, TableData>	tables	= new HashMap<Class, TableData>();	

	// TODO find a way to "free" this list from the database (kind of close)
	// TODO append sort, and group by
	// TODO implement every possible joins
	// TODO rethink this all 'introspection' thing
	
	// ##########################################################################
	// ACCESS TABLES OBJECT BEGIN
	// ##########################################################################
		
	// tables are like select except that they keep in sync with the database content (observable) 
	
	public <T> TableData<T> tableFor(Class<T> table) {
		return tables.get(table);
	}

	public <T> Table<T> tableFor(TableDef<T> table) {
		return table.asTable(this);
	}
	
	public <T> TableList<T> listFor(Class<T> table) {
		return new TableList<T>(tableFor(table));
	}

	public <T> TableList<T> listFor(TableDef<T> table) {
		return new TableList<T>(tableFor(table));
	}	
	
	// ##########################################################################
	// ACCESS TABLES OBJECT END
	// ##########################################################################



	// ##########################################################################
	// EVENTS BEGIN
	// ##########################################################################
	
	public <T> void addTableListener(Class<T> table, TableListener<T> listener) {
		tableFor(table).addTableListener(listener);
	}

	public <T> void removeTableListener(Class<T> table, TableListener<T> listener) {
		tableFor(table).removeTableListener(listener);
	}

	<T> void addInternalTableListener(Class<T> table, TableListener<T> listener) {
		tableFor(table).addInternalTableListener(listener);
	}

	<T> void removeInternalTableListener(Class<T> table, TableListener<T> listener) {
		tableFor(table).removeInternalTableListener(listener);
	}
	// ##########################################################################
	// EVENTS END
	// ##########################################################################


	// ##########################################################################
	// EXECUTE SCRIPT VISITOR PATTERN BEGIN
	// ##########################################################################
	
	<T> void execute(CreateTable<T> createTable) {

		Class<T> table = createTable.getTable();
		TableData<T> data = new TableData<T>(this, table);
		this.tables.put(table, data);
		data.install();
	}
	<T> void execute(DropTable<T> dropTable) {

		Class<T> table = dropTable.getTable();
		tableFor(table).uninstall();
		this.tables.remove(table);
	}
	<T> void execute(DeleteFrom<T> deleteFrom) {
		TableData<T> data = tableFor(deleteFrom.getTable());
		data.delete(deleteFrom.getWhere());
	}

	<T> T execute(InsertInto<T> insertInto) {
		TableData<T> data = tableFor(insertInto.getTable());
		return data.insert(insertInto.getRow());

	}

	<T> void execute(Update<T> update) {
		Class<T> table = update.getType();
		TableData<T> data = tableFor(table);

		Predicate<? super T> where = update.getWhere();
		ColumnValuePair<T, ?>[] setters = update.getColumnValuePairs();
		for (T row : data)
			if (where.eval(row)) {
				data.update(row, setters);
			}
	}
	// ##########################################################################
	// EXECUTE SCRIPT VISITOR PATTERN END
	// ##########################################################################


	// select do not get stick to the database, once the iterator is executed everything is over
	
	// ##########################################################################
	// SELECTS BEGIN
	// ##########################################################################
		
	public <T> Iterable<T> select(Class<T> table) {
		return select(table, NeoQL.True);
	}

	public <T> Iterable<T> select(Class<T> table, Predicate<? super T> where) {
		final Select<T> select = new Select<T>(table, where);
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return new SelectIterator<T>(select, tableFor(select.getTable()));
			}
		};
	}
	// ##########################################################################
	// SELECTS END
	// ##########################################################################


	

	// ##########################################################################
	// VISITOR CALL BACK FOR TABLE CREATION BEGIN
	// ##########################################################################
		

	<T> Table<T> table(Select<T> select) {
		return new SelectTable<T>(select, tableFor(select.getTable()));
	}
	
	<S,T> Table<T> table(MapSelect<S,T> select) {
		SelectTable<S> table = new SelectTable<S>(select, tableFor(select.getTable()) );
		return new MappedTable<S,T>(select.getMapper(), table );
	}
	
	<T> Table<T> table(Class<T> table) {
		return tableFor(table);
	}

	<L, R> Table<Pair<L, R>> table(InnerJoin<L, R> innerjoin) {
		Table<L> left = innerjoin.getLeftTable().asTable(this);
		Table<R> right = innerjoin.getRightTable().asTable(this);
		return new InnerJoinTable<L, R>(left, right, innerjoin.getOn());
	}
	
	
		
	// ##########################################################################
	// VISITOR CALL BACK FOR TABLE CREATION END
	// ##########################################################################


	// execute a script
	
	public void execute(Script script) {
		script.executeOn(this);
	}

}
