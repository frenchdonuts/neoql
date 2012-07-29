package net.ericaro.neoql;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.ericaro.neoql.changeset.Change;
import net.ericaro.neoql.changeset.ChangeSet;
import net.ericaro.neoql.changeset.ChangeVisitor;
import net.ericaro.neoql.eventsupport.PropertyListener;
import net.ericaro.neoql.eventsupport.TableListener;
import net.ericaro.neoql.eventsupport.TransactionListener;
import net.ericaro.neoql.eventsupport.TransactionListenerSupport;
import net.ericaro.neoql.tables.Pair;

public class Database implements DDL, DQL, DML, DTL {

	static Logger						LOG				= Logger.getLogger(Database.class.getName());

	boolean								autoCommit		= false;
	long								keySeed			= 0;											// default key

	// real class -> table mapping
	private Map<Class, ContentTable>	tables			= new HashMap<Class, ContentTable>();

	TransactionListenerSupport			support			= new TransactionListenerSupport();
	CommitBuilder						currentCommit	= new CommitBuilder(this);

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
	public <T> void atomicCreateTable(Class<T> table, Column<T, ?>... columns) {
		assert columns.length > 0 : "cannot create a table with no columns";
		LOG.fine("CREATE TABLE " + table); // always log before assert, so that assertion fail can be traced in the logs
		assert !tables.containsKey(table) : "failed to create a table data that already exists";
		assert allColumsAreOfType(table, columns) : "cannot create columns that do not have the same type";
		currentCommit.createTable(table, columns);
	}
	
	
	/** little convenient method, creates a cursor atomically, commit, and then rerieve the table. If you need
	 * fine control over the "commits" do no use this method.
	 * 
	 * @param table
	 * @return
	 */
	public <T> ContentTable<T> createTable(Class<T> table, Column<T, ?>... columns) {
		atomicCreateTable(table, columns );
		commit();
		return getTable(table);
	}
	

	private <T> void doCreateTable(Class<T> table, Column<T, ?>... columns) {
		// assert !tables.containsKey(table):"failed to create a table data that already exists";
		assert columns.length > 0 : "cannot create a table with no columns";

		LOG.fine("CREATE TABLE " + table); // always log before assert, so that assertion fail can be traced in the logs
		assert !tables.containsKey(table) : "failed to create a table data that already exists";
		assert allColumsAreOfType(table, columns) : "cannot create columns that do not have the same type";
		ContentTable<T> data = new ContentTable<T>(this, table, columns);
		doReuseTable(data);
	}

	private <T> void doReuseTable(ContentTable<T> data) {
		this.tables.put(data.getType(), data);
		data.install(); // let this table connect to others foreign key. This implies that foreign keys are already created, hence there is no dependency loop
	}

	private static <T> boolean allColumsAreOfType(Class<T> type, Column... columns) {
		for (Column c : columns)
			if (c.getTable() != type)
				return false;
		return true;
	}


	

	/**
	 * remove table from the database
	 * 
	 * @param tableType
	 */
	public <T> void dropTable(Class<T> tableType) {
		currentCommit.dropTable(tableType, getTable(tableType).columns);
		precommit();
	}

	private <T> void doDropTable(Class<T> tableType) {
		ContentTable<T> table = getTable(tableType);
		this.tables.remove(tableType);
		table.drop();
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
		return tables.get(type);
	}



	/**
	 * Return the actual ContentTables.
	 * 
	 * @param type
	 * @return
	 */
	@Override
	public Iterable<ContentTable> getTables() {
		return tables.values();
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
			T row = table.insert(table.newInstance(values));
			assert table.insertOperation != null : "unexpected empty transaction";
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

	// ##########################################################################
	// UPDATE END
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

		ChangeSet otx = currentCommit.build(); // flush buffers into the transaction tx
		currentCommit = new CommitBuilder(this);
		otx = apply(otx);
		return otx;
	}

	@Override
	public ChangeSet rollback() {
		ChangeSet tx = currentCommit.build();
		currentCommit = new CommitBuilder(this);
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
		return apply(new ChangeSet(c));
	}

	public ChangeSet apply(ChangeSet c) {
		ChangeSet tx = rollback(); // this has no effect if there was no incoming changes
		assert tx.isEmpty() : "cannot commit a changeset when there are local changes not yet applyed";
		c.accept(new ChangeVisitor() {

			// ##########################################################################
			// SPECIAL CASES BEGIN
			// ##########################################################################
			@Override
			public void changed(ChangeSet change) { // composite pattern
				for (Change c : change)
					c.accept(this);
			}

			// ##########################################################################
			// SPECIAL CASES END
			// ##########################################################################

			@Override
			public <T> void changed(final UpdateChange<T> change) {
				getTable(change.getType()).doCommit(change);
			}

			@Override
			public <T> void changed(DeleteChange<T> change) {
				getTable(change.getType()).doCommit(change);
			}

			@Override
			public <T> void changed(InsertChange<T> change) {
				getTable(change.getType()).doCommit(change);

			}

			@Override
			public void changed(CreateTableChange change) {
				for (Pair<Class, Column[]> key : change.created()) 
						doCreateTable(key.getLeft(), key.getRight());
			}

			@Override
			public void changed(DropTableChange change) {
				for (Pair<Class, Column[]> c : change.dropped())
					doDropTable(c.getLeft());
			}
		});

		// now clean the txTables
		support.fireCommitted(c);
		return c;
	}

	// ##########################################################################
	// HISTORY END
	// ##########################################################################

}
