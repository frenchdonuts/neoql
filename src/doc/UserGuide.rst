==================================================
NeoQL User Guide
==================================================

A primer user guide to NeoQL
--------------------------------------------------

Introduction
--------------------------------------------------

.. |neoql| replace:: Neo QL


|neoql| is a new way to handle object's relation in a Java application.
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

The basic workflow defined for |neoql| is to:

	- model you `Domain Object`_:
	
		- as a `Relational Model`_
		- as `Immutable Object`_
		- Provide Column_ accessor for Table_
		
	- model your GUI `Relation Table`_ using |neoql|.
	  This is when you decide to provide a list of selected items, and the list of all available items
	- implement you edition operations using |neoql|. Also called the Service Layer
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