package net.ericaro.neoql.notinvasive;

import javax.swing.ListModel;
import javax.swing.event.ListSelectionListener;

import net.ericaro.neoql.Column;
import net.ericaro.neoql.Database;
import net.ericaro.neoql.NeoQL;
import net.ericaro.neoql.Predicate;
import net.ericaro.neoql.TableData;
import net.ericaro.neoql.swing.ListMultiSelectionModel;
import net.ericaro.neoql.swing.SwingQL;
import net.ericaro.neoql.swing.TableList;

public class StuffListModel {

	Database database = new Database();
	private Column<Stuff, String>	stuffName;
	private Column<Stuff, Integer>	stuffId;
	private Column<Stuff, Boolean>	stuffSelected;
	private TableData<Stuff>	stuffTable;
	private TableList<Stuff>	listModel;
	private int	count;
	private ListMultiSelectionModel<Stuff> listSelectionModel;

	public StuffListModel() {
		super();
		// creates columns
		stuffName = NeoQL.column(Stuff.class, "name", String.class, false);
		stuffId = NeoQL.column(Stuff.class, "id", Integer.class, false);
		stuffSelected = NeoQL.column(Stuff.class, "selected", Boolean.class, false);
		
		stuffTable = database.createTable(stuffName, stuffId, stuffSelected);
		listModel = SwingQL.listFor(stuffTable);
		listSelectionModel = new ListMultiSelectionModel<Stuff>(database, listModel, stuffSelected);
	}

	public ListModel getListModel() {
		return listModel;
	}
	
	public void addStuff(String name) {
		database.insert(stuffName.set(name), stuffId.set(++count) );
		database.update(Stuff.class,(Predicate<Stuff>) NeoQL.True, stuffSelected.set(true) );
	}
	public void removeStuff(Stuff stuff) {
		database.delete(stuff);
		database.update(Stuff.class,(Predicate<Stuff>) NeoQL.True, stuffSelected.set(false) );
	}

	public ListMultiSelectionModel<Stuff> getListSelectionModel() {
		return listSelectionModel;
	}
}
