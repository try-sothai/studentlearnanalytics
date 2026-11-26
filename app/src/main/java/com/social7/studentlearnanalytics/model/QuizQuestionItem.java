package com.social7.studentlearnanalytics.model;

import com.google.gson.JsonObject;

public class QuizQuestionItem {
    public int id;
    public int quizId;
    public int questionOrder;
    public String questionText;
    public String questionType; // multiple_choice | true_false | short_answer
    public double marks;
    public String optionA;
    public String optionB;
    public String optionC;
    public String optionD;
    public int optionCount;
    public String correctAnswer;
    public boolean isRequired = true;
    public String gradingGuidance;
    public boolean expanded = false;

    public static QuizQuestionItem fromJson(JsonObject obj) {
        QuizQuestionItem q = new QuizQuestionItem();
        q.id = optInt(obj, "id", 0);
        q.quizId = optInt(obj, "quiz_id", 0);
        q.questionOrder = optInt(obj, "question_order", optInt(obj, "number", 1));
        q.questionText = optStr(obj, "question_text", "");
        q.questionType = optStr(obj, "question_type", "multiple_choice");
        q.marks = optDouble(obj, "marks", 1.0);
        q.isRequired = optBool(obj, "is_required", true);
        q.optionA = optStr(obj, "option_a", null);
        q.optionB = optStr(obj, "option_b", null);
        q.optionC = optStr(obj, "option_c", null);
        q.optionD = optStr(obj, "option_d", null);
        q.optionCount = optInt(obj, "option_count", countNonEmpty(q.optionA, q.optionB, q.optionC, q.optionD));
        q.correctAnswer = optStr(obj, "correct_answer", null);
        q.gradingGuidance = optStr(obj, "grading_guidance", q.correctAnswer);
        return q;
    }

    private static boolean optBool(JsonObject o, String k, boolean def) {
        if (o == null || !o.has(k) || o.get(k).isJsonNull()) return def;
        try { return o.get(k).getAsBoolean(); } catch (Exception e) { return def; }
    }

    private static int countNonEmpty(String... vals) {
        int c = 0;
        for (String v : vals) {
            if (v != null && !v.trim().isEmpty()) c++;
        }
        return c;
    }

    private static int optInt(JsonObject o, String k, int def) {
        if (o == null || !o.has(k) || o.get(k).isJsonNull()) return def;
        try { return o.get(k).getAsInt(); } catch (Exception e) { return def; }
    }

    private static double optDouble(JsonObject o, String k, double def) {
        if (o == null || !o.has(k) || o.get(k).isJsonNull()) return def;
        try { return o.get(k).getAsDouble(); } catch (Exception e) { return def; }
    }

    private static String optStr(JsonObject o, String k, String def) {
        if (o == null || !o.has(k) || o.get(k).isJsonNull()) return def;
        try { return o.get(k).getAsString(); } catch (Exception e) { return def; }
    }

    public String getTypeLabel() {
        if ("true_false".equalsIgnoreCase(questionType)) return "True / False";
        if ("short_answer".equalsIgnoreCase(questionType)) return "Short Answer";
        return "Multiple Choice";
    }
}
