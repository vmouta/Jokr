package com.jokrapp.android.data;

/**
 * Created by pat on 10/28/2015.
 */
public class RestResponse {

    private int code;
    private String body;

    public RestResponse() {

    }

    public RestResponse(int code, String body) {
        this.code = code;
        this.body = body;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
