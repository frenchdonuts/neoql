package net.ericaro.neoql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.ericaro.neoql.changeset.ChangeSet;
import net.ericaro.neoql.eventsupport.TableListener;



public class Database {
	
	static Logger LOG = Logger.getLogger(Database.class.getName());
	
	// real class -> table mapping
	private Map<Class, ContentTable>	typed		= new HashMap<Class, ContentTable>();
	ChangeSet						tx			= new ChangeSet(new ChangeSet()); // trick to force a "root" changeset
	boolean							autocommit	= true;

	private Collection<RowSingleton>	singletons  = new HashSet<RowSingleton>();
	
    
//	private Map<Property, Singleton>		values	= new HashMap<Property, Singleton>();

	// TODO provide generic JTAble for every table for debug purpose )
	// TODO add unit tests
	
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
	
	public Database() {
		super();
	}
	
	public Database(boolean autocommit) {
		super();
		this.autocommit = autocommit ;
	}
	
	
	// ##########################################################################
	// CREATE BEGIN
	// ##########################################################################
	
	
	
	/** creates a basic table in this database
	 * 
	 * @param table
	 * @return
	 */
	public <T> ContentTable<T>  createTable(Column<T,?>... columns){
//		assert !tables.containsKey(table):"failed to create a table data that already exists";
		assert columns.length>0 : "cannot create a table with no columns";
		Class<T> table = columns[0].getTable();
		
		assert !typed.containsKey(table):"failed to create a table data that already exists";
		assert allTypes(table, columns) : "cannot create columns that do not have the same type";
			
		
		LOG.fine("creating table "+ table);
		ContentTable<T> data = new ContentTable<T>(this, table, columns);
//		this.tables.put(table, data);
		this.typed.put(table, data);
		data.install();
		return data;
	}
	
	private static <T> boolean allTypes(Class<T> type, Column... columns) {
		for(Column c: columns)
			if (c.getTable() != type) return false;
		return true;
	}
		
	
	public <T> RowSingleton<T> createSingleton(Class<T> type){
		RowSingleton<T> s = new RowSingleton<T>(get(type));
		singletons.add(s);
		return s;
	}

	
	public Collection<Singleton> getSingletons(){
		return Collections.unmodifiableCollection((Collection) singletons);
	}
	
	public <T> Collection<Singleton<T>> getSingletons(Class<T> type){
		Collection<Singleton<T>> singletons = new HashSet<Singleton<T>>();
		for(Singleton s: this.singletons)
			if (s.getType() == type )
				singletons.add(s);
		return Collections.unmodifiableCollection(singletons);
	}
	
	
	public <T> ContentTable<T> get(Class<T> type){
		return typed.get(type);
	}

	
	// ##########################################################################
	// CREATE END
	// ##########################################################################
	
	// ##########################################################################
	// INSERT BEGIN
	// ##########################################################################
	
	public <T> T insert(ColumnSetter<T,?>... values){
		if (values.length == 0) return null;// nothing to do
		
		Class<T> type = values[0].column.getTable() ;
		ContentTable<T> data = typed.get(type);
		T row = data.insert(data.newInstance(values) );
		assert data.insertOperation !=null:"unexpected empty transaction" ;
		precommit();
		return row;
		
	}
	
	
	// ##########################################################################
	// INSERT END
	// ##########################################################################
	
	// ##########################################################################
	// DELETE BEGIN
	// ##########################################################################
	
	public <T> void delete(Singleton<T> value){
		delete(value.get());
	}
	public <T> void delete(T value){
		ContentTable<T> data = typed.get(value.getClass());
		Predicate<T> p = NeoQL.is(value);
		data.delete(p);
		assert data.deleteOperation !=null : "unexpected null delete operation";
		precommit();
	}
	
	public <T> void delete(Class<T> type, Predicate<T> predicate){
		ContentTable<T> data = typed.get(type);
		data.delete(predicate);
		assert data.deleteOperation !=null : "unexpected null delete operation";
		precommit();
	}
	
	
	// ##########################################################################
	// DELETE END
	// ##########################################################################
	// ##########################################################################
	// UPDATE BEGIN
	// ##########################################################################
	public <T> T update(Singleton<T> oldValue, ColumnSetter<T,?>... values){
		return update(oldValue.get(), values);
	}
	
	/** Update the given row. It creates automatically an identity predicate
	 * 
	 * @param oldValue
	 * @param values
	 * @return 
	 */
	public <T> T update(T oldValue, ColumnSetter<T,?>... values){
		ContentTable<T> data = typed.get(oldValue.getClass());
		T n = data.update(oldValue, values);
		assert data.updateOperation !=null : "unexpected null update operation";
		precommit();
		return n;
		
	}

	
	
