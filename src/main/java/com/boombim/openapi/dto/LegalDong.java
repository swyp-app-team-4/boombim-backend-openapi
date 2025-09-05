package com.boombim.openapi.dto;

public record LegalDong(
    String gu,
    String dong,
    String code
) {

    public String display() {
        return gu + " " + dong;
    }

}
