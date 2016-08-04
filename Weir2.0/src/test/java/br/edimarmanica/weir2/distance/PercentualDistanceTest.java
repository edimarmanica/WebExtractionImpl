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
public class PercentualDistanceTest extends TestCase {
    
    public PercentualDistanceTest(String testName) {
        super(testName);
        
        InterSite.MIN_SHARED_ENTITIES = 1;
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of normalize method, of class PercentualDistance.
     */
    public void testNormalize() throws Exception {
        System.out.println("normalize");
        String numericValue = "-4.33%";
        PercentualDistance instance = new PercentualDistance();
        Double expResult = 4.33;
        Double result = instance.normalize(numericValue);
        assertEquals(expResult, result);
        
        numericValue = "+4.33%";
        expResult = 4.33;
        result = instance.normalize(numericValue);
        assertEquals(expResult, result);
        
        numericValue = "4.33%";
        expResult = 4.33;
        result = instance.normalize(numericValue);
        assertEquals(expResult, result);
        
    }
    
    public void testDistance02() throws InsufficientOverlapException {
        System.out.println("distance");
        Map<String, String> r1S1 = new HashMap<>();
        r1S1.put("e1", "-0.24%"); 
        r1S1.put("e2", "+4.33%"); 
        r1S1.put("e3", "-2.07"); 
        r1S1.put("e4", "-3.62%"); 
        

        Map<String, String> r1S2 = new HashMap<>();
        r1S2.put("e1", "-0.239");   
        r1S2.put("e2", "4.326"); 
        r1S2.put("e3", "-2.066"); 
        r1S2.put("e4", "-3.625"); 
        

        PercentualDistance instance = new PercentualDistance();
        double expResult = 0; //4 é o número de instâncias compartilhadas
        double result = instance.distance(r1S1, r1S2);
        assertEquals(expResult, result, 0.0);

    }
    
}
