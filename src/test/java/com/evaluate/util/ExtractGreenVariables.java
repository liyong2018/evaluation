package com.evaluate.util;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 提取Word文档中绿色高亮的变量，按照章节和表格结构组织成JSON
 */
public class ExtractGreenVariables {

    private static final String TEMPLATE_PATH = "xxxx年四川省xx市xx县减灾能力评估技术报告-系统模板.docx";

    // 当前章节上下文
    private static String currentChapter = "文档开始";
    private static String currentSection = "";
    private static int tableIndex = 0;

    public static void main(String[] args) {
        try {
            System.out.println("开始提取绿色高亮变量...");

            File file = new File(TEMPLATE_PATH);
            if (!file.exists()) {
                System.err.println("文件不存在: " + file.getAbsolutePath());
                return;
            }

            FileInputStream fis = new FileInputStream(file);
            XWPFDocument document = new XWPFDocument(fis);
            fis.close();

            // 使用LinkedHashMap保持顺序
            Map<String, Map<String, Set<String>>> structuredVars = new LinkedHashMap<>();
            Set<String> allVariables = new LinkedHashSet<>();

            // 扫描文档
            scanDocument(document, structuredVars, allVariables);

            document.close();

            // 构建JSON
            StringBuilder json = buildJson(structuredVars, allVariables);

            // 输出结果
            System.out.println("\n========== 结构化JSON ==========");
            System.out.println(json);

            // 保存到文件（使用UTF-8编码）
            String outputFile = "green_variables_structured.json";
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(outputFile), "UTF-8")) {
                writer.write(json.toString());
                System.out.println("\n结果已保存到: " + new File(outputFile).getAbsolutePath());
            }

            // 打印统计
            printStatistics(structuredVars, allVariables);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void scanDocument(XWPFDocument document,
                                     Map<String, Map<String, Set<String>>> structuredVars,
                                     Set<String> allVariables) {
        tableIndex = 0;
        currentChapter = "文档开始";
        currentSection = "";

        // 用于调试：收集所有颜色
        Set<String> allHighlightColors = new TreeSet<>();
        Set<String> allShadingColors = new TreeSet<>();

        List<IBodyElement> bodyElements = document.getBodyElements();

        for (int i = 0; i < bodyElements.size(); i++) {
            IBodyElement element = bodyElements.get(i);

            if (element instanceof XWPFParagraph) {
                XWPFParagraph para = (XWPFParagraph) element;
                processParagraph(para, structuredVars, allVariables, allHighlightColors, allShadingColors);
            } else if (element instanceof XWPFTable) {
                XWPFTable table = (XWPFTable) element;
                tableIndex++;
                processTable(table, structuredVars, allVariables, bodyElements, i, allHighlightColors, allShadingColors);
            }
        }

        // 打印所有发现的颜色
        System.out.println("\n========== 发现的所有颜色 ==========");
        System.out.println("高亮颜色: " + allHighlightColors);
        System.out.println("底纹颜色: " + allShadingColors);
    }

    private static void processParagraph(XWPFParagraph para,
                                         Map<String, Map<String, Set<String>>> structuredVars,
                                         Set<String> allVariables,
                                         Set<String> allHighlightColors,
                                         Set<String> allShadingColors) {
        String text = para.getText().trim();

        // 检查是否是标题（样式或格式）
        if (isHeading(para)) {
            String style = para.getStyle();
            if (style != null) {
                if (style.contains("Heading1") || style.contains("1")) {
                    currentChapter = text.isEmpty() ? "第" + (currentChapter.split("章").length) + "章" : text;
                    currentSection = "";
                } else if (style.contains("Heading2") || style.contains("2")) {
                    currentSection = text.isEmpty() ? currentChapter + ".节" : text;
                }
            } else if (text.matches(".*第.*章.*") || text.matches(".*\\d+\\..*")) {
                currentChapter = text;
                currentSection = "";
            }
        }

        // 提取绿色高亮/底纹变量
        List<XWPFRun> runs = para.getRuns();
        if (runs != null) {
            for (XWPFRun run : runs) {
                String runText = run.getText(0);
                if (runText != null && !runText.trim().isEmpty()) {
                    // 收集所有颜色用于调试
                    collectColors(run, allHighlightColors, allShadingColors);

                    if (isGreenBackground(run)) {
                        String var = runText.trim();
                        allVariables.add(var);

                        // 添加到结构化变量
                        String sectionKey = currentSection.isEmpty() ? currentChapter : currentChapter + " - " + currentSection;
                        addToStructure(structuredVars, sectionKey, "正文段落", var);
                    }
                }
            }
        }
    }

