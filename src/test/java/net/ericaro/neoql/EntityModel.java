package net.ericaro.neoql;


public class EntityModel {

		public static final ClassTableDef<EntityA> ENTITYA = EntityA.TABLE;
		public static final ClassTableDef<EntityB> ENTITYB = EntityB.TABLE;
		
		
		 public static class EntityA{
			 public static final ClassTableDef<EntityA> TABLE = new ClassTableDef<EntityA>(EntityA.class); 
			 
			 
			public static final Column<EntityA, String> CODE = TABLE.addColumn("code");
			public static final Column<EntityA, String> NAME = TABLE.addColumn("name");
			
			
			String code;
			String name;
			 String getCode() {
				return code;
			}
			 String getName() {
				return name;
			}
			@Override
			public  String toString() {
				return "EntityA [code=" + code + ", name=" + name + "]";
			}
			
			
		}
		 public static class EntityB{
			 public static final ClassTableDef<EntityB> TABLE = new ClassTableDef<EntityB>(EntityB.class);
			 
			 static final Column<EntityB, String> CODE = TABLE.addColumn("code");
			 static final Column<EntityB, String> NAME = TABLE.addColumn("name");
			 
				
			String code;
			String name;
			 String getCode() {
				return code;
			}
			 String getName() {
				return name;
			}
			@Override
			public  String toString() {
				return "EntityB [code=" + code + ", name=" + name + "]";
			}
			
		}	
}
