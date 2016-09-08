/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.creatingdataset;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author edimar
 */
public class Action {

    private static final String[] SITES = {
        "champ.csv", "f_1.csv", "f_3.csv", "f_truck.csv", "gp_2.csv", "gp_update.csv",
        "indycar.csv", "motor_gp.csv", "nascar.csv", "stock_car.csv", "wrc.csv"
    };
    
    private static final String pathGabarito = "";
    private static final String pathHtmls = "";
    private static final String pathOutput = "";
    
    public static void execute(){
        for(String site: SITES){
            Set<String> pages = Utils.readGroundTruth(pathGabarito, site);
            for(String page: pages){
                String localName = "";
                try {
                    localName = Utils.getLocalName(page);
                    
                } catch (SQLException | ClassNotFoundException ex) {
                    Logger.getLogger(Action.class.getName()).log(Level.SEVERE, "Erro na página: "+page, ex);
                }
                
                try {
                    Utils.copy(pathHtmls+"/"+localName, pathOutput+"/"+localName);
                } catch (IOException ex) {
                    Logger.getLogger(Action.class.getName()).log(Level.SEVERE, "Erro ao copiar: "+localName, ex);
                }
            }
        }
    }
    
    public static void main(String[] args) {
        execute();
    }
}
