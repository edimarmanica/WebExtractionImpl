/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package check;

import br.edimarmanica.alignment.beans.Rule;
import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Attribute;
import br.edimarmanica.dataset.Domain;
import br.edimarmanica.dataset.Site;
import br.edimarmanica.wrapperinduction.trinity.LoadRules;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author edimar
 */
public class CheckAlignment02 {

    private final Domain domain;
    private final String pathRules;
    private final String pathMapping;
    private Map<Attribute, Set<Rule>> expectedRules;
    private Map<Rule, CSVRecord> mappedRules;

    public CheckAlignment02(Domain domain, String pathRules, String pathMapping) {
        this.domain = domain;
        this.pathRules = pathRules;
        this.pathMapping = pathMapping;
    }

    private void readExpectedRules() {
        expectedRules = new HashMap<>();

        try (Reader in = new FileReader(pathRules + "/" + domain.getPath() + "/" + "/result.csv")) {
            try (CSVParser parser = new CSVParser(in, CSVFormat.EXCEL.withHeader())) {
                for (CSVRecord record : parser) { //para cada value

                    Attribute attr = domain.getAttributeIDbyDataset(record.get("ATTRIBUTE"));

                    if (expectedRules.containsKey(attr)) {
                        expectedRules.get(attr).add(new Rule(getSite(record.get("SITE")), record.get("RULE")));
                    } else {
                        Set<Rule> rules = new HashSet<>();
                        rules.add(new Rule(getSite(record.get("SITE")), record.get("RULE")));
                        expectedRules.put(attr, rules);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LoadRules.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LoadRules.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Site getSite(String folderName) {
        for (Site site : domain.getSites()) {
            if (folderName.equals(site.getFolderName())) {
                return site;
            }
        }
        return null;
    }

    private void readMappingRules() {
        mappedRules = new HashMap<>();

        try (Reader in = new FileReader(pathMapping + "/" + domain.getPath() + "/" + "/mappings.csv")) {
            try (CSVParser parser = new CSVParser(in, CSVFormat.EXCEL.withHeader())) {
                for (CSVRecord record : parser) { //para cada value
                    mappedRules.put(new Rule(domain.getSiteOf(record.get("SITE")), record.get("RULE")), record);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LoadRules.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LoadRules.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void execute() {
        readExpectedRules();
        readMappingRules();

        for (Attribute attr : expectedRules.keySet()) {
            for (Rule rule : expectedRules.get(attr)) {
                System.out.print(attr.toString());
                System.out.print(";");
                System.out.print(rule.getSite());
                System.out.print(";");
                System.out.print(rule.getRuleID());
                System.out.print(";");
                if (mappedRules.containsKey(rule)) {
                    CSVRecord record = mappedRules.get(rule);
                    System.out.print(record.get("MAP_ID"));
                    System.out.print(";");
                    System.out.print(record.get("THRESHOLD"));
                    System.out.print(";");
                } else {
                    System.out.print(";");
                    System.out.print(";");
                }

                if (rule.getRuleID().equals("Attribute not found")) {
                    System.out.print("Attribute not found");
                    System.out.print(";");
                    System.out.print(";");
                    System.out.print(";");
                    System.out.print(";");
                    System.out.print(";");
                } else {

                    int i = 0;
                    Map<String, String> pageValues = LoadRules.loadPageValues(new File(pathRules + "/" + rule.getSite().getPath() + "/extracted_values/" + rule.getRuleID()), false);
                    for (String page : pageValues.keySet()) {
                        System.out.print(pageValues.get(page));
                        System.out.print(";");

                        if (i >= 5) {
                            break;
                        }
                        i++;
                    }
                }

                System.out.println();

            }
        }
    }

    public static void main(String[] args) {
        Domain domain = br.edimarmanica.dataset.swde.Domain.NBA_PLAYER;
        String pathMap = Paths.PATH_TRINITY_PLUS_WEIR;
        String pathRules = Paths.PATH_TRINITY + "/ved_w1_auto/";
        CheckAlignment02 check = new CheckAlignment02(domain, pathRules, pathMap);
        check.execute();
    }
}
