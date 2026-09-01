package com.knowledge.knowledgeagentplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.knowledge.knowledgeagentplatform.mapper")
public class KnowledgeAgentPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeAgentPlatformApplication.class, args);
    }

}
