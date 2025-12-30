package com.evaluate.util;

import org.apache.poi.xwpf.usermodel.*;
import java.io.*;
import java.util.*;

public class TemplateModifier {

    private static final String TEMPLATE_PATH = "src/main/resources/templates/xxxx年四川省xx市xx县减灾能力评估技术报告-系统模板.bak";
    private static final String OUTPUT_PATH = "d:/Evaluation/evaluation/src/main/resources/templates/xxxx年四川省xx市xx县减灾能力评估技术报告-系统模板_v3.docx";

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
            System.out.println("Starting Template Modification for: " + TEMPLATE_PATH);
            
            FileInputStream fis = new FileInputStream(TEMPLATE_PATH);
            XWPFDocument document = new XWPFDocument(fis);
            fis.close();

            System.out.println("Document loaded. Paragraphs: " + document.getParagraphs().size() + ", Tables: " + document.getTables().size());
            
            int replacements = 0;
            
            // 1. Scan Body Paragraphs
            for (XWPFParagraph p : document.getParagraphs()) {
                boolean pShading = false;
                try {
                     // Check Paragraph Shading (CTP)
                     // Note: XWPFParagraph doesn't expose getCTP directly in older versions easily, but it has getCTP() usually.
                     // But we can check if we can access the underlying object or property.
                     // Or just rely on runs.
                     // p.getCTP() might be protected/internal in some POI versions or need casting.
                     // But XWPFParagraph usually has getCTP()
                     pShading = hasShading(p.getCTP());
                } catch (Exception e) { /* ignore */ }
                
                replacements += modifyRuns(p.getRuns(), "Body Paragraph", pShading);
            }
            
