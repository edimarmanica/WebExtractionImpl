/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.trinity.extract;

import br.edimarmanica.configuration.General;
import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Domain;
import br.edimarmanica.dataset.Site;
import gnu.regexp.REException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import tdg.tex.Node;
import tdg.tex.Text;

/**
 * Executa a expressão regular usando gnu.regexp.RE;
 *
 * @author edimar
 */
public class ExtractDirectInTree extends Extract {

    public ExtractDirectInTree(Site site) {
        super(site);
    }

    @Override
    protected void compileRegex(String regex) {
        //nada pois extrai direto da TrinaryTree
    }

    @Override
    protected void execute(File file, int offset) throws IOException {
        //nada pois extrai direto da TrinaryTree
    }

    @Override
    protected void execute(int offset) throws IOException, REException {

        if (General.DEBUG) {
            System.out.println("Starting extracting");
        }

        List<Node> leaves = getRoot().getLeaves();//obtendo os nodos folha da TrinaryTree
        List<Map<String, String>> rules = new ArrayList<>(); //List<Rule<Map<Page,Value>>>
        Set<String> pages = new HashSet<>(); //Set<Page>
        for (Node leaf : leaves) { //Para cada nodo folha
            Map<String, String> pageValues = new HashMap<>(); //Map<Page, Value>
            if (leaf.getSharedText() == null && leaf.size() > 0 && leaf.hasVariability()) { //se tem algum valor e não é sempre o mesmo
                for (Text text : leaf) { //para cada valor (suporta atributos multivalorados)
                    if (!pageValues.containsKey(text.getFile().getName())) { //o métrics trabalha com um valor de atributo por página e o trinity pegaria multiattributos. Então aqui vou pegar um só valor por atributo por página
                        pageValues.put(text.getFile().getName(), text.toString());
                        pages.add(text.getFile().getName());
                    }
                }
            }
            if (pageValues.size() > 0) { //se essa regra extraiu valor em pelo menos uma página
                rules.add(pageValues);
            }
        }

        //imprimindo
        for (String page : pages) {
            List<String> dataRecord = new ArrayList<>();
            dataRecord.add(page);
            for (Map<String, String> rule : rules) {
                if (rule.containsKey(page)) {
                    dataRecord.add(format(rule.get(page)));
                } else {
                    dataRecord.add("");
                }
            }
            printResults(dataRecord, offset);
        }

        if (General.DEBUG) {
            System.out.println("Ending extracting");
        }
    }

    public static void main(String[] args) {
        //configurar essa opção (-Xss40m) da VM para não dar stackoverflow 
        General.DEBUG = true;
        Extract.WINDOW_SIZE = 30;
        //Paths.PATH_TRINITY = Paths.PATH_TRINITY + "/ved_w" + (Extract.WINDOW_SIZE - Extract.NR_SHARED_PAGES);
        Paths.PATH_TRINITY = "/media/edimar/Dados/doutorado04/bases/ORION/driver/groundtruth/";

        Domain domain = br.edimarmanica.dataset.orion.Domain.DRIVER;
        for (Site site : domain.getSites()) {

           /* if (site != br.edimarmanica.dataset.orion.driver.Site.CHAMP) {
                continue;
            }*/
            System.out.println("Site: " + site);
            Extract run = new ExtractDirectInTree(site);
            try {
                run.execute();
            } catch (IOException | REException ex1) {
                Logger.getLogger(Extract.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }
}
