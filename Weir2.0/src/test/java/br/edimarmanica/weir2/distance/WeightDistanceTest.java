/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir2.distance;

import br.edimarmanica.configuration.InterSite;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author edimar
 */
public class WeightDistanceTest extends TestCase {

    public WeightDistanceTest() {
        InterSite.MIN_SHARED_ENTITIES = 1;
    }

    /**
     * Test of normalize method, of class WeightDistance.
     * @throws br.edimarmanica.weir2.distance.NoiseException
     */
    public void testNormalize() throws NoiseException {
        System.out.println("normalize");
        String numericValue = "15 g";
        WeightDistance instance = new WeightDistance();
        Double expResult = 15.0/1000;
        Double result = instance.normalize(numericValue);
        assertEquals(expResult, result);

        numericValue = "2 kg";
        expResult = 2.0;
        result = instance.normalize(numericValue);
        assertEquals(expResult, result);
        
        numericValue = "265 lbs.";
        expResult = 120.2;
        result = instance.normalize(numericValue);
        assertEquals(expResult, result, 0.01);
    }

    public void testDistanceSpecific() throws InsufficientOverlapException {
        System.out.println("distanceSpecific");
        Map<String, String> r1S1 = new HashMap<>();
        r1S1.put("e1", "3 kg"); // 1 pq tem valor nulo em r1S2
        r1S1.put("e2", "4 kg"); // 0 pq são iguais
        r1S1.put("e3", "5 kg"); // nada pq não tem em r1S2
        r1S1.put("e4", "6 kg"); // 0 pq são iguais
        r1S1.put("e5", "7 kg"); // 0 pq são iguais  

        Map<String, String> r1S2 = new HashMap<>();
        r1S2.put("e1", null); // -- valor nulo
        r1S2.put("e2", "4000 g");  // ---
        r1S2.put("e4", "6 kg");  // ---
        r1S2.put("e5", "7 kg");        // ---
        r1S2.put("e6", "8 kg");      //nada pq não tem em r1S1
        r1S2.put("e7", "9 kg");      // nada pq não tem em r1S1

        WeightDistance instance = new WeightDistance();
        double expResult = 1.0 / 4;
        double result = instance.distance(r1S1, r1S2);
        assertEquals(expResult, result, 0.01);

    }
    
     public void testDistanceSpecific02() throws InsufficientOverlapException {
        System.out.println("distanceSpecific");
        Map<String, String> r1S1 = new HashMap<>();
        r1S1.put("e1", "245.05 lbs."); 
        

        Map<String, String> r1S2 = new HashMap<>();
        r1S2.put("e1", "245.05"); // valor padrão é em libras
        
        WeightDistance instance = new WeightDistance();
        double expResult = 0;
        double result = instance.distance(r1S1, r1S2);
        assertEquals(expResult, result, 0.01);

    }
}
