-- AI-Code 为您构建代码，享受智慧生活!
CREATE SCHEMA IF NOT EXISTS tencent;
USE tencent;
CREATE TABLE tencent (id varchar(64) NOT NULL comment 'id', qq varchar(32) NOT NULL comment 'qq', email varchar(32) NOT NULL comment '邮箱', phone varchar(16) comment '手机号', PRIMARY KEY (id, qq, email), INDEX (phone)) comment='腾讯数据';

CREATE TABLE `worker_node` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'auto increment id',
  `HOST_NAME` varchar(64) NOT NULL COMMENT 'host name',
  `PORT` varchar(64) NOT NULL COMMENT 'port',
  `TYPE` int(11) NOT NULL COMMENT 'node type: ACTUAL or CONTAINER',
  `LAUNCH_DATE` date NOT NULL COMMENT 'launch date',
  `MODIFIED` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT 'modified time',
  `CREATED` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT 'created time',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1263 DEFAULT CHARSET=utf8 COMMENT='DB WorkerID Assigner for UID Generator';
