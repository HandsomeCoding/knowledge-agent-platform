
ALTER TABLE `document`
    ADD COLUMN `source_type` tinyint NOT NULL DEFAULT 0 COMMENT '来源类型: 0(手动新建),1(文件上传)';

ALTER TABLE `document`
    ADD COLUMN `content_version` int NOT NULL DEFAULT 0 COMMENT '文档正文版本';

ALTER TABLE `document`
    ADD COLUMN `document_type` int NOT NULL DEFAULT 0 COMMENT '文档类型 md txt pdf';


ALTER TABLE `document`
    ADD COLUMN   `content_hash`    CHAR(32)     COMMENT 'content 的 MD5，编辑短路用';