package com.tencent.wxcloudrun.dto;

import lombok.Data;

/**
 * 用户用量信息
 * @author chenzm
 * @version V1.0
 * @since 2023/5/12
 **/
@Data
public class UserUsage {

    private String userId;

    private long allowCount = 20;

    private long usedCount = 0;

    private long allowToken = 1000;

    private long usedToken = 0;
}
