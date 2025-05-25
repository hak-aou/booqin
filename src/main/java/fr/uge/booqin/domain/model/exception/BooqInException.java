package fr.uge.booqin.domain.model.exception;

import java.util.List;

public sealed abstract class BooqInException extends RuntimeException permits TheirFaultException, OurFaultException {

    private final List<String> reasons;

    public BooqInException(String message, List<String> reasons) {
        super(message);
        this.reasons = List.copyOf(reasons);
    }

    public BooqInException(String message) {
        super(message);
        this.reasons = List.of();
    }

    public BooqInException(String message, Throwable cause) {
        super(message, cause);
        this.reasons = List.of();
    }

    public List<String> reasons() {
        return reasons;
    }

}
