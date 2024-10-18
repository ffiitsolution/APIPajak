package com.ffi.api.pajak.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Outlet {
    private String name;
    private String address;
    private String phone;
    private String date;
    private String requestRows;
    private int actualResult;

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String value) {
        this.name = value;
    }

    @JsonProperty("address")
    public String getAddress() {
        return address;
    }

    @JsonProperty("address")
    public void setAddress(String value) {
        this.address = value;
    }

    @JsonProperty("phone")
    public String getPhone() {
        return phone;
    }

    @JsonProperty("phone")
    public void setPhone(String value) {
        this.phone = value;
    }
    
    @JsonProperty("date")
    public String getDate() {
        return date;
    }

    @JsonProperty("date")
    public void setDate(String value) {
        this.date = value;
    }
    
    @JsonProperty("requestRows")
    public String getRequestRows() {
        return requestRows;
    }

    @JsonProperty("requestRows")
    public void setRequestRows(String value) {
        this.requestRows = value;
    }
    
    @JsonProperty("actualResult")
    public int getActualResult() {
        return actualResult;
    }

    @JsonProperty("actualResult")
    public void setActualResult(int value) {
        this.actualResult = value;
    }
}
