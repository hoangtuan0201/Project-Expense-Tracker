package com.example.projectexpensetracker.activities;

import com.example.projectexpensetracker.R;
import com.example.projectexpensetracker.models.*;
import com.example.projectexpensetracker.database.*;
import com.example.projectexpensetracker.adapters.*;
import com.example.projectexpensetracker.utils.*;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.io.File;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;

/**
 * AddEditProjectActivity — dùng cho cả hai trường hợp:
 *   1. Thêm project mới  → gọi từ ProjectListActivity (không truyền project_id)
 *   2. Sửa project cũ   → gọi từ ProjectDetailActivity (truyền project_id)
 */
public class AddEditProjectActivity extends AppCompatActivity {

    // ─── UI Views ────────────────────────────────────────────────────────────────
    private TextInputLayout   layoutProjectCode, layoutProjectName, layoutDescription;
    private TextInputLayout   layoutStartDate, layoutEndDate, layoutManager;
    private TextInputLayout   layoutStatus, layoutBudget;
    private TextInputEditText etProjectCode, etProjectName, etDescription;
    private TextInputEditText etStartDate, etEndDate, etManager, etBudget;
    private TextInputEditText etSpecialRequirements, etClientInfo;
    private AutoCompleteTextView spinnerStatus;
    private MaterialButton    btnSaveProject;
    private MaterialButton    btnTakePhoto;
    private ImageView         ivProjectPhoto;

    // ─── Data ────────────────────────────────────────────────────────────────────
    private DatabaseHelper databaseHelper;
    private Project existingProject; // null nếu đang thêm mới
    private int currentUserId;
    private boolean isEditMode = false;
    
    private File currentPhotoFile;
    private String uploadedImageUrl = null;
    private ActivityResultLauncher<String> pickImageLauncher;

    // ─── Lifecycle ───────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_project);

        databaseHelper = new DatabaseHelper(this);

        // Lấy userId từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);

