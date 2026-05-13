package com.edusmart.service;

import com.edusmart.model.PlagiarismResult;
import java.util.List;

public interface PlagiarismDetectionService {
    /**
     * Checks for plagiarism in a text against a list of other submissions or web sources.
     */
    PlagiarismResult checkPlagiarism(String text, List<String> compareTo);
}
