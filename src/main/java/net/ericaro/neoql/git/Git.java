package net.ericaro.neoql.git;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;
import net.ericaro.neoql.Column;
import net.ericaro.neoql.ColumnSetter;
import net.ericaro.neoql.ContentTable;
import net.ericaro.neoql.Cursor;
import net.ericaro.neoql.DDL;
import net.ericaro.neoql.DML;
import net.ericaro.neoql.DQL;
import net.ericaro.neoql.Database;
import net.ericaro.neoql.Predicate;
import net.ericaro.neoql.Table;
import net.ericaro.neoql.changeset.Change;
import net.ericaro.neoql.changeset.Changes;
import net.ericaro.neoql.tables.Pair;

public class Git implements DDL, DML, DQL {
	// TODO handle branches (pointers moving with head, are branches shared among repositories ? yes)

	private static Logger	LOG		= Logger.getLogger(Git.class.getName());
	private Database		db;
	private Repository		repository;
	private Commit			head;
	private Branch			branch	= new Branch();

	public static final Git clone(Repository repo) {
		return new Git(repo);
	}

	Git(Repository repository) {// to be called only by checkouting the repo dude, and then checking out to some "master"
		this.db = new Database(); // always creates a local repo
		this.repository = repository;
		this.head = repository.getRoot();
	}

	public Commit commit() {
		return commit("");
	}

	public Commit commit(String comment) {
		LOG.fine("git commit -m \"" + comment + "\"");
		Change commit = db.commit();
		LOG.fine(Changes.toString(commit));
		head = repository.commit(commit, head, branch, comment);
		LOG.fine("END OF REPOSITORY TRANSACTION");

		return head;
	}

	public Branch getBranch() {
		return branch;
	}

	public Branch createBranch() {
		return branch = new Branch();
	}

	public Commit tag() {
		return head;
	}

	public void checkout(Commit tag) {
		for (Pair<Change, Commit> c : repository.path(head, tag)) {
			db.apply(c.getLeft());
			LOG.fine(Changes.toString(c.getLeft()));
			head = c.getRight();
			LOG.info("git new head " + head);
		}
		assert head == tag : "checkout failed to reach the asked tag " + head + " instead of " + tag;
	}

	public DirectedGraph<Commit, Change> getRepositoryGraph() {
		return repository.getGraph();
	}

	// ##########################################################################
	// NeoQL Wrapping BEGIN
	// ##########################################################################
	public <T> void atomicCreateTable(Class<T> table, Column<T, ?>... columns) {
		db.atomicCreateTable(table, columns);
	}

	public <T> ContentTable<T> getTable(Class<T> type) {
		return db.getTable(type);
	}

	public <T> Cursor<T> getCursor(Object key) {
		return db.getCursor(key);
	}

	public <T> T insert(ContentTable<T> table, ColumnSetter<T, ?>... values) {
		return db.insert(table, values);
	}

	public <T> void delete(ContentTable<T> table, Predicate<T> predicate) {
		db.delete(table, predicate);
	}

	public <T> void update(ContentTable<T> table, Predicate<T> predicate, ColumnSetter<T, ?>... setters) {
		db.update(table, predicate, setters);
	}

	public <T> void moveTo(Cursor<T> property, T value) {
		db.moveTo(property, value);
	}

	public <T> Object atomicCreateCursor(Class<T> table) {
		return db.atomicCreateCursor(table);
	}

	public <T> void atomicCreateCursor(Class<T> table, Object key) {
		db.atomicCreateCursor(table, key);
	}

	public <T> void dropCursor(Object key) {
		db.dropCursor(key);
	}

	public <T> void dropTable(Class<T> tableType) {
		db.dropTable(tableType);
	}

	public Iterable<ContentTable> getTables() {
		return db.getTables();
	}

	public Iterable<Cursor> getCursors() {
		return db.getCursors();
	}

	/** creates and retrieve a table. Warning, this methods provoque a commit
	 * 
	 * @param table
	 * @param columns
	 * @return
	 */
	public <T> ContentTable<T> createTable(Class<T> table, Column<T, ?>... columns) {
		db.atomicCreateTable(table, columns );
		commit("auto commit for table creation");
		return db.getTable(table);
	}

	/** convenient method to create a cursor, warning: causes a commit.
	 * 
	 * @param type
	 * @return
	 */
	public <T> Cursor<T> createCursor(Class<T> type) {
		Object newKey = db.atomicCreateCursor(type);
		commit("autocommit for cursor creation");
		return db.getCursor(newKey);
	}

	public <T> Cursor<T> createCursor(ContentTable<T> table) {
		return createCursor(table.getType());
	}

	// ##########################################################################
	// NeoQL Wrapping END
	// ##########################################################################

}