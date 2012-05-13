package net.ericaro.osql.system;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import net.ericaro.osql.lang.Predicate;
import net.ericaro.osql.lang.Select;


// TODO find a way to "free" this list from the database (kind of close)

public class SelectList<T> extends AbstractList<T> implements List<T>, ListModel {

	List<T>						content	= new ArrayList<T>();

	private Select<T>			select;
	private Table<T>			table;
	private ListDataSupport		events	= new ListDataSupport(this);

	private Predicate<? super T>	where;

	SelectList(Select<T> select, Table<T> table) {
		super();
		this.select = select;
		this.table = table;
		where = select.getWhere();
		for (T t : table)
			if (where.eval(t))
				addImpl(t);
		// first fill the filtered table
		// then add events to keep in touch with list content
		table.addTableListener(new TableListener<T>() {

			public void inserted(T row) {
				if (where.eval(row))
					addImpl(row);
			}

			public void deleted(T row) {
				int i = content.indexOf(row); // always try to remove the old one
				if (i >= 0)
					removeImpl(i);
			}

			public void updated(T old, T row) {
				System.out.println("updated" + old + ", " + row);

				int i = content.indexOf(old); // always try to remove the old one
				boolean willbe = where.eval(row);
				if (i >= 0 && willbe) {
					// it was before, it will be after too, I need to update the content
					setImpl(i, row);
				} else {
					if (i >= 0)
						removeImpl(i);
					if (willbe)
						addImpl(row); // act like if the new row was added
				}
				// if we add a "sort" algorithm, I would need to "workout" this a little bit
			}

		});
	}

	@Override
	public int getSize() {
		return size();
	}

	@Override
	public Object getElementAt(int index) {
		return get(index);
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		events.addListDataListener(l);

	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		events.removeListDataListener(l);
	}

	private T setImpl(int index, T element) {
		T old = content.set(index, element);
		events.fireContentChanged(index);
		return old;
	}

	private void addImpl(T element) {
		addImpl(size(), element);
	}

	private void addImpl(int index, T element) {
		content.add(index, element);
		events.fireIntervalAdded(index);
	}

	private T removeImpl(int index) {
		T old = content.remove(index);
		events.fireIntervalRemoved(index);
		return old;
	}

	@Override
	public T get(int index) {
		return content.get(index);
	}

	@Override
	public int size() {
		return content.size();
	}
}