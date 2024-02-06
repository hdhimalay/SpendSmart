package com.example.spendsmart;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText amountEditText, notesEditText, categoryEditText;
    private RadioGroup typeRadioGroup;
    private RadioButton incomeRadioButton, expenseRadioButton;
    private Button saveButton;
    private ExpenseModel expenseModel;
    private ExpensesAdapter expensesAdapter;


    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        firestore = FirebaseFirestore.getInstance();

        amountEditText = findViewById(R.id.addAmount);
        notesEditText = findViewById(R.id.addNotes);
        categoryEditText = findViewById(R.id.addCategory);
        typeRadioGroup = findViewById(R.id.typeRadioGroup);
        saveButton = findViewById(R.id.saveButton);
        incomeRadioButton = findViewById(R.id.incomeRadio);
        expenseRadioButton = findViewById(R.id.expenseRadio);


        Log.d("AddExpenseActivity", "Received Intent: " + getIntent());

        expenseModel = (ExpenseModel) getIntent().getSerializableExtra("model");
        Log.d("AddExpenseActivity", "Received ExpenseModel: " + expenseModel);
        expensesAdapter = new ExpensesAdapter(this, null);

        if (expenseModel != null) {
            //  Update mode
            amountEditText.setText(String.valueOf(expenseModel.getAmount()));
            notesEditText.setText(expenseModel.getNotes());
            categoryEditText.setText(expenseModel.getCategory());

            // Set RadioButtons based on the type
            if ("Income".equals(expenseModel.getType())) {
                incomeRadioButton.setChecked(true);
            } else if ("Expense".equals(expenseModel.getType())) {
                expenseRadioButton.setChecked(true);
            }

            saveButton.setText("Update");

            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onUpdateButtonClick();
                }
            });
        } else {

            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSaveButtonClick();
                }
            });
        }
    }

    private void onSaveButtonClick() {
        long amount;
        try {
            amount = Long.parseLong(amountEditText.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount == 0) {
            Toast.makeText(this, "Amount cannot be zero", Toast.LENGTH_SHORT).show();
            return;
        }

        String notes = notesEditText.getText().toString();
        String category = categoryEditText.getText().toString();

        int selectedRadioId = typeRadioGroup.getCheckedRadioButtonId();
        String type = "";
        if (selectedRadioId == R.id.incomeRadio) {
            type = "Income";
        } else if (selectedRadioId == R.id.expenseRadio) {
            type = "Expense";
        }

        String expenseId = generateRandomExpenseId();

        ExpenseModel expense = new ExpenseModel(expenseId, amount, System.currentTimeMillis(), type, notes, category, FirebaseAuth.getInstance().getUid());


        saveExpenseToFirestore(expense);
    }

    private void onUpdateButtonClick() {
        long amount;
        try {
            amount = Long.parseLong(amountEditText.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount == 0) {
            Toast.makeText(this, "Amount cannot be zero", Toast.LENGTH_SHORT).show();
            return;
        }

        String notes = notesEditText.getText().toString();
        String category = categoryEditText.getText().toString();


        int selectedRadioId = typeRadioGroup.getCheckedRadioButtonId();
        String type = "";
        if (selectedRadioId == R.id.incomeRadio) {
            type = "Income";
        } else if (selectedRadioId == R.id.expenseRadio) {
            type = "Expense";
        }

        // Update the values of the existing expenseModel
        expenseModel.setAmount(amount);
        expenseModel.setNotes(notes);
        expenseModel.setCategory(category);
        expenseModel.setType(type);

        // Save updated expense data to Firestore
        updateExpenseInFirestore(expenseModel);

        // After updating in Firestore, update item in the RecyclerView
        expensesAdapter.addOrUpdate(expenseModel);
    }


    private String generateRandomExpenseId() {

        return "EXP" + System.currentTimeMillis();
    }

    private void saveExpenseToFirestore(ExpenseModel expense) {

        firestore.collection("expenses")
                .document(expense.getExpenseId())  // Use the specific document ID
                .set(expense)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Expense saved successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity after saving
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateExpenseInFirestore(ExpenseModel expense) {

        firestore.collection("expenses")
                .document(expense.getExpenseId())
                .set(expense)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Expense updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update expense", Toast.LENGTH_SHORT).show();
                });
    }
}
