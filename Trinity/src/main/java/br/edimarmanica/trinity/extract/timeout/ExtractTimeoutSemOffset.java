package br.edimarmanica.trinity.extract.timeout;

import br.edimarmanica.configuration.General;
import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Domain;
import br.edimarmanica.dataset.Site;
import br.edimarmanica.trinity.extract.Extract;
import br.edimarmanica.trinity.util.FileUtils;
import gnu.regexp.REException;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.xml.sax.SAXException;
import tdg.cedar.tokeniser.Token;
import tdg.cedar.tokeniser.Tokeniser;
import tdg.cedar.tokeniser.TokeniserConfig;
import tdg.cedar.utilities.Statistic;
import tdg.cedar.utilities.UTF8FileUtil;
import tdg.tex.Learner;
import tdg.tex.Node;
import tdg.tex.Text;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Constrói a árvore com as sizeTrainning primeiras páginas e gera a expressão
 * regular. Depois roda para as todas as páginas só a extração
 *
 * @author edimar
 */
public class ExtractTimeoutSemOffset {

    private final Site site;
    private final int sizeTraining;

    private Tokeniser tokeniser;
    private Node root;
    private boolean append = false;
    private Pattern pattern;
    public static final int VALUE_MAX_LENGHT = 150; //um valor com mais caracteres é descartado
    private static final int TIME_OUT_MILLIS = 60 * 1000; //em milisegundos

    public ExtractTimeoutSemOffset(Site site, int sizeTraining) {
        this.site = site;
        this.sizeTraining = sizeTraining;
    }

    /**
     * constroi a árvore com as páginas [start-end], where start=0 e end =
     * sizeTraing-1
     *
     * @throws IOException
     */
    protected void train() throws IOException {
        if (General.DEBUG) {
            System.out.println("Starting training");
        }
        root = new Node();
        TokeniserConfig tokeniserConf;
        try {
            tokeniserConf = new TokeniserConfig(new File(System.getProperty("user.dir") + "/Tokeniser.cfg"));
            tokeniser = new Tokeniser(tokeniserConf);
        } catch (ParserConfigurationException | SAXException | IOException | REException ex) {
            Logger.getLogger(Extract.class.getName()).log(Level.SEVERE, null, ex);
        }

        File dir = new File(Paths.PATH_BASE + site.getPath());
        int i = 0;
        for (File fPage : dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".html") || name.endsWith(".htm");
            }
        })) {
            
            if (i >= 0 && i < sizeTraining) {
                if (General.DEBUG) {
                    System.out.println("\t Page (" + i + "): " + fPage.getName());
                }
                train(fPage);
            }
            i++;
        }

        Statistic stat = new Statistic();
        if (General.DEBUG) {
            System.out.println("Learning extraction rules...");
        }
        stat.startCPUWatch();
        String regex = Learner.learn(root, 1, 100, tokeniser);
        //System.out.println(regex);
        stat.stopCPUWatch();
        if (General.DEBUG) {
            System.out.println("CPU Learning time: " + stat.getCPUInterval() / 1.0E10D);
        }

        compileRegex(regex);
        if (General.DEBUG) {
            System.out.println("Ending training");
        }
    }

    protected void compileRegex(String regex) {
        pattern = Pattern.compile(regex);
    }

    private void train(File fPage) throws IOException {
        String sPage = UTF8FileUtil.readStrippedHTML(fPage.toURI());

        TreeMap<Integer, Token> tokensPages = tokeniser.tokenise(sPage).getTokensMap();
        tokensPages.remove(tokensPages.lastKey());
        Text textPage = new Text(fPage, tokensPages.values());

        root.add(textPage);
    }

    public Matcher getMatcher(File file) throws IOException {
        CharSequence charSequence = new TimeoutRegexCharSequence(UTF8FileUtil.readStrippedHTML(file.toURI()), TIME_OUT_MILLIS, file.getName());
        Matcher m = pattern.matcher(charSequence);
        return m;
    }

    protected void execute(File file) throws IOException {

        List<String> dataRecord = new ArrayList<>();
        Matcher m = getMatcher(file);
        dataRecord.add(file.getName());
        try {
            if (m.matches()) {
                for (int i = 0; i != m.groupCount(); i++) {
                    if (m.group(i) == null) {
                        dataRecord.add("");
                    } else {
                        dataRecord.add(format(m.group(i)));
                    }
                }
            } else {
                for (int i = 0; i != m.groupCount(); i++) {
                    dataRecord.add("not matched");
                }
            }
        } catch (StackOverflowError ex) {
            System.out.println("Ignoring: " + file.getPath());
            return;
        }
        printResults(dataRecord);
    }

    protected String format(String value) {
        return value.replaceAll("\n", " ");
    }

    protected void printResults(List<String> dataRecord) {
        /**
         * ********************** results ******************
         */
        File dir = new File(Paths.PATH_TRINITY + site.getPath() + "/groups/");
        dir.mkdirs();

        if (!append) {
            FileUtils.deleteDir(dir);
            dir.mkdirs();
        }

        File file = new File(dir.getAbsolutePath() + "/rules.csv");
        CSVFormat format = CSVFormat.EXCEL;

        try (Writer out = new FileWriter(file, append)) {
            try (CSVPrinter csvFilePrinter = new CSVPrinter(out, format)) {
                csvFilePrinter.printRecord(dataRecord);
            }
        } catch (IOException ex) {
            Logger.getLogger(Extract.class.getName()).log(Level.SEVERE, null, ex);
        }
        append = true;
    }

    public void execute() throws IOException, REException {
        File dir = new File(Paths.PATH_BASE + site.getPath());
        train();

        int i = 0;
        for (File file : dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".html") || name.endsWith(".htm");
            }
        })) {
            if (General.DEBUG) {
                System.out.println("\t Page (" + i + "): " + file.getName());
            }
            execute(file);
            append = true;
            i++;
        }
    }

    public static void main(String[] args) {
        //configurar essa opção (-Xss40m) da VM para não dar stackoverflow 
        General.DEBUG = true;
        int sizeTraining = 5;
        Paths.PATH_TRINITY = Paths.PATH_TRINITY + "/vre_w" + sizeTraining + "/";

        Domain domain = br.edimarmanica.dataset.weir.Domain.BOOK;
        for (Site site : domain.getSites()) {

            if (site != br.edimarmanica.dataset.weir.book.Site.GOODREADS) {
                continue;
            }

            System.out.println("Site: " + site);
            ExtractTimeoutSemOffset run = new ExtractTimeoutSemOffset(site, sizeTraining);
            try {
                run.execute();
            } catch (IOException | REException ex1) {
                Logger.getLogger(ExtractTimeoutSemOffset.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }

}
