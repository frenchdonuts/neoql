package net.ericaro.osql.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
// TODO handle all the features of a sql database:
// foreign key: meaning that updating a row require to change it also in all the foreign keys.
// group by and order by in selects
// joins -> Couple of values
//
public class Database {
	Map<Class, TableData>	tables		= new HashMap<Class, TableData>();

	// act as a transaction
	List<Operation<?>>		operations	= new ArrayList<Operation<?>>();
	Callbacks				callbacks	= new Callbacks();

	
	
	
	
	public <T> void addDatabaseListener(Class<T> table, DatabaseListener<T> listener) {
		callbacks.addDatabaseListener(table, listener);
	}

	public <T> void removeDatabaseListener(Class<T> table, DatabaseListener<T> listener) {
		callbacks.removeDatabaseListener(table, listener);
	}
	
	public <T,V> void addColumnListener(Column<T,V> column,
			ColumnListener<V> listener) {
		callbacks.addColumnListener(column, listener);
	}

	public <T,V> void removeColumnListener(Column<T,V> column,
			ColumnListener<V> listener) {
		callbacks.removeColumnListener(column, listener);
	}

	public void beginTransaction() {
		operations.clear();
		callbacks.transactionBegun();
	}

	public void commit() {
		while (operations.size()>0) {
			Operation<?> next = operations.remove(0);
			next.run(this);
		}
		callbacks.transactionCommitted();
		assert operations.size() == 0 : "commit has not flushed all the operations";
	}

	public <T> void createTable(Class<T> c) {
		schedule(new CreateTable(c));
	}

	public <T> Insert<T> insertInto(Class<T> table) {
		return schedule(DQL.insertInto(table));
	}

	public <T> Update<T> update(Class<T> table) {
		return schedule(DQL.update(table));
	}

	public <T> DeleteFrom<T> deleteFrom(Class<T> table) {
		return schedule(DQL.deleteFrom(table));
	}

	private <T extends Operation<?>> T schedule(T operation) {
		operations.add(operation);
		return operation;
	}

	public <T> SelectList<T> select(Class<T> table, Where<? super T> p) {
		Select<T> o = DQL.select(table, p);
		return run(o);
	}

	public void run(CreateTable createTable) {
		add(createTable.getTable());
	}

	public void add(Class... tables) {
		for (Class table : tables) {
			TableData<?> data = new TableData(this, table);
			this.tables.put(table, data);
			callbacks.tableCreated(table);
		}
	}

	public <T> void run(Insert<T> insert) {
		Class<T> table = insert.getTable();
		TableData<T> data = getDataFor(table);
		T row = data.append(insert.getRow() );
		callbacks.rowInserted(table, row);
	}

	protected <T> TableData<T> getDataFor(Class<T> table) {
		return tables.get(table);
	}

	public <T> void run(Update<T> update) {
		Class<T> table = update.getTable();
		TableData<T> data = getDataFor(table);
		Where<? super T> where = update.getWhere();
		Setter<T,?>[] setters = update.getSetters();
		for (T row : data)
			if (where.isTrue(row)) {
				T clone = data.clone(row);
				for (Setter<T,?> s : setters) {
					s.set(clone);
					callbacks.columnUpdated(s, row);
				}
				data.update(row, clone);
				callbacks.rowUpdated(table, row, clone);
			}
	}

	public <T> void run(DeleteFrom<T> deleteFrom) {
		Class<T> table = deleteFrom.getTable();
		TableData<T> data = getDataFor(table);
		Where<? super T> where = deleteFrom.getWhere();
		for (ListIterator<T> i = data.listIterator(); i.hasNext();) {
			T row = i.next();
			if (where.isTrue(row)) {
				i.remove();
				callbacks.rowDeleted(table, row);

			}
		}
	}


	public <T> SelectList<T> run(Select<T> select) {
		return new SelectList<T>(select, this.tables.get(select.table) );
		
	}
}
