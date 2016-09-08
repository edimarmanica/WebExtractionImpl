/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.creatingdataset;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * Le os gabaritos do descoberta de páginas-entidade
 *
 * @author edimar
 */
public class Utils {

    public static Set<String> readGroundTruth(String path, String site) {
        Set<String> pages = new HashSet<>();

        try (Reader in = new FileReader(path + "/" + site + ".csv")) {
            try (CSVParser parser = new CSVParser(in, CSVFormat.MYSQL)) {
                for (CSVRecord record : parser) {
                    pages.add(record.get(0));
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return pages;
    }

    public static String getLocalName(String url) throws SQLException, ClassNotFoundException {
        String sql = "SELECT local_name FROM page where url=?";
        PreparedStatement ps = Conexao.getConexao().prepareStatement(sql);
        ps.setString(1, url);
        ResultSet rs = ps.executeQuery();
        //rs.next();
        return rs.getString(1);
    }

    public static void copy(String from, String to) throws IOException {
        Path FROM = Paths.get(from);

        File toDir = new File(to);
        toDir.mkdirs();

        Path TO = Paths.get(toDir.getAbsolutePath());
        //overwrite existing file, if exists
        CopyOption[] options = new CopyOption[]{
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.COPY_ATTRIBUTES
        };
        Files.copy(FROM, TO, options);
    }
}
