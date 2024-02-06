package com.example.spendsmart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spendsmart.databinding.ActivityMainBinding;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnItemsClick{
    ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private ExpensesAdapter expensesAdapter;

    private Intent intent;
    private long income=0,expense=0;
    private List<ExpenseModel> allExpenses = new ArrayList<>();
    private boolean showingAllItems = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.recyclerView.setOnCreateContextMenuListener(this);

        expensesAdapter = new ExpensesAdapter(this, this);
        binding.recyclerView.setAdapter(expensesAdapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        TextView incomeButton = findViewById(R.id.incomeButton);
        TextView expenseButton = findViewById(R.id.expenseButton);

        incomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFilter("Income");
            }
        });

        expenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFilter("Expense");
            }
        });


        Button addIncomeButton = findViewById(R.id.addIncome);
        Button addExpenseButton = findViewById(R.id.addExpense);

        addIncomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddExpenseActivity("Income");
            }
        });

        addExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddExpenseActivity("Expense");
            }
        });
    }

    private void toggleFilter(String filterType) {
        if (!showingAllItems) {
            expensesAdapter.showAllItems();
        } else {
            if (filterType.equals("Income")) {
                filterByIncome(false);
            } else {
                filterByExpense(false);
            }
        }
        showingAllItems = !showingAllItems;
    }




    private void openAddExpenseActivity(String type) {
        intent = new Intent(MainActivity.this, AddExpenseActivity.class);
        intent.putExtra("expenseType", type);
        startActivity(intent);
    }

    protected void onStart() {
        super.onStart();
        showProgressDialog("Please wait...");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            hideProgressDialog();
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                            } else {
                            }
                        }
                    });
        } else if (!currentUser.isAnonymous()) {
            mAuth.signOut();
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            hideProgressDialog();
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                            } else {
                            }
                        }
                    });
        } else {
            hideProgressDialog();
        }
    }

    private void showProgressDialog(String message) {
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    protected void onResume(){
        super.onResume();
        income=0;
        expense=0;
        getData();
    }

    private void getData() {
        FirebaseFirestore.getInstance()
                .collection("expenses")
                .whereEqualTo("uid", FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    expensesAdapter.clear();
                    allExpenses.clear();
                    List<DocumentSnapshot> dsList = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot ds : dsList) {
                        ExpenseModel expenseModel = ds.toObject(ExpenseModel.class);
                        allExpenses.add(expenseModel);
                        if (expenseModel.getType().equalsIgnoreCase("income")) {
                            income += expenseModel.getAmount();
                        } else {
                            expense += expenseModel.getAmount();
                        }
                    }
                    expensesAdapter.setItems(allExpenses);
                    setUpGraph();
                });
    }

    public void setUpGraph() {
        List<PieEntry> pieEntryList=new ArrayList<>();
        List<Integer> colorsList=new ArrayList<>();
        if(income!=0){
            pieEntryList.add(new PieEntry(income,"Income"));
            colorsList.add(getResources().getColor(R.color.green));
        }
        if(expense!=0){
            pieEntryList.add(new PieEntry(expense,"Expense"));
            colorsList.add(getResources().getColor(R.color.yellow));
        }
        PieDataSet pieDataSet=new PieDataSet(pieEntryList, String.valueOf(income-expense));
        pieDataSet.setColors(colorsList);
        PieData pieData=new PieData(pieDataSet);

        binding.pieChart.setData(pieData);
        binding.pieChart.invalidate();
    }

    public void filterByIncome(boolean showAll) {
        if (showAll) {
            expensesAdapter.showAllItems();
            return;
        }

        List<ExpenseModel> filteredIncome = new ArrayList<>();
        for (ExpenseModel expense : expensesAdapter.getAllItems()) {
            if (expense.getType().equalsIgnoreCase("Income")) {
                filteredIncome.add(expense);
            }
        }
        expensesAdapter.filterItems(filteredIncome);
    }

    public void filterByExpense(boolean showAll) {
        if (showAll) {
            expensesAdapter.showAllItems();
            return;
        }

        List<ExpenseModel> filteredExpense = new ArrayList<>();
        for (ExpenseModel expense : expensesAdapter.getAllItems()) {
            if (expense.getType().equalsIgnoreCase("Expense")) {
                filteredExpense.add(expense);
            }
        }
        expensesAdapter.filterItems(filteredExpense);
    }


    @Override
    public void onClick(ExpenseModel expenseModel) {
        Intent intent=new Intent(MainActivity.this, AddExpenseActivity.class);
        if (expenseModel != null) {
            intent.putExtra("model", expenseModel);
            startActivity(intent);
        } else {
            Toast.makeText(this, "ExpenseModel is null", Toast.LENGTH_SHORT).show();
        }
    }
}
