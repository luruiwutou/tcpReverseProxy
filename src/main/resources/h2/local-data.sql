-- insert proxy_config
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (1, 'DEFAULT_ENV', 'UAT', '默认sit环境', 'CUPY');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (2, 'RUNTIME_ENV', 'UAT', '运行时环境', 'CUPY');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (3, 'UAT_DEFAULT_FIELD_LENGTH_KEY', '10240,0,4,0,0', '消息长度域的长度', 'CUPY');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (4, 'SIT_DEFAULT_FIELD_LENGTH_KEY', '10240,0,4,0,0', '消息长度域的长度', 'CUPY');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (5, 'UAT_PROXY_SERVER_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'CUPY');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (6, 'UAT_PROXY_CLIENT_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'CUPY');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (7, 'SIT_PROXY_SERVER_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'CUPY');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (8, 'SIT_PROXY_CLIENT_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'CUPY');
-- insert proxy_config
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (11, 'DEFAULT_ENV', 'UAT', '默认sit环境', 'EPSCO');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (12, 'RUNTIME_ENV', 'UAT', '运行时环境', 'EPSCO');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (13, 'UAT_DEFAULT_FIELD_LENGTH_KEY', '10240,0,4,0,0', '消息长度域的长度', 'EPSCO');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (14, 'SIT_DEFAULT_FIELD_LENGTH_KEY', '10240,0,4,0,0', '消息长度域的长度', 'EPSCO');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (15, 'UAT_PROXY_SERVER_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'EPSCO');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (16, 'UAT_PROXY_CLIENT_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'EPSCO');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (17, 'SIT_PROXY_SERVER_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'EPSCO');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (18, 'SIT_PROXY_CLIENT_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'EPSCO');
-- -- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (9, 'UAT_PROXY_CLIENT_IDLE_CONFIG', '240,0,0', '空闲配置', 'CUPY');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (21, 'DEFAULT_ENV', 'UAT', '默认sit环境', 'MYSQL');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (22, 'RUNTIME_ENV', 'UAT', '运行时环境', 'MYSQL');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (23, 'DEFAULT_ENV', 'UAT', '默认sit环境', 'REDIS');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (24, 'RUNTIME_ENV', 'UAT', '运行时环境', 'REDIS');
-- INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (25, 'UAT_PROXY_CLIENT_IDLE_CONFIG', '240,5,0,/* ping */ SELECT 1', '空闲配置', 'MYSQL');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (1, 'DEFAULT_ENV', 'UAT', '默认sit环境', 'JETCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (2, 'RUNTIME_ENV', 'UAT', '运行时环境', 'JETCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (3, 'UAT_DEFAULT_FIELD_LENGTH_KEY', '10240,0,4,0,0', '消息长度域的长度', 'JETCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (4, 'UAT_PROXY_SERVER_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'JETCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (5, 'UAT_PROXY_CLIENT_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'JETCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (13, 'SIT_DEFAULT_FIELD_LENGTH_KEY', '10240,0,4,0,0', '消息长度域的长度', 'JETCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (14, 'SIT_PROXY_SERVER_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'JETCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (15, 'SIT_PROXY_CLIENT_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'JETCO');

---- insert tcp_proxy_mapping
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (1, '192.168.21.5', '32235', NULL, 'localhost', '19002', 'UAT', 'CUPY');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (2, '192.168.21.11', '20115', '', 'localhost', '14551', 'UAT', 'EPSCO');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (3, '192.168.21.11', '20116', '', 'localhost', '14552', 'UAT', 'EPSCO');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (4, '192.168.21.11', '20117', '', 'localhost', '14553', 'UAT', 'EPSCO');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (4, '192.168.21.5', '19003', '-', 'localhost', '14553', 'UAT', 'CUPY');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (5, '192.168.21.5', '19005', '-', 'localhost', '14554', 'UAT', 'CUPY');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (1, '192.168.21.3', '20112', '', '192.168.21.3', '5998', 'UAT', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (2, '192.168.21.3', '20112', '-', '192.168.21.3', '5998', 'SIT', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (4, '192.168.21.3', '20114', '20101', '192.168.21.3', '16001', 'UAT', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (5, '192.168.21.3', '20115', '20102', '192.168.21.3', '16002', 'UAT', 'JETCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (6, '192.168.21.3', '20116', '20103', '192.168.21.3', '16003', 'UAT', 'JETCO');

