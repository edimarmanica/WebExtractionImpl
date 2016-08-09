/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.trinity.intrasitemapping.auto;

import br.edimarmanica.trinity.extract.Extract;
import java.util.List;
import java.util.Map;

/**
 *
 * @author edimar
 */
public class Mapping {

    private final Map<String, String> groupOffsetX;
    private final List<Map<String, String>> offsetY;

    /**
     *
     * @param groupOffsetX a group of the offsetX. Ex: Map<URL, ExtractedValue>
     * @param offsetY all the groups of an offset x. Ex:
     * List<Group<URL, ExtractedValue>>
     */
    public Mapping(Map<String, String> groupOffsetX, List<Map<String, String>> offsetY) {
        this.groupOffsetX = groupOffsetX;
        this.offsetY = offsetY;
    }

    /**
     *
     * @param groupOffsetX
     * @param groupOffsetY
     * @return the number of pages that the two groups extract the same value
     */
    private int nrMatches(Map<String, String> groupOffsetX, Map<String, String> groupOffsetY) {
        int nrMatches = 0;

        for (String page : groupOffsetX.keySet()) {
            if (groupOffsetX.get(page).equals(groupOffsetY.get(page))) {
                nrMatches++;
            }
        }
        return nrMatches;
    }

    /**
     * @return o grupo do offsetY que deve ser mapeado com o grupo do offsetX
     * @throws
     * br.edimarmanica.trinity.intrasitemapping.auto.MappingNotFoundException
     */
    public int mapping() throws MappingNotFoundException {
        int maxNrMatches = 0;
        int positionMaxMatches = -1;

        for (int nrGroup = 0; nrGroup < offsetY.size(); nrGroup++) {
            int nrMatches = nrMatches(groupOffsetX, offsetY.get(nrGroup));

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
