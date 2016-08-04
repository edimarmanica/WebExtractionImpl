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
public class PercentualDistance extends NumberDistance {

    /**
     * standard unit = grams
     *
     * @param numericValue
     * @return
     * @throws br.edimarmanica.weir2.distance.NoiseException
     */
    @Override
    public Double normalize(String numericValue) throws NoiseException {
     
        NumberFormat form01 = NumberFormat.getNumberInstance(new Locale("en", "US"));
        try {
            return form01.parse(numericValue.replaceAll("%|\\+|-*", "")).doubleValue();
        } catch (ParseException ex) {
            throw new NoiseException(numericValue, DataType.PERCENTUAL);
        }
    }
}
