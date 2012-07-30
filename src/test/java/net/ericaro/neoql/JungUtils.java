package net.ericaro.neoql;

import javax.swing.JDialog;

import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;

public class JungUtils {

	public static <V, E> void disp(Graph<V, E> g) {
		disp(g, false);
	}

	public static <V, E> void disp(Graph<V, E> g, boolean modal) {
		disp(g, modal, true, true);
	}

	public static <V, E> void disp(Graph<V, E> g, boolean modal, boolean showEdges, boolean showVertices) {

		JDialog jf = new JDialog();
		jf.setModal(modal);
		// Layout<V, E> layout = new FRLayout<V, E>(g);
		Layout<V, E> layout = new ISOMLayout<V, E>(g);

		// Layout<Integer, String> layout = new CircleLayout(g);
		VisualizationViewer<V, E> vv = new VisualizationViewer<V, E>(layout);
		// Show vertex and edge labels
		if(showVertices) vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
		if(showEdges)  vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
		// Create a graph mouse and add it to the visualization component
		DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
		gm.setMode(Mode.PICKING);
		vv.setGraphMouse(gm);

		jf.getContentPane().add(new GraphZoomScrollPane(vv));

		jf.setBounds(100, 100, 800, 600);
		jf.setVisible(true);
	}

}
