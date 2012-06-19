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
	private Map<Class, TableData>	typed		= new HashMap<Class, TableData>();
	ChangeSet						tx			= new ChangeSet(new ChangeSet()); // trick to force a "root" changeset
	boolean							autocommit	= true;

	private Collection<Singleton>	singletons  = new HashSet<Singleton>();
	
    
//	private Map<Property, Singleton>		values	= new HashMap<Property, Singleton>();

	// observable transaction, and also handle transactions for the singletons
	// TODO handle undo/redo ? (isn't it related to transaction ? yes it is
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
	public <T> TableData<T>  createTable(Column<T,?>... columns){
//		assert !tables.containsKey(table):"failed to create a table data that already exists";
		assert columns.length>0 : "cannot create a table with no columns";
		Class<T> table = columns[0].getTable();
		
		assert !typed.containsKey(table):"failed to create a table data that already exists";
		assert allTypes(table, columns) : "cannot create columns that do not have the same type";
			
		
		LOG.fine("creating table "+ table);
		TableData<T> data = new TableData<T>(this, table, columns);
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
		
	
	public <T> Singleton<T> createSingleton(Class<T> type){
		Singleton<T> s = new Singleton<T>(get(type));
		singletons.add(s);
		return s;
	}

	
	public Collection<Singleton> getSingletons(){
		return Collections.unmodifiableCollection(singletons);
	}
	
	public <T> Collection<Singleton<T>> getSingletons(Class<T> type){
		Collection<Singleton<T>> singletons = new HashSet<Singleton<T>>();
		for(Singleton s: this.singletons)
			if (s.getType() == type )
				singletons.add(s);
		return Collections.unmodifiableCollection(singletons);
	}
	
	
	public <T> TableData<T> get(Class<T> type){
		return typed.get(type);
	}

	
	// ##########################################################################
	// CREATE END
	// ##########################################################################
	
	// ##########################################################################
	// INSERT BEGIN
	// ##########################################################################
	
	public <T> T insert(ColumnValue<T,?>... values){
		if (values.length == 0) return null;// nothing to do
		
		Class<T> type = values[0].column.getTable() ;
		TableData<T> data = typed.get(type);
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
		TableData<T> data = typed.get(value.getClass());
		Predicate<T> p = NeoQL.is(value);
		data.delete(p);
		assert data.deleteOperation !=null : "unexpected null delete operation";
		precommit();
	}
	
	public <T> void delete(Class<T> type, Predicate<T> predicate){
		TableData<T> data = typed.get(type);
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
	public <T> T update(Singleton<T> oldValue, ColumnValue<T,?>... values){
		return update(oldValue.get(), values);
	}
	
	/** Update the given row. It creates automatically an identity predicate
	 * 
	 * @param oldValue
	 * @param values
	 * @return 
	 */
	public <T> T update(T oldValue, ColumnValue<T,?>... values){
		TableData<T> data = typed.get(oldValue.getClass());
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
	public <T> void update(Class<T> type, Predicate<T> predicate, ColumnValue<T,?>... values){
		TableData<T> data = typed.get(type);
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
		if (table instanceof TableData)
			this.typed.remove(((TableData)table).getType());
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
		prop.set(value);
		precommit();
	}
	
	/** creates and returns a singleton initialized on the value
	 * 
	 * @param value
	 */
	public <T> Singleton<T> track(T value) {
		Singleton<T> prop = (Singleton<T>) createSingleton(value.getClass());
		prop.set(value);
		precommit();
		return prop;
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

	<T> void addInternalTableListener(TableData<T> table, TableListener<T> listener) {
		table.addInternalTableListener(listener);
	}

	<T> void removeInternalTableListener(TableData<T> table, TableListener<T> listener) {
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
		for(Singleton s : singletons) {
			tx.addChange(s.singletonChange);
			s.singletonChange = null;
		}
		
		// collect all "changes" in the tables
		for (TableData t: typed.values()) {
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
