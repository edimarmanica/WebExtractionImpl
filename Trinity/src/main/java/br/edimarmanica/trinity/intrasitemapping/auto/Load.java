/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.trinity.intrasitemapping.auto;

import br.edimarmanica.metrics.Formatter;
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
     * @return List<Rule<Page,ExtractedValue>>
     */
    public static List<Map<String, String>> loadOffset(File offsetFile, boolean onlyDigitsAndLetters) {
        List<Map<String, String>> offset = new ArrayList<>(); //cada arquivo é um offset

        try (Reader in = new FileReader(offsetFile)) {
            try (CSVParser parser = new CSVParser(in, CSVFormat.EXCEL)) {
                int nrRegistro = 0;
                for (CSVRecord record : parser) {

                    for (int nrRegra = 0; nrRegra < record.size(); nrRegra++) {
                        String value;
                        try {
                            if (onlyDigitsAndLetters) {
                                value = Formatter.formatValue(Preprocessing.filter(record.get(nrRegra)));
                            } else {
                                value = Preprocessing.filter(record.get(nrRegra));
                            }

                        } catch (InvalidValue ex) {
                            value = "";
                        }

                        if (nrRegistro == 0) {
                            Map<String, String> regra = new HashMap<>();
                            regra.put(Formatter.formatURL(record.get(0)), value);
                            offset.add(regra);
                        } else {
                            offset.get(nrRegra).put(Formatter.formatURL(record.get(0)), value);
                        }
                    }
                    nrRegistro++;
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(br.edimarmanica.trinity.intrasitemapping.manual.Mapping.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(br.edimarmanica.trinity.intrasitemapping.manual.Mapping.class.getName()).log(Level.SEVERE, null, ex);
        }

        return offset;
    }

    public static void loadMapping() {

    }
}
