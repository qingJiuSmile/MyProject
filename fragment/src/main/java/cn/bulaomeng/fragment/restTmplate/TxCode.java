package cn.bulaomeng.fragment.restTmplate;

import cn.bulaomeng.fragment.config.HttpsClientRequestFactory;
import cn.bulaomeng.fragment.entity.TxwxKeySecret;
import cn.bulaomeng.fragment.util.DeCodeUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.client.RestTemplate;


import java.util.*;

public class TxCode {
    public static void main(String[] args) {
        //2.生成随机数
        String random = DeCodeUtil.getGUID();
        System.out.println(random);
        String uuid = UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");
  /*      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date =new Date();
        String currentDate = sdf.format(date);*/
        String app_key = "0FE8C24D97E240CD"; //服务商id
        String signature; //签名
        String timestamp = String.valueOf(System.currentTimeMillis()/1000) ; //当前时间戳
        String nonce = uuid;   //随机字符串
        System.out.println(nonce);
        String school_code = "wxcampus"; //学校代码
        String key="59C563A47F92D78076B71FE29F1BA209";

        //签名算法
        SortedMap<Object,Object> parameters = new TreeMap<Object,Object>();
        parameters.put("app_key", app_key);
        parameters.put("timestamp",timestamp);
        parameters.put("nonce",nonce);
        parameters.put("school_code",school_code);
        signature = DeCodeUtil.createSign(parameters,key);
        System.out.println("我的签名是："+signature);

        Map<String,Object> map = new HashMap<>();
        System.out.println(timestamp);
        map.put("app_key",app_key);
        map.put("timestamp",timestamp);
        map.put("nonce",nonce);
        map.put("school_code",school_code);
        map.put("signature",signature);
        RestTemplate restTemplate = new RestTemplate(new HttpsClientRequestFactory());
        JSONObject js = restTemplate.getForObject("https://weixiao.qq.com/apps/school-api/key-secret?app_key={app_key}&timestamp={timestamp}&nonce={nonce}&school_code={school_code}&signature={signature}",JSONObject.class,map);
        System.out.println(js);
        TxwxKeySecret wxKeySecret = new TxwxKeySecret();
        wxKeySecret.setSecret(js.getString("secret"));
        wxKeySecret.setPublicKey(deleteLine(js.getString("public_key")));
        wxKeySecret.setPrevPublic(deleteLine(js.getString("prev_public")));
        wxKeySecret.setNextPublic(deleteLine(js.getString("next_public")));
        wxKeySecret.setRule(js.getJSONObject("rule").toJSONString());
        wxKeySecret.setNextRule(js.getJSONObject("next_rule").toJSONString());
        wxKeySecret.setPrevRule(js.getJSONObject("prev_rule").toJSONString());
        System.out.println(wxKeySecret);

        /*
            /////////////////////////////////////////////////////////////////////////////////////////////
         */

    }

    /** 
    * @Description: 去掉换行及begin/end
    * @Param: [str] 
    * @return: java.lang.String 
    * @Author: tjy
    * @Date: 2019/9/26 
    */ 
    public static String deleteLine(String str){
        return str.replaceAll("\r|\n","").replaceAll("-----BEGIN PUBLIC KEY-----","").replaceAll("-----END PUBLIC KEY-----","");
    }
}