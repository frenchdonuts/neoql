package net.ericaro.neoql.lbr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.ericaro.neoql.Column;
import net.ericaro.neoql.ContentTable;
import net.ericaro.neoql.DDL;
import net.ericaro.neoql.DQL;
import net.ericaro.neoql.Database;
import net.ericaro.neoql.Property;
import net.ericaro.neoql.Cursor;
import net.ericaro.neoql.Table;
import net.ericaro.neoql.changeset.Change;
import net.ericaro.neoql.changeset.ChangeSet;
import net.ericaro.neoql.eventsupport.TransactionListener;
import net.ericaro.neoql.properties.SingletonProperty;

/**
 * handles three databases, a local one, a "proxy" of a remote one (called remote), and a "base" one. 
 * Every DDL instructions are sent to the three databases, so that
 * all three shares the same structure.
 * 
 * Local should receive every local edit<br/>
 * Remote is expected to track remote changes.<br/>
 * Base is used as the common ancestor for local and remote.
 * 
 * There are three "atomic" operations <ul>
 * <li>fetch: retrieve data from the actual remote database, and write them down into the "remote" database</li>
 * <li>update: apply changes present in the "remote" database into the local database. This operation is pure local.</li>
 * <li>checkout: force all three database to be equals to the "remote" one.</li>
 * </ul>
 * 
 * There are less "atomic" operations, like, <ul>
 * <li>save: fetch data from the database, update, and commit</li>
 * </ul>
 * 
 * @author eric
 * 
 */
public class LBRDatabase implements DDL {

	Database				local, base, remote;						// the three mountains of pleasure
	Operations				operations;
	private List<Change>	remoteChanges	= new ArrayList<Change>();
	private List<Change>	localChanges	= new ArrayList<Change>();

	public LBRDatabase() {
		local = new Database();
		base = new Database();
		remote = new Database();
		local.addTransactionListener(new TransactionListener() {
			@Override
			public void committed(Change change) {
				localChanges.add(change);
			}

			@Override
			public void reverted(Change change) {
				localChanges.remove(change);
			}

			@Override
			public void rolledBack(Change change) {	}
			
		});
		remote.addTransactionListener(new TransactionListener() {
			@Override
			public void committed(Change change) {
				remoteChanges.add(change);
			}
			@Override
			public void reverted(Change change) {
				remoteChanges.remove(change);
			}
			
			@Override
			public void rolledBack(Change change) {	}
			
		});
	}

	@Override
	public <T> ContentTable<T> createTable(Class<T> table, Column<T, ?>... columns) {
		base  .createTable(table, columns);
		remote.createTable(table, columns);
		return local.createTable(table, columns);
	}

	@Override
	public <T> Cursor<T> createCursor(Table<T> table) {// I don't like it, this is a fraud ! the cursor is associated to a table that do not belong to the database !! beurk !
		base  .createCursor(table );
		remote.createCursor(table );
		return local.createCursor(table);
	}



	/**
	 * access the local database.
	 * 
	 * @return
	 */
	public Database getLocal() {
		return local;
	}

	/**
	 * access the base database for read only
	 * 
	 * @return
	 */
	public DQL getBase() {
		return base;
	}

	/**
	 * access the remote database for read only
	 * 
	 * @return
	 */
	public DQL getRemote() {
		return remote;
	}

	public Operations getOperations() {
		return operations;
	}

	public void setOperations(Operations operations) {
		this.operations = operations;
	}

	/**
	 * update the proxy "remote" database (i.e the local database used to proxy the remote one) to match the actual remote database
	 * 
	 * @throws FetchException
	 * 
	 */
	public void fetch() throws FetchException {
		operations.fetch(remote);
		remote.commit();
	}
	
	public void commit() throws CommitException {
		operations.commit(local);
	}
	/** assume that the remote has been fetched, then apply as many changes from the remote one, to the local database.
	 * @throws MergeException 
	 * 
	 */
	public void update() throws MergeException {
		ChangeSet locals  = new ChangeSet(localChanges);
		ChangeSet remotes = new ChangeSet(remoteChanges);
		operations.merge(local, remote, base, locals, remotes);
	}
	/** force all databases to be equals to the remote one. All changes are lost
	 * 
	 */
	public void checkout() {
		ChangeSet remotes = new ChangeSet(remoteChanges);
		ChangeSet locals  = new ChangeSet(localChanges);
		local.revert(locals);
		local.apply(remotes);
		base.apply(remotes);
		localChanges.clear();
		remoteChanges.clear();
		
	}
	
	
	/** force a commit from the local database ( this is delegated to the Operation object).
	 * then fetch the changes, and check them out to all databases
	 * 
	 * @throws CommitException
	 * @throws FetchException
	 * @throws MergeException 
	 */
	public void save() throws CommitException, FetchException, MergeException {
		fetch();update();
		
		commit(); // cause the local database to be fully copied to the remote one
		
		fetch();checkout(); // force local sync
	}
	
	/** reset all local changes to match the remote database now.
	 * 
	 * @throws FetchException
	 */
	public void reset() throws FetchException {
		fetch();checkout() ;
	}

	
	
	
	

}
