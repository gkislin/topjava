package ru.javawebinar.topjava.util.exception;

import java.util.Arrays;

public class ApplicationException extends RuntimeException {

    private final ErrorType type;
    private final String msgCode;
    private final String[] args;

    public ApplicationException(String msgCode) {
        this(ErrorType.APP_ERROR, msgCode);
    }

    public ApplicationException(ErrorType type, String msgCode, String... args) {
        super(String.format("type=%s, msgCode=%s, args=%s", type, msgCode, Arrays.toString(args)));
        this.type = type;
        this.msgCode = msgCode;
        this.args = args;
    }

    public ErrorType getType() {
        return type;
    }

    public String getMsgCode() {
        return msgCode;
    }

    public String[] getArgs() {
        return args;
    }
}
