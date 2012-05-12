package net.ericaro.osql.system;

import java.util.Iterator;

public class JoinTable<L, R> implements Table<Pair<L, R>> {

	Table<L>							left;
	Table<R>							right;
	Where<Pair<? super L, ? super R>>	where;

	@Override
	public Iterator<Pair<L, R>> iterator() {
		return new Iterator<Pair<L, R>>() {
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
			public boolean hasNext() {
				return next != null;
			}

			public void computeNext() {
				do {
					while (iright.hasNext()) {
						R rnext = iright.next();
						next = new Pair<L, R>(rleft, rnext);
						if (where.isTrue(next)) // based on outer right , left inner join the behaviour here is different //TODO implement every join, in the meanwhile we use inner join
							return;
					}
					iright = right.iterator(); // restart the right iterator
					if (ileft.hasNext())
						rleft = ileft.next();
				} while (ileft.hasNext());
				next = null;
			}

			@Override
			public Pair<L, R> next() {
				Pair<L, R> current = next;
				computeNext();
				return current;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();

			}
		};
	}

	@Override
	public void addDatabaseListener(DatabaseListener<Pair<L, R>> listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeDatabaseListener(DatabaseListener<Pair<L, R>> listener) {
		// TODO Auto-generated method stub

	}

}
