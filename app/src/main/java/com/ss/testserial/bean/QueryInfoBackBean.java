package com.ss.testserial.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Administrator on 2018/1/5 0005.
 */

public class QueryInfoBackBean {

    /**
     * sign : fca5129814307b3456041e8c45a08a1f
     * timestamp : 1515141301
     * data : {"success":true,"list":{"fail":[{"id":306554,"phone":"15102868137","express_num":"112233","resend_msg":1,"lockitem_id":17371}],"success":[{"id":306554,"phone":"15102868137","express_num":"112233","resend_msg":1,"lockitem_id":17371}]}}
     * class : DevOp
     * method : resendMsg
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
         * list : {"fail":[{"id":306554,"phone":"15102868137","express_num":"112233","resend_msg":1,"lockitem_id":17371}],"success":[{"id":306554,"phone":"15102868137","express_num":"112233","resend_msg":1,"lockitem_id":17371}]}
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
            private List<FailBean> fail;
            private List<SuccessBean> success;

            public List<FailBean> getFail() {
                return fail;
            }

            public void setFail(List<FailBean> fail) {
                this.fail = fail;
            }

            public List<SuccessBean> getSuccess() {
                return success;
            }

            public void setSuccess(List<SuccessBean> success) {
                this.success = success;
            }

            public static class FailBean {
                /**
                 * id : 306554
                 * phone : 15102868137
                 * express_num : 112233
                 * resend_msg : 1
                 * lockitem_id : 17371
                 */

                private int id;
                private String phone;
                private String express_num;
                private int resend_msg;
                private int lockitem_id;

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

                public int getResend_msg() {
                    return resend_msg;
                }

                public void setResend_msg(int resend_msg) {
                    this.resend_msg = resend_msg;
                }

                public int getLockitem_id() {
                    return lockitem_id;
                }

                public void setLockitem_id(int lockitem_id) {
                    this.lockitem_id = lockitem_id;
                }
            }

            public static class SuccessBean {
                /**
                 * id : 306554
                 * phone : 15102868137
                 * express_num : 112233
                 * resend_msg : 1
                 * lockitem_id : 17371
                 */

                private int id;
                private String phone;
                private String express_num;
                private int resend_msg;
                private int lockitem_id;

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

                public int getResend_msg() {
                    return resend_msg;
                }

                public void setResend_msg(int resend_msg) {
                    this.resend_msg = resend_msg;
                }

                public int getLockitem_id() {
                    return lockitem_id;
                }

                public void setLockitem_id(int lockitem_id) {
                    this.lockitem_id = lockitem_id;
                }
            }
        }
    }
}
