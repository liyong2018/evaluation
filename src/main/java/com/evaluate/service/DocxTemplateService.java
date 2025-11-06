package com.evaluate.service;

import com.evaluate.util.DocxTemplateProcessor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.List;
import java.util.Set;

/**
 * Simple service wrapper for generating a Word report from a template.
 */
public class DocxTemplateService {

    /**
     * Generate a new Word document from a template with replacements.
     *
     * @param templatePath  root-relative or absolute path to the .docx template
     * @param outputPath    output .docx path
     * @param replacements  map of ${key} to replacement value
     * @param highlightKeys keys whose values should be highlighted in yellow
     */
    public void generate(Path templatePath,
                         Path outputPath,
                         Map<String, String> replacements,
                         Set<String> highlightKeys) throws IOException {
        DocxTemplateProcessor.processDocx(templatePath, outputPath, replacements, highlightKeys);
    }

    /** Overload: highlight keys as List */
    public void generate(Path templatePath,
                         Path outputPath,
                         Map<String, String> replacements,
                         List<String> highlightKeys) throws IOException {
        var doc = DocxTemplateProcessor.loadTemplate(templatePath);
        try (doc) {
            DocxTemplateProcessor.process(doc, replacements, highlightKeys);
            DocxTemplateProcessor.write(doc, outputPath);
        }
    }
}
