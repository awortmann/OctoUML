///*******************************************************************************
// * JetUML - A desktop application for fast UML diagramming.
// *
// * Copyright (C) 2016 by the contributors of the JetUML project.
// *
// * See: https://github.com/prmr/JetUML
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// *******************************************************************************/
package util.commands;


import javafx.scene.layout.Pane;
import model.AbstractEdge;
import model.Edge;
import model.Graph;
import model.Node;
import view.AbstractEdgeView;

/**
 * Stores the addition/removal of a node from the graph.
 * @author EJBQ
 */
public class AddDeleteEdgeCommand implements Command
{
    private Pane aPane;
    private AbstractEdgeView aEdgeView;
	private Edge aEdge;
	private Graph aGraph;
	private Node aP1;
	private Node aP2;
	private boolean aAdding; //true for adding, false for deleting

	/**
	 * Creates the command.
	 * @param pGraph The panel to add/delete the edge
	 * @param pEdge The edge to be added/deleted
	 * @param pAdding True when adding, false when deleting
	 */ //Pane pPane, AbstractNodeView pNodeView, AbstractNode pNode, Graph pGraph, boolean pAdding
	public AddDeleteEdgeCommand(Pane pPane, AbstractEdgeView pEdgeView, AbstractEdge pEdge, Graph pGraph, boolean pAdding)
	{
        aPane = pPane;
        aEdgeView = pEdgeView;
		aGraph = pGraph;
		aEdge = pEdge;
		aP1 = aEdge.getStartNode();
		aP2 = aEdge.getEndNode();
		aAdding = pAdding;
	}

	/**
	 * Undoes the command and adds/deletes the edge.
	 */
	public void undo()
	{
		if(aAdding)
		{
			delete();
		}
		else
		{
			add();
		}
	}

	/**
	 * Performs the command and adds/deletes the edge.
	 */
	public void execute()
	{
		if(aAdding)
		{
			add();
		}
		else
		{
			delete();
		}
	}

	/**
	 * Removes the node from the graph.
	 */
	private void delete()
	{
		aGraph.removeEdge(aEdge);
        aPane.getChildren().remove(aEdgeView);
	}

	/**
	 * Adds the edge to the graph at the points in its start and end node properties.
	 */
	private void add()
	{
		aGraph.connect(aP1, aP2, aEdge);
        aPane.getChildren().add(aEdgeView);
	}

}