/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.trinity.check;

import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Site;
import br.edimarmanica.trinity.intrasitemapping.manual.OffsetToRule;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author edimar
 */
public class CheckMappingAuto {

    private final Site site;
    private final String pathManual;
    private final String pathAuto;

    private final Map<String, Map<String, Integer>> mappingsManual = new HashMap<>(); //<Attribute,<Offset, Group>>
    private final Map<Integer, Map<String, Integer>> mappingsAuto = new HashMap<>(); //<GroupOffset0,<IndOffsetX, GroupOfsetX>>

    public CheckMappingAuto(Site site, String pathManual, String pathAuto) {
        this.site = site;
        this.pathManual = pathManual;
        this.pathAuto = pathAuto;
    }

    private void readMappingsManual() {
        try (Reader in = new FileReader(pathManual + "/" + site.getPath() + "/mappings.csv")) {
            try (CSVParser parser = new CSVParser(in, CSVFormat.EXCEL.withHeader())) {
                for (CSVRecord record : parser) {

                    if (mappingsManual.containsKey(record.get("ATTRIBUTE"))) {
                        mappingsManual.get(record.get("ATTRIBUTE")).put(record.get("OFFSET"), Integer.parseInt(record.get("GROUP")));
                    } else {
                        Map<String, Integer> map = new HashMap<>();
                        map.put(record.get("OFFSET"), Integer.parseInt(record.get("GROUP")));
                        mappingsManual.put(record.get("ATTRIBUTE"), map);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CheckMappingAuto.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CheckMappingAuto.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void readMappingsAuto() {
        try (Reader in = new FileReader(pathAuto + "/" + site.getPath() + "/mappings.csv")) {
            try (CSVParser parser = new CSVParser(in, CSVFormat.EXCEL.withHeader())) {
                for (CSVRecord record : parser) {

                    if (mappingsAuto.containsKey(Integer.parseInt(record.get("GROUP_OFFSET0")))) {
                        mappingsAuto.get(Integer.parseInt(record.get("GROUP_OFFSET0"))).put(record.get("NAME_OFFSETX"), Integer.parseInt(record.get("GROUP_OFFSETX")));
                    } else {
                        Map<String, Integer> map = new HashMap<>();
                        map.put(record.get("NAME_OFFSETX"), Integer.parseInt(record.get("GROUP_OFFSETX")));
                        mappingsAuto.put(Integer.parseInt(record.get("GROUP_OFFSET0")), map);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CheckMappingAuto.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CheckMappingAuto.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void check() {
        readMappingsManual();
        readMappingsAuto();

        //para cada atributo do offset manual
        for (String attr : mappingsManual.keySet()) {//Map<OFFSET_NAME, GROUP> 
            int indOffset0 = mappingsManual.get(attr).get("result_0.csv"); //índice do offset0 que extraí o atributo corrente

            for (String offsetX : mappingsManual.get(attr).keySet()) { // para cada offset que extraí esse atributo
                if (offsetX.equals("result_0.csv")) {
                    continue;
                }

                Integer autoMap = -1;
                try {
                    autoMap = mappingsAuto.get(indOffset0).get(offsetX);
                } catch (NullPointerException ex) {
                    //ficará com o -1
                }

                if (!mappingsManual.get(attr).get(offsetX).equals(autoMap)) { //verifica se o mapeamento automático foi igual ao manual
                    System.out.println("Diferença;" + attr + ";" + offsetX + ";" + mappingsManual.get(attr).get(offsetX) + ";" + autoMap);
                }

            }
        }
    }

    public static void main(String[] args) {
        Site site = br.edimarmanica.dataset.weir.book.Site.BOOKDEPOSITORY;
        String pathManual = Paths.PATH_TRINITY + "/ved_w1";
        String pathAuto = Paths.PATH_TRINITY + "/ved_w1_auto";
        CheckMappingAuto check = new CheckMappingAuto(site, pathManual, pathAuto);
        check.check();
    }
}
