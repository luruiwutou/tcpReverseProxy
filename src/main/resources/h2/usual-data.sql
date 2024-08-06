-- insert proxy_config
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (21, 'DEFAULT_ENV', 'UAT', '默认sit环境', 'MYSQL');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (22, 'RUNTIME_ENV', 'UAT', '运行时环境', 'MYSQL');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (23, 'DEFAULT_ENV', 'UAT', '默认sit环境', 'REDIS');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (24, 'RUNTIME_ENV', 'UAT', '运行时环境', 'REDIS');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (25, 'UAT_PROXY_CLIENT_IDLE_CONFIG', '240,0,0,/* ping */ SELECT 1', '空闲配置', 'MYSQL');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (26, 'UAT_PROXY_CLIENT_IDLE_CONFIG', '240,5,0,PING\n', '空闲配置', 'REDIS');
-- -- insert tcp_proxy_mapping
-- UAT
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (15, '10.6.20.27', '3306', NULL, '10.71.34.214', '3306', 'UAT', 'MYSQL');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (20, '10.6.20.27', '6379', NULL, '10.71.35.212', '6379', 'UAT', 'REDIS');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (21, '10.6.20.27', '19005', '-', '203.184.81.84', '14552', 'UAT', 'CUPY');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (23, '10.6.20.27', '19001', NULL, '25.6.72.42', '19003', 'UAT', 'CUPY');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (27, '10.6.20.28', '19002', NULL, '25.6.72.42', '19003', 'UAT', 'CUPY');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (28, '10.6.20.28', '19002', NULL, '25.6.72.43', '19003', 'UAT', 'CUPY');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (30, '10.6.20.28', '19004', '-', '203.184.81.74', '14553', 'UAT', 'CUPY');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (31, '10.6.20.28', '19006', '-', '203.184.81.84', '14554', 'UAT', 'CUPY');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (32, '10.6.20.27', '19001', NULL, '25.6.72.40', '19003', 'SIT', 'CUPY');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (33, '10.6.20.27', '19001', NULL, '25.6.72.41', '19003', 'SIT', 'CUPY');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (34, '10.6.20.28', '19002', NULL, '25.6.72.40', '19003', 'SIT', 'CUPY');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (35, '10.6.20.28', '19002', NULL, '25.6.72.41', '19003', 'SIT', 'CUPY');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (36, '10.6.20.27', '19003', '-', '203.184.81.74', '14551', 'SIT', 'CUPY');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (37, '10.6.20.28', '19004', '-', '203.184.81.74', '14553', 'SIT', 'CUPY');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (38, '10.6.20.27', '19005', '-', '203.184.81.84', '14552', 'SIT', 'CUPY');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (39, '10.6.20.28', '19006', '-', '203.184.81.84', '14554', 'SIT', 'CUPY');


