/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.templatevariation.checkscore;

import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Dataset;
import br.edimarmanica.dataset.Domain;
import br.edimarmanica.dataset.Site;
import br.edimarmanica.metrics.MergeDomainResults;
import br.edimarmanica.metrics.Printer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
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
public class MergeCheckScore {

    private boolean append = false;
    protected String outputPath;

    public MergeCheckScore(String outputPath) {
        this.outputPath = outputPath;
    }

    private void addMetricsSite(Site site) {

        File file = new File(outputPath + "/" + site.getPath() + "/result.csv");
        if (!file.exists()){
            return;
        }
        
        try (Reader in = new FileReader(file)) {
            try (CSVParser parser = new CSVParser(in, CSVFormat.EXCEL.withHeader())) {

                for (CSVRecord record : parser) {
                    List<String> recordList = new ArrayList<>();
                    for (String head : CheckScore.header) {
                        recordList.add(record.get(head));
                    }
                    add(site, recordList);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MergeDomainResults.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MergeDomainResults.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void add(Site site, List<String> dataRecord) {
        File file = new File(outputPath + "/" + site.getDomain().getDataset().getFolderName() + "/result.csv");
        CSVFormat format;
        if (append) {
            format = CSVFormat.EXCEL;
        } else {

            format = CSVFormat.EXCEL.withHeader(CheckScore.header);
        }

        try (Writer out = new FileWriter(file, append)) {
            try (CSVPrinter csvFilePrinter = new CSVPrinter(out, format)) {
                csvFilePrinter.printRecord(dataRecord);
            }
        } catch (IOException ex) {
            Logger.getLogger(MergeDomainResults.class.getName()).log(Level.SEVERE, null, ex);
        }
        append = true;
    }

    private void merge(Domain domain) {
        for (Site site : domain.getSites()) {
            addMetricsSite(site);
        }
    }
    
    public void merge(Dataset dataset) {
        for (Domain domain : dataset.getDomains()) {
            merge(domain);
        }
    }
    
    public static void main(String[] args) {
        String output = Paths.PATH_SCORE_ANALYSIS;
        MergeCheckScore merge = new MergeCheckScore(output);
        merge.merge(Dataset.SWDE);
    }
}
