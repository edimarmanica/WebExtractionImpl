/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.templatevariation;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author edimar
 */
public class Teste {
    public static void main(String[] args) {
        Map<String, String> v1 = new HashMap<>();
        v1.put("Teste", "Valor");
        v1.put("Vaca", "Gado");
        
        Map<String, String> v2 = new HashMap<>();
        v2.put("Vaca", "Valor");
        v2.put("Teste", "Gado");
        
        System.out.println(v1.equals(v2));
        
    }
}
