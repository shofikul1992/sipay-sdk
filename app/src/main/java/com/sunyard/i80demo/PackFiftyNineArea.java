package com.sunyard.i80demo;

import android.util.Log;

import com.socsi.smartposapi.serialnumber.SerialNumberOperation;
import com.socsi.smartposapi.serialnumber.TerminalSerialNumberInfo;
import com.socsi.utils.StringUtil;
import com.sunyard.i80demo.util.HexUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class PackFiftyNineArea {

    private final String TAG = PackFiftyNineArea.class.getSimpleName();
    private static PackFiftyNineArea instance;

    private String thirdArea = "I8000";//入网证号

    private String deviceType = "04";//设备类型

    private String fiveArea = "";//终端序列号

//    private String appVersion = "01001001";//应用程序版本号
    private String appVersion = "320006  ";//应用程序版本号
    private String random = "";//随机数
    private String sn = "";//sn
    private String encrypSn = "";//密文sn

    public static PackFiftyNineArea getInstance() {
        if (instance == null) {
            instance = new PackFiftyNineArea();
        }
        return instance;
    }

    public String pack59(String cardNo) {
        clearSnInfo();
        String temp = cardNo.substring(cardNo.length() - 6);
        random = StringUtil.byte2HexStr(temp.getBytes());
        getRandomAndEnSn();

        String info59 = "";
        Map<String, String> map = new HashMap();
        map.put("01", deviceType);  //设备类型
        map.put("02", sn);          //终端硬件序列号

        if (!StringUtil.isEmpty(random) && !StringUtil.isEmpty(encrypSn)) {
            map.put("03", temp);  //加密随机因子
            map.put("04", new String(com.socsi.utils.StringUtil.hexStr2Bytes(encrypSn)));          //硬件序列号密文数据
        }
        map.put("05", appVersion);          //应用程序版本号
        String body = "";
        //直接取tag 01-05
        for (int i = 1; i <= 5; i++) {
            String key = "0" + i;
            if (map.containsKey(key)) {
                String value = map.get(key);
                body += key;
                body += String.format(Locale.getDefault(), "%03d", value.length());
                body += value;
            }
        }

        info59 += "A2";
        info59 += String.format(Locale.getDefault(), "%03d", body.length());
        info59 += body;
        map.clear();

        return StringUtil.byte2HexStr(info59.getBytes());
    }

//    public String pack59() {
//        clearSnInfo();
//        getRandomAndEnSn();
//
//        String info59 = "";
//        Map<String, Pair<Integer, byte[]>> map = new HashMap();
//        Tlv tlvChild = new TagStringASCValue();
//        tlvChild.setTag("01").setValue(deviceType).put(map);    //设备类型
//        tlvChild.setTag("02").setValue(sn).put(map);            //终端硬件序列号
//        byte[] body = TlvUtil.encodingTLV(map);
//        Log.e("xx", "普通子域:" + TlvUtil.printLogStr(body));
//        info59 += StringUtil.byte2HexStr(body);
//        map.clear();
//
//        if (!StringUtil.isEmpty(random) && !StringUtil.isEmpty(encrypSn)) {
//            Tlv tlvSnInfo = new TagStringBCDValue();
//            tlvSnInfo.setTag("03").setValue(random).put(map);   //加密随机因子
//            tlvSnInfo.setTag("04").setValue(encrypSn).put(map); //硬件序列号密文数据
//            body = TlvUtil.encodingTLV(map);
//            Log.d(TAG, "sn 加密域子域:" + TlvUtil.printLogStr(body));
//            info59 += StringUtil.byte2HexStr(body);
//            map.clear();
//        }
//        Tlv tlvChild2 = new TagStringASCValue();
//        tlvChild2.setTag("05").setValue(appVersion).put(map);    //应用程序版本号
//        body = TlvUtil.encodingTLV(map);
//        Log.e("xx", "普通子域2:" + TlvUtil.printLogStr(body));
//        info59 += StringUtil.byte2HexStr(body);
//        map.clear();
//
//        int innerLen = info59.length() / 2;
//        String temp = "";
//        if (innerLen < 100) {
//            temp = "0" + innerLen;
//        }
//        temp = "A2" + temp;
//        String resultString = "";
//        resultString = StringUtil.byte2HexStr(temp.getBytes());
//        resultString += info59;
//        return resultString;
//    }

    /**
     * 清除sn加密所需的变量
     */
    private void clearSnInfo() {
        sn = "";
        encrypSn = "";
        random = "";
    }

    /**
     * 获取随机数以及sn密文等信息
     *
     * @return
     */
    private boolean getRandomAndEnSn() {
        try {
            if (!getEnSNRandom()) {
                return false;
            }
            //test
//			if(!loadSnEnKey(sn,"11111111111111111111111111111111")){
//				return false;
//			}
//			if(!loadSnEnKey(sn,"88888888888888888888888888888888")){
//				return false;
//			}
            encrypSn = getEncryptedSn(random);
            Log.d(TAG, "encrypSn:" + encrypSn);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 得到sn和随机数
     *
     * @return
     */
    private boolean getEnSNRandom() {
        TerminalSerialNumberInfo snInfo = new TerminalSerialNumberInfo();
        try {
            snInfo = SerialNumberOperation.getInstance().queryTerminalSn(SerialNumberOperation.TERMINAL_MODEL_I80, (byte) 6);
            if (snInfo != null) {
//                random = getRandomNum();
                sn = snInfo.getSN();
//                Log.d(TAG, "random:" + random);
                Log.d(TAG, "sn:" + sn);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

//	/**
//	 * 导入sn加密明文
//	 * @param SN
//	 * @param snKey
//	 * @return
//	 */
//	private boolean loadSnEnKey(String SN ,String snKey){
//		try {
//			return SerialNumberOperation.getInstance().loadEnctyptSnKey(SerialNumberOperation.TERMINAL_MODEL_I80,
//					 SerialNumberOperation.ALGORITHM_IDENTIFIES_SM4, SN.getBytes(), HexUtil.hexStringToByte(snKey));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return false;
//	}

    /**
     * 通过随机数获得密文sn
     *
     * @param random
     * @return
     */
    private String getEncryptedSn(String random) {
        byte[] res = null;
        try {
            res = SerialNumberOperation.getInstance().getEncryptedSn(SerialNumberOperation.TERMINAL_MODEL_I80,
                    SerialNumberOperation.ALGORITHM_IDENTIFIES_SM4, HexUtil.hexStringToByte(random));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (res != null) {
            return StringUtil.bytesToHexString(res);
        }
        return "";
    }

    /**
     * 获取16进制格式的随机数字符串
     *
     * @return
     */
    private String getRandomNum() {
        String randomNum = "";
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            randomNum += ("3" + random.nextInt(10));
        }
        return randomNum;
    }
}
