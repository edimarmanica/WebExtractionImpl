/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.trinity.intrasitemapping.auto;

import br.edimarmanica.dataset.Site;
import br.edimarmanica.metrics.Formatter;
import br.edimarmanica.trinity.check.CheckMappingAuto;
import br.edimarmanica.trinity.extract.Extract;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
public class Load {

    /**
     *
     * @param offsetFile
     * @param onlyDigitsAndLetters se true filtra para ficar só letras, dígitos
     * e . --> usado para o mapping
     * @return List<Group<Page,ExtractedValue>>
     */
    public static List<Map<String, String>> loadOffset(File offsetFile, boolean onlyDigitsAndLetters) {
        List<Map<String, String>> offset = new ArrayList<>(); //cada arquivo é um offset

        try (Reader in = new FileReader(offsetFile)) {
            try (CSVParser parser = new CSVParser(in, CSVFormat.EXCEL)) {
                for (CSVRecord record : parser) {
                    for (int nrGroup = 0; nrGroup < record.size(); nrGroup++) {
                        String value;
                        try {
                            if (onlyDigitsAndLetters) {
                                value = Formatter.formatValue(Preprocessing.filter(record.get(nrGroup)));
                            } else {
                                value = Preprocessing.filter(record.get(nrGroup));
                            }

                            Preprocessing.check(value);
                        } catch (InvalidValue ex) {
                            if (offset.size() - 1 < nrGroup) {//só pode adicionar no primeiro, senão pode já ter valor e vc zerar
                                offset.add(nrGroup, new HashMap<String, String>());
                            }
                            continue;
                        }

                        if (offset.size() - 1 < nrGroup) {
                            Map<String, String> group = new HashMap<>();
                            group.put(Formatter.formatURL(record.get(0)), value);
                            offset.add(nrGroup, group);
                        } else {
                            offset.get(nrGroup).put(Formatter.formatURL(record.get(0)), value);
                        }
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(br.edimarmanica.trinity.intrasitemapping.manual.Mapping.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(br.edimarmanica.trinity.intrasitemapping.manual.Mapping.class.getName()).log(Level.SEVERE, null, ex);
        }

        return offset;
    }

    /**
     * 
     * @param path
     * @param site
     * @return <NameOffsetY,<GroupOffsetY, GroupOfsetX>>
     */
    public static Map<String, Map<Integer, Integer>> loadMapping(String path, Site site) {
        Map<String, Map<Integer, Integer>> mappingsAuto = new HashMap<>(); //<GroupOffsetX,<NameOffsetY, GroupOfsetY>>
        try (Reader in = new FileReader(path + "/" + site.getPath() + "/mappings.csv")) {
            try (CSVParser parser = new CSVParser(in, CSVFormat.EXCEL.withHeader())) {
                for (CSVRecord record : parser) {

                    if (mappingsAuto.containsKey(record.get("NAME_OFFSET_Y"))) {
                        mappingsAuto.get(record.get("NAME_OFFSET_Y")).put(Integer.parseInt(record.get("GROUP_OFFSET_Y")), Integer.parseInt(record.get("GROUP_OFFSET_X")));
                    } else {
                        Map<Integer, Integer> map = new HashMap<>();
                        map.put(Integer.parseInt(record.get("GROUP_OFFSET_Y")), Integer.parseInt(record.get("GROUP_OFFSET_X")));
                        mappingsAuto.put(record.get("NAME_OFFSET_Y"), map);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CheckMappingAuto.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CheckMappingAuto.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mappingsAuto;
    }

    public static void main(String[] args) {
        List<Map<String, String>> offset = loadOffset(new File("/media/edimar/Dados/doutorado04/trinity/ved_w1_auto/WEIR/book/www.bookdepository.co.uk/offset/result_0.csv"), true);
        int indGroup = 0;
        System.out.println("Size: " + offset.size());
        for (Map<String, String> group : offset) {
            for (String page : group.keySet()) {
                System.out.println(indGroup + ":" + page + "=>" + group.get(page));
            }
            indGroup++;
        }
    }
}
