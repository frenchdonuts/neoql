package net.ericaro.osql.system;

import java.util.Iterator;


public class Select implements Operation<Iterable<Object[]>>{

	
	Projector p;
	Class table;
	Predicate where;
	
	
	
	public Select(Projector p, Class table, Predicate where) {
		super();
		this.p = p;
		this.table = table;
		this.where = where;
	}



	public Iterable<Object[]> run(Database database){
		return database.run(this);
	}
	
	
	class SelectIterable implements Iterable<Object[]> {

		
		TableData table;
		
		public SelectIterable(TableData table) {
			super();
			this.table = table;
		}

		@Override
		public Iterator<Object[]> iterator() {

			
			return new Iterator<Object[]>() {
				Iterator<Object[]> itable = table.iterator();
				private Object[] next = null;
				{
					computeNext();
				}
				@Override
				public boolean hasNext() {
					return next !=null;
				}

				@Override
				public Object[] next() {
					Object[] res = next;
					computeNext();
					return res;
				}
				
				public void computeNext() {
					
					do {
						if (itable.hasNext())
							next = p.project( itable.next() );
						else {
							next = null;
							return;
						}
							
					}
					while ( ! where.eval(next));
				}
				
				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
				
			};
		}
		
	};

	
}
