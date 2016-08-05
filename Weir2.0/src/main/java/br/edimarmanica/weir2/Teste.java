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
       
        String st = "1.81 m";
        System.out.println(st.replaceAll("(a-zA-Z)+", ""));
    }
}
