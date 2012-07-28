package net.ericaro.neoql;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.ericaro.neoql.changeset.Change;
import net.ericaro.neoql.changeset.ChangeVisitor;
import net.ericaro.neoql.tables.Pair;

public class CreateTableChange implements Change {

	protected Set<Pair<Class,Column[]>>	inserted	= new HashSet<Pair<Class,Column[]>>();
	
	CreateTableChange() { }


	@Override
	public Change copy() {
		CreateTableChange that = new CreateTableChange();
		for (Pair<Class,Column[]> key : this.inserted)
			that.inserted.add(key);
		return that;
	}

	@Override
	public Change reverse() {
		DropTableChange i = new DropTableChange();
		i.deleted.addAll(inserted);
		return i;
	}

	public boolean contains(Object key) {
		return inserted.contains(key);
	}

	public Iterable<Pair<Class,Column[]>> created() {
		return Collections.unmodifiableCollection(inserted);
	}

	void create(Class table, Column[] row) {
		inserted.add(new Pair<Class,Column[]>(table, row));
	}

	public void accept(ChangeVisitor visitor) {
		visitor.changed(this);
	}
}
