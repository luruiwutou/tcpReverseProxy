-- insert proxy_config
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (1, 'DEFAULT_ENV', 'UAT', '默认sit环境', 'CUPY');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (2, 'RUNTIME_ENV', 'UAT', '运行时环境', 'CUPY');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (3, 'SIT_DEFAULT_FIELD_LENGTH_KEY', '10240,0,4,0,0', '消息长度域的长度', 'CUPY');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (4, 'SIT_PROXY_SERVER_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'CUPY');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (5, 'SIT_PROXY_CLIENT_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'CUPY');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (6, 'SIT_PROXY_CLIENT_IDLE_CONFIG', '240,0,0', '空闲配置', 'CUPY');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (13, 'UAT_DEFAULT_FIELD_LENGTH_KEY', '10240,0,4,0,0', '消息长度域的长度', 'CUPY');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (14, 'UAT_PROXY_SERVER_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'CUPY');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (15, 'UAT_PROXY_CLIENT_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'CUPY');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (16, 'UAT_PROXY_CLIENT_IDLE_CONFIG', '240,0,0', '空闲配置', 'CUPY');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (23, 'PRD_DEFAULT_FIELD_LENGTH_KEY', '10240,0,4,0,0', '消息长度域的长度', 'CUPY');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (24, 'PRD_PROXY_SERVER_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'CUPY');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (25, 'PRD_PROXY_CLIENT_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'CUPY');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (26, 'PRD_PROXY_CLIENT_IDLE_CONFIG', '240,0,0', '空闲配置', 'CUPY');
-- -- insert tcp_proxy_mapping
-- UAT
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (1, '10.6.20.27', '19001', NULL, '25.6.72.40', '19003', 'SIT', 'CUPY');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (2, '10.6.20.27', '19001', NULL, '25.6.72.41', '19003', 'SIT', 'CUPY');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (3, '10.6.20.28', '19002', NULL, '25.6.72.40', '19003', 'SIT', 'CUPY');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (4, '10.6.20.28', '19002', NULL, '25.6.72.41', '19003', 'SIT', 'CUPY');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (5, '10.6.20.27', '19003', '-', '203.184.81.74', '14551', 'SIT', 'CUPY');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (6, '10.6.20.28', '19004', '-', '203.184.81.74', '14553', 'SIT', 'CUPY');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (7, '10.6.20.27', '19005', '-', '203.184.81.84', '14552', 'SIT', 'CUPY');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (8, '10.6.20.28', '19006', '-', '203.184.81.84', '14554', 'SIT', 'CUPY');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (11, '10.6.20.27', '19001', NULL, '25.6.72.42', '19003', 'UAT', 'CUPY');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (12, '10.6.20.27', '19001', NULL, '25.6.72.43', '19003', 'UAT', 'CUPY');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (13, '10.6.20.28', '19002', NULL, '25.6.72.42', '19003', 'UAT', 'CUPY');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (14, '10.6.20.28', '19002', NULL, '25.6.72.43', '19003', 'UAT', 'CUPY');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (15, '10.6.20.27', '19003', '-', '203.184.81.74', '14551', 'UAT', 'CUPY');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (16, '10.6.20.28', '19004', '-', '203.184.81.74', '14553', 'UAT', 'CUPY');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (17, '10.6.20.27', '19005', '-', '203.184.81.84', '14552', 'UAT', 'CUPY');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (18, '10.6.20.28', '19006', '-', '203.184.81.84', '14554', 'UAT', 'CUPY');
-- 预生产
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (21, '10.6.23.10', '19001', NULL, '10.6.10.130', '19001', 'PRD', 'CUPY');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (22, '10.6.23.10', '19001', NULL, '10.6.10.130', '19002', 'PRD', 'CUPY');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (23, '10.6.23.11', '19002', NULL, '10.6.10.130', '19001', 'PRD', 'CUPY');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (24, '10.6.23.11', '19002', NULL, '10.6.10.130', '19002', 'PRD', 'CUPY');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (25, '10.6.23.10', '19003', '-', '203.184.81.74', '14551', 'PRD', 'CUPY');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (26, '10.6.23.11', '19004', '-', '203.184.81.74', '14553', 'PRD', 'CUPY');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (27, '10.6.23.10', '19005', '-', '203.184.81.84', '14552', 'PRD', 'CUPY');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (28, '10.6.23.11', '19006', '-', '203.184.81.84', '14554', 'PRD', 'CUPY');

