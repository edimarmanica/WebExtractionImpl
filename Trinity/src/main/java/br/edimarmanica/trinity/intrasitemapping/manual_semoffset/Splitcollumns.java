/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.trinity.intrasitemapping.manual_semoffset;

import br.edimarmanica.configuration.General;
import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Dataset;
import br.edimarmanica.dataset.Domain;
import br.edimarmanica.dataset.Site;
import br.edimarmanica.metrics.Formatter;
import br.edimarmanica.metrics.Printer;
import br.edimarmanica.trinity.intrasitemapping.auto.InvalidValue;
import br.edimarmanica.trinity.intrasitemapping.auto.Preprocessing;
import br.edimarmanica.trinity.util.FileUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
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
public class Splitcollumns {

    private final Site site;
    public static final String[] HEADER = {"URL", "EXTRACTED VALUE"};

    public Splitcollumns(Site site) {
        this.site = site;
    }

    private List<Map<String, String>> read() {
        List<Map<String, String>> rules = new ArrayList<>(); //List<Regra, Map<PageURL, ExpectedValue>>

        try (Reader in = new FileReader(Paths.PATH_TRINITY + site.getPath() + "/groups/rules.csv")) {
            try (CSVParser parser = new CSVParser(in, CSVFormat.EXCEL)) {
                int nrRegistro = 0;
                for (CSVRecord record : parser) {

                    for (int nrRegra = 0; nrRegra < record.size(); nrRegra++) {
                        String value;
                        try {
                            value = Formatter.formatValue(Preprocessing.filter(record.get(nrRegra)));
                        } catch (InvalidValue ex) {
                            value = "";
                        }
                        
                        if (value.equals("notmatched")){
                            value = "";
                        }

                        if (nrRegistro == 0) {
                            Map<String, String> regra = new HashMap<>();
                            regra.put(Formatter.formatURL(record.get(0))+".html", value);
                            rules.add(regra);
                        } else {
                            rules.get(nrRegra).put(Formatter.formatURL(record.get(0))+".html", value);
                        }
                    }
                    nrRegistro++;
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Splitcollumns.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Splitcollumns.class.getName()).log(Level.SEVERE, null, ex);
        }

        return rules;
    }

    private void split() {
        print(read());
    }

    private void print(List<Map<String, String>> rules) {
        File dir = new File(Paths.PATH_TRINITY + "/" + site.getPath() + "/extracted_values/");

        FileUtils.deleteDir(dir);
        dir.mkdirs();

        for (int ruleID = 0; ruleID < rules.size(); ruleID++) {

            File file = new File(dir.getAbsolutePath() + "/rule_" + ruleID + ".csv");
            CSVFormat format = CSVFormat.EXCEL.withHeader(HEADER);

            try (Writer out = new FileWriter(file)) {
                try (CSVPrinter csvFilePrinter = new CSVPrinter(out, format)) {
                    for (String url : rules.get(ruleID).keySet()) {
                        List<String> dataRecord = new ArrayList<>();
                        dataRecord.add(url);
                        dataRecord.add(rules.get(ruleID).get(url));
                        csvFilePrinter.printRecord(dataRecord);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Printer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void main(String[] args) {
        General.DEBUG = true;
        int sizeTraining = 5;
        Paths.PATH_TRINITY = Paths.PATH_TRINITY + "/vre_w" + sizeTraining + "/";

        for (Dataset dataset : Dataset.values()) {
            System.out.println("Dataset: " + dataset);
            for (Domain domain : dataset.getDomains()) {

                if (domain != br.edimarmanica.dataset.weir.Domain.BOOK) {
                    continue;
                }
                System.out.println("\tDomain: " + domain);
                for (Site site : domain.getSites()) {

                    /*if (site != br.edimarmanica.dataset.weir.book.Site.AMAZON) {
                        continue;
                    }*/
                    try {
                        System.out.println("\t\tSite: " + site);
                        Splitcollumns am = new Splitcollumns(site);
                        am.split();
                    } catch (Exception ex) {
                        System.out.println("\t\t\tIgnorando");
                    }

                }
            }
        }
    }

}
