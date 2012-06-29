package net.ericaro.neoql.swing;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.ericaro.neoql.Column;
import net.ericaro.neoql.Database;

public class ListMultiSelectionModel<T> extends DefaultListSelectionModel {

	private Database			database;
	private Column<T, Boolean>	selected;
	private TableList<T>		data;
	private boolean				on;

	public ListMultiSelectionModel(Database database, TableList<T> data, Column<T, Boolean> selected) {
		super();
		this.database = database;
		this.data = data;
		this.selected = selected;
		this.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				onChanged(e.getFirstIndex(), e.getLastIndex());
			}
		});
		data.addListDataListener(new ListDataListener() {

			@Override
			public void intervalRemoved(ListDataEvent e) {}

			@Override
			public void intervalAdded(ListDataEvent e) {
				addSelectionInterval(e.getIndex0(), e.getIndex1());
			}

			@Override
			public void contentsChanged(ListDataEvent e) {
				whenChanged(e.getIndex0(), e.getIndex1());
			}

		});
	}

	void onChanged(int first, int last) {
		on = true;
		for (int i = first; i < Math.min(last + 1, data.getSize()); i++)
			database.update(data.getElementAt(i), selected.set(isSelectedIndex(i)));
		on = false;
	}

	void whenChanged(int first, int last) {
		// if on ignore this change, I'm the source of it, but in case
		if (!on)
			for (int i = first; i <= last; i++) {
				boolean isSelected = selected.get(data.getElementAt(i));
				if (isSelectedIndex(i) != isSelected) {
					if (isSelected)
						addSelectionInterval(i, i);// this should not start a fixed point
					else
						removeSelectionInterval(i, i);// this should not start a fixed point
				}
			}
	}
}
