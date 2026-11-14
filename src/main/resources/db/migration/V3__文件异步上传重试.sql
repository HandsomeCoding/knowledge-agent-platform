ALTER TABLE document ADD `retry_count`  int NOT NULL DEFAULT 0 COMMENT '重试次数';
ALTER TABLE document ADD `error_msg`  varchar(512) COMMENT '错误信息';
ALTER TABLE document_chunk ADD UNIQUE KEY uk_doc_index (document_id, chunk_sort);