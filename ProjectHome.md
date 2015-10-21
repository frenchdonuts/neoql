NeoQL provides a static-typed, SQL-Like, in-memory, Java-centric, graphical user interface oriented, database system.

# Introduction #

NeoQL provide a lightweight database object that will manage a complex object's graph.

Objects are organized in sets by type, called TableData. These are the foundation of NeoQL database.

Then you will build derived list, like ( the `Student` whose name starts with an 'A' ).

NeoQL offers a way to express those derived list (called Table) much like you would do in SQL.
But in NeoQL changes made to any of the foundation tables are forwarded to the derived table, and you are notified. It is possible to build for instance swing ListModel or any other observer model.

NeoQL provide tools to modify the object's graph, much like SQL does, INSERT, UPDATE and DELETE operations are available on TableData level.
Those operations are made transactional, and the transaction object ( a ChangeSet ) can be reused to implement an Undo/Redo (or more complicated).

Objects are expected to be any type but should be immutable.
Objects are **not** persisted at all.
Hence, it is not dedicated at handling huge object's graph, but rather small ones but very close to the GUI, in fact NeoQL aim at beeing the database system the closest to the final GUI.


## Static Typed ##

Queries are static-typed, hence compiler-checked.

```

public static TableDef<Person> SELECTED = select(Person.TABLE, Person.SELECTED.is(true));
public static TableDef<Person> UNSELECTED = select(Person.TABLE,  Person.SELECTED.is(false));
public static TableDef<Person> ALL = select(Person.TABLE);
	
```

The Database Model is also static-typed. The above query applies for

```
public static class Person {
    public static final ClassTableDef<Person> TABLE = new ClassTableDef<Person>(Person.class);
    
    public static Column<Person, String > NAME     = TABLE.addColumn("name");
    public static Column<Person, Boolean> SELECTED = TABLE.addColumn("selected");

    private String			name;
    private boolean			selected = false;
}
```

## SQL-Like ##

NeoQL provides:

  * SELECT with arbitrary where clause
  * CREATE TABLE
  * DROP TABLE
  * INNER JOIN (more to come)
  * ORDER BY
  * SORTED BY
  * INSERT INTO
  * UPDATE WHERE
  * SELECT ALL  or select a single column

## in-memory ##

You can create as many database as you want. A database is just an instance of Database.
Database instances are fully isolated.

## Java Centric ##

Queries result can be `Iterator` or `Iterable`, and even observable through  `javax.swing.ListModel`

## graphical user interface oriented ##

Queries result can be plugged into JList or JTable. Changes made anywhere else in the database are propagated to the JList.
This is specially powerful for complex queries involving GROUP BY, or INNER JOIN.

Furthermore, NeoQL also provide a concept of singleton, that is compulsory for GUI.

For instance, in a GUI that involve a list of `Student`, the `selected Student` is a typical use case for singleton.

Singletons are observables too, and they follow changes on the database too.

# Objective #

NeoQL aim at beeing the database that is the closest to the final GUI. For that reason it has to be:
  * pure in-memory: several instances of the same panel may coexist with different data.
  * fully observable: in a GUI you need to be aware of every changes. No polling is acceptable.
  * static-typed: GUI code is an ever changing boiler plate code. You can't afford to check every SQL script every time you make some changes in the GUI Data model.



# Status #

2012-06-17