/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.trinity.intrasitemapping.auto;

import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Dataset;
import br.edimarmanica.dataset.Domain;
import br.edimarmanica.dataset.Site;
import br.edimarmanica.metrics.Printer;
import br.edimarmanica.trinity.extract.Extract;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public class AllMappings {

    public static final String[] HEADER = {"NAME_OFFSET_X", "GROUP_OFFSET_X", "NAME_OFFSET_Y", "GROUP_OFFSET_Y"};

    private final Site site;
    private final String path;

    private boolean append = false;
    private final Set<Integer> blackList = new HashSet<>(); //lista de grupos do offsetX que não são mapeados pq extraem poucas informações
    private List<Map<String, String>> groupsOffsetX;
    private String nameOffsetX;

    public AllMappings(Site site, String path) {
        this.site = site;
        this.path = path;
    }

    /**
     * mapeia todos os grupos do offsetY com os grupos do offsetX. O offsetX é o
     * offset com o maior número de grupos, pois em alguns casos tem um atributo
     * tem o mesmo valor em todas as páginas-entidade consideradas no offset
     * (ex. Collins para publisher) ai não extraí naquele offset pois pensou que
     * era template
     */
    private void mapping() {
        nameOffsetX = findBestOffset();
        groupsOffsetX = Load.loadOffset(new File(path + "/" + site.getPath() + "/offset/" + nameOffsetX), true);
        checkGroupsOffsetX();

        File dir = new File(path + "/" + site.getPath() + "/offset");
        for (File offsetFile : dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".csv");
            }
        })) {
            if (offsetFile.getName().equals(nameOffsetX)) {//offsetY = offsetX
                for (int i = 0; i < groupsOffsetX.size(); i++) {
                    if (blackList.contains(i)) {//esse grupo do offsetX não extrai valores
                        continue;
                    }

                    List<String> dataRecord = new ArrayList<>();
                    dataRecord.add(nameOffsetX);
                    dataRecord.add(i + "");
                    dataRecord.add(nameOffsetX);
                    dataRecord.add(i + "");
                    print(dataRecord);

                }
            } else {
                List<Map<String, String>> offsetY = Load.loadOffset(offsetFile, true);
                mapping(offsetFile.getName(), offsetY);
            }
        }
    }

    /**
     * mapeia o offsetY com o offsetX
     *
     * @param offsetY
     */
    private void mapping(String nameOffsetY, List<Map<String, String>> valuesOffsetY) {

        for (int i = 0; i < groupsOffsetX.size(); i++) {

            if (blackList.contains(i)) {//esse grupo do offsetX não extrai valores
                continue;
            }

            Mapping map = new Mapping(groupsOffsetX.get(i), valuesOffsetY);
            int mapping;
            try {
                mapping = map.mapping();
            } catch (MappingNotFoundException ex) {
                //mapping = -1;
                continue;//não encontrou mapping
            }

            List<String> dataRecord = new ArrayList<>();
            dataRecord.add(nameOffsetX);
            dataRecord.add(i + "");
            dataRecord.add(nameOffsetY);
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
     * Cada grupo do OffsetX para ser considerado tem que extrair pelo menos
     * NR_SHARED_PAGES/2 valores não vazios
     *
     * @param regraOffsetX
     */
    private void checkGroupsOffsetX() {

        int i = 0;
        for (Map<String, String> group : groupsOffsetX) {

            int nrNotEmpty = 0;
            for (String value : group.values()) {

                if (!value.isEmpty()) {
                    nrNotEmpty++;
                }
            }

            if (nrNotEmpty < Extract.NR_SHARED_PAGES / 2) {
                blackList.add(i);
            }
            i++;
        }
    }

    /**
     * Encontra o offset com o maior número de grupos que extrairam valores em
     * todas as páginas
     *
     * @return
     */
    private String findBestOffset() {
        String maxOffset = null;
        int maxGroups = 0;
        File dir = new File(path + "/" + site.getPath() + "/offset");
        for (File offsetFile : dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".csv");
            }
        })) {

            int nrGroups = nrGroups(offsetFile);
            if (nrGroups > maxGroups) {
                maxGroups = nrGroups;
                maxOffset = offsetFile.getName();
            }
        }
        return maxOffset;
    }

    /**
     * encontra o nr de grupos do offset que extrairam valores em todas as
     * páginas
     *
     * @param offset
     * @return
     */
    private int nrGroups(File offset) {
        int nrGroups = -1;

        List<Map<String, String>> offsetY = Load.loadOffset(offset, true);
        int maxNrPages = 0;
        //número máximo de páginas
        for (Map<String, String> group : offsetY) {
            if (maxNrPages < group.size()) {
                maxNrPages = group.size();
            }
        }

        //nr de grupos com o número máximo de páginas
        int count = 0;
        for (Map<String, String> group : offsetY) {
            if (group.size() == maxNrPages) {
                count++;
            }
        }

        return count;
    }

    public static void main(String[] args) {

        for (Dataset dataset : Dataset.values()) {
            System.out.println("Dataset: "+dataset);
            for (Domain domain : dataset.getDomains()) {
               /* if (domain != br.edimarmanica.dataset.weir.Domain.BOOK){
                    continue;
                }*/
                
                System.out.println("\tDomain: "+domain);
                
                for (Site site : domain.getSites()) {
                    /*if (site != br.edimarmanica.dataset.weir.book.Site.AMAZON) {
                        continue;
                    }*/

                    System.out.println("\t\tSite: " + site);
                    String path = Paths.PATH_TRINITY + "/ved_w1_auto";
                    AllMappings am = new AllMappings(site, path);
                    am.mapping();
                }
            }
        }
    }

}
