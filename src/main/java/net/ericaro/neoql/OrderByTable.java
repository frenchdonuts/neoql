package net.ericaro.neoql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.ericaro.neoql.lang.Column;

public class OrderByTable<T,V extends Comparable<? super V> > implements Table<T> {
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

	private Table<T> table;
	private TableListenerSupport<T> events = new TableListenerSupport<T>();
	private Column<T, V> orderByColumn;
	private TableListener<T> listener;
	private List<T> content = new ArrayList<T>();
	private Comparator<? super T> comparator;

	OrderByTable(Table<T> table, Column<T,V> orderBy ) {
		super();
		this.table = table;
		this.orderByColumn = orderBy;
		comparator = new Comparator<T>() {

			@Override
			public int compare(T t1, T t2) {
				V v1 = orderByColumn.get(t1);
				V v2 = orderByColumn.get(t2);
				if (v1 == null || v2 == null)
					if (v1 == null && v2 == null)
						return 0;
					else
						return v1 == null? -1 : 1;
				return v1.compareTo(v2);
			}
		};

		// first fill the filtered table
		// then add events to keep in touch with list content
		listener = new TableListener<T>() {
			public void inserted(T row) {
				content.add(row);
				Collections.sort(content, comparator);
				events.fireInserted(row);
			}

			public void deleted(T row) {
				if (content.remove(row))
					events.fireDeleted(row);
			}

			public void updated(T old, T row) {
				boolean removed = content.remove(old);
				content.add(row);
				Collections.sort(content, comparator);
				if (removed)
					events.fireUpdated(old, row);
				else
					events.fireInserted(row);
			}
		};

		// fake call to fireinserted
		for (T r : table)
			listener.inserted(r); // cause events to be fire just like if the items where appended
		table.addTableListener(listener); // register to actual changes after the loop so that no changes can be tested twice
	}

	@Override
	public void drop(Database from) {
		table.removeTableListener(listener);
	}

	@Override
	public Iterator<T> iterator() {
		return content.iterator();
	}

	public void addTableListener(TableListener<T> l) {
		events.addTableListener(l);
	}

	public void removeTableListener(TableListener<T> l) {
		events.removeTableListener(l);
	}
}