    private static void collectColors(XWPFRun run, Set<String> allHighlightColors, Set<String> allShadingColors) {
        // 收集高亮颜色
        String highlightColor = run.getTextHighlightColor() != null ?
            run.getTextHighlightColor().toString() : null;
        if (highlightColor != null) {
            allHighlightColors.add(highlightColor);
        }

        // 收集底纹颜色
        try {
            if (run.getCTR() != null) {
                String xml = run.getCTR().toString();
                if (xml.contains("<w:shd")) {
                    int fillIdx = xml.indexOf("fill=\"");
                    if (fillIdx > 0) {
                        int endIdx = xml.indexOf("\"", fillIdx + 6);
                        if (endIdx > 0) {
                            String fill = xml.substring(fillIdx + 6, endIdx);
                            allShadingColors.add(fill);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }
    }

    /**
     * 检查Run是否有绿色或黄色背景（高亮）
     * 支持多种颜色：green, yellow, lime 等
     */
    private static boolean isGreenBackground(XWPFRun run) {
        // 1. 检查高亮颜色 - 支持多种可能的"绿色"
        String highlightColor = run.getTextHighlightColor() != null ?
            run.getTextHighlightColor().toString() : null;
        if (isGreenOrYellowHighlight(highlightColor)) {
            return true;
        }

        // 2. 检查底纹颜色 (通过XML)
        try {
            if (run.getCTR() != null) {
                String xml = run.getCTR().toString();
                if (xml.contains("<w:shd")) {
                    // 提取fill值
                    int fillIdx = xml.indexOf("fill=\"");
                    if (fillIdx > 0) {
                        int endIdx = xml.indexOf("\"", fillIdx + 6);
                        if (endIdx > 0) {
                            String fill = xml.substring(fillIdx + 6, endIdx);
                            // 检查是否为绿色或黄色
                            if (isGreenColor(fill) || isYellowColor(fill)) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }

        return false;
    }

    /**
     * 检查高亮颜色是否为绿色或黄色相关
     */
    private static boolean isGreenOrYellowHighlight(String color) {
        if (color == null) return false;
        // 绿色系
        String[] greenColors = {"green", "lime", "teal", "aqua"};
        // 黄色系
        String[] yellowColors = {"yellow", "gold", "orange"};

        for (String c : greenColors) {
            if (c.equalsIgnoreCase(color)) return true;
        }
        for (String c : yellowColors) {
            if (c.equalsIgnoreCase(color)) return true;
        }
        return false;
    }

    /**
     * 检查颜色值是否为黄色
     */
    private static boolean isYellowColor(String color) {
        if (color == null || color.isEmpty()) {
            return false;
        }
        // Word常见黄色颜色值
        String[] yellowColors = {
            "FFFF00",      // 纯黄色
            "FFC000",      // 深黄色 (Word常用)
            "FFEB9C",      // 浅黄色
            "yellow",      // 英文yellow
            "gold",        // 英文gold
        };

        for (String yellow : yellowColors) {
            if (yellow.equalsIgnoreCase(color)) {
                return true;
            }
        }

        // 检查RGB格式 - 黄色通道为主
        if (color.matches("[0-9A-Fa-f]{6}")) {
            int r = Integer.parseInt(color.substring(0, 2), 16);
            int g = Integer.parseInt(color.substring(2, 4), 16);
            int b = Integer.parseInt(color.substring(4, 6), 16);
            // 黄色：红和绿都高，蓝色低
            return r > 200 && g > 200 && b < 100;
        }

        return false;
    }

    /**
     * 检查颜色值是否为绿色
     */
    private static boolean isGreenColor(String color) {
        if (color == null || color.isEmpty()) {
            return false;
        }
        // Word常见绿色颜色值
        String[] greenColors = {
            "00FF00",      // 纯绿色
            "92D050",      // 浅绿色 (Word常用)
            "00B050",      // 中绿色
            "00B050",      // 深绿色
            "4472C4",      // 可能的蓝色-绿色
            "70AD47",      // 另一种绿色
            "green",       // 英文green
            "lime",        // 英文lime
            "00FF00",      // 自动绿色
        };

        for (String green : greenColors) {
            if (green.equalsIgnoreCase(color)) {
                return true;
            }
        }

        // 检查RGB格式 - 绿色通道为主
        if (color.matches("[0-9A-Fa-f]{6}")) {
            int r = Integer.parseInt(color.substring(0, 2), 16);
            int g = Integer.parseInt(color.substring(2, 4), 16);
            int b = Integer.parseInt(color.substring(4, 6), 16);
            // 绿色通道明显大于红蓝通道
            return g > r && g > b && g > 100;
        }

        return false;
    }

    private static void processTable(XWPFTable table,
                                     Map<String, Map<String, Set<String>>> structuredVars,
                                     Set<String> allVariables, List<IBodyElement> bodyElements, int elementIndex,
                                     Set<String> allHighlightColors, Set<String> allShadingColors) {
        // 查找表格前的标题
        String tableCaption = "表格" + tableIndex;
        for (int i = elementIndex - 1; i >= 0 && i >= elementIndex - 5; i--) {
            IBodyElement prevElement = bodyElements.get(i);
            if (prevElement instanceof XWPFParagraph) {
                XWPFParagraph prevPara = (XWPFParagraph) prevElement;
                String prevText = prevPara.getText().trim();
                if (prevText.matches(".*表.*\\d+.*") || prevText.matches(".*Table.*\\d+.*")) {
                    tableCaption = prevText;
                    break;
                }
            }
        }

        // 提取表格中的绿色高亮/底纹变量
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph para : cell.getParagraphs()) {
                    List<XWPFRun> runs = para.getRuns();
                    if (runs != null) {
                        for (XWPFRun run : runs) {
                            String runText = run.getText(0);
                            if (runText != null && !runText.trim().isEmpty()) {
                                // 收集所有颜色用于调试
                                collectColors(run, allHighlightColors, allShadingColors);

                                if (isGreenBackground(run)) {
                                    String var = runText.trim();
                                    allVariables.add(var);

                                    String sectionKey = currentSection.isEmpty() ? currentChapter : currentChapter + " - " + currentSection;
                                    addToStructure(structuredVars, sectionKey, tableCaption, var);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isHeading(XWPFParagraph para) {
        String style = para.getStyle();
        if (style != null && (style.contains("Heading") || style.contains("heading"))) {
            return true;
        }
        String text = para.getText().trim();
        return text.matches(".*第.*章.*") || text.matches("^\\d+\\.") ||
               (para.getAlignment() != null && para.getAlignment() == ParagraphAlignment.CENTER && text.length() < 50);
    }

    private static void addToStructure(Map<String, Map<String, Set<String>>> structuredVars,
                                       String chapter, String subsection, String var) {
        structuredVars.computeIfAbsent(chapter, k -> new LinkedHashMap<>())
                      .computeIfAbsent(subsection, k -> new LinkedHashSet<>())
                      .add(var);
    }

    private static StringBuilder buildJson(Map<String, Map<String, Set<String>>> structuredVars,
                                          Set<String> allVariables) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"totalVariables\": ").append(allVariables.size()).append(",\n");
        json.append("  \"summary\": {\n");
        json.append("    \"totalChapters\": ").append(structuredVars.size()).append(",\n");
        json.append("    \"totalSections\": ").append(structuredVars.values().stream()
            .mapToInt(Map::size).sum()).append("\n");
        json.append("  },\n");
        json.append("  \"documentStructure\": {\n");

        boolean firstChapter = true;
        for (Map.Entry<String, Map<String, Set<String>>> chapter : structuredVars.entrySet()) {
            if (!firstChapter) {
                json.append(",\n");
            }
            firstChapter = false;
            json.append("    \"").append(escapeJson(chapter.getKey())).append("\": {\n");

            boolean firstSection = true;
            for (Map.Entry<String, Set<String>> section : chapter.getValue().entrySet()) {
                if (!firstSection) {
                    json.append(",\n");
                }
                firstSection = false;
                json.append("      \"").append(escapeJson(section.getKey())).append("\": [");
                boolean firstVar = true;
                for (String var : section.getValue()) {
                    if (!firstVar) {
                        json.append(", ");
                    }
                    firstVar = false;
                    json.append("\"").append(escapeJson(var)).append("\"");
                }
                json.append("]");
            }
            json.append("\n    }");
        }

        json.append("\n  }\n");
        json.append("}");
        return json;
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static void printStatistics(Map<String, Map<String, Set<String>>> structuredVars,
                                       Set<String> allVariables) {
        System.out.println("\n========== 统计信息 ==========");
        System.out.println("总变量数: " + allVariables.size());
        System.out.println("章节数: " + structuredVars.size());

        for (Map.Entry<String, Map<String, Set<String>>> chapter : structuredVars.entrySet()) {
            int chapterVars = chapter.getValue().values().stream().mapToInt(Set::size).sum();
            System.out.println("  " + chapter.getKey() + ": " + chapterVars + " 个变量 (" +
                chapter.getValue().size() + " 个区域)");
        }
    }
}
