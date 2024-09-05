package com.forward.core.tcpReverseProxy.utils;

import org.owasp.esapi.ESAPI;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class SecureFileAccess {

    public static String SAFE_DIR = ESAPI.securityConfiguration().getStringProp("Safe.Dir");
    public static List<String> AllowedExtensions = Arrays.asList(ESAPI.securityConfiguration().getStringProp("AllowedExtensions").split(","));

    public static String getSafePath(String filePath) throws Exception {
        String path = filePath.substring(0, filePath.lastIndexOf(File.separator));
        String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
        String validFileName = ESAPI.validator().getValidFileName("Invalid file name ", fileName, AllowedExtensions, false);
        return ESAPI.validator().getValidDirectoryPath("Invalid file path ", path, new File(SAFE_DIR), false) + File.separator + validFileName;
    }
}
