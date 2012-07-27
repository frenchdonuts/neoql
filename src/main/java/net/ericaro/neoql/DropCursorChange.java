package net.ericaro.neoql;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.ericaro.neoql.changeset.Change;
import net.ericaro.neoql.changeset.ChangeVisitor;
import net.ericaro.neoql.tables.Pair;

public class DropCursorChange implements Change {

	protected Set<Pair<Class,Object>>	deleted	= new HashSet<Pair<Class,Object>>();

	DropCursorChange() {

	}

	@Override
	public Change copy() {
		DropCursorChange that = new DropCursorChange();
		for (Pair<Class,Object> key : this.deleted)
			that.deleted.add(key);
		return that;
	}

	@Override
	public Change reverse() {
		CreateCursorChange i = new CreateCursorChange();
		i.inserted.addAll(deleted);
		return i;
	}

	public boolean contains(Object key) {
		return deleted.contains(key);
	}
	

	public Iterable<Pair<Class,Object>> dropped() {
		return Collections.unmodifiableCollection(deleted);
	}

	void drop(Class table, Object row) {
		deleted.add(new Pair<Class,Object>(table,row));
	}

	public void accept(ChangeVisitor visitor) {
		visitor.changed(this);
	}
}
