package cn.bulaomeng.fragment.restTmplate;

import cn.bulaomeng.fragment.entity.User;
import cn.bulaomeng.fragment.util.AESPKCS7Util;
import cn.bulaomeng.fragment.util.DeCodeUtil;
import cn.bulaomeng.fragment.util.RSAUtil;
import com.alibaba.fastjson.JSONObject;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.web.client.RestTemplate;

import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.*;

/**
* @Description: 新中新二维码配置
* @Param:  
* @return:  
* @Author: tjy
* @Date: 2019/9/20 
*/ 
public class XZXDeCode {
    public static void main(String[] args) {
        //公钥
       String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAObmFl6JiP3ea9SUJtROpPvdmKjJGG0l2YgLZN3HO0pa3g4kqBYs6IAN9Zjil6K92D3Siq55ujkqESjE31+fNUMCAwEAAQ==";
        //二维码
        String code = "RFT://k2n/+6HCVbDCpgEsA+oqCbpqYikBZ39qnq1AG0m9zfhVHK7itN+1h6rlJzb1Zyf067lfnVohYRKkVnVasUzm/A==|ionGGcU+zaOgkeP8mqlGrVXUtydb35qLh08+mTI/zZM=";
        //配置接口协议头
        String protocolHeader = "RFT://";
        System.out.println("===========================签名============================");
        System.out.println(deCode(publicKey,code,protocolHeader));
    }

