/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.trinity.check;

import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Site;
import gnu.regexp.RE;
import gnu.regexp.REException;
import gnu.regexp.REMatch;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import tdg.cedar.tokeniser.Token;
import tdg.cedar.tokeniser.Tokeniser;
import tdg.cedar.tokeniser.TokeniserConfig;
import tdg.cedar.utilities.Statistic;
import tdg.cedar.utilities.UTF8FileUtil;
import tdg.tex.Learner;
import tdg.tex.Node;
import tdg.tex.Text;

/**
 *
 * @author edimar
 */
public class CheckTree {

    private final Site site;
    private final String[] pagesID;
    private String[] pagesContent;

    public CheckTree(Site site, String[] pagesID) {
        this.site = site;
        this.pagesID = pagesID;
    }

    public void execute() throws REException, IOException, ParserConfigurationException, SAXException {
        pagesContent = new String[pagesID.length];

        for (int i = 0; i < pagesID.length; i++) {
            pagesContent[i] = UTF8FileUtil.readStrippedHTML(new File(Paths.PATH_BASE + "/" + site.getPath() + "/" + pagesID[i]).toURI());
        }

        TokeniserConfig tokeniserConf = new TokeniserConfig(new File(System.getProperty("user.dir") + "/Tokeniser.cfg"));
        Tokeniser tokeniser = new Tokeniser(tokeniserConf);

        Node root = new Node();
        for (int i = 0; i < pagesID.length; i++) {
            TreeMap<Integer, Token> tokens = tokeniser.tokenise(pagesContent[i]).getTokensMap();
            tokens.remove(tokens.lastKey());
            Text text = new Text(new File(Paths.PATH_BASE + "/" + site.getPath() + "/" + pagesID[i]), tokens.values());
            root.add(text);
        }

        Statistic stat = new Statistic();
        System.out.println("Learning extraction rules...");
        stat.startCPUWatch();
        String regex = Learner.learn(root, 1, 100, tokeniser);
        stat.stopCPUWatch();
        System.out.println("CPU Learning time: " + stat.getCPUInterval() / 1.0E10D);

        root.drawTree("Result");

    }

    public static void main(String[] args) {
        Site site = br.edimarmanica.dataset.weir.soccer.Site.FOOTBALL;
        String[] pages = {"ricardo-oliveira.html", "dejan-stankovic.html", "luigi-di-biagio.html", "roberto-carlos.html", "jose-chamot.html", "andreas-herzog.html"};
      //  String[] pages = {"ricardo-oliveira.html", "andreas-herzog.html"};

        CheckTree cr = new CheckTree(site, pages);
        try {
            cr.execute();
        } catch (REException | IOException | ParserConfigurationException | SAXException ex) {
            Logger.getLogger(CheckTree.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
