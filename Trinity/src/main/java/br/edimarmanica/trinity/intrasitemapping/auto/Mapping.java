/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.trinity.intrasitemapping.auto;

import br.edimarmanica.trinity.extract.Extract;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author edimar
 */
public class Mapping {

    private final Map<String, String> groupOffset0; 
    private final List<Map<String, String>> offsetX;

    /**
     *
     * @param groupOffset0 a group of the offset0. Ex: Map<URL, ExpectedValue>
     * @param offsetX all the groups of an offset x. Ex: List<Rule<URL, ExpectedValue>>
     */
    public Mapping(Map<String, String> groupOffset0, List<Map<String, String>> offsetX) {
        this.groupOffset0 = groupOffset0;
        this.offsetX = offsetX;
    }

    /**
     * 
     * @param groupOffset0
     * @param groupOffsetX
     * @return the number of pages that the two groups extract the same value
     */
    private int nrMatches(Map<String, String> groupOffset0, Map<String, String> groupOffsetX) {
        int nrMatches = 0;

        for (String page: groupOffset0.keySet()) {
            if (groupOffset0.get(page).equals(groupOffsetX.get(page))) {
                nrMatches++;
            }
        }
        return nrMatches;
    }

    /**
     * @return a regra do offsetX que deve ser mapeada para a regra ruleIndex do
     * offset0
     * @throws br.edimarmanica.trinity.intrasitemapping.auto.MappingNotFoundException
     */
    public int mapping() throws MappingNotFoundException {
        int maxNrMatches = 0;
        int positionMaxMatches = -1;

        for (int nrGroup = 0; nrGroup < offsetX.size(); nrGroup++) {
            int nrMatches = nrMatches(groupOffset0, offsetX.get(nrGroup));

            if (nrMatches == Extract.NR_SHARED_PAGES) {
                return nrGroup;
            }

            if (nrMatches > maxNrMatches) {
                maxNrMatches = nrMatches;
                positionMaxMatches = nrGroup;
            }

        }

        //tem que casar com pelo menos 50% + 1. Não pode ser todos pois existem casos onde não extrai pq os NR_SHARED_PAGES são diferentes
        if (positionMaxMatches == -1 || maxNrMatches <= (Extract.NR_SHARED_PAGES / 2)) {
            throw new MappingNotFoundException();
        }

        return positionMaxMatches;
    } 
}
