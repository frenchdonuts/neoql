package net.ericaro.osql.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;


public class Database {
	Map<Class, TableData> tables = new HashMap<Class, TableData>();
	
	List<Operation<?>> operations = new ArrayList<Operation<?>>();
	
	
	public void beginTransaction() {
		operations.clear();
	}
	
	public void commit() {
		for(ListIterator<Operation<?>> i = operations.listIterator();i.hasNext();i.remove()) 
			i.next().run(this);
		assert operations.size() == 0 :"commit has not flushed all the operations" ;
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
	
	public DeleteFrom deleteFrom(Class table) {
		return schedule(DQL.deleteFrom(table));
	}
	
	
	private <T extends Operation<?>> T schedule(T operation) {
		operations.add(operation);
		return operation;
	}
	
	public Iterable<Object[]> select(Projector what, Class table, Predicate p) {
		Select o = DQL.select(what, table, p);
		return run(o);
		
	}
	
	public <T> Iterable<T> select(Class<T> table, Where<? super T> p) {
		SingleSelect<T> o = DQL.select(table, p);
		return run(o);
		
	}
	
	
	public void run(CreateTable createTable) {
		add(createTable.getTable());
	}

	public void add(Class... tables) {
		for (Class table: tables)
			this.tables.put(table, new TableData(table));
	}
	
	public void run(Insert insert) {
		Class<?> table = insert.getTable() ;
		TableData<?> data = tables.get(table);
		Object row = data.newRow();
		for (Setter s : insert.getSetters() )
			s.set(row);
	}

	public void run(Update update) {
		TableData data = tables.get(update.getTable());
		Where where = update.getWhere();
		Setter[] setters = update.getSetters();
		for (Object row : data)
			if (where.isTrue(row))
				for (Setter s : setters)
					s.set(row);
	}
	

	public void run(DeleteFrom deleteFrom) {
		TableData data = tables.get(deleteFrom.getTable());
		Predicate where = deleteFrom.getWhere();
		for (ListIterator<Object[]> i = data.listIterator(); i.hasNext();) 
			if (where.eval(i.next()))
				i.remove() ;
	}

	public Iterable<Object[]> run(Select select) {
		return select.new SelectIterable(this.tables.get(select.table) );
	}
	public <T> Iterable<T> run(SingleSelect<T> select) {
		return select.new SelectIterable(this.tables.get(select.table) );
	}
}
