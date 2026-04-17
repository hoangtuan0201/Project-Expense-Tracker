package com.example.projectexpensetracker;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * AddEditExpenseActivity — Req (c)
 * Dùng cho add mới và edit expense.
 * Truyền vào: "project_id"  (bắt buộc khi add)
 *             "expense_id"  (khi edit — nếu có sẽ vào edit mode)
 */
public class AddEditExpenseActivity extends AppCompatActivity {

    // ─── Views ───────────────────────────────────────────────────────────────────
    private TextInputLayout   layoutCode, layoutAmount, layoutType;
    private TextInputLayout   layoutDate, layoutPaymentMethod, layoutClaimant, layoutStatus;
    private TextInputEditText etCode, etAmount, etDate, etClaimant, etDescription, etLocation;
    private AutoCompleteTextView spinnerCurrency, spinnerType, spinnerPaymentMethod, spinnerStatus;
    private MaterialButton    btnSave;

    // ─── Data ────────────────────────────────────────────────────────────────────
    private DatabaseHelper databaseHelper;
    private Expense  existingExpense;
    private int      projectId;
    private boolean  isEditMode = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_expense);

        databaseHelper = new DatabaseHelper(this);
        projectId      = getIntent().getIntExtra("project_id", -1);
        int expenseId  = getIntent().getIntExtra("expense_id", -1);

        initViews();
        setupToolbar();
        setupSpinners();
        setupDatePicker();

        if (expenseId != -1) {
            isEditMode      = true;
            existingExpense = databaseHelper.getExpenseById(expenseId);
            if (existingExpense != null) {
                projectId = existingExpense.getProjectId();
                populateForm(existingExpense);
                ((MaterialToolbar) findViewById(R.id.toolbar)).setTitle("Edit Expense");
                btnSave.setText("Update Expense");
            }
        }

        btnSave.setOnClickListener(v -> {
            if (validateForm()) showConfirmationDialog();
        });
    }

    // ─── Init ────────────────────────────────────────────────────────────────────

    private void initViews() {
        layoutCode          = findViewById(R.id.layoutExpenseCode);
        layoutAmount        = findViewById(R.id.layoutAmount);
        layoutType          = findViewById(R.id.layoutExpenseType);
        layoutDate          = findViewById(R.id.layoutExpenseDate);
        layoutPaymentMethod = findViewById(R.id.layoutPaymentMethod);
        layoutClaimant      = findViewById(R.id.layoutClaimant);
        layoutStatus        = findViewById(R.id.layoutPaymentStatus);

        etCode        = findViewById(R.id.etExpenseCode);
        etAmount      = findViewById(R.id.etAmount);
        etDate        = findViewById(R.id.etExpenseDate);
        etClaimant    = findViewById(R.id.etClaimant);
        etDescription = findViewById(R.id.etExpDescription);
        etLocation    = findViewById(R.id.etLocation);

        spinnerCurrency      = findViewById(R.id.spinnerCurrency);
        spinnerType          = findViewById(R.id.spinnerExpenseType);
        spinnerPaymentMethod = findViewById(R.id.spinnerPaymentMethod);
        spinnerStatus        = findViewById(R.id.spinnerPaymentStatus);

        btnSave = findViewById(R.id.btnSaveExpense);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupSpinners() {
        String[] currencies = {"USD", "EUR", "GBP", "VND", "JPY", "AUD", "CAD"};
        spinnerCurrency.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, currencies));
        spinnerCurrency.setText(currencies[0], false);

        spinnerType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, Expense.EXPENSE_TYPES));
        spinnerType.setText(Expense.EXPENSE_TYPES[0], false);

        spinnerPaymentMethod.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, Expense.PAYMENT_METHODS));
        spinnerPaymentMethod.setText(Expense.PAYMENT_METHODS[0], false);

        spinnerStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, Expense.PAYMENT_STATUSES));
        spinnerStatus.setText(Expense.PAYMENT_STATUSES[0], false);
    }

    private void setupDatePicker() {
        etDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Date of Expense").build();
            picker.addOnPositiveButtonClickListener(sel -> {
                String formatted = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(new Date(sel));
                etDate.setText(formatted);
            });
            picker.show(getSupportFragmentManager(), "EXPENSE_DATE");
        });
    }

    private void populateForm(Expense e) {
        etCode.setText(e.getExpenseCode());
        etAmount.setText(String.valueOf(e.getAmount()));
        spinnerCurrency.setText(e.getCurrency(), false);
        spinnerType.setText(e.getType(), false);
        etDate.setText(e.getDate());
        spinnerPaymentMethod.setText(e.getPaymentMethod(), false);
        etClaimant.setText(e.getClaimant());
        spinnerStatus.setText(e.getPaymentStatus(), false);
        if (e.getDescription() != null) etDescription.setText(e.getDescription());
        if (e.getLocation()    != null) etLocation.setText(e.getLocation());
    }

    // ─── Validation ──────────────────────────────────────────────────────────────

    private boolean validateForm() {
        boolean ok = true;
        layoutCode.setError(null);
        layoutAmount.setError(null);
        layoutDate.setError(null);
        layoutClaimant.setError(null);

        if (text(etCode).isEmpty())    { layoutCode.setError("Please enter an expense code"); ok = false; }
        if (text(etAmount).isEmpty())  { layoutAmount.setError("Please enter an amount"); ok = false; }
        else {
            try { double v = Double.parseDouble(text(etAmount)); if (v <= 0) { layoutAmount.setError("Amount must be > 0"); ok = false; } }
            catch (NumberFormatException e) { layoutAmount.setError("Invalid number"); ok = false; }
        }
        if (text(etDate).isEmpty())    { layoutDate.setError("Please select a date"); ok = false; }
        if (text(etClaimant).isEmpty()) { layoutClaimant.setError("Please enter a claimant name"); ok = false; }

        return ok;
    }

    // ─── Confirmation (Req c mirrors Req a) ──────────────────────────────────────

    private void showConfirmationDialog() {
        String msg =
                "Code: "     + text(etCode)   + "\n" +
                "Amount: "   + spinnerCurrency.getText() + " " + text(etAmount) + "\n" +
                "Type: "     + spinnerType.getText()    + "\n" +
                "Date: "     + text(etDate)             + "\n" +
                "Payment: "  + spinnerPaymentMethod.getText() + "\n" +
                "Claimant: " + text(etClaimant)         + "\n" +
                "Status: "   + spinnerStatus.getText();

        new MaterialAlertDialogBuilder(this)
                .setTitle(isEditMode ? "Confirm Update" : "Confirm New Expense")
                .setMessage(msg)
                .setPositiveButton("Confirm & Save", (d, w) -> saveExpense())
                .setNegativeButton("Edit Information", null)
                .show();
    }

    // ─── Save ────────────────────────────────────────────────────────────────────

    private void saveExpense() {
        String code    = text(etCode);
        double amount  = Double.parseDouble(text(etAmount));
        String cur     = spinnerCurrency.getText().toString();
        String type    = spinnerType.getText().toString();
        String date    = text(etDate);
        String method  = spinnerPaymentMethod.getText().toString();
        String claim   = text(etClaimant);
        String status  = spinnerStatus.getText().toString();
        String desc    = text(etDescription);
        String loc     = text(etLocation);

        if (isEditMode && existingExpense != null) {
            existingExpense.setExpenseCode(code);
            existingExpense.setAmount(amount);
            existingExpense.setCurrency(cur);
            existingExpense.setType(type);
            existingExpense.setDate(date);
            existingExpense.setPaymentMethod(method);
            existingExpense.setClaimant(claim);
            existingExpense.setPaymentStatus(status);
            existingExpense.setDescription(desc.isEmpty() ? null : desc);
            existingExpense.setLocation(loc.isEmpty() ? null : loc);
            existingExpense.setIsSynced(0);

            int rows = databaseHelper.updateExpense(existingExpense);
            if (rows > 0) {
                Toast.makeText(this, "Expense updated!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Update failed, please try again.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Expense newExp = new Expense(code, projectId, date, amount, cur, type,
                    method, claim, status,
                    desc.isEmpty() ? null : desc,
                    loc.isEmpty()  ? null : loc);

            long id = databaseHelper.addExpense(newExp);
            if (id != -1) {
                Toast.makeText(this, "Expense added!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to save. Expense code may already exist.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // ─── Helper ──────────────────────────────────────────────────────────────────

    private String text(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
