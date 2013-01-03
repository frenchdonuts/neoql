package net.ericaro.neoql.git;

import edu.uci.ics.jung.graph.DirectedGraph;
import net.ericaro.neoql.*;
import net.ericaro.neoql.eventsupport.GitListener;
import net.ericaro.neoql.eventsupport.GitListenerSupport;
import net.ericaro.neoql.patches.Patch;
import net.ericaro.neoql.patches.PatchBuilder;

import java.util.logging.Logger;

/** Git provides an advanced usage mode for local model edition.
 *
 * To start with you must create a repository ( <code> new Repository()</code> ), then just clone it
 * <code> Git.clone( repo ) </code>
 *
 * Like git we provide tags, Branches, commits, checkouts, merge.
 *
 * Like any neoql database we provide update insert delete for data into tables, this would be more like editing
 * the working directory files.
 * We provide very basic abilities to query objects in the Git workspace. But we provide an external object, NeoQL that help
 * you in the task of creating advanced queries.
 *
 * Putting your local model under the control of Git unleashes the real power of git !
 *
 * @author eric
 *
 */
public class Git implements DDL, DML, DQL {
	private static Logger	   LOG		   = Logger.getLogger(Git.class.getName());
	private Database           db;         // neoql db unique for a single git instance
	private Repository         repository; // common history to be shared amongs git instances.
	private Commit             head;       // always the latest commit representing the database. That's why we cannot let user access the database.
	private Branch             branch;     // a simple commit handler, moved around when commiting.
	private GitListenerSupport listeners;

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
		this.listeners = new GitListenerSupport();
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
		Patch patch = db.commit();
		if (patch != null ) {
			LOG.fine("commit "+String.valueOf(patch) );
			Commit c = repository.commit(patch, head, comment);
			fireHeadChanged(head, head = c);
			if (branch !=null ) branch.setCommit(c);
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

	public void checkout(Commit c) {
		LOG.fine("git checkout " + c);
		for (Patch p : repository.path(head, c)) {
			db.apply(p);
			LOG.fine(String.valueOf( p ));
		}
		fireHeadChanged(head, head = c);
		branch.setCommit(c);
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

	public <T> void delete(ContentTable<T> table, Predicate<? super T> predicate) {
		db.delete(table, predicate);
	}

	public <T> void update(ContentTable<T> table, Predicate<? super T> predicate, ColumnSetter<T, ?>... setters) {
		db.update(table, predicate, setters);
	}

	public <T> void update(ContentTable<T> table, Predicate<? super T> predicate, T t) {
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
		return merge(remote.getCommit());
	}

	/** computes the merge to be applied.
	 *
	 * @param remoteCommit
	 * @return
	 */
	public Merge merge(Commit remoteCommit) {
		Commit localCommit = head;
		Commit base = repository.commonAncestor(localCommit, remoteCommit);
		if (base == localCommit || base == remoteCommit) // fast forward or nothing to update
			return new Merge(base, localCommit, remoteCommit, base);

		// compact path between the common ancestor and the current position (in both branches)
		// so that every changes are compacted ( undo are skipped )
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
			// apply the merge, create the new node, and the new edges
			Commit to = repository.merge(merge.base, merge.localHead, merge.remoteHead, middle, "auto merge");
			// move the current head to this new checkout
			checkout(to);
		}
	}


	/** creates and retrieve a table. Warning, this methods cause a commit
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

	// ##########################################################################
	// EVENTS BEGIN
	// ##########################################################################

	public void addGitListener(GitListener l) {
		listeners.addGitListener(l);
	}

	public void removeGitListener(GitListener l) {
		listeners.removeGitListener(l);
	}

	protected void fireHeadChanged(Commit from, Commit to) {
		listeners.fireHeadChanged(from, to);
	}

	// ##########################################################################
	// EVENTS END
	// ##########################################################################

}
