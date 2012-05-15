package net.ericaro.neoql;

import java.util.Iterator;

// TODO find a way to "free" this list from the database (kind of close)

 class SelectTable<T> implements Table<T> {

	private Table<T> table;
	private TableListenerSupport<T> events = new TableListenerSupport<T>();

	private Predicate<? super T> where;
	private Select<T> select;

	SelectTable(Select<T> select, Table<T> table) {
		super();
		this.table = table;
		this.select = select;
		where = select.getWhere();
		for (T t : table)
			if (where.eval(t))
				events.fireInserted(t); // cause events to be fire just like if the items where appended

		// first fill the filtered table
		// then add events to keep in touch with list content
		table.addTableListener(new TableListener<T>() {

			public  void inserted(T row) {
				if (where.eval(row))
					events.fireInserted(row);
			}

			public  void deleted(T row) {
				if (where.eval(row))
					events.fireDeleted(row);
			}

			public  void updated(T old, T row) {
				boolean was = where.eval(old);
				boolean willbe = where.eval(row);
				if (was && willbe) {
					// it was before, it will be after too, I need to update the content
					events.fireUpdated(old, row);
				} else {
					if (was)
						events.fireDeleted(old);
					if (willbe)
						events.fireInserted(row); // act like if the new row was added
				}
				// if we add a "sort" algorithm, I would need to "workout" this a little bit
			}

		});
	}

	@Override
	public  Iterator<T> iterator() {
		return new SelectIterator<T>(select, table);
	}

	public  void addTableListener(TableListener<T> l) {
		events.addTableListener(l);
	}

	public  void removeTableListener(TableListener<T> l) {
		events.removeTableListener(l);
	}

}