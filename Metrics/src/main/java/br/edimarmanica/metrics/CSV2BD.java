/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.metrics;

import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Dataset;
import br.edimarmanica.dataset.Domain;
import br.edimarmanica.dataset.Site;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author edimar
 */
public class CSV2BD {

    private final Site site;
    private final String method;
    private final String path;

    public CSV2BD(Site site, String method, String path) {
        this.site = site;
        this.method = method;
        this.path = path;
    }

    /**
     * Excluir os resultados do método para o site no banco de dados
     *
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    private void deleteSiteResults() throws SQLException, ClassNotFoundException {
        String sql = "DELETE FROM " + method + " WHERE base=? and dominio=? and site = ? ";
        PreparedStatement ps = Conexao.getConexao().prepareStatement(sql);
        ps.setString(1, site.getDomain().getDataset().toString());
        ps.setString(2, site.getDomain().toString());
        ps.setString(3, site.toString());
        ps.execute();
    }

    private void insertSiteResults(String atributo, String regra, String rotulo, int relevantes, int recuperados, int relevantes_recuperados, double revocacao, double precisao, String data) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO " + method + " (base, dominio, site, atributo, regra, rotulo, relevantes, recuperados, relevantes_recuperados, revocacao, precisao, data_avaliacao) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement ps = Conexao.getConexao().prepareStatement(sql);
        int i = 1;
        ps.setString(i++, site.getDomain().getDataset().toString());
        ps.setString(i++, site.getDomain().toString());
        ps.setString(i++, site.toString());
        ps.setString(i++, atributo);
        ps.setString(i++, regra);
        ps.setString(i++, rotulo);
        ps.setInt(i++, relevantes);
        ps.setInt(i++, recuperados);
        ps.setInt(i++, relevantes_recuperados);
        ps.setDouble(i++, revocacao);
        ps.setDouble(i++, precisao);
        ps.setString(i++, data);
        ps.execute();
    }

    private void execute() throws SQLException, ClassNotFoundException {
        Conexao.getConexao().setAutoCommit(false);
        deleteSiteResults();

        try (Reader in = new FileReader(path + "/" + site.getPath() + "/result.csv")) {
            try (CSVParser parser = new CSVParser(in, CSVFormat.EXCEL.withHeader())) {

                for (CSVRecord record : parser) {
                    insertSiteResults(record.get("ATTRIBUTE"), record.get("RULE"), record.get("LABEL"), Integer.parseInt(record.get("RELEVANTS")), Integer.parseInt(record.get("RETRIEVED")), Integer.parseInt(record.get("RETRIEVED RELEVANTS")), Double.parseDouble(record.get("RECALL")), Double.parseDouble(record.get("PRECISION")), record.get("DATE"));
                }
            }
            Conexao.getConexao().commit();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MergeDomainResults.class.getName()).log(Level.SEVERE, null, ex);
            Conexao.getConexao().rollback();
        } catch (IOException ex) {
            Logger.getLogger(MergeDomainResults.class.getName()).log(Level.SEVERE, null, ex);
            Conexao.getConexao().rollback();
        }
        
    }

    public static void main(String[] args) {
        Map<String, String> methodPath = new HashMap<>();
        methodPath.put("identificacaomanual", Paths.PATH_TEMPLATE_VARIATION_MANUAL);
        methodPath.put("weir", Paths.PATH_INTRASITE);
        methodPath.put("trinity", Paths.PATH_TRINITY+"/ved_w1/");
        methodPath.put("orion_x", Paths.PATH_TEMPLATE_VARIATION_AUTO+"/limiar_x");
        methodPath.put("orion_0", Paths.PATH_TEMPLATE_VARIATION_AUTO+"/limiar_0.0");
        methodPath.put("orion_1", Paths.PATH_TEMPLATE_VARIATION_AUTO+"/limiar_1.0");
        methodPath.put("orion_2", Paths.PATH_TEMPLATE_VARIATION_AUTO+"/limiar_2.0");
        methodPath.put("orion_3", Paths.PATH_TEMPLATE_VARIATION_AUTO+"/limiar_3.0");
        methodPath.put("orion_4", Paths.PATH_TEMPLATE_VARIATION_AUTO+"/limiar_4.0");
        methodPath.put("orion_5", Paths.PATH_TEMPLATE_VARIATION_AUTO+"/limiar_5.0");
        
        String method = "orion_0";

        for (Dataset dataset : Dataset.values()) {
            for (Domain domain : dataset.getDomains()) {
                for (Site site : domain.getSites()) {
                    CSV2BD ex = new CSV2BD(site, method, methodPath.get(method));
                    try {
                        ex.execute();
                    } catch (SQLException | ClassNotFoundException ex1) {
                        Logger.getLogger(CSV2BD.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
            }
        }

    }
}
