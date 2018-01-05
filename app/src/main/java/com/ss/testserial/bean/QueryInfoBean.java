package com.ss.testserial.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Administrator on 2018/1/4 0004.
 */

public class QueryInfoBean {


    /**
     * sign : 1e15f6ac3696a54dd2c963a2f30aba9f
     * timestamp : 1515135018
     * data : {"success":true,"list":[{"id":306554,"phone":"15102868137","express_num":"112233"}]}
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
         * list : [{"id":306554,"phone":"15102868137","express_num":"112233"}]
         */

        private boolean success;
        private List<ListBean> list;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public List<ListBean> getList() {
            return list;
        }

        public void setList(List<ListBean> list) {
            this.list = list;
        }

        public static class ListBean {
            /**
             * id : 306554
             * phone : 15102868137
             * express_num : 112233
             */

            private int id;
            private String phone;
            private String express_num;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getPhone() {
                return phone;
            }

            public void setPhone(String phone) {
                this.phone = phone;
            }

            public String getExpress_num() {
                return express_num;
            }

            public void setExpress_num(String express_num) {
                this.express_num = express_num;
            }
        }
    }
}
