package com.social7.studentlearnanalytics;

import android.os.Bundle;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.social7.studentlearnanalytics.model.LoginRequest;
import com.social7.studentlearnanalytics.model.LoginResponse;
import com.social7.studentlearnanalytics.network.ApiClient;
import com.social7.studentlearnanalytics.network.ApiService;
import com.social7.studentlearnanalytics.utils.ReopenPermissionHelper;

import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.junit.Assert.*;

/**
 * Real-device Android -> Live Laravel API -> Live MySQL End-to-End Integration Audit.
 * Runs real Retrofit calls against {@link ApiClient#getBaseUrl()} using an in-memory bearer token
 * so the user's on-device SessionManager state is never modified.
 *
 * Skips automatically when live audit instrumentation arguments are not supplied.
 */
@RunWith(AndroidJUnit4.class)
public class UniversalReopenLiveIntegrationTest {

    private static final String TAG = "SLAS_E2E_AUDIT";

    private static final class TokenHolder {
        volatile String token = "";
    }

    private ApiService createApiClient(TokenHolder holder) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request.Builder req = chain.request().newBuilder()
                            .header("Accept", "application/json");
                    String tk = holder.token;
                    if (tk != null && !tk.isEmpty()) {
                        req.header("Authorization", "Bearer " + tk);
                    }
                    return chain.proceed(req.build());
                })
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(ApiClient.getBaseUrl())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);
    }

    private static String futureIso(int daysOffset) {
        Date dt = new Date(System.currentTimeMillis() + (daysOffset * 24L * 3600L * 1000L));
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        return fmt.format(dt);
    }

    private static byte[] samplePdfBytes(String marker) {
        String content = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Note (" + marker + ") >>\nendobj\n"
                + "2 0 obj\n<< /Length 12 >>\nstream\nBT (" + marker + ") ET\nendstream\nendobj\n"
                + "trailer\n<< /Root 1 0 R >>\n%%EOF\n";
        return content.getBytes(StandardCharsets.UTF_8);
    }

    private Response<JsonObject> uploadPdf(ApiService api, int assignmentId, String fileName, String text) throws Exception {
        RequestBody idBody = RequestBody.create(String.valueOf(assignmentId), MediaType.parse("text/plain"));
        RequestBody pathBody = RequestBody.create(fileName, MediaType.parse("text/plain"));
        RequestBody textBody = RequestBody.create(text, MediaType.parse("text/plain"));
        RequestBody fileBody = RequestBody.create(samplePdfBytes(fileName + "_" + text), MediaType.parse("application/pdf"));
        MultipartBody.Part part = MultipartBody.Part.createFormData("submission_file", fileName, fileBody);
        return api.submitAssignmentMultipartWithText(idBody, pathBody, textBody, part).execute();
    }

    private String loginForToken(ApiService api, TokenHolder holder, String email, String password) throws Exception {
        holder.token = "";
        Response<LoginResponse> res = api.login(new LoginRequest(email, password)).execute();
        assertTrue("Expected login HTTP 200 for " + email + " got " + res.code(), res.isSuccessful() && res.body() != null);
        String tk = res.body().getToken();
        assertFalse("Expected non-empty Sanctum token for " + email, tk == null || tk.isEmpty());
        return tk;
    }

    private static JsonObject unwrapData(JsonObject root) {
        assertNotNull(root);
        return root.has("data") && root.get("data").isJsonObject() ? root.getAsJsonObject("data") : root;
    }

    @Test
    public void fullEndToEndScenariosAtoI_andRegressionOnLiveServer() throws Exception {
        Bundle args = InstrumentationRegistry.getArguments();
        String teacherEmail = args.getString("audit_teacher_email");
        Assume.assumeTrue("Skipping live E2E audit when audit_teacher_email argument is absent",
                teacherEmail != null && !teacherEmail.trim().isEmpty());

        String teacherPass = args.getString("audit_teacher_password", "");
        String stuAEmail = args.getString("audit_student_a_email", "");
        String stuAPass = args.getString("audit_student_a_password", "");
        String stuBEmail = args.getString("audit_student_b_email", "");
        String stuBPass = args.getString("audit_student_b_password", "");
        int assignmentId = Integer.parseInt(args.getString("audit_assignment_id", "0"));
        int homeworkId = Integer.parseInt(args.getString("audit_homework_id", "0"));
        int quizId = Integer.parseInt(args.getString("audit_quiz_id", "0"));
        int studentAId = Integer.parseInt(args.getString("audit_student_a_id", "0"));
        int studentBId = Integer.parseInt(args.getString("audit_student_b_id", "0"));

        assertTrue(assignmentId > 0 && homeworkId > 0 && quizId > 0 && studentAId > 0 && studentBId > 0);

        TokenHolder holder = new TokenHolder();
        ApiService api = createApiClient(holder);

        String teacherToken = loginForToken(api, holder, teacherEmail, teacherPass);
        String studentAToken = loginForToken(api, holder, stuAEmail, stuAPass);
        String studentBToken = loginForToken(api, holder, stuBEmail, stuBPass);
        Log.i(TAG, "AUTH_OK teacher+studentA+studentB tokens acquired from " + ApiClient.getBaseUrl());

        // Scenario A: Student A never submitted Assignment and deadline expired
        holder.token = studentAToken;
        Response<JsonObject> resAStatus = api.getStudentAssessmentReopenStatus("assignment", assignmentId).execute();
        assertEquals(200, resAStatus.code());
        ReopenPermissionHelper.ReopenStatusModel mA = ReopenPermissionHelper.parseStatusPayload(unwrapData(resAStatus.body()));
        assertEquals("Expired", mA.reopenStatus);
        assertEquals("Not Submitted", mA.studentSubmissionStatus);
        assertFalse(mA.canSubmit);
        Response<JsonObject> resABlocked = uploadPdf(api, assignmentId, "blocked_v1.pdf", "Should be rejected");
        assertEquals(422, resABlocked.code());
        Log.i(TAG, "SCENARIO_A_OK status=" + resAStatus.body());

        // Scenario B: Teacher reopens expired Assignment for Student A only (granting new deadline + late submission)
        holder.token = teacherToken;
        JsonObject reopenBody = new JsonObject();
        reopenBody.addProperty("assessment_type", "assignment");
        reopenBody.addProperty("assessment_id", assignmentId);
        JsonArray idsA = new JsonArray();
        idsA.add(studentAId);
        reopenBody.add("student_ids", idsA);
        reopenBody.addProperty("allow_submission", true);
        reopenBody.addProperty("allow_resubmission", false);
        reopenBody.addProperty("allow_late_submission", true);
        reopenBody.addProperty("additional_attempts", 0);
        reopenBody.addProperty("new_deadline", futureIso(2));
        reopenBody.addProperty("reason", "Live Android E2E Scenario B");
        reopenBody.addProperty("teacher_notes", "Medical excuse approved");
        Response<JsonObject> resB = api.reopenTeacherAssessment(reopenBody).execute();
        assertEquals(200, resB.code());
        Log.i(TAG, "SCENARIO_B_OK reopen=" + resB.body());

        // Check Student B unaffected (Scenario I partial)
        holder.token = studentBToken;
        Response<JsonObject> resBStuStatus = api.getStudentAssessmentReopenStatus("assignment", assignmentId).execute();
        assertEquals(200, resBStuStatus.code());
        ReopenPermissionHelper.ReopenStatusModel mB = ReopenPermissionHelper.parseStatusPayload(unwrapData(resBStuStatus.body()));
        assertEquals("Expired", mB.reopenStatus);
        assertFalse(mB.canSubmit);
        assertNull(mB.extendedDeadline);

        // Scenario C: Student A submits Assignment via Android Retrofit multipart
        holder.token = studentAToken;
        Response<JsonObject> resCStatus = api.getStudentAssessmentReopenStatus("assignment", assignmentId).execute();
        ReopenPermissionHelper.ReopenStatusModel mAfterReopen = ReopenPermissionHelper.parseStatusPayload(unwrapData(resCStatus.body()));
        assertEquals("Reopened", mAfterReopen.reopenStatus);
        assertTrue(mAfterReopen.canSubmit);

        Response<JsonObject> resCSubmit = uploadPdf(api, assignmentId, "android_v1.pdf", "Android Scenario C v1 text");
        assertEquals(201, resCSubmit.code());
        JsonObject subV1 = unwrapData(resCSubmit.body());
        int subV1Id = subV1.get("id").getAsInt();
        assertEquals(1, subV1.get("version_number").getAsInt());
        assertTrue(subV1.get("is_latest").getAsBoolean());
        assertTrue(subV1.get("is_late").getAsBoolean());
        Log.i(TAG, "SCENARIO_C_OK submit_v1=" + resCSubmit.body());

        // Scenario D: Teacher grades v1, allows resubmission + 1 attempt, Student A uploads v2
        holder.token = teacherToken;
        JsonObject gradeBody = new JsonObject();
        gradeBody.addProperty("score", 72);
        gradeBody.addProperty("feedback", "Initial draft graded before resubmission");
        gradeBody.addProperty("publish", true);
        Response<JsonObject> resGrade = api.gradeTeacherSubmission(subV1Id, gradeBody).execute();
        assertEquals(200, resGrade.code());

        holder.token = studentAToken;
        Response<JsonObject> resDBlocked = uploadPdf(api, assignmentId, "blocked_v2.pdf", "Blocked before reopen");
        assertEquals(409, resDBlocked.code());

        holder.token = teacherToken;
        JsonObject reopenResub = new JsonObject();
        reopenResub.addProperty("assessment_type", "assignment");
        reopenResub.addProperty("assessment_id", assignmentId);
        reopenResub.add("student_ids", idsA);
        reopenResub.addProperty("allow_submission", true);
        reopenResub.addProperty("allow_resubmission", true);
        reopenResub.addProperty("allow_late_submission", true);
        reopenResub.addProperty("additional_attempts", 1);
        reopenResub.addProperty("new_deadline", futureIso(3));
        reopenResub.addProperty("reason", "Live Android E2E Scenario D resubmission");
        Response<JsonObject> resDReopen = api.reopenTeacherAssessment(reopenResub).execute();
        assertEquals(200, resDReopen.code());

        holder.token = studentAToken;
        Response<JsonObject> resDStatus = api.getStudentAssessmentReopenStatus("assignment", assignmentId).execute();
        ReopenPermissionHelper.ReopenStatusModel mD = ReopenPermissionHelper.parseStatusPayload(unwrapData(resDStatus.body()));
        assertTrue(mD.canSubmit);
        assertTrue(mD.canResubmit);
        assertEquals(Integer.valueOf(1), mD.remainingAttempts);

        Response<JsonObject> resDSubmit = uploadPdf(api, assignmentId, "android_v2.pdf", "Android Scenario D v2 revised text");
        assertEquals(201, resDSubmit.code());
        JsonObject subV2 = unwrapData(resDSubmit.body());
        int subV2Id = subV2.get("id").getAsInt();
        assertNotEquals(subV1Id, subV2Id);
        assertEquals(2, subV2.get("version_number").getAsInt());
        assertTrue(subV2.get("is_latest").getAsBoolean());

        Response<JsonObject> resStuHist = api.getStudentAssessmentSubmissionHistory("assignment", assignmentId).execute();
        assertEquals(200, resStuHist.code());
        JsonArray histSubs = unwrapData(resStuHist.body()).getAsJsonArray("submissions");
        assertEquals(2, histSubs.size());
        assertEquals(2, histSubs.get(0).getAsJsonObject().get("version_number").getAsInt());
        assertEquals(1, histSubs.get(1).getAsJsonObject().get("version_number").getAsInt());
        assertEquals(72.0, histSubs.get(1).getAsJsonObject().get("score").getAsDouble(), 0.01);
        Log.i(TAG, "SCENARIO_D_OK history=" + resStuHist.body());

        // Scenario E: Expired Homework -> Reopened late submission -> Student A submits late
        holder.token = studentAToken;
        Response<JsonObject> resHwPre = api.getStudentAssessmentReopenStatus("homework", homeworkId).execute();
        assertEquals("Expired", ReopenPermissionHelper.parseStatusPayload(unwrapData(resHwPre.body())).reopenStatus);
        assertEquals(422, uploadPdf(api, homeworkId, "hw_blocked.pdf", "Blocked").code());

        holder.token = teacherToken;
        JsonObject reopenHw = new JsonObject();
        reopenHw.addProperty("assessment_type", "homework");
        reopenHw.addProperty("assessment_id", homeworkId);
        reopenHw.add("student_ids", idsA);
        reopenHw.addProperty("allow_submission", true);
        reopenHw.addProperty("allow_late_submission", true);
        reopenHw.addProperty("new_deadline", futureIso(2));
        reopenHw.addProperty("reason", "Live Android E2E Scenario E homework");
        assertEquals(200, api.reopenTeacherAssessment(reopenHw).execute().code());

        holder.token = studentAToken;
        Response<JsonObject> resHwSubmit = uploadPdf(api, homeworkId, "hw_late.pdf", "Android late homework submission");
        assertEquals(201, resHwSubmit.code());
        assertTrue(unwrapData(resHwSubmit.body()).get("is_late").getAsBoolean());
        assertEquals(1, unwrapData(resHwSubmit.body()).get("version_number").getAsInt());
        Log.i(TAG, "SCENARIO_E_OK homework_submit=" + resHwSubmit.body());

        // Scenario F & G: Quiz attempts exhausted -> grant additional attempt -> retake without losing attempt #1 answers/score
        holder.token = studentAToken;
        Response<JsonObject> qStart1 = api.startStudentQuiz(String.valueOf(quizId)).execute();
        assertEquals(200, qStart1.code());
        JsonObject qStart1Data = unwrapData(qStart1.body());
        int att1Id = qStart1Data.get("attempt_id").getAsInt();
        JsonArray qQuestions = qStart1Data.getAsJsonArray("questions");
        assertTrue(qQuestions.size() >= 2);
        int q1Id = qQuestions.get(0).getAsJsonObject().get("id").getAsInt();
        int q2Id = qQuestions.get(1).getAsJsonObject().get("id").getAsInt();

        JsonArray ans1 = new JsonArray();
        ans1.add(answerItem(q1Id, "A")); // right (5)
        ans1.add(answerItem(q2Id, "B")); // wrong (0)
        JsonObject submitQ1 = new JsonObject();
        submitQ1.add("answers", ans1);
        Response<JsonObject> qSub1 = api.submitStudentQuiz(String.valueOf(att1Id), submitQ1).execute();
        assertEquals(200, qSub1.code());
        assertEquals(5.0, unwrapData(qSub1.body()).get("score").getAsDouble(), 0.01);

        // Exhausted attempt #2 blocked with 409
        Response<JsonObject> qStartBlocked = api.startStudentQuiz(String.valueOf(quizId)).execute();
        assertEquals(409, qStartBlocked.code());

        // Scenario F: Teacher grants +1 additional attempt for Student A
        holder.token = teacherToken;
        JsonObject grantQuiz = new JsonObject();
        grantQuiz.addProperty("assessment_type", "quiz");
        grantQuiz.addProperty("assessment_id", quizId);
        grantQuiz.addProperty("student_id", studentAId);
        grantQuiz.addProperty("additional_attempts", 1);
        grantQuiz.addProperty("reason", "Live Android E2E Scenario F grant attempt");
        Response<JsonObject> resF = api.grantTeacherAssessmentAttempts(grantQuiz).execute();
        assertEquals(200, resF.code());
        Log.i(TAG, "SCENARIO_F_OK quiz_grant=" + resF.body());

        // Scenario G: Student A completes Attempt #2 and keeps Attempt #1 score + answers
        holder.token = studentAToken;
        Response<JsonObject> qStart2 = api.startStudentQuiz(String.valueOf(quizId)).execute();
        assertEquals(200, qStart2.code());
        int att2Id = unwrapData(qStart2.body()).get("attempt_id").getAsInt();
        assertNotEquals(att1Id, att2Id);

        JsonArray ans2 = new JsonArray();
        ans2.add(answerItem(q1Id, "A")); // right (5)
        ans2.add(answerItem(q2Id, "A")); // right (5)
        JsonObject submitQ2 = new JsonObject();
        submitQ2.add("answers", ans2);
        Response<JsonObject> qSub2 = api.submitStudentQuiz(String.valueOf(att2Id), submitQ2).execute();
        assertEquals(200, qSub2.code());
        assertEquals(10.0, unwrapData(qSub2.body()).get("score").getAsDouble(), 0.01);

        Response<JsonObject> qHistRes = api.getStudentQuizAttemptHistory(quizId).execute();
        assertEquals(200, qHistRes.code());
        JsonObject qHistData = unwrapData(qHistRes.body());
        JsonArray qAttempts = qHistData.getAsJsonArray("attempts");
        assertEquals(2, qAttempts.size());
        assertEquals(2, qAttempts.get(0).getAsJsonObject().get("attempt_number").getAsInt());
        assertEquals(10.0, qAttempts.get(0).getAsJsonObject().get("score").getAsDouble(), 0.01);
        assertEquals(1, qAttempts.get(1).getAsJsonObject().get("attempt_number").getAsInt());
        assertEquals(5.0, qAttempts.get(1).getAsJsonObject().get("score").getAsDouble(), 0.01);
        Log.i(TAG, "SCENARIO_G_OK quiz_history=" + qHistRes.body());

        // Scenario H: Teacher revokes reopen permissions on Assignment + Quiz -> Student A loses future access while history stays
        holder.token = teacherToken;
        JsonObject grantAsnExtra = new JsonObject();
        grantAsnExtra.addProperty("assessment_type", "assignment");
        grantAsnExtra.addProperty("assessment_id", assignmentId);
        grantAsnExtra.addProperty("student_id", studentAId);
        grantAsnExtra.addProperty("additional_attempts", 1);
        grantAsnExtra.addProperty("reason", "Pre-revoke attempt check");
        assertEquals(200, api.grantTeacherAssessmentAttempts(grantAsnExtra).execute().code());

        JsonObject revokeAsn = new JsonObject();
        revokeAsn.addProperty("assessment_type", "assignment");
        revokeAsn.addProperty("assessment_id", assignmentId);
        revokeAsn.add("student_ids", idsA);
        revokeAsn.addProperty("reason", "Live Android E2E Scenario H revoke assignment");
        assertEquals(200, api.revokeTeacherAssessmentReopen(revokeAsn).execute().code());

        JsonObject revokeQuiz = new JsonObject();
        revokeQuiz.addProperty("assessment_type", "quiz");
        revokeQuiz.addProperty("assessment_id", quizId);
        revokeQuiz.add("student_ids", idsA);
        revokeQuiz.addProperty("reason", "Live Android E2E Scenario H revoke quiz");
        assertEquals(200, api.revokeTeacherAssessmentReopen(revokeQuiz).execute().code());

        holder.token = studentAToken;
        assertEquals(409, uploadPdf(api, assignmentId, "after_revoke.pdf", "Should be revoked").code());
        assertEquals(409, api.startStudentQuiz(String.valueOf(quizId)).execute().code());
        assertEquals(2, unwrapData(api.getStudentAssessmentSubmissionHistory("assignment", assignmentId).execute().body()).getAsJsonArray("submissions").size());
        assertEquals(2, unwrapData(api.getStudentQuizAttemptHistory(quizId).execute().body()).getAsJsonArray("attempts").size());
        Log.i(TAG, "SCENARIO_H_OK revocations blocked new actions while preserving history");

        // Scenario I: Verify Student B remained isolated across Assignment, Homework, and Quiz
        holder.token = studentBToken;
        assertEquals(422, uploadPdf(api, homeworkId, "bob_hw.pdf", "Isolated").code());
        Response<JsonObject> bobHist = api.getStudentAssessmentSubmissionHistory("assignment", assignmentId).execute();
        assertEquals(200, bobHist.code());
        assertEquals(0, unwrapData(bobHist.body()).getAsJsonArray("submissions").size());
        // Quiz has max_attempts=1 originally; Student B still has original limit 1 and never received Student A's +1 attempt
        ReopenPermissionHelper.ReopenStatusModel bobQuizState = ReopenPermissionHelper.parseStatusPayload(
                unwrapData(api.getStudentAssessmentReopenStatus("quiz", quizId).execute().body()));
        assertEquals(Integer.valueOf(1), bobQuizState.originalAttemptLimit);
        assertEquals(0, bobQuizState.additionalAttempts);
        Log.i(TAG, "SCENARIO_I_OK student B remained isolated");

        // Android Client Regression APIs on Live Server
        holder.token = studentAToken;
        assertEquals(200, api.getStudentDashboard(null, null).execute().code());
        Response<JsonObject> stuAsnList = api.getStudentAssignments(null).execute();
        assertEquals(200, stuAsnList.code());
        assertEquals(200, api.getStudentQuizzes(null).execute().code());
        assertEquals(200, api.getStudentSubmissions().execute().code());

        holder.token = teacherToken;
        assertEquals(200, api.getTeacherAssessmentsOverview(null).execute().code());
        Map<String, String> logFilters = new HashMap<>();
        logFilters.put("assessment_type", "assignment");
        logFilters.put("assessment_id", String.valueOf(assignmentId));
        Response<JsonObject> auditRes = api.getTeacherAssessmentAuditLogs(logFilters).execute();
        assertEquals(200, auditRes.code());
        JsonArray logs = auditRes.body().getAsJsonArray("data");
        assertTrue("Expected audit logs >= 3", logs.size() >= 3);
        Log.i(TAG, "REGRESSION_OK audit_logs_count=" + logs.size());
    }

    private static JsonObject answerItem(int qId, String answer) {
        JsonObject o = new JsonObject();
        o.addProperty("question_id", qId);
        o.addProperty("answer", answer);
        return o;
    }
}
