package org.academiadecodigo.splicegirls36.simplewebserver;

public enum StatusCode {

    OK (200),
    NOT_FOUND (404);

    private int code;

    StatusCode(int code) {

        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        switch (this) {
            case OK:
                return "OK";
            default:
                return "NOT FOUND";
        }
    }
}
