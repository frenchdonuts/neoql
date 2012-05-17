package net.ericaro.neoql;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.ericaro.neoql.lang.Column;

/**
 * Mapped By provides a way to turn a table of S into a table of T using a
 * map(S)-> T interface
 * 
 */
public class MappedTable<S, T> implements Table<T> {

	private Table<S> table;
	private TableListenerSupport<T> events = new TableListenerSupport<T>();
	private Mapping<S, T> mapping;
	private TableListener<S> listener;

	MappedTable(Mapper<S,T> mapper, Table<S> table) {
		super();
		this.table = table;
		this.mapping = new MapMapper<S, T>(mapper);
		for (S s : table)
			events.fireInserted(mapping.push(s)); // cause events to be fire just like if the items where appended

		// first fill the filtered table
		// then add events to keep in touch with list content
		listener = new TableListener<S>() {

			public void inserted(S row) {
				events.fireInserted(mapping.push(row));
			}

			public void deleted(S row) {
				events.fireDeleted(mapping.pop(row));
			}

			public void updated(S old, S row) {
				events.fireUpdated(mapping.pop(old), mapping.push(row));
			}
		};
		table.addTableListener(listener);
	}

	
	
	@Override
	public void drop(Database from) {
		table.removeTableListener(listener);
	}



	@Override
	public Iterator<T> iterator() {
		final Iterator<S> i = table.iterator();
		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return i.hasNext();
			}

			@Override
			public T next() {
				return mapping.peek(i.next());
			}

			@Override
			public void remove() {
				i.remove();
			}

		};
	}

	public void addTableListener(TableListener<T> l) {
		events.addTableListener(l);
	}

	public void removeTableListener(TableListener<T> l) {
		events.removeTableListener(l);
	}

	public static class MappedIterator<S, T> extends Generator<T> {

		Iterator<S>					isource;
		Mapper<S,T>				mapper;

		public MappedIterator(Iterator<S> isource, Mapper<S,T> mapper) {
			super();
			this.isource = isource;
			this.mapper = mapper;
		}

		@Override
		protected T gen() throws StopIteration {
			while (isource.hasNext())
				return mapper.map(isource.next());
			throw new StopIteration();
		}
	}
	
}