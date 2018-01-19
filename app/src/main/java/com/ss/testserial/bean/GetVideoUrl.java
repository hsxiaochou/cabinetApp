package com.ss.testserial.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Administrator on 2018/1/19 0019.
 */

public class GetVideoUrl {

    /**
     * sign : 1257dcfd1d299da2a7913b170ee547c2
     * timestamp : 1516355616
     * data : {"video":["/home/cabinet/Public/adVideo/B0F1EC21B552/16596c72ee6eeca64257219b32061107.mp4","/home/cabinet/Public/adVideo/B0F1EC21B552/e2ef671e72279c5d616d3362aa38b4a0.mp4"],"switch":true}
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
         * video : ["/home/cabinet/Public/adVideo/B0F1EC21B552/16596c72ee6eeca64257219b32061107.mp4","/home/cabinet/Public/adVideo/B0F1EC21B552/e2ef671e72279c5d616d3362aa38b4a0.mp4"]
         * switch : true
         */

        @SerializedName("switch")
        private boolean switchX;
        private List<String> video;

        public boolean isSwitchX() {
            return switchX;
        }

        public void setSwitchX(boolean switchX) {
            this.switchX = switchX;
        }

        public List<String> getVideo() {
            return video;
        }

        public void setVideo(List<String> video) {
            this.video = video;
        }
    }
}
