package net.ericaro.neoql;

import java.util.Iterator;

/**
 * Mapped By provides a way to turn a table of S into a table of T using a
 * map(S)-> T interface
 * 
 */
class MappedTable<S, T> implements Table<T> {

	private Table<S> table;
	private TableListenerSupport<T> events = new TableListenerSupport<T>();
	private Mapping<S, T> mapping;

	MappedTable(Mapper<S,T> mapper, Table<S> table) {
		super();
		this.table = table;
		this.mapping = new MapMapper<S, T>(mapper);
		for (S s : table)
			events.fireInserted(mapping.push(s)); // cause events to be fire just like if the items where appended

		// first fill the filtered table
		// then add events to keep in touch with list content
		table.addTableListener(new TableListener<S>() {

			public void inserted(S row) {
				events.fireInserted(mapping.push(row));
			}

			public void deleted(S row) {
				events.fireDeleted(mapping.pop(row));
			}

			public void updated(S old, S row) {
				events.fireUpdated(mapping.pop(old), mapping.push(row));
			}
		});
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

}