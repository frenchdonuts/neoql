package net.ericaro.neoql.git;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.ericaro.neoql.patches.Patch;
import net.ericaro.neoql.patches.PatchBuilder;
import net.ericaro.neoql.patches.Patches;
import net.ericaro.neoql.tables.Pair;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.ShortestPathUtils;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.Graphs;

/** represents a bare "git" like repository, i.e pure history, and totally stateless.
 * 
 * Method are left public only for the sake of writing stuff around git. Do not use as a "standard" user.
 * 
 * @author eric
 *
 */
public class Repository {
	DirectedGraph<Commit, Patch>  graph; // The actual history
	private Commit	root;
	
	/** creates a new empty repository.
	 * 
	 */
	public Repository() {
		graph = new DirectedSparseMultigraph<Commit, Patch>();
		graph.addVertex(root = new Commit("init"));
	}
	
	/** called by git client to record a patch (i.e a change) into this repository history.
	 * 
	 * @param change the patch to be used as edge
	 * @param from the starting edge
	 * @param comment the comment to associate with the commit
	 * @return
	 */
	public Commit commit(Patch change, Commit from, String comment) {
		Commit to = new Commit(comment);
		graph.addEdge(change, from, to);
		return to;
	}
	/** creates a merge commit from two points.
	 * 
	 * @param local the local change
	 * @param localHead 
	 * @param remote
	 * @param remoteHead
	 * @param comment
	 * @return
	 */
	public Commit merge(Commit common, Commit localHead, Commit remoteHead, Patch middle, String comment) {
		
		
		PatchBuilder localTransaction = asPatchBuilder(localHead, common);
		localTransaction.apply(middle);// apply middle (i.e merge)
		Patch local = localTransaction.build();
		
		PatchBuilder remoteTransaction = asPatchBuilder(remoteHead, common);
		remoteTransaction.apply(middle);
		Patch remote = remoteTransaction.build();
		// accept a single patch as input and computes the left part and the right part to guarantee that those
		// two patches are consistent, i.e. the commit is the same no matter the path
		
		Commit to = new Commit(comment);
		graph.addEdge(local, localHead, to);
		graph.addEdge(remote, remoteHead, to);
		return to;
	}

	
	/** Compute the shortest path between two commits.
	 * Cave at: Patches return are potentially "reversed" so that to checkout you only need to apply those path. 
	 * Another consequence is that potentially the Path returned does not belong to the graph.
	 * 
	 * @param from
	 * @param target
	 * @return
	 */
	public Iterable<Patch> path(Commit from, Commit target) {
		// creates a undirected graph to compute distances
		SparseMultigraph<Commit, Patch> g = new SparseMultigraph<Commit, Patch>();
		for(Patch c: graph.getEdges())
			g.addEdge(c, graph.getEndpoints(c));
		DijkstraShortestPath<Commit, Patch> sp = new DijkstraShortestPath<Commit, Patch>(g) ;
		List<Patch> path = new ArrayList<Patch>();
		Commit temphead = from;
		
		List<Patch> path2 = ShortestPathUtils.getPath(graph, sp, from, target);
		for(Patch c: path2 ) {
			// changes are correct, but the "order" is not deduced at all
			Commit next = graph.getOpposite(temphead, c);
			if(graph.getSource(c).equals(temphead)) // the change is in the right order
            	path.add(c);
            else {
            	// change is backward
            	System.out.println("using path backward");
            	path.add(Patches.reverse(c) );
            }
			temphead = next;
            
		}
		assert temphead == target : "the computed path did not end into the target as expected";
		return path;
	}
	
	
	
	
	/** computes a common ancestor between two commits
	 * 
	 * if the common ancestor is from: then we are in a fastforward situation
	 * if the common ancestor is target: we are in a nothing to update situation
	 * 
	 * @param from
	 * @param target
	 * @return the common ancestor:
	 */
	public Commit commonAncestor(Commit from, Commit target) {
		if(from == target) {
			return from;
		}
		if ( graph.isPredecessor(from, target)) {
			// I'm ahead of the remote branch
			return from; // nothing to update
		}
		if ( graph.isPredecessor(target, from))
			return target; // fastfroward
		for( Commit c: graph.getPredecessors(from) )
			if ( graph.isPredecessor(target, c))
				return c;
		assert false: "there is no common ancestor, obviously";
		return null;
		}
	/** use path instead
	 * 
	 * @param from
	 * @param target
	 * @return
	 */
	@Deprecated
	public Iterable<Patch> changePath(Commit from, Commit target){
		return path(from, target);
	}
	
	/** build a patch builder "along" the shortest path.
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public PatchBuilder asPatchBuilder(Commit from, Commit to) {
		PatchBuilder builder = new PatchBuilder();
		for (Patch p : path(from, to ) )  
			builder.apply(p);
		return builder;
	}
	
	
	public String printPath(Branch from, Branch target) {
		return printPath(from.getCommit(), target.getCommit());
	}
	public String printPath(Commit from, Commit target) {
		StringBuilder sb = new StringBuilder();
		for(Patch p: changePath(from, target))
			sb.append(p).append("\n");
		return sb.toString();
	}

	public DirectedGraph<Commit, Patch> getGraph() {
		// TODO Auto-generated method stub
		return Graphs.unmodifiableDirectedGraph(graph);
	}
	

	public Commit getRoot() {
		return root;
	}

	public Commit getTargetOf(Patch p) {
		return graph.getDest(p);
	}

	
	
	 
	
	// TODO look for create a common ancestor lookup !
	
	
}