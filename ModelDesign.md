# WIP: Guide lines to design a model in NEoQL.

# Model Design Step by Step #


## Model ##

First, all you have to do is to create a !Model object.

A !Model is made of !Tables and !Relation. Every definition is splitted into a static part, and  an instance part.

### Static ###


_Relations_ are defined in the model, as TableDef, just like that:

```
public static final TableDef<MyEntity> SELECTED_STUDENTS = <the table def definition> ;
```

You can think of relations just like 'virtual' tables.

Note: Do not skip 'trivial' relations (like 'all students' for instance) because they are 'trivial'. If sometime in the future you need to change this definition, it will not be possible.

### Instance ###

Instance definition for tables and relations are made by executing a building Script on the database.

```

database.execute(new Script(){{
	//create actual table
	createTable(Student.TABLE);
	createTable(Teacher.TABLE);
	
	// create virtual ones
	createTable(SELECTED_STUDENTS);
	...
	
}} );
```

That's it the `database` is ready to operate


## Table ##

A !Table is fully  defined by a ClassTableDef instance. The best pratice we
recommend is to define the table within the class just like that:

```
public class Student{
public static final ClassTableDef<Student> TABLE = <the table def definition> ;
}
```

A TableDef is made of `Column`s too,  and as column are often reused, we recommend that you keep track of those columns
```
public class Student{
public static final ClassTableDef<Student> TABLE = <the table def definition> ;

public static final Column<Student,String> NAME = TABLE.addColumn(<column definition goes here>) ;

}
```

## Relation ##

NeoQL support relations as foreign key. use instance instead of id.

```
public static final Column<Student,Teacher> NAME = TABLE.addColumn(Teacher.TABLE) ;

... further in the object

private Teacher teacher;

```

This will establish a foreign key relation beteen the Student table and the Teacher one.


## Database ##

Can't be simpler.
```
new Database();
```

A Database should be a private field of the _Model_.

## Observable ##

Observables can be retrieve easily from the !Database instance
```
public ListModel getSelectedStudents(){
	return database.listFor(SELECTED_STUDENTS) ;
	}
```