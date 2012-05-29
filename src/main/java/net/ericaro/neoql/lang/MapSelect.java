package net.ericaro.neoql.lang;

import java.util.Iterator;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.MappedTable;
import net.ericaro.neoql.MappedTable.MappedIterator;
import net.ericaro.neoql.system.Mapper;
import net.ericaro.neoql.system.Table;
import net.ericaro.neoql.system.TableDef;


/**
 * SELECT * FROM table WHERE
 * 
 * @author eric
 * 
 * @param <T>
 */
public class MapSelect<S, T> implements TableDef<T> {

	Mapper<S, T> mapper;
	private TableDef<S> table;

	MapSelect(Mapper<S, T> mapper, TableDef<S> table) {
		this.table = table;
		this.mapper = mapper;
	}

	public Mapper<S, T> getMapper() {
		return mapper;
	}

	public TableDef<S> getTable() {
		return table;
	}

	@Override
	public Table<T> asTable(Database database) {
		return database.table(this);
	}

	@Override
	public Iterator<T> iterator(Database database) {
		return new MappedTable.MappedIterator<S, T>(database.iterator(table), mapper);
	}
	
	

	
}