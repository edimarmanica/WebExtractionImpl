/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir2;

import br.edimarmanica.dataset.weir.finance.Site;
import br.edimarmanica.weir2.distance.CurrencyDistance;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author edimar
 */
public class Teste {

    public static void main(String[] args) {
        /*
        2012        
        01/01/2012 or 1/1/2012
        2012-01-01 or 2012-1-1        
        01/2012 or 1/2012        
        2012-01 or 2012-1
        05 Oct 1995 or 5 October 1995
        October 23, 1995" or Jan 28, 1978
        January 1996
        */
        String st = "January 1996";
        String months = "(Jan|Feb|Mar|Apr|Aug|Sept|Oct|Nov|Dec|January|February|March|April|May|Jun|July|August|September|October|November|December)";
        String p = "\\d{4}|\\d{1,2}/\\d{1,2}/\\d{4}|\\d{4}-\\d{1,2}-\\d{1,2}|\\d{1,2}/\\d{4}|\\d{4}-\\d{1,2}"
                + "|\\d{1,2} "+months+" \\d{4}|"+months+" \\d{1,2}, \\d{4}|"+months+" \\d{4}";
        System.out.println(st.matches(p));
    }
}
