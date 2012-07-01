package net.ericaro.neoql.tables;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.ericaro.neoql.Table;
import net.ericaro.neoql.eventsupport.TableListener;
import net.ericaro.neoql.eventsupport.TableListenerSupport;

public class MergedTable<T> implements Table<T> {

	protected Table<? extends T>[]			tables;
	protected TableListenerSupport<T>		events	= new TableListenerSupport<T>();
	protected TableListener<? extends T>	listener;
	Set<T>									values	= new HashSet<T>();

	public MergedTable(Table<? extends T>... tables) {
		super();
		this.tables = tables;
		for (Table<? extends T> table : tables)
			for (T t : table)
				events.fireInserted(t); // cause events to be fire just like if the items where appended
		
		listener = new TableListener<T>() {

			public void inserted(T row) {
				values.add(row);
				events.fireInserted(row);
			}

			public void deleted(T row) {
					values.remove(row);
					events.fireDeleted(row);
			}
			
			public void updated(T old, T row) {
				values.remove(old);
				values.add(row);
				events.fireUpdated(old, row);
			}

			@Override
			public void dropped(Table<T> table) {
				drop();
			}

		};
		for (Table t : tables)
			t.addTableListener(listener);

	}

	void drop() {
		for (Table t : tables)
			t.removeTableListener(listener);
		events.fireDrop(this);
	}

	public void addTableListener(TableListener<T> l) {
		events.addTableListener(l);
	}

	public void removeTableListener(TableListener<T> l) {
		events.removeTableListener(l);
	}
	
	@Override
	public Iterator<T> iterator() {
		return new MergeIterator<T>();
	}
	public static class MergeIterator<T> extends Generator<T> {

		Iterator<Iterator<T>> subs;
		Iterator<T> sub;

		public MergeIterator(Table<? extends T>... tables) {
			super();
			List<Iterator<T>> iterators = new ArrayList<Iterator<T>>();
			for(Table<? extends T> table : tables)
				iterators.add((Iterator<T>) table.iterator());
			subs = iterators.iterator();
		}
		
		protected T gen() throws StopIteration {
			
			if (sub.hasNext())
				return sub.next();
			while (sub.hasNext() || subs.hasNext()) {
				if (sub.hasNext())
					return sub.next();
				else
					sub = subs.next();
			}
			throw new StopIteration();
		}
	}
}
