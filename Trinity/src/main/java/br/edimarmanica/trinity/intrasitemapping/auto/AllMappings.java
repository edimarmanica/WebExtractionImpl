/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.trinity.intrasitemapping.auto;

import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Site;
import br.edimarmanica.metrics.Printer;
import br.edimarmanica.trinity.extract.Extract;
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
public class AllMappings {

    public static final String[] HEADER = {"GROUP_OFFSET0", "NAME_OFFSETX", "GROUP_OFFSETX"};

    private final Site site;
    private final String path;

    private boolean append = false;
    private final Set<Integer> blackList = new HashSet<>(); //lista de índices do offset0 que não são mapeados pq extraem poucas informações
    private List<Map<String, String>> offset0;

    public AllMappings(Site site, String path) {
        this.site = site;
        this.path = path;
    }

    /**
     * mapeia todos os offsets com o offset0
     */
    private void mapping() {
        offset0 = Load.loadOffset(new File(path + "/" + site.getPath() + "/offset/result_0.csv"), true);

        File dir = new File(path + "/" + site.getPath() + "/offset");
        for (File offsetFile : dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".csv");
            }
        })) {
            if (offsetFile.getName().equals("result_0.csv")) {
                continue;
            }
            List<Map<String, String>> offsetX = Load.loadOffset(offsetFile, true);
            mapping(offsetFile.getName(), offsetX);
        }
    }

    /**
     * mapeia o offsetX com o offset0
     *
     * @param offsetX
     */
    private void mapping(String nameOffsetX, List<Map<String, String>> valuesOffsetX) {

        for (int i = 0; i < offset0.size(); i++) {

            if (blackList.contains(i)) {//esse grupo do offset0 não extrai valores
                continue;
            }

            Mapping map = new Mapping(offset0.get(i), valuesOffsetX);
            int mapping;
            try {
                mapping = map.mapping();
            } catch (MappingNotFoundException ex) {
                mapping = -1;
            }

            List<String> dataRecord = new ArrayList<>();
            dataRecord.add(i + "");
            dataRecord.add(nameOffsetX);
            dataRecord.add(mapping + "");
            print(dataRecord);
        }
    }

    private void print(List<String> dataRecord) {
        /**
         * ********************** results ******************
         */
        File dir = new File(path + "/" + site.getPath());
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

    /**
     *
     * @param regraOffset0
     * @throws MappingNotFoundException
     */
    private void checkGroupsOffset0() throws MappingNotFoundException {

        int i = 0;
        for (Map<String, String> group : offset0) {

            int nrEmpty = 0;
            for (String value : group.values()) {
                if (!value.isEmpty()) {
                    nrEmpty++;
                }
            }

            if (nrEmpty >= Extract.NR_SHARED_PAGES/2) {
                blackList.add(i);
            }
            i++;
        }
    }
    
    public static void main(String[] args) {
        Site site = br.edimarmanica.dataset.weir.book.Site.BOOKDEPOSITORY;
        String path = Paths.PATH_TRINITY+"/ved_w1_auto";
        AllMappings am = new AllMappings(site, path);
        am.mapping();
    }

}
