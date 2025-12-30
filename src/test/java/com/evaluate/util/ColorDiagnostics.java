package com.evaluate.util;

import org.apache.poi.xwpf.usermodel.*;
import java.io.*;
import java.util.*;

/**
 * 诊断程序：扫描Word文档中所有有颜色的文本
 */
public class ColorDiagnostics {

    private static final String TEMPLATE_PATH = "xxxx年四川省xx市xx县减灾能力评估技术报告-系统模板.docx";

    public static void main(String[] args) {
        try {
            System.out.println("开始扫描Word文档中的颜色...");

            File file = new File(TEMPLATE_PATH);
            if (!file.exists()) {
                System.err.println("文件不存在: " + file.getAbsolutePath());
                return;
            }

            FileInputStream fis = new FileInputStream(file);
            XWPFDocument document = new XWPFDocument(fis);
            fis.close();

            // 收集所有颜色
            Map<String, Integer> highlightColors = new TreeMap<>();
            Map<String, Integer> shadingColors = new TreeMap<>();
            Map<String, List<String>> colorSamples = new HashMap<>();

            // 扫描段落
            System.out.println("\n=== 扫描段落 ===");
            for (XWPFParagraph p : document.getParagraphs()) {
                scanRuns(p.getRuns(), "段落", highlightColors, shadingColors, colorSamples);
            }

            // 扫描表格
            System.out.println("\n=== 扫描表格 ===");
            int tableCount = 0;
            for (XWPFTable table : document.getTables()) {
                tableCount++;
                System.out.println("表格 " + tableCount);
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph p : cell.getParagraphs()) {
                            scanRuns(p.getRuns(), "表格" + tableCount, highlightColors, shadingColors, colorSamples);
                        }
                    }
                }
            }

            document.close();

            // 打印颜色统计
            System.out.println("\n========== 高亮颜色统计 ==========");
            for (Map.Entry<String, Integer> entry : highlightColors.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue() + " 次");
            }

            System.out.println("\n========== 底纹颜色统计 ==========");
            for (Map.Entry<String, Integer> entry : shadingColors.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue() + " 次");
            }

            System.out.println("\n========== 颜色样本 ==========");
            for (Map.Entry<String, List<String>> entry : colorSamples.entrySet()) {
                System.out.println("\n颜色 [" + entry.getKey() + "] 的样本:");
                for (String sample : entry.getValue()) {
                    if (sample.length() > 50) {
                        sample = sample.substring(0, 50) + "...";
                    }
                    System.out.println("  - " + sample);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void scanRuns(List<XWPFRun> runs, String source,
                                   Map<String, Integer> highlightColors,
                                   Map<String, Integer> shadingColors,
                                   Map<String, List<String>> colorSamples) {
        if (runs == null) return;

        for (XWPFRun run : runs) {
            String text = run.getText(0);
            if (text == null || text.trim().isEmpty()) continue;

            // 检查高亮颜色
            String highlight = run.getTextHighlightColor() != null ?
                run.getTextHighlightColor().toString() : null;
            if (highlight != null) {
                highlightColors.merge(highlight, 1, Integer::sum);
                addSample(colorSamples, "高亮-" + highlight, text);
            }

            // 检查底纹颜色
            try {
                if (run.getCTR() != null) {
                    String xml = run.getCTR().toString();
                    if (xml.contains("<w:shd")) {
                        int fillIdx = xml.indexOf("fill=\"");
                        if (fillIdx > 0) {
                            int endIdx = xml.indexOf("\"", fillIdx + 6);
                            if (endIdx > 0) {
                                String fill = xml.substring(fillIdx + 6, endIdx);
                                shadingColors.merge(fill, 1, Integer::sum);
                                addSample(colorSamples, "底纹-" + fill, text);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // 忽略异常
            }
        }
    }

    private static void addSample(Map<String, List<String>> colorSamples, String colorKey, String text) {
        colorSamples.computeIfAbsent(colorKey, k -> new ArrayList<>());
        // 最多保存10个样本
        if (colorSamples.get(colorKey).size() < 10) {
            colorSamples.get(colorKey).add(text.trim());
        }
    }
}
