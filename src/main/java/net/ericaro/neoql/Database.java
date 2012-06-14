package net.ericaro.neoql;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.ListModel;

import net.ericaro.neoql.lang.ClassTableDef;
import net.ericaro.neoql.lang.ColumnValue;
import net.ericaro.neoql.lang.CreateProperty;
import net.ericaro.neoql.lang.CreateTable;
import net.ericaro.neoql.lang.DeleteFrom;
import net.ericaro.neoql.lang.DropProperty;
import net.ericaro.neoql.lang.DropTable;
import net.ericaro.neoql.lang.GroupBySelect;
import net.ericaro.neoql.lang.InnerJoin;
import net.ericaro.neoql.lang.InsertInto;
import net.ericaro.neoql.lang.MapSelect;
import net.ericaro.neoql.lang.OrderBySelect;
import net.ericaro.neoql.lang.Script;
import net.ericaro.neoql.lang.Select;
import net.ericaro.neoql.lang.Update;
import net.ericaro.neoql.system.OrderByTable;
import net.ericaro.neoql.system.Pair;
import net.ericaro.neoql.system.Predicate;
import net.ericaro.neoql.system.Property;
import net.ericaro.neoql.system.PropertyListener;
import net.ericaro.neoql.system.Table;
import net.ericaro.neoql.system.TableDef;
import net.ericaro.neoql.system.TableList;
import net.ericaro.neoql.system.TableListener;

public class Database {
	
	static Logger LOG = Logger.getLogger(Database.class.getName());
	
	// real class -> table mapping
	private Map<ClassTableDef, TableData>	tables	= new HashMap<ClassTableDef, TableData>();
	private Map<Class, TableData>			typed   = new HashMap<Class, TableData>();
	
	private Map<TableDef, Table>	virtual	= new HashMap<TableDef, Table>();
	private Map<Property, Singleton>		values	= new HashMap<Property, Singleton>();

	
	// TODO separate table creation, drop from table get. 
	// TODO Add a garbage collector for unused tables
	// tables explicitly created cannot be garbage collected, but intermediate tables can.
	// TODO handle transaction ?
	// TODO handle undo/redo ? (isn't it related to transaction ? yes it is
	// TODO implement every possible joins
	// TODO find a way to express the definition of a singleton
	// for instance THE selected Item, so that it can be observed in the app, and reused
	// TODO oops, found a bug in the drop stuff. If I create a table to serve another one, I need to drop it too. (for instance select(mapper) creates a normal select, then mapper table, hence the "normal select is never deleted
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
	
	/** Creates a table
	 * 
	 * @param select
	 * @return
	 */
	public <T> Table<T> createTable(TableDef<T> table) {
		return table.asTable(this); // reverse visitor pattern
	}
	
	
	/** creates a basic table in this database
	 * 
	 * @param table
	 * @return
	 */
	public <T> TableData<T>  createTable(ClassTableDef<T> table){
		assert !tables.containsKey(table):"failed to create a table data that already exists";
		assert !typed.containsKey(table.getTable()):"failed to create a table data that already exists";
		
		LOG.fine("creating table "+ table);
		TableData<T> data = new TableData<T>(this, table);
		this.tables.put(table, data);
		this.typed.put(table.getTable(), data);
		data.install();
		return data;
	}
	
	private <T> Table<T> register(TableDef<T> table, Table<T> t){
		virtual.put(table, t);
		return t;
	}
	
	public <T> Table<T> createTable(Select<T> select) {
		return register(select, new SelectTable<T>(getOrCreate(select.getTable()), select.getWhere()));
	}

	public <S, T> Table<T> createTable(MapSelect<S, T> select) {
		return register(select, new MappedTable<S, T>(select.getMapper(), getOrCreate(select.getTable())));
	}

	public <S, T> Table<T> createTable(GroupBySelect<S, T> select) {
		return register(select, new GroupByTable<S, T>(select.getGroupBy(), getOrCreate(select.getTable())));
	}

	public <T, V extends Comparable<? super V>> Table<T> createTable(OrderBySelect<T, V> select) {
		return register(select, new OrderByTable<T, V>(getOrCreate(select.getTable()), select.getOrderBy(), select.isAscendent()));
	}

	public <L, R> Table<Pair<L, R>> createTable(InnerJoin<L, R> innerjoin) {
		Table<L> left =  getOrCreate(innerjoin.getLeftTable() );
		Table<R> right = getOrCreate(innerjoin.getRightTable() );
		return register(innerjoin, new InnerJoinTable<L, R>(left, right, innerjoin.getOn()));
	}
	
	// ##########################################################################
	// CREATE END
	// ##########################################################################
	
	// ##########################################################################
	// INSERT BEGIN
	// ##########################################################################
	
	public <T> T insert(T value){
		TableData<T> data = typed.get(value.getClass());
		data.insert(value);
		// TODO handle default values (like ids etc, so I need to 'update' the object a little bit and return it
		return value;
	}
	
	// ##########################################################################
	// INSERT END
	// ##########################################################################
	
