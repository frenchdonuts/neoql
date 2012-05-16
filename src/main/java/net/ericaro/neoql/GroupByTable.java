package net.ericaro.neoql;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.ericaro.neoql.lang.Column;

/**
 * Group By group items togethers
 * 
 * 
 */
class GroupByTable<S, T> implements Table<T> {
	// TODO handle counts
	// theory
	/*
	 * An equivalence relation is passed to this table. A Set is maintained with
	 * a unique representant per equivalence classes.
	 * 
	 * row addition: the new row relation to every representant is evaluated. If
	 * none is a match, then, the row is considered as the representant of a new
	 * class. row update: considered as a remove then a add. row remove: if the
	 * row is one of the representant, then one other representant is look for.
	 * If one is found, it is used instead, if none is found the representent is
	 * removed (and an event is fired)
	 */

	private Table<S> table;
	private TableListenerSupport<T> events = new TableListenerSupport<T>();
	private Column<S, T> groupByColumn;
	private TableListener<S> listener;
	private Set<T> equivalents = new HashSet<T>();

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
				for (T eq : equivalents)
					if (v.equals(eq))
						return; // not real insertion

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
}