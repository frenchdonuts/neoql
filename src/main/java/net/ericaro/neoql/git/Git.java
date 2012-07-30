package net.ericaro.neoql.git;

import java.util.logging.Logger;

import net.ericaro.neoql.Column;
import net.ericaro.neoql.ColumnSetter;
import net.ericaro.neoql.ContentTable;
import net.ericaro.neoql.DDL;
import net.ericaro.neoql.DML;
import net.ericaro.neoql.DQL;
import net.ericaro.neoql.Database;
import net.ericaro.neoql.Predicate;
import net.ericaro.neoql.patches.Patch;
import net.ericaro.neoql.tables.Pair;
import edu.uci.ics.jung.graph.DirectedGraph;

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
		Patch commit = db.commit();
		LOG.fine(String.valueOf(commit));
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
		for (Pair<Patch, Commit> c : repository.path(head, tag)) {
			db.apply(c.getLeft());
			LOG.fine(String.valueOf(c.getLeft() ));
			head = c.getRight();
			LOG.info("git new head " + head);
		}
		assert head == tag : "checkout failed to reach the asked tag " + head + " instead of " + tag;
	}

	public DirectedGraph<Commit, Patch> getRepositoryGraph() {
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

	public <T> T insert(ContentTable<T> table, ColumnSetter<T, ?>... values) {
		return db.insert(table, values);
	}

	@Override
	public <T> T insert(ContentTable<T> table, T t) {
		return db.insert(table, t);
	}

	public <T> void delete(ContentTable<T> table, Predicate<T> predicate) {
		db.delete(table, predicate);
	}

	public <T> void update(ContentTable<T> table, Predicate<T> predicate, ColumnSetter<T, ?>... setters) {
		db.update(table, predicate, setters);
	}
	
	public <T> void update(ContentTable<T> table, Predicate<T> predicate, T t) {
		db.update(table, predicate, t);
	}

	public <T> void dropTable(Class<T> tableType) {
		db.dropTable(tableType);
	}

	public Iterable<ContentTable> getTables() {
		return db.getTables();
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

	// ##########################################################################
	// NeoQL Wrapping END
	// ##########################################################################

}