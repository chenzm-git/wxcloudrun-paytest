package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.dao.UserServiceDao;
import com.tencent.wxcloudrun.dto.UserInfo;
import com.tencent.wxcloudrun.dto.UserUsage;
import com.tencent.wxcloudrun.dto.UserUsed;
import com.tencent.wxcloudrun.model.OpenId;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * @author chenzm
 * @version V1.0
 * @since 2023/5/15
 **/
@Service
@Slf4j
public class WxLoginService {

    @Autowired
    private UserServiceDao userServiceDao;

    /**
     * 查询用户列表
     * @return 用户列表
     */
    public List<UserInfo> queryUserInfo(){
        return userServiceDao.queryUserInfo();
    }

    /**
     * 通过微信注册
     * @param openId 微信openId
     * @return 用户信息
     */
    public UserInfo register(OpenId openId){

        //先判断是否已存在用户
        UserInfo userInfo = userServiceDao.queryByOpenId(openId.getOpenId());

        if (userInfo == null){
            userInfo = new UserInfo();

            //生成用户ID
            String userId = UUID.randomUUID().toString();
            userInfo.setUserId(userId);

            userInfo.setRegType(2);
            userInfo.setWxOpenId(openId.getOpenId());

            userServiceDao.addUserInfo(userInfo);

            //增加默认使用次数
            UserUsage userUsage = new UserUsage();
            userUsage.setUserId(userId);
            userServiceDao.addUsage(userUsage);
        }

        return userInfo;
    }

    /**
     * 根据openId查询用户
     * @param openId openId
     * @return 用户信息
     */
    public UserInfo queryByOpenId(OpenId openId){
        return userServiceDao.queryByOpenId(openId.getOpenId());
    }

    /**
     * 根据用户id查询使用限额
     * @param openId openId
     * @return 使用限额
     */
    public UserUsage queryUsage(OpenId openId){

        UserInfo userInfo = queryByOpenId(openId);
        if (userInfo != null){
            return userServiceDao.queryUsage(userInfo.getUserId());
        }
        else {
            return null;
        }
    }


    /**
     * 更新使用限额
     * @param userUsed 使用限额
     * @return 使用限额
     */
    public UserUsage updateUsage(UserUsed userUsed){

        //查询用户信息
        UserInfo userInfo = userServiceDao.queryByOpenId(userUsed.getOpenId());

        if (userInfo != null){

            //查询当前信息
            UserUsage userUsage = userServiceDao.queryUsage(userInfo.getUserId());

            userUsage.setUsedToken(userUsage.getUsedToken() + userUsed.getToken());
            userUsage.setUsedCount(userUsage.getUsedCount() + 1);

            //更新
            int ret = userServiceDao.updateUsage(userUsage);
            log.info("更新使用限额结果：" + ret);

            return userUsage;
        }
        else {
            return null;
        }
    }

    /**
     * 获取openid
     * @param code jdcode
     * @return openid
     */
    public String jxcode(String code){

        String openid = "";

        String param = "?appid=wx9ab70e5c6f40b297&secret=b2d1af9f78af07fad1c101db95f237d5&grant_type=authorization_code&js_code=" + code;

        CloseableHttpClient httpClient = HttpClients.createDefault();

        //请求URL
        HttpGet httpGet = new HttpGet("https://api.weixin.qq.com/sns/jscode2session" + param);

        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200 || statusCode == 204) {
                String retStr = EntityUtils.toString(response.getEntity());
                log.info("return : " + retStr);
                JSONObject jsonObject = new JSONObject(retStr);
                if (jsonObject.has("openid")){
                    openid = jsonObject.getString("openid");
                }
            } else {
                log.info("failed,resp code = " + statusCode+ ",return body = " + EntityUtils.toString(response.getEntity()));
            }

            response.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        //openid = "oy5vk5dB6ahNPJlkCNrC5FOpMyEE";
        return openid;
    }
}
