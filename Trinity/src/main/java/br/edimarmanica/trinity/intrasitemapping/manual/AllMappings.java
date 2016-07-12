/*
 * To change this license HEADER, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.trinity.intrasitemapping.manual;

import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Attribute;
import br.edimarmanica.dataset.Dataset;
import br.edimarmanica.dataset.Domain;
import br.edimarmanica.dataset.Site;
import br.edimarmanica.metrics.GroundTruth;
import br.edimarmanica.metrics.Printer;
import br.edimarmanica.metrics.SiteWithoutThisAttribute;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 *
 * @author edimar
 */
public class AllMappings {

    private final Site site;
    public static final String[] HEADER = {"ATTRIBUTE", "OFFSET", "GROUP"};
    private boolean append = false;

    public AllMappings(Site site) {
        this.site = site;
    }

    /**
     *
     * @return Map<Attribute, Map<PageURL, ExpectedValue>>
     */
    private Map<Attribute, Map<String, String>> getGroundTruth() {
        Map<Attribute, Map<String, String>> groundTruth = new HashMap<>();
        for (Attribute attr : site.getDomain().getAttributes()) {
            try {
                groundTruth.put(attr, getGroundTruth(attr));
            } catch (SiteWithoutThisAttribute ex) {
                Logger.getLogger(AllMappings.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return groundTruth;
    }

    /**
     *
     * @param attribute
     * @return Map<PageURL, ExpectedValue>
     * @throws SiteWithoutThisAttribute
     */
    private Map<String, String> getGroundTruth(Attribute attribute) throws SiteWithoutThisAttribute {
        GroundTruth gt = GroundTruth.getInstance(site, attribute);
        gt.load();

        return gt.getGroundTruth();
    }

    public void execute() {
        Map<Attribute, Map<String, String>> groundTruth = getGroundTruth();

        File dir = new File(Paths.PATH_TRINITY + site.getPath() + "/offset");
        int i = 0;
        for (File offset : dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".csv");
            }
        })) {
            System.out.println("Page : " + i);
            i++;
            Mapping map = new Mapping(site, offset, groundTruth);
            Map<Attribute, Integer> maps = map.getBestGroups();
            for (Attribute attr : maps.keySet()) {
                List<String> dataRecord = new ArrayList<>();
                dataRecord.add(attr.getAttributeID());
                dataRecord.add(offset.getName());
                dataRecord.add(maps.get(attr).toString());
                print(dataRecord);
            }
        }
    }

    private void print(List<String> dataRecord) {
        /**
         * ********************** results ******************
         */
        File dir = new File(Paths.PATH_TRINITY + "/" + site.getPath());
        dir.mkdirs();

        File file = new File(dir.getAbsolutePath() + "/mappings.csv");
        CSVFormat format;
        if (append) {
            format = CSVFormat.EXCEL;
        } else {
            format = CSVFormat.EXCEL.withHeader(HEADER);
        }

        try (Writer out = new FileWriter(file, append)) {
            try (CSVPrinter csvFilePrinter = new CSVPrinter(out, format)) {
                csvFilePrinter.printRecord(dataRecord);
            }
        } catch (IOException ex) {
            Logger.getLogger(Printer.class.getName()).log(Level.SEVERE, null, ex);
        }

        append = true;
    }

    public static void main(String[] args) {

        Paths.PATH_TRINITY = Paths.PATH_TRINITY + "/ved_w1/";

        for (Dataset dataset : Dataset.values()) {
            System.out.println("Dataset: " + dataset);
            for (Domain domain : dataset.getDomains()) {
                System.out.println("\tDomain: " + domain);

                if (domain != br.edimarmanica.dataset.swde.Domain.AUTO) {
                    continue;
                }

                for (Site site : domain.getSites()) {

                    if (site != br.edimarmanica.dataset.swde.auto.Site.YAHOO) {
                        continue;
                    }
                    try {
                        System.out.println("\t\tSite: " + site);
                        AllMappings am = new AllMappings(site);
                        am.execute();
                    } catch (Exception ex) {
                        System.out.println("\t\t\tIgnorando");
                    }
                }
            }
        }
    }

}
