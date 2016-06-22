/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.trinity.check;

import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Site;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author edimar
 */
public class AbrirNavegador {

    public static void openPage(String url) {
        try {
            Runtime.getRuntime().exec("firefox " + url);
        } catch (IOException ex) {
            Logger.getLogger(AbrirNavegador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void openPages(Site site, String[] pages) {
        String urls = "";
        for (String page : pages) {
            urls += Paths.PATH_BASE+"/"+site.getPath()+"/"+page + " ";

        }
        openPage(urls);
    }

    public static void main(String[] args) {

        Site site = br.edimarmanica.dataset.weir.book.Site.AMAZON;

        String[] pages = {
            "0001049682.html",
            "0002201127.html",
            "0001050427.html",
            "0001050214.html",
            "0002201089.html",
            "0002201003.html"
        };

        openPages(site, pages);
    }
}