	// ##########################################################################
	// DELETE BEGIN
	// ##########################################################################
	
	public <T> void delete(T value){
		TableData<T> data = typed.get(value.getClass());
		Predicate<T> p = data.getDef().is(value);
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
	
	public <T> void update(T oldValue, T newValue){
		TableData<T> data = typed.get(oldValue.getClass());
		Predicate<T> p = data.getDef().is(oldValue);
		data.update(oldValue, newValue );
		data.fireUpdate();
	}
	
	// ##########################################################################
	// DELETE END
	// ##########################################################################
	
	/** retrieve an existing table
	 * 
	 * @param table
	 * @return
	 */
	public <T> TableData<T> get(ClassTableDef<T> table){
		return tables.get(table);
	}
	
	/** retrieve an existing table
	 * 
	 * @param table
	 * @return
	 */
	public <T> Table<T> get(TableDef<T> table){
		if (table instanceof ClassTableDef)
			return get((ClassTableDef<T>) table);
		else
			return virtual.get(table);
	}
	
	
	<T> Table<T> create(ClassTableDef<T> table){
		if (tables.containsKey(table))
			return tables.get(table);
		else
			return createTable(table);
	}
	<T> Table<T> getOrCreate(TableDef<T> table){
		if (table instanceof ClassTableDef)
			return create( (ClassTableDef) table);
		else if (virtual.containsKey(table))
			return virtual.get(table);
		else{
			Table<T> t = table.asTable(this);
			virtual.put(table, t);
			return t;
		}
	}
	

	public <T> void drop(ClassTableDef<T> table) {
		
		tables.get(table).uninstall(); // I should actually trigger a delete table in all the dependncies
		this.tables.remove(table);
	}

	public <T> void drop(TableDef<T> table) {
		if (table instanceof ClassTableDef)
			drop( (ClassTableDef<T>) table);
		else if (virtual.containsKey(table)){ // here, removing a virtual table might trigger a garbage collect
			Table t = this.virtual.get(table);
			virtual.remove(table);
			t.drop(this);
		}
	}

	
	public <T, U extends ListModel&Iterable<T>> U listFor(TableDef<T> table) {
		return (U) new TableList<T>(get(table));
	}

	public <T> Iterator<T> iterator(final TableDef<T> table) {
		return table.iterator(this);// reverse visitor pattern
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

	public <T> void addTableListener(ClassTableDef<T> table, TableListener<T> listener) {
		get(table).addTableListener(listener);
	}

	public <T> void removeTableListener(ClassTableDef<T> table, TableListener<T> listener) {
		get(table).removeTableListener(listener);
	}

	<T> void addInternalTableListener(ClassTableDef<T> table, TableListener<T> listener) {
		get(table).addInternalTableListener(listener);
	}

	<T> void removeInternalTableListener(ClassTableDef<T> table, TableListener<T> listener) {
		get(table).removeInternalTableListener(listener);
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

	public <T> void execute(CreateTable<T> createTable) {

		TableDef<T> table = createTable.getTableDef();
		getOrCreate(table);
	}

	public <T> void execute(DropTable<T> dropTable) {

		ClassTableDef<T> table = dropTable.getTable();
		drop(table);
	}

	public <T> void execute(DeleteFrom<T> deleteFrom) {
		TableData<T> data = get(deleteFrom.getTable());
		data.delete(deleteFrom.getWhere());
	}

	// remove all execute from database, and move them to a 'statement visitor interface, let this databse have a statement visitor inner class instead'
	public <T> T execute(InsertInto<T> insertInto, T row) {
		TableData<T> data = get(insertInto.getTable());
		
		return data.insert(row);

	}

	public <T> void execute(Update<T> update) {
		TableData<T> data = get(update.getTable());
		ColumnValue<T, ?>[] setters = update.getColumnValuePairs();
		Predicate<? super T> where = update.getWhere();
		for (T row : data)
			if (where.eval(row)) {
				data.update(row, setters);
			}
		data.fireUpdate();
	}

	public <T> void execute(CreateProperty<T> createProperty) {
		Singleton<T> ton = new Singleton<T>(get(createProperty.getTable()));
		values.put(createProperty.getProperty(), ton);
	}

	public <T> void execute(DropProperty<T> prop) {
		Singleton<T> removed = values.remove(prop.getProperty());
		removed.dropTable();
	}

	public <T> void put(Property<T> prop, T value) {
		getSingleton(prop).set(value);
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

	// what is the source for a singleton ? for now the "latest" of a table
	// a table with a unique id
	// interesting... make it a table ?

	<T> Singleton<T> getSingleton(Property<T> prop) {
		Singleton<T> ton = values.get(prop);
		return ton;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (ClassTableDef x: tables.keySet())
			sb.append("CREATE TABLE ").append(x.toTableDefinition()).append("\n");
		for (TableDef x: virtual.keySet())
			sb.append("CREATE VIRTUAL TABLE ").append(x.toTableDefinition()).append("\n");
		return sb.toString();
	}
	
	

}
