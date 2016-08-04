/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir2.rule.type;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author edimar
 */
public class RuleDataTypeTest extends TestCase {
    
    public RuleDataTypeTest(String testName) {
        super(testName);
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
     * Test of getMostFrequentType method, of class RuleDataType.
     */
    public void testGetMostFrequentType_Collection() {
        System.out.println("getMostFrequentType");
        Map<String, String> r1S2 = new HashMap<>();
        r1S2.put("e1", "-0.239");   
        r1S2.put("e2", "4.326"); 
        r1S2.put("e3", "-2.066"); 
        r1S2.put("e4", "-3.625"); 
        
        DataType expResult = DataType.NUMBER;
        DataType result = RuleDataType.getMostFrequentType(r1S2.values());
        assertEquals(expResult, result);
    }

}
