package net.ericaro.neoql;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Group By group items togethers
 * 
 * 
 */
public class GroupByTable<S, T> implements Table<T> {

	private Table<S>				table;
	private TableListenerSupport<T>	events		= new TableListenerSupport<T>();
	private Column<S, T>			groupByColumn;
	private TableListener<S>		listener;
	private Set<T>					equivalents	= new HashSet<T>();

	GroupByTable(Column<S, T> groupBy, Table<S> table) {
		super();
		this.table = table;
		this.groupByColumn = groupBy;

		// first fill the filtered table
		// then add events to keep in touch with list content
		listener = new TableListener<S>() {

			public void inserted(S row) {
				T v = groupByColumn.get(row);
				insertedByCol(v);
			}

			private void insertedByCol(T v) { // weird function, but this is the way I wan't to reuse it for the update, to avoid evaluatin get() twice
				if (equivalents.contains(v) )
					return ;
				equivalents.add(v);// add and fire
				events.fireInserted(v);
			}

			public void deleted(S row) {
				T v = groupByColumn.get(row);
				deletedByCol(v);
			}

			private void deletedByCol(T v) {
				for (S r : GroupByTable.this.table) {
					if (v.equals(groupByColumn.get(r))) // I'm not alone, cool
						return;
				}
				equivalents.remove(v);
				events.fireDeleted(v);
			}

			public void updated(S old, S row) {
				T vold = groupByColumn.get(row);
				T vnew = groupByColumn.get(row);

				if (vold.equals(vnew))
					return; // the new one is equivalent to the previous one, hence equivalent to others.
				deletedByCol(vold);
				insertedByCol(vnew);
			}
		};

		// fake call to fireinserted
		for (S r : table)
			listener.inserted(r); // cause events to be fire just like if the items where appended
		table.addTableListener(listener); // register to actual changes after the loop so that no changes can be tested twice
	}

	@Override
	public void drop(Database from) {
		table.removeTableListener(listener);
	}

	@Override
	public Iterator<T> iterator() {
		return equivalents.iterator();
	}

	public void addTableListener(TableListener<T> l) {
		events.addTableListener(l);
	}

	public void removeTableListener(TableListener<T> l) {
		events.removeTableListener(l);
	}

	public static class GroupByIterator<S, T> extends Generator<T> {

		Iterator<S>					isource;
		Column<S, T>				groupByColumn;
		private transient Set<T>	equivalents	= new HashSet<T>();

		public GroupByIterator(Iterator<S> isource, Column<S, T> groupByColumn) {
			super();
			this.isource = isource;
			this.groupByColumn = groupByColumn;
		}

		@Override
		protected T gen() throws StopIteration {
			while (isource.hasNext()) {
				S snext = isource.next();
				T next = groupByColumn.get(snext);
				if (!equivalents.contains(next)) {
					equivalents.add(next);// add and fire
					return next;
				}
			}
			throw new StopIteration();

		}
	}
}