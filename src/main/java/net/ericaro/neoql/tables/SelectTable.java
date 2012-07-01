package net.ericaro.neoql.tables;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.ericaro.neoql.Predicate;
import net.ericaro.neoql.Table;
import net.ericaro.neoql.eventsupport.TableListener;
import net.ericaro.neoql.eventsupport.TableListenerSupport;



public class SelectTable<T> implements Table<T> {

	private Table<T>				table;
	private TableListenerSupport<T>	events	= new TableListenerSupport<T>();

	private Predicate<? super T>	where;
	private TableListener<T>		listener;
	Set<T>							values	= new HashSet<T>();

	public SelectTable(Table<T> table, Predicate<? super T> where) {
		super();
		this.table = table;
		this.where = where;
		for (T t : table)
			if (where.eval(t))
				events.fireInserted(t); // cause events to be fire just like if the items where appended

		// first fill the filtered table
		// then add events to keep in touch with list content
		listener = new TableListener<T>() {

			public void inserted(T row) {
				if (where(row))
					whenInserted(row);
			}

			private void whenInserted(T row) {
				values.add(row);
				events.fireInserted(row);
			}

			public void deleted(T row) {
				if (where(row))
					whenDeleted(row);
			}

			private void whenDeleted(T row) {
				values.remove(row);
				events.fireDeleted(row);
			}

			public void updated(T old, T row) {
				boolean was = values.contains(old);
				boolean willbe = where(row);
				if (was && willbe) {
					// it was before, it will be after too, I need to update the content
					
					whenUpdated(old, row);
				} else {
					if (was)
						whenDeleted(old);
					if (willbe)
						whenInserted(row);
				}
				// if we add a "sort" algorithm, I would need to "workout" this a little bit
			}

			private void whenUpdated(T old, T row) {
				values.remove(old);
				values.add(row);
				events.fireUpdated(old, row);
			}
			@Override
			public void dropped(Table<T> table) {
				drop();
			}

		};
		table.addTableListener(listener);
	}

	protected boolean where(T row) {
		return where.eval(row);
	}

	void drop() {
		table.removeTableListener(listener);
		events.fireDrop(this);
	}

	@Override
	public Iterator<T> iterator() {
		return new SelectIterator<T>(table.iterator(), where);
	}

	public void addTableListener(TableListener<T> l) {
		events.addTableListener(l);
	}

	public void removeTableListener(TableListener<T> l) {
		events.removeTableListener(l);
	}

	public static class SelectIterator<T> extends Generator<T> {

		Iterator<T>						sub;
		private Predicate<? super T>	where;

		public SelectIterator(Iterator<T> table, Predicate<? super T> where) {
			super();
			sub = table;
			this.where = where;
		}
		protected T gen() throws StopIteration {
			while (sub.hasNext()) {
				T next = sub.next();
				if (where.eval(next))
					return next; // breaks
			}
			throw new StopIteration();
		}
	}

}