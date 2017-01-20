/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.trinity.extract;

import br.edimarmanica.trinity.extract.timeout.TimeoutRegexCharSequence;
import br.edimarmanica.trinity.util.FileUtils;
import gnu.regexp.REException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.StringEscapeUtils;
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
public class ExtractRafael {

    private static final boolean DEBUG = true;
    private static final int TIME_OUT_MILLIS = 60 * 1000; //em milisegundos

    private final Set<File> trainningPages;
    private final Set<File> testPages;
    private final int min;
    private final int max;
    private final String outputDir;

    private Tokeniser tokeniser;
    private Node root;
    private Pattern pattern;
    private boolean append;

    public ExtractRafael(Set<File> trainningPages, Set<File> testPages, int min, int max, String outputDir) {
        this.trainningPages = trainningPages;
        this.testPages = testPages;
        this.min = min;
        this.max = max;
        this.outputDir = outputDir;
    }

    private void train() throws IOException {
        if (DEBUG) {
            System.out.println("Starting training");
        }
        TokeniserConfig tokeniserConf;
        try {
            tokeniserConf = new TokeniserConfig(new File(System.getProperty("user.dir") + "/Tokeniser.cfg"));
            tokeniser = new Tokeniser(tokeniserConf);
        } catch (ParserConfigurationException | SAXException | IOException | REException ex) {
            Logger.getLogger(ExtractRafael.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (File page : trainningPages) {
            try {
                train(page);
            } catch (IOException ex) {
                Logger.getLogger(ExtractRafael.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        Statistic stat = new Statistic();

        stat.startCPUWatch();
        if (DEBUG) {
            System.out.println("Learning extraction rules...");
        }
        String regex = Learner.learn(root, min, max, tokeniser);
        stat.stopCPUWatch();
        if (DEBUG) {
            System.out.println("CPU Learning time: " + stat.getCPUInterval() / 1.0E10D);
        }

        compileRegex(regex);
        if (DEBUG) {
            System.out.println("Ending training");
        }
    }

    private void compileRegex(String regex) {
        pattern = Pattern.compile(regex);
    }

    private void train(File page) throws IOException {
        String sPage = UTF8FileUtil.readStrippedHTML(page.toURI());

        TreeMap<Integer, Token> tokensPages = tokeniser.tokenise(sPage).getTokensMap();
        tokensPages.remove(tokensPages.lastKey());
        Text textPage = new Text(page, tokensPages.values());

        root.add(textPage);
    }

    private void test() {
        if (DEBUG) {
            System.out.println("Starting extracting");
        }

        for (File page : testPages) {
            try {
                test(page);
            } catch (IOException ex) {
                Logger.getLogger(ExtractRafael.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (DEBUG) {
            System.out.println("Ending extracting");
        }
    }

    private void test(File file) throws IOException {

        List<String> dataRecord = new ArrayList<>();
        Matcher m = getMatcher(file);
        dataRecord.add(file.getName());

        if (m.matches()) {
            for (int i = 0; i != m.groupCount(); i++) {
                if (m.group(i) == null) {
                    dataRecord.add("");
                } else {
                    dataRecord.add(formatValue(m.group(i)));
                }
            }
        } else {
            for (int i = 0; i != m.groupCount(); i++) {
                dataRecord.add("not matched");
            }
        }
        printResults(dataRecord);
    }

    private Matcher getMatcher(File file) throws IOException {
        CharSequence charSequence = new TimeoutRegexCharSequence(UTF8FileUtil.readStrippedHTML(file.toURI()), TIME_OUT_MILLIS, file.getName());
        Matcher m = pattern.matcher(charSequence);
        return m;
    }

    private void execute() throws IOException {
        append = false; //vai apagar o arquivo de saída caso tenha sido gerado algum anteriormente
        root = new Node();
        train();
        test();
    }

    private void printResults(List<String> dataRecord) {
        File dir = new File(outputDir);
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
            Logger.getLogger(ExtractRafael.class.getName()).log(Level.SEVERE, null, ex);
        }
        append = true;
    }
    
     public static String formatValue(String value){
        return StringEscapeUtils.unescapeHtml(value)
                .replaceAll("\\s+", " ") //removing extra spaces
                .replaceAll("<[^>]*>", "") //removing tags
                .replaceAll("\\&[^;]*;", "");//removing html entities
    }

    public static void main(String[] args) {
        int min = 1; //parâmetro do Trinity (tamanho mínimo de um padrão) - Trinity usou 1
        int max = 100;// parâmetro do Trinity (tamanho máximo de um padrão) - Trinity usou 0.05 X m, onde m denota o tamanho em tokens da menor página dentro do conjunto de entrada
        String outputDir = "/home/edimar/Área de Trabalho/teste"; //diretório onde salvar a saída. A saída é um CSV, onde a primeira coluna é a URL da página e as demais são os valores extraídos. Inúmeras colunas extraem lixo
        Set<File> trainningPages = new HashSet<>(); //páginas utilizadas para aprender as regras de extração -- Nós testamos com as páginas desse conjunto https://swde.codeplex.com/

        /**
         * * Estou usando 10 páginas do site auto-aol da base
         * https://swde.codeplex.com/ para demonstração **
         */
        File dir = new File("/media/edimar/Dados/doutorado04/bases/SWDE/auto/auto-aol/");
        int i = 0;
        for (File page : dir.listFiles()) {

            trainningPages.add(page);
            i++;
            if (i >= 10) {
                break;
            }
        }

        Set<File> testPages = new HashSet<>(); //páginas utilizadas para avaliação
        /**
         * * Estou usando as mesmas páginas de treinamento para demonstração ***
         */
        testPages = trainningPages;

        ExtractRafael extractor = new ExtractRafael(trainningPages, testPages, min, max, outputDir);
        try {
            extractor.execute();
        } catch (IOException ex) {
            Logger.getLogger(ExtractRafael.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
