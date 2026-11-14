package com.knowledge.knowledgeagentplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.knowledge.knowledgeagentplatform.mapper")
@EnableAsync
public class KnowledgeAgentPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeAgentPlatformApplication.class, args);
    }

}
