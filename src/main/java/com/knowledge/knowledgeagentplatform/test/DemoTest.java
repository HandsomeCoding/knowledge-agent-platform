package com.knowledge.knowledgeagentplatform.test;

import com.knowledge.knowledgeagentplatform.common.utils.DocumentSplitUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

public class DemoTest {

    private static final Pattern[] TIER_PATTERNS = {
            Pattern.compile("(?<=\\n)"),              // 1档：行边界（最大）
            Pattern.compile("(?<=[。？！.?!])"),      // 2档：完整句末（中英文）
            Pattern.compile("(?<=[；，;,\\s])"),      // 3档：半句 + 词边界（中英文）
    };

    public static void main(String[] args) {
        String desktopPath = System.getProperty("user.home") + "/Desktop";
        // 2. 拼接文件名（记得把 demo.md 换成你自己的文件名）
        Path filePath = Paths.get(desktopPath, "test.md");

        // 3. 读取内容（推荐指定 UTF-8，防止中文乱码）
        try {
            String content = Files.readString(filePath, StandardCharsets.UTF_8);

            System.out.println("开始切片");
            List<String> split = DocumentSplitUtils.split(content);

            System.out.println("切片完成  总个数为" + split.size());
            System.out.println("开始打印");
            for (int i = 0; i < split.size(); i++) {
                System.out.println("第 " + (i + 1) + " 个切片,内容为： " + split.get(i));
            }
            System.out.println("打印结束");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("文件未找到或读取失败");
        }
    }
}
