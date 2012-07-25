package net.ericaro.neoql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import net.ericaro.neoql.changeset.Change;
import net.ericaro.neoql.changeset.ChangeSet;
import net.ericaro.neoql.eventsupport.PropertyListener;
import net.ericaro.neoql.eventsupport.TableListener;
import net.ericaro.neoql.eventsupport.TransactionListener;
import net.ericaro.neoql.eventsupport.TransactionListenerSupport;
import net.ericaro.neoql.keys.ClassName;
import net.ericaro.neoql.keys.ColumnName;
import net.ericaro.neoql.properties.SingletonProperty;

public class Database implements DDL, DQL, DML, DTL {

	static Logger						LOG			= Logger.getLogger(Database.class.getName());

	// real class -> table mapping
	private Map<Class, ContentTable>	typed		= new HashMap<Class, ContentTable>();

	boolean								autoCommit	= false;

	// private Map<Class, SingletonProperty> singletons = new HashMap<Class, SingletonProperty>();

	private Set<Cursor>					rows		= new HashSet<Cursor>();
	TransactionListenerSupport			support		= new TransactionListenerSupport();

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

	// ##########################################################################
	// DDL BEGIN
	// ##########################################################################

	/**
	 * creates a content table, using the columns definition
	 * 
	 * @param columns
	 *            column definition
	 * @return
	 */
	@Override
	public <T> ContentTable<T> createTable(Class<T> table, Column<T, ?>... columns) {
		// assert !tables.containsKey(table):"failed to create a table data that already exists";
		assert columns.length > 0 : "cannot create a table with no columns";

		LOG.fine("CREATE TABLE " + table); // always log before assert, so that assertion fail can be traced in the logs
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

	/**
	 * Creates a Singleton Property. A Singleton Property holds a single value, and notify changes for it.
	 * 
	 * @param type
	 * @return
	 */
	// public <T> SingletonProperty<T> createSingletonProperty(Class<T> type) {
	// SingletonProperty<T> s = new SingletonProperty<T>(type);
	// singletons.put(type, s);
	// return s;
	// }

	/**
	 * Creates a Property for a given table type.
	 * A Property is an object that will return always the same row from a table.
	 * If the row changes in the table, so does the property.
	 * Start with a null row (i.e does nothing, until the value is set).
	 * 
	 * @param type
	 *            must correspond to an existing table type
	 * @return
	 */
	@Override
	public <T> Cursor<T> createCursor(Table<T> table) {
		Cursor<T> s = new Cursor<T>(table);
		rows.add(s);
		return s;
	}

	// ##########################################################################
	// DDL END
	// ##########################################################################

	// ##########################################################################
	// DQL BEGIN
	// ##########################################################################
	/**
	 * Return the actual ContentTable associated with this type.
	 * 
	 * @param type
	 * @return
	 */
	@Override
	public <T> ContentTable<T> getTable(Class<T> type) {
		return typed.get(type);
	}
	

	/**
	 * Return the actual ContentTables.
	 * 
	 * @param type
	 * @return
	 */
	@Override
	public Iterable<ContentTable> getTables() {
		return typed.values();
	}

//	@Override
//	public Iterable<SingletonProperty> getSingletons() {
//		return singletons.values();
//	}

	public Iterable<Cursor> getRows() {
		return Collections.unmodifiableCollection(rows);
	}

	// ##########################################################################
	// DQL END
	// ##########################################################################

	// ##########################################################################
	// INSERT BEGIN
	// ##########################################################################

	/**
	 * Insert a new value in a table.
	 * The table is inferred from the column setters (they know their column type).
	 * 
	 * 
	 * @param values
	 * @return
	 */
	@Override
	public <T> T insert(ContentTable<T> table, ColumnSetter<T, ?>... values) {
		if (values.length == 0) {
			T row = table.insert(table.newInstance());
			assert table.insertOperation != null : "unexpected empty transaction";
			precommit();
			return row;
		} else {
			Class<T> type = values[0].column.getTable();
			ContentTable<T> data = getTable(type);
			T row = data.insert(data.newInstance(values));
			assert data.insertOperation != null : "unexpected empty transaction";
			precommit();
			return row;
		}

	}

	// ##########################################################################
	// INSERT END
	// ##########################################################################

	// ##########################################################################
	// DELETE BEGIN
	// ##########################################################################

	/**
	 * generic delete: delete all values from the table 'type' that matches the predicate.
	 * 
	 * @param type
	 * @param predicate
	 */
	@Override
	public <T> void delete(ContentTable<T> table, Predicate<T> predicate) {
		table.delete(predicate);
		assert table.deleteOperation != null : "unexpected null delete operation";
		precommit();
	}

	// ##########################################################################
	// DELETE END
	// ##########################################################################
	// ##########################################################################
	// UPDATE BEGIN
	// ##########################################################################

	/**
	 * Update rows matching the given predicate, with the given setters.
	 * 
	 * @param type
	 *            table to update
	 * @param predicate
	 *            the predicate that triggers the update
	 * @param setters
	 *            the action to take
	 */
	@Override
	public <T> void update(ContentTable<T> table, Predicate<T> predicate, ColumnSetter<T, ?>... setters) {
		table.update(predicate, setters);
		assert table.updateOperation != null : "unexpected null update operation";
		precommit();
	}

	/**
	 * Update the cell referenced by this property.
	 * 
	 * @param prop
	 * @param value
	 */
//	@Override
//	public <T> T update(SingletonProperty<T> prop, T value) {
//		prop.set(value);
//		precommit();
//		return value;
//	}

	@Override
	public <T> void moveTo(Cursor<T> property, T value) {
		property.set(value);
		precommit();
	}

	// ##########################################################################
	// UPDATE END
	// ##########################################################################

	// ##########################################################################
	// DROP BEGIN
	// ##########################################################################

	/**
	 * remove table from the database
	 * 
	 * @param tableType
	 */
	public <T> void dropTable(Class<T> tableType) {
		ContentTable<T> table = getTable(tableType);
		this.typed.remove(tableType);
		table.drop();
	}

	/**
	 * remove property from the database.
	 * 
	 * @param s
	 */
	public <T> void dropCursor(Cursor<T> cursor) {
		rows.remove(cursor);
		cursor.drop();
	}

//	@Override
//	public <T> void dropSingletonProperty(Class<T> type) {
//		SingletonProperty<T> p = getSingletonProperty(type);
//		singletons.remove(type);
//		p.drop();
//
//	}

	// ##########################################################################
	// DROP END
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

	@Override
	public void addTransactionListener(TransactionListener l) {
		support.addTransactionListener(l);
	}

	@Override
	public void removeTransactionListener(TransactionListener l) {
		support.removeTransactionListener(l);
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
		if (autoCommit)
			commit();

	}

	@Override
	public Change commit() {

		ChangeSet otx = popChangeSet(); // flush buffers into the transaction tx
		otx.commit();
		support.fireCommitted(otx);
		return otx;
	}

	@Override
	public ChangeSet rollback() {
		ChangeSet tx = popChangeSet();
		if (!tx.isEmpty())
			support.fireRolledBack(tx);
		return tx;
	}

	@Override
	public boolean isAutocommit() {
		return autoCommit;
	}

	@Override
	public void setAutocommit(boolean autocommit) {
		this.autoCommit = autocommit;
	}

	/**
	 * remove all changes from local buffers and collect them into the current transaction
	 * 
	 * @return
	 * 
	 * @return
	 */
	private ChangeSet popChangeSet() {
		List<Change> tx = new ArrayList<Change>();
		for (Cursor s : rows) {
			tx.add(s.propertyChange);
			s.propertyChange = null;
		}
//		for (SingletonProperty s : singletons.values()) {
//			tx.add(s.propertyChange);
//			s.propertyChange = null;
//		}

		// collect all "changes" in the tables
		for (ContentTable t : typed.values()) {
			tx.add(t.insertOperation);
			t.insertOperation = null;

			tx.add(t.updateOperation);
			t.updateOperation = null;

			tx.add(t.deleteOperation);
			t.deleteOperation = null;
		}
		return new ChangeSet(tx);
	}

	// ##########################################################################
	// TRANSACTIONS END
	// ##########################################################################

	// ##########################################################################
	// HISTORY BEGIN
	// ##########################################################################
	/**
	 * Commit an alien changeset.
	 * assert that the current transaction is empty.
	 * 
	 * @param cs
	 * @return
	 */
	public ChangeSet apply(Change... c) {
		return apply(Arrays.asList(c));
	}

	public ChangeSet apply(Iterable<Change> c) {
		ChangeSet tx = rollback(); // this has no effect if there was no incoming changes
		assert tx.isEmpty() : "cannot commit a changeset when there are local changes not yet applyed";
		// I need to clone the cs, before applying.
		ChangeSet cs = new ChangeSet(c);
		cs.commit();
		support.fireCommitted(cs);
		return cs;
	}

	public ChangeSet revert(Change... c) {
		return revert(Arrays.asList(c));
	}

	public ChangeSet revert(Iterable<Change> c) {
		ChangeSet tx = rollback(); // this has no effect if there was no incoming changes
		assert tx.isEmpty() : "cannot commit a changeset when there are local changes not yet applyed";
		// I need to clone the cs, before applying.
		ChangeSet cs = new ChangeSet(c);
		cs.revert();
		support.fireReverted(cs);
		return cs;
	}

	// ##########################################################################
	// HISTORY END
	// ##########################################################################

}
