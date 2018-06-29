package se.skl.tp.vp.vagval.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum;
import se.skl.tp.vp.exceptions.VpSemanticException;

public class ErrorUtil {
    private static final Logger LOGGER = LogManager.getLogger(ErrorUtil.class);

    private ErrorUtil() {
        // Util class
    }

    public static void raiseError(boolean test, VpSemanticErrorCodeEnum codeenum) {
        if(test) {
            raiseError(codeenum, null);
        }
    }

    public static void raiseError(boolean test, VpSemanticErrorCodeEnum codeenum, String suffix) {
        if(test) {
            raiseError(codeenum, suffix);
        }
    }

    public static void raiseError(VpSemanticErrorCodeEnum codeEnum, String suffix) {
        // TODO fix errormessage
        String errmsg = String.format("Raising VpSemanticException code: %s. %s", codeEnum.getCode(), suffix);
//        String errmsg = MessageProperties.getInstance().get(codeEnum, suffix);
        LOGGER.error(errmsg);
        throw new VpSemanticException(errmsg, codeEnum);
    }
}
