package com.ss.testserial.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Administrator on 2018/1/4 0004.
 */

public class QueryInfoBean {


    /**
     * sign : 946cc7fdd6c159eb67cbc4b5020a7681
     * timestamp : 1515051176
     * data : {"success":true,"list":["12345","22222","3333"]}
     * class : DevOp
     * method : packageInfo
     */

    private String sign;
    private int timestamp;
    private DataBean data;
    @SerializedName("class")
    private String classX;
    private String method;

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public String getClassX() {
        return classX;
    }

    public void setClassX(String classX) {
        this.classX = classX;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public static class DataBean {
        /**
         * success : true
         * list : ["12345","22222","3333"]
         */

        private boolean success;
        private List<String> list;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public List<String> getList() {
            return list;
        }

        public void setList(List<String> list) {
            this.list = list;
        }
    }
}
