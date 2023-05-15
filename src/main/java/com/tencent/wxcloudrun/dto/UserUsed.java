package com.tencent.wxcloudrun.dto;

import lombok.Data;

/**
 * @author chenzm
 * @version V1.0
 * @since 2023/5/12
 **/
@Data
public class UserUsed {

    private String openId;

    private int token;
}
