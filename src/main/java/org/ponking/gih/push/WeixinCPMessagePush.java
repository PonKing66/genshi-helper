package org.ponking.gih.push;


import org.ponking.gih.server.weixincp.service.PushMessageServiceImpl;
import org.ponking.gih.server.weixincp.service.WXUserInfo;

/**
 * @Author ponking
 * @Date 2021/5/7 20:15
 */
public class WeixinCPMessagePush implements MessagePush {

    private final WXUserInfo userInfo;

    public WeixinCPMessagePush(WXUserInfo userInfo) {
        this.userInfo = userInfo;
    }


    @Override
    public void sendMessage(String title, String desp) {
        sendMessageCardType(title, desp);
    }


    public void sendMessageCardType(String title, String desp) {
        PushMessageServiceImpl service = new PushMessageServiceImpl(userInfo);
        service.sendWithTextCard(title, desp);
    }

    public void sendMessageTexTType(String content) {
        PushMessageServiceImpl service = new PushMessageServiceImpl(userInfo);
        service.sendWithText(content);
    }

}
