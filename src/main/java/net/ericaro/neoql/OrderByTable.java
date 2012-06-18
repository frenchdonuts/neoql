package net.ericaro.neoql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.ericaro.neoql.eventsupport.TableListener;
import net.ericaro.neoql.eventsupport.TableListenerSupport;



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

	public static final class ColumnComparator<T, V extends Comparable<? super V>> implements Comparator<T> {
		Column<T,V> orderByColumn;
		private int inf;
		
		public ColumnComparator(Column<T, V> orderByColumn, boolean ascendent) {
			super();
			this.orderByColumn = orderByColumn;
			this.inf = (ascendent?1:-1);
		}

		@Override
		public int compare(T t1, T t2) {
			V v1 = orderByColumn.get(t1);
			V v2 = orderByColumn.get(t2);
			if (v1 == null || v2 == null)
				if (v1 == null && v2 == null)
					return 0;
				else
					return v1 == null? -inf : inf;
			return inf*v1.compareTo(v2);
		}
	}

	public static class OrderByIterator<T, V extends Comparable<? super V> > implements Iterator<T> {

		private Iterator<T>	source;

		public OrderByIterator(Iterator<T> iterator, Column<T, V> orderBy, boolean ascendent) {
			List<T> unsorted = new ArrayList<T>();
			while(iterator.hasNext())
				unsorted.add(iterator.next());
			Collections.sort(unsorted, new ColumnComparator<T, V>(orderBy, ascendent));
			source = unsorted.iterator() ;
		}

		public boolean hasNext() {
			return source.hasNext();
		}

		public T next() {
			return source.next();
		}

		public void remove() {
			source.remove();
		}
		
	}

	private Table<T> table;
	private TableListenerSupport<T> events = new TableListenerSupport<T>();
	private TableListener<T> listener;
	private List<T> content = new ArrayList<T>();
	private Comparator<? super T> comparator;

	public OrderByTable(Table<T> table, Column<T,V> orderBy , boolean ascendent) {
		super();
		this.table = table;
		comparator = new ColumnComparator<T, V>(orderBy, ascendent);

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
			@Override
			public void dropped(Table<T> table) {
				drop();
			}
		};

		// fake call to fireinserted
		for (T r : table)
			listener.inserted(r); // cause events to be fire just like if the items where appended
		table.addTableListener(listener); // register to actual changes after the loop so that no changes can be tested twice
	}

	@Override
	public void drop() {
		this.content.clear();
		table.removeTableListener(listener);
		events.fireDrop(this);
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
