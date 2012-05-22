package net.ericaro.neoql;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.ListModel;

import net.ericaro.neoql.lang.ClassTableDef;
import net.ericaro.neoql.lang.ColumnValue;
import net.ericaro.neoql.lang.CreateProperty;
import net.ericaro.neoql.lang.CreateTable;
import net.ericaro.neoql.lang.DeleteFrom;
import net.ericaro.neoql.lang.DropProperty;
import net.ericaro.neoql.lang.DropTable;
import net.ericaro.neoql.lang.GroupBySelect;
import net.ericaro.neoql.lang.InnerJoin;
import net.ericaro.neoql.lang.InsertInto;
import net.ericaro.neoql.lang.MapSelect;
import net.ericaro.neoql.lang.OrderBySelect;
import net.ericaro.neoql.lang.Script;
import net.ericaro.neoql.lang.Select;
import net.ericaro.neoql.lang.Update;
import net.ericaro.neoql.system.OrderByTable;
import net.ericaro.neoql.system.Pair;
import net.ericaro.neoql.system.Predicate;
import net.ericaro.neoql.system.Property;
import net.ericaro.neoql.system.PropertyListener;
import net.ericaro.neoql.system.Table;
import net.ericaro.neoql.system.TableDef;
import net.ericaro.neoql.system.TableList;
import net.ericaro.neoql.system.TableListener;

public class Database {
	// real class -> table mapping
	private Map<ClassTableDef, TableData>	tables	= new HashMap<ClassTableDef, TableData>();
	private Map<Property, Singleton>		values	= new HashMap<Property, Singleton>();

	
	
	
	// TODO implement every possible joins
	// TODO find a way to express the definition of a singleton
	// for instance THE selected Item, so that it can be observed in the app, and reused
	// TODO oops, found a bug in the drop stuff. If I create a table to serve another one, I need to drop it too. (for instance select(mapper) creates a normal select, then mapper table, hence the "normal select is never deleted
	// TODO do some stress test
	// TODO provide an SQL like toString for table def

	// ##########################################################################
	// PUBLIC API BEGIN
	// ##########################################################################

	// public API: mainly triggers visitor pattern on statements objects
	// except for the basic ones

	// should I add insert into here or not. Should I get the new instance private or not.
	/**
	 * Retrieve the primary table associated with a class (if exists)
	 * 
	 * @param table
	 * @return
	 */
	public <T> TableData<T> tableFor(ClassTableDef<T> table) {
		return tables.get(table);
	}

	// TODO what the fuck ? retrieving, and creating is the same process ? It shouldn't of course
	// the doc is correct, not the code: the code says: create and retr
	/**
	 * Retrieve a table associated with this table definition
	 * 
	 * @param table
	 * @return
	 */
	public <T> Table<T> tableFor(TableDef<T> table) {
		return table.asTable(this);
	}

	public <T> void drop(ClassTableDef<T> table) {
		tableFor(table).uninstall();
		this.tables.remove(table);
	}

	public <T> void drop(Table<T> table) {
		table.drop(this);
	}

	public <T, U extends ListModel&Iterable<T>> U listFor(TableDef<T> table) {
		return (U) new TableList<T>(tableFor(table));
	}

	public <T> Iterator<T> iterator(final TableDef<T> table) {
		return table.iterator(this);// reverse visitor pattern
	}

	public <T> Iterable<T> select(final TableDef<T> table) {
		return new Iterable<T>() {

			@Override
			public Iterator<T> iterator() {
				return Database.this.iterator(table);
			}

		};
	}

	public <T> T get(Property<T> prop) {
		return getSingleton(prop).get();
	}

	// execute a script

	public void execute(Script script) {
		script.executeOn(this);
	}

	// ##########################################################################
	// ACCESS TABLES OBJECT END
	// ##########################################################################

	// ##########################################################################
	// EVENTS BEGIN
	// ##########################################################################

	public <T> void addTableListener(ClassTableDef<T> table, TableListener<T> listener) {
		tableFor(table).addTableListener(listener);
	}

	public <T> void removeTableListener(ClassTableDef<T> table, TableListener<T> listener) {
		tableFor(table).removeTableListener(listener);
	}

