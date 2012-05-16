package net.ericaro.neoql;

import java.util.HashSet;
import java.util.Set;

import net.ericaro.neoql.Model.Teacher;
import net.ericaro.neoql.lang.Script;

import org.junit.Test;


public class GroupByTableTest {

	@Test public void testSimple() {
		Database db = new Database();
		db.execute(new Script() {{
			createTable(Teacher.class) ;
			insertInto(Teacher.class).set(Teacher.NAME, "Allison");
			insertInto(Teacher.class).set(Teacher.NAME, "Madison");
			insertInto(Teacher.class).set(Teacher.NAME, "Bedison");
			insertInto(Teacher.class).set(Teacher.NAME, "Allison");
			insertInto(Teacher.class).set(Teacher.NAME, "Madison");
			insertInto(Teacher.class).set(Teacher.NAME, "Toto"   ); 
		}});
		
		
		Table<Teacher> table = db.table(Teacher.class);
		// no EDSL for simple test
		GroupByTable<Teacher, String> gtable = new GroupByTable<Teacher, String>(Teacher.NAME, table);
		
		
		HashSet<String> res = new HashSet<String>();
		for(String s: gtable)
			res.add(s);
		Set<String> gold = new HashSet<String>();
		gold.add("Allison");
		gold.add("Madison");
		gold.add("Bedison");
		gold.add("Allison");
		gold.add("Madison");
		gold.add("Toto"   );
		
		assert gold.equals(res) : "failed to get the write result" ;
	}
	
}
