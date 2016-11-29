/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.check;

import br.edimarmanica.dataset.Site;
import br.edimarmanica.weir_3_0.bean.Rule;
import br.edimarmanica.weir_3_0.distance.DataType;
import br.edimarmanica.weir_3_0.distance.DataTypeController;
import br.edimarmanica.weir_3_0.load.LoadRules;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author edimar
 */
public class DataTypeCheck {
    private static DataType getMostFrequenteDataType(Site site, int ruleID){
        return DataTypeController.getMostFrequentType(loadRule(site, ruleID));
    }
    
     private static Rule loadRule(Site site, int ruleID) {
        Set<Integer> rules = new HashSet<>();
        rules.add(ruleID);
        LoadRules load = new LoadRules(site);
        List<Rule> rulesList = new ArrayList<>(load.getRules(rules));
        if (rulesList.size() != 1) {
            System.out.println("Erro com a regra: " + ruleID + " do site " + site);
        }
        return rulesList.get(0);
    }
     
     public static void main(String[] args) {
        Site site = br.edimarmanica.dataset.orion.driver.Site.STOCKCAR;
        int rule = 67;
        
         System.out.println("DataType: "+getMostFrequenteDataType(site, rule));
    }
}
