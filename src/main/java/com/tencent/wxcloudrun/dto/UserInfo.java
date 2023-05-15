package com.tencent.wxcloudrun.dto;

import lombok.Data;

/**
 * @author chenzm
 * @version V1.0
 * @since 2023/5/9
 **/
@Data
public class UserInfo {

    private String userId;

    private String userName;

    /**
     * 1:email
     * 2:微信
     */
    private int regType;

    private String email;

    private String wxOpenId;

    private String wcUnionId;

    private Long regTime;

    private String nickTime;

    private String avater;

    private String userPasswd;

}
