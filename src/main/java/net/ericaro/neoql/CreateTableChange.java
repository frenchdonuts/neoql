package net.ericaro.neoql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.ericaro.neoql.changeset.Change;
import net.ericaro.neoql.changeset.ChangeVisitor;
import net.ericaro.neoql.changeset.Changes;
import net.ericaro.neoql.tables.Pair;

public class CreateTableChange implements Change {

	protected List<Pair<Class,Column[]>>	inserted	= new ArrayList<Pair<Class,Column[]>>();
	
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
	public String toString() {
		return Changes.toString(this);
	}


	public boolean isEmpty() {
		return inserted.isEmpty();
	}
}
