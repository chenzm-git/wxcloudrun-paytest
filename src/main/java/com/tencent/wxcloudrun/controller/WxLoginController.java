package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.dto.UserInfo;
import com.tencent.wxcloudrun.dto.UserUsage;
import com.tencent.wxcloudrun.dto.UserUsed;
import com.tencent.wxcloudrun.model.OpenId;
import com.tencent.wxcloudrun.service.WxLoginService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author chenzm
 * @version V1.0
 * @since 2023/5/15
 **/
@RestController
@Slf4j
public class WxLoginController {

    @Autowired
    private WxLoginService wxLoginService;

    @ApiOperation(notes = "根据jscode查询openid", value = "根据jscode查询openid")
    @PostMapping(value = "/user/jxcode")
    public String jxcode(@RequestBody String code){
        log.info("code 输入 : " + code);
        String output = wxLoginService.jxcode(code);
        log.info("openid 输出 : " + output);
        return output;
    }

    @ApiOperation(notes = "查询用户列表", value = "查询用户列表")
    @PostMapping(value = "/user/list")
    public List<UserInfo> queryUserInfo(){
        return wxLoginService.queryUserInfo();
    }

    @ApiOperation(notes = "openid注册", value = "openid注册")
    @PostMapping(value = "/user/openidRegister")
    public UserInfo openidRegister(@RequestBody OpenId openId){
        log.info("通过openid注册用户！");

        return wxLoginService.register(openId);
    }

    @ApiOperation(notes = "根据openid查询用户", value = "根据openid查询用户")
    @PostMapping(value = "/user/queryUserByOpenId")
    public UserInfo queryUserByOpenId(@RequestBody OpenId openId){
        log.info("通过openid查询用户！");

        return wxLoginService.queryByOpenId(openId);
    }

    @ApiOperation(notes = "根据openid用户查询使用限额", value = "根据openid用户查询使用限额")
    @PostMapping(value = "/user/queryUsage")
    public UserUsage queryUsage(@RequestBody OpenId openId){
        log.info("查询用户使用限额：" + openId.getOpenId());

        return wxLoginService.queryUsage(openId);
    }

    @ApiOperation(notes = "更新使用限额", value = "更新使用限额，传入本次使用token数")
    @PostMapping(value = "/user/updateUsage")
    public UserUsage updateUsage(@RequestBody UserUsed userUsed){
        log.info("更新用户使用限额：" + userUsed.getOpenId());

        return wxLoginService.updateUsage(userUsed);
    }

}
