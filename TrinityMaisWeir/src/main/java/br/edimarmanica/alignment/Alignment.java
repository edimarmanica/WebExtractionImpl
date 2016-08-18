/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.alignment;

import br.edimarmanica.alignment.beans.Mapping;
import br.edimarmanica.alignment.beans.Pair;
import br.edimarmanica.alignment.beans.Rule;
import br.edimarmanica.configuration.General;
import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Domain;
import br.edimarmanica.dataset.Site;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 *
 * @author edimar
 */
public class Alignment {

    private final Domain domain;
    private final String path;

    private Set<Mapping> mappings;

    public Alignment(Domain domain, String path) {
        this.domain = domain;
        this.path = path;
    }

    public void execute() {
        List<Pair> sortedPairs = ScorePairs.loadAndSort(domain, path);
        createSingleMappings(sortedPairs);
        integration(sortedPairs);
        persistResults();
    }

    private void createSingleMappings(List<Pair> sortedPairs) {
        mappings = new HashSet<>();

        Set<Rule> rules = new HashSet<>();
        for (Pair pair : sortedPairs) {
            if (!rules.contains(pair.getRule1())) {
                rules.add(pair.getRule1());
                mappings.add(new Mapping(pair.getRule1()));
            }

            if (!rules.contains(pair.getRule2())) {
                rules.add(pair.getRule2());
                mappings.add(new Mapping(pair.getRule2()));
            }

        }
    }

    private void integration(List<Pair> sortedPairs) {

        for (Pair pair : sortedPairs) {
            if (General.DEBUG) {
                System.out.println("Evaluating the pair rule " + pair.getRule1().getRuleID() + " of Site " + pair.getRule1().getSite() + " with rule " + pair.getRule2().getRuleID() + " of Site " + pair.getRule2().getSite());
                System.out.println("\tScore: " + pair.getDistance());
            }

            if (pair.getDistance() == 1) {
                break; //Os próximos todos são 1 pq está ordenado. Não vai unir mapeamentos com distância de 1 (totalmente diferentes)
            }

            //se as duas regras já estão no mesmo mapping ignora
            if (inSameMapping(pair.getRule1(), pair.getRule2(), mappings)) {
                continue;
            }

            //Minha tentativa: não está no artigo: se as duas regras são do mesmo site e ainda pertencem a mapeamentos únicos, então ignora, pq possivelmente uma delas é uma regra fraca. Ver caso no Goodreadings as regras 79 e 467 não entram nos filtros pq tem alguns valores um pouco diferente para título, mas extraem a mesma coisa
            if (pair.getRule1().getSite() == pair.getRule2().getSite()) {
                if (getMapping(pair.getRule1(), mappings).getRules().size() == 1 && getMapping(pair.getRule2(), mappings).getRules().size() == 1) {
                    continue;
                }
            }

            if (isComplete(pair.getRule1(), mappings) || isComplete(pair.getRule2(), mappings) || isLC(pair.getRule1(), pair.getRule2(), mappings)) {
                //Minha tentativa: não está no artigo: se a distância é 0, então ignora, pq possivelmente uma delas é uma regra fraca. Ver name do nbaplayer
                if (pair.getDistance() == 0.0) {
                    continue;
                }

                Mapping mR1 = getMapping(pair.getRule1(), mappings);
                mR1.setComplete(true);
                mR1.setThreshold(pair.getDistance());
                Mapping mR2 = getMapping(pair.getRule2(), mappings);
                mR2.setComplete(true);
                mR2.setThreshold(pair.getDistance());

                if (General.DEBUG) {
                    System.out.println("\t Mapping complete");
                    System.out.print("\t M_R:");
                    for (Rule r : getMapping(pair.getRule1(), mappings).getRules()) {
                        System.out.print(r.getSite() + "->" + r.getRuleID() + ",");
                    }
                    System.out.println("");
                    System.out.print("\t M_S:");
                    for (Rule r : getMapping(pair.getRule2(), mappings).getRules()) {
                        System.out.print(r.getSite() + "->" + r.getRuleID() + ",");
                    }
                    System.out.println("");
                }
            } else {
                if (General.DEBUG) {
                    System.out.println("\t Unindo...");
                }
                Mapping mR1 = getMapping(pair.getRule1(), mappings);
                Mapping mS1 = getMapping(pair.getRule2(), mappings);
                Mapping mUnion = new Mapping();
                mUnion.addRules(mR1.getRules());
                mUnion.addRules(mS1.getRules());

                if (General.DEBUG) {
                    System.out.print("\t ");
                    for (Rule r : mUnion.getRules()) {
                        System.out.print(r.getSite() + "->" + r.getRuleID() + ",");
                    }
                    System.out.println("");
                }

                mappings.remove(mR1);
                mappings.remove(mS1);
                mappings.add(mUnion);
            }
        }
    }

