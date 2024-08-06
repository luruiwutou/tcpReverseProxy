package com.forward.core.tcpReverseProxy.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class SecureFileAccess {
    private static final String BASE_DIR = "/safe/dir/";

    public static String getSafePath(String path) throws Exception {
        Path normalizedPath = normalizePath(path);
        if (isPathWithinBaseDir(normalizedPath)) {
            // Perform file operations safely
            File file = normalizedPath.toFile();
            // Example: Read file
            String content = new String(Files.readAllBytes(file.toPath()));
            return content;
        } else {
            throw new Exception("Invalid file path");
        }
    }

    private static String getFilePathFromDatabase() {
        // Simulate getting a file path from a database
        return "/safe/dir/etc/passwd"; // Example of a potentially dangerous path
    }

    private static Path normalizePath(String filePath) throws Exception {
        Path path = Paths.get(BASE_DIR, filePath);
        return path.normalize();
    }

    private static boolean isPathWithinBaseDir(Path path) {
        Path baseDir = Paths.get(BASE_DIR).normalize();
        return path.startsWith(baseDir);
    }
}
