package com.knowledge.knowledgeagentplatform.common.utils;

import com.knowledge.knowledgeagentplatform.common.exception.BusinessException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 文本切片工具（V1：段落优先 + 长度兜底 + 带重叠）
 * <p>
 * 设计原则（依据用户要求）：
 * - 按段落优先：优先在 {@code \n\n}（段落）边界断开，最大程度保留自然语义
 * - 按长度兜底：段落过长时按 行 → 句末（。？！.?！）→ 半句（；，;,）→ 词边界
 * 逐级降级切分，最后按字符长度滑窗兜底
 * - 带重叠：相邻切片共享 overlap 字符，缓解跨缝信息丢失
 * <p>
 * V1 显式不做（后期按需演进）：
 * - 语义切片（embedding 检测边界）
 * - token 精确切片
 * - Markdown 结构感知
 * <p>
 * 推荐初始参数（中文语料）：
 * - chunkSize = 500（字符）
 * - overlap   = 100（字符，约 20%）
 */
public class DocumentSplitUtils {

    private static final int DEFAULT_CHUNK_SIZE = 500;
    private static final int DEFAULT_OVERLAP = 100;
    private static final int MIN_CHUNK_SIZE = 50;


    /**
     * 降级切分档位：从大粒度到小粒度，共 3 档，逐档尝试
     * <p>
     * 同档符号合并为一个正则（lookbehind 断言），一次 String.split 完成整档切分——
     * 分隔符保留在每片尾部（切片落在语义边界且上下文连贯）。
     * <p>
     * 选入标准：符号必须标记"语义单元的结束"（行/句末/半句），
     * 结构性符号（< > [ ] 等 HTML/Markdown 标记）不是语义边界——
     * 会把链接 []()、标签 <a>、代码 ArrayList<String> 切碎，禁止加入。
     * <p>
     * 注意：英文句号 "." 有误切风险（小数 3.14、URL、扩展名 .pdf），
     * V1 接受该风险；若实际文档中问题频发，V2 换正则 (?<!\d)\.(?!\d)。
     */
    private static final Pattern[] TIER_PATTERNS = {
            Pattern.compile("(?<=\\n)"),              // 1档：行边界（最大）
            Pattern.compile("(?<=[。？！.?!])"),      // 2档：完整句末（中英文）
            Pattern.compile("(?<=[；，;,\\s])"),      // 3档：半句 + 词边界（中英文）
    };

    private DocumentSplitUtils() {
    }

    /**
     * 使用默认 配置来进行分割
     *
     * @param text
     * @return
     */
    public static List<String> split(String text) {
        return split(text, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP);
    }

    public static List<String> split(String text, int chunkSize, int overlap) {
        //参数校验
        if (text == null || text.isBlank()) throw new BusinessException("文件内容不能为空");
        if (chunkSize <= 0) throw new BusinessException("文件块大小不能为0或者负数");
        if (overlap < 0) throw new BusinessException("overlap需大于0");
        if (chunkSize < overlap) throw new BusinessException("参数设置不合理，chunkSize应大于overtlap");
        //对文档进行清洗  去空行
        String content = normalize(text);
        //空直接返回空
        if (content.isBlank()) return List.of();
        //且切割为单元
        List<String> chunks = new ArrayList<>();
        //切割好了然后进行匹配
        List<String> units = splitToUnits(text, chunkSize);

        StringBuilder current = new StringBuilder();
        for (String unit : units) {
            //尽可能拼接小的块
            if (current.length() + unit.length() < chunkSize) {
                current.append(unit);
                continue;
            }
            //current有值的 直接提交
            if (!current.isEmpty()) {
                chunks.add(current.toString().trim());
                current = new StringBuilder(getOverlap(current.toString().trim()));
            }
            //切片很大
            if (unit.length() > chunkSize) {
                List<String> forceChunks = forceSplit(unit);
                //最后一篇单独处理
                for (int i = 0; i < forceChunks.size() - 1; i++) {
                    current.append(forceChunks.get(i).trim());
                    chunks.add(current.toString().trim());
                    current = new StringBuilder(getOverlap(chunks.get(chunks.size() - 1).trim()));
                }
                current.append(forceChunks.get(forceChunks.size() - 1));
            } else {
                current.append(unit);
            }
            //方便处理最后一块
            if (!current.isEmpty()) {
                String lastchunk = current.toString().trim();
                //最后一片很小时候,和前面一片进行存储了,就不自己进行修改了
                if (lastchunk.length() < MIN_CHUNK_SIZE && !chunks.isEmpty()) {
                    chunks.set(chunks.size() - 1, lastchunk + chunks.get(chunks.size() - 1));
                } else {
                    chunks.add(current.toString().trim());
                }
            }
        }
        return chunks;
    }


    /**
     * 把文档切割问单元
     * 段位，行，标点符号切割
     *
     * @param content
     * @param chunkSize
     * @return
     */
    private static List<String> splitToUnits(String content, int chunkSize) {
        if (content.isBlank()) return List.of();

        List<String> units = new ArrayList<>();
        //根据段位划分
        String[] paragraphs = content.split("\\n\\n");

        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();

            if (paragraph.isBlank()) continue;

            if (paragraph.length() <= chunkSize) {
                units.add(paragraph);
                continue;
            }
            //根据符号拆分
            for (Pattern tierPattern : TIER_PATTERNS) {
                String[] split = paragraph.split(tierPattern.pattern());
                for (String temp : split) {
                    if (!temp.isBlank()) units.add(temp);
                }
            }

        }

        return units;
    }

    /**
     * 获取当前文档的片尾 方便后续拼接片头
     *
     * @return
     */
    public static String getOverlap(String content) {

        if (content.length() < DEFAULT_OVERLAP) return content;

        return content.substring(content.length() - DEFAULT_OVERLAP);
    }

    /**
     * 根据固定大小的字符进行分割
     *
     * @param content
     * @return
     */
    public static List<String> forceSplit(String content) {
        if (content.isBlank()) return List.of();
        //记录返回结果
        List<String> returnList = new ArrayList<>();
        for (int start = 0; start < content.length(); start += DEFAULT_CHUNK_SIZE) {
            int end = Math.min(start + DEFAULT_CHUNK_SIZE, content.length());
            returnList.add(content.substring(start, end));
        }
        return returnList;
    }

    /**
     * 清洗多余空格；保留段落边界。
     * <p>
     * 后续考虑要不要抽出去
     */
    public static String normalize(String rawContent) {
        if (rawContent == null) {
            return "";
        }

        return rawContent
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

}
