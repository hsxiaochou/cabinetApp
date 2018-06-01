package com.ss.testserial.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Administrator on 2018/5/31 0031.
 */

public class GetImageBean {

    /**
     * sign : 64c1e73c539c0ad2f5227d7ac88c3b97
     * timestamp : 1527748073
     * data : {"success":true,"list":{"big":["/Public/adPic/big1.jpg","/Public/adPic/big2.jpg"],"small":["/Public/adPic/small1.jpg","/Public/adPic/small2.jpg","/Public/adPic/small3.jpg","/Public/adPic/small4.jpg"]}}
     * class : DevOp
     * method : getAdPic
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
         * list : {"big":["/Public/adPic/big1.jpg","/Public/adPic/big2.jpg"],"small":["/Public/adPic/small1.jpg","/Public/adPic/small2.jpg","/Public/adPic/small3.jpg","/Public/adPic/small4.jpg"]}
         */

        private boolean success;
        private ListBean list;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public ListBean getList() {
            return list;
        }

        public void setList(ListBean list) {
            this.list = list;
        }

        public static class ListBean {
            private List<String> big;
            private List<String> small;

            public List<String> getBig() {
                return big;
            }

            public void setBig(List<String> big) {
                this.big = big;
            }

            public List<String> getSmall() {
                return small;
            }

            public void setSmall(List<String> small) {
                this.small = small;
            }
        }
    }
}
