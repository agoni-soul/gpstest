package com.midea.iot.msmart.common.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * <h1>工具类</h1>
 * <p>一些工具方法。
 *
 * @author <a href="http://msmart.midea.com">美的智慧家居科技有限公司</a>
 * @date 2016/7/4
 */
public class Util {
    private final static String TAG = "Util";

    private final static String HEX_UC = "0123456789ABCDEF";
    private final static String HEX_LC = "0123456789abcdef";

    /**
     * 匹配正则表达式.
     *
     * @param regex 正则表达式
     * @param str   带匹配字符串
     * @return 是否匹配
     */
    public static boolean match(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    /**
     * Convert byte to upper-case hex string
     *
     * @param src byte
     * @return the hex string
     */
    public static String byteToUcHexString(byte src) {
        return String.format("%s%s", HEX_UC.charAt((src >> 4) & 0x0F), HEX_UC.charAt(src & 0x0F));
    }

    /**
     * 将byte转换程16进制字符串.
     * 返回值不带0x
     *
     * @param src 待转换字节
     * @return 16进制字符串
     */
    public static String byteToHexString(byte src) {
        return byteToUcHexString(src);
    }


    public static String bytesToLcHexString(byte[] src) {
        if (src == null)
            return null;
        StringBuilder sb = new StringBuilder(2 * src.length);
        for (byte data : src) {
            sb.append(byteToLcHexString(data));
        }
        return sb.toString();
    }
    public static  String byteToLcHexString(byte src) {
        return String.format("%s%s", HEX_LC.charAt((src >> 4) & 0x0F), HEX_LC.charAt(src & 0x0F));
    }
    /**
     * Convert byte[] to upper-case hex string
     *
     * @param src byte
     * @return the hex string
     */
    public static String bytesToUcHexString(byte[] src) {
        if (src == null || src.length == 0) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder(2 * src.length);
            for (byte data : src) {
                sb.append(byteToUcHexString(data));
            }
            return sb.toString();
        }
    }

    /**
     * 将byte数组转换成16进制字符串.
     * 每个16进制不带0x
     */
    public static String bytesToHexString(byte[] src) {
        return bytesToUcHexString(src);
    }

    /**
     * 将字节数组转换成16进制字符串.
     * 返回值不带0x
     *
     * @param src 待转换的byte数组
     * @return 16进制字符串
     */
    public static String bytesToSpaceHexString(byte[] src) {
        if (src == null || src.length <= 0) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder("");
        for (int i = 0; i < src.length; i++) {
            String hv = byteToUcHexString(src[i]);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append("[").append(i).append("]=").append(hv).append("  ");
        }
        return stringBuilder.toString();
    }

    /**
     * 把16进制字符串转成byte数组.
     *
     * @param hexString 16进制字符串
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (TextUtils.isEmpty(hexString)) {
            return null;
        } else {
            try {
                int len = hexString.length() / 2;
                byte[] bytes = new byte[len];
                for (int i = 0; i < len; i++) {
                    bytes[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16)
                            .byteValue();
                }
                return bytes;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Short To Int
     *
     * @param data short
     */
    public static int shortToInt(short data) {
        return data & 0xFF;
    }

    /**
     * long类型转成byte数组.
     * 小端模式。
     *
     * @param number 待转换数字
     * @return byte[]
     */
    public static byte[] longToBytes(long number) {
        long temp = number;
        byte[] b = new byte[8];
        for (int i = 0; i < b.length; i++) {
            b[i] = Long.valueOf(temp & 0xFF).byteValue();
            temp = temp >> 8;
        }
        return b;
    }

    /**
     * byte数组转成long
     *
     * @param bytes 待转换的byte数组
     * @return long
     */
    public static long bytesToLong(byte[] bytes) {
        if (bytes == null) {
            return 0;
        } else {
            long ret = 0;
            for (int i = 0; i < 8; i++) {
                long temp = 0;
                if (i < bytes.length) {
                    temp = bytes[i] & 0xFF;
                }
                ret |= temp << (i * 8);
            }
            return ret;
        }
    }

