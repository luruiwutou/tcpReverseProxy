package com.forward.core.sftp;

import com.forward.core.sftp.utils.DateUtil;
import com.forward.core.sftp.utils.SftpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SftpExample {
    Logger logger = LoggerFactory.getLogger(SftpExample.class);

    public void getUpiCompressFile() throws Exception {
        String yesterdayStr = DateUtil.getYesterday("yyyyMMdd");
        Date yesterdayDate = strToDate(yesterdayStr);

        Date todayDate = DateUtil.getNextDay(yesterdayDate);
        String todayStr = dateToStr(todayDate);

        String path = "/datat/cybhk_t/0825210344/out";
        String extractPath = "/home/support/atmp/cupyFile/";

        File filePath = new File(extractPath);
        if (!filePath.exists()) {
            filePath.mkdir();
        }
        //yesterday 20200923 -> 200923
        String dateSign = DateUtil.format(yesterdayDate, "yyMMdd");
        String fileName = "IFD" + dateSign + "COMPRESS.tar.gz";

        logger.info("准备下载对账文件:{}", fileName);
        try {
            String host = "203.184.81.98";
            String port = "9990";

            String username = "cybhk_t";


            String priKey = "/root/.ssh/upi_id_rsa";
            String passPhrase = "";
            SftpUtil.getPriKeyChannelSftp(host, port, username, priKey, passPhrase);
            SftpUtil.downloadCompress(path+File.separator+yesterdayStr, filePath+File.separator+fileName);
        } catch (Exception e) {
            logger.error("下载Cupy清算对账文件异常！errorMsg: ", e);
            throw e;
        } finally {
            SftpUtil.release();
        }
        logger.info("已读取到文件：{}", fileName);
    }

    //String yyyyMMdd -> Date
    private Date strToDate(String yyyyMMdd) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date date = format.parse(yyyyMMdd);
        return date;
    }

    //Date yyyyMMdd -> String
    private String dateToStr(Date date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        return format.format(date);
    }

}
