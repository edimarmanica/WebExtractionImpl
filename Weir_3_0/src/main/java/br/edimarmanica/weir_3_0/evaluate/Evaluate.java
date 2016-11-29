/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.evaluate;

import br.edimarmanica.configuration.InterSite;
import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Attribute;
import br.edimarmanica.dataset.Domain;
import br.edimarmanica.dataset.Site;
import br.edimarmanica.metrics.GroundTruth;
import br.edimarmanica.metrics.Labels;
import br.edimarmanica.metrics.Printer;
import br.edimarmanica.metrics.Results;
import br.edimarmanica.metrics.RuleMetrics;
import br.edimarmanica.metrics.SiteWithoutThisAttribute;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author edimar
 */
public class Evaluate {

    private final Domain domain;
    private final String path;
    private final Map<Attribute, Map<Site, Integer>> map = new HashMap<>();//<Attribute,Map<Site,Rule>>
    private Labels labels;
    private Printer printer;
    private static final String[] HEADER = {"SITE", "ATTRIBUTE", "RULE"};

    public Evaluate(Domain domain, String path) {
        this.domain = domain;
        this.path = path;
    }

    private void loadMap() {
        Map<String, String> values = new HashMap<>();
        try (Reader in = new FileReader(path + "/" + domain.getPath() + "/" + "/mappings_manual.csv")) {
            try (CSVParser parser = new CSVParser(in, CSVFormat.EXCEL.withHeader())) {
                for (CSVRecord record : parser) {
                    Attribute attr = domain.getAttributeIDbyDataset(record.get("ATTRIBUTE"));
                    Site site = domain.getSiteOf(record.get("SITE"));
                    if (map.containsKey(attr)) {
                        map.get(attr).put(site, Integer.parseInt(record.get("RULE")));
                    } else {
                        map.put(attr, new HashMap<>());
                        map.get(attr).put(site, Integer.parseInt(record.get("RULE")));
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Results.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Results.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Map<String, String> loadRule(Site site, int rule) {
        Results results = new Results(site);
        return results.loadRule(new File(Paths.PATH_INTRASITE + "/" + site.getPath() + "/extracted_values/rule_" + rule + ".csv"));
    }

    private void printMetrics(Site site, Attribute attribute) throws SiteWithoutThisAttribute {

        GroundTruth groundTruth = GroundTruth.getInstance(site, attribute);
        groundTruth.load();

        if (groundTruth.getGroundTruth().isEmpty()) {
            return;//não tem esse atributo no gabarito
        }

        Integer rule = map.get(attribute).get(site);
        if (rule == null) { //o mapeamento não encontrou regra para esse atributo nesse site
            printer.print(attribute, "Attribute not found", "", groundTruth.getGroundTruth(), new HashMap<String, String>(), new HashSet<String>(), 0, 0, 0);
        } else {
            Map<String, String> ruleValues = loadRule(site, rule);
            RuleMetrics metrics = RuleMetrics.getInstance(site, ruleValues, groundTruth.getGroundTruth());
            metrics.computeMetrics();
            printer.print(attribute, "rule_" + rule + ".csv", labels.getLabels().get("rule_" + rule + ".csv"), groundTruth.getGroundTruth(), ruleValues, metrics.getIntersection(), metrics.getRecall(), metrics.getPrecision(), metrics.getRelevantRetrieved());
        }

    }

    public void printMetrics(Site site) {
        labels = new Labels(site);
        labels.load();

        printer = new Printer(site, path);

        for (Attribute attr : site.getDomain().getAttributes()) {
            try {
                printMetrics(site, attr);
            } catch (SiteWithoutThisAttribute ex) {
                // Logger.getLogger(Evaluate.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void printMetrics() {
        loadMap();
        for (Site site : domain.getSites()) {
            printMetrics(site);
        }
    }

    public static void main(String[] args) {

        Domain domain = br.edimarmanica.dataset.orion.Domain.DRIVER;

        System.out.println("\tDomain: " + domain);
        String path = Paths.PATH_WEIR+"/shared_"+InterSite.MIN_SHARED_ENTITIES;
        Evaluate eval = new Evaluate(domain, path);
        eval.printMetrics();

    }
}
