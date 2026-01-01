package com.soul.network.encrypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Decode and encode utils.
 *
 * @author xiudong.yuan@midea.com
 * @since 2018-6-7
 */
@LDPProtect
public class EncodeAndDecodeUtils {
    private final static String HEX_UC = "0123456789ABCDEF";
    private final static String HEX_LC = "0123456789abcdef";

    private static EncodeAndDecodeUtils instance = new EncodeAndDecodeUtils();

    private EncodeAndDecodeUtils() {
    }

    public static EncodeAndDecodeUtils getInstance() {
        return instance;
    }

    /**
     * Encode AES string.
     *
     * @param cleartext raw string
     * @param seed      seed
     * @return encrypt string
     */
    @LDPProtect
    public String encodeAES(String cleartext, String seed) {
        try {
            byte[] result = encodeAES(cleartext.getBytes("UTF-8"), seed.getBytes());
            return bytesToLcHexString(result);
        } catch (Exception e) {
            return null;
        }
    }

    @LDPProtect
    public String encodeAES(String cleartext, String seed,String IV) {
        try {
            byte[] result = encodeAES(cleartext.getBytes("UTF-8"), seed.getBytes(),IV==null?null:IV.getBytes());
            return bytesToLcHexString(result);
        } catch (Exception e) {
            return null;
        }
    }
    /**
     * Decode AES string.
     *
     * @param encrypted encrypted string
     * @param seed      seed
     * @return decode string
     */
    @LDPProtect
    public String decodeAES(String encrypted, String seed) {
//        try {
//            byte[] enc = hexStringToBytes(encrypted);
//            byte[] result = decodeAES(enc, seed.getBytes());
//            if (result != null) {
//                return new String(result, "UTF-8");
//            }
//            return null;
//        } catch (Exception e) {
//            return null;
//        }
        return decodeAES(encrypted,seed,null);
    }
    /**
     * 解密AES.
     *
     * @param encrypted 加密后的字符串
     * @param seed      解密种子
     * @param iv  解密iv
     * @return 解密字符串
     */
    @LDPProtect
    public String decodeAES(String encrypted, String seed,String iv) {
        try {
            byte[] enc = hexStringToBytes(encrypted);
            byte[] result = decodeAES(enc, seed.getBytes(),iv==null?null:iv.getBytes());
            if (result != null) {
                return new String(result, "UTF-8");
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }



    /**
     * Encode AES data.
     *
     * @param clear raw data
     * @param raw   seed
     * @return encrypt data
     */
    @LDPProtect
    public byte[] encodeAES(byte[] clear, byte[] raw) {
//        try {
//            SecretKeySpec sKeySpec = new SecretKeySpec(raw, "AES");
//            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
//            cipher.init(Cipher.ENCRYPT_MODE, sKeySpec);
//            return cipher.doFinal(clear);
//        } catch (Exception e) {
//            return null;
//        }
        return encodeAES(clear,raw,null);
    }
    /**
     * AES 加密
     *
     * @param clear 原始数据
     * @param raw   加密种子
     * @param iv  加密iv
     * @return 加密后数据
     */
    @LDPProtect
    public byte[] encodeAES(byte[] clear, byte[] raw,byte iv[]) {
        try {
            SecretKeySpec sKeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = null;
            if(iv==null) {
                cipher=Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, sKeySpec);
            }else {
                cipher=Cipher.getInstance( "AES/CBC/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, new IvParameterSpec(iv));
            }
            return cipher.doFinal(clear);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Decode AES data.
     *
     * @param encrypted data bytes
     * @param raw       seed
     * @return decode data
     */
    @LDPProtect
    public byte[] decodeAES(byte[] encrypted, byte[] raw) {
//        try {
//            SecretKeySpec sKeySpec = new SecretKeySpec(raw, "AES");
//            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
//            cipher.init(Cipher.DECRYPT_MODE, sKeySpec);
//            return cipher.doFinal(encrypted);
//        } catch (Exception e) {
//            return null;
//        }
        return decodeAES(encrypted,raw,null);
    }

    /**
     * AES解密
     *
     * @param encrypted 加密的数据
     * @param raw       解密种子
     * @param iv    解密iv
     * @return 解密后的数据
     */
    @LDPProtect
    public byte[] decodeAES(byte[] encrypted, byte[] raw,byte []iv) {
        try {
            SecretKeySpec sKeySpec = new SecretKeySpec(raw,"AES");
            Cipher cipher =null;
            if(iv==null) {
                cipher= Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, sKeySpec);
            } else {
                cipher= Cipher.getInstance( "AES/CBC/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, sKeySpec, new IvParameterSpec(iv));
            }
            return cipher.doFinal(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * MD5 encrypt.
     *
     * @param cleartext raw string
     * @return HEX encrypted string
     */
    @LDPProtect
    public String encodeMD5(String cleartext) {
        if (cleartext != null && cleartext.length() > 0) {
            return bytesToLcHexString(encodeMD5(cleartext.getBytes()));
        } else {
            return cleartext;
        }
    }

    /**
     * MD5 encrypt.
     *
     * @param bytes raw data bytes
     * @return encrypted data byte
     */
    @LDPProtect
    public byte[] encodeMD5(byte[] bytes) {
        try {
            MessageDigest messagedigest = MessageDigest.getInstance("MD5");
            messagedigest.update(bytes);
            return messagedigest.digest();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * SHA256 encode.
     *
     * @param cleartext raw text
     * @return HEX encrypted text
     */
    @LDPProtect
    public String encodeSHA(String cleartext) {
        return bytesToLcHexString(encodeSHA(cleartext.getBytes()));
    }
    @LDPProtect
    public byte[] encodeSHA(byte[] clearbytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(clearbytes);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    @LDPProtect
    public static byte[] HMACSHA256(String data, String key) throws Exception {

        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] array = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
        return array;

    }

    /**
     * Change HEX string to bytes.
     *
     * @param hexString HEX string
     * @return bytes
     */

    @LDPProtect
    private byte[] hexStringToBytes(String hexString) {
        if (hexString == null) {
            return null;
        }
        int len = hexString.length() / 2;
        byte[] bytes = new byte[len];
        try {
            for (int i = 0; i < len; i++) {
                bytes[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    /**
     * Change bytes to uppercase string.
     *
     * @param src bytes
     * @return upper HEX string
     */
    @LDPProtect
    private String bytesToUcHexString(byte[] src) {
        if (src == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(2 * src.length);
        for (byte data : src) {
            sb.append(byteToUcHexString(data));
        }
        return sb.toString();
    }

    /**
     * Change bytes to lowercase string.
     *
     * @param src bytes
     * @return lowercase HEX string
     */
    @LDPProtect
    private String bytesToLcHexString(byte[] src) {
        if (src == null)
            return null;
        StringBuilder sb = new StringBuilder(2 * src.length);
        for (byte data : src) {
            sb.append(byteToLcHexString(data));
        }
        return sb.toString();
    }

    /**
     * Change byte to lowercase HEX string.
     *
     * @param src byte
     * @return lowercase HEX string
     */
    @LDPProtect
    private String byteToLcHexString(byte src) {
        return String.format("%s%s", HEX_LC.charAt((src >> 4) & 0x0F), HEX_LC.charAt(src & 0x0F));
    }

    /**
     * Change byte to uppercase HEX string.
     *
     * @param src byte
     * @return upper HEX string
     */
    @LDPProtect
    private String byteToUcHexString(byte src) {
        return String.format("%s%s", HEX_UC.charAt((src >> 4) & 0x0F), HEX_UC.charAt(src & 0x0F));
    }
}
