package se.skl.tp.vp.wsdl;

import java.util.Objects;

public class WsdlConfig {

    private String tjanstekontrakt;
    private String wsdlfilepath;
    private String wsdlurl;

    public String getTjanstekontrakt() {
        return tjanstekontrakt;
    }

    public void setTjanstekontrakt(String tjanstekontrakt) {
        this.tjanstekontrakt = tjanstekontrakt;
    }

    public String getWsdlfilepath() {
        return wsdlfilepath;
    }

    public void setWsdlfilepath(String wsdlfilepath) {
        this.wsdlfilepath = wsdlfilepath;
    }

    public String getWsdlurl() {
        return wsdlurl;
    }

    public void setWsdlurl(String wsdlurl) {
        this.wsdlurl = wsdlurl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WsdlConfig)) {
            return false;
        }
        WsdlConfig that = (WsdlConfig) o;
        return Objects.equals(tjanstekontrakt, that.tjanstekontrakt) &&
            Objects.equals(wsdlfilepath, that.wsdlfilepath) &&
            Objects.equals(wsdlurl, that.wsdlurl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tjanstekontrakt, wsdlfilepath, wsdlurl);
    }
}
