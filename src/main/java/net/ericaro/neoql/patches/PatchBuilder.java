package net.ericaro.neoql.patches;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.ericaro.neoql.Column;

public class PatchBuilder {
	
	Set<Object> inserted = new HashSet<Object>();
	Map<Object,Object> updated = new HashMap<Object,Object>(); // carefull this is a reversed map, key is new value, value is original one
	Set<Object> deleted = new HashSet<Object>();
	private List<Patch>	patches = new ArrayList<Patch>() ; // only for create and drop table
	
	public <T> void insert(Class<T> type, T t) {
		inserted.add(t);
		deleted.remove(t);
		assert !updated.containsKey(t) :"an item cannot be updated before beeing inserted";
	}

	public <T> void update(Class<T> type, T oldValue, T newValue) {
		if (inserted.remove(oldValue) ) 			// the value was in the inserted set
			inserted.add(newValue);
		else {
			Object src = updated.remove(newValue) ;
			if (src == null)
				src = oldValue;			// oldValue will become the new src
			updated.put(newValue, src);
		}
	}

	public <T> void delete(Class<T> type, T t) {
		if (inserted.remove(t) )
			return;// this deletion has no effect
		Object src = updated.remove(t); // remove from "updated in case
		if (src ==null) src = t;// was neither inserted, not updated, it's a source
		deleted.add(src);
	}

	public <T> void dropTable(Class<T> table, Column... columns) {
		patches .add(new DropTable(table, columns) );
	}
	public <T> void createTable(Class<T> table, Column... columns) {
		patches.add(new CreateTable(table, columns) );
	}

	
	public Patch build() {
		
		for(Object o: inserted)
			patches.add(new Insert( o.getClass(), o));
		
		for(Entry<Object,Object> o: updated.entrySet() )
			patches.add(new Update( o.getKey().getClass(), o.getValue(), o.getKey() ));
		
		for(Object o: deleted)
			patches.add(new Delete( o.getClass(), o));
		
		PatchSet p = new PatchSet(patches);
		patches.clear();
		inserted.clear();
		updated.clear();
		deleted.clear();
		if (p.isEmpty()) return null;
		return p;
	}

	/** return true if the value is expected to be new, hence, it has already be cloned, and do not need to
	 * 
	 * @param value
	 * @return
	 */
	public <T> boolean containsNew(T value) {
		//look into updated // note it was only in the updated, inserted is my own guess
		
		return inserted.contains(value) || updated.containsKey(value);
	}
	public <T> boolean containsOld(T value) {
		return deleted.contains(value) || updated.containsValue(value);
	}
	// return an iterable over the potentially updated or inserted values
	public Iterable newValues(){
		return plus(inserted, updated.keySet());
		
	}
	
	
	static <U> Iterable<U> plus(final Iterable<U> u, final Iterable<U> v) {
		return new Iterable<U>() {

			@Override
			public Iterator<U> iterator() {
				return new Iterator<U>() {
					private Iterator<U>	iu;
					private Iterator<U>	iv;

					{
						iu = u.iterator();
						iv = v.iterator();
					}

					@Override
					public boolean hasNext() {
						return iu.hasNext() || iv.hasNext();
					}

					@Override
					public U next() {
						if (iu.hasNext())
							return iu.next();
						else
							return iv.next();
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}
	
}