	<T> void addInternalTableListener(ClassTableDef<T> table, TableListener<T> listener) {
		tableFor(table).addInternalTableListener(listener);
	}

	<T> void removeInternalTableListener(ClassTableDef<T> table, TableListener<T> listener) {
		tableFor(table).removeInternalTableListener(listener);
	}

	public <T> void addPropertyListener(Property<T> prop, PropertyListener<T> l) {
		getSingleton(prop).addPropertyListener(l);
	}

	public <T> void removePropertyListener(Property<T> prop, PropertyListener<T> l) {
		getSingleton(prop).removePropertyListener(l);
	}

	// ##########################################################################
	// EVENTS END
	// ##########################################################################

	// ##########################################################################
	// EXECUTE SCRIPT VISITOR PATTERN BEGIN
	// ##########################################################################

	public <T> void execute(CreateTable<T> createTable) {

		ClassTableDef<T> table = createTable.getTableDef();
		TableData<T> data = new TableData<T>(this, table);
		this.tables.put(table, data);
		data.install();
	}

	public <T> void execute(DropTable<T> dropTable) {

		ClassTableDef<T> table = dropTable.getTable();
		drop(tableFor(table));
	}

	public <T> void execute(DeleteFrom<T> deleteFrom) {
		TableData<T> data = tableFor(deleteFrom.getTable());
		data.delete(deleteFrom.getWhere());
	}

	// remove all execute from database, and move them to a 'statement visitor interface, let this databse have a statement visitor inner class instead'
	public <T> T execute(InsertInto<T> insertInto, T row) {
		TableData<T> data = tableFor(insertInto.getTable());
		
		return data.insert(row);

	}

	public <T> void execute(Update<T> update) {
		TableData<T> data = tableFor(update.getTable());
		ColumnValue<T, ?>[] setters = update.getColumnValuePairs();
		Predicate<? super T> where = update.getWhere();
		for (T row : data)
			if (where.eval(row)) {
				data.update(row, setters);
			}
		data.fireUpdate();
	}

	public <T> void execute(CreateProperty<T> createProperty) {
		Singleton<T> ton = new Singleton<T>(tableFor(createProperty.getTable()));
		values.put(createProperty.getProperty(), ton);
	}

	public <T> void execute(DropProperty<T> prop) {
		Singleton<T> removed = values.remove(prop.getProperty());
		removed.dropTable();
	}

	public <T> void put(Property<T> prop, T value) {
		getSingleton(prop).set(value);
	}

	// ##########################################################################
	// EXECUTE SCRIPT VISITOR PATTERN END
	// ##########################################################################

	// ##########################################################################
	// VISITOR CALL BACK FOR TABLE CREATION BEGIN
	// ##########################################################################

	public <T> Table<T> table(ClassTableDef<T> table) {
		return tableFor(table);
	}

	public <T> Table<T> table(Select<T> select) {
		return new SelectTable<T>(tableFor(select.getTable()), select.getWhere());
	}

	public <S, T> Table<T> table(MapSelect<S, T> select) {
		return new MappedTable<S, T>(select.getMapper(), tableFor(select.getTable()));
	}

	public <S, T> Table<T> table(GroupBySelect<S, T> select) {
		return new GroupByTable<S, T>(select.getGroupBy(), tableFor(select.getTable()));
	}

	public <T, V extends Comparable<? super V>> Table<T> table(OrderBySelect<T, V> select) {
		return new OrderByTable<T, V>(tableFor(select.getTable()), select.getOrderBy(), select.isAscendent());
	}

	public <L, R> Table<Pair<L, R>> table(InnerJoin<L, R> innerjoin) {
		Table<L> left = innerjoin.getLeftTable().asTable(this);
		Table<R> right = innerjoin.getRightTable().asTable(this);
		return new InnerJoinTable<L, R>(left, right, innerjoin.getOn());
	}

	// ##########################################################################
	// VISITOR CALL BACK FOR TABLE CREATION END
	// ##########################################################################

	// what is the source for a singleton ? for now the "latest" of a table
	// a table with a unique id
	// interesting... make it a table ?

	<T> Singleton<T> getSingleton(Property<T> prop) {
		Singleton<T> ton = values.get(prop);
		return ton;
	}

}
