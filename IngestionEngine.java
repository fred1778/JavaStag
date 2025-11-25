package edu.uob;
import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Id;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

public class IngestionEngine {

    private File entityFile;
    private File actionFile;
    private Game newGame = new Game();

    private boolean suceesfulIngestion = false;

    public boolean getIngestionState(){
        return this.suceesfulIngestion;
    }

    public IngestionEngine(File entityFile, File actions) {
        this.entityFile = entityFile;
        this.actionFile = actions;
        try {
            this.parseEntity();
            this.parseActions();
        }
        catch(RuntimeException runtimeEx){
            this.suceesfulIngestion = false;
        }
        this.suceesfulIngestion = true;
    }

    public Game getGame(){
        return this.newGame;
    }

    private Vector<String> buildActionComponents(NodeList nodes, String subTag) {
        Vector<String> components = new Vector<>();
        Element component = (Element) nodes.item(0);
        if (!component.hasChildNodes()) {
            String element = component.getTextContent();
            components.add(element);
        } else{
            NodeList values = component.getElementsByTagName(subTag);
            for (int i = 0; i < values.getLength(); i++) {
                String element = values.item(i).getTextContent();
                components.add(element);
            }
        }
        return components;
    }

    private void parseActions() {
        HashMap<String, String> actionComponents = new HashMap<>();
        actionComponents.put("triggers" , "keyphrase");
        actionComponents.put("subjects" , "entity");
        actionComponents.put("consumed", "entity");
        actionComponents.put("produced", "entity");
        try{
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document actionDoc = docBuilder.parse(this.actionFile);
            Element root = actionDoc.getDocumentElement();
            NodeList actionNodes = root.getChildNodes();
            for (int i = 1; i < actionNodes.getLength(); i += 2) {
                 GameAction newAction = new GameAction();
                    Element action = (Element) actionNodes.item(i);
                    for(String actionComponent : actionComponents.keySet()) {
                        newAction.setActionComponents(this.buildActionComponents(action.getElementsByTagName(actionComponent), actionComponents.get(actionComponent)), actionComponent);
                        this.newGame.addGameAction(newAction);
                    }
                   String narrative =  action.getElementsByTagName("narration").item(0).getTextContent();
                    newAction.setNarrative(narrative);
                }
        } catch (IOException | ParserConfigurationException e) {
            this.suceesfulIngestion = false;
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }


    public void parseEntity() {
        Parser parser = new Parser();
        try { parser.parse(this.readEntityFile());
        } catch (Exception parseException) {
            this.suceesfulIngestion = false;
          }

        Iterator<Graph> graphs = parser.getGraphs().iterator();
        Id locId = new Id();
        locId.setId("locations");
        Graph mainGraph = graphs.next();
        Graph locationGraph = mainGraph.getSubgraphs().get(0);
        this.newGame.setLocations(GameLocation.buildEntityList(EntityType.location, locationGraph));
        this.setPathways(mainGraph.getSubgraphs().get(1), this.newGame);
    }


    private void setPathways(Graph graph, Game game) {
        Iterator<Edge> edges = graph.getEdges().iterator();
        game.setPathways(edges);
    }

    private Reader readEntityFile() throws IOException {
        return new FileReader(this.entityFile);
    }


}
