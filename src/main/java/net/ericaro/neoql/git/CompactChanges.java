package net.ericaro.neoql.git;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.ericaro.neoql.CreateTableChange;
import net.ericaro.neoql.DeleteChange;
import net.ericaro.neoql.DropTableChange;
import net.ericaro.neoql.InsertChange;
import net.ericaro.neoql.UpdateChange;
import net.ericaro.neoql.changeset.Change;
import net.ericaro.neoql.changeset.ChangeSet;
import net.ericaro.neoql.changeset.ChangeVisitor;

/** compact a path of changes, into a "poorly named" row change, i.e a single change per entity.
 * this is an intermediate storage before the holly grail: the merge
 * 
 * @author eric
 *
 */
public class CompactChanges {

	static class RowChange<T>{
		static enum ChangeType {I,D,U};
		ChangeType type ;
		T init;
		T end;
		public RowChange(T init) {
			this.init = init;
		}
		
		
		
		
		public ChangeType getType() {
			return type;
		}




		public T getInit() {
			return init;
		}




		public T getEnd() {
			return end;
		}




		public void update(T newValue) {
			assert type != ChangeType.D : "cannot update an entity after it has been deleted";
			if (type == null ) type = ChangeType.U;
			this.end = newValue;
		}

		public void inserted() {
			assert type != ChangeType.U : "cannot insert an entity after it has already been updated";
			assert type != ChangeType.I : "cannot insert an entity already inserted";
			// the only valid states are D or null
			
			if (type == ChangeType.D) // I then D => deletion from the map, on the other hand, U, then D => keep D hence, D => there was an U before
				type = ChangeType.U;
			else 
				type = ChangeType.I;
			if (this.end == null)
				this.end = this.init;
		}
		
		public void deleted() {
			assert type != ChangeType.D : "cannot be dead again (though you can be born again ;-) ";
			
			// I then D => deletion from the map, on the other hand, U, then D => keep D hence, D => there was an U before
			if (type == ChangeType.U || type == null) {
				type = ChangeType.D;
				if (this.end == null) 
					this.end = this.init;
			}
			else if (type == ChangeType.I) {
				// delete this change from the final list (but where is the final list dude?
				this.end = null; // this will force the map to garbage everything in the null key
			}
		}
		
		
		
	}
	
	public CompactChanges() {
	}
	
	/** compact change into a per instance change, only three outcome per row: 
	 * I+row_end for inserted
	 * D+row_init for deleted (row is at its initial state
	 * U+row_init + row_end for a bunch of updates
	 * 
	 * @param change
	 * @return 
	 */
	public static final Map<Object, RowChange> compact(Change change) {
		final Map<Object, RowChange> map = new HashMap<Object, RowChange>();
		// map is structured as follow, the row -> rowchange.
		// if a row is updated then, using the old value you get or create the rowchange, then update to new value
		
		change.accept(new ChangeVisitor() {
			
			<T> RowChange<T> getOrCreateRowChange(T t) {
				RowChange r = map.get(t);
				if (r == null )	r = new RowChange<T>(t);
				return r;
			}
			
			@Override
			public <T> void changed(UpdateChange<T> change) {
				for(T t : change.oldValues()) {
					RowChange<T> r = getOrCreateRowChange(t);
					r.update(change.getNewValueFor(t) );
					map.put(r.end, r);
				}
			}
			
			@Override
			public <T> void changed(InsertChange<T> change) {
				for(T t : change.inserted() ) {
					RowChange<T> r = getOrCreateRowChange(t);
					r.inserted();
					map.put(r.end, r);
				}				
			}
			
			@Override
			public <T> void changed(DeleteChange<T> change) {
				for(T t : change.deleted()) {
					RowChange<T> r = getOrCreateRowChange(t);
					r.deleted();
					map.put(r.end, r);
				}
			}
			
			@Override
			public void changed(ChangeSet change) { // dive into details
				for(Change c: change.changes())
					c.accept(this);
			}
			
			@Override
			public void changed(DropTableChange dropTableChange) {
				// TODO handle drop and table creation !
			}
			
			@Override
			public void changed(CreateTableChange createTableChange) {
				// TODO handle drop and table creation !
			}
			
			
		});
		
		// clean the temp map from
		Iterator<Entry<Object, RowChange>> i = map.entrySet().iterator();
		while(i.hasNext()) {
			Entry<Object, RowChange> e = i.next();
			if (e.getValue().end == null || e.getValue().init != e.getKey() )
				i.remove() ;
		}
		return map;
		
	}

}
