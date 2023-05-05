package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.util.SignHelper;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.*;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.sql.Time;

/**
 * @author chenzm
 * @version V1.0
 * @since 2023-05-05$
 **/
@Service
@Slf4j
public class PayService {

    private CloseableHttpClient httpClient;

    /** 商户号 */
    public static String merchantId = "1643223534";
    /** 商户证书序列号 */
    public static String merchantSerialNumber = "5E48960119160E6FEA875FDCC4A0257314AE125D";
    /** 商户APIV3密钥 */
    public static String apiV3key = "qwertgbvfdj5323drtj74dfr78jyttew";

    /**
     * 私钥
     */
    private String privateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDF4TWVI2Fczjfx\n" +
            "UPx2sI0ulY+t/yJk4Fn3lbetmdKY8Yc7Aj7l8XChL0J54QeJSEHoFIZZJcjAtkhc\n" +
            "JQE+JY/aN/Ux63ekq31sBmiKS5sD6lPdDXbtoBJqyEsYu9TfCTqczl7WAMQeRkQH\n" +
            "FW5FgpRfKh2fFbg89sS+QsicZiVPS5tE9Gngw+k2APYDRxVQeV4nb5jo8/BZqeGn\n" +
            "3aYFyW8TzKelyY5VJ/Hzos9+OJCu8XbUUeqgwbOgk9FsqPjYmW9yAkRuGa0g2gUy\n" +
            "evzIKv/rdBrZwKyAJ/e6Bg+71PeTEXq3gVKPhoF9yvI0rKNPwfs1ePJS7fdAtk7i\n" +
            "2BrbRqZXAgMBAAECggEBAJrWuSj0QHFwZFIOPx9Y04DKQ12xsOYisAOOQeYz4ZkQ\n" +
            "FfNUtIcVwD5ATtI0BcOkqP3DYcVMTaSOflysECbjGYd2FgVz7XELR99JvG/K7YfI\n" +
            "ysrEiHU0tnScOjcmc1H7VMPysD7g5pWSAhVQ7bKylQtKV1dulXYO2rDObVAEldlP\n" +
            "PG4pd9q75fl637Sc96Lnx9sQ29cv0bEbeQZs2oLKrHBEyYBbZC/IRmn1JEHycIet\n" +
            "BKW7eNgJY/2HU1t5Xosrwhjt+yYCTr+HhG7sKaY0uPhNpzIU72SOmElwKcYIEYvs\n" +
            "eV7rqdf+Ppp53lPYclrjcX3aE32ZW7UlxAfZJWsv16ECgYEA9yimoE6V1S+h9k7S\n" +
            "XrIoxW6dC2QBM3FTcJtE7yzEetUtY1zqh57ab08xjWBo9UKMOuFTkO+WsKGk8rFQ\n" +
            "2qSifn6LnllDjalZJyKa+DO9lizetFUQAQfFuqZHnEjZY+c/1I5C9uTOzpcAKSd5\n" +
            "tUPjAvFDzZXNLM9sCXfD3rWs7W8CgYEAzPVJbyy+YjFbpfYRRXhjc9tVaKDQtUFU\n" +
            "Tep95HyFhaMVyb7ElfoP26sp846MA1ORRaPR29YJrMoPqcCumXFifhyMGp/yQ5mJ\n" +
            "nRV5Hq0YlUJ4IT2X5GGzPBGOId5r45pqrXFF5vSb2bc8Zj3B3rRK0K7wVkVlRkqq\n" +
            "JEiEvQntsZkCgYB++6S4QgfbEvDsgky1GGW4If+PpZ60VmofNbbyBxcfYL1ECq34\n" +
            "ZdYmUBLOZxUlxT4U1kW/9kh+kV4UzqMS4nkV8mA7R/NcKgDDCZWDJdom+QCmt/lT\n" +
            "/jFJlzq9gfQmzt3NkBW5kY7rN0t+2Wg/iBRvI5PJYUib2CnSp3S7zK1/AwKBgCPR\n" +
            "VeT838SPNaH6L6iBUngDw5hGSlLyuMXpDdkpPbhN+NfJ49cF3VGZRvqOVb+bEg8m\n" +
            "gt01OXmd0kDrMFgWbYz2djGM9CyGH3t5LjKDM4GaHR5KAkpiHI2Jz9nxYc9jw/LN\n" +
            "kda7tqTEleSUNFY0EcMIX23kML+o+rTei3vxyT05AoGBANGhotfDF2D/ixBMp7Ql\n" +
            "MlbL3ej1GHR0co8kAn28lBbPTe5wAs+i1SxkNZnHuCmwJjV6D5ia2MZiUxSFl6PY\n" +
            "PLkCfc4RanbUNwkV7GGDqKpLSzKlklTd0ghx9lXgV8gtmP9QLYHx3rOBWPVA0mUg\n" +
            "zsPePrPUniUSaLc+ybX1qD7p";


