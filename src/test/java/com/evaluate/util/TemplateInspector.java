package com.evaluate.util;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STShd;
import java.io.*;
import java.util.*;

public class TemplateInspector {

    private static final String TEMPLATE_PATH = "src/main/resources/templates/xxxx年四川省xx市xx县减灾能力评估技术报告-系统模板.bak";

    public static void main(String[] args) {
        try {
            System.out.println("STARTING INSPECTION...");
            FileInputStream fis = new FileInputStream(TEMPLATE_PATH);
            XWPFDocument document = new XWPFDocument(fis);
            fis.close();
            System.out.println("Document loaded. Paragraphs: " + document.getParagraphs().size() + ", Tables: " + document.getTables().size());

            // 1. Scan ParagraphsSystem.out.println("Scanning for remaining highlights...");
            
            // 1. Scan Paragraphs
            for (XWPFParagraph p : document.getParagraphs()) {
                scanRuns(p.getRuns(), "Paragraph: " + (p.getText().length() > 20 ? p.getText().substring(0, 20) : p.getText()));
            }
            
            // 2. Scan Tables
            int tIdx = 0;
            for (XWPFTable t : document.getTables()) {
                tIdx++;
                analyzeTable(t, tIdx);
            }

            // 3. Scan Headers/Footers
            for (XWPFHeader header : document.getHeaderList()) {
                System.out.println("Scanning Header...");
                for (XWPFParagraph p : header.getParagraphs()) {
                    scanRuns(p.getRuns(), "Header");
                }
                for (XWPFTable t : header.getTables()) {
                    analyzeTable(t, -1);
                }
            }
            for (XWPFFooter footer : document.getFooterList()) {
                System.out.println("Scanning Footer...");
                for (XWPFParagraph p : footer.getParagraphs()) {
                    scanRuns(p.getRuns(), "Footer");
                }
                for (XWPFTable t : footer.getTables()) {
                     analyzeTable(t, -2);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void scanRuns(List<XWPFRun> runs, String context) {
        if (runs == null) return;
        for (XWPFRun run : runs) {
            String text = run.getText(0);
            if (text == null || text.trim().isEmpty()) continue;
            
            // 1. Check Highlight
            String color = run.getTextHighlightColor() != null ? run.getTextHighlightColor().toString() : "none";
            
            // 2. Check Shading (via XML string)
            String shading = "none";
            try {
                if (run.getCTR() != null) {
                    String xml = run.getCTR().toString();
                    if (xml.contains("<w:shd")) {
                         // Extract fill value roughly
                         int fillIdx = xml.indexOf("fill=\"");
                         if (fillIdx > 0) {
                             int endIdx = xml.indexOf("\"", fillIdx + 6);
                             if (endIdx > 0) {
                                 shading = xml.substring(fillIdx + 6, endIdx);
                             }
                         }
                    }
                }
            } catch (Exception e) {}

            if (!"none".equals(color) || !"none".equals(shading)) {
                 System.out.println("FOUND HIGHLIGHT/SHADING (H:" + color + ", S:" + shading + ") in [" + context + "]: " + text);
            }
            
            if (text.contains("{{") || text.toLowerCase().contains("xx")) {
                 System.out.println("FOUND POTENTIAL PLACEHOLDER in [" + context + "]: " + text);
            }
        }
    }

    private static void analyzeTable(XWPFTable table, int tableIndex) {
        // if (tableIndex > 3) return; // Limit removed
        System.out.println("--- Table " + tableIndex + " ---");
        int rowIndex = 0;
        for (XWPFTableRow row : table.getRows()) {
            rowIndex++;
            int colIndex = 0;
            StringBuilder rowContent = new StringBuilder();
            for (XWPFTableCell cell : row.getTableCells()) {
                colIndex++;
                String cellText = cell.getText().trim();
                rowContent.append(String.format("[%d,%d] %s | ", rowIndex, colIndex, cellText));
                
                String cellColor = cell.getColor();
                if (cellColor != null && !cellColor.equals("auto")) {
                     System.out.printf("FOUND CELL COLOR (%s) in [Table %d, R%d, C%d]: '%s'\n", cellColor, tableIndex, rowIndex, colIndex, cellText);
                }

                for (XWPFParagraph p : cell.getParagraphs()) {
                    scanRuns(p.getRuns(), "Table " + tableIndex + " R" + rowIndex + " C" + colIndex);
                }
            }
            if (tableIndex == 8 && rowIndex <= 3) { // Print first 3 rows of Table 8
                System.out.println("Row " + rowIndex + ": " + rowContent.toString());
            }
        }
    }

    private static void analyzeRuns(List<XWPFRun> runs, String source) {
        if (runs == null) return;
        
        for (XWPFRun run : runs) {
            String text = run.getText(0);
            if (text == null || text.trim().isEmpty()) continue;

            String highlight = null;
            if (run.getTextHighlightColor() != null) {
                highlight = run.getTextHighlightColor().toString();
            }

            // Check for potential variables (highlighted or braces)
            boolean isPotentialVar = false;
            if (highlight != null) isPotentialVar = true;
            if (text.contains("{{") || text.contains("${") || text.contains("xx") || text.contains("XX")) isPotentialVar = true;
            
            // Specifically look for Yellow or Green
            if (highlight != null) {
                 System.out.printf("[%s] Found Highlighted Text (%s): '%s'\n", source, highlight, text);
            }
            
            // Also print if it looks like a placeholder
            if (isPotentialVar && highlight == null) {
                System.out.printf("[%s] Found Potential Placeholder: '%s'\n", source, text);
            }
        }
    }
}
