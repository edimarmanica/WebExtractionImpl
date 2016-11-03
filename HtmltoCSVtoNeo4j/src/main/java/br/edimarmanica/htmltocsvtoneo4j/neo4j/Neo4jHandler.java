/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.htmltocsvtoneo4j.neo4j;

import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Site;
import br.edimarmanica.htmltocsvtoneo4j.csv2neo4j.RelTypes;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

/**
 *
 * @author edimar
 */
public class Neo4jHandler {

    private final GraphDatabaseService graphDb;
    private final Site site;
    private final static String DB_FILE_NAME = "graph.db";

    public Neo4jHandler(Site site) {
        this.site = site;
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(new File(getDBPath()));
        registerShutdownHook(graphDb);
    }

    public Result executeCypher(String cypher) {
        return graphDb.execute(cypher);
    }

    public Result executeCypher(String cypher, Map<String, Object> params) {
        return graphDb.execute(cypher, params);
    }

    /**
     *
     * @param properties
     * @return the node created
     */
    public Node insertNode(Map<String, String> properties) {
        Node node = graphDb.createNode();
        for (String key : properties.keySet()) {
            node.setProperty(key, properties.get(key));
        }
        return node;
    }

    /**
     * insert the new node and create a relationalShip with its parent node
     *
     * @param properties
     * @param parentNode
     * @return
     */
    public Node insertNode(Map<String, String> properties, Node parentNode) {
        Node node = graphDb.createNode();
        for (String key : properties.keySet()) {
            node.setProperty(key, properties.get(key));
        }

        if (parentNode != null) {
            insertRelationship(parentNode, node, properties);
        }
        return node;
    }

    public void insertRelationship(Node from, Node to, Map<String, String> properties) {
        Relationship relationship = from.createRelationshipTo(to, RelTypes.has_child);

        if (properties != null) {
            for (String key : properties.keySet()) {
                relationship.setProperty(key, properties.get(key));
            }
        }
    }

    public List<Object> querySingleColumn(String cypherQuery, Map<String, Object> parameters, String columnName) {
        List<Object> results = new ArrayList<>();

        Iterator<Map<String, Object>> iterator;
        if (parameters == null) {
            iterator = executeCypher(cypherQuery);
        } else {
            iterator = executeCypher(cypherQuery, parameters);
        }

        while (iterator.hasNext()) {
            Map<String, Object> st = iterator.next();
            results.add(st.get(columnName));
        }
        return results;
    }

    public List<Object> querySingleColumn(String cypherQuery, String columnName) {
        return querySingleColumn(cypherQuery, null, columnName);
    }

    /**
     *
     * @param cypherRule a cypher rule
     * @return the values extracted by the cypher rule (Map<URL,
     * ExtractedValue>).
     */
    public Map<String, String> extract(String cypherRule, String keyColumn, String valueColumn) {
        return extract(cypherRule, null, keyColumn, valueColumn);
    }

    public Map<String, String> extract(String cypherRule, Map<String, Object> params, String keyColumn, String valueColumn) {
        Map<String, String> extractedValues = new HashMap<>();

        Iterator<Map<String, Object>> iterator;
        if (params == null) {
            iterator = executeCypher(cypherRule);
        } else {
            iterator = executeCypher(cypherRule, params);
        }

        while (iterator.hasNext()) {
            Map<String, Object> map = iterator.next();
            extractedValues.put(map.get(keyColumn).toString(), map.get(valueColumn).toString());
        }
        return extractedValues;
    }

    public static void registerShutdownHook(final GraphDatabaseService graphDb) {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }

    public void shutdown() {
        graphDb.shutdown();
    }

    public Transaction beginTx() {
        return graphDb.beginTx();
    }

    public static void deleteDatabase(Site site) {
        try {
            Runtime.getRuntime().exec("rm -rf " + getDBPath(site));
        } catch (IOException ex) {
            Logger.getLogger(Neo4jHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String getDBPath() {
        return getDBPath(site);
    }

    private static String getDBPath(Site site) {
        return Paths.PARTIAL_DB_PATH + site.getPath() + "/" + DB_FILE_NAME;
    }

    public GraphDatabaseService getGraphDb() {
        return graphDb;
    }

    public static void main(String[] args) {
        Site site = br.edimarmanica.dataset.orion.driver.Site.CHAMP;
        Neo4jHandler neo = new Neo4jHandler(site);
        
        String query = "MATCH (o) WHERE id(o) IN {id} SET o:Root ";
        Map<String, Object> params = new HashMap<>();
        List<Long> nodeID = new ArrayList<>();
        nodeID.add(3l);
        nodeID.add(4l);
        params.put("id", nodeID);
        Result rs = neo.executeCypher(query, params);
        
//        neo.executeCypher("MATCH (n) WHERE id(n)=0 SET n:Teste ");
        System.out.println(neo.querySingleColumn("match (n:Root) return count(n) as t", "t").get(0));
    }

}
