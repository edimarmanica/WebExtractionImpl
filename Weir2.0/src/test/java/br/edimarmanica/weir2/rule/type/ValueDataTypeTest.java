/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir2.rule.type;

import junit.framework.TestCase;

/**
 *
 * @author edimar
 */
public class ValueDataTypeTest extends TestCase {

    public ValueDataTypeTest(String testName) {
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
     * Test of getDataType method, of class ValueDataType.
     */
    public void testDate() {
        System.out.println("getDataType");
        String examples[] = {"2012", "01/01/2012", "1/1/2012",
            "2012-01-01", "2012-1-1", "01/2012", "1/2012",
            "2012-01", "2012-1", "05 Oct 1995", "5 October 1995",
            "October 23, 1995", "Jan 28, 1978", "January 1996"};
        DataType expResult = DataType.DATE;

        for (String value : examples) {
            DataType result = ValueDataType.getDataType(value);
            assertEquals(value, expResult, result);
        }
        
        String contraExample = "October 23, 1995 (age 25)";
        expResult = DataType.STRING;
        DataType result = ValueDataType.getDataType(contraExample);
        assertEquals(expResult, result);
    }
    
    public void testCurrency() {
        System.out.println("getDataType");
        String examples[] = {"$231,400", "$201,500"};
        DataType expResult = DataType.CURRENCY;

        for (String value : examples) {
            DataType result = ValueDataType.getDataType(value);
            assertEquals(value, expResult, result);
        }
    }
    
    public void testPhone() {
        System.out.println("getDataType");
        String examples[] = {"(803) 256-4220"};
        DataType expResult = DataType.PHONE;

        for (String value : examples) {
            DataType result = ValueDataType.getDataType(value);
            assertEquals(value, expResult, result);
        }
    }

}
