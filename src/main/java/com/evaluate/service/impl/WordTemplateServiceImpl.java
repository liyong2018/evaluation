package com.evaluate.service.impl;

import com.evaluate.service.IWordTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Word模板处理服务实现类
 *
 * @author System
 * @since 2025-12-18
 */
@Slf4j
@Service
public class WordTemplateServiceImpl implements IWordTemplateService {

    // 模板文件路径（放在resources/templates目录下）
    // private static final String TEMPLATE_FILE_NAME = "templates/四川省眉山市青神县减灾能力评估技术报告-系统模板.docx";
    private static final String TEMPLATE_FILE_NAME = "templates/xxxx年四川省xx市xx县减灾能力评估技术报告-系统模板_v3.docx";

    // 变量匹配模式（支持多种格式）
    private static final Pattern[] VARIABLE_PATTERNS = {
        Pattern.compile("\\{\\{(.*?)\\}\\}", Pattern.DOTALL),           // {{variable}}
        Pattern.compile("\\$\\{(.*?)\\}", Pattern.DOTALL),             // ${variable}
        Pattern.compile("\\{(.*?)\\}", Pattern.DOTALL),                // {variable}
        Pattern.compile("DOCPROPERTY \"(.*?)\"", Pattern.DOTALL),       // Word字段格式
        Pattern.compile("MERGEFIELD \"(.*?)\"", Pattern.DOTALL)        // Word合并字段
    };

    @Override
    public byte[] generateReportFromTemplate(Map<String, Object> variables) {
        return generateReportFromTemplate(variables, null);
    }

