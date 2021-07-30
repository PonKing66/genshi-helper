package org.ponking.gih.sign;

import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ponking.gih.push.MessagePush;
import org.ponking.gih.push.ServerChanMessagePush;
import org.ponking.gih.push.WeixinCPMessagePush;
import org.ponking.gih.server.weixincp.service.WXUserInfo;
import org.ponking.gih.sign.gs.GenShinSignMiHoYo;
import org.ponking.gih.sign.gs.GenshinHelperProperties;
import org.ponking.gih.sign.gs.MiHoYoConfig;
import org.ponking.gih.sign.gs.MiHoYoSignMiHoYo;
import org.ponking.gih.util.FileUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * @Author ponking
 * @Date 2021/5/31 15:54
 */
public class DailyTask implements Runnable {

    private static final Logger log = LogManager.getLogger(DailyTask.class);

    public GenShinSignMiHoYo genShinSign;

    public MiHoYoSignMiHoYo miHoYoSign;

    public MessagePush messagePush = null;

    public boolean pushed = false; // 是否推送日志

    public String workDir; // 工作目录


    /**
     * @param mode       推送消息方式
     * @param sckey      server酱sckey
     * @param corpid     企业微信corpid
     * @param corpsecret 企业微信corpsecret
     * @param agentid    企业微信agentid
     * @param account    账号配置信息
     */
    public DailyTask(String mode, String sckey, String corpid, String corpsecret, String agentid,
                     GenshinHelperProperties.Account account) {
        // 默认目录,因为云腾讯函数，只能在/temp有读取日志权限，故手动设置腾讯云函数使用/temp
        String baseDir = System.getProperty("user.dir");
        this.workDir = baseDir + File.separator + "logs";

        if (mode == null) {
            genShinSign = new GenShinSignMiHoYo(account.getCookie());
            if (account.getStuid() != null && account.getStoken() != null) {
                miHoYoSign = new MiHoYoSignMiHoYo(MiHoYoConfig.HubsEnum.YS.getGame(), account.getStuid(), account.getStoken());
            }
        } else if ("serverChan".equals(mode)) {
            genShinSign = new GenShinSignMiHoYo(account.getCookie());
            if (account.getStuid() != null && account.getStoken() != null) {
                miHoYoSign = new MiHoYoSignMiHoYo(MiHoYoConfig.HubsEnum.YS.getGame(), account.getStuid(), account.getStoken());
            }
            messagePush = new ServerChanMessagePush(sckey);
            pushed = true;
        } else if ("weixincp".equals(mode)) {
            genShinSign = new GenShinSignMiHoYo(account.getCookie());
            if (account.getStuid() != null && account.getStoken() != null) {
                miHoYoSign = new MiHoYoSignMiHoYo(MiHoYoConfig.HubsEnum.YS.getGame(), account.getStuid(), account.getStoken());
            }
            WXUserInfo wxUserInfo = new WXUserInfo(corpid, corpsecret, agentid, account.getToUser());
            messagePush = new WeixinCPMessagePush(wxUserInfo);
            pushed = true;
        } else {
            throw new UnsupportedOperationException("参数异常");
        }
    }

    @SneakyThrows
    @Override
    public void run() {
        doDailyTask();
    }

    public void doDailyTask() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        log.info("开始执行时间 {}", dtf.format(LocalDateTime.now()));
        if (miHoYoSign != null) {
            try {
                miHoYoSign.doSingleThreadSign();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (genShinSign != null) {
            genShinSign.doSign();
        }
        if (pushed && messagePush != null) {

            String fileName = Thread.currentThread().getName() + ".log";
            messagePush.sendMessage("原神签到", FileUtils.loadDaily(this.workDir + File.separator + fileName));
        }
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }
}