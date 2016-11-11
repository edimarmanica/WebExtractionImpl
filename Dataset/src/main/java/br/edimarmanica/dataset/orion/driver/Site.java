/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.dataset.orion.driver;

import br.edimarmanica.dataset.Attribute;
import br.edimarmanica.dataset.orion.Domain;
import java.io.File;

/**
 *
 * @author edimar
 */
public enum Site implements br.edimarmanica.dataset.Site {

    CHAMP("champ"), F1("f_1"), F3("f_3"), FTRUCK("f_truck"), GP2("gp_2"), GPUPDATE("gp_update"), INDYCAR("indycar"), MOTOR_GP("motor_gp"), NASCAR("nascar"), STOCKCAR("stock_car"), WRC("wrc"),
    FORMULAE("formula_e"), SKY_SPORTS("sky_sports"), EURO_SPORTS("euro_sports"), MOTOR_SPORTS("motor_sports");
    private String folderName;

    private Site(String folderName) {
        this.folderName = folderName;
    }

    @Override
    public String getFolderName() {
        return folderName;
    }

    @Override
    public Domain getDomain() {
        return Domain.DRIVER;
    }

    @Override
    public String getPath() {
        return getDomain().getPath() + File.separator + getFolderName();
    }

    @Override
    public String getGroundTruthPath() {
        return getDomain().getDataset().getFolderName() + File.separator + "groundtruth/" + getDomain().getFolderName() + File.separator + getFolderName() + ".csv";
    }

    @Override
    public String getEntityPath() {
        return getDomain().getDataset().getFolderName() + File.separator + "entity/" + getDomain().getFolderName() + File.separator + getFolderName() + ".csv";
    }

    @Override
    public String getGroundTruthPath(Attribute attr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