    @Override
    public byte[] generateReportFromTemplate(Map<String, Object> variables, String thematicMapImagePath) {
        try {
            log.info("开始生成Word报告，模板文件: {}, 变量数量: {}, 专题图: {}",
                TEMPLATE_FILE_NAME, variables.size(), thematicMapImagePath);

            // 1. 读取模板文件
            XWPFDocument document = loadTemplateDocument();

            // 2. 如果提供了专题图路径，替换模板中的专题图图片
            if (thematicMapImagePath != null && !thematicMapImagePath.isEmpty()) {
                replaceThematicMapImage(document, thematicMapImagePath);
            }

            // 3. 替换文本中的变量
            replaceTextVariables(document, variables);

            // 3.1 处理动态表格 (List类型的变量)
            processDynamicTables(document, variables);

            // 4. 替换表格中的变量
            replaceTableVariables(document, variables);

            // 5. 替换页眉页脚中的变量
            replaceHeaderFooterVariables(document, variables);

            // 6. 转换为字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.write(outputStream);
            document.close();

            byte[] result = outputStream.toByteArray();
            log.info("Word报告生成成功，文件大小: {} bytes", result.length);

            return result;

        } catch (Exception e) {
            log.error("生成Word报告失败", e);
            throw new RuntimeException("生成Word报告失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean templateExists() {
        try {
            // 1. 检查外部文件系统
            File externalFile = Paths.get("src/main/resources/" + TEMPLATE_FILE_NAME).toFile();
            if (externalFile.exists() && externalFile.canRead()) {
                return true;
            }

            // 2. 检查根目录
            File rootFile = Paths.get(TEMPLATE_FILE_NAME).toFile();
            if (rootFile.exists() && rootFile.canRead()) {
                return true;
            }

            // 3. 检查classpath
            org.springframework.core.io.Resource resource =
                new org.springframework.core.io.ClassPathResource(TEMPLATE_FILE_NAME);
            return resource.exists() && resource.isReadable();

        } catch (Exception e) {
            log.warn("检查模板文件是否存在时出错: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> getTemplateVariables() {
        try {
            log.info("开始提取模板变量，模板文件: {}", TEMPLATE_FILE_NAME);
            XWPFDocument document = loadTemplateDocument();
            Set<String> variables = new HashSet<>();

            int paragraphCount = 0;
            int tableCount = 0;
            int cellCount = 0;
            int variableTextCount = 0;

            log.info("开始分析段落内容...");

            // 收集文本中的变量
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                paragraphCount++;
                String text = paragraph.getText();

                // 记录所有非空文本
                if (!text.trim().isEmpty()) {
                    variableTextCount++;
                    log.info("段落 #{} [长度: {}]: {}", variableTextCount, text.length(), text);
                }

                // 检查所有变量格式
                Set<String> paragraphVariables = extractVariablesFromText(text);
                if (!paragraphVariables.isEmpty()) {
                    log.info("段落包含变量: {} -> {}", text, paragraphVariables);
                    variables.addAll(paragraphVariables);
                }

                // 检查绿色背景变量
                Set<String> highlightedVariables = extractHighlightedVariables(paragraph.getRuns());
                if (!highlightedVariables.isEmpty()) {
                    log.info("段落包含绿色背景变量: {}", highlightedVariables);
                    variables.addAll(highlightedVariables);
                }
            }

            // 收集表格中的变量
            for (XWPFTable table : document.getTables()) {
                tableCount++;
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        cellCount++;
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            String text = paragraph.getText();
                            
                            // 提取文本中的变量
                            Set<String> cellVariables = extractVariablesFromText(text);
                            if (!cellVariables.isEmpty()) {
                                log.debug("表格单元格包含变量: {}", text);
                                variables.addAll(cellVariables);
                            }

                            // 提取绿色背景变量
                            Set<String> highlightedVariables = extractHighlightedVariables(paragraph.getRuns());
                            if (!highlightedVariables.isEmpty()) {
                                variables.addAll(highlightedVariables);
                            }
                        }
                    }
                }
            }

            document.close();

            log.info("模板变量提取完成 - 段落数: {}, 表格数: {}, 单元格数: {}, 找到变量数: {}",
                paragraphCount, tableCount, cellCount, variables.size());

            List<String> result = new ArrayList<>(variables);
            result.sort(String::compareTo);
            return result;

        } catch (Exception e) {
            log.error("获取模板变量失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 加载模板文档
     */
    private XWPFDocument loadTemplateDocument() throws IOException {
        // 1. 首先尝试从外部文件系统读取（开发环境）
        File externalFile = Paths.get("src/main/resources/" + TEMPLATE_FILE_NAME).toFile();
        if (externalFile.exists()) {
            log.info("从项目目录读取模板文件: {}", externalFile.getAbsolutePath());
            return new XWPFDocument(new FileInputStream(externalFile));
        }

        // 2. 尝试从根目录读取（用户提供的模板文件）
        File rootFile = Paths.get(TEMPLATE_FILE_NAME).toFile();
        if (rootFile.exists()) {
            log.info("从根目录读取模板文件: {}", rootFile.getAbsolutePath());
            return new XWPFDocument(new FileInputStream(rootFile));
        }

        // 3. 尝试从classpath读取（jar包内）
        org.springframework.core.io.Resource resource =
            new org.springframework.core.io.ClassPathResource(TEMPLATE_FILE_NAME);
        if (resource.exists()) {
            log.info("从classpath读取模板文件: {}", TEMPLATE_FILE_NAME);
            return new XWPFDocument(resource.getInputStream());
        }

        throw new FileNotFoundException("未找到模板文件: " + TEMPLATE_FILE_NAME +
            " 请确保模板文件存在于以下任一位置：\n" +
            "1. src/main/resources/templates/\n" +
            "2. 项目根目录\n" +
            "3. classpath:templates/");
    }

    /**
     * 处理动态表格（List变量展开）
     */
    private void processDynamicTables(XWPFDocument document, Map<String, Object> variables) {
        log.info("开始处理动态表格，变量总数: {}", variables.size());
        // 查找所有List类型的变量
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            if (entry.getValue() instanceof List) {
                try {
                    List<?> list = (List<?>) entry.getValue();
                    if (list.isEmpty()) {
                        log.info("变量 {} 是空列表，跳过", entry.getKey());
                        continue;
                    }
                    if (!(list.get(0) instanceof Map)) {
                        log.info("变量 {} 不是Map列表，跳过", entry.getKey());
                        continue;
                    }
                    
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> listData = (List<Map<String, Object>>) list;

                    String listName = entry.getKey();
                    List<String> requiredMarkers = null;
                    List<Map<String, Object>> effectiveListData = listData;
                    if ("table6_data".equals(listName)) {
                        requiredMarkers = Arrays.asList("表6", "乡镇（街道）");
                        effectiveListData = listData;
                    } else if ("table7_data".equals(listName)) {
                        requiredMarkers = Arrays.asList("表7", "乡镇单元", "社区（行政村）");
                        effectiveListData = listData;
                    }
                    
                    // 获取第一行数据的Key作为标识
                    Set<String> keys = effectiveListData.get(0).keySet();
                    if (keys.isEmpty()) continue;
                    
                    log.info("处理列表变量: {}, Keys: {}", entry.getKey(), keys);
                    
                    // 查找包含这些Key的表格并展开
                    if (requiredMarkers != null) {
                        findAndReplaceTable(document, keys, effectiveListData, requiredMarkers);
                    } else {
                        findAndReplaceTable(document, keys, effectiveListData);
                    }
                    
                } catch (Exception e) {
                    log.error("处理动态表格变量 {} 时出错", entry.getKey(), e);
                }
            }
        }
    }

    /**
     * 查找并替换表格内容
     */
    private void findAndReplaceTable(XWPFDocument doc, Set<String> keys, List<Map<String, Object>> data) {
        for (XWPFTable table : doc.getTables()) {
            int templateRowIndex = -1;
            
            // 寻找模板行：包含List数据中某个Key的行
            // 扫描整个表格寻找模板行 (Previously limited to 5 rows, causing issues with large headers)
            for (int i = 0; i < table.getRows().size(); i++) {
                XWPFTableRow row = table.getRow(i);
                if (rowContainsKeys(row, keys)) {
                    templateRowIndex = i;
                    break;
                }
            }
            
            if (templateRowIndex != -1) {
                log.info("找到动态表格模板行，索引: {}, 匹配Key: {}", templateRowIndex, keys);
                XWPFTableRow templateRow = table.getRow(templateRowIndex);
                
                // 插入数据
                int insertIndex = templateRowIndex + 1;
                log.info("开始插入数据行，总数: {}", data.size());
                int successCount = 0;
                for (Map<String, Object> rowData : data) {
                    try {
                        // 使用底层XML复制行，保留样式
                        CTRow ctRow = (CTRow) templateRow.getCtRow().copy();
                        replaceVariablesInCtRow(ctRow, rowData);
                        XWPFTableRow newRow = new XWPFTableRow(ctRow, table);
                        
                        // 在指定位置插入新行
                        table.addRow(newRow, insertIndex);
                        
                        XWPFTableRow liveRow = table.getRow(insertIndex);
                        
                        if (liveRow != null) {
                            // 替换新行中的变量
                            for (XWPFTableCell cell : liveRow.getTableCells()) {
                                for (XWPFParagraph p : cell.getParagraphs()) {
                                    replaceVariablesInParagraph(p, rowData);
                                }
                            }
                        } else {
                            log.error("无法获取插入的行，索引: {}", insertIndex);
                        }
                        
                        insertIndex++;
                        successCount++;
                    } catch (Exception e) {
                        log.error("插入表格行失败", e);
                    }
                }
                log.info("插入数据行完成，成功: {}/{}", successCount, data.size());
                
                // 删除模板行
                table.removeRow(templateRowIndex);
                
                // 假设一个List对应一个表格，处理完就跳出当前List的处理
                // (如果多个表格使用同一个List，可以移除break)
                break;
            }
        }
    }

    private void findAndReplaceTable(XWPFDocument doc, Set<String> keys, List<Map<String, Object>> data, List<String> requiredMarkers) {
        for (XWPFTable table : doc.getTables()) {
            if (!tableMatchesMarkers(doc, table, requiredMarkers)) {
                continue;
            }
            int templateRowIndex = -1;
            for (int i = 0; i < table.getRows().size(); i++) {
                XWPFTableRow row = table.getRow(i);
                if (rowContainsKeys(row, keys)) {
                    templateRowIndex = i;
                    break;
                }
            }

            if (templateRowIndex != -1) {
                log.info("找到动态表格模板行，索引: {}, 匹配Key: {}", templateRowIndex, keys);
                XWPFTableRow templateRow = table.getRow(templateRowIndex);

                int insertIndex = templateRowIndex + 1;
                log.info("开始插入数据行，总数: {}", data.size());
                int successCount = 0;
                for (Map<String, Object> rowData : data) {
                    try {
                        CTRow ctRow = (CTRow) templateRow.getCtRow().copy();
                        replaceVariablesInCtRow(ctRow, rowData);
                        XWPFTableRow newRow = new XWPFTableRow(ctRow, table);

                        table.addRow(newRow, insertIndex);

                        XWPFTableRow liveRow = table.getRow(insertIndex);

                        if (liveRow != null) {
                            for (XWPFTableCell cell : liveRow.getTableCells()) {
                                for (XWPFParagraph p : cell.getParagraphs()) {
                                    replaceVariablesInParagraph(p, rowData);
                                }
                            }
                        } else {
                            log.error("无法获取插入的行，索引: {}", insertIndex);
                        }

                        insertIndex++;
                        successCount++;
                    } catch (Exception e) {
                        log.error("插入表格行失败", e);
                    }
                }
                log.info("插入数据行完成，成功: {}/{}", successCount, data.size());

                table.removeRow(templateRowIndex);
                break;
            }
        }
    }

    private boolean tableMatchesMarkers(XWPFDocument doc, XWPFTable table, List<String> markers) {
        if (markers == null || markers.isEmpty()) {
            return true;
        }
        for (String marker : markers) {
            if (marker == null || marker.isEmpty()) continue;
            if (tableContainsMarker(table, marker)) return true;
            if (docContainsMarkerNearTable(doc, table, marker, 3)) return true;
        }
        return false;
    }

    private boolean tableContainsMarker(XWPFTable table, String marker) {
        String markerNoSpaces = marker.replace(" ", "");
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                String text = cell.getText();
                if (text == null || text.isEmpty()) continue;
                if (text.contains(marker)) {
                    return true;
                }
                if (text.replace(" ", "").contains(markerNoSpaces)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean docContainsMarkerNearTable(XWPFDocument doc, XWPFTable table, String marker, int maxParagraphsBack) {
        if (doc == null) return false;
        List<IBodyElement> elements = doc.getBodyElements();
        int tableIndex = -1;
        for (int i = 0; i < elements.size(); i++) {
            IBodyElement el = elements.get(i);
            if (el instanceof XWPFTable && el == table) {
                tableIndex = i;
                break;
            }
        }
        if (tableIndex <= 0) return false;

        String markerNoSpaces = marker.replace(" ", "");
        int checked = 0;
        for (int i = tableIndex - 1; i >= 0 && checked < maxParagraphsBack; i--) {
            IBodyElement el = elements.get(i);
            if (el instanceof XWPFParagraph) {
                checked++;
                String text = ((XWPFParagraph) el).getText();
                if (text == null || text.isEmpty()) continue;
                if (text.contains(marker)) return true;
                if (text.replace(" ", "").contains(markerNoSpaces)) return true;
            } else if (el instanceof XWPFTable) {
                break;
            }
        }
        return false;
    }

    private List<Map<String, Object>> withPrefixedAliases(List<Map<String, Object>> listData, String primaryPrefix, String aliasPrefix) {
        if (listData == null || listData.isEmpty()) {
            return listData;
        }
        String primary = primaryPrefix + "_";
        List<Map<String, Object>> out = new ArrayList<>(listData.size());
        for (Map<String, Object> row : listData) {
            Map<String, Object> copy = new LinkedHashMap<>(row);
            for (Map.Entry<String, Object> e : row.entrySet()) {
                String k = e.getKey();
                if (k != null && k.startsWith(primary)) {
                    String aliasKey = aliasPrefix + k.substring(primaryPrefix.length());
                    copy.putIfAbsent(aliasKey, e.getValue());
                }
            }
            out.add(copy);
        }
        return out;
    }

    private void replaceVariablesInCtRow(CTRow ctRow, Map<String, Object> variables) {
        if (ctRow == null || variables == null || variables.isEmpty()) {
            return;
        }

        for (CTTc tc : ctRow.getTcList()) {
            for (CTP p : tc.getPList()) {
                for (CTR r : p.getRList()) {
                    for (CTText t : r.getTList()) {
                        String text = t.getStringValue();
                        if (text == null || text.isEmpty()) {
                            continue;
                        }
                        String replaced = replaceVariablesInText(text, variables);
                        if (!Objects.equals(text, replaced)) {
                            t.setStringValue(replaced);
                        }
                    }
                }
            }
        }
    }

    /**
     * 检查行是否包含指定的Key
     */
    private boolean rowContainsKeys(XWPFTableRow row, Set<String> keys) {
        for (XWPFTableCell cell : row.getTableCells()) {
            String text = cell.getText();
            if (text == null || text.isEmpty()) continue;
            
            // 1. 直接包含检查 (快速)
            for (String key : keys) {
                // Simplified check: just look for the key string.
                if (text.contains(key)) {
                    log.info("Row matched key: {}", key);
                    return true;
                }
                // Aggressive check: remove spaces (handle {{ t6_idx }})
                if (text.replace(" ", "").contains(key)) {
                    log.info("Row matched key (aggressive): {}", key);
                    return true;
                }
            }
            
            // 2. 正则检查 (更鲁棒，处理 {{ key }} 等情况)
            // 提取 {{...}} 或 ${...} 中的内容进行匹配
            Pattern[] patterns = {
                Pattern.compile("\\{\\{(.*?)\\}\\}"),
                Pattern.compile("\\$\\{(.*?)\\}")
            };
            
            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    String foundKey = matcher.group(1).trim();
                    if (keys.contains(foundKey)) {
                         log.info("Row matched key via regex: {}", foundKey);
                         return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 替换段落中的文本变量
     */
    private int replaceTextVariables(XWPFDocument document, Map<String, Object> variables) {
        int count = 0;
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            count += replaceVariablesInParagraph(paragraph, variables);
        }
        return count;
    }

    /**
     * 替换表格中的变量
     */
    private int replaceTableVariables(XWPFDocument document, Map<String, Object> variables) {
        int count = 0;
        for (XWPFTable table : document.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        count += replaceVariablesInParagraph(paragraph, variables);
                    }
                }
            }
        }
        return count;
    }

    /**
     * 替换页眉页脚中的变量
     */
    private void replaceHeaderFooterVariables(XWPFDocument document, Map<String, Object> variables) {
        // 处理页眉
        for (XWPFHeader header : document.getHeaderList()) {
            for (XWPFParagraph paragraph : header.getParagraphs()) {
                replaceVariablesInParagraph(paragraph, variables);
            }
        }

        // 处理页脚
        for (XWPFFooter footer : document.getFooterList()) {
            for (XWPFParagraph paragraph : footer.getParagraphs()) {
                replaceVariablesInParagraph(paragraph, variables);
            }
        }
    }

    private int replaceVariablesAcrossRuns(XWPFParagraph paragraph, Map<String, Object> variables) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs == null || runs.isEmpty()) return 0;

        int replacements = 0;
        for (int i = 0; i < runs.size(); i++) {
            XWPFRun startRun = runs.get(i);
            String startText = startRun.getText(0);
            if (startText == null || startText.isEmpty()) continue;

            int braceStart = startText.indexOf("{{");
            if (braceStart < 0) braceStart = startText.indexOf("${");
            if (braceStart < 0) continue;

            boolean hasEndInSameRun = startText.indexOf("}}", braceStart) >= 0 || startText.indexOf("}", braceStart) >= 0;
            if (hasEndInSameRun) continue;

            StringBuilder combined = new StringBuilder();
            int endIndex = -1;
            for (int j = i; j < runs.size(); j++) {
                String t = runs.get(j).getText(0);
                if (t != null) combined.append(t);
                if (t != null && (t.contains("}}") || t.contains("}"))) {
                    endIndex = j;
                    break;
                }
            }

            if (endIndex < 0) continue;

            String combinedText = combined.toString();
            if (combinedText.isEmpty()) continue;

            String replacedText = replaceVariablesInText(combinedText, variables);
            if (!Objects.equals(combinedText, replacedText)) {
                startRun.setText(replacedText, 0);
                for (int k = i + 1; k <= endIndex; k++) {
                    XWPFRun r = runs.get(k);
                    String rt = r.getText(0);
                    if (rt != null && !rt.isEmpty()) {
                        r.setText("", 0);
                    }
                }
                replacements++;
            }
        }
        return replacements;
    }

    /**
     * 在段落中替换变量（保留样式）
     */
    private int replaceVariablesInParagraph(XWPFParagraph paragraph, Map<String, Object> variables) {
        int replacements = 0;
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs == null || runs.isEmpty()) return 0;

        String originalParagraphText = paragraph.getText();
        if (originalParagraphText != null && !originalParagraphText.isEmpty()) {
            boolean mediumTownContext =
                    originalParagraphText.contains("分布于{{community_medium_count}}个乡镇") ||
                    originalParagraphText.contains("分布于 {{community_medium_count}} 个乡镇");
            if (mediumTownContext) {
                String adjusted = originalParagraphText;
                if (mediumTownContext) {
                    adjusted = adjusted.replace("{{community_medium_count}}", "{{community_by_town_medium_count}}");
                }
                // Removed weakDownContext replacement as it incorrectly overwrites specific variables
                
                String replaced = replaceVariablesInText(adjusted, variables);
                if (!Objects.equals(originalParagraphText, replaced)) {
                    replaceParagraphWithText(paragraph, replaced);
                    return 1;
                }
            }
        }

        replacements += replaceVariablesAcrossRuns(paragraph, variables);
        runs = paragraph.getRuns();

        // 1. 尝试逐个Run替换 (保留原有逻辑)
        for (XWPFRun run : runs) {
            try {
                String text = run.getText(0);
                if (text != null && !text.trim().isEmpty()) {
                    String newText = replaceVariablesInText(text, variables);
                    
                    // 特殊逻辑：处理绿色背景变量等
                    if (text.equals(newText)) {
                        String key = text.trim();
                        String highlightColor = run.getTextHighlightColor() != null ? 
                            run.getTextHighlightColor().toString() : "";
                        boolean isHighlight = "green".equalsIgnoreCase(highlightColor) || 
                                            "yellow".equalsIgnoreCase(highlightColor);
                        
                        if ((isHighlight || variables.containsKey(key)) && variables.containsKey(key)) {
                            Object value = variables.get(key);
                            if (value != null) {
                                newText = value.toString();
                            }
                        }
                    }
                    
                    // Fallback for t6_idx (Integer) direct replacement if exact match
                    if (text.trim().startsWith("t6_") && variables.containsKey(text.trim())) {
                         Object val = variables.get(text.trim());
                         if (val != null) newText = val.toString();
                    }

                    // FIX: Special handling for template bug where {{year}} is used instead of township list
                    if (paragraph.getText().contains("乡镇（街道）减灾能力") && paragraph.getText().contains("乡镇为")) {
                            if (variables.containsKey("township_mode_list")) {
                                String modeList = variables.get("township_mode_list").toString();
                                String yearVal = variables.containsKey("year") ? variables.get("year").toString() : "2024";
                                
                                if (newText.contains("2025")) {
                                    log.info("Applying fix for township list template bug: replaced 2025 with {}", modeList);
                                    newText = newText.replace("2025", modeList);
                                }
                                if (newText.contains(yearVal)) {
                                     log.info("Applying fix for township list template bug: replaced {} with {}", yearVal, modeList);
                                     newText = newText.replace(yearVal, modeList);
                                }
                            }
                    }

                    // Explicitly replace "中等" if it exists in the text and variables map
                    // This handles cases where they are part of a run but not wrapped in {{}}
                    if (newText.contains("中等") && variables.containsKey("中等")) {
                         String val = variables.get("中等").toString();
                         newText = newText.replace("中等", val);
                    }

                    if (!text.equals(newText)) {
                        run.setText(newText, 0);
                        replacements++;
                    }
                }
            } catch (Exception e) {
                log.warn("替换Run变量时出错: {}", e.getMessage());
            }
        }

        // 2. 如果还有未替换的变量（通常是因为变量跨越了多个Run），尝试全段落替换
        // 只有当段落包含 {{ 或 ${ 或 { 时，或者包含特定的中文关键词（如"中等"）才进行此操作
        String paragraphText = paragraph.getText();
        
        // Debug logging for Table 6 variables
        if (paragraphText.contains("t6_idx")) {
            log.info("Processing paragraph with t6_idx. Text: '{}'", paragraphText);
            log.info("Runs count: {}", runs.size());
            for (int i=0; i<runs.size(); i++) {
                log.info("Run[{}]: '{}'", i, runs.get(i).getText(0));
            }
            log.info("Available keys in variables: {}", variables.keySet());
        }
        
        boolean hasBraces = paragraphText.contains("{") || paragraphText.contains("DOCPROPERTY");
        boolean hasKeywords = paragraphText.contains("中等");
        
        if (hasBraces || hasKeywords) {
            String newParagraphText = replaceVariablesInText(paragraphText, variables);
            
            // Explicitly handle "中等" for whole paragraph text as well
            // This ensures that even if they are not in {{}}, they get replaced if a mapping exists
            if (paragraphText.contains("中等") && variables.containsKey("中等")) {
                newParagraphText = newParagraphText.replace("中等", variables.get("中等").toString());
            }

            // FIX: Special handling for template bug where {{year}} is used instead of township list (Paragraph Level)
            // This is needed because sometimes "2025" or "2024" is split across runs or merged strangely
            if (paragraphText.contains("乡镇（街道）减灾能力") && paragraphText.contains("乡镇为")) {
                 if (variables.containsKey("township_mode_list")) {
                     String modeList = variables.get("township_mode_list").toString();
                     // Replace "2025" if present in this specific context
                     if (newParagraphText.contains("2025")) {
                         log.info("Applying fix for township list template bug (Paragraph): replaced 2025 with {}", modeList);
                         newParagraphText = newParagraphText.replace("2025", modeList);
                     }
                     // Replace current year if present
                     String yearVal = variables.containsKey("year") ? variables.get("year").toString() : "2024";
                     if (newParagraphText.contains(yearVal)) {
                         log.info("Applying fix for township list template bug (Paragraph): replaced {} with {}", yearVal, modeList);
                         newParagraphText = newParagraphText.replace(yearVal, modeList);
                     }
                 }
            }

            // Fallback: If standard regex didn't catch {{key}} (e.g. weird spacing), try direct key replacement
            // This is critical for table rows where regex might fail but simple string matching works
            if (hasBraces) {
                // Try improved fallback using regex on the whole paragraph text again
                // This double-check ensures that if replaceVariablesInText missed something (unlikely but possible), we catch it
                String tempText = newParagraphText;
                for (Pattern pattern : VARIABLE_PATTERNS) {
                     Matcher matcher = pattern.matcher(tempText);
                     StringBuffer buffer = new StringBuffer();
                     boolean changed = false;
                     while (matcher.find()) {
                         String variableName = matcher.group(1);
                         if (variableName != null) variableName = variableName.trim();
                         
                         if (variables.containsKey(variableName)) {
                             Object value = variables.get(variableName);
                             String replacement = value != null ? value.toString() : "";
                             replacement = replacement.replace("\\", "\\\\").replace("$", "\\$");
                             matcher.appendReplacement(buffer, replacement);
                             changed = true;
                         } else {
                             matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group()));
                         }
                     }
                     if (changed) {
                         matcher.appendTail(buffer);
                         tempText = buffer.toString();
                     }
                }
                newParagraphText = tempText;
            }

