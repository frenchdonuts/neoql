package net.ericaro.osql.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.ListModel;

public class Database {
	Map<Class, TableData>	tables		= new HashMap<Class, TableData>();

	// act as a transaction
	List<Operation<?>>		operations	= new ArrayList<Operation<?>>();
	Callbacks				callbacks	= new Callbacks();

	
	
	
	
	public <T> void addDatabaseListener(Class<T> table, DatabaseListener<T> listener) {
		callbacks.addDatabaseListener(table, listener);
	}

	public void removeDatabaseListener(Class table, DatabaseListener listener) {
		callbacks.removeDatabaseListener(table, listener);
	}

	public void beginTransaction() {
		operations.clear();
		callbacks.transactionBegun();
	}

	public void commit() {
		for (ListIterator<Operation<?>> i = operations.listIterator(); i.hasNext(); i.remove())
			i.next().run(this);
		callbacks.transactionCommitted();
		assert operations.size() == 0 : "commit has not flushed all the operations";
	}

	public void createTable(Class c) {
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

			TableData data = new TableData(this, table);
			this.tables.put(table, data);
			callbacks.tableCreated(table);
		}
	}

	public <T> void run(Insert<T> insert) {
		Class<T> table = insert.getTable();
		TableData<T> data = getDataFor(table);
		T row = data.newRow();
		for (Setter<T, ?> s : insert.getSetters())
			s.set(row);
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
				for (Setter<T,?> s : setters)
					s.set(clone);
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
