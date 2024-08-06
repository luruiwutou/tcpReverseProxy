package com.forward.core.tcpReverseProxy.utils;

import com.alibaba.druid.pool.ha.PropertiesUtils;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.owasp.esapi.ESAPI;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public class SecureFileAccess {

    public static String SAFE_DIR = ESAPI.securityConfiguration().getStringProp("Safe.Dir");
    public static String getSafePath(String filePath) throws Exception {
        String path = filePath.substring(0, filePath.lastIndexOf(File.separator));
        String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
        return ESAPI.validator().getValidDirectoryPath("Invalid file path ", path, new File(SAFE_DIR), false) + File.separator + fileName;
    }
}