    /**
     * int类型转换成字节数组.
     * 小端模式。
     *
     * @param number 待转换数字
     * @return byte[]
     */
    public static byte[] intToBytes(int number) {
        int temp = number;
        byte[] b = new byte[4];
        for (int i = 0; i < b.length; i++) {
            b[i] = Integer.valueOf(temp & 0xFF).byteValue();
            temp = temp >> 8;
        }
        return b;
    }

    /**
     * 将字节数组转换程int.
     *
     * @param bytes 待转换的byte数组
     * @return int
     */
    public static int bytesToInt(byte[] bytes) {
        if (bytes == null) {
            return 0;
        } else {
            int ret = 0;
            for (int i = 0; i < 4; i++) {
                int temp = 0;
                if (i < bytes.length) {
                    temp = bytes[i] & 0xFF;
                }
                ret |= temp << (i * 8);
            }
            return ret;
        }
    }

    /**
     * short类型转换成字节数组.
     * 小端模式。
     *
     * @param number 待转换数字
     * @return byte[]
     */
    public static byte[] shortToBytes(short number) {
        short temp = number;
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            b[i] = Short.valueOf((short) (temp & 0xFF)).byteValue();
            temp = (short) (temp >> 8);
        }
        return b;
    }

    /**
     * 将字节数组转换程short.
     *
     * @param bytes 待转换的byte数组
     * @return short
     */
    public static short byteToShort(byte[] bytes) {
        if (bytes == null) {
            return 0;
        } else {
            short ret = 0;
            for (int i = 0; i < 2; i++) {
                short temp = 0;
                if (i < bytes.length) {
                    temp = (short) (bytes[i] & 0xFF);
                }
                ret |= temp << (i * 8);
            }
            return ret;
        }
    }

    /***
     * byte 数组逆向
     * @param data
     * @return
     */
    public static  byte[] byteArrayReverse(byte[] data) {
        byte[] reverseArray = new byte[data.length];
        ArrayList arraylist = new ArrayList();
        for (int i = 0; i < data.length; i++) {
            arraylist.add(data[i]); //存放元素
        }
        Collections.reverse(arraylist); //使用方法进行逆序
        //完成逆序后,可以保存到新数组reverseArray
        for (int i = 0; i < data.length; i++) {
            reverseArray[i] = (byte) arraylist.get(i);
        }
        return reverseArray;
    }


    /**
     * 16进制字符串转10进制字符串
     *
     * @param hexStr 16进制字符串
     * @return 十进制字符串
     */
    public static String hexToDecString(String hexStr) {
        byte[] bytes = hexStringToBytes(hexStr);
        return String.valueOf(bytesToLong(bytes));
    }

    /**
     * 10进制字符串转16进制字符串
     *
     * @param decStr 10进制字符串
     * @return 16进制字符串
     */
    public static String decToHexString(String decStr) {
        long nValue = Long.parseLong(decStr);
        return bytesToHexString(longToBytes(nValue));
    }


    /**
     * 10进制字符串转字节数组（字符串用“，”分隔）
     *
     * @param decStr 10进制字符串
     * @return byte[]
     */
    public static byte[] decStringToBytes(String decStr) {
        if (decStr == null) {
            return null;
        }
        String[] szDec = decStr.split(",");
        byte[] bytes = new byte[szDec.length];
        for (int nIndex = 0; nIndex < szDec.length; nIndex++) {
            try {
                bytes[nIndex] = (byte) (Integer.parseInt(szDec[nIndex]));
            } catch (Exception ex) {
                LogUtils.i(TAG, "decStringToBytes()," + ex.getMessage());
            }

        }
        return bytes;
    }

    public static byte[] hexStringWithSplitToBytes(String decStr, String splitChar) {
        if (decStr == null) {
            return null;
        }
        String[] szDec = decStr.split(splitChar);
        byte[] bytes = new byte[szDec.length];
        for (int nIndex = 0; nIndex < szDec.length; nIndex++) {
            try {
                bytes[nIndex] = (byte) (Integer.parseInt(szDec[nIndex]));
            } catch (Exception ex) {
                LogUtils.i("decStringToBytes", ex.getMessage());
            }

        }
        return bytes;
    }

    /**
     * 字节数组转10进制字符串（字符串用“，”分隔）
     *
     * @param bytes byte数组
     * @return 字符串
     */
    public static String bytesToDecString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        } else {
            StringBuilder resultBuilder = new StringBuilder();
            for (byte data : bytes) {
                resultBuilder.append(data).append(",");
            }
            String result = resultBuilder.toString();
            return result.substring(0, result.length() - 1);
        }
    }

    /**
     * 字节数组转10进制无符号字符串（字符串用“，”分隔）
     *
     * @param bytes byte数组
     * @return 字符串
     */
    public static String bytesToOXString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        } else {
            StringBuilder resultBuilder = new StringBuilder();
            for (byte data : bytes) {
                resultBuilder.append(data & 0xff).append(",");
            }
            String result = resultBuilder.toString();
            return result.substring(0, result.length() - 1);
        }
    }

    /**
     * 从WifiInfo中获取SSID，由于4.0以上会增加“” 所以要去掉
     *
     * @param info WifiInfo
     * @return ssid
     */
    public static String getSSIDFromWifiInfo(WifiInfo info) {
        if (info == null) {
            return "";
        }
        String ssid = info.getSSID();
        if (Build.VERSION.SDK_INT >= 17) {
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
        }
        return ssid;
    }

    /**
     * 将Json转换成Map.
     * 只能解析一维JSON.
     *
     * @param json json的数据
     * @return Map
     */
    public static Map<String, Object> jsonToMap(String json) throws JSONException {
        if (TextUtils.isEmpty(json)) {
            throw new IllegalArgumentException("Json is empty");
        }
        JSONObject jsonObject = new JSONObject(json);
        return jsonToMap(jsonObject);
    }

    /**
     * 将Json转换成Map.
     * 只能解析一维JSON.
     *
     * @param jsonObject JSON对象
     * @return Map
     */
    public static Map<String, Object> jsonToMap(JSONObject jsonObject) throws JSONException {
        if (jsonObject == null) {
            throw new IllegalArgumentException("Json object is null");
        }
        Map<String, Object> resultMap = new HashMap<>();
        Iterator it = jsonObject.keys();
        while (it.hasNext()) {
            String key = String.valueOf(it.next());
            Object value = jsonObject.get(key);
            resultMap.put(key, value);
        }
        return resultMap;
    }

    /**
     * 把JSON转换成由&符号连接的键值对字符串
     *
     * @param jsonObject JSON对象
     * @return 字符串
     * @throws JSONException
     */
    public static String jsonObjectToAppendString(JSONObject jsonObject) throws JSONException {
        if (jsonObject == null) {
            throw new IllegalArgumentException("Json object is null");
        }
        Iterator<String> it = jsonObject.keys();
        StringBuilder paramsStrBuilder = new StringBuilder();
        while (it.hasNext()) {
            String key = it.next();
            Object value = jsonObject.get(key);
            String valueStr = value == null ? "" : value.toString();
            paramsStrBuilder.append(key).append("=").append(valueStr).append("&");
        }
        String paramsStr = paramsStrBuilder.toString();
        return paramsStr.substring(0, paramsStrBuilder.length() - 1);
    }

    /**
     * 把MAP转换成Key=Value形式的字符串，由指定分隔符分割.
     * 默认分隔符是空格
     *
     * @param mapData   MAP
     * @param separator 分隔符
     * @return 转换的字符串
     */
    public static String mapToKeyValueStr(Map<String, Object> mapData, String separator) {
        if (mapData == null || mapData.size() == 0) {
            return null;
        }
        separator = TextUtils.isEmpty(separator) ? " " : separator;
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : mapData.keySet()) {
            Object value = mapData.get(key);
            String valueStr = value == null ? "" : value.toString();
            stringBuilder.append(key).append("=").append(valueStr).append(separator);
        }
        String KeyValueStr = stringBuilder.toString();
        return KeyValueStr.substring(0, KeyValueStr.length() - 1);
    }

    /**
     * 将字符串形式的MAC 转换成 byte数组
     *
     * @param mac -"xx:xx:xx:xx:xx:xx"
     * @return byte数组
     */
    public static byte[] getMacBytes(String mac) {
        byte[] macBytes = new byte[6];
        String[] strArr = mac.split(":");

        for (int i = 0; i < strArr.length; i++) {
            int value = 0;
            try {
                value = Integer.parseInt(strArr[i], 16);
            }catch (Exception e){
                e.printStackTrace();
            }
            macBytes[i] = (byte) value;
        }
        return macBytes;
    }

    /**
     * 将MAC
     *
     * @param mac
     * @return
     */
    public static String getMacStr(byte[] mac) {
        if (mac != null && mac.length == 6) {
            return String.format("%s:%s:%s:%s:%s:%s",
                    byteToHexString(mac[0]),
                    byteToHexString(mac[1]),
                    byteToHexString(mac[2]),
                    byteToHexString(mac[3]),
                    byteToHexString(mac[4]),
                    byteToHexString(mac[5]));
        }
        return null;
    }

    public static byte[] getIPBytes(String ipAddress) {
        byte[] ipBytes = new byte[4];
        String[] strArr = ipAddress.split(".");
        for (int i = 0; i < strArr.length; i++) {
            int value = Integer.parseInt(strArr[i], 10);
            ipBytes[i] = (byte) value;
        }
        return ipBytes;
    }


    /**
     * BCD码转换成字符串
     *
     * @param bytes -BCD
     * @return 转换后的字符串
     */
    public static String bcd2Str(byte[] bytes) {
        char temp[] = new char[bytes.length * 2], val;

        for (int i = 0; i < bytes.length; i++) {
            val = (char) (((bytes[i] & 0xf0) >> 4) & 0x0f);
            temp[i * 2] = (char) (val > 9 ? val + 'A' - 10 : val + '0');

            val = (char) (bytes[i] & 0x0f);
            temp[i * 2 + 1] = (char) (val > 9 ? val + 'A' - 10 : val + '0');
        }
        return new String(temp);
    }

    /**
     * 将int[] 转换成16进制字符串
     *
     * @param array -数组
     * @return 字符串
     */
    public static String intArrayToHexString(int[] array) {
        String str = "";
        for (int i : array) {
            if (i < 0x10) {
                str += "0" + Integer.toHexString(i);
            } else {
                str += Integer.toHexString(i);
            }
        }
        return str;
    }

    public static String bundleToKeyString(Bundle bundle) {
        if (bundle != null && bundle.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            for (String key : bundle.keySet()) {
                sb.append(key).append(":").append(bundle.get(key)).append(",");
            }
            sb.append("}");
            return sb.toString();
        }
        return "";
    }


    /**
     * 使用zip进行压缩
     *
     * @param rawStr 压缩前的文本
     * @return 返回压缩后的文本
     */
    public static String zip(String rawStr) {
        if (rawStr == null || rawStr.length() == 0) {
            return null;
        }
        ByteArrayOutputStream out = null;
        ZipOutputStream zOut = null;
        try {
            out = new ByteArrayOutputStream();
            zOut = new ZipOutputStream(out);
            zOut.putNextEntry(new ZipEntry("aa"));
            zOut.write(rawStr.getBytes());
            zOut.closeEntry();
            return out.toString("ISO-8859-1");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (zOut != null) {
                try {
                    zOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 使用zip进行解压缩
     *
     * @param compressedStr 压缩后的文本
     * @return 解压后的字符串
     */
    public static String unzip(String compressedStr) {
        if (compressedStr == null || compressedStr.length() == 0) {
            return null;
        }

        ByteArrayOutputStream out = null;
        ByteArrayInputStream in = null;
        ZipInputStream zin = null;
        try {
            byte[] compressed = compressedStr.getBytes("ISO-8859-1");
            out = new ByteArrayOutputStream();
            in = new ByteArrayInputStream(compressed);
            zin = new ZipInputStream(in);
            zin.getNextEntry();
            byte[] buffer = new byte[1024];
            int offset = -1;
            while ((offset = zin.read(buffer)) != -1) {
                out.write(buffer, 0, offset);
            }
            return out.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (zin != null) {
                try {
                    zin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static byte[] intArrayToByteArray(int[] array) {
        byte[] data = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            data[i] = (byte) array[i];
        }
        return data;
    }

    /**
     * 创建美的设备的默认热点名称.
     * 仅限于美的设备。
     *
     * @param devSN   设备SN
     * @param devType 设备类型
     * @return 设备默认规则的热点
     */
    public static String createDeviceSSID(String devSN, String devType) {
        if (TextUtils.isEmpty(devSN) || TextUtils.isEmpty(devType)) {
            throw new IllegalArgumentException("Device sn and type is null!");
        }
        int length = devSN.length();
        return String.format("midea_%s_%s", devType.replace("0x", ""), devSN.substring(length - 8, length - 4));
    }

    /**
     * 二进制字符串转byte
     */
    public static byte decodeBinaryString(String byteStr) {
        int re, len;
        if (null == byteStr) {
            return 0;
        }
        len = byteStr.length();
        if (len != 4 && len != 8) {
            return 0;
        }
        if (len == 8) {// 8 bit处理
            if (byteStr.charAt(0) == '0') {// 正数
                re = Integer.parseInt(byteStr, 2);
            } else {// 负数
                re = Integer.parseInt(byteStr, 2) - 256;
            }
        } else {// 4 bit处理
            re = Integer.parseInt(byteStr, 2);
        }
        return (byte) re;
    }

    /**
     * byte转二进制字符
     */

    public static String byteToBit(byte b) {
        return ""
                + (byte) ((b >> 7) & 0x1) + (byte) ((b >> 6) & 0x1)
                + (byte) ((b >> 5) & 0x1) + (byte) ((b >> 4) & 0x1)
                + (byte) ((b >> 3) & 0x1) + (byte) ((b >> 2) & 0x1)
                + (byte) ((b >> 1) & 0x1) + (byte) ((b >> 0) & 0x1);
    }

    /**
     * 字符串转换成十六进制字符串
     */
    public static String str2HexStr(String str) {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
        }
        return sb.toString().trim();
    }

    public static boolean isNetworkAvailable(Context context) {
        if (context == null) return false;

        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isAvailable();
    }

    public static boolean isHaveNetwork(Context context) {
        if (context == null) return false;

        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        return networkInfo != null ;
    }

    /**
     * 根据IP地址获取MAC地址
     *
     * @return
     */
    public static String getLocalMacAddressFromIp() {
        String strMacAddr = null;
        try {
            //获得IpD地址
            InetAddress ip = getLocalInetAddress();
            byte[] b = NetworkInterface.getByInetAddress(ip).getHardwareAddress();
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < b.length; i++) {
                if (i != 0) {
                    buffer.append(':');
                }
                String str = Integer.toHexString(b[i] & 0xFF);
                buffer.append(str.length() == 1 ? 0 + str : str);
            }
            strMacAddr = buffer.toString().toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e("get localmac failed");
        }
        LogUtils.e("get localmac strMacAddr = "+strMacAddr);
        return strMacAddr;
    }

    /**
     * 获取移动设备本地IP
     *
     * @return
     */
    private static InetAddress getLocalInetAddress() {
        InetAddress ip = null;
        try {
            //列举
            Enumeration<NetworkInterface> en_netInterface = NetworkInterface.getNetworkInterfaces();
            while (en_netInterface.hasMoreElements()) {//是否还有元素
                NetworkInterface ni = (NetworkInterface) en_netInterface.nextElement();//得到下一个元素
                Enumeration<InetAddress> en_ip = ni.getInetAddresses();//得到一个ip地址的列举
                while (en_ip.hasMoreElements()) {
                    ip = en_ip.nextElement();
                    if (!ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1)
                        break;
                    else
                        ip = null;
                }

                if (ip != null) {
                    break;
                }
            }
        } catch (SocketException e) {
            LogUtils.e("get local ip failed");
            e.printStackTrace();
        }
        return ip;
    }



    public boolean stringIsEmpty(String string) {
        return false;
    }
}