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

The solution aim at answering a poorly adressed issue: middle man model.
We all know of solution for the hardcore model ( ORM, database, JPA, JDO etc.).
But we are left on our own to handle models between _Entities_ (i.e. objects extracted from the hardcore model) and the GUI.
There is a necessary data buffer between both.
The final GUI modeled everything the user can see:

  - selection
  - button status
  - list order
  - filtering
  -  ...

The hardcore model don't bother handling those stuff, fortunately.
Nevertheless, GUI developper do have to bother, this is their job, and unfortunately there is no "good" tool to adress this issue.   
NeoQL aim at solving this necessary in-between data-buffer.

The idea behind NeoQL was to model data reusing the benefits of SQL-like data management.
Because after at least 15 years of developping GUI we believe that there is no "good" way to model relations in pure object.
relation tends to be modelled as collection attached to the object itself.
Those collections should never be editable, for the same reason OOP recommends to hide data, and expose methods instead.
If you expose those collections as editable collections, then in the GUI Controller you will add/remove items from those collections.
Unfortunately, those collections are linked together by hidden relations (like selected list and unselected list, one beeing exlusive of the other).
Therefore, the GUI Controller decides wich items goes where, therefore it's hard to change those list definition.
So exposing editable list moves their definition into the client code, there is no need to explain why this is bad.

Let's sum it up in a quick use case.
In a GUI where you want to select items among existing ones, there are two way to do:

  - two lists: the selected ones, and the unselected ones. You drag and drop items from one list to another.
  
  - two lists: the selected ones, and all the availables. You display the available list, along with a check box and check selected items.


Hence wrong silution
 


