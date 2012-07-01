package net.ericaro.neoql;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

import net.ericaro.neoql.changeset.ChangeSet;
import net.ericaro.neoql.eventsupport.TableListener;

public class Database {

	static Logger						LOG			= Logger.getLogger(Database.class.getName());

	// real class -> table mapping
	private Map<Class, ContentTable>	typed		= new HashMap<Class, ContentTable>();
	
	ChangeSet							tx			= new ChangeSet(new ChangeSet());				// trick to force a "root" changeset
	boolean								autocommit	= true;

	private Collection<PropertyRow>		properties	= new HashSet<PropertyRow>();
	// I need to track properties, because they hold one bit of information: the actual type they are tracking, this might change => it should be transactional.

	// TODO provide generic JTAble for every table for debug purpose )
	// TODO add unit tests
	// TODO implement every possible joins (mainly outter join )
	// TODO do some stress test

	// ##########################################################################
	// PUBLIC API BEGIN
	// ##########################################################################

	public Database() {
		super();
	}

	public Database(boolean autocommit) {
		super();
		this.autocommit = autocommit;
	}

	// ##########################################################################
	// CREATE BEGIN
	// ##########################################################################

	/**
	 * creates a content table, using the columns definition
	 * 
	 * @param columns column definition
	 * @return
	 */
	public <T> ContentTable<T> createTable(Column<T, ?>... columns) {
		// assert !tables.containsKey(table):"failed to create a table data that already exists";
		assert columns.length > 0 : "cannot create a table with no columns";
		Class<T> table = columns[0].getTable();

		LOG.fine("creating table " + table); // always log before assert, so that assertion fail can be traced in the logs
		assert !typed.containsKey(table) : "failed to create a table data that already exists";
		assert allColumsAreOfType(table, columns) : "cannot create columns that do not have the same type";

		ContentTable<T> data = new ContentTable<T>(this, table, columns);
		this.typed.put(table, data);
		data.install(); // let this table connect to others foreign key. This implies that foreign keys are already created, hence there is no dependency loop
		return data;
	}

	private static <T> boolean allColumsAreOfType(Class<T> type, Column... columns) {
		for (Column c : columns)
			if (c.getTable() != type)
				return false;
		return true;
	}
	
	/** Return the actual ContentTable associated with this type.
	 * 
	 * @param type
	 * @return
	 */
	public <T> ContentTable<T> get(Class<T> type) {
		return typed.get(type);
	}

	/** Creates a Property for a given table type. 
	 * A Property is an object that will return always the same row from a table.
	 * If the row changes in the table, so does the property.
	 * Start with a null row (i.e does nothing, until the value is set).
	 * @param type must correspond to an existing table type
	 * @return
	 */
	public <T> PropertyRow<T> createProperty(Class<T> type) {
		PropertyRow<T> s = new PropertyRow<T>(get(type));
		properties.add(s);
		return s;
	}
	
	/**
	 * creates and returns a table property initialized on the value
	 * 
	 * @param value
	 */
	public <T> Property<T> track(T value) {
		PropertyRow<T> prop = createProperty((Class<T>) value.getClass());
		prop.set(value);
		precommit();
		return prop;
	}

	public <T, V> Property<V> track(Property<T> row, Column<T, V> column) {
		return new PropertyColumn<T, V>(row, column);
	}

	

	// ##########################################################################
	// CREATE END
	// ##########################################################################

	// ##########################################################################
	// INSERT BEGIN
	// ##########################################################################

	/** insert a new default value in the table identified by its type.
	 * 
	 * @param type
	 * @return
	 */
	public <T> T insert(Class<T> type) {
		ContentTable<T> data = typed.get(type);
		T row = data.insert(data.newInstance());
		assert data.insertOperation != null : "unexpected empty transaction";
		precommit();
		return row;
	}
	/** Insert a new value in a table.
	 * The table is inferred from the column setters (they know their column type).
	 * 
	 * 
	 * @param values
	 * @return
	 */
	public <T> T insert(ColumnSetter<T, ?>... values) {
		if (values.length == 0)
			return null;// nothing to do

		Class<T> type = values[0].column.getTable();
		ContentTable<T> data = typed.get(type);
		T row = data.insert(data.newInstance(values));
		assert data.insertOperation != null : "unexpected empty transaction";
		precommit();
		return row;

	}

	// ##########################################################################
	// INSERT END
	// ##########################################################################

	// ##########################################################################
	// DELETE BEGIN
	// ##########################################################################

	/** delete the row pointed by the property.
	 * 
	 * @param value
	 */
	public <T> void delete(Property<T> value) {
		delete(value.getType(), NeoQL.is(value));
	}
	
	/** delete the value from its table.
	 * 
	 * @param value
	 */
	public <T> void delete(T value) {
		delete((Class<T>)value.getClass(), NeoQL.is(value));
	}

	/** generic delete: delete all values from the table 'type' that matches the predicate.
	 * 
	 * @param type
	 * @param predicate
	 */
	public <T> void delete(Class<T> type, Predicate<T> predicate) {
		ContentTable<T> data = typed.get(type);
		data.delete(predicate);
		assert data.deleteOperation != null : "unexpected null delete operation";
		precommit();
	}

	// ##########################################################################
	// DELETE END
	// ##########################################################################
	// ##########################################################################
	// UPDATE BEGIN
	// ##########################################################################
	
