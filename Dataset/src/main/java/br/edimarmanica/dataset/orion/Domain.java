/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.dataset.orion;

import br.edimarmanica.dataset.Attribute;
import br.edimarmanica.dataset.Dataset;
import br.edimarmanica.dataset.Site;
import java.io.File;

/**
 *
 * @author edimar
 */
public enum Domain implements br.edimarmanica.dataset.Domain {

    DRIVER("driver");
    private final String folderName;

    private Domain(String folderName) {
        this.folderName = folderName;
    }

    @Override
    public String getFolderName() {
        return folderName;
    }

    @Override
    public Dataset getDataset() {
        return Dataset.ORION;
    }

    @Override
    public Attribute getAttributebyMyID(String attributeID) {
        for (Attribute attr : getAttributes()) {
            if (attr.getAttributeID().equals(attributeID)) {
                return attr;
            }
        }
        return null;
    }

    @Override
    public Attribute getAttributeIDbyDataset(String attributeIDbyDataset) {
        for (Attribute attr : getAttributes()) {
            if (attr.getAttributeIDbyDataset().equals(attributeIDbyDataset)) {
                return attr;
            }
        }
        return null;
    }

    @Override
    public String getPath() {
        return getDataset().getFolderName() + File.separator + getFolderName();
    }

    @Override
    public Attribute[] getAttributes() {
        switch (this) {
            case DRIVER:
                return br.edimarmanica.dataset.orion.driver.Attribute.values();
            default:
                throw new UnsupportedOperationException("Domain not configurated yet!");
        }

    }

    @Override
    public Site[] getSites() {
        switch (this) {
            case DRIVER:
                return br.edimarmanica.dataset.orion.driver.Site.values();
            default:
                throw new UnsupportedOperationException("Domain not configurated yet!");
        }
    }

    @Override
    public Site getSiteOf(String site) {
        switch (this) {
            case DRIVER:
                return br.edimarmanica.dataset.orion.driver.Site.valueOf(site);
            default:
                throw new UnsupportedOperationException("Domain not configurated yet!");
        }
    }

}
