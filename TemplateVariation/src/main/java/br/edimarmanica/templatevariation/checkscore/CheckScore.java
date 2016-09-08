/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.templatevariation.checkscore;

import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Attribute;
import br.edimarmanica.dataset.Dataset;
import br.edimarmanica.dataset.Domain;
import br.edimarmanica.dataset.Site;
import br.edimarmanica.metrics.Results;
import br.edimarmanica.metrics.SiteWithoutThisAttribute;
import br.edimarmanica.templatevariation.auto.UnionRules;
import br.edimarmanica.templatevariation.auto.bean.Rule;
import br.edimarmanica.templatevariation.auto.load.LoadRule;
import br.edimarmanica.templatevariation.auto.similarity.DomainSimilarity;
import br.edimarmanica.templatevariation.auto.similarity.EntitySimilarity;
import br.edimarmanica.templatevariation.auto.similarity.LabelSimilarity;
import br.edimarmanica.templatevariation.auto.similarity.RuleSimilarity;
import br.edimarmanica.templatevariation.auto.similarity.SizeSimilarity;
import br.edimarmanica.templatevariation.auto.similarity.XPathSimilarity;
import br.edimarmanica.templatevariation.manual.ComplementaryRule;
import br.edimarmanica.templatevariation.manual.MasterRule;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 *
 * @author edimar
 */
public class CheckScore {

    public static final String[] header = {"DATASET", "DOMAIN", "SITE", "ATTRIBUTE", "RULE", "SIM_LABEL", "SIM_XPATH", "SIM_ATTR", "SIM_ENTITY", "SIZE_BOOST", "CLASS"};

    private final Site site;
    private final Map<String, Map<String, String>> allRules;
    private int nrPages;
    private boolean append = false;

    public CheckScore(Site site) {
        this.site = site;

        Results results = new Results(site);
        allRules = results.loadAllRules(Paths.PATH_INTRASITE);
    }

    // para cada atributo
    //seleciona as regras mestres (serão ignoradas)
    //seleciona as regras complementares (e classifica como complementares)
    //seleciona as regras que extraem os mesmos valores que as regras complementares (e classifica como complementares)
    //classifica as demais regras que também são disjuntas como não complementares
    //imprime a regra e os escores de similaridade
    public void execute() {
        //      printer = new Printer(site, Paths.PATH_TEMPLATE_VARIATION_MANUAL);
        nrPages = getNrPages(site);

        for (Attribute attribute : site.getDomain().getAttributes()) { //para cada atributo
            try {
                //seleciona as regras mestres (serão ignoradas)
                String masterRuleID = MasterRule.getMasterRule(site, attribute);
                if (masterRuleID == null) {
                    continue;
                }
                //seleciona as regras complementares (e classifica como complementares)
                List<String> complementaryRulesID = getComplementaryRules(attribute, masterRuleID);
                if (complementaryRulesID.isEmpty()) {
                    continue;
                }

                //seleciona as regras que extraem os mesmos valores que as regras complementares (e classifica como complementares)
                List<String> identicalComplementaryRulesID = getIdenticalComplementaryRules(complementaryRulesID);

                //seleciona as demais regras (regras que não são nem mestre, nem complementares, nem identicas as complementares) que são disjuntas a regra mestre
                List<String> otherRules = getOtherRules(masterRuleID, complementaryRulesID, identicalComplementaryRulesID);

                //imprime
                print(attribute, masterRuleID, complementaryRulesID, identicalComplementaryRulesID, otherRules);
            } catch (SiteWithoutThisAttribute ex) {
                continue;
            }

        }
    }

    /**
     * Obtém as regras complementares
     *
     * @param attr
     * @param masterRuleID
     * @return
     * @throws SiteWithoutThisAttribute
     */
    public List<String> getComplementaryRules(Attribute attr, String masterRuleID) throws SiteWithoutThisAttribute {
        List<String> complementaryRulesID = new ArrayList<>();
        Map<String, String> extractedValues = allRules.get(masterRuleID);

        while (extractedValues.size() != nrPages) {
            ComplementaryRule rum = new ComplementaryRule(site, attr, allRules, complementaryRulesID, extractedValues, nrPages);

            String complementaryRuleID = rum.getComplementaryRule();
            if (complementaryRuleID == null) {
                break; //não tem mais complementares
            }

            complementaryRulesID.add(complementaryRuleID);
            extractedValues.putAll(allRules.get(complementaryRuleID));
        }

        return complementaryRulesID;
    }

    /**
     * regras que extraem os mesmos valores que as regras complementares
     *
     * @param complementaryRulesID
     * @return
     */
    public List<String> getIdenticalComplementaryRules(List<String> complementaryRulesID) {
        List<String> identicalComplementaryRulesID = new ArrayList<>();

        for (String complementaryRule : complementaryRulesID) {
            for (String candIdentical : allRules.keySet()) {
                if (candIdentical.equals(complementaryRule)) { //é a mesma
                    continue;
                }

                //tem que ter o mesmo número de valores extraídos
                if (allRules.get(complementaryRule).size() != allRules.get(candIdentical).size()) {
                    continue;
                }

                //se tem os mesmos valores
                if (allRules.get(complementaryRule).equals(allRules.get(candIdentical))) {
                    identicalComplementaryRulesID.add(candIdentical);
                }
            }
        }
        return identicalComplementaryRulesID;
    }

