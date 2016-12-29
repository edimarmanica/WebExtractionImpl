/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.trinity.check;

import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Dataset;
import br.edimarmanica.dataset.Domain;
import br.edimarmanica.dataset.Site;
import br.edimarmanica.trinity.intrasitemapping.manual_semoffset.Splitcollumns;
import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author edimar
 */
public class Teste {

    public static void main(String[] args) {
        for (Dataset dataset : Dataset.values()) {
            //System.out.println("Dataset: " + dataset);
            for (Domain domain : dataset.getDomains()) {

                /*if (domain != br.edimarmanica.dataset.weir.Domain.BOOK) {
                    continue;
                }*/
                //System.out.println("\tDomain: " + domain);
                for (Site site : domain.getSites()) {

                    /*if (site != br.edimarmanica.dataset.weir.book.Site.AMAZON) {
                        continue;
                    }*/
                    try {
                        File file = new File(Paths.PATH_BASE + "/" + site.getPath());
                        System.out.println(dataset+";"+domain+";"+site+";"+file.list(new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                               return name.endsWith(".html") || name.endsWith(".htm");
                            }
                        }).length);
                    } catch (Exception ex) {
                        System.out.println("\t\t\tIgnorando");
                    }

                }
            }
        }
    }
}
