/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.alignment;

import br.edimarmanica.alignment.beans.Pair;
import br.edimarmanica.alignment.beans.Rule;
import br.edimarmanica.configuration.General;
import br.edimarmanica.dataset.Domain;
import br.edimarmanica.dataset.Site;
import br.edimarmanica.weir2.distance.TypeAwareDistance;
import br.edimarmanica.weir2.rule.type.DataType;
import br.edimarmanica.weir2.rule.type.RulesDataTypeController;
import br.edimarmanica.wrapperinduction.trinity.LoadRules;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author edimar
 */
public class ScorePairs {

    private final Domain domain;
    private boolean append = false;
    private final String pathRules;
    private final String pathOutput;

    public ScorePairs(Domain domain, String pathRules, String pathOutput) {
        this.domain = domain;
        this.pathRules = pathRules;
        this.pathOutput = pathOutput;
    }

    public void compareAndPersist() {
        List<List<File>> rules = new ArrayList<>(); // Site<RuleName>
        List<Map<String, String>> entityIDsBySite = new ArrayList<>(); //Site<Page,Entity>>
        List<Map<String, DataType>> dataTypes = new ArrayList<>(); //Site<RuleName,DataType>

        for (Site site : domain.getSites()) {
            rules.add(Arrays.asList(new File(pathRules + "/" + site.getPath() + "/extracted_values/").listFiles()));
            entityIDsBySite.add(LoadRules.loadEntityID(site));
            dataTypes.add(RulesDataTypeController.load(site, pathOutput));
        }

        for (int indexSiteI = 0; indexSiteI < rules.size(); indexSiteI++) {
            for (int indexRuleI = 0; indexRuleI < rules.get(indexSiteI).size(); indexRuleI++) {

                File fileRi = rules.get(indexSiteI).get(indexRuleI);
                DataType typeRi = dataTypes.get(indexSiteI).get(fileRi.getName());
                Map<String, String> entityValuesRi = LoadRules.loadEntityValues(fileRi, entityIDsBySite.get(indexSiteI));

                for (int indexSiteJ = indexSiteI; indexSiteJ < rules.size(); indexSiteJ++) {
                    for (int indexRuleJ = 0; indexRuleJ < rules.get(indexSiteJ).size(); indexRuleJ++) {
                        if (indexSiteJ == indexSiteI && indexRuleJ <= indexRuleI) {
                            continue;
                        }

                        File fileRj = rules.get(indexSiteJ).get(indexRuleJ);
                        DataType typeRj = dataTypes.get(indexSiteJ).get(fileRj.getName());
                        Map<String, String> entityValuesRj = LoadRules.loadEntityValues(fileRj, entityIDsBySite.get(indexSiteJ));
                        double distance = TypeAwareDistance.typeDistance(entityValuesRi, typeRi, entityValuesRj, typeRj);

                        if (General.DEBUG) {
                            System.out.println("i[" + indexSiteI + "][" + indexRuleI + "] X j[" + indexSiteJ + "][" + indexRuleJ + "] = " + distance);
                        }

                        if (distance < 1.00) { //os com distância = 1 não interessam
                            persiste(domain.getSites()[indexSiteI], rules.get(indexSiteI).get(indexRuleI).getName(), domain.getSites()[indexSiteJ], rules.get(indexSiteJ).get(indexRuleJ).getName(), distance);
                        }

                    }
                }
            }
        }
    }

    private void persiste(Site site1, String rule1, Site site2, String rule2, double score) {
        List<String> dataRecord = new ArrayList<>();
        dataRecord.add(site1.toString());
        dataRecord.add(rule1);
        dataRecord.add(site2.toString());
        dataRecord.add(rule2);
        dataRecord.add(score + "");

        File dirOutput = new File(pathOutput + "/" + domain.getPath());
        dirOutput.mkdirs();

        File file = new File(dirOutput.getAbsolutePath() + "/scores.csv");

        CSVFormat format;
        if (append) {
            format = CSVFormat.EXCEL;
        } else {
            String[] HEADER = {"SITE1", "RULE1", "SITE2", "RULE2", "SCORE"};
            format = CSVFormat.EXCEL.withHeader(HEADER);
        }

        try (Writer out = new FileWriter(file, append)) {
            try (CSVPrinter csvFilePrinter = new CSVPrinter(out, format)) {
                csvFilePrinter.printRecord(dataRecord);
            }
        } catch (IOException ex) {
            Logger.getLogger(ScorePairs.class.getName()).log(Level.SEVERE, null, ex);
        }
        append = true;
    }

    public static List<Pair> loadAndSort(Domain domain, String path) {
        List<Pair> pairs = new ArrayList<>();

        try (Reader in = new FileReader(new File(path + "/" + domain.getPath() + "/scores.csv"))) {
            try (CSVParser parser = new CSVParser(in, CSVFormat.EXCEL.withHeader())) {
                for (CSVRecord record : parser) { //para cada value
                    double score = Double.valueOf(record.get("SCORE"));
                    if (score == 1) {
                        continue;
                    }
                    Pair pair = new Pair(new Rule(domain.getSiteOf(record.get("SITE1")), record.get("RULE1")), new Rule(domain.getSiteOf(record.get("SITE2")), record.get("RULE2")), score);
                    pairs.add(pair);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ScorePairs.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ScorePairs.class.getName()).log(Level.SEVERE, null, ex);
        }

        Collections.sort(pairs);

        return pairs;

    }
}
