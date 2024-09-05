-- insert proxy_config
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (1, 'DEFAULT_ENV', 'UAT', '默认sit环境', 'JETCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (2, 'RUNTIME_ENV', 'UAT', '运行时环境', 'JETCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (3, 'UAT_DEFAULT_FIELD_LENGTH_KEY', '10240,0,4,0,0', '消息长度域的长度', 'JETCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (5, 'UAT_PROXY_SERVER_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'JETCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (6, 'UAT_PROXY_CLIENT_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'JETCO');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (20, 'SIT_DEFAULT_FIELD_LENGTH_KEY', '10240,0,9,-9,0', '消息长度域的长度', 'ATM');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (21, 'SIT_PROXY_SERVER_OPEN_SSL', 'TRUE', '是否开启ssl加密', 'ATM');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (22, 'SIT_PROXY_CLIENT_OPEN_SSL', 'TRUE', '是否开启ssl加密', 'ATM');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (23, 'SIT_PATH_SSL_TSL_PEM_PATH', '/home/support/acquiring/key-sit/server.pem', '私钥地址', 'ATM');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (24, 'SIT_PATH_SSL_TSL_KEY_PATH', '/home/support/acquiring/key-sit/server.key', '私钥密钥地址', 'ATM');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (25, 'SIT_PATH_SSL_TSL_CERT_PATH', '/home/support/acquiring/key-sit/server.crt', '公钥证书地址', 'ATM');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (26, 'UAT_DEFAULT_FIELD_LENGTH_KEY', '10240,0,4,0,0', '消息长度域的长度', 'CHEQUE');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (27, 'SIT_DEFAULT_FIELD_LENGTH_KEY', '10240,0,4,0,0', '消息长度域的长度', 'CHEQUE');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (28, 'RUNTIME_ENV', 'UAT', '运行时环境', 'CHEQUE');

-- insert tcp_proxy_mapping
-- UAT
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (1, '10.6.20.27', '20113', '-', '25.6.72.40', '20115', 'SIT', 'JETCO');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (2, '10.6.20.27', '20116', '20103', '10.3.20.193', '6003', 'SIT', 'JETCO');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (3, '10.6.20.27', '20115', '20102', '10.3.20.193', '6002', 'SIT', 'JETCO');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (4, '10.6.20.27', '20114', '20101', '10.3.20.193', '6001', 'SIT', 'JETCO');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (5, '10.6.20.27', '20111', '-', '25.6.72.40', '20115', 'SIT', 'JETCO');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (6, '10.6.20.27', '20112', '-', '25.6.72.40', '20115', 'SIT', 'JETCO');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (7, '10.6.20.27', '20114', '20101', '10.3.20.193', '6001', 'UAT', 'JETCO');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (8, '10.6.20.27', '20111', '-', '25.6.72.42', '20115', 'UAT', 'JETCO');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (9, '10.6.20.27', '20112', '-', '25.6.72.42', '20115', 'UAT', 'JETCO');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (10, '10.6.20.27', '20113', '-', '25.6.72.42', '20115', 'UAT', 'JETCO');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (11, '10.6.20.27', '20115', '20102', '10.3.20.193', '6002', 'UAT', 'JETCO');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (12, '10.6.20.27', '20116', '20103', '10.3.20.193', '6003', 'UAT', 'JETCO');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (13, '10.6.20.27', '20113', '-', '25.6.72.43', '20115', 'UAT', 'JETCO');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (14, '10.6.20.27', '20112', '-', '25.6.72.43', '20115', 'UAT', 'JETCO');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (15, '10.6.20.27', '20111', '-', '25.6.72.43', '20115', 'UAT', 'JETCO');

-- 预生产
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (1, '10.6.23.10', '20111', '-', '10.6.10.130', '20111', 'UAT', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (2, '10.6.23.10', '20112', '-', '10.6.10.130', '20112', 'UAT', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (3, '10.6.23.10', '20113', '-', '10.6.10.130', '20113', 'UAT', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (4, '10.6.23.10', '20114', '20101', '10.3.20.193', '6001', 'UAT', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (5, '10.6.23.10', '20115', '20102', '10.3.20.193', '6002', 'UAT', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (6, '10.6.23.10', '20116', '20103', '10.3.20.193', '6003', 'UAT', 'JETCO');

