package net.ericaro.osql;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;


// TODO find a way to "free" this list from the database (kind of close)
/**
 * A Table List is the Swing ListModel version of the Table !
 * 
 * @author eric
 * 
 * @param <T>
 */
public class TableList<T> extends AbstractListModel<T> implements ListModel<T> {

	List<T> content = new ArrayList<T>();
	private Table<T> table;

	TableList(Table<T> table) {
		super();
		this.table = table;
		for (T t : table)
			addImpl(t);
		// first fill the filtered table
		// then add events to keep in touch with list content
		table.addTableListener(new TableListener<T>() {
			public void inserted(T row) {
				addImpl(row);
			}

			public void deleted(T row) {
				int i = content.indexOf(row); // always try to remove the old one
				if (i >= 0)
					removeImpl(i);
			}

			public void updated(T old, T row) {
				int i = content.indexOf(old); // always try to remove the old one
				if (i >= 0)
					setImpl(i, row);
				else
					addImpl(row); // act like if the new row was added
			}
			// if we add a "sort" algorithm, I would need to "workout" this a little bit
		});
	}

	@Override
	public int getSize() {
		return content.size();
	}

	@Override
	public T getElementAt(int index) {
		return content.get(index);
	}

	private T setImpl(int index, T element) {
		T old = content.set(index, element);
		super.fireContentsChanged(this, index, index);
		return old;
	}

	private void addImpl(T element) {
		addImpl(getSize(), element);
	}

	private void addImpl(int index, T element) {
		content.add(index, element);
		super.fireIntervalAdded(this, index, index);
	}

	private T removeImpl(int index) {
		T old = content.remove(index);
		super.fireIntervalRemoved(this, index, index);
		return old;
	}
}