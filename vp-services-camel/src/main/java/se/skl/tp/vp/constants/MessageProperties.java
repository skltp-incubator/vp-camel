package se.skl.tp.vp.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum;

@Configuration
public class MessageProperties {

    @Value("${VP001}")
    public String VP001;

    @Value("${VP002}")
    public String VP002;

    @Value("${VP003}")
    public String VP003;

    @Value("${VP004}")
    public String VP004;

    @Value("${VP005}")
    public String VP005;

    @Value("${VP006}")
    public String VP006;

    @Value("${VP007}")
    public String VP007;

    @Value("${VP008}")
    public String VP008;

    @Value("${VP009}")
    public String VP009;

    @Value("${VP010}")
    public String VP010;

    @Value("${VP011}")
    public String VP011;

    @Value("${VP012}")
    public String VP012;

    //TODO: Detta bör designas om så inte denna metod behövs.
    public String getValueOnErrorCode(VpSemanticErrorCodeEnum vpSemanticErrorCodeEnum) {
        switch (vpSemanticErrorCodeEnum.getCode()) {
            case "VP001" : return VP001;
            case "VP002" : return VP002;
            case "VP003" : return VP003;
            case "VP004" : return VP004;
            case "VP005" : return VP005;
            case "VP006" : return VP006;
            case "VP007" : return VP007;
            case "VP008" : return VP008;
            case "VP009" : return VP009;
            case "VP010" : return VP010;
            case "VP011" : return VP011;
            case "VP012" : return VP012;
            default: return "";
        }
    }
}
