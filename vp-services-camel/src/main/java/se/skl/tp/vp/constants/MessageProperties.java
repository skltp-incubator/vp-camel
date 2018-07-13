package se.skl.tp.vp.constants;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum;

@Configuration
@PropertySource("classpath:vp-messages.properties")
@ConfigurationProperties()
public class MessageProperties {

    private String vp001;
    private String vp002;
    private String vp003;
    private String vp004;
    private String vp005;
    private String vp006;
    private String vp007;
    private String vp008;
    private String vp009;
    private String vp010;
    private String vp011;
    private String vp012;

    //TODO: Detta bör designas om så inte denna metod behövs.
    public String getValueOnErrorCode(VpSemanticErrorCodeEnum vpSemanticErrorCodeEnum) {
        switch (vpSemanticErrorCodeEnum.getCode()) {
            case "VP001" : return vp001;
            case "VP002" : return vp002;
            case "VP003" : return vp003;
            case "VP004" : return vp004;
            case "VP005" : return vp005;
            case "VP006" : return vp006;
            case "VP007" : return vp007;
            case "VP008" : return vp008;
            case "VP009" : return vp009;
            case "VP010" : return vp010;
            case "VP011" : return vp011;
            case "VP012" : return vp012;
            default: return "";
        }
    }

    public String getVp001() {
        return vp001;
    }

    public void setVp001(String vp001) {
        this.vp001 = vp001;
    }

    public String getVp002() {
        return vp002;
    }

    public void setVp002(String vp002) {
        this.vp002 = vp002;
    }

    public String getVp003() {
        return vp003;
    }

    public void setVp003(String vp003) {
        this.vp003 = vp003;
    }

    public String getVp004() {
        return vp004;
    }

    public void setVp004(String vp004) {
        this.vp004 = vp004;
    }

    public String getVp005() {
        return vp005;
    }

    public void setVp005(String vp005) {
        this.vp005 = vp005;
    }

    public String getVp006() {
        return vp006;
    }

    public void setVp006(String vp006) {
        this.vp006 = vp006;
    }

    public String getVp007() {
        return vp007;
    }

    public void setVp007(String vp007) {
        this.vp007 = vp007;
    }

    public String getVp008() {
        return vp008;
    }

    public void setVp008(String vp008) {
        this.vp008 = vp008;
    }

    public String getVp009() {
        return vp009;
    }

    public void setVp009(String vp009) {
        this.vp009 = vp009;
    }

    public String getVp010() {
        return vp010;
    }

    public void setVp010(String vp010) {
        this.vp010 = vp010;
    }

    public String getVp011() {
        return vp011;
    }

    public void setVp011(String vp011) {
        this.vp011 = vp011;
    }

    public String getVp012() {
        return vp012;
    }

    public void setVp012(String vp012) {
        this.vp012 = vp012;
    }
}
