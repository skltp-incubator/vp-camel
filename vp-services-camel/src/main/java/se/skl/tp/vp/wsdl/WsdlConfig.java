package se.skl.tp.vp.wsdl;

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
}
