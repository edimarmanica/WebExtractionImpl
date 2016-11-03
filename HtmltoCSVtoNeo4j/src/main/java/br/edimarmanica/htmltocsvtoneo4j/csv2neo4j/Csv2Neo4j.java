/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.htmltocsvtoneo4j.csv2neo4j;

import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Domain;
import br.edimarmanica.dataset.Site;

/**
 *
 * @author edimar
 */
public class Csv2Neo4j {

    public static void main(String[] args) {
        Domain domain = br.edimarmanica.dataset.orion.Domain.DRIVER;
        for (Site site : domain.getSites()) {
            System.out.println("./neo4j-import --into " + Paths.PARTIAL_DB_PATH + "/" + site.getPath() + "/graph.db --id-type string --nodes "+ Paths.PARTIAL_CSV_PATH + "/" + site.getPath() +"/nodes.csv "
                    +  "--relationships:has_child "+Paths.PARTIAL_CSV_PATH + "/" + site.getPath()+"/rels.csv ");
            
        }
        
        
    }
}
//457277