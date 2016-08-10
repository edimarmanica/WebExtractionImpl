/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir2.distance;

import br.edimarmanica.weir2.rule.type.DataType;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author edimar
 */
public class PhoneDistance extends TypeAwareDistance {

    /**
     * 0 if vR1 == vR2, 1 otherwise
     *
     * @param vR1
     * @param vR2
     * @return
     */
    @Override
    public double distanceSpecific(String vR1, String vR2) {
        String auxR1, auxR2;
        try {
            auxR1 = normalize(vR1);
            auxR2 = normalize(vR2);
        } catch (NoiseException ex) {
            return 1;
        }

        if (auxR1.equals(auxR2)) {
            return 0;
        } else {
            return 1;
        }

    }

    /**
     *
     * @param phone
     * @return tira os 4 últimos digitos pois pode ser o ramal
     * @throws br.edimarmanica.weir2.distance.NoiseException
     */
    public static String normalize(String phone) throws NoiseException {
        String aux = phone.replaceAll("[^\\d]", ""); //só ficam os números

        try {
            return aux.substring(0, aux.length() - 4);
        } catch (StringIndexOutOfBoundsException ex) {
            throw new NoiseException(aux, DataType.PHONE);
        }
    }
}
