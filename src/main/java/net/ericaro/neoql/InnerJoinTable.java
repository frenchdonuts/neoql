package net.ericaro.neoql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

 class InnerJoinTable<L, R> implements Table<Pair<L, R>> {

	Table<L>							left;
	Table<R>							right;
	Predicate<? super Pair<L, R>>		where;
	List<Pair<L, R>>					data	= new ArrayList<Pair<L, R>>();

	TableListenerSupport<Pair<L, R>>	events	= new TableListenerSupport<Pair<L, R>>();

	 InnerJoinTable(Table<L> left, Table<R> right, Predicate<? super Pair<L, R>> on) {
		super();
		this.left = left;
		this.right = right;
		this.where = on;
		left.addTableListener(new TableListener<L>() {
			@Override
			public void updated(L oldRow, L newRow) {
				leftUpdated(oldRow, newRow);
			}

			@Override
			public  void deleted(L oldRow) {
				leftDeleted(oldRow);
			}

			@Override
			public  void inserted(L newRow) {
				leftInserted(newRow);
			}
		});
		if (left != right) // auto join does not need right listener
			right.addTableListener(new TableListener<R>() {
				@Override
				public  void updated(R oldRow, R newRow) {
					rightUpdated(oldRow, newRow);
				}
	
				@Override
				public  void deleted(R oldRow) {
					rightDeleted(oldRow);
				}
	
				@Override
				public  void inserted(R newRow) {
					rightInserted(newRow);
				}
			});
		
		for (Iterator<L> ileft = left.iterator(); ileft.hasNext();)
			leftInserted(ileft.next()); // fake the insertion to fire events, and fill the table
	}

	private void leftInserted(L newRow) {
		// there is a new lefter, need to reparse the right for matching pairs
		Iterator<R> iright = right.iterator();
		while (iright.hasNext()) {
			R rnext = iright.next();
			Pair<L, R> next = new Pair<L, R>(newRow, rnext);
			if (where.eval(next))
				doInsert(next);
		}
	}

	private void leftDeleted(L oldRow) {
		for (ListIterator<Pair<L, R>> i = data.listIterator(); i.hasNext();) {
			Pair<L, R> p = i.next();
			if (oldRow.equals(p.getLeft())) {
				i.remove();
				events.fireDeleted(p);
			}
		}
	}

	private void leftUpdated(L oldRow, L newRow) {
		System.out.println("left updated "+ oldRow + " -> "+ newRow );
		
		// handled what was
		for (ListIterator<Pair<L, R>> i = data.listIterator(); i.hasNext();) {
			Pair<L, R> old = i.next();
			boolean was = oldRow.equals(old.getLeft());
			if (was) {
				Pair<L, R> newPair = new Pair<L, R>(newRow, old.getRight()); // this is the new pair that should be
				boolean willbe = where.eval(newPair);
				if (willbe) {
					// it was before, it will be after too, I need to update the content
					i.set(newPair);
					events.fireUpdated(old, newPair);
				} else {
					i.remove();
					events.fireDeleted(old);
				}
			}
		}
		// handle every thing (including what was, but this case is skipped
		for (R row : right) {
			
			boolean wasnt = false;
			if (row == newRow) {
				R r = (R) oldRow;
				Pair<L, R> oldPair = new Pair<L, R>(oldRow,r); // this is the new pair that was in there
				wasnt = !where.eval(oldPair);
			}
			else {
				Pair<L, R> oldPair = new Pair<L, R>(oldRow, row); // this is the new pair that was in there
				wasnt = !where.eval(oldPair);
			}

			if (wasnt) { // the case, was and will be was already handled
				Pair<L, R> newPair = new Pair<L, R>(newRow, row); // this is the new pair that should be
				boolean willbe = where.eval(newPair);
				if (willbe) {
					data.add(newPair);
					events.fireInserted(newPair); // act like if the new row was added
				}
			}
		}

	}

	private void rightInserted(R newRow) {
		// there is a new lefter, need to reparse the right for matching pairs
		Iterator<L> ileft = left.iterator();
		while (ileft.hasNext()) {
			L lnext = ileft.next();
			Pair<L, R> next = new Pair<L, R>(lnext, newRow);
			if (where.eval(next))
				doInsert(next);
		}
	}

	private void rightDeleted(R oldRow) {
		for (ListIterator<Pair<L, R>> i = data.listIterator(); i.hasNext();) {
			Pair<L, R> p = i.next();
			if (oldRow.equals(p.getRight())) {
				i.remove();
				events.fireDeleted(p);
			}
		}
	}

	private void rightUpdated(R oldRow, R newRow) {
		System.out.println("right updated "+ oldRow + " -> "+ newRow );
		
		for (ListIterator<Pair<L, R>> i = data.listIterator(); i.hasNext();) {
			Pair<L, R> old = i.next();

			boolean was = oldRow.equals(old.getRight());
			if (was) {
				Pair<L, R> newPair = new Pair<L, R>(old.getLeft(), newRow); // this is the new pair that should be
				boolean willbe = where.eval(newPair);
				if (willbe) {
					// it was before, it will be after too, I need to update the content
					i.set(newPair);
					events.fireUpdated(old, newPair);
				} else {
						i.remove();
						events.fireDeleted(old); // act like if the new row was added
				}
			}
		}

		// handle every thing (including what was, but this case is skipped
		for (L row : left) {
			// handle the case of self join
			boolean wasnt = false;
			if (row == newRow) {
				L r = (L) oldRow;
				Pair<L, R> oldPair = new Pair<L, R>(r, oldRow); // this is the new pair that was in there
				wasnt = !where.eval(oldPair);
			}
			else {
				Pair<L, R> oldPair = new Pair<L, R>(row, oldRow); // this is the new pair that was in there
				wasnt = !where.eval(oldPair);
			}

			
			if (wasnt) { // the case, was and will be was already handled
				Pair<L, R> newPair = new Pair<L, R>(row, newRow); // this is the new pair that should be
				boolean willbe = where.eval(newPair);
				if (willbe) {
					data.add(newPair);
					events.fireInserted(newPair); // act like if the new row was added
				}
			}
		}

	}

	private void doInsert(Pair<L, R> row) {
		data.add(row);
		events.fireInserted(row);
	}

	private void doRemove(Pair<L, R> row) {
		data.remove(row);
		events.fireDeleted(row);
	}

	private void doUpdate(int i, Pair<L, R> row) {
		Pair<L, R> old = data.remove(i);
		events.fireUpdated(old, row);
	}

	@Override
	public  Iterator<Pair<L, R>> iterator() {
		return data.iterator();
	}

	void fill() {
		Iterator<Pair<L, R>> i = new Iterator<Pair<L, R>>() {
			Iterator<L>			ileft;
			Iterator<R>			iright;
			private Pair<L, R>	next;
			private L			rleft	= null;

			{
				ileft = left.iterator();
				iright = right.iterator();
				if (ileft.hasNext())
					rleft = ileft.next();
				computeNext();
			}

			@Override
			public  boolean hasNext() {
				return next != null;
			}

			 void computeNext() {
				do {
					while (iright.hasNext()) {
						R rnext = iright.next();
						next = new Pair<L, R>(rleft, rnext);
						if (where.eval(next)) // based on outer right , left
												// inner join the behaviour here
												// is different
							
							// , in the meantime we use inner join
							return;
					}
					iright = right.iterator(); // restart the right iterator
					if (ileft.hasNext())
						rleft = ileft.next();
				} while (ileft.hasNext());
				next = null;
			}

			@Override
			public  Pair<L, R> next() {
				Pair<L, R> current = next;
				computeNext();
				return current;
			}

			@Override
			public  void remove() {
				throw new UnsupportedOperationException();

			}
		};
		while (i.hasNext())
			data.add(i.next());

	}

	@Override
	public  void addTableListener(TableListener<Pair<L, R>> listener) {
		events.addTableListener(listener);
	}

	@Override
	public  void removeTableListener(TableListener<Pair<L, R>> listener) {
		events.addTableListener(listener);
	}

}
