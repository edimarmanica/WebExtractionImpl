/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.metrics.orion;

import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Attribute;
import br.edimarmanica.dataset.Site;
import br.edimarmanica.metrics.GroundTruth;
import br.edimarmanica.metrics.SiteWithoutThisAttribute;
import br.edimarmanica.metrics.Formatter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author edimar
 */
public class GroundTruthOrion extends GroundTruth{

    public GroundTruthOrion(Site site, Attribute attribute) {
        super(site, attribute);
    }

    /**
     * GroundTruth => Map<PageID, Value>
     * @throws SiteWithoutThisAttribute 
     */
    @Override
    public void load() throws SiteWithoutThisAttribute {
        try (Reader in = new FileReader(Paths.PATH_BASE + site.getGroundTruthPath())) {
            try (CSVParser parser = new CSVParser(in, CSVFormat.EXCEL.withHeader())) {
                for (CSVRecord record : parser) {
                    if (!record.isMapped(attribute.getAttributeIDbyDataset())) {
                        throw new SiteWithoutThisAttribute(attribute.getAttributeID(), site.getFolderName());
                    }
                    String value = Formatter.formatValue(record.get(attribute.getAttributeIDbyDataset()));
                    if (!value.isEmpty()) {
                        groundTruth.put(Formatter.formatURL(record.get("url")), value);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GroundTruthOrion.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GroundTruthOrion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
