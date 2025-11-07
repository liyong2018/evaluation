package com.evaluate.util;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Utility for processing a Word (.docx) template and generating a new document
 * with placeholder replacements and background color handling.
 *
 * 3 core capabilities exposed as methods:
 * - loadTemplate(Path): load a .docx template into XWPFDocument
 * - process(XWPFDocument, Map<String,String>, List<String>): replace placeholders and apply formats
 * - write(XWPFDocument, Path): write to a target .docx file
 *
 * Guidelines/assumptions for the template:
 * - Placeholders use a single-run token pattern like ${key} and are not split across runs.
 * - Green-background text blocks in the template will keep their green background after replacement.
 * - Keys listed in highlightKeys will be highlighted with yellow background after replacement.
 */
public final class DocxTemplateProcessor {

    // Typical green fills used by Word (hex without #)
    private static final Set<String> GREEN_FILLS = new HashSet<>(Arrays.asList(
            "00FF00", // pure green
            "92D050", // Office theme green
            "00B050"  // another theme green
    ));

    // Yellow fill (hex without #)
    private static final String YELLOW_FILL = "FFFF00";

    private DocxTemplateProcessor() {}

    /** Load .docx template */
    public static XWPFDocument loadTemplate(Path templatePath) throws IOException {
        Objects.requireNonNull(templatePath, "templatePath");
        if (!Files.exists(templatePath)) {
            throw new IOException("Template not found: " + templatePath);
        }
        try (FileInputStream fis = new FileInputStream(templatePath.toFile())) {
            return new XWPFDocument(fis);
        }
    }

    /** Process document with replacement map and highlight keys (List). */
    public static void process(XWPFDocument document,
                               Map<String, String> replacements,
                               List<String> highlightKeys) {
        Objects.requireNonNull(document, "document");
        Objects.requireNonNull(replacements, "replacements");
        Set<String> highlightSet = highlightKeys == null ? Collections.emptySet() : new HashSet<>(highlightKeys);

        // Body paragraphs
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            processParagraph(paragraph, replacements, highlightSet);
        }

        // Tables
        for (XWPFTable table : document.getTables()) {
            processTable(table, replacements, highlightSet);
        }

        // Headers/Footers
        for (XWPFHeader header : document.getHeaderList()) {
            header.getParagraphs().forEach(p -> processParagraph(p, replacements, highlightSet));
            header.getTables().forEach(t -> processTable(t, replacements, highlightSet));
        }
        for (XWPFFooter footer : document.getFooterList()) {
            footer.getParagraphs().forEach(p -> processParagraph(p, replacements, highlightSet));
            footer.getTables().forEach(t -> processTable(t, replacements, highlightSet));
        }

