/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.trinity.intrasitemapping.auto;

import br.edimarmanica.configuration.General;
import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Dataset;
import br.edimarmanica.dataset.Domain;
import br.edimarmanica.dataset.Site;
import br.edimarmanica.metrics.Printer;
import static br.edimarmanica.trinity.intrasitemapping.manual.OffsetToRule.HEADER;
import br.edimarmanica.trinity.util.FileUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 *
 * @author edimar
 */
public class Offset2Rule {

    public static final String[] HEADER = {"URL", "EXTRACTED VALUE"};

    private final Site site;
    private final String path;

    private Map<String, Map<Integer, Integer>> allMappings;//<NameOffsetY,<GroupOffsetY, GroupOfsetX>>
    private final Set<String> pages = new HashSet<>(); //Set<ruleID@page> páginas já inseridas. Para evitar de inserir as páginas compartilhadas várias vezes

    public Offset2Rule(Site site, String path) {
        this.site = site;
        this.path = path;
    }

    private void execute() {
        File dirRules = new File(path + "/" + site.getPath() + "/extracted_values/");
        FileUtils.deleteDir(dirRules);
        dirRules.mkdirs();

        allMappings = Load.loadMapping(path, site);

        File dirOffsets = new File(path + "/" + site.getPath() + "/offset");
        for (File offsetFile : dirOffsets.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".csv");
            }
        })) {
            execute(offsetFile);
        }

    }

    private void execute(File offsetFile) {
        Map<Integer, Integer> mapOffset = allMappings.get(offsetFile.getName());

        List<Map<String, String>> offsetValues = Load.loadOffset(offsetFile, false);//List<Group<Page,ExtractedValue>>
        int i = 0;
        for (Map<String, String> group : offsetValues) {

            Integer ruleID = mapOffset.get(i);
            if (ruleID == null || ruleID == -1) {
                i++;
                continue;
            }
            print(ruleID, group);
            i++;
        }
    }

    private void print(int ruleID, Map<String, String> values) {
        File output = new File(path + "/" + site.getPath() + "/extracted_values/" + "/rule_" + ruleID + ".csv");

        CSVFormat format;
        if (output.exists()) {
            format = CSVFormat.EXCEL;
        } else {
            format = CSVFormat.EXCEL.withHeader(HEADER);
        }

        try (Writer out = new FileWriter(output, output.exists())) {
            try (CSVPrinter csvFilePrinter = new CSVPrinter(out, format)) {
                for (String page : values.keySet()) {

                    if (pages.contains(ruleID + General.SEPARADOR + page)) {//É uma página compartilhada. Já foi inserida por outro offset
                        continue;
                    }
                    pages.add(ruleID + General.SEPARADOR + page);

                    List<String> dataRecord = new ArrayList<>();
                    dataRecord.add(page);
                    dataRecord.add(values.get(page));
                    csvFilePrinter.printRecord(dataRecord);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Offset2Rule.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        for (Dataset dataset : Dataset.values()) {
            for (Domain domain : dataset.getDomains()) {
                for (Site site : domain.getSites()) {
                    if (site != br.edimarmanica.dataset.weir.book.Site.AMAZON) {
                        continue;
                    }

                    System.out.println("Site: " + site);
                    String path = Paths.PATH_TRINITY + "/ved_w1_auto";
                    Offset2Rule o2r = new Offset2Rule(site, path);
                    o2r.execute();
                }
            }
        }
    }
}
