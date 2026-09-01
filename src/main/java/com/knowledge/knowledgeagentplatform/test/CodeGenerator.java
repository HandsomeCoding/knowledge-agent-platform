package com.knowledge.knowledgeagentplatform.test;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Column;

import java.util.Collections;
/**
 * MyBatis-Plus 代码生成器（3.5.17 新版链式 API）
 *
 * 作用：根据数据库表结构，自动生成 Entity / Mapper / Mapper.xml / Service / ServiceImpl / Controller
 *
 * 使用方法：
 *   1. pom.xml 引入 mybatis-plus-generator + 模板引擎依赖（见文件底部注释）
 *   2. 修改下方数据源、表名、包名
 *   3. 直接运行 main 方法
 */
public class CodeGenerator {
    public static void main(String[] args) {
        // ========== 1. 数据源配置（改成你自己的库） ==========
        // 注意 remarks=true&useInformationSchema=true 这两个参数：让生成器能读到表/字段注释
        String url = "jdbc:mysql://localhost:3306/knowledge-agent-platform"
                + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
                + "&remarks=true&useInformationSchema=true";
        String username = "root";
        String password = "root123456";

        // 要生成哪些表，多个用英文逗号分隔，例如："user,order,product"
        String tables = "app_user,document,document_chunk,knowledge_base";

        FastAutoGenerator.create(url, username, password)

                // ========== 2. 全局配置 ==========
                .globalConfig(builder -> builder
                                .author("whw")                    // 类注释上的作者
                                .outputDir("src/main/java")            // Java 代码输出目录
                                .dateType(DateType.TIME_PACK)          // 日期字段映射为 java.time.LocalDateTime
                                .commentDate("yyyy-MM-dd")             // 类注释日期格式
                                .disableOpenDir()                      // 生成后不自动打开资源管理器
                        //建议不要打开，生成是还是2.0的老注解，3.0直接弃用
//                        .enableSwagger()                     // 若项目用了 springdoc/swagger 再打开
                )

                // ========== 3. 包配置 ==========
                .packageConfig(builder -> builder
                        .parent("com.knowledge")            // 父包名
                        .moduleName("knowledgeagentplatform")                  // 模块名，生成路径为 parent + moduleName
                        .entity("entity")                      // 实体包
                        .mapper("mapper")                      // Mapper 接口包
                        .service("service")                    // Service 接口包
                        .serviceImpl("service.impl")           // ServiceImpl 包
                        .controller("controller")              // Controller 包
                        .pathInfo(Collections.singletonMap(    // 单独指定 mapper.xml 输出目录
                                OutputFile.xml, "src/main/resources/mapper"))
                )

                // ========== 4. 策略配置（决定生成哪些类、什么风格） ==========
                .strategyConfig(builder -> builder
                        .addInclude(tables.split(","))         // 指定要生成的表名
                        //.addTablePrefix("t_", "sys_")        // 表前缀，生成类名时自动去掉
                        //.addTableSuffix("_info")

                        // --- 实体类策略 ---
                        .entityBuilder()
                        .enableLombok()                        // 实体上加 @Data，省 getter/setter
                        .enableTableFieldAnnotation()          // 字段上加 @TableField("列名")
                        .idType(IdType.ASSIGN_ID)              // 主键策略：雪花算法（MyBatis-Plus 默认）
                        .naming(NamingStrategy.underline_to_camel) // 数据库下划线 → 驼峰
                        .enableChainModel()                    // 链式 setter（可选）
                        .addTableFills(                        // 公共字段自动填充
                                new Column("create_time", FieldFill.INSERT),
                                new Column("update_time", FieldFill.INSERT_UPDATE)
                        )
                        //.logicDeleteColumnName("deleted")    // 逻辑删除字段（字段名）
                        //.versionColumnName("version")        // 乐观锁字段

                        // --- Controller 策略 ---
                        .controllerBuilder()
                        .enableRestStyle()                     // 生成 @RestController（而不是 @Controller）
                        //.enableHyphenStyle()                 // 路径用中划线，如 /user-info

                        // --- Service 策略 ---
                        .serviceBuilder()
                        .formatServiceFileName("%sService")         // 接口名：UserService
                        .formatServiceImplFileName("%sServiceImpl") // 实现类：UserServiceImpl

                        // --- Mapper 策略 ---
                        .mapperBuilder()
                        .enableBaseResultMap()                 // XML 里生成 BaseResultMap
                        .enableBaseColumnList()                // XML 里生成 Base_Column_List
                )

                // ========== 5. 模板引擎（默认 Velocity，可换 Freemarker） ==========
                // 用默认 Velocity 则删掉这一行；用 Freemarker 需在 pom 加 org.freemarker:freemarker
                .templateEngine(new FreemarkerTemplateEngine())

                .execute();

        System.out.println("代码生成完成！");
    }
}