        // Second pass: highlight arbitrary contents (substring match) if provided
        if (!highlightSet.isEmpty()) {
            highlightDocument(document, new ArrayList<>(highlightSet));
        }
    }

    /** Write document to disk */
    public static void write(XWPFDocument document, Path outputPath) throws IOException {
        Objects.requireNonNull(document, "document");
        Objects.requireNonNull(outputPath, "outputPath");
        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }
        try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
            document.write(fos);
        }
    }

    /** Convenience one-shot call: load + process + write */
    public static void processDocx(Path templatePath,
                                   Path outputPath,
                                   Map<String, String> replacements,
                                   Set<String> highlightKeys) throws IOException {
        try (XWPFDocument doc = loadTemplate(templatePath)) {
            process(doc, replacements, highlightKeys == null ? null : new ArrayList<>(highlightKeys));
            write(doc, outputPath);
        }
    }

    private static void processTable(XWPFTable table,
                                     Map<String, String> replacements,
                                     Set<String> highlightKeys) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    processParagraph(paragraph, replacements, highlightKeys);
                }
                for (XWPFTable nested : cell.getTables()) {
                    processTable(nested, replacements, highlightKeys);
                }
            }
        }
    }

    private static void processParagraph(XWPFParagraph paragraph,
                                         Map<String, String> replacements,
                                         Set<String> highlightKeys) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs == null || runs.isEmpty()) return;

        for (int i = 0; i < runs.size(); i++) {
            XWPFRun run = runs.get(i);
            String text = run.text();
            if (text == null || text.isEmpty()) continue;

            // Replace placeholders in this run if present (pattern ${key})
            boolean replaced = false;
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                if (text.contains(placeholder)) {
                    text = text.replace(placeholder, entry.getValue() == null ? "" : entry.getValue());
                    replaced = true;
                    // If highlight list includes key or replacement value, mark yellow
                    if (highlightKeys.contains(entry.getKey()) ||
                        (entry.getValue() != null && highlightKeys.contains(entry.getValue()))) {
                        setRunBackground(run, YELLOW_FILL);
                    }
                }
            }

            if (replaced) {
                // Update run text preserving formatting
                run.setText(text, 0);
            }
        }

        // Optional: normalize spacing if the paragraph contains only shading blocks
        normalizeParagraphSpacing(paragraph);
    }

    private static void normalizeParagraphSpacing(XWPFParagraph paragraph) {
        // Keep it minimal: ensure no null text runs remain
        List<XWPFRun> runs = new ArrayList<>(paragraph.getRuns());
        for (XWPFRun run : runs) {
            if (run.text() == null) {
                // Remove empty CTR if possible
                CTR ctr = run.getCTR();
                CTP ctp = paragraph.getCTP();
                XmlCursor cursor = ctr.newCursor();
                cursor.removeXml();
                cursor.dispose();
                // Re-attach a blank run if paragraph gets empty automatically by POI
                if (paragraph.getRuns().isEmpty()) {
                    paragraph.createRun();
                }
            }
        }
    }

    private static void setRunBackground(XWPFRun run, String hexFillNoHash) {
        CTR ctr = run.getCTR();
        if (!ctr.isSetRPr()) ctr.addNewRPr();
        CTShd shd = ctr.getRPr().isSetShd() ? ctr.getRPr().getShd() : ctr.getRPr().addNewShd();
        shd.setFill(hexFillNoHash);
    }

    /**
     * Check if the run currently has a green background (for information/debugging or downstream logic).
     */
    @SuppressWarnings("unused")
    private static boolean hasGreenBackground(XWPFRun run) {
        CTR ctr = run.getCTR();
        if (ctr.isSetRPr() && ctr.getRPr().isSetShd()) {
            CTShd shd = ctr.getRPr().getShd();
            String fill = shd.getFill();
            return fill != null && GREEN_FILLS.contains(fill.toUpperCase());
        }
        return false;
    }

    /** Allow caller to override acceptable green fill hex values at runtime. */
    public static void setGreenFills(Collection<String> hexFillsWithoutHash) {
        GREEN_FILLS.clear();
        if (hexFillsWithoutHash != null) {
            for (String f : hexFillsWithoutHash) {
                if (f != null) GREEN_FILLS.add(f.toUpperCase());
            }
        }
    }

    // ===== Highlight arbitrary substrings by splitting runs =====
    private static void highlightDocument(XWPFDocument doc, List<String> contents) {
        if (contents == null || contents.isEmpty()) return;
        // paragraphs
        for (XWPFParagraph p : doc.getParagraphs()) {
            highlightInParagraph(p, contents);
        }
        // tables
        for (XWPFTable t : doc.getTables()) {
            highlightInTable(t, contents);
        }
        // header/footer
        for (XWPFHeader header : doc.getHeaderList()) {
            header.getParagraphs().forEach(p -> highlightInParagraph(p, contents));
            header.getTables().forEach(t -> highlightInTable(t, contents));
        }
        for (XWPFFooter footer : doc.getFooterList()) {
            footer.getParagraphs().forEach(p -> highlightInParagraph(p, contents));
            footer.getTables().forEach(t -> highlightInTable(t, contents));
        }
    }

    private static void highlightInTable(XWPFTable table, List<String> contents) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph p : cell.getParagraphs()) {
                    highlightInParagraph(p, contents);
                }
                for (XWPFTable nested : cell.getTables()) {
                    highlightInTable(nested, contents);
                }
            }
        }
    }

    private static void highlightInParagraph(XWPFParagraph paragraph, List<String> contents) {
        if (contents == null || contents.isEmpty()) return;
        List<XWPFRun> runs = new ArrayList<>(paragraph.getRuns());
        if (runs.isEmpty()) return;

        // Build regex pattern for all contents (literal match)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < contents.size(); i++) {
            String c = contents.get(i);
            if (c == null || c.isEmpty()) continue;
            if (sb.length() > 0) sb.append("|");
            sb.append(java.util.regex.Pattern.quote(c));
        }
        if (sb.length() == 0) return;
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(sb.toString());

        for (XWPFRun run : runs) {
            String text = run.text();
            if (text == null || text.isEmpty()) continue;

            java.util.regex.Matcher m = pattern.matcher(text);
            if (!m.find()) continue;

            int runIndex = paragraph.getRuns().indexOf(run);
            String fill = getRunFill(run);

            int last = 0;
            List<Fragment> fragments = new ArrayList<>();
            do {
                int start = m.start();
                int end = m.end();
                if (start > last) {
                    fragments.add(new Fragment(text.substring(last, start), false));
                }
                fragments.add(new Fragment(text.substring(start, end), true));
                last = end;
            } while (m.find());
            if (last < text.length()) {
                fragments.add(new Fragment(text.substring(last), false));
            }

            // Remove original run content
            clearRun(run);

            // Insert new runs with formatting copied
            int insertPos = runIndex;
            for (Fragment f : fragments) {
                XWPFRun nr = paragraph.insertNewRun(insertPos++);
                copyFormatting(run, nr);
                if (f.highlight) {
                    setRunBackground(nr, YELLOW_FILL);
                } else if (fill != null) {
                    setRunBackground(nr, fill);
                }
                nr.setText(f.text);
            }
        }
    }

    private static void clearRun(XWPFRun run) {
        CTR ctr = run.getCTR();
        CTP ctp = (CTP) ctr.getDomNode().getParentNode();
        XmlCursor cursor = ctr.newCursor();
        cursor.removeXml();
        cursor.dispose();
    }

    private static void copyFormatting(XWPFRun src, XWPFRun dst) {
        dst.setBold(src.isBold());
        dst.setItalic(src.isItalic());
        dst.setStrike(src.isStrikeThrough());
        dst.setUnderline(src.getUnderline());
        dst.setColor(src.getColor());
        if (src.getFontSize() > 0) dst.setFontSize(src.getFontSize());
        if (src.getFontFamily() != null) dst.setFontFamily(src.getFontFamily());
        dst.setTextPosition(src.getTextPosition());
    }

    private static String getRunFill(XWPFRun run) {
        CTR ctr = run.getCTR();
        if (ctr.isSetRPr() && ctr.getRPr().isSetShd()) {
            CTShd shd = ctr.getRPr().getShd();
            String fill = shd.getFill();
            if (fill != null && !fill.isEmpty()) return fill;
        }
        return null;
    }

    private static final class Fragment {
        final String text;
        final boolean highlight;
        Fragment(String t, boolean h) { this.text = t; this.highlight = h; }
    }
}
