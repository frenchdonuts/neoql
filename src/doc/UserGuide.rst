=NeoQL User Guide=


==Concepts 1==

NeoQL is a Data Manager suitable for Model for GUI.

Modeling for a GUI require that:
  * data are stored in POJO (or almost POJO)
  * collections of such  data are observables
  * singleton is a special case of collection (i.e. observable too)
  * data operations are made independently of collections.

==Data stored in Pojo==

Because its the most natural way to handle a piece of data. Managing students, is more natural like that:
{{{
studentLabel.setText( student.getName() );
}}}  

rather thant
{{{
studentLabel.setText( data[NAME_COLUMN] );
}}}

==Collection are Observables==

Every time you have a JList of JTable you want a ListModel to plug-in. The Model must provide it. Nevertheless the
list model can't be the list of all available students. We need a way to select the student in the lists ( hence Query )

==Singleton==
 singletons are required in GUI. For instance, the "beeing edited Student" for instance.


==Concepts 2==

NeoQL is a new way to handle object's relation in a Java application.
Vastly inspired by Database modelling it enforces some Object Programming best practices.

It shares most of the operations available in an SQL database like:

  - select
  
    - order by
    - group by
    - `*` or a single column
  - joins
  - create table
  - drop table
  - insert into
  - update 
  - delete from

A **Table** is basically a list of Objects of a certain *Type*.

There a some few requirement on those Objects :

  - Objects are expected to be  `Immutable Object`_
  - Objects implements a `Relational Model`_

A **Column** is basically a static accessor to the `Immutable Object`_ attributes ( get and set). Just like java ``Field`` object.


Workflow
-------------------------------------------------------------

The basic workflow defined for NeoQL is to:

	- model you `Domain Object`_:
	
		- as a `Relational Model`_
		- as `Immutable Object`_
		- Provide Column_ accessor for Table_
		
	- model your GUI `Relation Table`_ using NeoQL.
	  This is when you decide to provide a list of selected items, and the list of all available items
	- implement you edition operations using NeoQL. Also called the Service Layer
	- connect the `Relation Table`_ to their corresponding observers (JTable, JList, ... ).

Relational Modeling
================================================

Relation Modeling is all about defining entities, and relations.

An Entity is modeled using a pure-java class, that defines an `Immutable Object`_.

::
	
	public class 

Its attributes are modeled

 
Definitions
=================================================

.. _table:

Table
	The result of a Query, on other tables, or a `Primary Table`_
	
.. _primary table:

Primary Table
	A List of `Domain Object`_


.. _column:

Column
	Object providing getter, and setter and identification to an object's attribute, 

.. _relation table:

Relation Table
	Table defined by a query.

.. _Domain Object:

Domain Object
	A Plain Old Java Object that is modelling the current application domain.
	If you are build a Computer Store, this would be ``Computer``, ``Shelve``, ``Keyboard``

.. _Immutable Object: http://en.wikipedia.org/wiki/Immutable_object
.. _Relational Model: http://en.wikipedia.org/wiki/Relational_model