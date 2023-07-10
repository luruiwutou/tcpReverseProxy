package com.forward.core.sftp.controller;

import com.forward.core.sftp.SftpExample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sftp")
@Slf4j
public class SftpController {


    @GetMapping("/getFile")
    public void getUpiCompressFile() throws Exception {
        new SftpExample().getUpiCompressFile();
    }

}
