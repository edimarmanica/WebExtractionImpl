/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.check;

import br.edimarmanica.dataset.Site;
import br.edimarmanica.weir_3_0.bean.Rule;
import br.edimarmanica.weir_3_0.distance.TypeAwareDistance;
import br.edimarmanica.weir_3_0.load.LoadRules;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author edimar
 */
public class PairDistanceCheck {

    private final int r1;
    private final int r2;
    private final Site siteR1;
    private final Site siteR2;

    public PairDistanceCheck(int r1, int r2, Site siteR1, Site siteR2) {
        this.r1 = r1;
        this.r2 = r2;
        this.siteR1 = siteR1;
        this.siteR2 = siteR2;
    }

    private void printDistance() {
        Rule rule1 = loadRule(r1, siteR1);
        Rule rule2 = loadRule(r2, siteR2);

        System.out.println("Distance: " + TypeAwareDistance.typeDistance(rule1, rule2));
    }

    private Rule loadRule(int ruleID, Site site) {
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
         Site site1 = br.edimarmanica.dataset.orion.driver.Site.GP2;
        int r1 = 140;
        Site site2 = br.edimarmanica.dataset.orion.driver.Site.GPUPDATE;
        int r2 = 3575;

        PairDistanceCheck check = new PairDistanceCheck(r1, r2, site1, site2);
        check.printDistance();
    }
}