	/** Update the value denoted by the property setting values using the setters.
	 * 
	 * @param property
	 * @param setters
	 * @return
	 */
	public <T> T update(Property<T> property, ColumnSetter<T, ?>... setters) {
		update((Class<T>)property.getType(), NeoQL.is(property), setters);
		return property.get() ;// this is the new value
		
	}

	/** Update the value using the setters.
	 * 
	 * @param oldValue
	 * @param setters
	 * @return
	 */
	public <T> T update(T oldValue, ColumnSetter<T, ?>... setters) {
		Property<T> p = track(oldValue); // creates a temporary tracker
		T n = update(p, setters);
		drop(p);
		return n;
		

	}

	/**
	 * Update rows matching the given predicate, with the given setters.
	 * 
	 * @param type table to update
	 * @param predicate the predicate that triggers the update
	 * @param setters the action to take
	 */
	public <T> void update(Class<T> type, Predicate<T> predicate, ColumnSetter<T, ?>... setters) {
		ContentTable<T> data = typed.get(type);
		data.update(predicate, setters);
		assert data.updateOperation != null : "unexpected null update operation";
		precommit();
	}
	
	/** Update the cell referenced by this property.
	 * 
	 * @param prop
	 * @param value
	 */
	public <V, T> void update(PropertyColumn<V, T> prop, T value) {
		if ((value == null && prop.get() == null) || (value != null && value.equals(prop.get())))
			return; // nothing to do, it's the exact same value that is set
		update(prop.getRow(), prop.getColumn().set(value));
	}
	
	// ##########################################################################
	// UPDATE END
	// ##########################################################################

	// ##########################################################################
	// DROP BEGIN
	// ##########################################################################

	/** remove table from the database
	 * 
	 * @param tableType
	 */
	public <T> void drop(Class<T> tableType) {
		ContentTable<T> table = get(tableType);
		this.typed.remove(((ContentTable) table).getType());
		table.drop();
	}

	/** remove property from the database.
	 * 
	 * @param s
	 */
	public <T> void drop(Property<T> s) {
		s.drop();
	}

	// ##########################################################################
	// DROP END
	// ##########################################################################

	// ##########################################################################
	// PROPERTY EDIT BEGIN
	// ##########################################################################
	
	/** manually change the value of a given row property 
	 * 
	 * @param prop
	 * @param value
	 */
	public <T> void put(Property<T> prop, T value) {
		if (prop instanceof PropertyRow) {
			PropertyRow t = (PropertyRow) prop;
			put(t, value);
		} else
			throw new IllegalArgumentException("Cannot assign a value to a Property that is not either a Table or a Column one");
	}

	/** change the row this property is tracking. This a single bit of information, hence it cause a transaction.
	 * 
	 * @param prop
	 * @param value
	 */
	public <T> void put(PropertyRow<T> prop, T value) {
		prop.set(value);
		precommit();
	}


	// ##########################################################################
	// PROPERTY EDIT END
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

	public <T> void addPropertyListener(Property<T> prop, PropertyListener<T> l) {
		prop.addPropertyListener(l);
	}

	public <T> void removePropertyListener(Property<T> prop, PropertyListener<T> l) {
		prop.removePropertyListener(l);
	}

	// ##########################################################################
	// EVENTS END
	// ##########################################################################

	// ##########################################################################
	// TRANSACTIONS BEGIN
	// ##########################################################################

	/**
	 * collect changes from tables and store them in the current changeset
	 * 
	 */
	private void precommit() {
		if (autocommit)
			commit();

	}

	public ChangeSet commit() {
		for (PropertyRow s : properties) {
			tx.addChange(s.propertyChange);
			s.propertyChange = null;
		}

		// collect all "changes" in the tables
		for (ContentTable t : typed.values()) {
			tx.addChange(t.insertOperation);
			t.insertOperation = null;

			tx.addChange(t.updateOperation);
			t.updateOperation = null;

			tx.addChange(t.deleteOperation);
			t.deleteOperation = null;
		}

		ChangeSet otx = tx;
		tx = new ChangeSet(otx);
		otx.commit();
		return otx;
	}

	public ChangeSet revert() {
		ChangeSet otx = tx;
		tx = new ChangeSet(otx.getParent());
		return otx;
	}

	/**
	 * Enter staging mode: i.e do not auto commit
	 * 
	 */
	public void stage() {
		autocommit = false;
	}

	/**
	 * leave staging mode: commit and reset autocommit mode
	 * 
	 */
	public void unstage() {
		commit();
		autocommit = true;
	}

	/**
	 * Commit an alien changeset.
	 * assert that the current transaction is empty.
	 * 
	 * @param cs
	 */
	public void commit(ChangeSet cs) {
		assert tx.isEmpty() : "cannot commit a changeset when there are local changes not yet applyed";
		// I need to clone the cs, before applying.
		cs = cs.clone(tx);
		cs.commit();
		tx = new ChangeSet(cs);// creates a new empty tx, with this as parent

	}

	/** Reverts an alien changeset.
	 * 
	 * @param cs
	 * @return
	 */
	public ChangeSet revert(ChangeSet cs) {
		assert tx.isEmpty() : "cannot revert a changeset when there are local changes not yet applied";
		// assert tx.getParent() == cs : "cannot rebase change set";
		// this assertion was not correct (parent might get null if garbage collected)
		// but not totatlly wrong, I might have issues if trying to revert the wrong changeset.
		cs.revert();
		ChangeSet otx = tx;
		tx = new ChangeSet(cs.getParent()); // fork here
		return otx;
	}

	// ##########################################################################
	// TRANSACTIONS END
	// ##########################################################################

}
