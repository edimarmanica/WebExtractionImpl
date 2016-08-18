/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package check;

import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Domain;
import br.edimarmanica.wrapperinduction.trinity.LoadRules;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
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
public class CheckAlignment {

    private final Domain domain;
    private final String pathMapping;
    private final String pathRules;

    public CheckAlignment(Domain domain, String pathMapping, String pathRules) {
        this.domain = domain;
        this.pathMapping = pathMapping;
        this.pathRules = pathRules;
    }

    public void execute() {
        String[] header = {"MAP_ID", "SITE", "RULE", "SIZE", "THRESHOLD"};

        try (Reader in = new FileReader(pathMapping + "/" + domain.getPath() + "/" + "/mappings.csv")) {
            try (CSVParser parser = new CSVParser(in, CSVFormat.EXCEL.withHeader())) {
                for (CSVRecord record : parser) { //para cada value
                    System.out.print(record.get("MAP_ID"));
                    System.out.print(";");
                    System.out.print(record.get("SITE"));
                    System.out.print(";");
                    System.out.print(record.get("RULE"));
                    System.out.print(";");
                    System.out.print(record.get("SIZE"));
                    System.out.print(";");
                    System.out.print(record.get("THRESHOLD"));
                    System.out.print(";");

                    int i = 0;
                    Map<String, String> pageValues = LoadRules.loadPageValues(new File(pathRules + "/" + domain.getSiteOf(record.get("SITE")).getPath() + "/extracted_values/" + record.get("RULE")), false);
                    for (String page : pageValues.keySet()) {
                        System.out.print(pageValues.get(page));
                        System.out.print(";");

                        if (i >= 5) {
                            break;
                        }
                        i++;
                    }

                    System.out.println();

                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LoadRules.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LoadRules.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        Domain domain = br.edimarmanica.dataset.swde.Domain.NBA_PLAYER;
        String pathMap = Paths.PATH_TRINITY_PLUS_WEIR;
        String pathRules = Paths.PATH_TRINITY + "/ved_w1_auto/";
        CheckAlignment check = new CheckAlignment(domain, pathMap, pathRules);
        check.execute();
    }

}
