SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `app_user`;
CREATE TABLE `app_user`  (
  `id` bigint NOT NULL,
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `user_name` varchar(255)  NOT NULL COMMENT '用户名',
  `user_password` varchar(255)  NOT NULL COMMENT '用户密码',
  `usr_phone` varchar(32)  NOT NULL COMMENT '用户电话',
  `user_email` varchar(32)  NULL DEFAULT NULL COMMENT '用户邮箱',
  PRIMARY KEY (`id` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;




DROP TABLE IF EXISTS `document`;
CREATE TABLE `document`  (
  `id` bigint NOT NULL COMMENT '主键',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `document_name` varchar(255)  NOT NULL COMMENT '文件名',
  `document_path` varchar(255)  NULL DEFAULT NULL COMMENT '文件位置',
  `knowledge_base_id` bigint NOT NULL COMMENT '归属知识库id',
  `process_status` varchar(32)  NOT NULL COMMENT '文档处理状态',
  `document_content` mediumtext  NULL COMMENT '文档全文  目前支持16m',
  PRIMARY KEY (`id` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `document_chunk`;
CREATE TABLE `document_chunk`  (
  `id` bigint NOT NULL COMMENT '主键',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `chunk_sort` int NOT NULL COMMENT '文件块个数',
  `content` text  NULL COMMENT '文件块内容',
  `chunk_sum` int NOT NULL COMMENT '文件块总数',
  PRIMARY KEY (`id` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;


DROP TABLE IF EXISTS `knowledge_base`;
CREATE TABLE `knowledge_base`  (
  `id` bigint NOT NULL COMMENT '主键id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `knowledage_name` varchar(255) NOT NULL COMMENT '知识库名称',
  `belong_id` bigint NOT NULL COMMENT '归属人id',
  PRIMARY KEY (`id` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;



