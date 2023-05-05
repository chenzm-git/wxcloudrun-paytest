package com.tencent.wxcloudrun.util;


import com.google.common.base.Joiner;
import com.google.common.base.Joiner.MapJoiner;

import org.apache.commons.codec.binary.Hex;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @author chenzm
 * @version V1.0
 * @since 2023-05-05$
 **/
public class SignHelper {
    public static final String KEY_ALGORTHM = "RSA";
    public static final String SIGNATURE_ALGORITHM = "SHA256WithRSA";

    public static final String SIGNATURE_PARAM_KEY = "signature";

    public static final MapJoiner PARAM_MAP_JOINER = Joiner.on("&").withKeyValueSeparator("=")
            .useForNull(StringUtils.EMPTY);

    private static final Joiner PARAM_ARRAY_JOINER = Joiner.on(",").skipNulls();

    /**
     * 用私钥对信息生成数字签名
     *
     * @param bizContent 数据
     * @param privateKey 私钥
     * @return
     * @throws Exception
     */
    public static String sign(String bizContent, String privateKey) throws Exception {
        //解密私钥
        byte[] keyBytes = decryptBASE64(privateKey);
        //byte[] keyBytes = privateKey.getBytes();

        //构造PKCS8EncodedKeySpec对象
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
        //指定加密算法
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORTHM);
        //取私钥匙对象
        PrivateKey privateKey2 = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        //用私钥对信息生成数字签名
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateKey2);
        signature.update(bizContent.getBytes());

        return encryptBASE64(signature.sign());
    }

    /**
     * 用私钥对信息生成数字签名
     *
     * @param bizParams  Map参数
     * @param privateKey 私钥
     * @return
     * @throws Exception
     */
    public static String signStringParam(Map<String, String> bizParams,  String privateKey) throws Exception {
        TreeMap<String, String> bizSortParam = new TreeMap<>();
        bizSortParam.putAll(bizParams);
        String bizContent = PARAM_MAP_JOINER.join(bizSortParam);
        return sign(bizContent, privateKey);
    }

    /**
     * 用私钥对信息生成数字签名
     *
     * @param bizParams  Map参数
     * @param privateKey 私钥
     * @return
     * @throws Exception
     */
    public static String signArrayParam(Map<String, String[]> bizParams,  String privateKey) throws Exception {
        TreeMap<String, String> bizSortParam = new TreeMap<>();

        for (Entry<String, String[]> entry : bizParams.entrySet()) {
            String paramKey = entry.getKey();
            String[] paramValue = entry.getValue();

            if (ArrayUtils.isNotEmpty(paramValue)) {
                bizSortParam.put(paramKey, PARAM_ARRAY_JOINER.join(paramValue));
            } else {
                bizSortParam.put(paramKey, StringUtils.EMPTY);
            }
        }
        String bizContent = PARAM_MAP_JOINER.join(bizSortParam);
        return sign(bizContent, privateKey);
    }

    /**
     * 校验数字签名
     *
     * @param bizContent 数据
     * @param publicKey  公钥
     * @param sign       数字签名
     * @return
     * @throws Exception
     */
    public static boolean verify(String bizContent, String publicKey,
                                 String sign) throws Exception {

        if (StringUtils.isBlank(publicKey) || StringUtils.isBlank(sign)) {
            return false;
        }

        //解密公钥
        byte[] keyBytes = decryptBASE64(publicKey);
        //构造X509EncodedKeySpec对象
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
        //指定加密算法
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORTHM);
        //取公钥匙对象
        PublicKey publicKey2 = keyFactory.generatePublic(x509EncodedKeySpec);

        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicKey2);
        signature.update(bizContent.getBytes());
        //验证签名是否正常
        try {
            return signature.verify(decryptBASE64(sign));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 校验数字签名
     *
     * @param bizParams map数据
     * @param publicKey 公钥
     * @param sign      数字签名
     * @return
     * @throws Exception
     */
    public static boolean verifyStringParam(Map<String, String> bizParams, String publicKey,
                                            String sign) throws Exception {
        TreeMap<String, String> bizSortParam = new TreeMap<>();
        bizSortParam.putAll(bizParams);
        String bizContent = PARAM_MAP_JOINER.join(bizSortParam);
        return verify(bizContent, publicKey, sign);
    }

    /**
     * 校验数字签名
     *
     * @param bizParams map数据
     * @param publicKey 公钥
     * @param sign      数字签名
     * @return
     * @throws Exception
     */
    public static boolean verifyArrayParam(Map<String, String[]> bizParams, String publicKey,
                                           String sign) throws Exception {
        TreeMap<String, String> bizSortParam = new TreeMap<>();

        for (Entry<String, String[]> entry : bizParams.entrySet()) {
            String paramKey = entry.getKey();
            String[] paramValue = entry.getValue();

            if (ArrayUtils.isNotEmpty(paramValue)) {
                bizSortParam.put(paramKey, PARAM_ARRAY_JOINER.join(paramValue));
            } else {
                bizSortParam.put(paramKey, StringUtils.EMPTY);
            }
        }

        String bizContent = PARAM_MAP_JOINER.join(bizSortParam);
        return verify(bizContent, publicKey, sign);
    }


    /** * BASE64解密 * * @param key = 需要解密的密码字符串 * @return * @throws Exception */
    public static byte[] decryptBASE64(String key) throws Exception {
        return (new BASE64Decoder()).decodeBuffer(key);
    }

    /** * BASE64加密 * * @param key = 需要加密的字符数组 * @return * @throws Exception */
    public static String encryptBASE64(byte[] key) throws Exception {
        return (new BASE64Encoder()).encodeBuffer(key);
    }

    public static void main(String[] args){
        try{
            String sign = SignHelper.sign("wx9ab70e5c6f40b297\n1683280962\nqwertgbvfdj5323drtj74dfr78jyttew\nprepay_id=wx05180242831895a0e9c8ed01ccc8090000\n",
                    "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDF4TWVI2Fczjfx\n" +
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
                    "zsPePrPUniUSaLc+ybX1qD7p");

            System.out.println(sign.replaceAll("\r\n", ""));

            String paySign = new sun.misc.BASE64Encoder().encode(sign.getBytes());
            System.out.println(paySign);
        }
        catch (Exception e){
            e.printStackTrace();
        }
//
//        signRsa("wx8888888888888888\n" +
//                "1414561699\n" +
//                "5K8264ILTKCH16CQ2502SI8ZNMTM67VS\n" +
//                "prepay_id=wx201410272009395522657a690389285100");
    }


    public static void signRsa(String src){
        try {
            //1.初始化密钥
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(512);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPublicKey rsaPublicKey = (RSAPublicKey)keyPair.getPublic();
            RSAPrivateKey rsaPrivateKey = (RSAPrivateKey)keyPair.getPrivate();

            //2.执行签名
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(rsaPrivateKey.getEncoded());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
            Signature signature = Signature.getInstance("MD5withRSA");
            signature.initSign(privateKey);
            signature.update(src.getBytes());
            byte[] result = signature.sign();
            System.out.println("jdk rsa sign : " + Hex.encodeHexString(result));

            //3.验证签名
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(rsaPublicKey.getEncoded());
            keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
            signature = Signature.getInstance("MD5withRSA");
            signature.initVerify(publicKey);
            //请注意此处，方法入参为源数据的字节数组，如果源数据很大，这里会报错OOM之类的
            signature.update(src.getBytes());
            boolean bool = signature.verify(result);
            System.out.println("jdk rsa verify : " + bool);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
