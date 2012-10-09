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
import net.ericaro.neoql.patches.PatchBuilder;
import edu.uci.ics.jung.graph.DirectedGraph;

/** Git provides an advanced usage mode for local model edition.
 * 	
 * To start with you must create a repository ( <code> new Repository()</code> ), then just clone it
 * <code> Git.clone( repo ) </code>
 * 
 * Like git we provide tags, Branches, commits, checkouts, merge.
 * Like any database we provide update insert delete for data into tables.
 * We provide very basic ability to query objects in the Git workspace. But we provide an external object, NeoQL that help
 * you in the task of creating advanced queries.
 * 
 * Putting your local model under the control of Git unleashes the real power of git !
 * 
 * @author eric
 *
 */
public class Git implements DDL, DML, DQL {
	private static Logger	LOG		= Logger.getLogger(Git.class.getName());
	private Database		db; // neoql db unique for a single git instance
	private Repository		repository; // can be shared amongs git instances.
	private Commit			head; // always the latest commit representing the database. That's why we cannot let user acces the database.
	private Branch			branch; // a simple commit handler, moved around when commiting.

	/** Creates a new Git Workspace sharing the repo.
	 * 
	 * @param repo
	 * @return
	 */
	public static final Git clone(Repository repo) {
		return new Git(repo);
	}

	Git(Repository repository) {// to be called only by checkouting the repo dude, and then checking out to some "master"
		this.db = new Database(); // always creates a local repo
		this.repository = repository;
		this.head = repository.getRoot();
		this.branch = new Branch(head);
	}

	/** commit the current changes with no messages.
	 * 
	 * @return
	 */
	public Commit commit() {
		return commit("");
	}
	/** commit current changes in the repository
	 * 
	 * @param comment
	 * @return
	 */
	public Commit commit(String comment) {
		LOG.fine("git commit -m \"" + comment + "\"");
		Patch commit = db.commit();
		if (commit != null ) {
			LOG.fine("commit "+String.valueOf(commit) );
			head = repository.commit(commit, head, comment);
			if (branch !=null ) branch.setCommit(head);
		}
		return head;
	}

	public Branch getBranch() {
		return branch;
	}

	public Branch createBranch() {
		return new Branch(head);
	}

	public Commit tag() {
		return head;
	}

	public void checkout(Commit tag) {
		LOG.fine("git checkout "+tag);
		for (Patch p : repository.path(head, tag)) {
			db.apply(p);
			LOG.fine(String.valueOf( p ));
		}
		head = tag;
		branch.setCommit(tag);
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

	
	/** computes the merge to be applied.
	 * 
	 * @param remote
	 * @return
	 */
	public Merge merge(Branch remote) {
		Commit remoteCommit = remote.getCommit();
		Commit localCommit = head;
		Commit base = repository.commonAncestor(localCommit, remoteCommit);
		if (base == localCommit || base == remoteCommit) // fast forward or nothing to update
			return new Merge(base, localCommit, remoteCommit, base);
		
		PatchBuilder remoteTransaction = repository.asPatchBuilder(base, remoteCommit);
		PatchBuilder localTransaction = repository.asPatchBuilder(base, localCommit);
		return new MergeBuilder(base, localCommit, localTransaction, remoteCommit, remoteTransaction).build();
	}
	
	
	public void apply(Merge merge) {
		if (merge.isNothingToUpdate() )
			System.out.println("nothing to update");
		else if (merge.isFastForward()) {
			checkout(merge.forward);
		}
		else {
			// built the left part and the right part
			Patch middle = merge.patchBuilder.build();
			Commit to = repository.merge(merge.base, merge.localHead, merge.remoteHead, middle, "auto merge");
			checkout(to);
		}
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

	public void checkout(Branch branch) {
		this.branch = branch;
		checkout(branch.getCommit());
	}
	
	public Branch checkoutNewBranch() {
		Branch b = new Branch(head);
		this.branch = b;
		return b;
	}

	@Override
	public boolean isClean() {
		return db.isClean();
	}

	public Patch reset() {
		return db.rollback();
		
	}

	// ##########################################################################
	// NeoQL Wrapping END
	// ##########################################################################

}