    /**
     * Seleciona as regras disjuntas a regra mestre que não são complementares
     * nem extrai os mesmos valores que as complementares
     *
     * @param masterRule
     * @param complementaryRule
     * @param identicalComplementaryRule
     * @return
     */
    public List<String> getOtherRules(String masterRule, List<String> complementaryRule, List<String> identicalComplementaryRule) {
        List<String> otherRules = new ArrayList<>();
        for (String candOtherRule : allRules.keySet()) {
            if (candOtherRule.equals(masterRule)) {
                continue;
            }

            if (complementaryRule.contains(candOtherRule)) {
                continue;
            }

            if (identicalComplementaryRule.contains(candOtherRule)) {
                continue;
            }

            if (isDisjoint(masterRule, candOtherRule)) {
                otherRules.add(candOtherRule);
            }
        }
        return otherRules;
    }

    public static int getNrPages(Site site) {
        File dir = new File(Paths.PATH_BASE + site.getPath());
        return dir.list().length;
    }

    private List<String> getSimilarities(Attribute attr, String ruleID, String masterRuleID) {
        LoadRule lr = new LoadRule(site, Integer.parseInt(ruleID.replaceAll("rule_", "").replaceAll(".csv", "")));
        Rule rule = lr.loadRule();

        lr = new LoadRule(site, Integer.parseInt(masterRuleID.replaceAll("rule_", "").replaceAll(".csv", "")));
        Rule masterRule = lr.loadRule();

        RuleSimilarity xpathSim = new XPathSimilarity(masterRule, rule);
        RuleSimilarity labelSim = new LabelSimilarity(masterRule, rule);
        RuleSimilarity domainSim = new DomainSimilarity(masterRule, rule);
        RuleSimilarity entitySim = new EntitySimilarity(UnionRules.getMasterRulesInOtherSitesManual(site, attr), masterRule, rule);
        RuleSimilarity sizeSim = new SizeSimilarity(nrPages, masterRule, rule);

        List<String> sim = new ArrayList<>();
        sim.add(labelSim.score() + "");
        sim.add(xpathSim.score() + "");
        sim.add(domainSim.score() + "");
        sim.add(entitySim.score() + "");
        sim.add(sizeSim.score() + "");
        return sim;
    }

    private void print(Attribute attr, String masterRule, List<String> complementaryRule, List<String> identicalComplementaryRule, List<String> otherRules) {
        for (String rule : complementaryRule) {
            List<String> dataRecord = new ArrayList<>();
            dataRecord.add(site.getDomain().getDataset().toString());
            dataRecord.add(site.getDomain().toString());
            dataRecord.add(site.toString());
            dataRecord.add(attr.toString());
            dataRecord.add(rule);
            dataRecord.addAll(getSimilarities(attr, rule, masterRule));
            dataRecord.add("COMPLEMENTAR");
            printResults(dataRecord);
        }

        for (String rule : identicalComplementaryRule) {
            List<String> dataRecord = new ArrayList<>();
            dataRecord.add(site.getDomain().getDataset().toString());
            dataRecord.add(site.getDomain().toString());
            dataRecord.add(site.toString());
            dataRecord.add(attr.toString());
            dataRecord.add(rule);
            dataRecord.addAll(getSimilarities(attr, rule, masterRule));
            dataRecord.add("COMPLEMENTAR");
            printResults(dataRecord);
        }

        for (String rule : otherRules) {
            List<String> dataRecord = new ArrayList<>();
            dataRecord.add(site.getDomain().getDataset().toString());
            dataRecord.add(site.getDomain().toString());
            dataRecord.add(site.toString());
            dataRecord.add(attr.toString());
            dataRecord.add(rule);
            dataRecord.addAll(getSimilarities(attr, rule, masterRule));
            dataRecord.add("NON-COMPLEMENTAR");
            printResults(dataRecord);
        }
    }

    private void printResults(List<String> dataRecord) {
        /**
         * ********************** results ******************
         */
        File dir = new File(Paths.PATH_SCORE_ANALYSIS + "/" + site.getPath());
        dir.mkdirs();

        File file = new File(dir.getAbsolutePath() + "/result.csv");
        CSVFormat format;
        if (append) {
            format = CSVFormat.EXCEL;
        } else {
            format = CSVFormat.EXCEL.withHeader(header);
        }

        try (Writer out = new FileWriter(file, append)) {
            try (CSVPrinter csvFilePrinter = new CSVPrinter(out, format)) {
                csvFilePrinter.printRecord(dataRecord);
            }
        } catch (IOException ex) {
            Logger.getLogger(CheckScore.class.getName()).log(Level.SEVERE, null, ex);
        }
        append = true;
    }

    private boolean isDisjoint(String rule1, String rule2) {

        if (nrPages < allRules.get(rule1).size() + allRules.get(rule2).size()) {
            return false;
        }

        Set<String> intersection = new HashSet<>();
        intersection.addAll(allRules.get(rule1).keySet());
        intersection.retainAll(allRules.get(rule2).keySet());
        return intersection.isEmpty();
    }

    public static void main(String[] args) {

        for (Dataset dataset : Dataset.values()) {
            System.out.println("Dataset: " + dataset);
            for (Domain domain : dataset.getDomains()) {
                System.out.println("\tDomain: " + domain);
                for (Site site : domain.getSites()) {
                    /*if (site != br.edimarmanica.dataset.weir.soccer.Site.FOOTBALL) {
                        continue;
                    }*/

                    CheckScore check = new CheckScore(site);
                    check.execute();
                }
            }
        }
    }

}
