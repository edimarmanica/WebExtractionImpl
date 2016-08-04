/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir2.distance;

import br.edimarmanica.configuration.InterSite;
import java.util.HashMap;
import java.util.Map;
import static junit.framework.Assert.assertEquals;
import junit.framework.TestCase;

/**
 *
 * @author edimar
 */
public class CurrencyDistanceTest extends TestCase {

    public CurrencyDistanceTest(String testName) {
        super(testName);
        InterSite.MIN_SHARED_ENTITIES = 1;
    }

    /**
     * Test of normalize method, of class CurrencyDistance.
     * @throws java.lang.Exception
     */
    public void testNormalize() throws Exception {
        System.out.println("normalize");
        String numericValue = "$ -19.05";
        CurrencyDistance instance = new CurrencyDistance();
        Double expResult = -19.05;
        Double result = instance.normalize(numericValue);
        assertEquals(expResult, result);

        numericValue = "R$ 15.325,23";//tem que ter o espaço depois do cifrão
        instance = new CurrencyDistance();
        expResult = 15325.23;
        result = instance.normalize(numericValue);
        assertEquals(expResult, result);

        numericValue = "$ 43,217.45";
        expResult = 43217.45;
        result = instance.normalize(numericValue);
        assertEquals(expResult, result);

        numericValue = "€ 2123,89";
        expResult = 2123.89;
        result = instance.normalize(numericValue);
        assertEquals(expResult, result);

        numericValue = "8,759.59 €";
        System.out.println(numericValue.matches(".*\\.\\d\\d €"));
        expResult = 8759.59;
        result = instance.normalize(numericValue);
        assertEquals(expResult, result);
        
        numericValue = "1.79";
        expResult = 1.79;
        result = instance.normalize(numericValue);
        assertEquals(expResult, result);
        
    }
    
     public void testDistance() throws InsufficientOverlapException {
        System.out.println("distance");

        Map<String, String> r1S1 = new HashMap<>();
        r1S1.put("e1", "7.19"); 

        Map<String, String> r1S2 = new HashMap<>();
        r1S2.put("e1", "$ 7.19");               
        

        CurrencyDistance instance = new CurrencyDistance();
        double expResult = 0; //4 is the number of shared entities
        double result = instance.distance(r1S1, r1S2);
        System.out.println("Result: " + result);
        assertEquals(expResult, result, 0.001);

    }
}
