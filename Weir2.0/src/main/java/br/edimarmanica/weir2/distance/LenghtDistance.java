/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir2.distance;

import br.edimarmanica.weir2.rule.type.DataType;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author edimar
 */
public class LenghtDistance extends NumberDistance {


    /**
     * standard unit = meters
     *
     * @param numericValue
     * @return
     */
    @Override
    public Double normalize(String numericValue) throws NoiseException {

        numericValue = numericValue.toLowerCase();

        double multipication = 1;

        if (numericValue.contains("km")) {
            multipication = 1000;
        } else if (numericValue.contains("cm")) {
            multipication = 1.00 / 100;
        } else if (numericValue.matches("(\\d{1,3}(\\,\\d{3})*|\\d+)(\\.\\d{2})?(\\s)?m(\\.)?")) { 
            multipication = 1;
        } else if (numericValue.matches("(\\d{1,3}(\\,\\d{3})*|\\d+)(\\.\\d{2})?")) { 
            multipication = 1;
        } else {
            throw new NoiseException(numericValue, DataType.LENGHT);
        }

        NumberFormat form01 = NumberFormat.getNumberInstance(new Locale("en", "US"));
        try {
            return form01.parse(numericValue.replaceAll("(a-zA-Z)+", "")).doubleValue() * multipication;
        } catch (ParseException ex) {
            throw new NoiseException(numericValue.replaceAll("(a-zA-Z)+", ""), DataType.LENGHT);
        }
    }
}
