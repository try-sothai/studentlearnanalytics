package com.social7.studentlearnanalytics;

import static org.junit.Assert.assertEquals;

import com.social7.studentlearnanalytics.utils.SubmissionFileViewerHelper;

import org.junit.Test;

public class SubmissionFileViewerHelperUnitTest {

    @Test
    public void sanitizeFileName_removesDirectoriesAndTraversal() {
        assertEquals("homework.png", SubmissionFileViewerHelper.sanitizeFileName("submissions/homework.png", 10));
        assertEquals("passwd", SubmissionFileViewerHelper.sanitizeFileName("../../etc/passwd", 10));
        assertEquals("submission_42", SubmissionFileViewerHelper.sanitizeFileName("...", 42));
        assertEquals("submission_42", SubmissionFileViewerHelper.sanitizeFileName("", 42));
    }

    @Test
    public void inferPreviewType_mapsImagesPdfDocumentsAndFallback() {
        assertEquals("image", SubmissionFileViewerHelper.inferPreviewType(null, "photo.PNG", "image/png"));
        assertEquals("image", SubmissionFileViewerHelper.inferPreviewType(null, "scan.jpeg", null));
        assertEquals("pdf", SubmissionFileViewerHelper.inferPreviewType(null, "report.pdf", "application/pdf"));
        assertEquals("document", SubmissionFileViewerHelper.inferPreviewType(null, "essay.docx", null));
        assertEquals("document", SubmissionFileViewerHelper.inferPreviewType(null, "notes.doc", "application/msword"));
        assertEquals("unsupported", SubmissionFileViewerHelper.inferPreviewType(null, "archive.zip", "application/zip"));
    }

    @Test
    public void resolveMimeType_normalizesByExtensionWhenGeneric() {
        assertEquals("application/pdf", SubmissionFileViewerHelper.resolveMimeType("solution.pdf", "application/octet-stream"));
        assertEquals("image/jpeg", SubmissionFileViewerHelper.resolveMimeType("photo.jpg", null));
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                SubmissionFileViewerHelper.resolveMimeType("paper.docx", ""));
    }
}
