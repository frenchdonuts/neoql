package net.ericaro.neoql.simpleeditor;

import javax.swing.ListModel;

import net.ericaro.neoql.Column;
import net.ericaro.neoql.Database;
import net.ericaro.neoql.NeoQL;
import net.ericaro.neoql.Property;
import net.ericaro.neoql.ContentTable;
import net.ericaro.neoql.swing.SwingQL;
import net.ericaro.neoql.tables.MergedTable;
import net.ericaro.neoql.tables.SelectTable;

public class EditorModel {

	
	private Database	database;
	private Column<Editable, String>	editableName;
	private Column<Editable, Directory>	editableParent;
	private Column<Directory, String>	dirName;
	private Column<Directory, Directory>	dirParent;
	private ContentTable<Directory>	dirTable;
	private ContentTable<Editable>	editableTable;
	private Column<Editable, Boolean>	editableEditing;
	private ListModel	editingList;
	

	public EditorModel() {
		super();
		editableName = NeoQL.column(Editable.class, "name", String.class, false);
		editableEditing = NeoQL.column(Editable.class, "editing", Boolean.class, false);
		editableParent = NeoQL.column(Editable.class, "parent", Directory.class, true);
		
		dirName = NeoQL.column(Directory.class, "name", String.class, false);
		dirParent = NeoQL.column(Directory.class, "parent", Directory.class, true);
		
		
		database = new Database() ;
		dirTable = database.createTable(Directory.class, dirName, dirParent);
		editableTable = database.createTable(Editable.class, editableName, editableEditing , editableParent);
		// base for the database, I've got everything I need here.
		
		// now building stuff required for the GUI
		editingList = SwingQL.listFor( NeoQL.where(editableTable, editableEditing.is(true)) );
		
	}

	public ListModel childsOf(Property<Directory> parent) {
		SelectTable<Directory> dirs = NeoQL.where(dirTable, dirParent.is(parent));
		SelectTable<Editable> edits = NeoQL.where(editableTable, editableParent.is(parent));
		
		//editable and directory could share a common interface or parent, then I could do better than object
		MergedTable<HasName> merged = new MergedTable<HasName>(dirs, edits);
		return SwingQL.listFor(merged) ;
	}

	public Property<Directory> propertyOf(Directory item) {
		
		return database.track(Directory.class, item);
	}

	public Property<Editable> propertyOf(Editable item) {
		return database.track(Editable.class, item);
	}
	
	
	
}
