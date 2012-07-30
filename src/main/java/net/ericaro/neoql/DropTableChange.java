package net.ericaro.neoql;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.ericaro.neoql.changeset.Change;
import net.ericaro.neoql.changeset.ChangeVisitor;
import net.ericaro.neoql.tables.Pair;

public class DropTableChange implements Change {

	protected Set<Pair<Class,Column[]>>	deleted	= new HashSet<Pair<Class,Column[]>>();

	DropTableChange() {

	}

	@Override
	public Change copy() {
		DropTableChange that = new DropTableChange();
		for (Pair<Class,Column[]> key : this.deleted)
			that.deleted.add(key);
		return that;
	}

	@Override
	public Change reverse() {
		CreateTableChange i = new CreateTableChange();
		i.inserted.addAll(deleted);
		return i;
	}

	public boolean contains(Object key) {
		return deleted.contains(key);
	}
	

	public Iterable<Pair<Class,Column[]>> dropped() {
		return Collections.unmodifiableCollection(deleted);
	}

	void drop(Class table, Column[] row) {
		deleted.add(new Pair<Class,Column[]>(table,row));
	}

	public void accept(ChangeVisitor visitor) {
		visitor.changed(this);
	}
}