        initViews();
        setupToolbar();
        setupStatusDropdown();
        setupDatePickers();
        setupCameraLauncher();
        checkEditMode(); // xác định add hay edit
        setupSaveButton();
    }

    // ─── Init ────────────────────────────────────────────────────────────────────

    private void initViews() {
        layoutProjectCode  = findViewById(R.id.layoutProjectCode);
        layoutProjectName  = findViewById(R.id.layoutProjectName);
        layoutDescription  = findViewById(R.id.layoutDescription);
        layoutStartDate    = findViewById(R.id.layoutStartDate);
        layoutEndDate      = findViewById(R.id.layoutEndDate);
        layoutManager      = findViewById(R.id.layoutManager);
        layoutStatus       = findViewById(R.id.layoutStatus);
        layoutBudget       = findViewById(R.id.layoutBudget);

        etProjectCode         = findViewById(R.id.etProjectCode);
        etProjectName         = findViewById(R.id.etProjectName);
        etDescription         = findViewById(R.id.etDescription);
        etStartDate           = findViewById(R.id.etStartDate);
        etEndDate             = findViewById(R.id.etEndDate);
        etManager             = findViewById(R.id.etManager);
        etBudget              = findViewById(R.id.etBudget);
        etSpecialRequirements = findViewById(R.id.etSpecialRequirements);
        etClientInfo          = findViewById(R.id.etClientInfo);
        spinnerStatus         = findViewById(R.id.spinnerStatus);
        btnSaveProject        = findViewById(R.id.btnSaveProject);
        btnTakePhoto          = findViewById(R.id.btnTakePhoto);
        ivProjectPhoto        = findViewById(R.id.ivProjectPhoto);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        // Nút back trên toolbar
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupStatusDropdown() {
        String[] statuses = {"Active", "Completed", "On Hold"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, statuses);
        spinnerStatus.setAdapter(adapter);
        spinnerStatus.setText(statuses[0], false); // default = "Active"
    }

    private void setupDatePickers() {
        // Mở date picker khi click vào Start Date field
        etStartDate.setOnClickListener(v -> showDatePicker("Select Start Date", etStartDate));

        // Mở date picker khi click vào End Date field
        etEndDate.setOnClickListener(v -> showDatePicker("Select End Date", etEndDate));
    }

    private void setupCameraLauncher() {
        pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        File cacheDir = new File(getCacheDir(), "camera_photos");
                        if (!cacheDir.exists()) cacheDir.mkdirs();
                        currentPhotoFile = File.createTempFile("project_photo_", ".jpg", cacheDir);
                        
                        InputStream is = getContentResolver().openInputStream(uri);
                        OutputStream os = new FileOutputStream(currentPhotoFile);
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = is.read(buffer)) > 0) {
                            os.write(buffer, 0, length);
                        }
                        os.flush();
                        os.close();
                        is.close();
                        
                        processPickedOrCapturedImage();
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to process selected image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );

        btnTakePhoto.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });
    }

    private void processPickedOrCapturedImage() {
        // Hiển thị ảnh
        ivProjectPhoto.setVisibility(android.view.View.VISIBLE);
        Glide.with(this).load(currentPhotoFile).into(ivProjectPhoto);
        
        // Bắt đầu upload lên Cloudinary
        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();
        btnSaveProject.setEnabled(false); // disable save while uploading
        
        CloudinaryHelper.uploadImage(currentPhotoFile, new CloudinaryHelper.UploadCallback() {
            @Override
            public void onSuccess(String secureUrl) {
                uploadedImageUrl = secureUrl;
                Toast.makeText(AddEditProjectActivity.this, "Image uploaded!", Toast.LENGTH_SHORT).show();
                btnSaveProject.setEnabled(true);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AddEditProjectActivity.this, error, Toast.LENGTH_LONG).show();
                btnSaveProject.setEnabled(true);
            }
        });
    }

    /**
     * Kiểm tra Intent có truyền project_id không.
     * Nếu có → edit mode: load data lên form.
     */
    private void checkEditMode() {
        int projectId = getIntent().getIntExtra("project_id", -1);

        if (projectId != -1) {
            isEditMode      = true;
            existingProject = databaseHelper.getProjectById(projectId);

            if (existingProject != null) {
                populateForm(existingProject);
                // Đổi title toolbar
                MaterialToolbar toolbar = findViewById(R.id.toolbar);
                toolbar.setTitle("Edit Project");
                btnSaveProject.setText("Update Project");
            }
        }
    }

    /** Điền dữ liệu project vào form (chế độ edit). */
    private void populateForm(Project p) {
        etProjectCode.setText(p.getProjectCode());
        etProjectName.setText(p.getProjectName());
        etDescription.setText(p.getDescription());
        etStartDate.setText(p.getStartDate());
        etEndDate.setText(p.getEndDate());
        etManager.setText(p.getManager());
        spinnerStatus.setText(p.getStatus(), false);
        etBudget.setText(String.valueOf(p.getBudget()));
        etSpecialRequirements.setText(p.getSpecialRequirements());
        etClientInfo.setText(p.getClientInfo());
        
        if (p.getPhotoUrl() != null && !p.getPhotoUrl().isEmpty()) {
            uploadedImageUrl = p.getPhotoUrl();
            ivProjectPhoto.setVisibility(android.view.View.VISIBLE);
            Glide.with(this).load(uploadedImageUrl).into(ivProjectPhoto);
            btnTakePhoto.setText("Retake Photo");
        }
    }

    // ─── Save ────────────────────────────────────────────────────────────────────

    private void setupSaveButton() {
        btnSaveProject.setOnClickListener(v -> {
            if (validateForm()) {
                showConfirmationDialog();
            }
        });
    }

    /**
     * Req (a): Hiển thị lại toàn bộ thông tin đã nhập để user xác nhận trước khi lưu.
     * User có thể bấm "Edit" để quay lại sửa, hoặc "Confirm" để lưu.
     */
    private void showConfirmationDialog() {
        String code    = getFieldText(etProjectCode);
        String name    = getFieldText(etProjectName);
        String start   = getFieldText(etStartDate);
        String end     = getFieldText(etEndDate);
        String manager = getFieldText(etManager);
        String status  = spinnerStatus.getText().toString().trim();
        String budget  = getFieldText(etBudget);
        String specReq = getFieldText(etSpecialRequirements);
        String client  = getFieldText(etClientInfo);

        StringBuilder message = new StringBuilder();
        message.append("Code: ").append(code).append("\n");
        message.append("Name: ").append(name).append("\n");
        message.append("Manager: ").append(manager).append("\n");
        message.append("Status: ").append(status).append("\n");
        message.append("Budget: $").append(budget).append("\n");
        message.append("Period: ").append(start).append(" → ").append(end);
        if (!specReq.isEmpty()) message.append("\nSpecial: ").append(specReq);
        if (!client.isEmpty())  message.append("\nClient: ").append(client);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(isEditMode ? "Confirm Update" : "Confirm New Project")
                .setMessage(message.toString())
                .setPositiveButton("Confirm & Save", (d, w) -> saveProject())
                .setNegativeButton("Edit Information", null)
                .show();
    }

    /**
     * Validate tất cả required fields.
     * Nếu field nào trống → set error message trên TextInputLayout và trả về false.
     * Nếu tất cả hợp lệ → xoá hết lỗi và trả về true.
     */
    private boolean validateForm() {
        boolean isValid = true;

        // Xoá lỗi cũ
        clearErrors();

        // Project Code
        String code = getFieldText(etProjectCode);
        if (code.isEmpty()) {
            layoutProjectCode.setError("Please enter a project code");
            isValid = false;
        }

        // Project Name
        String name = getFieldText(etProjectName);
        if (name.isEmpty()) {
            layoutProjectName.setError("Please enter a project name");
            isValid = false;
        }

        // Description
        String desc = getFieldText(etDescription);
        if (desc.isEmpty()) {
            layoutDescription.setError("Please enter a description");
            isValid = false;
        }

        // Start Date
        String startDate = getFieldText(etStartDate);
        if (startDate.isEmpty()) {
            layoutStartDate.setError("Please select a start date");
            isValid = false;
        }

        // End Date
        String endDate = getFieldText(etEndDate);
        if (endDate.isEmpty()) {
            layoutEndDate.setError("Please select an end date");
            isValid = false;
        }

        // End date phải sau start date
        if (!startDate.isEmpty() && !endDate.isEmpty() && endDate.compareTo(startDate) < 0) {
            layoutEndDate.setError("End date must be after start date");
            isValid = false;
        }

        // Manager
        String manager = getFieldText(etManager);
        if (manager.isEmpty()) {
            layoutManager.setError("Please enter a project manager");
            isValid = false;
        }

        // Status
        String status = spinnerStatus.getText().toString().trim();
        if (status.isEmpty()) {
            layoutStatus.setError("Please select a status");
            isValid = false;
        }

        // Budget
        String budgetStr = getFieldText(etBudget);
        if (budgetStr.isEmpty()) {
            layoutBudget.setError("Please enter a budget");
            isValid = false;
        } else {
            try {
                double budget = Double.parseDouble(budgetStr);
                if (budget < 0) {
                    layoutBudget.setError("Budget cannot be negative");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                layoutBudget.setError("Please enter a valid number");
                isValid = false;
            }
        }

        return isValid;
    }

    private void clearErrors() {
        layoutProjectCode.setError(null);
        layoutProjectName.setError(null);
        layoutDescription.setError(null);
        layoutStartDate.setError(null);
        layoutEndDate.setError(null);
        layoutManager.setError(null);
        layoutStatus.setError(null);
        layoutBudget.setError(null);
    }

    /** Lưu hoặc cập nhật project sau khi validate thành công. */
    private void saveProject() {
        String code     = getFieldText(etProjectCode);
        String name     = getFieldText(etProjectName);
        String desc     = getFieldText(etDescription);
        String start    = getFieldText(etStartDate);
        String end      = getFieldText(etEndDate);
        String manager  = getFieldText(etManager);
        String status   = spinnerStatus.getText().toString().trim();
        double budget   = Double.parseDouble(getFieldText(etBudget));
        String specReq  = getFieldText(etSpecialRequirements);
        String client   = getFieldText(etClientInfo);

        if (isEditMode && existingProject != null) {
            // ── UPDATE ────────────────────────────────────────────────────────
            existingProject.setProjectCode(code);
            existingProject.setProjectName(name);
            existingProject.setDescription(desc);
            existingProject.setStartDate(start);
            existingProject.setEndDate(end);
            existingProject.setManager(manager);
            existingProject.setStatus(status);
            existingProject.setBudget(budget);
            existingProject.setSpecialRequirements(specReq);
            existingProject.setClientInfo(client);
            if (uploadedImageUrl != null) {
                existingProject.setPhotoUrl(uploadedImageUrl);
            }
            // Đánh dấu chưa sync vì data vừa thay đổi
            existingProject.setIsSynced(0);

            int rows = databaseHelper.updateProject(existingProject);
            if (rows > 0) {
                Toast.makeText(this, "Project updated successfully!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Update failed. Please try again.", Toast.LENGTH_SHORT).show();
            }

        } else {
            // ── INSERT ────────────────────────────────────────────────────────
            Project newProject = new Project(
                    code, name, desc, start, end,
                    manager, status, budget,
                    specReq.isEmpty() ? null : specReq,
                    client.isEmpty()  ? null : client,
                    uploadedImageUrl, // photoUrl
                    currentUserId
            );

            long newId = databaseHelper.addProject(newProject);
            if (newId != -1) {
                Toast.makeText(this, "Project added successfully!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                // Lỗi phổ biến nhất: trùng project code
                Toast.makeText(this,
                        "Failed to save. Project code may already exist.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    /** Lấy text từ EditText, loại bỏ khoảng trắng thừa. Trả về "" nếu null. */
    private String getFieldText(TextInputEditText field) {
        if (field.getText() == null) return "";
        return field.getText().toString().trim();
    }

    /** Hiển thị Material Date Picker và ghi kết quả vào target EditText. */
    private void showDatePicker(String title, TextInputEditText target) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText(title)
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            String formatted = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(new Date(selection));
            target.setText(formatted);
        });

        picker.show(getSupportFragmentManager(), "DATE_PICKER_" + title);
    }
}
