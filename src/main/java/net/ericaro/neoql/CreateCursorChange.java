package net.ericaro.neoql;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.ericaro.neoql.changeset.Change;
import net.ericaro.neoql.changeset.ChangeVisitor;
import net.ericaro.neoql.tables.Pair;

public class CreateCursorChange implements Change {

	protected Set<Pair<Class,Object>>	inserted	= new HashSet<Pair<Class,Object>>();
	
	CreateCursorChange() { }

	@Override
	public Change copy() {
		CreateCursorChange that = new CreateCursorChange();
		for (Pair<Class,Object> key : this.inserted)
			that.inserted.add(key);
		return that;
	}

	@Override
	public Change reverse() {
		DropCursorChange i = new DropCursorChange();
		i.deleted.addAll(inserted);
		return i;
	}

	public boolean contains(Object key) {
		return inserted.contains(key);
	}
	
	public Iterable<Pair<Class,Object>> created() {
		return Collections.unmodifiableCollection(inserted);
	}

	void create(Class table, Object row) {
		inserted.add(new Pair<Class,Object>(table, row));
	}

	public void accept(ChangeVisitor visitor) {
		visitor.changed(this);
	}
}
