package net.ericaro.neoql;

import java.util.ArrayList;

import net.ericaro.neoql.eventsupport.TableListener;

/** A table cloner. Used to compare expected outcomes. 
 * 
 * @author eric
 *
 * @param <T>
 */
public class CloneTable<T> extends ArrayList<T>{

	
	ArrayList<T> content = new ArrayList<T>();
	private TableListener<T>	listener;

	public CloneTable(Table<T> table) {
		listener = new TableListener<T>() {
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

			@Override
			public void dropped(Table<T> table) {
				int size = content.size();
				content.clear();
				table.removeTableListener(listener);
			}
			
			
			// if we add a "sort" algorithm, I would need to "workout" this a little bit
		};
		table.addTableListener(listener);
	}
	
	
	private T setImpl(int index, T element) {
		T old = content.set(index, element);
		return old;
	}

	private void addImpl(T element) {
		addImpl(content.size(), element);
	}

	private void addImpl(int index, T element) {
		content.add(index, element);
	}

	private T removeImpl(int index) {
		T old = content.remove(index);
		return old;
	}
	
	public boolean areEquals() {
		if (content.size() != size() ) return false;
		
		for (int i=0;i < size();i++)
			if (! content.get(i).equals(get(i) )) 
					return false;
		return true;
		
	}
	
	
}
