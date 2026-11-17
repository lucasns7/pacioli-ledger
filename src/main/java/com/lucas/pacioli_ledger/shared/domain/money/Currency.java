package com.lucas.pacioli_ledger.shared.domain.money;

public enum Currency {

    BRL("BRL", 2);

    private final String code;
    private final int precision;

    Currency(String code, int precision){
        this.code = code;
        this.precision = precision;
    }

    public String getCode() {
        return code;
    }

    public int getPrecision() {
        return precision;
    }
}
