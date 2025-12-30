package com.evaluate.util;

import org.apache.poi.xwpf.usermodel.*;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class TemplateAutoFixer {

    private static final String SRC_PATH = "d:/Evaluation/evaluation/src/main/resources/templates/xxxx年四川省xx市xx县减灾能力评估技术报告-系统模板.docx";
    // 保存到同一目录，文件名加 _fixed
    private static final String DEST_PATH = "d:/Evaluation/evaluation/src/main/resources/templates/xxxx年四川省xx市xx县减灾能力评估技术报告-系统模板_fixed.docx";

    public static void main(String[] args) {
        try {
            System.out.println("开始修复模板...");
            FileInputStream fis = new FileInputStream(SRC_PATH);
            XWPFDocument document = new XWPFDocument(fis);
            fis.close();

            // 1. 全局文本替换
            replaceGlobalText(document);

            // 2. 基于上下文的变量替换
            processContextualVariables(document);

            // 3. 保存
            FileOutputStream fos = new FileOutputStream(DEST_PATH);
            document.write(fos);
            fos.close();
            document.close();
            
            System.out.println("模板修复完成: " + DEST_PATH);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void replaceGlobalText(XWPFDocument document) {
        // 简单遍历所有段落和表格进行文本替换
        for (XWPFParagraph p : document.getParagraphs()) {
            replaceInParagraph(p);
        }
        for (XWPFTable t : document.getTables()) {
            for (XWPFTableRow r : t.getRows()) {
                for (XWPFTableCell c : r.getTableCells()) {
                    for (XWPFParagraph p : c.getParagraphs()) {
                        replaceInParagraph(p);
                    }
                }
            }
        }
    }

    private static void replaceInParagraph(XWPFParagraph p) {
        List<XWPFRun> runs = p.getRuns();
        if (runs == null) return;
        
        for (XWPFRun run : runs) {
            String text = run.getText(0);
            if (text == null) continue;
            
            if (text.contains("xxxx年") || text.contains("2024") || text.contains("2025")) {
                text = text.replace("xxxx年", "{{year}}年")
                           .replace("2024", "{{year}}")
                           .replace("2025", "{{year}}"); // 慎重，但用户需求优先
                run.setText(text, 0);
            }
            if (text.contains("xx市")) {
                run.setText(text.replace("xx市", "{{city}}"), 0);
            }
            if (text.contains("xx县")) {
                run.setText(text.replace("xx县", "{{county}}"), 0);
            }
            if (text.contains("XX")) {
                run.setText(text.replace("XX", "{{year}}"), 0);
            }
        }
    }

    private static void processContextualVariables(XWPFDocument document) {
        String currentSection = null; // comprehensive, management, support, selfRescue, community
        
        List<IBodyElement> elements = document.getBodyElements();
        for (IBodyElement element : elements) {
            if (element.getElementType() == BodyElementType.PARAGRAPH) {
                XWPFParagraph p = (XWPFParagraph) element;
                String text = p.getText().trim();
                
                // 识别章节
                if (text.contains("综合减灾能力") && (text.contains("评估结果") || text.contains("分析"))) {
                    currentSection = "comprehensive";
                    System.out.println("进入章节: " + currentSection);
                } else if (text.contains("管理能力") && text.contains("评估")) {
                    currentSection = "management";
                    System.out.println("进入章节: " + currentSection);
                } else if (text.contains("保障能力") && text.contains("评估")) {
                    currentSection = "support";
                    System.out.println("进入章节: " + currentSection);
                } else if (text.contains("自救能力") && text.contains("评估")) {
                    currentSection = "selfRescue";
                    System.out.println("进入章节: " + currentSection);
                } else if (text.contains("社区") && text.contains("行政村") && text.contains("评估")) {
                    currentSection = "community";
                    System.out.println("进入章节: " + currentSection);
                }
                
                // 处理段落中的统计变量 (平均分、最高分等)
                if (currentSection != null && !text.isEmpty()) {
                    replaceStatsInParagraph(p, currentSection);
                }
                
            } else if (element.getElementType() == BodyElementType.TABLE) {
                XWPFTable table = (XWPFTable) element;
                if (currentSection != null) {
                    replaceTableVars(table, currentSection);
                }
            }
        }
    }
    
    private static void replaceStatsInParagraph(XWPFParagraph p, String prefix) {
        String text = p.getText();
        // 简单状态机：遇到关键字后，寻找下一个绿色数字
        // 关键字：平均、最高、最低
        
        List<XWPFRun> runs = p.getRuns();
        String expecting = null; // avg, max, min
        
        for (XWPFRun run : runs) {
            String runText = run.getText(0);
            if (runText == null) continue;
            
            // 检查关键字
            if (runText.contains("平均")) expecting = "avg";
            else if (runText.contains("最高")) expecting = "max";
            else if (runText.contains("最低")) expecting = "min";
            
            // 检查是否为目标变量 (绿色/黄色/数字)
            String color = run.getTextHighlightColor() != null ? run.getTextHighlightColor().toString() : "";
            boolean isDigit = false;
            String trimmed = runText.trim();
            if (!trimmed.isEmpty()) {
                isDigit = Character.isDigit(trimmed.charAt(0));
            }
            boolean isTarget = "green".equalsIgnoreCase(color) || "yellow".equalsIgnoreCase(color) || isDigit;
            
            if (expecting != null && isTarget) {
                String placeholder = "{{" + prefix + "_" + expecting + "_score}}";
                System.out.println("  替换统计变量: " + runText + " -> " + placeholder);
                run.setText(placeholder, 0);
                run.setTextHighlightColor("none"); // 移除高亮
                expecting = null; // 重置
            }
        }
    }
    
    private static void replaceTableVars(XWPFTable table, String prefix) {
        // 遍历行，寻找等级关键字
        Map<String, String> levelMap = new HashMap<>();
        levelMap.put("强", "strong");
        levelMap.put("较强", "mediumStrong");
        levelMap.put("中等", "medium");
        levelMap.put("较弱", "weak");
        levelMap.put("弱", "veryWeak");
        
        for (XWPFTableRow row : table.getRows()) {
            String firstCellText = "";
            if (row.getTableCells().size() > 0) {
                firstCellText = row.getTableCells().get(0).getText().trim();
            }
            
            // 确定当前行对应的等级
            String levelKey = null;
            for (String key : levelMap.keySet()) {
                if (firstCellText.contains(key)) {
                    levelKey = levelMap.get(key);
                    break;
                }
            }
            
            if (levelKey != null) {
                // 在该行中寻找绿色/黄色文本进行替换
                int foundCount = 0;
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph p : cell.getParagraphs()) {
                        for (XWPFRun run : p.getRuns()) {
                            String text = run.getText(0);
                            if (text == null || text.trim().isEmpty()) continue;
                            
                            String color = run.getTextHighlightColor() != null ? run.getTextHighlightColor().toString() : "";
                            // 如果是绿色/黄色或者是纯数字
                            boolean isTarget = "green".equalsIgnoreCase(color) || "yellow".equalsIgnoreCase(color) || text.matches("^[0-9\\.]+$") || text.contains("%");
                            
                            // 忽略第一列（等级名称）
                            if (cell == row.getTableCells().get(0)) continue;
                            
                            if (isTarget) {
                                foundCount++;
                                String placeholder = "";
                                if (foundCount == 1) {
                                    // 数量
                                    placeholder = "{{" + prefix + "_" + levelKey + "_count}}";
                                } else if (foundCount == 2) {
                                    // 占比
                                    placeholder = "{{" + prefix + "_" + levelKey + "_percent}}";
                                } else if (foundCount >= 3) {
                                    // 列表 (通常列表也是高亮的，或者就是占位符)
                                    // 注意：列表可能是一个长字符串
                                    placeholder = "{{" + prefix + "_" + levelKey + "_list}}";
                                }
                                
                                if (!placeholder.isEmpty()) {
                                    System.out.println("  替换表格变量: " + text + " -> " + placeholder);
                                    run.setText(placeholder, 0);
                                    run.setTextHighlightColor("none");
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
