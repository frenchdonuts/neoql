package net.ericaro.neoql;

import java.util.Iterator;

/**
 * Group By group items togethers
 * 
 * 
 */
class GroupByTable<S, T> implements Table<T> {
// TODO handle counts
// theory
	/* (sorry for the french this is related to theories teached in french
	 * une propriété collectivisante est définie, elle définie une class d'équivalence, et un des représentants est retourné
	 * Mais pour pouvoir faire des count() il faut plus.
	 * il faut des "projecteur" ( équivalent de columns) qui permettent d'accumuler les résultats.
	 * le projecteur qui à un item associe son représentant est le projecteur par défaut ( dans select name () group by name
	 * 
	 * mais d'autres projecteurs peuvent être employés: 
	 *   - count()
	 *   - max/min/avg ( column)
	 *   en même temps cela ressemble fort à la feature (count, max ) usuelle, quel est le lien ?
	 *   
	 *   il ne peut s'agire du même count() 
	 *   dans un cas count accumule, dans un représentant de la classe
	 *   dans l'autre count accumule dans un unique représentant pout toute la query
	 *   max: accumule le max dans un représentant
	 *   => les représentant doivent être accumulable.
	 *   
	 *   accumulable => necessité de pouvoir maintenir le résultat pour l'ajout suppression update (équivalent à un ajout suppresion)
	 *   => c'est chaud banane pour des trucs comme le "max" (on peut être amené à tout recalculer !)
	 *   donc: l'accumulabilité peut aller d'un rapide et faible en mémoire ( count, average)
	 *   à un potentiel énorme en mémoire (max )
	 *   
	 *   le principe est donc :
	 *   ajout suppression update d'un item :
	 *   ( group by c'est chaud banane aussi pour la mémoire, quand est ce qu'on tue un représentant d'une classe ?
	 *   
	 *   
	 *   callback à un "accumulateur" f(accumulable )
	 *   
	 *   accumulateur : peut avoir une mémoire interne, peut avoir accès à l'intégralité de la table.
	 *   il est acceptable de ne permettre qu'un set fixé d'accumulateur (intégré avec la table)
	 *   
	 *   
	 *   et si :
	 *   x = collect( row )
	 *   if x not in keys:
	 *   	keys.put(x, new Accu )
	 *   	fire_added( accu ) # does not comply with the "imutable rule"
	 *   keys.get(x).accumule( x )
	 *   
	 *   
	 *   
	  
	  
	 */
	
	private Table<S> table;
	private TableListenerSupport<T> events = new TableListenerSupport<T>();
	private Mapping<S, T> mapping;
	private TableListener<S> listener;

	GroupByTable(Mapper<S,T> mapper, Table<S> table) {
		super();
		this.table = table;
		this.mapping = new MapMapper<S, T>(mapper);
		for (S s : table)
			events.fireInserted(mapping.push(s)); // cause events to be fire just like if the items where appended

		// first fill the filtered table
		// then add events to keep in touch with list content
		listener = new TableListener<S>() {

			public void inserted(S row) {
				events.fireInserted(mapping.push(row));
			}

			public void deleted(S row) {
				events.fireDeleted(mapping.pop(row));
			}

			public void updated(S old, S row) {
				events.fireUpdated(mapping.pop(old), mapping.push(row));
			}
		};
		table.addTableListener(listener);
	}

	
	
	@Override
	public void drop(Database from) {
		table.removeTableListener(listener);
	}



	@Override
	public Iterator<T> iterator() {
		final Iterator<S> i = table.iterator();
		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return i.hasNext();
			}

			@Override
			public T next() {
				return mapping.peek(i.next());
			}

			@Override
			public void remove() {
				i.remove();
			}

		};
	}

	public void addTableListener(TableListener<T> l) {
		events.addTableListener(l);
	}

	public void removeTableListener(TableListener<T> l) {
		events.removeTableListener(l);
	}

}