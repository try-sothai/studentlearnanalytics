package com.social7.studentlearnanalytics.utils;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.social7.studentlearnanalytics.network.ApiClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Authenticated download, safe local caching, bitmap/PDF rendering, Downloads export,
 * and external viewer handoff for student assignment submission files.
 */
public final class SubmissionFileViewerHelper {

    public interface FileFetchCallback {
        void onSuccess(File cachedFile, String resolvedMimeType);
        void onError(String message, int httpStatus);
    }

    private static Handler mainHandler() {
        return new Handler(Looper.getMainLooper());
    }

    private SubmissionFileViewerHelper() {
    }

    public static String sanitizeFileName(@Nullable String rawName, int submissionId) {
        String name = rawName != null ? rawName.trim() : "";
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0 && slash + 1 < name.length()) {
            name = name.substring(slash + 1);
        }
        name = name.replaceAll("[^A-Za-z0-9._-]", "_").replaceAll("^\\.+", "");
        if (name.isEmpty()) {
            name = "submission_" + submissionId;
        }
        return name;
    }

    public static String inferPreviewType(@Nullable String previewType, @Nullable String fileName, @Nullable String mimeType) {
        if (previewType != null && !previewType.trim().isEmpty()) {
            return previewType.trim().toLowerCase(Locale.US);
        }
        String mime = mimeType != null ? mimeType.toLowerCase(Locale.US) : "";
        String lowerName = fileName != null ? fileName.toLowerCase(Locale.US) : "";

        if (mime.startsWith("image/") || lowerName.endsWith(".png") || lowerName.endsWith(".jpg")
                || lowerName.endsWith(".jpeg") || lowerName.endsWith(".webp") || lowerName.endsWith(".gif")) {
            return "image";
        }
        if ("application/pdf".equals(mime) || lowerName.endsWith(".pdf")) {
            return "pdf";
        }
        if (mime.contains("wordprocessingml") || "application/msword".equals(mime)
                || lowerName.endsWith(".doc") || lowerName.endsWith(".docx")) {
            return "document";
        }
        return "unsupported";
    }

    public static String resolveMimeType(@Nullable String fileName, @Nullable String declaredMime) {
        if (declaredMime != null && !declaredMime.trim().isEmpty()
                && !"application/octet-stream".equalsIgnoreCase(declaredMime.trim())) {
            return declaredMime.trim();
        }
        if (fileName != null) {
            int dot = fileName.lastIndexOf('.');
            if (dot >= 0 && dot + 1 < fileName.length()) {
                String ext = fileName.substring(dot + 1).toLowerCase(Locale.US);
                switch (ext) {
                    case "pdf": return "application/pdf";
                    case "png": return "image/png";
                    case "jpg":
                    case "jpeg": return "image/jpeg";
                    case "webp": return "image/webp";
                    case "gif": return "image/gif";
                    case "doc": return "application/msword";
                    case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                    case "zip": return "application/zip";
                    case "txt": return "text/plain";
                    default:
                        String mapped = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
                        if (mapped != null) return mapped;
                }
            }
        }
        return "application/octet-stream";
    }

    public static Call<ResponseBody> fetchSubmissionFile(
            @NonNull Context context,
            int submissionId,
            @Nullable String preferredFileName,
            @Nullable String declaredMime,
            @NonNull FileFetchCallback callback
    ) {
        return fetchSubmissionFileForRole(context, submissionId, false, preferredFileName, declaredMime, callback);
    }

    public static Call<ResponseBody> fetchSubmissionFileForRole(
            @NonNull Context context,
            int submissionId,
            boolean isStudentMode,
            @Nullable String preferredFileName,
            @Nullable String declaredMime,
            @NonNull FileFetchCallback callback
    ) {
        final Context appCtx = context.getApplicationContext();
        final String safeName = sanitizeFileName(preferredFileName, submissionId);

        Call<ResponseBody> call = isStudentMode
                ? ApiClient.getApiService(appCtx).downloadStudentSubmissionFile(submissionId, null)
                : ApiClient.getApiService(appCtx).downloadTeacherSubmissionFile(submissionId, null);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> c, @NonNull Response<ResponseBody> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    int code = response.code();
                    String msg = code == 404
                            ? "Submitted file was not found or cannot be accessed on the server."
                            : (code == 403
                            ? "You are not authorized to access this submission."
                            : "Failed to load submission file (HTTP " + code + ").");
                    callback.onError(msg, code);
                    return;
                }

                String headerMime = response.headers().get("Content-Type");
                String effectiveMime = resolveMimeType(safeName,
                        headerMime != null && !headerMime.contains("octet-stream") ? headerMime.split(";")[0].trim() : declaredMime);

                final ResponseBody body = response.body();
                new Thread(() -> {
                    try {
                        File cacheRoot = new File(appCtx.getCacheDir(), "submission_preview");
                        if (!cacheRoot.exists() && !cacheRoot.mkdirs()) {
                            throw new IllegalStateException("Unable to create preview cache directory.");
                        }
                        String prefix = isStudentMode ? "stu_sub_" : "tch_sub_";
                        File target = new File(cacheRoot, prefix + submissionId + "_" + safeName);
                        try (InputStream in = body.byteStream(); OutputStream out = new FileOutputStream(target)) {
                            byte[] buf = new byte[8192];
                            int len;
                            while ((len = in.read(buf)) != -1) {
                                out.write(buf, 0, len);
                            }
                            out.flush();
                        }
                        if (!target.exists() || target.length() == 0) {
                            mainHandler().post(() -> callback.onError("Downloaded file is empty or corrupted.", 0));
                            return;
                        }
                        mainHandler().post(() -> callback.onSuccess(target, effectiveMime));
                    } catch (Exception e) {
                        mainHandler().post(() -> callback.onError("Unable to read submission file: " + e.getMessage(), 0));
                    }
                }).start();
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> c, @NonNull Throwable t) {
                if (c.isCanceled()) return;
                callback.onError("Network error while retrieving submission file.", 0);
            }
        });
        return call;
    }

    @Nullable
    public static Bitmap decodeSampledBitmap(@NonNull File file, int maxDimensionPx) {
        if (!file.exists()) return null;
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), bounds);
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null;
        }

        int inSampleSize = 1;
        while ((bounds.outWidth / inSampleSize) > maxDimensionPx || (bounds.outHeight / inSampleSize) > maxDimensionPx) {
            inSampleSize *= 2;
        }

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = inSampleSize;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
    }

    public static int getPdfPageCount(@NonNull File pdfFile) {
        if (!pdfFile.exists()) return 0;
        try (ParcelFileDescriptor pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
             PdfRenderer renderer = new PdfRenderer(pfd)) {
            return renderer.getPageCount();
        } catch (Exception e) {
            return 0;
        }
    }

    @Nullable
    public static Bitmap renderPdfPage(@NonNull File pdfFile, int pageIndex, int targetWidthPx) {
        if (!pdfFile.exists() || pageIndex < 0) return null;
        try (ParcelFileDescriptor pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
             PdfRenderer renderer = new PdfRenderer(pfd)) {
            int count = renderer.getPageCount();
            if (pageIndex >= count) return null;
            try (PdfRenderer.Page page = renderer.openPage(pageIndex)) {
                int w = Math.max(400, Math.min(2000, targetWidthPx));
                int h = Math.max(1, Math.round((float) w * page.getHeight() / Math.max(1, page.getWidth())));
                Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                bmp.eraseColor(Color.WHITE);
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                return bmp;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static void openWithExternalViewer(@NonNull Context context, @NonNull File cachedFile, @Nullable String mimeType) {
        try {
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", cachedFile);
            String resolvedMime = resolveMimeType(cachedFile.getName(), mimeType);

            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
            viewIntent.setDataAndType(uri, resolvedMime);
            viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);

            Intent chooser = Intent.createChooser(viewIntent, "Open submission with");
            context.startActivity(chooser);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "No installed viewer found for this file type. Try Download instead.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, "Unable to open file externally: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static void saveToDownloads(
            @NonNull Context context,
            @NonNull File sourceFile,
            @NonNull String displayFileName,
            @Nullable String mimeType
    ) {
        String safeDisplay = sanitizeFileName(displayFileName, 0);
        String resolvedMime = resolveMimeType(safeDisplay, mimeType);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, safeDisplay);
                values.put(MediaStore.Downloads.MIME_TYPE, resolvedMime);
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                values.put(MediaStore.Downloads.IS_PENDING, 1);

                Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) {
                    throw new IllegalStateException("Could not create Downloads entry.");
                }
                try (InputStream in = new FileInputStream(sourceFile);
                     OutputStream out = context.getContentResolver().openOutputStream(uri)) {
                    if (out == null) throw new IllegalStateException("Could not open Downloads stream.");
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
                }
                values.clear();
                values.put(MediaStore.Downloads.IS_PENDING, 0);
                context.getContentResolver().update(uri, values, null, null);
            } else {
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!dir.exists() && !dir.mkdirs()) {
                    throw new IllegalStateException("Downloads directory unavailable.");
                }
                File outFile = new File(dir, safeDisplay);
                try (InputStream in = new FileInputStream(sourceFile); OutputStream out = new FileOutputStream(outFile)) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
                }
            }
            Toast.makeText(context, "Saved to Downloads: " + safeDisplay, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
