---- insert proxy_config
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (1, 'DEFAULT_ENV', 'UAT', '默认sit环境', 'CUPY');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (2, 'RUNTIME_ENV', 'UAT', '运行时环境', 'CUPY');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (3, 'DEFAULT_ENV', 'UAT', '默认sit环境', 'JETCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (4, 'RUNTIME_ENV', 'UAT', '运行时环境', 'JETCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (5, 'DEFAULT_ENV', 'UAT', '默认sit环境', 'EPSCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (6, 'RUNTIME_ENV', 'UAT', '运行时环境', 'EPSCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (7, 'RUNTIME_ENV', 'UAT', '运行时环境', 'ATM');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (8, 'UAT_DEFAULT_FIELD_LENGTH_KEY', '10240,0,4,0,0', '消息长度域的长度', 'JETCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (9, 'UAT_DEFAULT_FIELD_LENGTH_KEY', '10240,0,4,0,0', '消息长度域的长度', 'CUPY');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (10, 'UAT_DEFAULT_FIELD_LENGTH_KEY', '10240,0,4,0,0', '消息长度域的长度', 'EPSCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (11, 'UAT_DEFAULT_FIELD_LENGTH_KEY', '10240,0,9,-9,0', '消息长度域的长度', 'ATM');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (12, 'UAT_PROXY_SERVER_OPEN_SSL', 'TRUE', '是否开启ssl加密', 'ATM');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (13, 'UAT_PROXY_CLIENT_OPEN_SSL', 'TRUE', '是否开启ssl加密', 'ATM');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (14, 'UAT_PATH_SSL_TSL_PEM_PATH', '/home/support/acquiring/key-uat/server.pem', '私钥地址', 'ATM');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (15, 'UAT_PATH_SSL_TSL_KEY_PATH', '/home/support/acquiring/key-uat/server.key', '私钥密钥地址', 'ATM');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (16, 'UAT_PATH_SSL_TSL_CERT_PATH', '/home/support/acquiring/key-uat/server.crt', '公钥证书地址', 'ATM');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (17, 'SIT_DEFAULT_FIELD_LENGTH_KEY', '10240,0,4,0,0', '消息长度域的长度', 'JETCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (18, 'SIT_DEFAULT_FIELD_LENGTH_KEY', '10240,0,4,0,0', '消息长度域的长度', 'CUPY');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (19, 'SIT_DEFAULT_FIELD_LENGTH_KEY', '10240,0,4,0,0', '消息长度域的长度', 'EPSCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (20, 'SIT_DEFAULT_FIELD_LENGTH_KEY', '10240,0,9,-9,0', '消息长度域的长度', 'ATM');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (21, 'SIT_PROXY_SERVER_OPEN_SSL', 'TRUE', '是否开启ssl加密', 'ATM');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (22, 'SIT_PROXY_CLIENT_OPEN_SSL', 'TRUE', '是否开启ssl加密', 'ATM');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (23, 'SIT_PATH_SSL_TSL_PEM_PATH', '/home/support/acquiring/key-sit/server.pem', '私钥地址', 'ATM');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (24, 'SIT_PATH_SSL_TSL_KEY_PATH', '/home/support/acquiring/key-sit/server.key', '私钥密钥地址', 'ATM');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (25, 'SIT_PATH_SSL_TSL_CERT_PATH', '/home/support/acquiring/key-sit/server.crt', '公钥证书地址', 'ATM');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (26, 'UAT_DEFAULT_FIELD_LENGTH_KEY', '10240,0,4,0,0', '消息长度域的长度', 'CHEQUE');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (27, 'SIT_DEFAULT_FIELD_LENGTH_KEY', '10240,0,4,0,0', '消息长度域的长度', 'CHEQUE');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (28, 'RUNTIME_ENV', 'UAT', '运行时环境', 'CHEQUE');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (29, 'UAT_PROXY_SERVER_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'JETCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (30, 'UAT_PROXY_CLIENT_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'JETCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (31, 'SIT_PROXY_SERVER_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'JETCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (32, 'SIT_PROXY_CLIENT_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'JETCO');
---- insert tcp_proxy_mapping
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (2, '10.6.20.27', '20113', '-', '25.6.72.40', '20115', 'sit', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (3, '10.6.20.27', '20116', '20103', '10.3.20.193', '6003', 'sit', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (4, '10.6.20.27', '20115', '20102', '10.3.20.193', '6002', 'sit', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (7, '10.6.20.27', '20114', '20101', '10.3.20.193', '6001', 'uat', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (8, '10.6.20.27', '20111', '-', '25.6.72.42', '20115', 'uat', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (9, '10.6.20.27', '20112', '-', '25.6.72.42', '20115', 'uat', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (10, '10.6.20.27', '20113', '-', '25.6.72.42', '20115', 'uat', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (11, '10.6.20.27', '20115', '20102', '10.3.20.193', '6002', 'uat', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (12, '10.6.20.27', '20116', '20103', '10.3.20.193', '6003', 'uat', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (49, '10.6.20.27', '20114', '20101', '10.3.20.193', '6001', 'sit', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (50, 'localhost', '20112', NULL, '25.6.72.41', '20115', 'sit', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (51, 'localhost', '20113', NULL, '25.6.72.41', '20115', 'sit', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (52, 'localhost', '20111', NULL, '25.6.72.43', '20115', 'uat', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (53, 'localhost', '20112', NULL, '25.6.72.43', '20115', 'uat', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (54, 'localhost', '20113', NULL, '25.6.72.43', '20115', 'uat', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (55, '10.6.20.27local', '8030', NULL, '25.6.72.42', '8030', 'uat', 'ATM');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (58, '10.6.20.28', '8030', NULL, '25.6.72.42', '8030', 'uat', 'ATM');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (59, '10.6.20.27', '8020', '', '25.6.72.42', '8020', 'uat', 'CHEQUE');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (60, '10.6.20.28', '8020', '', '25.6.72.42', '8020', 'uat', 'CHEQUE');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (61, '10.6.20.27', '20111', '-', '25.6.72.40', '20115', 'sit', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (62, '25.6.72.46', '30000', '', '25.6.72.46', '33333', 'uat', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (63, '10.6.20.27', '20112', '-', '25.6.72.40', '20115', 'sit', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (64, '25.6.72.46', '30001', '', '25.6.72.46', '33333', 'uat', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (65, '25.6.72.46', '30002', '', '25.6.72.46', '33333', 'uat', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (66, '10.6.20.27', '20113', '-', '25.6.72.43', '20115', 'uat', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (67, '10.6.20.27', '20112', '-', '25.6.72.43', '20115', 'uat', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (68, '10.6.20.27', '20111', '-', '25.6.72.43', '20115', 'uat', 'JETCO');
