package com.edusmart.service.impl;

import com.edusmart.model.ExamSubmission;
import com.edusmart.model.PlagiarismResult;
import com.edusmart.service.PlagiarismDetectionService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * PlagiarismDetectionServiceImpl — pure-Java TF-IDF cosine similarity engine.
 * No external API or library required.
 *
 * Algorithm:
 *  1. Tokenise and normalise each student's answer.
 *  2. Build a TF-IDF vocabulary matrix.
 *  3. Compute pairwise cosine similarity.
 *  4. Flag any pair with similarity >= 70%.
 */
public class PlagiarismDetectionServiceImpl implements PlagiarismDetectionService {

    private static final double FLAG_THRESHOLD = 70.0;
    private final ExecutorService executor;

    public PlagiarismDetectionServiceImpl() {
        this.executor = Executors.newFixedThreadPool(2,
                r -> { Thread t = new Thread(r, "plagiarism-check"); t.setDaemon(true); return t; });
    }

    @Override
    public CompletableFuture<List<PlagiarismResult>> detectPlagiarismAsync(List<ExamSubmission> submissions) {
        return CompletableFuture.supplyAsync(() -> detectAll(submissions), executor);
    }

    @Override
    public double calculateSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) return 0.0;
        double[] vec1 = buildTfIdfVector(tokenise(text1), buildVocab(List.of(tokenise(text1), tokenise(text2))));
        double[] vec2 = buildTfIdfVector(tokenise(text2), buildVocab(List.of(tokenise(text1), tokenise(text2))));
        return cosine(vec1, vec2) * 100.0;
    }

    // ── Core detection logic ─────────────────────────────────────────────

    private List<PlagiarismResult> detectAll(List<ExamSubmission> submissions) {
        if (submissions == null || submissions.size() < 2) {
            return buildDefaultResults(submissions);
        }

        // Tokenise all submissions
        List<List<String>> tokensList = new ArrayList<>();
        for (ExamSubmission s : submissions) {
            tokensList.add(tokenise(s.getStudentAnswer()));
        }

        // Build shared vocabulary
        List<String> vocab = buildVocab(tokensList);

        // TF-IDF vectors for each submission
        double[][] matrix = new double[submissions.size()][];
        for (int i = 0; i < submissions.size(); i++) {
            matrix[i] = buildTfIdfVector(tokensList.get(i), vocab);
        }

        // Pairwise comparison
        Map<Integer, PlagiarismResult> resultMap = new LinkedHashMap<>();
        for (int i = 0; i < submissions.size(); i++) {
            ExamSubmission si = submissions.get(i);
            PlagiarismResult ri = new PlagiarismResult();
            ri.setSubmissionId(si.getId());
            ri.setStudentId(si.getStudentId());
            ri.setStudentName(si.getStudentName() != null ? si.getStudentName() : "Student " + si.getStudentId());
            resultMap.put(i, ri);
        }

        for (int i = 0; i < submissions.size(); i++) {
            for (int j = i + 1; j < submissions.size(); j++) {
                double sim = cosine(matrix[i], matrix[j]) * 100.0;
                if (sim > 0) {
                    ExamSubmission si = submissions.get(i);
                    ExamSubmission sj = submissions.get(j);
                    String nameI = si.getStudentName() != null ? si.getStudentName() : "Student " + si.getStudentId();
                    String nameJ = sj.getStudentName() != null ? sj.getStudentName() : "Student " + sj.getStudentId();

                    resultMap.get(i).addMatch(new PlagiarismResult.PlagiarismMatch(sj.getStudentId(), nameJ, sim));
                    resultMap.get(j).addMatch(new PlagiarismResult.PlagiarismMatch(si.getStudentId(), nameI, sim));
                }
            }
        }

        // Compute max similarity for each student
        List<PlagiarismResult> results = new ArrayList<>();
        for (PlagiarismResult r : resultMap.values()) {
            double maxSim = r.getMatches().stream()
                    .mapToDouble(PlagiarismResult.PlagiarismMatch::getSimilarityPercent)
                    .max().orElse(0.0);
            r.setMaxSimilarityScore(maxSim);
            r.setFlagged(maxSim >= FLAG_THRESHOLD);
            results.add(r);
        }
        return results;
    }

    // ── TF-IDF helpers ───────────────────────────────────────────────────

    private List<String> tokenise(String text) {
        if (text == null || text.isBlank()) return Collections.emptyList();
        return Arrays.asList(text.toLowerCase()
                .replaceAll("[^a-z0-9àâçéèêëîïôùûüÿæœ ]", " ")
                .trim().split("\\s+"));
    }

    private List<String> buildVocab(List<List<String>> tokensList) {
        Set<String> vocab = new LinkedHashSet<>();
        for (List<String> tokens : tokensList) vocab.addAll(tokens);
        return new ArrayList<>(vocab);
    }

    private double[] buildTfIdfVector(List<String> tokens, List<String> vocab) {
        double[] vec = new double[vocab.size()];
        Map<String, Long> tf = new HashMap<>();
        for (String t : tokens) tf.merge(t, 1L, Long::sum);
        for (int i = 0; i < vocab.size(); i++) {
            vec[i] = tf.getOrDefault(vocab.get(i), 0L);
        }
        return vec;
    }

    private double cosine(double[] v1, double[] v2) {
        double dot = 0, n1 = 0, n2 = 0;
        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            n1  += v1[i] * v1[i];
            n2  += v2[i] * v2[i];
        }
        if (n1 == 0 || n2 == 0) return 0;
        return dot / (Math.sqrt(n1) * Math.sqrt(n2));
    }

    @Override
    public double comparePDF(File f1, File f2) {
        if (f1 == null || f2 == null || !f1.exists() || !f2.exists()) return 0.0;
        try {
            String t1 = extractText(f1);
            String t2 = extractText(f2);
            return calculateJaccardSimilarity(t1, t2);
        } catch (IOException e) {
            System.err.println("Error extracting PDF text: " + e.getMessage());
            return 0.0;
        }
    }

    private String extractText(File file) throws IOException {
        try (PDDocument doc = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    private double calculateJaccardSimilarity(String t1, String t2) {
        if (t1 == null || t2 == null) return 0.0;
        Set<String> s1 = new HashSet<>(Arrays.asList(t1.toLowerCase().split("\\W+")));
        Set<String> s2 = new HashSet<>(Arrays.asList(t2.toLowerCase().split("\\W+")));
        s1.remove(""); s2.remove("");
        
        if (s1.isEmpty() && s2.isEmpty()) return 100.0;
        
        Set<String> intersection = new HashSet<>(s1);
        intersection.retainAll(s2);
        
        Set<String> union = new HashSet<>(s1);
        union.addAll(s2);
        
        return (double) intersection.size() / union.size() * 100.0;
    }

    private List<PlagiarismResult> buildDefaultResults(List<ExamSubmission> submissions) {
        List<PlagiarismResult> results = new ArrayList<>();
        if (submissions == null) return results;
        for (ExamSubmission s : submissions) {
            PlagiarismResult r = new PlagiarismResult();
            r.setSubmissionId(s.getId());
            r.setStudentId(s.getStudentId());
            r.setStudentName(s.getStudentName());
            r.setMaxSimilarityScore(0);
            results.add(r);
        }
        return results;
    }
}
