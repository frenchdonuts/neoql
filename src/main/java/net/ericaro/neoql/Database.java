package net.ericaro.neoql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;



public class Database {
	
	static Logger LOG = Logger.getLogger(Database.class.getName());
	
	// real class -> table mapping
	private Map<Class, TableData>	typed		= new HashMap<Class, TableData>();
	ChangeSet						tx			= new ChangeSet(new ChangeSet()); // trick to force a "root" changeset
	boolean							autocommit	= true;
	
    
//	private Map<Property, Singleton>		values	= new HashMap<Property, Singleton>();

	
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
		
		Class<T> type = values[0].column.getTable().getTable() ;
		TableData<T> data = typed.get(type);
		T row = data.insert(data.newInstance(values) );
		assert data.insertOperation !=null:"unexpected empty transaction" ;
		tx.addChange(data.insertOperation);
		data.insertOperation = null;// clear the transaction locally
		if (autocommit) commit();
		return row;
		
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
		assert data.deleteOperation !=null : "unexpected null delete operation";
		tx.addChange(data.deleteOperation);
		data.deleteOperation = null;
		if (autocommit) commit();
	}
	
	public <T> void delete(Class<T> type, Predicate<T> predicate){
		TableData<T> data = typed.get(type);
		data.delete(predicate);
		assert data.deleteOperation !=null : "unexpected null delete operation";
		tx.addChange(data.deleteOperation);
		data.deleteOperation = null;
		if (autocommit) commit();
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
	 * @return 
	 */
	public <T> T update(T oldValue, ColumnValue<T,?>... values){
		TableData<T> data = typed.get(oldValue.getClass());
		T n = data.update(oldValue, values);
		assert data.updateOperation !=null : "unexpected null update operation";
		commitUpdate();
		return n;
		
	}

	private void commitUpdate() {
		List<UpdateChange> updates = new ArrayList<UpdateChange>();
		for (TableData t: typed.values())
			if (t.updateOperation !=null ) {
				updates.add(t.updateOperation) ;
				t.updateOperation = null ;
			}
		tx.addChange(updates.toArray(new Change[updates.size()]));
		if (autocommit) commit();
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
		commitUpdate();
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
	
	// ##########################################################################
	// SINGLETON EDIT BEGIN
	// ##########################################################################
	public <T> void put(Singleton<T> prop, T value) {
		prop.set(value);
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
	
	public ChangeSet commit() {
		tx.commit();
		ChangeSet otx = tx;
		tx = new ChangeSet(otx);
		return otx;
	}
	public ChangeSet revert() {
		ChangeSet otx = tx;
		tx = new ChangeSet(otx.getParent() );
		return otx;
	}
	
	
	public void commit(ChangeSet cs) {
		assert tx.isEmpty(): "cannot commit a changeset when there are local changes nnot yet applyed";
		assert cs.getParent() == tx.parent : "cannot rebase a change set";
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