            if (!paragraphText.equals(newParagraphText)) {
                log.info("检测到跨Run变量或关键词，执行全段落替换: {} -> {}", paragraphText, newParagraphText);
                
                // 策略：清空所有Run，只保留第一个Run的样式（或创建一个新Run），并设置新文本
                // 注意：这会丢失段落内不同Run的个别样式差异（如部分加粗），但在表格填充场景中通常是可以接受的
                
                // 保存第一个Run的样式属性 (简单示例，可能不完整)
                XWPFRun firstRun = runs.get(0);
                String fontFamily = firstRun.getFontFamily();
                int fontSize = firstRun.getFontSize();
                boolean isBold = firstRun.isBold();
                boolean isItalic = firstRun.isItalic();
                String color = firstRun.getColor();
                
                // 移除所有Run (从后往前删)
                for (int i = runs.size() - 1; i >= 0; i--) {
                    paragraph.removeRun(i);
                }
                
                // 创建新Run
                XWPFRun newRun = paragraph.createRun();
                newRun.setText(newParagraphText);
                
                // 恢复样式
                if (fontFamily != null) newRun.setFontFamily(fontFamily);
                if (fontSize > 0) newRun.setFontSize(fontSize);
                newRun.setBold(isBold);
                newRun.setItalic(isItalic);
                if (color != null) newRun.setColor(color);
                
                replacements++;
            }
        }

        // 3. Fallback: Aggressive Exact Key Match (ignoring non-alphanumeric chars)
        // Useful for table cells where Word splits runs or adds spaces
        // Only attempt if we haven't made replacements yet and the paragraph is short (likely a cell value)
        if (replacements == 0 && paragraphText.length() < 100) {
             // Replace all non-alphanumeric characters (except underscore) with empty string
             String cleanText = paragraphText.replaceAll("[^a-zA-Z0-9_]", "");
             
             // Debug hex dump for t6_idx analysis
             if (paragraphText.contains("t6_idx")) {
                 StringBuilder hex = new StringBuilder();
                 for (char c : paragraphText.toCharArray()) {
                     hex.append(String.format("%04x ", (int) c));
                 }
                 log.info("Hex dump for t6_idx paragraph: {}", hex.toString());
                 log.info("Cleaned text: '{}'", cleanText);
             }

             if (!cleanText.isEmpty() && variables.containsKey(cleanText)) {
                  log.info("Aggressive Match: '{}' cleaned to '{}' matched key", paragraphText, cleanText);
                  replaceParagraphWithText(paragraph, variables.get(cleanText).toString());
                  replacements++;
             }
        }
        
        // 4. Super Aggressive Fallback for Table Variables (t6_, t7_, etc.)
        // If we still haven't replaced anything, and the text contains a known table key
        if (replacements == 0) {
            for (String key : variables.keySet()) {
                if ((key.startsWith("t6_") || key.startsWith("t7_") || key.startsWith("t8_") || key.startsWith("t9_")) 
                        && paragraphText.contains(key)) {
                    log.info("Super Aggressive Match: Paragraph '{}' contains key '{}'", paragraphText, key);
                    Object val = variables.get(key);
                    if (val != null) {
                        replaceParagraphWithText(paragraph, val.toString());
                        replacements++;
                        break; 
                    }
                }
            }
        }
        
        return replacements;
    }
    
    /**
     * Helper to replace entire paragraph content with a single text value, preserving style of first run.
     */
    private void replaceParagraphWithText(XWPFParagraph paragraph, String newText) {
        List<XWPFRun> currentRuns = paragraph.getRuns();
        if (currentRuns != null && !currentRuns.isEmpty()) {
            // Preserve style from first run
            XWPFRun firstRun = currentRuns.get(0);
            String fontFamily = firstRun.getFontFamily();
            int fontSize = firstRun.getFontSize();
            boolean isBold = firstRun.isBold();
            boolean isItalic = firstRun.isItalic();
            String color = firstRun.getColor();
            
            for (int i = currentRuns.size() - 1; i >= 0; i--) {
                paragraph.removeRun(i);
            }
            
            XWPFRun newRun = paragraph.createRun();
            newRun.setText(newText);
            
            if (fontFamily != null) newRun.setFontFamily(fontFamily);
            if (fontSize > 0) newRun.setFontSize(fontSize);
            newRun.setBold(isBold);
            newRun.setItalic(isItalic);
            if (color != null) newRun.setColor(color);
        } else {
            // No runs? Create one.
            XWPFRun newRun = paragraph.createRun();
            newRun.setText(newText);
        }
    }

    /**
     * 在文本中替换变量（支持多种格式）
     */
    private String replaceVariablesInText(String text, Map<String, Object> variables) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        String result = text;

        // 依次处理每种变量格式
        // Special check: if text is exactly a variable name like "t6_idx", try to replace it directly
        // This handles cases where {{}} are stripped or in separate runs, but we got here via paragraph replacement
        if (text.startsWith("t6_") && variables.containsKey(text.trim())) {
             Object val = variables.get(text.trim());
             if (val != null) return val.toString();
        }

        for (Pattern pattern : VARIABLE_PATTERNS) {
            Matcher matcher = pattern.matcher(result);
            StringBuffer buffer = new StringBuffer();

            while (matcher.find()) {
                String variableName = matcher.group(1); // 获取变量名
                if (variableName != null) {
                    variableName = variableName.trim(); // FIX: 去除变量名周围的空格
                }

                String normalizedName = null;
                if (variableName != null && !variableName.isEmpty()) {
                    normalizedName = variableName
                            .replace('\uFF5B', '{')
                            .replace('\uFF5D', '}')
                            .trim()
                            .replaceAll("^[\\{\\}\\s]+", "")
                            .replaceAll("[\\{\\}\\s]+$", "");
                    if (normalizedName.isEmpty()) normalizedName = null;
                }

                Object value = variables.get(variableName);
                if (value == null && normalizedName != null && (variableName == null || !normalizedName.equals(variableName))) {
                    value = variables.get(normalizedName);
                }
                if (value == null && normalizedName != null) {
                    value = variables.get("{{" + normalizedName + "}}");
                }
                if (value == null && normalizedName != null) {
                    value = variables.get("{" + normalizedName + "}");
                }
                if (value == null && normalizedName != null) {
                    value = variables.get("${" + normalizedName + "}");
                }

                if (value == null) {
                    // Only log if it looks like a variable we should have known about (e.g. starts with t6_)
                    if (variableName != null && (variableName.startsWith("t6_") || variableName.startsWith("t7_") || variableName.startsWith("t8_") || variableName.startsWith("t9_"))) {
                         log.info("Variable NOT FOUND in map: '{}'", variableName);
                    }
                } else {
                    // Log success for table variables to confirm
                    if (variableName != null && (variableName.startsWith("t6_") || variableName.startsWith("t7_") || variableName.startsWith("t8_") || variableName.startsWith("t9_"))) {
                         log.info("Variable FOUND: '{}' -> '{}'", variableName, value);
                    }
                }

                String replacement = value != null ? value.toString() : matcher.group(); // 如果找不到变量，保持原样

                // 处理特殊字符，防止appendReplacement出错
                replacement = replacement.replace("\\", "\\\\").replace("$", "\\$");
                try {
                    matcher.appendReplacement(buffer, replacement);
                } catch (IllegalArgumentException e) {
                    log.error("Failed to replace variable '{}' with value '{}'", variableName, replacement, e);
                    matcher.appendReplacement(buffer, ""); // Fallback
                }
            }
            matcher.appendTail(buffer);
            result = buffer.toString();
        }

        return result;
    }

    /**
     * 从文本中提取变量（支持多种格式）
     */
    private Set<String> extractVariablesFromText(String text) {
        Set<String> variables = new HashSet<>();
        if (text == null || text.trim().isEmpty()) {
            return variables;
        }

        for (Pattern pattern : VARIABLE_PATTERNS) {
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String variableName = matcher.group(1);
                if (variableName != null && !variableName.trim().isEmpty()) {
                    variables.add(variableName.trim());
                }
            }
        }

        return variables;
    }

    @Override
    public Map<String, Object> debugTemplateContent() {
        Map<String, Object> debugInfo = new HashMap<>();
        try {
            log.info("开始调试模板文件内容");
            XWPFDocument document = loadTemplateDocument();

            // 基本信息
            debugInfo.put("success", true);
            debugInfo.put("templateFile", TEMPLATE_FILE_NAME);
            debugInfo.put("fileSize", document.getProperties().getCoreProperties().getCreated());

            List<Map<String, Object>> allParagraphs = new ArrayList<>();
            List<Map<String, Object>> allTables = new ArrayList<>();
            List<String> suspiciousTexts = new ArrayList<>();
            Set<String> allVariables = new HashSet<>();

            int paragraphCount = 0;
            int tableCount = 0;
            int cellCount = 0;

            // 详细分析每个段落
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                paragraphCount++;
                String text = paragraph.getText();

                Map<String, Object> paragraphInfo = new HashMap<>();
                paragraphInfo.put("index", paragraphCount);
                paragraphInfo.put("text", text);
                paragraphInfo.put("length", text.length());

                // 检查是否包含可能的变量标记
                boolean isSuspicious = text.contains("{") || text.contains("}") || text.contains("$") ||
                                    text.contains("青神") || text.contains("年份") || text.contains("县") ||
                                    text.contains("市") || text.contains("省") || text.length() < 200;
                paragraphInfo.put("isSuspicious", isSuspicious);

                if (isSuspicious && !text.trim().isEmpty()) {
                    suspiciousTexts.add("段落#" + paragraphCount + ": " + text);
                }

                // 提取变量
                Set<String> paragraphVariables = new HashSet<>(extractVariablesFromText(text));
                paragraphVariables.addAll(extractHighlightedVariables(paragraph.getRuns()));
                
                if (!paragraphVariables.isEmpty()) {
                    paragraphInfo.put("variables", paragraphVariables);
                    allVariables.addAll(paragraphVariables);
                }

                allParagraphs.add(paragraphInfo);
            }

            // 详细分析每个表格
            for (XWPFTable table : document.getTables()) {
                tableCount++;
                Map<String, Object> tableInfo = new HashMap<>();
                tableInfo.put("tableIndex", tableCount);
                tableInfo.put("rowCount", table.getRows().size());

                List<Map<String, Object>> tableRows = new ArrayList<>();

                for (XWPFTableRow row : table.getRows()) {
                    Map<String, Object> rowInfo = new HashMap<>();
                    List<String> cellTexts = new ArrayList<>();

                    for (XWPFTableCell cell : row.getTableCells()) {
                        cellCount++;
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            String text = paragraph.getText();
                            cellTexts.add(text);

                            // 检查是否包含变量
                            if (text.contains("{") || text.contains("}") || text.contains("$")) {
                                suspiciousTexts.add("表格#" + tableCount + " 单元格: " + text);
                            }

                            Set<String> cellVariables = extractVariablesFromText(text);
                            allVariables.addAll(cellVariables);
                            allVariables.addAll(extractHighlightedVariables(paragraph.getRuns()));
                        }
                    }

                    rowInfo.put("cells", cellTexts);
                    tableRows.add(rowInfo);
                }

                tableInfo.put("rows", tableRows);
                allTables.add(tableInfo);
            }

            document.close();

            // 汇总调试信息
            debugInfo.put("paragraphCount", paragraphCount);
            debugInfo.put("tableCount", tableCount);
            debugInfo.put("cellCount", cellCount);
            debugInfo.put("variablesFound", new ArrayList<>(allVariables));
            debugInfo.put("variableCount", allVariables.size());
            debugInfo.put("suspiciousTexts", suspiciousTexts);
            debugInfo.put("paragraphs", allParagraphs);
            debugInfo.put("tables", allTables);

            log.info("调试完成 - 段落: {}, 表格: {}, 变量: {}", paragraphCount, tableCount, allVariables.size());

        } catch (Exception e) {
            log.error("调试模板内容失败", e);
            debugInfo.put("success", false);
            debugInfo.put("error", e.getMessage());
            debugInfo.put("stackTrace", getStackTraceAsString(e));
        }

        return debugInfo;
    }

    @Override
    public Map<String, Object> getRealTemplateContent() {
        Map<String, Object> contentData = new HashMap<>();
        try {
            log.info("开始获取Word模板实际内容用于预览");
            XWPFDocument document = loadTemplateDocument();

            // 文档基本信息
            contentData.put("success", true);
            contentData.put("templateFile", TEMPLATE_FILE_NAME);

            // 提取实际内容
            List<Map<String, Object>> documentContent = new ArrayList<>();
            int elementIndex = 0;

            // 获取文档中所有元素（段落、表格、图片）
            List<IBodyElement> bodyElements = document.getBodyElements();
            log.info("文档总元素数: {}", bodyElements.size());

            // 统计所有图片
            int totalImagesFound = 0;

            for (int i = 0; i < bodyElements.size(); i++) {
                IBodyElement element = bodyElements.get(i);

                if (element instanceof XWPFParagraph) {
                    XWPFParagraph paragraph = (XWPFParagraph) element;
                    String text = paragraph.getText().trim();

                    // 更全面的图片检测
                    boolean hasImageInRuns = false;
                    int imagesInRuns = 0;

                    // 检查段落运行中的图片
                    if (paragraph.getRuns() != null) {
                        for (XWPFRun run : paragraph.getRuns()) {
                            if (run.getEmbeddedPictures() != null && !run.getEmbeddedPictures().isEmpty()) {
                                hasImageInRuns = true;
                                imagesInRuns += run.getEmbeddedPictures().size();
                                log.info("在段落运行中发现图片 - 段落文本: '{}', 运行文本: '{}', 图片数: {}",
                                    text, run.getText(0), run.getEmbeddedPictures().size());
                            }
                        }
                    }

                    // 检查段落中的所有关系（包括图片）
                    boolean hasImageRelation = false;
                    try {
                        // 检查段落XML中是否有图片相关的元素
                        String paragraphXml = paragraph.getCTP().xmlText();
                        if (paragraphXml.contains("<a:blip") || paragraphXml.contains("<pic:pic") || paragraphXml.contains("<w:drawing>")) {
                            hasImageRelation = true;
                            log.info("在段落XML中发现图片元素 - 段落文本: '{}'", text);
                        }
                    } catch (Exception e) {
                        log.debug("检查段落XML图片时出错: {}", e.getMessage());
                    }

                    boolean hasImage = hasImageInRuns || hasImageRelation;
                    boolean hasText = !text.isEmpty();

                    if (hasImage) {
                        totalImagesFound += imagesInRuns;
                    }

                    // 详细日志图片检测
                    if (hasImage) {
                        log.info("发现图片段落 - 文本: '{}', 运行数: {}, 运行中图片数: {}, 总图片数: {}",
                            text, paragraph.getRuns().size(), imagesInRuns, totalImagesFound);
                    }

                    if (hasText || hasImage) {
                        Map<String, Object> paraElement = new HashMap<>();
                        paraElement.put("index", ++elementIndex);
                        paraElement.put("type", "paragraph");
                        paraElement.put("text", text);

                        // 获取段落格式信息
                        Map<String, Object> style = new HashMap<>();
                        if (paragraph.getStyle() != null) {
                            style.put("styleId", paragraph.getStyle());
                        }
                        if (paragraph.getAlignment() != null) {
                            style.put("alignment", paragraph.getAlignment().toString());
                        }
                        
                        // 提取缩进和间距信息
                        if (paragraph.getIndentationFirstLine() != -1) {
                            style.put("indentFirstLine", paragraph.getIndentationFirstLine());
                        }
                        if (paragraph.getIndentationLeft() != -1) {
                            style.put("indentLeft", paragraph.getIndentationLeft());
                        }
                        if (paragraph.getIndentationRight() != -1) {
                            style.put("indentRight", paragraph.getIndentationRight());
                        }
                        if (paragraph.getSpacingBefore() != -1) {
                            style.put("spacingBefore", paragraph.getSpacingBefore());
                        }
                        if (paragraph.getSpacingAfter() != -1) {
                            style.put("spacingAfter", paragraph.getSpacingAfter());
                        }
                        if (paragraph.getSpacingBetween() != -1) {
                            style.put("lineSpacing", paragraph.getSpacingBetween());
                        }
                        if (paragraph.getSpacingLineRule() != null) {
                            style.put("lineRule", paragraph.getSpacingLineRule().toString());
                        }
                        
                        // 尝试提取列表编号信息
                        try {
                            String numId = paragraph.getNumID() != null ? paragraph.getNumID().toString() : null;
                            if (numId != null) {
                                style.put("numId", numId);
                                style.put("ilvl", paragraph.getNumIlvl().toString());
                                if (paragraph.getNumFmt() != null) {
                                    style.put("numFmt", paragraph.getNumFmt());
                                }
                            }
                        } catch (Exception e) {
                            // 忽略列表提取错误
                        }
                        
                        // 提取详细的Run信息（样式、字体、颜色等）
                        List<Map<String, Object>> runs = new ArrayList<>();
                        if (paragraph.getRuns() != null) {
                            for (XWPFRun run : paragraph.getRuns()) {
                                Map<String, Object> runInfo = new HashMap<>();
                                String runText = run.getText(0);
                                if (runText != null) {
                                    runInfo.put("text", runText);
                                    runInfo.put("bold", run.isBold());
                                    runInfo.put("italic", run.isItalic());
                                    runInfo.put("color", run.getColor()); // Hex string without #
                                    runInfo.put("fontSize", run.getFontSize());
                                    runInfo.put("fontFamily", run.getFontFamily());
                                    if (run.getTextHighlightColor() != null) {
                                        runInfo.put("highlight", run.getTextHighlightColor().toString());
                                    }
                                    if (run.getUnderline() != null && run.getUnderline() != UnderlinePatterns.NONE) {
                                        runInfo.put("underline", true);
                                    }
                                    runs.add(runInfo);
                                }
                            }
                        }
                        paraElement.put("runs", runs);

                        // 检查是否有图片
                        if (hasImage) {
                            paraElement.put("hasImage", true);
                            // 提取图片信息
                            List<Map<String, Object>> images = new ArrayList<>();
                            for (XWPFRun run : paragraph.getRuns()) {
                                if (run.getEmbeddedPictures() != null) {
                                    for (XWPFPicture pic : run.getEmbeddedPictures()) {
                                        Map<String, Object> imageInfo = new HashMap<>();
                                        imageInfo.put("description", pic.getDescription() != null ? pic.getDescription() : "图片");
                                        imageInfo.put("title", "图片");
                                        // 添加更多图片信息
                                        try {
                                            if (pic.getPictureData() != null) {
                                                imageInfo.put("size", pic.getPictureData().getData().length);
                                                imageInfo.put("filename", pic.getPictureData().getFileName());
                                                
                                                // 添加Base64图片数据
                                                byte[] imgBytes = pic.getPictureData().getData();
                                                imageInfo.put("base64Data", Base64.getEncoder().encodeToString(imgBytes));
                                                
                                                // 尝试获取Content-Type
                                                String ext = pic.getPictureData().suggestFileExtension();
                                                String contentType = "image/" + (ext != null ? ext : "jpeg");
                                                imageInfo.put("contentType", contentType);
                                            }
                                        } catch (Exception e) {
                                            log.debug("获取图片详细信息时出错: {}", e.getMessage());
                                        }
                                        images.add(imageInfo);
                                    }
                                }
                            }
                            paraElement.put("images", images);
                        } else {
                            paraElement.put("hasImage", false);
                        }

                        paraElement.put("style", style);

                        // 检查是否包含变量
                        Set<String> variables = extractVariablesFromText(text);
                        if (!variables.isEmpty()) {
                            paraElement.put("variables", new ArrayList<>(variables));
                            paraElement.put("hasVariables", true);
                        } else {
                            paraElement.put("hasVariables", false);
                        }

                        documentContent.add(paraElement);
                    }

                } else if (element instanceof XWPFTable) {
                    XWPFTable table = (XWPFTable) element;

                    // 检查表格前面是否有标题段落（表题注）
                    String tableCaption = "";
                    if (i > 0 && bodyElements.get(i - 1) instanceof XWPFParagraph) {
                        XWPFParagraph prevParagraph = (XWPFParagraph) bodyElements.get(i - 1);
                        String prevText = prevParagraph.getText().trim();
                        if (prevText.matches(".*表[0-9]+.*") || prevText.matches(".*Table.*[0-9]+.*")) {
                            tableCaption = prevText;
                            // 移除之前已经添加的题注段落，避免重复
                            final String finalTableCaption = tableCaption;
                            documentContent.removeIf(e -> finalTableCaption.equals(e.get("text")));
                        }
                    }

                    Map<String, Object> tableElement = new HashMap<>();
                    tableElement.put("index", ++elementIndex);
                    tableElement.put("type", "table");
                    tableElement.put("caption", tableCaption);
                    tableElement.put("rowCount", table.getRows().size());

                    List<List<Map<String, Object>>> tableData = new ArrayList<>();

                    for (XWPFTableRow row : table.getRows()) {
                        List<Map<String, Object>> rowData = new ArrayList<>();

                        for (XWPFTableCell cell : row.getTableCells()) {
                            Map<String, Object> cellData = new HashMap<>();

                            // 获取单元格的所有段落文本
                            StringBuilder cellText = new StringBuilder();
                            Set<String> cellVariables = new HashSet<>();

                            for (XWPFParagraph cellParagraph : cell.getParagraphs()) {
                                String text = cellParagraph.getText();
                                if (!text.trim().isEmpty()) {
                                    if (cellText.length() > 0) {
                                        cellText.append("\n");
                                    }
                                    cellText.append(text);
                                    cellVariables.addAll(extractVariablesFromText(text));
                                }
                            }

                            cellData.put("text", cellText.toString());
                            if (!cellVariables.isEmpty()) {
                                cellData.put("variables", new ArrayList<>(cellVariables));
                                cellData.put("hasVariables", true);
                            } else {
                                cellData.put("hasVariables", false);
                            }

                            rowData.add(cellData);
                        }

                        tableData.add(rowData);
                    }

                    tableElement.put("data", tableData);
                    documentContent.add(tableElement);
                }
            }

            // 统计信息
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalElements", elementIndex);
            statistics.put("paragraphCount", (int) documentContent.stream().filter(e -> "paragraph".equals(e.get("type"))).count());
            statistics.put("tableCount", (int) documentContent.stream().filter(e -> "table".equals(e.get("type"))).count());

            long variableElements = documentContent.stream().filter(e -> Boolean.TRUE.equals(e.get("hasVariables"))).count();
            statistics.put("elementsWithVariables", variableElements);

            // 图片统计
            long imageElements = documentContent.stream().filter(e -> Boolean.TRUE.equals(e.get("hasImage"))).count();
            statistics.put("elementsWithImages", imageElements);
            statistics.put("totalImagesFound", totalImagesFound);

            contentData.put("content", documentContent);
            contentData.put("statistics", statistics);

            document.close();
            log.info("Word模板内容提取完成 - 总元素: {}, 段落: {}, 表格: {}, 包含变量: {}, 包含图片: {}, 总图片数: {}",
                elementIndex, statistics.get("paragraphCount"), statistics.get("tableCount"), variableElements, imageElements, totalImagesFound);

        } catch (Exception e) {
            log.error("获取Word模板内容失败", e);
            contentData.put("success", false);
            contentData.put("error", e.getMessage());
            contentData.put("stackTrace", getStackTraceAsString(e));
        }

        return contentData;
    }

    @Override
    public byte[] getTemplateFile() {
        try {
            log.info("开始获取Word模板文件");
            XWPFDocument document = loadTemplateDocument();

            // 将文档写入字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.write(outputStream);
            document.close();

            byte[] result = outputStream.toByteArray();
            outputStream.close();

            log.info("Word模板文件获取完成，文件大小: {} bytes", result.length);
            return result;

        } catch (Exception e) {
            log.error("获取Word模板文件失败", e);
            throw new RuntimeException("获取Word模板文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean saveEditedContent(String htmlContent) {
        try {
            log.info("开始保存编辑后的Word文档内容");

            if (htmlContent == null || htmlContent.trim().isEmpty()) {
                log.warn("HTML内容为空，跳过保存");
                return false;
            }

            // 创建一个新的Word文档
            XWPFDocument document = new XWPFDocument();

            // 将HTML内容转换为Word段落
            String[] paragraphs = htmlContent.split("(?=<\\/?(p|h[1-6]|li|table|\\/table|tr|\\/tr|td|\\/td|th|\\/th|ul|\\/ul|ol|\\/ol)>)");

            boolean inTable = false;
            XWPFTable currentTable = null;
            XWPFTableRow currentRow = null;

            for (String segment : paragraphs) {
                segment = segment.trim();
                if (segment.isEmpty()) continue;

                try {
                    if (segment.startsWith("<h1")) {
                        String text = extractTextFromHtmlTag(segment, "h1");
                        if (!text.isEmpty()) {
                            XWPFParagraph paragraph = document.createParagraph();
                            paragraph.setStyle("Heading1");
                            XWPFRun run = paragraph.createRun();
                            run.setText(text);
                            run.setBold(true);
                            run.setFontSize(20);
                            run.setFontFamily("Microsoft YaHei");
                        }
                    } else if (segment.startsWith("<h2")) {
                        String text = extractTextFromHtmlTag(segment, "h2");
                        if (!text.isEmpty()) {
                            XWPFParagraph paragraph = document.createParagraph();
                            paragraph.setStyle("Heading2");
                            XWPFRun run = paragraph.createRun();
                            run.setText(text);
                            run.setBold(true);
                            run.setFontSize(16);
                            run.setFontFamily("Microsoft YaHei");
                        }
                    } else if (segment.startsWith("<h3")) {
                        String text = extractTextFromHtmlTag(segment, "h3");
                        if (!text.isEmpty()) {
                            XWPFParagraph paragraph = document.createParagraph();
                            paragraph.setStyle("Heading3");
                            XWPFRun run = paragraph.createRun();
                            run.setText(text);
                            run.setBold(true);
                            run.setFontSize(14);
                            run.setFontFamily("Microsoft YaHei");
                        }
                    } else if (segment.startsWith("<p")) {
                        String text = extractTextFromHtmlTag(segment, "p");
                        if (!text.isEmpty()) {
                            XWPFParagraph paragraph = document.createParagraph();
                            paragraph.setSpacingAfter(200); // 12pt spacing
                            XWPFRun run = paragraph.createRun();
                            run.setText(text);
                            run.setFontSize(12);
                            run.setFontFamily("Microsoft YaHei");
                        }
                    } else if (segment.startsWith("<table")) {
                        // 开始表格
                        inTable = true;
                        currentTable = document.createTable();
                    } else if (segment.startsWith("</table>")) {
                        // 结束表格
                        inTable = false;
                        currentTable = null;
                        currentRow = null;
                    } else if (inTable && segment.startsWith("<tr")) {
                        // 开始表格行
                        if (currentTable != null) {
                            currentRow = currentTable.createRow();
                        }
                    } else if (inTable && segment.startsWith("<td") || segment.startsWith("<th")) {
                        // 表格单元格
                        if (currentRow != null) {
                            String text = extractTextFromHtmlTag(segment.startsWith("<th") ? "th" : "td", segment);
                            if (!currentRow.getTableCells().isEmpty()) {
                                XWPFTableCell cell = currentRow.getCell(currentRow.getTableCells().size() - 1);
                                cell.removeParagraph(0);
                                XWPFParagraph paragraph = cell.addParagraph();
                                XWPFRun run = paragraph.createRun();
                                run.setText(text);
                                run.setFontSize(10);
                                run.setFontFamily("Microsoft YaHei");
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("处理HTML段落时出错: {}, 段落内容: {}", e.getMessage(), segment);
                }
            }

            // 保存到文件
            // 使用时间戳避免文件被占用导致保存失败
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String outputPath = "src/main/resources/templates/四川省眉山市青神县减灾能力评估技术报告-系统模板-编辑版_" + timestamp + ".docx";
            File outputFile = new File(outputPath);

            // 确保目录存在
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                document.write(out);
            }

            document.close();

            log.info("编辑后的Word文档保存成功: {}", outputPath);
            return true;

        } catch (Exception e) {
            log.error("保存编辑后的Word文档内容失败", e);
            return false;
        }
    }

    /**
     * 从HTML标签中提取文本内容
     */
    private String extractTextFromHtmlTag(String segment, String tagName) {
        String pattern = "<" + tagName + "[^>]*>(.*?)</" + tagName + ">";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher m = p.matcher(segment);

        if (m.find()) {
            String content = m.group(1);
            // 移除HTML实体和标签
            content = content.replaceAll("<[^>]+>", "");
            content = content.replaceAll("&nbsp;", " ");
            content = content.replaceAll("&lt;", "<");
            content = content.replaceAll("&gt;", ">");
            content = content.replaceAll("&amp;", "&");
            content = content.replaceAll("&quot;", "\"");
            return content.trim();
        }

        return "";
    }

    @Override
    public byte[] convertHtmlToWord(String htmlContent) {
        try {
            XWPFDocument document = new XWPFDocument();

            // 正则表达式匹配标签或文本内容
            // <[^>]+> 匹配标签
            // [^<]+ 匹配文本
            Pattern tokenPattern = Pattern.compile("<[^>]+>|[^<]+");
            Matcher matcher = tokenPattern.matcher(htmlContent);

            XWPFParagraph currentParagraph = document.createParagraph();
            boolean inTable = false;
            XWPFTable currentTable = null;
            XWPFTableRow currentRow = null;
            XWPFTableCell currentCell = null;

            boolean isBold = false;
            boolean isItalic = false;
            boolean isUnderline = false;
            int fontSize = 12; // 默认字号

            while (matcher.find()) {
                String token = matcher.group();

                if (token.startsWith("<")) {
                    String lowerTag = token.toLowerCase();

                    // 块级元素处理
                    if (lowerTag.matches("<p[\\s>].*") || lowerTag.matches("<div[\\s>].*")) {
                        if (!inTable) {
                            currentParagraph = document.createParagraph();
                        } else if (currentCell != null) {
                            currentParagraph = currentCell.addParagraph();
                        }
                        // 重置段落样式
                        fontSize = 12;
                        isBold = false;
                    }
                    // 标题处理
                    else if (lowerTag.matches("<h[1-6][\\s>].*")) {
                        if (!inTable) {
                            currentParagraph = document.createParagraph();
                            currentParagraph.setAlignment(ParagraphAlignment.CENTER);
                        } else if (currentCell != null) {
                            currentParagraph = currentCell.addParagraph();
                            currentParagraph.setAlignment(ParagraphAlignment.CENTER);
                        }
                        
                        // 根据标题级别设置样式
                        isBold = true;
                        if (lowerTag.startsWith("<h1")) fontSize = 22; // 二号
                        else if (lowerTag.startsWith("<h2")) fontSize = 16; // 三号
                        else if (lowerTag.startsWith("<h3")) fontSize = 15; // 小三
                        else if (lowerTag.startsWith("<h4")) fontSize = 14; // 四号
                        else fontSize = 12;
                    }
                    else if (lowerTag.matches("</h[1-6]>")) {
                        isBold = false;
                        fontSize = 12;
                    }
                    // 表格处理
                    else if (lowerTag.startsWith("<table")) {
                        inTable = true;
                        currentTable = document.createTable();
                        // 移除默认创建的空行，以便后续逻辑统一
                        if (currentTable.getRows().size() > 0) {
                            currentTable.removeRow(0);
                        }
                        currentParagraph = null;
                    }
                    else if (lowerTag.startsWith("</table")) {
                        inTable = false;
                        currentTable = null;
                        currentRow = null;
                        currentCell = null;
                        currentParagraph = document.createParagraph();
                    }
                    else if (lowerTag.startsWith("<tr")) {
                        if (inTable && currentTable != null) {
                            currentRow = currentTable.createRow();
                        }
                    }
                    else if (lowerTag.startsWith("</tr")) {
                        currentRow = null;
                    }
                    else if (lowerTag.startsWith("<td") || lowerTag.startsWith("<th")) {
                        if (inTable && currentRow != null) {
                            currentCell = currentRow.createCell();
                            if (currentCell.getParagraphs().size() > 0) {
                                currentParagraph = currentCell.getParagraphs().get(0);
                            } else {
                                currentParagraph = currentCell.addParagraph();
                            }
                        }
                    }
                    else if (lowerTag.startsWith("</td") || lowerTag.startsWith("</th")) {
                        currentCell = null;
                    }
                    // 内联样式
                    else if (lowerTag.startsWith("<b") || lowerTag.startsWith("<strong")) isBold = true;
                    else if (lowerTag.startsWith("</b") || lowerTag.startsWith("</strong")) isBold = false;
                    else if (lowerTag.startsWith("<i") || lowerTag.startsWith("<em")) isItalic = true;
                    else if (lowerTag.startsWith("</i") || lowerTag.startsWith("</em")) isItalic = false;
                    else if (lowerTag.startsWith("<u")) isUnderline = true;
                    else if (lowerTag.startsWith("</u")) isUnderline = false;
                    // 换行
                    else if (lowerTag.startsWith("<br")) {
                        if (currentParagraph != null) {
                            currentParagraph.createRun().addBreak();
                        }
                    }

                } else {
                    // 文本内容处理
                    String text = token.replace("&nbsp;", " ")
                                       .replace("&lt;", "<")
                                       .replace("&gt;", ">")
                                       .replace("&amp;", "&")
                                       .replace("&quot;", "\"");
                    
                    // 规范化空白字符（HTML将换行视为相关空格，这里简单替换为普通空格）
                    text = text.replaceAll("\\s+", " ");
                    
                    if (!text.trim().isEmpty()) {
                        if (currentParagraph == null) {
                             if (!inTable) currentParagraph = document.createParagraph();
                        }
                        
                        if (currentParagraph != null) {
                            XWPFRun run = currentParagraph.createRun();
                            run.setText(text);
                            run.setFontFamily("Microsoft YaHei");
                            run.setFontSize(fontSize);
                            if (isBold) run.setBold(true);
                            if (isItalic) run.setItalic(true);
                            if (isUnderline) run.setUnderline(UnderlinePatterns.SINGLE);
                        }
                    }
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.write(outputStream);
            document.close();

            byte[] result = outputStream.toByteArray();
            log.info("HTML转Word成功，大小: {} bytes", result.length);
            return result;

        } catch (Exception e) {
            log.error("HTML转Word失败", e);
            throw new RuntimeException("HTML转Word失败: " + e.getMessage(), e);
        }
    }

    /**
     * 处理带样式的文本
     */
    private String processStyledText(String htmlText, XWPFRun run, XWPFDocument document) {
        // 简单的HTML标签清理，保留文本内容
        String cleanText = htmlText.replaceAll("<[^>]*>", "").trim();

        // 检测并应用基本样式
        if (htmlText.contains("<strong>") || htmlText.contains("<b>")) {
            run.setBold(true);
        }
        if (htmlText.contains("<em>") || htmlText.contains("<i>")) {
            run.setItalic(true);
        }
        if (htmlText.contains("<u>")) {
            run.setUnderline(UnderlinePatterns.SINGLE);
        }

        return cleanText;
    }

    /**
     * 从Runs中提取高亮（绿色背景）变量
     */
    private Set<String> extractHighlightedVariables(List<XWPFRun> runs) {
        Set<String> variables = new HashSet<>();
        if (runs != null) {
            for (XWPFRun run : runs) {
                try {
                    // 检查高亮颜色是否为绿色
                    if (run.getTextHighlightColor() != null && 
                        "green".equalsIgnoreCase(run.getTextHighlightColor().toString())) {
                        String text = run.getText(0);
                        if (text != null && !text.trim().isEmpty()) {
                            variables.add(text.trim());
                        }
                    }
                } catch (Exception e) {
                    log.warn("检查Run高亮时出错: {}", e.getMessage());
                }
            }
        }
        return variables;
    }

    /**
     * 将异常堆栈转换为字符串
     */
    private String getStackTraceAsString(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * 替换Word文档中的专题图图片
     * 查找模板中的占位图片，替换为实际生成的专题图
     */
    private void replaceThematicMapImage(XWPFDocument document, String imagePath) {
        try {
            log.info("开始替换专题图图片: {}", imagePath);

            // 读取新图片文件
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                log.warn("图片文件不存在: {}", imagePath);
                return;
            }

            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            String imageFileName = imageFile.getName();
            String fileExtension = imageFileName.substring(imageFileName.lastIndexOf('.') + 1).toLowerCase();

            // 确定图片类型
            int pictureType = determinePictureType(fileExtension);
            if (pictureType == -1) {
                log.error("不支持的图片格式: {}", fileExtension);
                return;
            }

            // 查找所有段落中的图片
            boolean replaced = false;
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                for (XWPFRun run : paragraph.getRuns()) {
                    if (run.getEmbeddedPictures() != null && !run.getEmbeddedPictures().isEmpty()) {
                        // 找到图片，检查是否为占位图片
                        for (XWPFPicture picture : run.getEmbeddedPictures()) {
                            String existingFileName = picture.getPictureData() != null ? picture.getPictureData().getFileName() : "";
                            log.info("找到模板中的图片: {}", existingFileName);

                            // 替换图片：移除旧图片，添加新图片
                            // 注意：POI不支持直接替换，需要重新创建run
                        }
                    }
                }
            }

            // 更简单的方案：在文档中查找包含专题图占位符的段落，然后在其后添加图片
            // 或者查找具有特定名称的图片进行替换
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                // 查找包含专题图占位符的段落（如果有）
                if (text.contains("专题图") || text.contains("thematic_map") || text.contains("{{thematic_map_image}}")) {
                    log.info("找到专题图占位段落: {}", text);
                    // 在占位符后添加图片
                    if (!paragraph.getRuns().isEmpty()) {
                        XWPFRun run = paragraph.getRuns().get(0);
                        // 清空占位符文本
                        run.setText("", 0);
                        // 添加图片
                        run.addPicture(new ByteArrayInputStream(imageBytes), pictureType, imageFileName, Units.toEMU(600), Units.toEMU(400));
                        log.info("成功替换专题图图片");
                        replaced = true;
                        break;
                    }
                }
            }

            // 如果没找到占位符，尝试替换第一张图片
            if (!replaced) {
                log.info("未找到专题图占位符，尝试替换文档中的第一张图片");
                boolean firstImageReplaced = replaceFirstImage(document, imageBytes, pictureType, imageFileName);
                if (firstImageReplaced) {
                    log.info("成功替换文档中的第一张图片为专题图");
                } else {
                    log.warn("文档中没有找到可替换的图片");
                }
            }

        } catch (Exception e) {
            log.error("替换专题图图片失败", e);
        }
    }

    /**
     * 替换文档中的第一张图片
     */
    private boolean replaceFirstImage(XWPFDocument document, byte[] newImageBytes, int pictureType, String fileName) {
        try {
            // 遍历所有段落查找图片
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                for (XWPFRun run : paragraph.getRuns()) {
                    if (run.getEmbeddedPictures() != null && !run.getEmbeddedPictures().isEmpty()) {
                        // 找到第一张图片，清空run并添加新图片
                        run.setText("", 0);
                        run.addPicture(new ByteArrayInputStream(newImageBytes), pictureType, fileName, Units.toEMU(600), Units.toEMU(400));
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            log.error("替换第一张图片失败", e);
            return false;
        }
    }

    /**
     * 根据文件扩展名确定图片类型
     */
    private int determinePictureType(String extension) {
        switch (extension) {
            case "png":
                return XWPFDocument.PICTURE_TYPE_PNG;
            case "jpg":
            case "jpeg":
                return XWPFDocument.PICTURE_TYPE_JPEG;
            case "gif":
                return XWPFDocument.PICTURE_TYPE_GIF;
            case "bmp":
                return XWPFDocument.PICTURE_TYPE_BMP;
            case "emf":
                return XWPFDocument.PICTURE_TYPE_EMF;
            case "wmf":
                return XWPFDocument.PICTURE_TYPE_WMF;
            default:
                return -1;
        }
    }
}
