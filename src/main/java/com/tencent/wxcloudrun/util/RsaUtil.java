package com.tencent.wxcloudrun.util;

import org.springframework.stereotype.Component;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * RSA加解密
 * @Author:blueskyevil
 * @Date:2019/3/15
 */
@Component
public class RsaUtil implements Serializable
{
    private static final long serialVersionUID = -7565189502268009837L;
    private static final String KEY_ALGORITHM = "RSA";
    private static final String PUBLIC_KEY = "RSAPublicKey";
    private static final String PRIVATE_KEY = "RSAPrivateKey";
    public static final String SIGNATURE_ALGORITHM="MD5withRSA";
    private static final String RSA_KEY_ALGORITHM = "RSA/None/OAEPWithSHA-1AndMGF1Padding";
    /*
    *RSA最大加密明文密码
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;
    /*
    *RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;

    /**
     * 初始化key
     * @return
     * @throws Exception
     */
    public  Map<String, String> initKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(2048);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        String publicKeyStr = encryptBase64(publicKey.getEncoded());
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        String privateKeyStr = encryptBase64(privateKey.getEncoded());
        Map<String, String> keyMap = new HashMap<>(4);
        keyMap.put(PUBLIC_KEY, publicKeyStr);
        keyMap.put(PRIVATE_KEY, privateKeyStr);
        return keyMap;
    }

    /**
     * 获取公钥字符串
     * @param keyMap
     * @return
     * @throws Exception
     */
    public  String getPublicKeyStr(Map<String, String> keyMap)
    {
        return keyMap.get(PUBLIC_KEY);
    }

    /**
     * 获取私钥字符串
     * @param keyMap
     * @return
     * @throws Exception
     */
    public  String getPrivateKeyStr(Map<String, String> keyMap)
    {
        return keyMap.get(PRIVATE_KEY);
    }

    /**
     * 获取公钥
     * @param key
     * @return
     * @throws Exception
     */
    public  PublicKey getPublicKey(String key) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes;
        keyBytes = (new BASE64Decoder()).decodeBuffer(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 获取私钥
     * @param key
     * @return
     * @throws Exception
     */
    public  PrivateKey getPrivateKey(String key) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes;
        keyBytes = (new BASE64Decoder()).decodeBuffer(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * base64编码
     * @param key
     * @return
     * @throws Exception
     */
    public  String encryptBase64(byte[] key)
    {
        return (new BASE64Encoder()).encodeBuffer(key);
    }

    /**
     * base64解码返回字节
     * @param key
     * @return
     * @throws Exception
     */
    public  byte[] decryptBase64(String key) throws IOException {
        return (new BASE64Decoder()).decodeBuffer(key);
    }

    /**
     * 签名
     * @param data
     * @param privateKeyStr
     * @return
     * @throws Exception
     */
    public  byte[] sign(byte[] data,String privateKeyStr) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        PrivateKey priK = getPrivateKey(privateKeyStr);
        Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
        sig.initSign(priK);
        sig.update(data);
        return sig.sign();
    }

    /**
     * 验证
     * @param data
     * @param sign
     * @param publicKeyStr
     * @return
     * @throws Exception
     */
    public  boolean verify(byte[] data,byte[] sign,String publicKeyStr) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        PublicKey pubK = getPublicKey(publicKeyStr);
        Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
        sig.initVerify(pubK);
        sig.update(data);
        return sig.verify(sign);
    }

    /**
     * 加密
     * @param plainText
     * @param publicKeyStr
     * @return
     * @throws Exception
     */
    public  byte[] encrypt(byte[] plainText,String publicKeyStr) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        PublicKey publicKey = getPublicKey(publicKeyStr);
        Cipher cipher = Cipher.getInstance(RSA_KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return endecrypt(plainText,cipher,MAX_ENCRYPT_BLOCK);
    }

    /**
     * 解密
     * @param encryptText
     * @param privateKeyStr
     * @return
     * @throws Exception
     */
    public  byte[] decrypt(byte[] encryptText,String privateKeyStr) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        PrivateKey privateKey = getPrivateKey(privateKeyStr);
        Cipher cipher = Cipher.getInstance(RSA_KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return endecrypt(encryptText,cipher,MAX_DECRYPT_BLOCK);
    }

    /**
     * 加密解密共用
     * @param encryptPlainText
     * @param cipher
     * @return
     * @throws Exception
     */
    private  byte[] endecrypt(byte[] encryptPlainText,Cipher cipher,int maxBlock) throws BadPaddingException, IllegalBlockSizeException, IOException {
        int inputLen = encryptPlainText.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        int i = 0;
        byte[] cache;
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > maxBlock) {
                cache = cipher.doFinal(encryptPlainText, offSet, maxBlock);
            }
            else {
                cache = cipher.doFinal(encryptPlainText, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * maxBlock;
        }
        byte[] commonText = out.toByteArray();
        out.close();
        return commonText;
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

        try{
            new RsaUtil().sign(str.getBytes(), key);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
