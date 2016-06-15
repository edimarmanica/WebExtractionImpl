/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir2.check;

import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Attribute;
import br.edimarmanica.dataset.Site;
import br.edimarmanica.metrics.GroundTruth;
import br.edimarmanica.metrics.SiteWithoutThisAttribute;
import br.edimarmanica.weir2.distance.TypeAwareDistance;
import br.edimarmanica.weir2.rule.Loader;
import br.edimarmanica.weir2.rule.type.DataType;
import br.edimarmanica.weir2.rule.type.RuleDataType;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author edimar
 */
public class CheckDistance {

    private Attribute attribute;
    private Site site1;
    private int R1;
    private Site site2;
    private int R2;

    private File rule1;
    private DataType typeRule1;
    private Map<String, String> entityValuesR1;
    private GroundTruth gabaritoR1;
    private Map<String, String> entityIDsR1;

    private File rule2;
    private DataType typeRule2;
    private Map<String, String> entityValuesR2;
    private GroundTruth gabaritoR2;
    private Map<String, String> entityIDsR2;

    private Set<String> sharedEntityIds;
    private double distance;

    public CheckDistance(Attribute attribute, Site site1, int R1, Site site2, int R2) {
        this.attribute = attribute;
        this.site1 = site1;
        this.R1 = R1;
        this.site2 = site2;
        this.R2 = R2;
    }

    private void processing() {
        rule1 = new File(Paths.PATH_INTRASITE + "/" + site1.getPath() + "/extracted_values/rule_" + R1 + ".csv");
        typeRule1 = RuleDataType.getMostFrequentType(rule1);
        entityIDsR1 = Loader.loadEntityID(site1);
        entityValuesR1 = Loader.loadEntityValues(rule1, entityIDsR1);
        gabaritoR1 = GroundTruth.getInstance(site1, attribute);
        try {
            gabaritoR1.load();
        } catch (SiteWithoutThisAttribute ex) {
            Logger.getLogger(CheckDistance.class.getName()).log(Level.SEVERE, null, ex);
        }

        rule2 = new File(Paths.PATH_INTRASITE + "/" + site2.getPath() + "/extracted_values/rule_" + R2 + ".csv");
        typeRule2 = RuleDataType.getMostFrequentType(rule2);
        entityIDsR2 = Loader.loadEntityID(site2);
        entityValuesR2 = Loader.loadEntityValues(rule2, entityIDsR2);
        gabaritoR2 = GroundTruth.getInstance(site2, attribute);
        try {
            gabaritoR2.load();
        } catch (SiteWithoutThisAttribute ex) {
            Logger.getLogger(CheckDistance.class.getName()).log(Level.SEVERE, null, ex);
        }

        sharedEntityIds = new HashSet<>();
        sharedEntityIds.addAll(entityValuesR1.keySet());
        sharedEntityIds.retainAll(entityValuesR2.keySet());

        distance = TypeAwareDistance.typeDistance(entityValuesR1, typeRule1, entityValuesR2, typeRule2);
    }

    private String getPage(String entity, Map<String, String> entityIDs) {
        for (String page : entityIDs.keySet()) {
            if (entity.equals(entityIDs.get(page))) {
                return page;
            }
        }
        return null;
    }

    private String getGabarito(String entity, boolean R1) {
        if (R1) {
            return gabaritoR1.getGroundTruth().get(getPage(entity, entityIDsR1));
        } else {
            return gabaritoR2.getGroundTruth().get(getPage(entity, entityIDsR2));
        }
    }

    public void printInfo() {
        processing();

        System.out.println("Nr Entities R1: " + entityValuesR1.size());
        System.out.println("Nr Entities R2: " + entityValuesR2.size());
        System.out.println("Shared entities: " + sharedEntityIds.size());
        System.out.println("Type R1: " + typeRule1);
        System.out.println("Type R2: " + typeRule2);
        System.out.println("Distance: " + distance);

        printValues();
    }

    private void printValues() {
        System.out.println("Entity =>  R1 (gabarito R1) =>  R2 (gabarito R2) => Page R1 X Page R2");
        for (String entity : entityValuesR1.keySet()) {
            System.out.println(entity + " => " + entityValuesR1.get(entity) + " (" + getGabarito(entity, true) + ") => " + entityValuesR2.get(entity) + " (" + getGabarito(entity, false) + ")" + " => " + getPage(entity, entityIDsR1) + " X " + getPage(entity, entityIDsR2));
        }
    }

    public static void main(String[] args) throws SiteWithoutThisAttribute {
        Attribute attr = br.edimarmanica.dataset.weir.book.Attribute.PUBLISHER;

        Site site1 = br.edimarmanica.dataset.weir.book.Site.BOOKDEPOSITORY;
        int R1 = 212;
        Site site2 = br.edimarmanica.dataset.weir.book.Site.ECAMPUS;
        int R2 = 529;

        CheckDistance check = new CheckDistance(attr, site1, R1, site2, R2);
        check.printInfo();
    }
}