    public void setup() {

        try{
            //私钥
            PrivateKey merchantPrivateKey = PemUtil.loadPrivateKey(
                    new ByteArrayInputStream(privateKey.getBytes("utf-8")));

            // 获取证书管理器实例
            CertificatesManager certificatesManager = CertificatesManager.getInstance();
            // 向证书管理器增加需要自动更新平台证书的商户信息
            certificatesManager.putMerchant(merchantId, new WechatPay2Credentials(merchantId,
                    new PrivateKeySigner(merchantSerialNumber, merchantPrivateKey)), apiV3key.getBytes(StandardCharsets.UTF_8));

            // 从证书管理器中获取verifier
            Verifier verifier = certificatesManager.getVerifier(merchantId);

            //http
            WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                    .withMerchant(merchantId, merchantSerialNumber, merchantPrivateKey)
                    .withValidator(new WechatPay2Validator(verifier));

            httpClient = builder.build();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 请求支付下单接口
     * @param code 前段编码
     * @return ok
     */
    public String pay(String code){

        String ret = "";

        if (httpClient == null){
            setup();
        }

        //现有code获取openid
        String openid = jxcode(code);
        
        if (openid != null && !"".equals(openid)){

            //请求URL
            HttpPost httpPost = new HttpPost("https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi");

            String tradeNo = "NO_" + System.currentTimeMillis();

            // 请求body参数
            String reqdata = "{"
                    + "\"amount\": {"
                    + "\"total\": 1,"
                    + "\"currency\": \"CNY\""
                    + "},"
                    + "\"mchid\": \"" + merchantId + "\","
                    + "\"description\": \"智能AI聊天程序\","
                    + "\"notify_url\": \"https://springboot-f3l5-47596-8-1318103173.sh.run.tcloudbase.com/api/pay/notify\","
                    + "\"payer\": {"
                    + "\"openid\": \"" + openid + "\"" + "},"
                    + "\"out_trade_no\": \"" + tradeNo + "\","
                    + "\"goods_tag\": \"WXG\","
                    + "\"appid\": \"wx9ab70e5c6f40b297\"" + "}";
            StringEntity entity = new StringEntity(reqdata,"utf-8");
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");

            try {
                //完成签名并执行请求
                CloseableHttpResponse response = httpClient.execute(httpPost);

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200 || statusCode == 204) {
                    ret = EntityUtils.toString(response.getEntity());
                    log.info("success,return body = " + ret);

                    String time = System.currentTimeMillis()/1000L + "";
                    //组装参数
                    JSONObject jsonObject = new JSONObject(ret);
                    jsonObject.put("timeStamp", time);
                    jsonObject.put("nonceStr", apiV3key);

                    String signStr = "wx9ab70e5c6f40b297\n" + time + "\n" + apiV3key + "\n" + "prepay_id=" + jsonObject.get("prepay_id") + "\n";
                    try{
                        jsonObject.put("signStr", signStr);
                        jsonObject.put("paySign", SignHelper.sign(signStr, privateKey));
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                    return jsonObject.toString();

                } else {
                    log.info("failed,resp code = " + statusCode+ ",return body = " + EntityUtils.toString(response.getEntity()));
                }

                response.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else{
            log.info("openid 为空！！！");
        }

        return ret;
    }

    /**
     * 获取openid
     * @param code
     * @return
     */
    private String jxcode(String code){

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


    private String sha256(){

        return "";
    }

}
