/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.alignment.beans;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author edimar
 */
public class Mapping {

    private Set<Rule> rules; //Set<Site@Rule>
    private boolean complete = false;
    private double threshold = -1;

    public Mapping() {
    }

    public Mapping(Rule rule) {
        addRule(rule);
    }

    public Set<Rule> getRules() {
        return rules;
    }

    public void setRules(Set<Rule> rules) {
        this.rules = rules;
    }

    public void addRule(Rule rule) {
        if (rules == null) {
            rules = new HashSet<>();
        }

        rules.add(rule);
    }

    public void addRules(Set<Rule> rules) {
        if (this.rules == null) {
            this.rules = new HashSet<>();
        }

        this.rules.addAll(rules);
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public void setThreshold(double threshold) {
        if (this.threshold == -1) { //se é -1 quer dizer que ainda não foi definido, pq só o primeiro valor (menor distância que um mapeamento torna-se completo) é considerado
            this.threshold = threshold;
        }
    }

    public double getThreshold() {
        return threshold;
    }

}
