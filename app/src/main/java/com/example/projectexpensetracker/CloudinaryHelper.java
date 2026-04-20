package com.example.projectexpensetracker;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CloudinaryHelper {

    // ────────────────────────────────────────────────────────────────────────
    // WARNING: Replace these placeholders with your actual Cloudinary details!
    // ────────────────────────────────────────────────────────────────────────
    public static final String CLOUD_NAME = "dispgthlu"; 
    public static final String UPLOAD_PRESET = "ml_default"; // Must be an UNSIGNED preset

    private static final String UPLOAD_URL = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";

    public interface UploadCallback {
        void onSuccess(String secureUrl);
        void onError(String error);
    }

    public static void uploadImage(File imageFile, UploadCallback callback) {
        if (CLOUD_NAME.equals("YOUR_CLOUD_NAME")) {
            new Handler(Looper.getMainLooper()).post(() -> 
                callback.onError("Please configure Cloudinary credentials in CloudinaryHelper.java")
            );
            return;
        }

        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .addFormDataPart("file", imageFile.getName(),
                        RequestBody.create(imageFile, MediaType.parse("image/jpeg")))
                .build();

        Request request = new Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> 
                    callback.onError("Network error: " + e.getMessage())
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String secureUrl = jsonObject.getString("secure_url");
                        
                        new Handler(Looper.getMainLooper()).post(() -> 
                            callback.onSuccess(secureUrl)
                        );
                    } catch (Exception e) {
                        new Handler(Looper.getMainLooper()).post(() -> 
                            callback.onError("Failed to parse Cloudinary response")
                        );
                    }
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> 
                        callback.onError("Upload failed with code: " + response.code())
                    );
                }
            }
        });
    }
}
