/*
 Navicat Premium Data Transfer

 Source Server         : 127.0.0.1
 Source Server Type    : MySQL
 Source Server Version : 80025
 Source Host           : 127.0.0.1:3306
 Source Schema         : gateway

 Target Server Type    : MySQL
 Target Server Version : 80025
 File Encoding         : 65001

 Date: 03/08/2023 10:53:08
*/


-- ----------------------------
-- Table structure for proxy_config
-- ----------------------------
DROP TABLE IF EXISTS `proxy_config`;
CREATE TABLE `proxy_config`
(
    `id`        int  NOT NULL AUTO_INCREMENT,
    `conf_key`  varchar(100),
    `conf_val`  varchar(256),
    `conf_desc` varchar(200),
    `channel`   varchar(50),
    PRIMARY KEY (`id`)
);

-- ----------------------------
-- Table structure for tcp_proxy_mapping
-- ----------------------------
DROP TABLE IF EXISTS `tcp_proxy_mapping`;
CREATE TABLE `tcp_proxy_mapping`
(
    `id`                int       NOT NULL AUTO_INCREMENT,
    `local_host`        varchar(20) NULL DEFAULT 'localhost',
    `local_port`        varchar(10) NULL DEFAULT NULL,
    `local_client_port` varchar(10) NULL DEFAULT '',
    `target_host`       varchar(20) NULL DEFAULT NULL,
    `target_port`       varchar(10) NULL DEFAULT NULL,
    `env`               varchar(10) NULL DEFAULT NULL COMMENT '环境：dev/sit/uat/prd',
    `channel`           varchar(50) NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
);
