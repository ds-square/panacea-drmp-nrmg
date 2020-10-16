package org.panacea.drmp.nrmg.exception;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NRMGException extends RuntimeException {

    protected Throwable throwable;

    public NRMGException(String message) {
        super(message);
    }

    public NRMGException(String message, Throwable throwable) {
        super(message);
        this.throwable = throwable;
        log.error("[NRMG]: ", message);
    }

    public Throwable getCause() {
        return throwable;
    }
}
