package net.ericaro.osql.system;

import java.util.Iterator;


public class SingleSelect<T> implements Operation<Iterable<T>>{

	
	Projector p;
	Class<T> table;
	Where<? super T>  where;
	
	
	
	public SingleSelect(Class<T> table, Where<? super T>  where) {
		super();
		this.table = table;
		this.where = where;
	}



	public Iterable<T> run(Database database){
		return database.run(this);
	}
	
	
	class SelectIterable implements Iterable<T> {

		
		TableData<T> table;
		
		public SelectIterable(TableData<T> table) {
			super();
			this.table = table;
		}

		@Override
		public Iterator<T> iterator() {
			return new Iterator<T>() {
				Iterator<T> itable = table.iterator();
				private T next = null;
				{
					computeNext();
				}
				@Override
				public boolean hasNext() {
					return next !=null;
				}

				@Override
				public T next() {
					T res = next;
					computeNext();
					return res;
				}
				
				public void computeNext() {
					
					do {
						if (itable.hasNext())
							next = itable.next() ;
						else {
							next = null;
							return;
						}
							
					}
					while ( ! where.isTrue(next));
				}
				
				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
				
			};
		}
		
	};

	
}
