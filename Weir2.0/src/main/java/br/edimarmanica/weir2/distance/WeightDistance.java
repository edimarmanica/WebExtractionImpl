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
public class WeightDistance extends NumberDistance {

    /**
     * standard unit = grams
     *
     * @param numericValue
     * @return
     */
    @Override
    public Double normalize(String numericValue) throws NoiseException {
        numericValue = numericValue.toLowerCase();

        double multipication = 1;

        if (numericValue.contains("kg")) {
            multipication = 1000;
        } else if (numericValue.matches("([^(a-zA-Z)])+g([^(a-zA-Z)])*")) { //evitar que mm entre aqui
            multipication = 1;
        } else {
            throw new NoiseException(numericValue, DataType.WEIGHT);
        }

        NumberFormat form01 = NumberFormat.getNumberInstance(new Locale("en", "US"));
        try {
            return form01.parse(numericValue.replaceAll("(a-zA-Z)+", "")).doubleValue() * multipication;
        } catch (ParseException ex) {
            throw new NoiseException(numericValue.replaceAll("(a-zA-Z)+", ""), DataType.WEIGHT);
        }
    }
}
