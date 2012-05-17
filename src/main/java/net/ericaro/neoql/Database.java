package net.ericaro.neoql;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.ericaro.neoql.lang.ColumnValuePair;
import net.ericaro.neoql.lang.CreateTable;
import net.ericaro.neoql.lang.DeleteFrom;
import net.ericaro.neoql.lang.DropTable;
import net.ericaro.neoql.lang.GroupBySelect;
import net.ericaro.neoql.lang.InnerJoin;
import net.ericaro.neoql.lang.InsertInto;
import net.ericaro.neoql.lang.MapSelect;
import net.ericaro.neoql.lang.NeoQL;
import net.ericaro.neoql.lang.Script;
import net.ericaro.neoql.lang.Select;
import net.ericaro.neoql.lang.Update;

public class Database {
	// real class -> table mapping
	private Map<Class, TableData> tables = new HashMap<Class, TableData>();

	// TODO append sort in EDSL
	// TODO implement every possible joins
	// TODO rethink this all 'introspection' thing:
	//     - columns are passed to the create table statement
	//     - user handle the columns the way he wants
	// TODO find a way to express the definition of a singleton 
	//      for instance THE selected Item, so that it can be observed in the app, and reused
	// TODO oops, found a bug in the drop stuff. If I create a table to serve another one, I need to drop it too. (for instance select(mapper) creates a normal select, then mapper table, hence the "normal select is never deleted
// TODO do some stress test
	// ##########################################################################
	// ACCESS TABLES OBJECT BEGIN
	// ##########################################################################

	// tables are like select except that they keep in sync with the database content (observable) 

	public <T> TableData<T> tableFor(Class<T> table) {
		return tables.get(table);
	}

	public <T> Table<T> tableFor(TableDef<T> table) {
		return table.asTable(this);
	}

	
	public <T> void drop(Class<T> table) {
		tableFor(table).uninstall();
		this.tables.remove(table);
	}
	
	public <T> void drop(Table<T> table) {
		table.drop(this);
	}
	

	public <T> TableList<T> listFor(Class<T> table) {
		return new TableList<T>(tableFor(table));
	}

	public <T> TableList<T> listFor(TableDef<T> table) {
		return new TableList<T>(tableFor(table));
	}

	// ##########################################################################
	// ACCESS TABLES OBJECT END
	// ##########################################################################

	// ##########################################################################
	// EVENTS BEGIN
	// ##########################################################################

	public <T> void addTableListener(Class<T> table, TableListener<T> listener) {
		tableFor(table).addTableListener(listener);
	}

	public <T> void removeTableListener(Class<T> table,
			TableListener<T> listener) {
		tableFor(table).removeTableListener(listener);
	}

	<T> void addInternalTableListener(Class<T> table, TableListener<T> listener) {
		tableFor(table).addInternalTableListener(listener);
	}

	<T> void removeInternalTableListener(Class<T> table,
			TableListener<T> listener) {
		tableFor(table).removeInternalTableListener(listener);
	}

	// ##########################################################################
	// EVENTS END
	// ##########################################################################

	// ##########################################################################
	// EXECUTE SCRIPT VISITOR PATTERN BEGIN
	// ##########################################################################

	public <T> void execute(CreateTable<T> createTable) {

		Class<T> table = createTable.getTable();
		TableData<T> data = new TableData<T>(this, createTable);
		this.tables.put(table, data);
		data.install();
	}

	public <T> void execute(DropTable<T> dropTable) {

		Class<T> table = dropTable.getTable();
		drop(tableFor(table));
	}

	public <T> void execute(DeleteFrom<T> deleteFrom) {
		TableData<T> data = tableFor(deleteFrom.getTable());
		data.delete(deleteFrom.getWhere());
	}

	public <T> T execute(InsertInto<T> insertInto) {
		TableData<T> data = tableFor(insertInto.getTable());
		return data.insert(insertInto.getRow());

	}

	public <T> void execute(Update<T> update) {
		Class<T> table = update.getTable();
		TableData<T> data = tableFor(table);
		ColumnValuePair<T, ?>[] setters = update.getColumnValuePairs();
		Predicate<? super T> where = update.getWhere();
		for (T row : data)
			if (where.eval(row)) {
				data.update(row, setters);
			}
	}

	// ##########################################################################
	// EXECUTE SCRIPT VISITOR PATTERN END
	// ##########################################################################

	// select do not get stick to the database, once the iterator is executed everything is over

	// ##########################################################################
	// SELECTS BEGIN
	// ##########################################################################

	public <T> Iterable<T> select(Class<T> table) {
		return select(table, NeoQL.True);
	}

	public <T> Iterable<T> select(final Class<T> table, final Predicate<? super T> where) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return new SelectIterator<T>(tableFor(table), where);
			}
		};
	}

	// ##########################################################################
	// SELECTS END
	// ##########################################################################

	// ##########################################################################
	// VISITOR CALL BACK FOR TABLE CREATION BEGIN
	// ##########################################################################

	public <T> Table<T> table(Class<T> table) {
		return tableFor(table);
	}
	
	public <T> Table<T> table(Select<T> select) {
		return new SelectTable<T>(tableFor(select.getTable()) , select.getWhere());
	}

	public <S, T> Table<T> table(MapSelect<S, T> select) {
		SelectTable<S> table = new SelectTable<S>(tableFor(select.getTable()) , select.getWhere());
		return new MappedTable<S, T>(select.getMapper(), table);
	}
	public <S, T> Table<T> table(GroupBySelect<S, T> select) {
		SelectTable<S> table = new SelectTable<S>(tableFor(select.getTable()), select.getWhere());
		return new GroupByTable<S, T>(select.getGroupBy(), table);
	}


	public <L, R> Table<Pair<L, R>> table(InnerJoin<L, R> innerjoin) {
		Table<L> left = innerjoin.getLeftTable().asTable(this);
		Table<R> right = innerjoin.getRightTable().asTable(this);
		return new InnerJoinTable<L, R>(left, right, innerjoin.getOn());
	}

	// ##########################################################################
	// VISITOR CALL BACK FOR TABLE CREATION END
	// ##########################################################################

	// execute a script

	public void execute(Script script) {
		script.executeOn(this);
	}

}
