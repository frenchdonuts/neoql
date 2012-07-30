package net.ericaro.neoql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.ericaro.neoql.changeset.Change;
import net.ericaro.neoql.changeset.ChangeSet;

/**
 * Open up a commit while in the transaction, then close it for ever as a "commit" object.
 * 
 * @author eric
 * 
 */
public class CommitBuilder {

	private DropTableChange		dropTableChange;
	private CreateTableChange	createTableChange;
	Database					owner;

	public CommitBuilder(Database owner) {
		super();
		this.owner = owner;
	}

	public <T> void createTable(Class<T> table, Column<T, ?>... columns) {
		if (createTableChange == null)
			createTableChange = new CreateTableChange();
		createTableChange.create(table, columns);

	}

	public <T> void dropTable(Class<T> tableType, Column<T, ?>... columns) {
		if (dropTableChange == null)
			dropTableChange = new DropTableChange();
		dropTableChange.drop(tableType, columns);
	}

	/**
	 * remove all changes from local buffers and collect them into the current transaction
	 * 
	 * @return
	 * 
	 * @return
	 */
	public ChangeSet build() {

		List<Change> tx = new ArrayList<Change>();
		if (createTableChange != null && !createTableChange.isEmpty())
			tx.add(createTableChange);
		if (dropTableChange != null && !dropTableChange.isEmpty())
			tx.add(dropTableChange);

		// collect all "changes" in the tables
		for (ContentTable t : owner.getTables()) {
			if (!(t.insertOperation == null || t.insertOperation.isEmpty()))
				tx.add(t.insertOperation);
			t.insertOperation = null;

			if (!(t.updateOperation == null || t.updateOperation.isEmpty()))
				tx.add(t.updateOperation);
			t.updateOperation = null;

			if (!(t.deleteOperation == null || t.deleteOperation.isEmpty()))
				tx.add(t.deleteOperation);
			t.deleteOperation = null;
		}

		createTableChange = null;
		dropTableChange = null;

		return new ChangeSet(tx);
	}

	static <U> Iterable<U> plus(final Iterable<U> u, final Iterable<U> v) {
		return new Iterable<U>() {

			@Override
			public Iterator<U> iterator() {
				return new Iterator<U>() {
					private Iterator<U>	iu;
					private Iterator<U>	iv;

					{
						iu = u.iterator();
						iv = v.iterator();
					}

					@Override
					public boolean hasNext() {
						return iu.hasNext() || iv.hasNext();
					}

					@Override
					public U next() {
						if (iu.hasNext())
							return iu.next();
						else
							return iv.next();
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

}
