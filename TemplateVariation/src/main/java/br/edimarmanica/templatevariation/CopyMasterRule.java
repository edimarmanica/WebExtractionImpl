/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.templatevariation;

import br.edimarmanica.dataset.Attribute;
import br.edimarmanica.dataset.Dataset;
import br.edimarmanica.dataset.Domain;
import br.edimarmanica.dataset.Site;
import br.edimarmanica.metrics.SiteWithoutThisAttribute;
import br.edimarmanica.templatevariation.manual.MasterRule;
import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author edimar
 */
public class CopyMasterRule {

    public static void copy(Site site, String ruleFileName) throws IOException {
        Path FROM = Paths.get(br.edimarmanica.configuration.Paths.PATH_INTRASITE+"/"+site.getPath()+"/extracted_values/"+ruleFileName);
        
        File toDir = new File("/media/edimar/Dados/inatan_weir/"+site.getPath()+"/extracted_values/");
        toDir.mkdirs();
                
        Path TO = Paths.get("/media/edimar/Dados/inatan_weir/"+site.getPath()+"/extracted_values/"+ruleFileName);
        //overwrite existing file, if exists
        CopyOption[] options = new CopyOption[]{
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.COPY_ATTRIBUTES
        };
        Files.copy(FROM, TO, options);
    }

    public static void main(String[] args) throws IOException {
        for(Domain domain: Dataset.SWDE.getDomains()){
            for(Site site: domain.getSites()){
                System.out.println("Site:"+site);
                for(Attribute attr: domain.getAttributes()){
                    try {
                        String rule = MasterRule.getMasterRule(site, attr);
                        copy(site, rule);
                    } catch (SiteWithoutThisAttribute ex) {
                        Logger.getLogger(CopyMasterRule.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
}
