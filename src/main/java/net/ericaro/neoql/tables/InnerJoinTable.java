package net.ericaro.neoql.tables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import net.ericaro.neoql.Predicate;
import net.ericaro.neoql.Table;
import net.ericaro.neoql.eventsupport.TableListener;
import net.ericaro.neoql.eventsupport.TableListenerSupport;




public class InnerJoinTable<L, R> implements Table<Pair<L, R>> {

	Table<L> left;
	Table<R> right;
	Predicate<? super Pair<L, R>> on;
	List<Pair<L, R>> data = new ArrayList<Pair<L, R>>();

	TableListenerSupport<Pair<L, R>> events = new TableListenerSupport<Pair<L, R>>();
	private TableListener<R> rightListener;
	private TableListener<L> leftListener;
	private Class<Pair<L, R>>	type;

	public InnerJoinTable(Class<Pair<L, R>> type, Table<L> left, Table<R> right,
			Predicate<? super Pair<L, R>> on) {
		super();
		this.left = left;
		this.right = right;
		this.on = on;
		this.type = type;
		leftListener = new TableListener<L>() {
			@Override
			public void updated(L oldRow, L newRow) {
				leftUpdated(oldRow, newRow);
			}

			@Override
			public void deleted(L oldRow) {
				leftDeleted(oldRow);
			}

			@Override
			public void inserted(L newRow) {
				leftInserted(newRow);
			}
			@Override
			public void dropped(Table<L> table) {
				drop();
			}
			
		};
		left.addTableListener(leftListener);
		if (left != right) {
			rightListener = new TableListener<R>() {
				@Override
				public void updated(R oldRow, R newRow) {
					rightUpdated(oldRow, newRow);
				}

				@Override
				public void deleted(R oldRow) {
					rightDeleted(oldRow);
				}

				@Override
				public void inserted(R newRow) {
					rightInserted(newRow);
				}
				@Override
				public void dropped(Table<R> table) {
					drop();
				}
			};
			right.addTableListener(rightListener);
		}

		for (Iterator<L> ileft = left.iterator(); ileft.hasNext();)
			leftInserted(ileft.next()); // fake the insertion to fire events, and fill the table
	}

	
	
	void drop() {
		this.data.clear();
		left.removeTableListener(leftListener);
		if (rightListener != null) // might be null because auto join does not record twice
			right.removeTableListener(rightListener);
		events.fireDrop(this);
	}



	private void leftInserted(L newRow) {
		// there is a new lefter, need to reparse the right for matching pairs
		Iterator<R> iright = right.iterator();
		while (iright.hasNext()) {
			R rnext = iright.next();
			Pair<L, R> next = new Pair<L, R>(newRow, rnext);
			if (on.eval(next))
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

		
		// handled what was
		for (ListIterator<Pair<L, R>> i = data.listIterator(); i.hasNext();) {
			Pair<L, R> old = i.next();
			boolean was = oldRow.equals(old.getLeft());
			if (was) {
				Pair<L, R> newPair = new Pair<L, R>(newRow, old.getRight()); // this is the new pair that should be
				boolean willbe = on.eval(newPair);
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
				Pair<L, R> oldPair = new Pair<L, R>(oldRow, r); // this is the new pair that was in there
				wasnt = !on.eval(oldPair);
			} else {
				Pair<L, R> oldPair = new Pair<L, R>(oldRow, row); // this is the new pair that was in there
				wasnt = !on.eval(oldPair);
			}

			if (wasnt) { // the case, was and will be was already handled
				Pair<L, R> newPair = new Pair<L, R>(newRow, row); // this is the new pair that should be
				boolean willbe = on.eval(newPair);
				if (willbe && ! data.contains(newPair)) {
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
			if (on.eval(next))
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
		System.out.println("right updated " + oldRow + " -> " + newRow);

		for (ListIterator<Pair<L, R>> i = data.listIterator(); i.hasNext();) {
			Pair<L, R> old = i.next();

			boolean was = oldRow.equals(old.getRight());
			if (was) {
				Pair<L, R> newPair = new Pair<L, R>(old.getLeft(), newRow); // this is the new pair that should be
				boolean willbe = on.eval(newPair);
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
				wasnt = !on.eval(oldPair);
			} else {
				Pair<L, R> oldPair = new Pair<L, R>(row, oldRow); // this is the new pair that was in there
				wasnt = !on.eval(oldPair);
			}

			if (wasnt) { // the case, was and will be was already handled
				Pair<L, R> newPair = new Pair<L, R>(row, newRow); // this is the new pair that should be
				boolean willbe = on.eval(newPair);
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
	public Iterator<Pair<L, R>> iterator() {
		return data.iterator();
	}

				
		public static class InnerJoinIterator<L,R> extends Generator<Pair<L, R>>		{
			Iterator<L> ileft;
			Iterator<R> iright;
			private L rleft = null;
			private Iterable<R>	right;
			private Predicate<? super Pair<L, R>>	on;
			
			public InnerJoinIterator(Iterator<L> ileft, Iterable<R> right, Predicate<? super Pair<L, R>> on ) {
				super();
				this.ileft = ileft;
				if (ileft.hasNext())
					rleft = ileft.next();
				
				this.right = right;
				iright = right.iterator();
				this.on = on;
			}

			public Pair<L, R> gen() throws StopIteration {
				do {
					while (iright.hasNext()) {
						R rnext = iright.next();
						Pair<L, R> next = new Pair<L, R>(rleft, rnext);
						if (on.eval(next)) // based on outer right , left
												// inner join the behaviour here
												// is different

							// , in the meantime we use inner join
							return next;
					}
					iright = right.iterator(); // restart the right iterator
					if (ileft.hasNext())
						rleft = ileft.next();
				} while (ileft.hasNext());
				throw new StopIteration();
			}
		}

	@Override
	public void addTableListener(TableListener<Pair<L, R>> listener) {
		events.addTableListener(listener);
	}

	@Override
	public void removeTableListener(TableListener<Pair<L, R>> listener) {
		events.addTableListener(listener);
	}



	@Override
	public Class<Pair<L, R>> getType() {
		return type;
	}

	
	
}
