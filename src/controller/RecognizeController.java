package controller;

import edu.tamu.core.sketch.BoundingBox;
import edu.tamu.core.sketch.Point;
import edu.tamu.core.sketch.Shape;
import edu.tamu.core.sketch.Stroke;
import edu.tamu.recognition.paleo.PaleoConfig;
import edu.tamu.recognition.paleo.PaleoSketchRecognizer;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import model.*;
import util.commands.AddDeleteEdgeCommand;
import util.commands.AddDeleteNodeCommand;
import util.commands.AddDeleteSketchCommand;
import util.commands.CompoundCommand;
import view.AbstractEdgeView;
import view.AbstractNodeView;

import java.util.ArrayList;
import java.util.List;

/**
 * Used by MainController for handling the recognition of drawn shapes and transforming them in to UML-notations.
 */
public class RecognizeController {
    private Pane aDrawPane;
    private MainController mainController;
    private PaleoSketchRecognizer recognizer;
    private Graph graph;


    public RecognizeController(Pane pDrawPane, MainController pController) {
        aDrawPane = pDrawPane;
        mainController = pController;
        graph = mainController.getGraphModel();

        //TODO Find a nicer solution for this:
        //This is to load the recognizer when starting app, not when starting to draw.
        recognizer = new PaleoSketchRecognizer(PaleoConfig.allOn());
        Stroke init = new Stroke();
        init.addPoint(new Point(0,1));
        recognizer.setStroke(init);
        recognizer.recognize().getBestShape();
    }

    public synchronized void recognize(List<Sketch> sketches) {
        ArrayList<AbstractNode> recognizedNodes = new ArrayList();
        ArrayList<Sketch> sketchesToBeRemoved = new ArrayList<>();
        ArrayList<AbstractEdge> recognizedEdges = new ArrayList<>();
        CompoundCommand recognizeCompoundCommand = new CompoundCommand();

        sketchesToBeRemoved = new ArrayList<>();

        //Go through all sketches to find Nodes.
        for (Sketch s : sketches) {
            if (s.getStroke() != null && s.getStroke().getPoints() != null && !s.getStroke().getPoints().isEmpty()) {
                //TODO This sometimes throws IndexOutOfBoundsException...
                recognizer.setStroke(s.getStroke());
                Shape bestMatch = recognizer.recognize().getBestShape();
                String bestMatchString = bestMatch.getInterpretation().label;
                if (bestMatchString.equals("Square") || bestMatchString.equals("Rectangle")) {
                    BoundingBox box = s.getStroke().getBoundingBox();
                    ClassNode node = new ClassNode(box.getX(), box.getY(),box.getWidth(), box.getHeight());
                    s.setRecognizedElement(node);
                    recognizedNodes.add(node);
                    sketchesToBeRemoved.add(s);
                    graph.addNode(node, false);
                }

            }
        }

        //Go through all sketches to find edges.
        for (Sketch s : sketches) {
            if (s.getStroke() != null) {
                recognizer.setStroke(s.getStroke());
                String bestMatchString = recognizer.recognize().getBestShape().getInterpretation().label;

                if (bestMatchString.equals("Line") || bestMatchString.startsWith("Polyline") || bestMatchString.equals("Arc") ||
                        bestMatchString.equals("Curve") || bestMatchString.equals("Arrow")){

                    Point2D startPoint = new Point2D(s.getStroke().getFirstPoint().getX(), s.getStroke().getFirstPoint().getY());
                    Point2D endPoint = new Point2D(s.getStroke().getLastPoint().getX(), s.getStroke().getLastPoint().getY());
                    Node startNode = graph.findNode(startPoint);
                    Node endNode = graph.findNode(endPoint);

                    //For arrows, which don't have an endpoint
                    List<Point> points = s.getStroke().getPoints();
                    for (int i = points.size()-1; i > points.size()/2; i--) {
                        Point2D point = new Point2D(points.get(i).getX(), points.get(i).getY());
                        if (graph.findNode(point) != null) {
                            endNode = graph.findNode(point);
                            break;
                        }
                    }

                    if (startNode != null && endNode != null && !startNode.equals(endNode)) {
                        AssociationEdge newEdge = new AssociationEdge(startNode, endNode);
                        if (bestMatchString.equals("Arrow")) {
                            newEdge.setDirection(AbstractEdge.Direction.END_TO_START);
                        }
                        s.setRecognizedElement(newEdge);
                        sketchesToBeRemoved.add(s);
                        recognizedEdges.add(newEdge);
                    }
                }
            }
        }

        for(AbstractNode node : recognizedNodes){
            AbstractNodeView nodeView = mainController.createNodeView(node, false);
            recognizeCompoundCommand.add(new AddDeleteNodeCommand(mainController, graph, nodeView, node, true));
        }
        for(AbstractEdge edge : recognizedEdges){
            AbstractEdgeView edgeView = mainController.addEdgeView(edge, false);
            if (edgeView != null) {
                recognizeCompoundCommand.add(new AddDeleteEdgeCommand(mainController, edgeView, edge, true));
            }
        }

        for(Sketch sketch : sketchesToBeRemoved){
            mainController.deleteSketch(sketch, recognizeCompoundCommand, false);

        }
        if(recognizeCompoundCommand.size() > 0){
            mainController.getUndoManager().add(recognizeCompoundCommand);
        }

    }
}
