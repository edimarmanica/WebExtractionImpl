/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.alignment;

import br.edimarmanica.configuration.General;
import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Dataset;
import br.edimarmanica.dataset.Domain;
import br.edimarmanica.dataset.Site;
import br.edimarmanica.weir2.rule.type.RulesDataTypeController;

/**
 *
 * @author edimar
 */
public class Main {

    /**
     * Verifica o tipo mais frequente em cada regra e persiste
     *
     * @param domain
     * @param pathRules
     * @param pathOutput
     */
    private static void persistDataType(Domain domain, String pathRules, String pathOutput) {
        for (Site site : domain.getSites()) {
            System.out.println("\t\tSite: " + site);
            RulesDataTypeController.persiste(site, pathRules, pathOutput);
        }
    }

    /**
     * Calcula a distancia entre cada par de regras e persiste
     *
     * @param domain
     * @param pathRules
     * @param pathOutput
     */
    private static void persistScores(Domain domain, String pathRules, String pathOutput) {
        ScorePairs sp = new ScorePairs(domain, pathRules, pathOutput);
        sp.compareAndPersist();
    }

    public static void main(String[] args) {
        General.DEBUG = true;

        String pathRules = Paths.PATH_TRINITY + "/ved_w1_auto/";
        String pathOuput = Paths.PATH_TRINITY_PLUS_WEIR;
        for (Dataset dataset : Dataset.values()) {
            System.out.println("Dataset: " + dataset);
            for (Domain domain : dataset.getDomains()) {
                /*if (domain != br.edimarmanica.dataset.swde.Domain.NBA_PLAYER) {
                    continue;
                }*/
                System.out.println("\tDomain: " + domain);

                //persistDataType(domain, pathRules, pathOuput);
                persistScores(domain, pathRules, pathOuput);
            }
        }
    }
}