    /**
     *
     * @param r
     * @return the Mapping containing the rule r
     */
    private static Mapping getMapping(Rule r, Set<Mapping> mappings) {
        for (Mapping m : mappings) {
            if (m.getRules().contains(r)) {
                return m;
            }
        }
        System.out.println("Não deve entrar aqui! - WEIR.java - getMapping");
        System.exit(-1);
        return null;
    }

    /**
     *
     * @param r
     * @param s
     * @return true if the rules are already in the same mapping
     */
    private static boolean inSameMapping(Rule r, Rule s, Set<Mapping> mappings) {
        boolean flag = false;
        for (Mapping m : mappings) {

            if (m.getRules().contains(r) && m.getRules().contains(s)) {
                flag = true;
                break;
            }
        }

        return flag;
    }

    /**
     *
     * @param r
     * @param wrappers
     * @return true if the mapping that contains the rule r is complete
     */
    private static boolean isComplete(Rule r, Set<Mapping> mappings) {
        return getMapping(r, mappings).isComplete();
    }

    /**
     *
     * @param r1
     * @param s1
     * @param mappings true se a intersecção entre (os sites do mapping que
     * contém o r1) e (os sites do mapping que contém o s1) não é vazia -- passo
     * 6 do algoritmo
     * @return
     */
    private static boolean isLC(Rule r1, Rule s1, Set<Mapping> mappings) {
        Mapping mR1 = getMapping(r1, mappings);
        Mapping mS1 = getMapping(s1, mappings);

        Set<Site> sitesR1 = new HashSet<>();
        for (Rule r : mR1.getRules()) {
            sitesR1.add(r.getSite());
        }

        for (Rule s : mS1.getRules()) {
            if (sitesR1.contains(s.getSite())) {
                return true;
            }
        }

        return false;
    }

    private void persistResults() {
        String[] header = {"MAP_ID", "SITE", "RULE", "SIZE", "THRESHOLD"};
        File file = new File(path + "/" + domain.getPath());
        file.mkdirs();

        try (Writer out = new FileWriter(file.getAbsolutePath() + "/mappings.csv")) {
            try (CSVPrinter csvFilePrinter = new CSVPrinter(out, CSVFormat.EXCEL.withHeader(header))) {
                int i = 0;
                for (Mapping map : mappings) {
                    if (map.getRules().size() < 3) {
                        continue;
                    }

                    for (Rule rule : map.getRules()) {
                        List<String> dataRecord = new ArrayList<>();
                        dataRecord.add(i + "");
                        dataRecord.add(rule.getSite().toString());
                        dataRecord.add(rule.getRuleID() + "");
                        dataRecord.add(map.getRules().size() + "");
                        dataRecord.add(map.getThreshold() + "");
                        csvFilePrinter.printRecord(dataRecord);
                    }
                    i++;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Alignment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        General.DEBUG = true;
        Domain domain = br.edimarmanica.dataset.swde.Domain.NBA_PLAYER;
        String path = Paths.PATH_TRINITY_PLUS_WEIR;
        Alignment align = new Alignment(domain, path);
        align.execute();
    }
}