	/** Update rows matching the given predicate, with the given setters.
	 * 
	 * @param oldValue
	 * @param values
	 */
	public <T> void update(Class<T> type, Predicate<T> predicate, ColumnSetter<T,?>... values){
		ContentTable<T> data = typed.get(type);
		data.update(predicate, values);
		assert data.updateOperation !=null : "unexpected null update operation";
		precommit();
	}
	
	
	// ##########################################################################
	// UPDATE END
	// ##########################################################################

	
	
	// ##########################################################################
	// DROP BEGIN
	// ##########################################################################
	

	public <T> void drop(Table<T> table) {
		if (table instanceof ContentTable)
			this.typed.remove(((ContentTable)table).getType());
		table.drop();
	}
	
	public <T> void drop(Singleton<T> s) {
		s.drop();
	}
	/** drop all singletons for a given type
	 * 
	 * @param type
	 */
	public <T> void drop(Class<T> type) {
		for(Singleton<T> s: getSingletons(type) )
			s.drop();
	}

	// ##########################################################################
	// DROP END
	// ##########################################################################
	
	// ##########################################################################
	// SINGLETON EDIT BEGIN
	// ##########################################################################
	public <T> void put(Singleton<T> prop, T value) {
		if (prop instanceof RowSingleton) {
			RowSingleton t = (RowSingleton) prop;
			put(t, value);
		}
		else if (prop instanceof ColumnSingleton) {
			ColumnSingleton t = (ColumnSingleton) prop;
			put(t, value);
		}
		else throw new IllegalArgumentException("Cannot assign a value to a Singleton that is not either a Table or a Column one");
	}
	
	public <T> void put(RowSingleton<T> prop, T value) {
		prop.set(value);
		precommit();
	}
	public <V,T> void put(ColumnSingleton<V,T> prop, T value) {
		if (
				(value == null && prop.get() == null)
				|| 
				(value!=null && value.equals( prop.get() ) )
			)
			return; // nothing to do, it's the exact same value that is set
		update(prop.getRow(), prop.getColumn().set(value) );
		precommit();
	}
	/** creates and returns a table singleton initialized on the value
	 * 
	 * @param value
	 */
	public <T> Singleton<T> track(T value) {
		RowSingleton<T> prop =  createSingleton((Class<T>) value.getClass());
		prop.set(value);
		precommit();
		return prop;
	}
	
	public <T,V> Singleton<V> track(Singleton<T> row, Column<T,V> column) {
		return new ColumnSingleton<T,V>(row, column);
	}
	
	
	public <T> T get(Singleton<T> prop) {
		return prop.get();
	}
	
	// ##########################################################################
	// SINGLETON EDIT END
	// ##########################################################################
	
	
	
	

	// ##########################################################################
	// EVENTS BEGIN
	// ##########################################################################

	public <T> void addTableListener(Table<T> table, TableListener<T> listener) {
		table.addTableListener(listener);
	}

	public <T> void removeTableListener(Table<T> table, TableListener<T> listener) {
		table.removeTableListener(listener);
	}

	<T> void addInternalTableListener(ContentTable<T> table, TableListener<T> listener) {
		table.addInternalTableListener(listener);
	}

	<T> void removeInternalTableListener(ContentTable<T> table, TableListener<T> listener) {
		table.removeInternalTableListener(listener);
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


	
	// ##########################################################################
	// TRANSACTIONS BEGIN
	// ##########################################################################
	
	/** collect changes from tables and store them in the current changeset
	 * 
	 */
	private void precommit() {
		if (autocommit) commit();
		
		
		
	}
	
	public ChangeSet commit() {
		// also collect changes from singletons
		for(RowSingleton s : singletons) {
			tx.addChange(s.singletonChange);
			s.singletonChange = null;
		}
		
		// collect all "changes" in the tables
		for (ContentTable t: typed.values()) {
			tx.addChange(t.insertOperation) ;
			t.insertOperation = null;
			
			tx.addChange(t.updateOperation) ;
			t.updateOperation = null;
			
			tx.addChange(t.deleteOperation) ;
			t.deleteOperation = null;
		}
		
		ChangeSet otx = tx;
		tx = new ChangeSet(otx);
		otx.commit();
		return otx;
	}
	public ChangeSet revert() {
		ChangeSet otx = tx;
		tx = new ChangeSet(otx.getParent() );
		return otx;
	}
	
	public void stage() {
		autocommit = false;
	}
	public void unstage() {
		autocommit = true;
	}
	
	public void commit(ChangeSet cs) {
		assert tx.isEmpty(): "cannot commit a changeset when there are local changes nnot yet applyed";
		cs.commit();
		tx = new ChangeSet(cs);
	}
	public ChangeSet revert(ChangeSet cs) {
		assert tx.isEmpty(): "cannot revert a changeset when there are local changes nnot yet applied";
		assert tx.getParent() == cs : "cannot rebase change set";
		cs.revert();
		ChangeSet otx = tx;
		tx = new ChangeSet(cs.getParent()); // fork here
		return otx;
	}
	
	
	
	// ##########################################################################
	// TRANSACTIONS END
	// ##########################################################################


}
