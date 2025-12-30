package com.evaluate.util;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.FileInputStream;
import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class TemplateColorExtractor {

    private static final String FILE_PATH = "src/main/resources/templates/四川省眉山市青神县减灾能力评估技术报告-系统模板.docx";

    public static void main(String[] args) {
        // Let's analyze the document structure more deeply
        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             XWPFDocument document = new XWPFDocument(fis)) {

            Map<String, List<List<String>>> categorizedContent = new LinkedHashMap<>();
            String currentChapter = "Start of Document";
            categorizedContent.put(currentChapter, new ArrayList<>());

            // Use getBodyElements to iterate over Paragraphs and Tables in order
            for (IBodyElement element : document.getBodyElements()) {
                if (element instanceof XWPFParagraph) {
                    XWPFParagraph p = (XWPFParagraph) element;
                    if (isHeading(p)) {
                        currentChapter = p.getText().trim();
                        categorizedContent.putIfAbsent(currentChapter, new ArrayList<>());
                        continue;
                    }
                    List<String> paragraphSegments = extractFromParagraph(p);
                    if (!paragraphSegments.isEmpty()) {
                        categorizedContent.get(currentChapter).add(paragraphSegments);
                    }
                } else if (element instanceof XWPFTable) {
                    XWPFTable t = (XWPFTable) element;
                    for (XWPFTableRow row : t.getRows()) {
                        for (XWPFTableCell cell : row.getTableCells()) {
                            // Check if the cell itself has Green shading
                            boolean isCellGreen = false;
                            if (cell.getCTTc() != null && cell.getCTTc().getTcPr() != null && cell.getCTTc().getTcPr().getShd() != null) {
                                Object fillObj = cell.getCTTc().getTcPr().getShd().getFill();
                                if (fillObj != null) {
                                    String cellFill = String.valueOf(fillObj);
                                    if (isGreenish(cellFill)) {
                                        isCellGreen = true;
                                    }
                                }
                            }

                            for (XWPFParagraph p : cell.getParagraphs()) {
                                List<String> paragraphSegments;
                                if (isCellGreen) {
                                    // If cell is green, extract all text
                                    String text = p.getText();
                                    paragraphSegments = new ArrayList<>();
                                    if (text != null && !text.trim().isEmpty()) {
                                        paragraphSegments.add(text);
                                    }
                                } else {
                                    // Otherwise, check paragraph/run level shading
                                    paragraphSegments = extractFromParagraph(p);
                                }

                                if (!paragraphSegments.isEmpty()) {
                                    categorizedContent.get(currentChapter).add(paragraphSegments);
                                }
                            }
                        }
                    }
                }
            }

            // Output result to text file
            try (java.io.PrintWriter writer = new java.io.PrintWriter("green_extraction_result.txt", "UTF-8")) {
                for (Map.Entry<String, List<List<String>>> entry : categorizedContent.entrySet()) {
                    writer.println("=== Chapter: " + entry.getKey() + " ===");
                    List<List<String>> paragraphs = entry.getValue();
                    for (int i = 0; i < paragraphs.size(); i++) {
                        List<String> segments = paragraphs.get(i);
                        if (segments != null && !segments.isEmpty()) {
                            String joined = String.join(" ", segments);
                            writer.println("  [Paragraph " + (i + 1) + "] " + joined);
                        }
                    }
                    writer.println();
                }
            }

            // Generate JSON Output
            generateAndSaveJson(categorizedContent);

            System.out.println("Extraction complete. Check green_extraction_result.txt and green_extraction_result.json");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- JSON Structure Classes ---

    static class EvaluationReport {
        public RegionInfo regionInfo = new RegionInfo();
        public Indicators indicators = new Indicators();
        public Results results = new Results();
    }

    static class RegionInfo {
        public String name;
        public String year;
        public List<String> commonVariables = new ArrayList<>();
    }

    static class Indicators {
        public List<String> township = new ArrayList<>();
        public List<String> community = new ArrayList<>();
        public List<String> comprehensive = new ArrayList<>();
    }

    static class Results {
        public TownshipResult township = new TownshipResult();
        public CommunityResult community = new CommunityResult();
        public List<String> comprehensive = new ArrayList<>();
    }

    static class TownshipResult {
        public List<String> summary = new ArrayList<>();
        public List<String> strongTowns = new ArrayList<>();
        public List<String> weakTowns = new ArrayList<>();
    }

    static class CommunityResult {
        public List<String> townshipUnit = new ArrayList<>();
        public List<String> communityUnit = new ArrayList<>();
    }

    private static void generateAndSaveJson(Map<String, List<List<String>>> content) {
        EvaluationReport report = new EvaluationReport();

        for (Map.Entry<String, List<List<String>>> entry : content.entrySet()) {
            String chapter = entry.getKey();
            List<List<String>> paragraphs = entry.getValue();
            
            // Flatten paragraphs for easier processing
            List<String> flatLines = new ArrayList<>();
            for (List<String> p : paragraphs) {
                if (!p.isEmpty()) {
                    flatLines.add(String.join(" ", p));
                }
            }

            if (flatLines.isEmpty()) continue;

            // Logic to map chapters to JSON fields
            if (chapter.contains("Start of Document") || chapter.contains("2025") || chapter.contains("评估任务")) {
                report.regionInfo.commonVariables.addAll(flatLines);
                // Attempt to guess Region Name
                for (String line : flatLines) {
                    if (line.contains("县") || line.contains("区") || line.contains("市")) {
                        if (report.regionInfo.name == null) report.regionInfo.name = line;
                    }
                    if (line.contains("20")) {
                        if (report.regionInfo.year == null) report.regionInfo.year = line;
                    }
                }
            } else if (chapter.contains("指标") || (chapter.contains("评估") && !chapter.contains("结果") && !chapter.contains("任务") && !chapter.contains("方法"))) {
                if (chapter.contains("乡镇")) {
                    report.indicators.township.addAll(flatLines);
                } else if (chapter.contains("社区")) {
                    report.indicators.community.addAll(flatLines);
                } else if (chapter.contains("综合")) {
                    report.indicators.comprehensive.addAll(flatLines);
                } else if (chapter.contains("指标权重")) {
                    // Generic indicators bucket or try to classify
                     report.indicators.comprehensive.addAll(flatLines); // Defaulting
                }
            } else if (chapter.contains("结果")) {
                if (chapter.contains("乡镇")) {
                    if (chapter.contains("较强")) {
                        report.results.township.strongTowns.addAll(flatLines);
                    } else if (chapter.contains("较弱")) {
                        report.results.township.weakTowns.addAll(flatLines);
                    } else {
                        report.results.township.summary.addAll(flatLines);
                    }
                } else if (chapter.contains("社区")) {
                    // Heuristic to split Township Unit vs Community Unit if not separated by chapters
                    // But usually, if they are subsections, they might be captured as chapters or just body text.
                    // If captured as one big chapter:
                    report.results.community.communityUnit.addAll(flatLines); // Default to community unit bucket
                } else if (chapter.contains("综合")) {
                    report.results.comprehensive.addAll(flatLines);
                }
            }
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(new File("green_extraction_result.json"), report);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isHeading(XWPFParagraph p) {
        String style = p.getStyleID();
        String text = p.getText().trim();
        if (text.isEmpty()) return false;
        
        // Heuristic: Headings are usually short.
        if (text.length() > 60) {
            return false;
        }

        // Strategy 1: Style ID (Strong signal)
        if (style != null) {
             if (style.matches("^[1-9]$") || style.toLowerCase().startsWith("heading")) {
                 return true;
             }
        }

        // Strategy 2: Text Regex (Fallback)
        boolean isChapter = text.matches(".*第\\s*[一二三四五六七八九十]+\\s*章.*");
        boolean isSection = text.matches(".*[一二三四五六七八九十]+\\s*、.*");
        boolean isNumbered = text.matches("^\\s*\\d+\\..*");

        if (isChapter || isSection || isNumbered) {
            // TOC Filter: If it ends with a digit
            if (text.matches(".*\\d+$")) {
                return false; 
            }
            return true;
        }
        
        // Check for specific known headings that might not match regex
        if (text.contains("结果") || text.contains("评估") || text.contains("指标")) {
             // If it looks like a title (short, no punctuation at end usually)
             if (!text.endsWith("。") && text.length() < 40) {
                 return true;
             }
        }
        
        return false;
    }


    private static List<String> extractFromParagraph(XWPFParagraph p) {
        List<String> segments = new ArrayList<>();

        // 1. Check Paragraph Shading
        // If the whole paragraph is shaded Green, return the whole text.
        if (p.getCTP() != null && p.getCTP().getPPr() != null && p.getCTP().getPPr().getShd() != null) {
            Object fillObj = p.getCTP().getPPr().getShd().getFill();
            if (fillObj != null) {
                String pFill = String.valueOf(fillObj);
                if (isGreenish(pFill)) {
                    String text = p.getText();
                    if (text != null && !text.trim().isEmpty()) {
                        segments.add(text);
                    }
                    return segments;
                }
            }
        }

        StringBuilder currentGreenSegment = new StringBuilder();
        
        for (XWPFRun run : p.getRuns()) {
            String text = run.getText(0);
            if (text == null || text.trim().isEmpty()) {
                continue; 
            }

            boolean isGreen = false;

            // 1. Check Highlight
            if (run.getTextHighlightColor() != null) {
                String highlight = run.getTextHighlightColor().toString();
                if (highlight.equalsIgnoreCase("green")) {
                    isGreen = true;
                }
            }

            // 2. Check Shading (Background Color)
            if (!isGreen) {
                String shading = getShadingColor(run);
                if (shading != null && isGreenish(shading)) {
                    isGreen = true;
                }
            }

            if (isGreen) {
                currentGreenSegment.append(text);
            } else {
                // Not green, flush previous segment if exists
                if (currentGreenSegment.length() > 0) {
                    segments.add(currentGreenSegment.toString().trim());
                    currentGreenSegment.setLength(0);
                }
            }
        }
        
        // Flush remaining at end of paragraph
        if (currentGreenSegment.length() > 0) {
            segments.add(currentGreenSegment.toString().trim());
        }
        
        return segments;
    }

    private static void addContent(String chapter, Map<String, List<String>> contentMap, String text) {
        // Deprecated
    }

    private static String getShadingColor(XWPFRun run) {
        if (run.getCTR() == null) return null;
        try {
            // Parsing XML directly as XWPFRun doesn't expose shading easily in all versions
            String xml = run.getCTR().toString();
            if (xml.contains("<w:shd")) {
                int fillIdx = xml.indexOf("w:fill=\"");
                if (fillIdx > 0) {
                    int start = fillIdx + 8;
                    int end = xml.indexOf("\"", start);
                    if (end > 0) {
                        return xml.substring(start, end);
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private static boolean isYellowish(String hex) {
        if (hex == null) return false;
        hex = hex.toLowerCase();
        return hex.equals("yellow") || hex.contains("ffff00") || hex.contains("fff200"); // Add more yellow variants if needed
    }

    private static boolean isGreenish(String hex) {
        if (hex == null) return false;
        hex = hex.toLowerCase();
        return hex.equals("green") || hex.contains("00ff00") || hex.contains("008000") || hex.contains("92d050"); // Add more green variants
    }
}
