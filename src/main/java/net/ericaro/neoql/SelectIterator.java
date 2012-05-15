package net.ericaro.neoql;

import java.util.Iterator;

 class SelectIterator<T> implements Iterator<T> {

	Iterator<T> sub;
	T next = null;
	private Predicate<? super T> where;

	 SelectIterator(Select<T> select, Table<T> table) {
		super();
		sub = table.iterator();
		this.where = select.getWhere() ;
		computeNext();
	}

	@Override
	public  boolean hasNext() {
		return next != null;
	}

	@Override
	public  T next() {
		T current = next;
		computeNext();
		return current;
	}

	private void computeNext() {
		while (sub.hasNext()) {
			next = sub.next();
			if (where.eval(next))
				return; // breaks
		}
		next = null;

	}

	@Override
	public  void remove() {
		throw new UnsupportedOperationException();

	}
}