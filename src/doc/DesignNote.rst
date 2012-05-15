Design Notes for neoql
--------------------------

NEO QL is a new way to handle objects in a Java application. It's a middle man between an SQL way of handling data, and a _so called_ pure object.

In SQL tables represents objects, links are embedded as values and extracted through queries.

In  _so called_ object representation, objects and there relations are modelled all together. 
Data are represented as fields, and relations as a field too but its type is a list.

NEOQL approach is to provide a way to model just like in SQL, because we believe that this is the best way to do.
Hence, in NEOQL there are tables that are simply a list of objects.
A top level manager (called a database) manages those lists|tables.
It handles all the access, Create, Update Insert Delete, but also almost all SQL queries.

The power of the solution is that there is no "good" way to model relations in pure object.
There is always the case when you want to filter a list.
For instance, in a GUI where you want to select items among existing ones, there are two way to do:

  - two lists: the selected ones, and the unselected ones. The problem with this models is that in the GUI you might need to display all the available ones.
  
  - two lists: the selected ones, and all the availables.

The problem is that in the two solutions, you would not do the object operations (for instance selecting an items) in the same way:

  - to select you need to move the items from one list to the other one.
  
  - to select you need to copy the items from the all list to the selected list. 

This is just to describe the differences of implementations for just the selection behaviour.
In a real life GUI there a hundreds of such small behaviours.
But moving necessary
Hence wrong silution
 


