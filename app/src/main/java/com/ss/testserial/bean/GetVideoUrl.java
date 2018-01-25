package com.ss.testserial.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Administrator on 2018/1/19 0019.
 */

public class GetVideoUrl {

    /**
     * sign : 401f9ef5c278aab9d0c7171556e425cb
     * timestamp : 1516861085
     * data : {"video":[{"size":37199938,"name":"/Public/adVideo/B0F1EC21B552/雷克萨斯新NX.mp4"}],"switch":true}
     * class : DevOp
     * method : getAdVideo
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
         * video : [{"size":37199938,"name":"/Public/adVideo/B0F1EC21B552/雷克萨斯新NX.mp4"}]
         * switch : true
         */

        @SerializedName("switch")
        private boolean switchX;
        private List<VideoBean> video;

        public boolean isSwitchX() {
            return switchX;
        }

        public void setSwitchX(boolean switchX) {
            this.switchX = switchX;
        }

        public List<VideoBean> getVideo() {
            return video;
        }

        public void setVideo(List<VideoBean> video) {
            this.video = video;
        }

        public static class VideoBean {
            /**
             * size : 37199938
             * name : /Public/adVideo/B0F1EC21B552/雷克萨斯新NX.mp4
             */

            private int size;
            private String name;

            public int getSize() {
                return size;
            }

            public void setSize(int size) {
                this.size = size;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
    }
}
