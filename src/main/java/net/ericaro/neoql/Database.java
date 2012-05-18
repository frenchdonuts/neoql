package net.ericaro.neoql;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class Database {
	// real class -> table mapping
	private Map<Class, TableData>	tables	= new HashMap<Class, TableData>();
	private Map<Property, Singleton> values = new HashMap<Property, Singleton>();
	
	// TODO fix the package layout, and permissions (protected, package etc) 
	// TODO implement every possible joins
	// TODO rethink this all 'introspection' thing:
	// - columns are passed to the create table statement
	// - user handle the columns the way he wants
	// TODO find a way to express the definition of a singleton
	// for instance THE selected Item, so that it can be observed in the app, and reused
	// TODO oops, found a bug in the drop stuff. If I create a table to serve another one, I need to drop it too. (for instance select(mapper) creates a normal select, then mapper table, hence the "normal select is never deleted
	// TODO do some stress test
	
	// ##########################################################################
	// PUBLIC API BEGIN
	// ##########################################################################
	
	// public API: mainly triggers visitor pattern on statements objects
	// except for the basic ones

	/** Retrieve the primary table associated with a class (if exists)
	 * 
	 * @param table
	 * @return
	 */
	public <T> TableData<T> tableFor(Class<T> table) {
		return tables.get(table);
	}

	// TODO what the fuck ? retrieving, and creating is the same process ? It shouldn't of course
	// the doc is correct, not the code: the code says: create and retr
	/** Retrieve a table associated with this table definition
	 * 
	 * @param table
	 * @return
	 */
	public <T> Table<T> tableFor(TableDef<T> table) {
		return table.asTable(this);
	}

	public <T> void drop(Class<T> table) {
		tableFor(table).uninstall();
		this.tables.remove(table);
	}
	
	public <T> void drop(Table<T> table) {
		table.drop(this);
	}
	
	

	

	public <T> TableList<T> listFor(Class<T> table) {
		return new TableList<T>(tableFor(table));
	}

	public <T> TableList<T> listFor(TableDef<T> table) {
		return new TableList<T>(tableFor(table));
	}

	public <T> Iterator<T> iterator(Class<T> table) {

		return new ClassTableDef<T>(table).iterator(this);// reverse visitor pattern
	}

	public <T> Iterator<T> iterator(final TableDef<T> table) {
		return table.iterator(this);// reverse visitor pattern
	}

	public <T> Iterable<T> select(final Class<T> table) {
		return new Iterable<T>() {

			@Override
			public Iterator<T> iterator() {
				return Database.this.iterator(table);
			}

		};
	}

	public <T> Iterable<T> select(final TableDef<T> table) {
		return new Iterable<T>() {

			@Override
			public Iterator<T> iterator() {
				return Database.this.iterator(table);
			}

		};
	}
	
	public <T> T get(Property<T> prop) {
		return getSingleton(prop).get();
	}

	
	// execute a script

		public void execute(Script script) {
			script.executeOn(this);
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
	
	public <T> void addPropertyListener(Property<T> prop, PropertyListener<T> l) {
		getSingleton(prop).addPropertyListener(l);
	}
	public <T> void removePropertyListener(Property<T> prop, PropertyListener<T> l) {
		getSingleton(prop).removePropertyListener(l);
	}

	// ##########################################################################
	// EVENTS END
	// ##########################################################################

	// ##########################################################################
	// EXECUTE SCRIPT VISITOR PATTERN BEGIN
	// ##########################################################################

	<T> void execute(CreateTable<T> createTable) {

		Class<T> table = createTable.getTable();
		TableData<T> data = new TableData<T>(this, createTable);
		this.tables.put(table, data);
		data.install();
	}

	<T> void execute(DropTable<T> dropTable) {

		Class<T> table = dropTable.getTable();
		drop(tableFor(table));
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
		Class<T> table = update.getTable();
		TableData<T> data = tableFor(table);
		ColumnValue<T, ?>[] setters = update.getColumnValuePairs();
		Predicate<? super T> where = update.getWhere();
		for (T row : data)
			if (where.eval(row)) {
				data.update(row, setters);
			}
	}
	
	<T> void execute(CreateProperty<T> createProperty) {
		Singleton<T> ton = new Singleton<T>(tableFor(createProperty.getTable()));
		values.put(createProperty.getProperty(), ton);
	}
	
	<T> void execute(DropProperty<T> prop) {
		Singleton<T> removed = values.remove(prop.getProperty());
		removed.dropTable();
	}
	
	<T> void put(Property<T> prop, T value) {
		getSingleton(prop).set(value);
	} 
	
	

	// ##########################################################################
	// EXECUTE SCRIPT VISITOR PATTERN END
	// ##########################################################################

	
	// ##########################################################################
	// VISITOR CALL BACK FOR TABLE CREATION BEGIN
	// ##########################################################################

	public <T> Table<T> table(Class<T> table) {
		return tableFor(table);
	}

	public <T> Table<T> table(Select<T> select) {
		return new SelectTable<T>(tableFor(select.getTable()), select.getWhere());
	}

	public <S, T> Table<T> table(MapSelect<S, T> select) {
		SelectTable<S> table = new SelectTable<S>(tableFor(select.getTable()), select.getWhere());
		return new MappedTable<S, T>(select.getMapper(), table);
	}

	public <S, T> Table<T> table(GroupBySelect<S, T> select) {
		return new GroupByTable<S, T>(select.getGroupBy(), tableFor(select.getTable()));
	}
	public <T,V extends Comparable<? super V>> Table<T> table(OrderBySelect<T,V> select) {
		return new OrderByTable<T,V>(tableFor(select.getTable()), select.getOrderBy(), select.isAscendent() );
	}
	public <L, R> Table<Pair<L, R>> table(InnerJoin<L, R> innerjoin) {
		Table<L> left = innerjoin.getLeftTable().asTable(this);
		Table<R> right = innerjoin.getRightTable().asTable(this);
		return new InnerJoinTable<L, R>(left, right, innerjoin.getOn());
	}

	// ##########################################################################
	// VISITOR CALL BACK FOR TABLE CREATION END
	// ##########################################################################


	// what is the source for a singleton ? for now the "latest" of a table
		// a table with a unique id
		// interesting... make it a table ?
		
		
		
		<T> Singleton<T> getSingleton(Property<T> prop) {
			Singleton<T> ton = values.get(prop);
			return ton;
		}
		
		
		
		

}
