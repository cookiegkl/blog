package com.mtons.mblog.config;

import lombok.Data;

@Data
public class SmmsResult {

    private String success;

    private String code;

    private Data data;

    @lombok.Data
    public static class Data {

        private Integer file_id;

        private Integer width;

        private Integer height;

        private String filename;

        private String storename;

        private Integer size;

        private String path;

        private String hash;

        private String url;

        private String delete;

        private String page;
    }

    private String RequestId;
}
