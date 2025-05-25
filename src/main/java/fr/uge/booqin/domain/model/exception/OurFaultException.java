package fr.uge.booqin.domain.model.exception;

import java.util.List;

public final class OurFaultException extends BooqInException {
    public OurFaultException(String message) {
        super(message);
    }
    public OurFaultException(String message, List<String> reasons) {
        super(message, reasons);
    }
    public OurFaultException(String message, Throwable cause) {
        super(message, cause);
    }
}
