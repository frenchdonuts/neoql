package net.ericaro.neoql;

import java.util.Iterator;

public abstract class Generator<T> implements Iterator<T>{

	private T next = null;
	private RuntimeException	exception = null; // exception throw a step before
	private boolean	hasNext = true;
	private boolean init = false;
	
	
	public Generator() {
		super();
	}

	/** generate the next item, or 
	 * 
	 * @return
	 */
	protected abstract T gen() throws StopIteration ;
	
	private void computeNext() {
		try {
			init = true;
			next = gen();
		}
		catch(StopIteration e) {
			exception = null;
			hasNext= false;
		}
		catch(RuntimeException e) {
			exception = e;
		}
	}

	@Override
	public boolean hasNext() {
		if (! init)
			computeNext();
		return hasNext;
	}

	@Override
	public T next() {
		// if exception where thrown during the 'previous compute next' rethrow it now
		if (exception !=null )
			throw exception;
		
		T last = next;
		computeNext();
		return last;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	
	
	
	
	
}