    /** 
    * @Description: 签名算法相关
    * @Param: [] 
    * @return: void 
    * @Author: tjy
    * @Date: 2019/9/20 
    */ 
    public static void sign(){
        /*
        APPID：LibraryAccessControl
        统一终端编号：DSF00002
        本地安全通讯秘钥：ryAJCLlelunLLb02rt4V7xUaH6ScZn1l
        安全服务（即第三方安全接入）的接口服务器：60.205.182.0:9000
        */
        //1.签名算法加密
        SortedMap<Object,Object> parameters = new TreeMap<Object,Object>();
        //String appId="LibraryAccessControl";
        String appId="rft123456";
        String itemNo="10000100";
        String outTradeNo=" ";
        String body="test";
        String nonceStr="ibuaiVcKdpRxkhJA";
        //需要提供
        //String key="ryAJCLlelunLLb02rt4V7xUaH6ScZn1l";
        String key="192006250b4c09247ec02edce69f6a2d";
        parameters.put("appId", appId);
        parameters.put("itemNo", itemNo);
        parameters.put("outTradeNo",outTradeNo);
        parameters.put("body",body);
        parameters.put("nonceStr",nonceStr);
        String mySign = DeCodeUtil.createSign(parameters,key);
        System.out.println("我 的签名是："+mySign);
        System.out.println("========================分隔线=============================");
        //2.生成随机数
        String uuid = UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");
        System.out.println(uuid);
        //签名
      /*  Map<String,Object> map = new HashMap<>();
        map.put("data",appId);
        map.put("nonceStr",uuid);
        map.put("localAuthSign",mySign.get("sign"));*/
        //goSign(map);
       /* Map<String,Object> signMap = new HashMap<>();
        signMap.put("appId",appId);
        signMap.put("nonceStr","5K8264ILTKCH16CQ2502SI8ZNMTM67VS");
        signMap.put("appSign",mySign.get("sign"));
        getCodeCofing(signMap);*/
    }
    //签到
    public  static JSONObject goSign (Map<String,Object> map){
        RestTemplate restTemplate = new RestTemplate();
        String data=restTemplate.postForObject("http://demo.greatge.net:28981/sign/createappsign",map,String.class);
        System.out.println("签名:---"+data);
        return JSONObject.parseObject(data);
    }
    //获得二维码配置接口
    public  static  JSONObject getCodeCofing(Map<String,Object> map){
        RestTemplate restTemplate = new RestTemplate();
        String data=restTemplate.postForObject("http://demo.greatge.net:28981/code/getqrcodeconfig",map,String.class);
        System.out.println(data);
        return null;
    }
    /** 
    * @Description: 解密 
    * @Param: [] 
    * @return: cn.bulaomeng.fragment.entity.User 
    * @Author: tjy
    * @Date: 2019/9/20 
    */ 
    public static User deCode(String publicKey,String code,String protocolHeader){

        //1.根据配置接口获取的协议头判断是否系统发放的码。
        String isHeader = code.substring(0,protocolHeader.length());
        //判断是否与配置接口获得的协议头吻合
        if(protocolHeader.equals(isHeader)){
            System.out.println(protocolHeader+" --> "+isHeader+" 匹配协议头");
            //2.去除协议头
            code = code.substring(isHeader.length());

            //3.使用竖线“|”分割字符串，得到第一部分字符串为二维码编码字符串。得到第二部分为展示时间和其它数据的字符串（以下简称时间字符串）。二维码字符串使用RSA算法解密，时间字符串使用AES算法解密。
            String splitCode[] = code.split("\\|");

/*

            4.使用AES算法解密“时间字符串”，密钥固定为“GreatgeRonfton81”算法使用“AES/CBC/PKCS7Padding”，向量为密钥的前16byte。解密后得到一个逗号分割的字符串，如：9,100000,7000,0,103717。最后一部分“103717”
            为二维码在小程序上展示的时间，通过与当前的时间进行比对，如果两者相差超过二维码配置接口返回的“displayLimitTime”的秒数，则认为二维码已经失效，程序应该提示用户二维码已经失效
*/

            String deCodeDate;
            try {
                //密钥
                String sessionKey = "GreatgeRonfton81";
                //向量
                String iv = "GreatgeRonfton81";
                Base64.Encoder encoder = Base64.getEncoder();
                String baseSessionKey = encoder.encodeToString(sessionKey.getBytes());
                String baseIv = encoder.encodeToString(iv.getBytes());
                Security.addProvider(new BouncyCastleProvider());
                //通过 AESPKCS7 解密字符串
                deCodeDate = AESPKCS7Util.decrypt(splitCode[1],baseSessionKey,baseIv);
                //根据逗号分隔时间字符串
                String getSpStr[] = deCodeDate.split(",");
                //获得截取时间字符串中最后的时间字段
                String codeDate = getSpStr[getSpStr.length-1];
                //获取当前时间时分秒，与截取的时间做差比较如果大于所规定时间为二维码超时
                SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
                String currentDate = sdf.format(new Date());
                //当前时间 - 二维码生成时间
                Integer endDate = Integer.parseInt(currentDate) - Integer.parseInt(codeDate);
                //按照配置接口返回的参数 规定大于65s为超时需要给出提示过期
                if(endDate>=65){
                    System.out.println("二维码已过期！");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //5.根据当前时间获取当前周期，进而获取当前周期的公钥

            //6.加载公钥，初始化环境。密钥长度为512bit，使用的padding为“RSA/ECB/PKCS1Padding”

            //7.对二维码字符串进行base64解码，得到byte数组。
            String byteStr = splitCode[0];
            //解码这步在第8 RSAUtil.publicDecrypt 工具类中实现

          /*  byte decode[] = Base64.decodeBase64(byteStr.getBytes());
            System.out.println(byteStr + "\t字符串解码后为：" + decode);*/


          /*
            8.使用公钥对byte数组解密，得到解密后的byte数组。如果解密失败，则根据风控允许的误差时间（参考配置接口获取）
            加载前一周期或者下一周期的公钥再尝试解密。注意，如果base64解码后得到的byte数组的比特长度超过密钥的长度时，需要分片进行解密，然后再顺序拼接成一个byte数组*/

            try {
                byte[] deByteArr = RSAUtil.publicDecrypt(byteStr,publicKey);
                String deStr = new String(deByteArr,"UTF-8");
                //9.把解密后的byte数组使用UTF_8编码转换为字符串。使用逗号分割符分割字符串为四部分，分别得到学工号、姓名、性别和字符串编码表示的二进制内容。
                String deSplitStr[] = deStr.split(",");
                //10.把字符串编码表示的二进制内容使用ISO_8859_1编码转换为byte数组，按照下面的分割方式获取对应的数据
                byte isoByteStr[] =deSplitStr [deSplitStr.length-1].getBytes("ISO_8859_1");
                User user =new User();
                user.setCheckCode(DeCodeUtil.bytesChar(DeCodeUtil.subBytes(isoByteStr,isoByteStr.length-5,5))); //存储解密后的校验码
                user.setUserName(deSplitStr[1]);//存储用户名称
                user.setUserNo(deSplitStr[0]);//存储学工号
                //把学工号使用UTF_8编码转换为byte数组。
                byte userNo[] = deSplitStr[0].getBytes("UTF-8");
                //把姓名使用UTF_8编码转换为byte数组。
                byte name[] = deSplitStr[1].getBytes("UTF-8");
                //把性别使用UTF_8编码转换为byte数组。
                byte sex[] = deSplitStr[2].getBytes("UTF-8");
                //除校验码的二进制内容
                byte all[] = DeCodeUtil.subBytes(isoByteStr,0,isoByteStr.length-5);
                //把byte数据按照学工号、姓名、性别和第9步的列表中除去校验码外的数据按照顺序拼接为一个byte数组。使用md5算法得到十六进编码的字符串，取最后5个字符，再使用UTF_8编码转换为5个字节的byte数组。
                byte[] byteResult = new byte[userNo.length+name.length+sex.length+all.length];
                System.arraycopy(userNo,0,byteResult,0,userNo.length);
                System.arraycopy(name,0,byteResult,userNo.length,name.length);
                System.arraycopy(sex,0,byteResult,userNo.length+name.length,sex.length);
                System.arraycopy(all,0,byteResult,userNo.length+name.length+sex.length,all.length);
                //比较解码得到的校验码和计算得到的校验码是否相同，确定解码的内容是否有效。
                String md5Str1 = DeCodeUtil.md5Encrypt3(new String(byteResult,"UTF-8"));
                byte md5StrSp[] = md5Str1.substring(md5Str1.length()-5).getBytes("UTF-8");
                user.setLinkCheckCode(DeCodeUtil.bytesChar(md5StrSp)); //截取后的校验码
                if(user.getCheckCode().equals(user.getLinkCheckCode())){
                    System.out.println("匹配校验码");
                }else {
                    return null;
                }
                return user;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return null;
    }
    
}
