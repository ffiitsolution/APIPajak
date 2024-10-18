package com.ffi.api.pajak.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;


public class ResponseMessage {
    
    private Boolean success;
    private String message;
    private Outlet outlet;
    private List item;

    @JsonProperty("success")
    public Boolean isSuccess() { return success; }
    @JsonProperty("success")
    public void setSuccess(Boolean value) { this.success = value; }

    @JsonProperty("message")
    public String getMessage() { return message; }
    @JsonProperty("message")
    public void setMessage(String value) { this.message = value; }

    @JsonProperty("outlet")
    public Outlet getOutlet() { return outlet; }
    @JsonProperty("outlet")
    public void setOutlet(Outlet value) { this.outlet = value; }
    
    @JsonProperty("item")
    public List getItem() { return item; }
    @JsonProperty("item")
    public void setItem(List value) { this.item = value; }
}

