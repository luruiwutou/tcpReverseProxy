-- insert proxy_config
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (1, 'DEFAULT_ENV', 'UAT', '默认sit环境', 'EPSCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (2, 'RUNTIME_ENV', 'UAT', '运行时环境', 'EPSCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (3, 'SIT_DEFAULT_FIELD_LENGTH_KEY', '10240,0,4,0,0', '消息长度域的长度', 'EPSCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (4, 'SIT_PROXY_SERVER_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'EPSCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (5, 'SIT_PROXY_CLIENT_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'EPSCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (13, 'UAT_DEFAULT_FIELD_LENGTH_KEY', '10240,0,4,0,0', '消息长度域的长度', 'EPSCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (14, 'UAT_PROXY_SERVER_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'EPSCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (15, 'UAT_PROXY_CLIENT_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'EPSCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (23, 'PRD_DEFAULT_FIELD_LENGTH_KEY', '10240,0,4,0,0', '消息长度域的长度', 'EPSCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (24, 'PRD_PROXY_SERVER_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'EPSCO');
INSERT INTO `proxy_config`(`id`, `conf_key`, `conf_val`, `conf_desc`, `channel`) VALUES (25, 'PRD_PROXY_CLIENT_OPEN_SSL', 'FALSE', '是否开启ssl加密', 'EPSCO');

-- insert tcp_proxy_mapping
-- UAT
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (1, '10.6.20.27', '30150', '-', '25.6.72.42', '30110', 'UAT', 'EPSCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (2, '10.6.20.28', '30160', '-', '25.6.72.43', '30110', 'UAT', 'EPSCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (3, '10.6.20.27', '32236', '-', '172.20.253.217', '32236', 'UAT', 'EPSCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (4, '10.6.20.28', '32236', '-', '172.20.253.218', '32236', 'UAT', 'EPSCO');

-- 预生产
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (11, '10.6.23.10', '30150', '-', '10.6.10.130', '30150', 'PRD', 'EPSCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (12, '10.6.23.11', '30160', '-', '10.6.10.130', '30160', 'PRD', 'EPSCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (13, '10.6.23.10', '32236', '-', '172.20.253.217', '32236', 'PRD', 'EPSCO');
INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (14, '10.6.23.11', '32236', '-', '172.20.253.218', '32236', 'PRD', 'EPSCO');
-- INSERT INTO `tcp_proxy_mapping`(`id`, `local_host`, `local_port`, `local_client_port`, `target_host`, `target_port`, `env`, `channel`) VALUES (5, '10.6.23.10', '32236', '-', '10.6.23.11', '32236', 'UAT', 'EPSCO');

