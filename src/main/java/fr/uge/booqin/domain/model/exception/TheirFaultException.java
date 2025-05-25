package fr.uge.booqin.domain.model.exception;

import java.util.List;

public final class TheirFaultException extends BooqInException {

    public TheirFaultException(String message) {
        super(message);
    }
    public TheirFaultException(String message, List<String> reasons) {
        super(message, reasons);
    }
    public TheirFaultException(String message, Throwable cause) {
        super(message, cause);
    }
}
