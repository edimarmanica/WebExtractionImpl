/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.intrasite.check.rules;

import br.edimarmanica.dataset.Site;
import br.edimarmanica.expressiveness.generate.beans.CypherRule;
import br.edimarmanica.htmltocsvtoneo4j.neo4j.Neo4jHandler;
import br.edimarmanica.intrasite.extract.Blocking;
import br.edimarmanica.util.Chronometer;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author edimar
 */
public class CheckTime {

    private final Site site;
    private final Neo4jHandler neo4j;

    public CheckTime(Site site) {
        this.site = site;
        neo4j = new Neo4jHandler(site);
    }

    private Map<String, String> scaleQuery(String query, String label) {
        String scaleQuery = query.replaceAll("MATCH ", "MATCH (block:Block) WHERE block.KEY=LEFT({valuex}, " + Blocking.BLOCK_SIZE + ") WITH block \n"
                + "MATCH (block)-->");
        Map<String, Object> scaleParams = new HashMap<>();
        scaleParams.put("valuex", label);

        return neo4j.extract(scaleQuery, scaleParams, "URL", "VALUE");
    }

    private void executeQuery(int queryID, String query, String label) {

        Chronometer chron = new Chronometer();
        chron.start();
        int nrRel = scaleQuery(query, label).size();
        System.out.println(site + ";" + queryID + ";" + nrRel + ";" + chron.elapsedTime());
        neo4j.shutdown();
    }
    
    private void execute(){
        int queryID = 0;
        String query = "MATCH (a3:Template)<--(a2)<--(a1)<--(a0)<--(b)-->(c0)-->(c1)-->(c2) "+
"WHERE a3.VALUE='Send to a Friend' AND a3.PATH='/HTML/BODY/DIV/DIV/DIV/DIV/DIV/DIV/A/text()' AND a3.POSITION='1' "+
"AND a2.VALUE='A' AND a2.POSITION='2' "+
"AND a1.VALUE='DIV' AND a1.POSITION='1' "+
"AND a0.VALUE='DIV' AND a0.POSITION='2' "+
"AND b.VALUE='DIV' "+
"AND c0.VALUE='DIV' AND c0.POSITION='1' "+
"AND c1.VALUE='H1' AND c1.POSITION='1' "+
"AND c2.NODE_TYPE='3' AND c2.POSITION='1' "+
" RETURN c2.VALUE AS VALUE, c2.URL AS URL, 'Template' in LABELS(c2) as template";
        String label = "Send to a Friend";
        executeQuery(queryID, query, label);
    }

    public static void main(String[] args) {
        
        Site site = br.edimarmanica.dataset.swde.university.Site.COLLEGEBOARD;
        CheckTime ck = new CheckTime(site);
        ck.execute();

    }
}
