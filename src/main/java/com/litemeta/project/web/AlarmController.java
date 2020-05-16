package com.litemeta.project.web;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.litemeta.project.dto.AlarmMessageDto;
import com.taobao.api.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/alarm")
public class AlarmController {


    private String secret = "密钥，机器人安全设置页面，加签一栏下面显示的SEC开头的字符串";


    private String webhook = "钉钉回调地址";


    @RequestMapping(value = "/pushData", method = RequestMethod.POST)
    public String alarm(@RequestBody List<AlarmMessageDto> alarmMessageList) {
        String message = "";
        alarmMessageList.forEach(info -> {
            baseMessage(info);
        });
        return message;
    }

    private String baseMessage(AlarmMessageDto info) {
        try {
            DingTalkClient client = new DefaultDingTalkClient(webhook);
            OapiRobotSendRequest request = new OapiRobotSendRequest();
            request.setMsgtype("text");
            OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
            text.setContent("监控告警:[" + info.getAlarmMessage() + "],发生时间:[" + convertTimeToString(info.getStartTime()) + "]");
            request.setText(text);
            OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
            at.setAtMobiles(Arrays.asList("所有人"));
            request.setAt(at);

            OapiRobotSendResponse response = client.execute(request);
            log.info("execute:{}" + response.toString());
        } catch (ApiException e) {
            log.warn(">>>>>>>>>> push dingTalk message is error,message:[{}],errorStack:{}", e.getMessage(), e);
        }
        return "success";

    }

    /**
     * 将Long类型的时间戳转换成String 类型的时间格式，时间格式为：yyyy-MM-dd HH:mm:ss
     */
    private static String convertTimeToString(Long time) {
        Assert.notNull(time, "time is null");
        DateTimeFormatter ftf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
    }

    private void secret(AlarmMessageDto info) {
        try {
            Long timestamp = System.currentTimeMillis();

            String stringToSign = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
            String sign = "&timestamp=" + timestamp + "&sign=" + URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");

            DingTalkClient client = new DefaultDingTalkClient(webhook + sign);
            OapiRobotSendRequest request = new OapiRobotSendRequest();
            request.setMsgtype("text");
            OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
            text.setContent("业务告警:\n" + info.getAlarmMessage());
            request.setText(text);

            OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
            at.setAtMobiles(Arrays.asList("所有人"));
            request.setAt(at);
            OapiRobotSendResponse response = client.execute(request);
            log.info("execute:{}" + response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}