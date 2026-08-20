package com.haha.network.encrypt;

import android.util.Log;

import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.ECPointUtil;
import org.spongycastle.jce.spec.ECNamedCurveParameterSpec;
import org.spongycastle.jce.spec.ECNamedCurveSpec;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;

import javax.crypto.KeyAgreement;

/**
 * @auther: haha
 * @Date: 2025/12/25
 * @Detail:
 */
public class Encryption {
    private static String TAG = Encryption.class.getSimpleName();

    public final static String S_WIFI_DEFAULT_CONTENTS = "gck0y5tTttyDfUZittKtwMqZtsKtTYtytu1tPcTvfXC=";

    //秘钥字节数组长度,两个字节
    private static final int KEY_LEN = 16;

    //本地生成的最终加密key
    private byte[] mSessionKey = new byte[KEY_LEN];
    //设备端返回的ECDH公钥
    private byte[] mPublicKey = new byte[64];
    private ECPublicKey mECPublicKey;
    private ECPrivateKey mECPrivateKey;

    public void init() {
        if (!initKeyPair()) {
            Log.e(TAG, "Failed to initialize key pair");
            return;
        }
        byte[] finalKey = setPublicKeyAndExKeyId(mPublicKey);
        if (finalKey == null) {
            Log.e(TAG, "Failed to generate final key");
            return;
        }

        System.arraycopy(finalKey, 0, mSessionKey, 0, KEY_LEN);
        byte[] aesKey = new byte[KEY_LEN];
        System.arraycopy(finalKey, 0, aesKey, 0, KEY_LEN);
        Log.i(TAG, Util.bytesToHexString(aesKey));
        byte[] key = EncodeAndDecodeUtils.getInstance().encodeAES(IOTPWManager.decode(S_WIFI_DEFAULT_CONTENTS).getBytes(), aesKey);
        Log.i(TAG, Util.bytesToHexString(key));
    }

    private boolean initKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            keyPairGenerator.initialize(256);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            mECPublicKey = (ECPublicKey) keyPair.getPublic();
            mECPrivateKey = (ECPrivateKey) keyPair.getPrivate();
            return true;
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }

    private byte[] setPublicKeyAndExKeyId(byte[] receivedPublickKeyByte) {
        try {
            KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
            keyAgreement.init(mECPrivateKey);
            //根据接受的public key 生成 PublicKey
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey receivedPublickKey = getPublicKeyFromBytes(receivedPublickKeyByte);
            if (receivedPublickKey == null) {
                Log.e(TAG, "Failed to get public key from bytes");
                return null;
            }
            keyAgreement.doPhase(receivedPublickKey, true);
            //生成交换ext_key
            byte data[] = keyAgreement.generateSecret();
            return data;
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    /***
     * 根据接受的public key 生成 PublicKey
     * @param pubKey
     * @return
     */
    private PublicKey getPublicKeyFromBytes(byte[] pubKey) {
        ECPublicKey pk = null;
        try {
            ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256r1");
            KeyFactory kf = KeyFactory.getInstance("EC");
            ECNamedCurveSpec params = new ECNamedCurveSpec("secp256r1", spec.getCurve(), spec.getG(), spec.getN());
            byte pbk[] = new byte[65];
            pbk[0] = 0x04;
            System.arraycopy(pubKey, 0, pbk, 1, 64);
            ECPoint point = ECPointUtil.decodePoint(params.getCurve(), pbk);
            ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, params);
            pk = (ECPublicKey) kf.generatePublic(pubKeySpec);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }
        return pk;
    }

    /***
     * 获取公钥
     * @return
     */
    private byte[] getPublicKeyByte() {
        byte encodeData[] = mECPublicKey.getEncoded();
        byte publicKey[] = new byte[64];
        System.arraycopy(encodeData, 27, publicKey, 0, 64);
        return publicKey;
    }
}
