package net.ericaro.neoql;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import net.ericaro.neoql.Model.Teacher;
import net.ericaro.neoql.lang.Script;

import org.junit.Test;

public class OrderByTableTest {

	@Test
	public void testIterator() {
		
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
			OrderByTable<Teacher,String> gtable = new OrderByTable<Teacher, String>(table, Teacher.NAME);
			
			
			List<String> res = new ArrayList<String>();
			for(Teacher s: gtable)
				res.add(s.getName());
			
			List<String> gold= new ArrayList<String>();
			gold.add("Allison");
			gold.add("Allison");
			gold.add("Bedison");
			gold.add("Madison");
			gold.add("Madison");
			gold.add("Toto"   );
			
			assert gold.equals(res) : "failed to get the write result" ;
	}

}
