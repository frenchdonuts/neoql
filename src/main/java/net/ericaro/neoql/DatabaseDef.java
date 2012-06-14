package net.ericaro.neoql;

import java.util.ArrayList;
import java.util.List;

import net.ericaro.neoql.lang.ClassTableDef;

/** A database definition: provide help to define:
 * <ul>
	 * <li>tables</li>
	 * <li>queries</li>
	 * <li>singletons</li>
 * </ul>
 * 
 * @author eric
 *
 */
public class DatabaseDef {

	private List<ClassTableDef>	tableDefs = new ArrayList<ClassTableDef>();


	public DatabaseDef() {
		super();
	}
	
	
	public <T> ClassTableDef<T> newTable( Class<T> tableClass){
		ClassTableDef<T> def = new ClassTableDef<T>(tableClass);
		tableDefs .add(def);
		return def;
	}
	
	
	
	
	public Iterable<ClassTableDef> getTables(){
		return tableDefs;
	}
	
	
	

}
