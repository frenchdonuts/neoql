package net.ericaro.neoql;

import java.util.ArrayList;
import java.util.List;

import net.ericaro.neoql.StudentModel.Teacher;

import org.junit.Test;

public class OrderByTableTest {
	public static final ClassTableDef<Teacher> TEACHER = Teacher.TABLE;
	@Test
	public void testIterator() {
		
			Database db = new Database();
			db.execute(new Script() {{
				createTable(TEACHER) ;
				insertInto(TEACHER).set(Teacher.NAME, "Allison");
				insertInto(TEACHER).set(Teacher.NAME, "Madison");
				insertInto(TEACHER).set(Teacher.NAME, "Bedison");
				insertInto(TEACHER).set(Teacher.NAME, "Allison");
				insertInto(TEACHER).set(Teacher.NAME, "Madison");
				insertInto(TEACHER).set(Teacher.NAME, "Toto"   ); 
			}});
			
			
			Table<Teacher> table = db.table(TEACHER);
			// no EDSL for simple test
			OrderByTable<Teacher,String> gtable = new OrderByTable<Teacher, String>(table, Teacher.NAME, true);
			
			
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