            // 2. Scan All Tables for Paragraphs (Recursive search for green text)
            for (XWPFTable tbl : document.getTables()) {
                for (XWPFTableRow row : tbl.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        boolean cellShading = false;
                        try {
                            cellShading = hasShading(cell.getCTTc());
                        } catch (Exception e) { /* ignore */ }
                        
                        for (XWPFParagraph p : cell.getParagraphs()) {
                            boolean pShading = false;
                            try {
                                pShading = hasShading(p.getCTP());
                            } catch (Exception e) { /* ignore */ }
                            
                            replacements += modifyRuns(p.getRuns(), "Table Cell Paragraph", cellShading || pShading);
                        }
                    }
                }
            }
            
            // 3. Modify Tables (Structure)
            modifyTables(document);

            // 3. Save
            // String outputPath = TEMPLATE_PATH.replace(".docx", "_v2.docx");
            String outputPath = OUTPUT_PATH;
            FileOutputStream fos = new FileOutputStream(outputPath);
            document.write(fos);
            fos.close();
            System.out.println("SUCCESS: Replaced " + replacements + " highlighted variables.");
            System.out.println("Saved to: " + outputPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // State for context detection
    private static String currentContext = "NONE"; // NONE, TOWNSHIP, COMMUNITY
    private static String groupContext = "NONE"; // STRONG_UP, MEDIUM_DOWN, MEDIUM_ONLY
    private static int statsIndex = 0; // 0 to 4
    private static boolean insideAnalysisBlock = false;

    private static boolean hasShading(Object obj) {
        try {
            String xml = obj.toString();
            if (xml.contains("<w:shd") && (xml.contains("w:fill=") || xml.contains("fill="))) {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile("fill=\"([^\"]+)\"");
                java.util.regex.Matcher m = p.matcher(xml);
                if (m.find()) {
                    String fill = m.group(1);
                    return !"auto".equals(fill) && !"000000".equals(fill) && !"none".equals(fill) && !"FFFFFF".equals(fill);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }

    private static int modifyRuns(List<XWPFRun> runs, String context) {
        return modifyRuns(runs, context, false);
    }

    private static int modifyRuns(List<XWPFRun> runs, String context, boolean inheritedHighlight) {
        int count = 0;
        if (runs == null || runs.isEmpty()) return 0;
        
        // 1. Detect Context from Paragraph Text
        StringBuilder paraTextBuilder = new StringBuilder();
        for (XWPFRun r : runs) paraTextBuilder.append(r.getText(0));
        String paraText = paraTextBuilder.toString();
        
        if (paraText.contains("乡镇（街道）") && paraText.contains("减灾能力")) {
            currentContext = "TOWNSHIP";
            statsIndex = 0;
        } else if (paraText.contains("社区（行政村）")) {
            currentContext = "COMMUNITY";
            statsIndex = 0;
        }
        
        // Reset group context per paragraph
        groupContext = "NONE";

        // Detect Context from Paragraph Text
        if (paraText.contains("强弱结对")) {
            groupContext = "STRONG_UP";
        }
        if (paraText.contains("综合减灾能力") && paraText.contains("包括")) {
             if (paraText.contains("较强")) groupContext = "STRONG_UP";
             if (paraText.contains("中等")) groupContext = "MEDIUM_ONLY";
             if (paraText.contains("以下")) groupContext = "MEDIUM_DOWN";
        }

        // Debug Context Logic
        if (paraText.contains("综合减灾能力")) {
            System.out.println("DEBUG PARAGRAPH (Analysis): " + paraText);
        }
        if (paraText.contains("瑞峰镇")) {
            System.out.println("DEBUG PARAGRAPH: " + paraText);
            for (XWPFRun r : runs) {
                System.out.println("  Run: [" + r.getText(0) + "]");
            }
        }
        insideAnalysisBlock = false;

        for (int i = 0; i < runs.size(); i++) {
            XWPFRun run = runs.get(i);
            String text = run.getText(0);
            if (text == null || text.trim().isEmpty()) continue;
            
            Object color = run.getTextHighlightColor();
            boolean isHighlighted = (color != null && !color.toString().equals("none"));
            
            if (inheritedHighlight) {
                isHighlighted = true;
            }

            // Debug specific text
            if (text.contains("14.29%") || text.contains("瑞峰镇") || text.contains("得益于")) {
                System.out.println("DEBUG RUN: [" + text + "]");
                System.out.println("  Highlight: " + run.getTextHighlightColor());
                try {
                    System.out.println("  XML: " + run.getCTR().toString());
                } catch (Exception e) {
                    System.out.println("  Error checking props: " + e.getMessage());
                }
            }

            // Check for Shading (via XML String check to avoid compilation issues)
            if (!isHighlighted) {
                 try {
                     // Check XML for <w:shd ... fill="..." ... />
                     // Note: run.getCTR().toString() might return the XML object string
                     String xml = run.getCTR().toString();
                     if (xml.contains("<w:shd") && (xml.contains("w:fill=") || xml.contains("fill="))) {
                         // Simple heuristic: if it has shading and fill is not "auto" or "000000" (implied by existence usually)
                         // But we should check the value.
                         // Regex to extract fill
                         java.util.regex.Pattern p = java.util.regex.Pattern.compile("fill=\"([^\"]+)\"");
                         java.util.regex.Matcher m = p.matcher(xml);
                         if (m.find()) {
                             String fill = m.group(1);
                             if (!"auto".equals(fill) && !"000000".equals(fill) && !"none".equals(fill) && !"FFFFFF".equals(fill)) {
                                 isHighlighted = true;
                                 System.out.println("Detected Shading via XML: " + text + " (Fill: " + fill + ")");
                             }
                         }
                     }
                 } catch (Exception e) {
                     // Ignore
                 }
            }

            // Check for Shading (via reflection or basic check if possible, skipping complex check to avoid errors)
            // If text is in the specific "Yellow Block" known from screenshot, treat as highlighted
            if (text.contains("瑞峰镇") || text.contains("得益于")) {
                // Assume highlighted for these specific known analysis keywords
                isHighlighted = true; 
            }

            String newText = text;
            boolean modified = false;

            // --- Context Update Logic ---
            if (newText.contains("中等及以下")) {
                groupContext = "MEDIUM_DOWN";
            } else if (newText.contains("较强及以上")) {
                groupContext = "STRONG_UP";
            } else if (newText.contains("中等的包括")) {
                groupContext = "MEDIUM_ONLY";
            }

            // --- Replacement Logic ---

            // 0. Analysis Block Handling (Yellow Block)
             // Stricter trigger: Must contain "得益于" (benefited from) which is characteristic of the analysis text
             // Or start with "具体而言" AND contain "综合减灾能力"
             boolean startOfAnalysis = isHighlighted && (
                 newText.contains("得益于") || 
                 (newText.contains("具体而言") && newText.contains("综合减灾能力")) ||
                 (newText.contains("综合减灾能力") && newText.contains("包括")) // "Includes" -> List context
             );
             
             // Exclude "Assessment Level" context (e.g., "处于...水平")
             if (newText.contains("处于") && newText.contains("水平")) {
                 startOfAnalysis = false;
             }

             if (startOfAnalysis) {
                  if (!insideAnalysisBlock) {
                      newText = "{{analysis_paragraph}}";
                      modified = true;
                      insideAnalysisBlock = true; // Mark that we entered the block
                  } else {
                      newText = ""; // Clear subsequent parts of the block
                      modified = true;
                  }
             }
            // If we are already in analysis block and this run looks like continuation (highlighted)
            else if (insideAnalysisBlock && isHighlighted) {
                 newText = "";
                 modified = true;
            }
            else {
                 // Reset if we hit non-highlighted text? 
                 // Risk: The block might be interrupted by non-highlighted punctuation. 
                 // But in the screenshot, the whole block is yellow.
                 // For safety, only reset if we see a clear "End" marker or new paragraph (which resets loop).
                 if (!isHighlighted && insideAnalysisBlock) {
                     // Maybe end of block
                     insideAnalysisBlock = false;
                 }
            }

            if (!modified) {
                // 0. Replace Year (2024 or 2025)
                if (newText.contains("2024年")) {
                    newText = newText.replace("2024年", "{{year}}年");
                    modified = true;
                } else if (newText.contains("2025年")) {
                    newText = newText.replace("2025年", "{{year}}年");
                    modified = true;
                }

                // 1. Replace County Name
                if (newText.contains("青神县")) {
                    newText = newText.replace("青神县", "{{county}}");
                    modified = true;
                }
                
                // 2. Grouped Stats: "5个（占比71.43%）"
                // Pattern: (\d+)个.*(\d+\.\d+%)
                if (newText.matches(".*\\d+个.*\\d+\\.\\d+%.*")) {
                     java.util.regex.Pattern p = java.util.regex.Pattern.compile("(\\d+)个.*[（(]占比(\\d+\\.\\d+%)[)）]");
                     java.util.regex.Matcher m = p.matcher(newText);
                     if (m.find()) {
                         String countVal = m.group(1);
                         String pctVal = m.group(2);
                         
                         String keyPrefix = "unknown";
                         if ("MEDIUM_DOWN".equals(groupContext)) keyPrefix = "medium_down";
                         else if ("STRONG_UP".equals(groupContext)) keyPrefix = "strong_up";
                         
                         if (!"unknown".equals(keyPrefix)) {
                             String replacement = "{{" + keyPrefix + "_count}}（占比{{" + keyPrefix + "_percent}}）";
                             newText = newText.replace(countVal + "个", "{{" + keyPrefix + "_count}}个")
                                              .replace(pctVal, "{{" + keyPrefix + "_percent}}");
                             modified = true;
                         }
                     }
                }
                // Handle "其余2个...（占比28.58%）" - split scenario?
                // Simple heuristic: if highlighted number "5" or "2" appears in this context
                else if (isHighlighted && newText.matches("\\d+")) {
                     if ("MEDIUM_DOWN".equals(groupContext)) {
                         newText = "{{medium_down_count}}";
                         modified = true;
                     } else if ("STRONG_UP".equals(groupContext)) {
                         newText = "{{strong_up_count}}";
                         modified = true;
                     }
                }
                else if (isHighlighted && newText.matches("\\d+\\.\\d+%")) {
                     if ("MEDIUM_DOWN".equals(groupContext)) {
                         newText = "{{medium_down_percent}}";
                         modified = true;
                     } else if ("STRONG_UP".equals(groupContext)) {
                         newText = "{{strong_up_percent}}";
                         modified = true;
                     }
                }

                // 3. Township Lists: "瑞峰镇和罗波乡"
                // Heuristic: Highlighted AND contains "镇" or "乡" AND (contains "、" or "和")
                if (isHighlighted && (newText.contains("镇") || newText.contains("乡")) && (newText.contains("、") || newText.contains("和"))) {
                    if ("STRONG_UP".equals(groupContext)) {
                        newText = "{{strong_up_townships_list}}";
                        modified = true;
                    } else if ("MEDIUM_ONLY".equals(groupContext)) {
                        newText = "{{medium_townships_list}}";
                        modified = true;
                    }
                }

                // 4. Stats Summary (Existing logic)
                if (!modified && newText.matches(".*\\d+[（(]\\d+\\.\\d+%[)）][、,].*")) {
                     String key = "TOWNSHIP".equals(currentContext) ? "{{township_stats_summary}}" : "{{community_stats_summary}}";
                     String summaryRegex = "(\\d+[（(]\\d+\\.\\d+%[)）][、,]?)+";
                     newText = newText.replaceAll(summaryRegex, key);
                     modified = true;
                } 
                else if (!modified && newText.matches(".*\\d+[（(]\\d+\\.\\d+%[)）].*")) {
                    // ... (Keep existing detailed stats logic) ...
                    StringBuffer sb = new StringBuffer();
                    java.util.regex.Pattern p = java.util.regex.Pattern.compile("(\\d+)[（(](\\d+\\.\\d+%)[)）]");
                    java.util.regex.Matcher m = p.matcher(newText);
                    while (m.find()) {
                        String keyPrefix = "";
                        if ("TOWNSHIP".equals(currentContext)) keyPrefix = ""; 
                        else if ("COMMUNITY".equals(currentContext)) keyPrefix = "community_"; 
                        else keyPrefix = "comprehensive_";
                        
                        String[] levels = {"strong", "mediumStrong", "medium", "weak", "veryWeak"};
                        String level = (statsIndex < levels.length) ? levels[statsIndex] : "unknown";
                        String replacement = "{{" + keyPrefix + level + "_count}}({{" + keyPrefix + level + "_percent}})";
                        m.appendReplacement(sb, replacement);
                        statsIndex++;
                        if (statsIndex >= 5) statsIndex = 0;
                    }
                    m.appendTail(sb);
                    newText = sb.toString();
                    modified = true;
                }
                
                // 5. Assessment Level (Existing)
                if (!modified && isHighlighted && (newText.contains("中等") || newText.contains("强") || newText.contains("弱")) && newText.length() < 15 && !newText.contains("%")) {
                    String key = "TOWNSHIP".equals(currentContext) ? "{{township_assessment_level}}" : "{{community_assessment_level}}";
                    newText = key;
                    modified = true;
                }
            }

            if (modified) {
                System.out.println("Modifying Run: " + text + " -> " + newText);
                run.setText(newText, 0);
                count++;
            }
        }
        return count;
    }

    private static void modifyTables(XWPFDocument doc) {
        modifyWeightTables(doc);

        // Table 6: Another Assessment List (Index 5)
        if (doc.getTables().size() >= 6) {
            XWPFTable table6 = doc.getTables().get(5); // Index 5 is Table 6
            System.out.println("Modifying Table 6 (Rows: " + table6.getRows().size() + ")");
            
            // Remove extra rows
            for (int i = table6.getRows().size() - 1; i > 1; i--) {
                table6.removeRow(i);
            }
            
            // Modify Template Row
            XWPFTableRow row = table6.getRow(1);
            int cellCount = row.getTableCells().size();
            System.out.println("Table 6 Cell Count: " + cellCount);
            
            setCellText(row.getCell(0), "{{t6_idx}}");
            setCellText(row.getCell(1), "{{t6_name}}");
            if (cellCount > 2) setCellText(row.getCell(2), "{{t6_c1}}");
            if (cellCount > 3) setCellText(row.getCell(3), "{{t6_c2}}");
            if (cellCount > 4) setCellText(row.getCell(4), "{{t6_c3}}");
            if (cellCount > 5) setCellText(row.getCell(5), "{{t6_c4}}");
            
            System.out.println("Table 6 modified to template.");
        }

        // Table 7: Community Assessment Results (Index 6)
        if (doc.getTables().size() >= 7) {
            XWPFTable table7 = doc.getTables().get(6); // Index 6 is Table 7
            System.out.println("Modifying Table 7 (Rows: " + table7.getRows().size() + ")");
            
            // Remove extra rows (from bottom up to avoid index shift)
            // Keep Header (0) and Template Row (1)
            for (int i = table7.getRows().size() - 1; i > 1; i--) {
                table7.removeRow(i);
            }
            
            // Modify Template Row (Row 2, index 1)
            XWPFTableRow row = table7.getRow(1);
            int cellCount = row.getTableCells().size();
            System.out.println("Table 7 Cell Count: " + cellCount);
            
            setCellText(row.getCell(0), "{{t7_idx}}");
            setCellText(row.getCell(1), "{{t7_name}}");
            if (cellCount > 2) setCellText(row.getCell(2), "{{t7_c1}}");
            if (cellCount > 3) setCellText(row.getCell(3), "{{t7_c2}}");
            if (cellCount > 4) setCellText(row.getCell(4), "{{t7_c3}}");
            if (cellCount > 5) setCellText(row.getCell(5), "{{t7_c4}}");
            if (cellCount > 6) setCellText(row.getCell(6), "{{t7_c5}}");
            
            System.out.println("Table 7 modified to template.");
        }

        // Table 9: Township Assessment Results
        // Structure: Header (R1) + Data Rows (R2...)
        // Action: Keep R1, R2. Replace R2 content. Delete R3+.
        if (doc.getTables().size() >= 9) {
            XWPFTable table9 = doc.getTables().get(8); // Index 8 is Table 9
            System.out.println("Modifying Table 9 (Rows: " + table9.getRows().size() + ")");
            
            // Remove extra rows (from bottom up to avoid index shift)
            // Keep Header (0) and Template Row (1)
            for (int i = table9.getRows().size() - 1; i > 1; i--) {
                table9.removeRow(i);
            }
            
            // Modify Template Row (Row 2, index 1)
            XWPFTableRow row = table9.getRow(1);
            int cellCount = row.getTableCells().size();
            System.out.println("Table 9 Cell Count: " + cellCount);
            
            setCellText(row.getCell(0), "{{t9_idx}}");
            setCellText(row.getCell(1), "{{t9_name}}");
            setCellText(row.getCell(2), "{{t9_comp}}");
            setCellText(row.getCell(3), "{{t9_mgt}}");
            setCellText(row.getCell(4), "{{t9_sup}}");
            if (cellCount > 5) {
                setCellText(row.getCell(5), "{{t9_self}}");
            }
            
            System.out.println("Table 9 modified to template.");
        }

        // Table 8: Community Stats per Township
        // Structure: Header + Data Rows + Total + Percent
        if (doc.getTables().size() >= 8) {
            XWPFTable table8 = doc.getTables().get(7); // Index 7 is Table 8
            System.out.println("Modifying Table 8 (Rows: " + table8.getRows().size() + ")");
            
            int totalRows = table8.getRows().size();
            
            if (totalRows >= 4) { // Header + 1 Data + Total + Percent
                int lastIdx = totalRows - 1;
                int secondLastIdx = totalRows - 2;
                
                // 1. Modify Footer Rows first (before deleting, indices are stable from bottom)
                XWPFTableRow pctRow = table8.getRow(lastIdx);
                setCellText(pctRow.getCell(0), "占比"); // Reset label to clear highlight
                setCellText(pctRow.getCell(1), ""); // Clear old value
                setCellText(pctRow.getCell(2), "{{t8_pct_c1}}");
                setCellText(pctRow.getCell(3), "{{t8_pct_c2}}");
                setCellText(pctRow.getCell(4), "{{t8_pct_c3}}");
                setCellText(pctRow.getCell(5), "{{t8_pct_c4}}");
                setCellText(pctRow.getCell(6), "{{t8_pct_c5}}");
                
                XWPFTableRow totalRow = table8.getRow(secondLastIdx);
                setCellText(totalRow.getCell(0), "合计"); // Reset label to clear highlight
                setCellText(totalRow.getCell(1), ""); // Clear old value
                setCellText(totalRow.getCell(2), "{{t8_total_c1}}");
                setCellText(totalRow.getCell(3), "{{t8_total_c2}}");
                setCellText(totalRow.getCell(4), "{{t8_total_c3}}");
                setCellText(totalRow.getCell(5), "{{t8_total_c4}}");
                setCellText(totalRow.getCell(6), "{{t8_total_c5}}");
                
                // 2. Modify First Data Row (Index 1? Or check if Header spans multiple rows?)
                // Usually Header is 1 row.
                XWPFTableRow dataRow = table8.getRow(1);
                // Check if it's actually data. If cell 0 is "Total", we are wrong.
                if (!dataRow.getCell(0).getText().contains("合计") && !dataRow.getCell(0).getText().contains("总计")) {
                    setCellText(dataRow.getCell(0), "{{t8_idx}}");
                    setCellText(dataRow.getCell(1), "{{t8_name}}");
                    setCellText(dataRow.getCell(2), "{{t8_c1}}");
                    setCellText(dataRow.getCell(3), "{{t8_c2}}");
                    setCellText(dataRow.getCell(4), "{{t8_c3}}");
                    setCellText(dataRow.getCell(5), "{{t8_c4}}");
                    setCellText(dataRow.getCell(6), "{{t8_c5}}");
                }
                
                // 3. Delete intermediate rows (from secondLastIdx-1 down to 2)
                for (int i = secondLastIdx - 1; i > 1; i--) {
                    table8.removeRow(i);
                }
                
                 System.out.println("Table 8 modified to template.");
            }
        }
    }

    private static void modifyWeightTables(XWPFDocument doc) {
        // Table 4: Weight Table (Index 3)
        if (doc.getTables().size() >= 4) {
            XWPFTable table4 = doc.getTables().get(3);
            System.out.println("Modifying Table 4 (Rows: " + table4.getRows().size() + ")");
            processFixedTable(table4, "t4");
        }

        // Table 5: Another Weight Table (Index 4)
        if (doc.getTables().size() >= 5) {
            XWPFTable table5 = doc.getTables().get(4);
            System.out.println("Modifying Table 5 (Rows: " + table5.getRows().size() + ")");
            processFixedTable(table5, "t5");
        }
    }

    private static void processFixedTable(XWPFTable table, String prefix) {
        int rowIdx = 0;
        for (XWPFTableRow row : table.getRows()) {
            rowIdx++;
            int colIdx = 0;
            for (XWPFTableCell cell : row.getTableCells()) {
                colIdx++;
                String text = cell.getText();
                if (text != null && text.matches(".*\\d+.*")) { // Simple heuristic: contains number
                     setCellText(cell, "{{" + prefix + "_r" + rowIdx + "_c" + colIdx + "}}");
                }
            }
        }
    }

    private static void setCellText(XWPFTableCell cell, String text) {
        if (cell == null) return;
        // Clear existing paragraphs
        for (int i = cell.getParagraphs().size() - 1; i >= 0; i--) {
            cell.removeParagraph(i);
        }
        cell.addParagraph().createRun().setText(text);
    }
}
