package net.ericaro.neoql;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.ListModel;


public class Database {
	
	static Logger LOG = Logger.getLogger(Database.class.getName());
	
	// real class -> table mapping
	private Map<Class, TableData>			typed   = new HashMap<Class, TableData>();
//	private Map<Property, Singleton>		values	= new HashMap<Property, Singleton>();

	
	// TODO handle transaction ?
	// TODO handle undo/redo ? (isn't it related to transaction ? yes it is
	// TODO provide generic JTAble for every table for debug purpose )
	
	// TODO implement every possible joins (mainly outter join )
	// TODO find a way to express the definition of a singleton (or improve the existing one)
	
	// TODO do some stress test
	// TODO provide an SQL like toString for table def

	// ##########################################################################
	// PUBLIC API BEGIN
	// ##########################################################################

	// public API: mainly triggers visitor pattern on statements objects
	// except for the basic ones

	// should I add insert into here or not. Should I get the new instance private or not.
	
	
	// ##########################################################################
	// CREATE BEGIN
	// ##########################################################################
	
	/** creates a basic table in this database
	 * 
	 * @param table
	 * @return
	 */
	public <T> TableData<T>  createTable(ClassTableDef<T> table){
//		assert !tables.containsKey(table):"failed to create a table data that already exists";
		assert !typed.containsKey(table.getTable()):"failed to create a table data that already exists";
		
		LOG.fine("creating table "+ table);
		TableData<T> data = new TableData<T>(this, table);
//		this.tables.put(table, data);
		this.typed.put(table.getTable(), data);
		data.install();
		return data;
	}

	
	// ##########################################################################
	// CREATE END
	// ##########################################################################
	
	// ##########################################################################
	// INSERT BEGIN
	// ##########################################################################
	
	public <T> T insert(ColumnValue<T,?>... values){
		if (values.length == 0) return null;// nothing to do
		
		Class<T> type = values[0].column.getTable().getTable() ;
		TableData<T> data = typed.get(type);
		return data.insert(data.newInstance(values) );
	}
	
	
	// ##########################################################################
	// INSERT END
	// ##########################################################################
	
	// ##########################################################################
	// DELETE BEGIN
	// ##########################################################################
	
	public <T> void delete(T value){
		TableData<T> data = typed.get(value.getClass());
		Predicate<T> p = NeoQL.is(value);
		data.delete(p);
	}
	
	public <T> void delete(Class<T> type, Predicate<T> predicate){
		TableData<T> data = typed.get(type);
		data.delete(predicate);
	}
	
	
	// ##########################################################################
	// DELETE END
	// ##########################################################################
	// ##########################################################################
	// UPDATE BEGIN
	// ##########################################################################
	
	/** Update the given row. It creates automatically an identity predicate
	 * 
	 * @param oldValue
	 * @param values
	 */
	public <T> void update(T oldValue, ColumnValue<T,?>... values){
		TableData<T> data = typed.get(oldValue.getClass());
		Predicate<T> p = NeoQL.is(oldValue);
		_update(data, values, p);
	}
	
	/** Update rows matching the given predicate, with the given setters.
	 * 
	 * @param oldValue
	 * @param values
	 */
	public <T> void update(Class<T> type, Predicate<T> predicate, ColumnValue<T,?>... values){
		TableData<T> data = typed.get(type);
		_update(data, values, predicate);
	}
	
	/** actual update
	 * 
	 * @param data
	 * @param setters
	 * @param where
	 */
	private <T> void _update(TableData<T> data, ColumnValue<T, ?>[] setters,
			Predicate<? super T> where) {
		for (T row : data)
			if (where.eval(row)) {
				data.update(row, setters);
			}
		data.fireUpdate();
	}
	
	// ##########################################################################
	// UPDATE END
	// ##########################################################################

	
	
	// ##########################################################################
	// DROP BEGIN
	// ##########################################################################
	

	public <T> void drop(Table<T> table) {
		if (table instanceof TableData)
			this.typed.remove(((TableData)table).getType());
		table.drop();
	}

	// ##########################################################################
	// DROP END
	// ##########################################################################
	
	public <T, U extends ListModel&Iterable<T>> U listFor(Table<T> table) {
		return (U) new TableList<T>(table);
	}

	
	
	// clean in progress below
	public <T> Iterator<T> iterator(final Table<T> table) {
		return table.iterator();// reverse visitor pattern
	}

	public <T> Iterable<T> select(final Table<T> table) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return table.iterator();
			}
		};
	}

	public <T> T get(Singleton<T> prop) {
		return prop.get();
	}

	// execute a script

//	public void execute(Script script) {
//		script.executeOn(this);
//	}

	// ##########################################################################
	// ACCESS TABLES OBJECT END
	// ##########################################################################

	// ##########################################################################
	// EVENTS BEGIN
	// ##########################################################################

	public <T> void addTableListener(ClassTableDef<T> table, TableListener<T> listener) {
		typed.get(table.getTable()).addTableListener(listener);
	}

	public <T> void removeTableListener(ClassTableDef<T> table, TableListener<T> listener) {
		typed.get(table.getTable()).removeTableListener(listener);
	}

	<T> void addInternalTableListener(ClassTableDef<T> table, TableListener<T> listener) {
		typed.get(table.getTable()).addInternalTableListener(listener);
	}

	<T> void removeInternalTableListener(ClassTableDef<T> table, TableListener<T> listener) {
		typed.get(table.getTable()).removeInternalTableListener(listener);
	}

	public <T> void addPropertyListener(Singleton<T> prop, PropertyListener<T> l) {
		prop.addPropertyListener(l);
	}

	public <T> void removePropertyListener(Singleton<T> prop, PropertyListener<T> l) {
		prop.removePropertyListener(l);
	}

	// ##########################################################################
	// EVENTS END
	// ##########################################################################


	public <T> void put(Singleton<T> prop, T value) {
		prop.set(value);
	}

	// ##########################################################################
	// EXECUTE SCRIPT VISITOR PATTERN END
	// ##########################################################################

	// ##########################################################################
	// VISITOR CALL BACK FOR TABLE CREATION BEGIN
	// ##########################################################################

	

	// ##########################################################################
	// VISITOR CALL BACK FOR TABLE CREATION END
	// ##########################################################################


//	<T> Singleton<T> getSingleton(Property<T> prop) {
//		Singleton<T> ton = values.get(prop);
//		return ton;
//	}
}
