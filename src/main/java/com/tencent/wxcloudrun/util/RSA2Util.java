package com.tencent.wxcloudrun.util;

import cn.hutool.crypto.KeyUtil;
import cn.hutool.crypto.asymmetric.SignAlgorithm;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chenzm
 * @version V1.0
 * @since 2023-05-05$
 **/
public class RSA2Util {
    // 算法类别
    private final static String SIGN_TYPE = "RSA";
    // 算法位数
    private final static Integer KEY_SIZE = 2048;

    /**
     * 生成公私钥
     */
    public Map<String, String> getPublicPrivateKey() {
        Map<String, String> pubPriKey = new HashMap<>();
        KeyPair keyPair = KeyUtil.generateKeyPair(SIGN_TYPE, KEY_SIZE);
        String publicKeyStr = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateKeyStr = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        pubPriKey.put("publicKey", publicKeyStr);
        pubPriKey.put("privateKey", privateKeyStr);
        return pubPriKey;
    }

    /**
     * 签名
     */
    public static String sign256(byte[] signData, String priKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(priKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(SIGN_TYPE);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            Signature si = Signature.getInstance(SignAlgorithm.SHA256withRSA.getValue());
            si.initSign(privateKey);
            si.update(signData);
            byte[] sign = si.sign();
            return Base64.getEncoder().encodeToString(sign);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 验签
     */
    public static boolean verify256(byte[] dataBytes, String sign, String pubkey) {
        boolean flag = false;
        try {
            byte[] signByte = Base64.getDecoder().decode(sign);
            byte[] encodedKey = Base64.getDecoder().decode(pubkey);
            Signature verf = Signature.getInstance(SignAlgorithm.SHA256withRSA.getValue());
            KeyFactory keyFac = KeyFactory.getInstance(SIGN_TYPE);
            PublicKey puk = keyFac.generatePublic(new X509EncodedKeySpec(encodedKey));
            verf.initVerify(puk);
            verf.update(dataBytes);
            flag = verf.verify(signByte);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return flag;
    }

    public static void main(String[] args){

        String str = "wx8888888888888888\n" +
                "1414561699\n" +
                "5K8264ILTKCH16CQ2502SI8ZNMTM67VS\n" +
                "prepay_id=wx201410272009395522657a690389285100";
        String key = "-----BEGIN PRIVATE KEY-----\n" +
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
                "zsPePrPUniUSaLc+ybX1qD7p\n" +
                "-----END PRIVATE KEY-----";

        System.out.println(RSA2Util.sign256(str.getBytes(), key));
    }
}
