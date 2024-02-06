package com.example.spendsmart;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpensesAdapter extends RecyclerView.Adapter <ExpensesAdapter.MyViewHolder> {
    private Context context;
    private OnItemsClick onItemsClick;
    private List<ExpenseModel> expenseModelList;
    private List<ExpenseModel> originalExpenseModelList;


    public ExpensesAdapter(Context context, OnItemsClick onItemsClick) {
        this.context = context;
        expenseModelList = new ArrayList<>();
        this.onItemsClick = onItemsClick;
        originalExpenseModelList = new ArrayList<>(expenseModelList);

    }

    public List<ExpenseModel> getAllItems() {
        return expenseModelList;
    }

    public void filterItems(List<ExpenseModel> filteredItems) {
        expenseModelList.clear();
        expenseModelList.addAll(filteredItems);
        notifyDataSetChanged();
    }

    public void showAllItems() {
        expenseModelList.clear();
        expenseModelList.addAll(originalExpenseModelList);
        notifyDataSetChanged();
    }

    public void addOrUpdate(ExpenseModel expenseModel) {
        int existingIndex = -1;
        for (int i = 0; i < expenseModelList.size(); i++) {
            if (expenseModelList.get(i).getExpenseId().equals(expenseModel.getExpenseId())) {
                existingIndex = i;
                break;
            }
        }

        if (existingIndex != -1) {
            // Update existing item
            expenseModelList.set(existingIndex, expenseModel);
        } else {
            //add new item
            expenseModelList.add(expenseModel);
        }

        notifyDataSetChanged();
    }


    public void setItems(List<ExpenseModel> items) {
        expenseModelList = items;
        originalExpenseModelList.clear();
        originalExpenseModelList.addAll(items);
        notifyDataSetChanged();
    }


    public void clear() {
        expenseModelList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ExpenseModel expenseModel = expenseModelList.get(position);
        holder.note.setText(expenseModel.getNotes());
        holder.category.setText(expenseModel.getCategory());
        holder.amount.setText(String.valueOf(expenseModel.getAmount()));
        holder.date.setText(String.valueOf(expenseModel.getTime())); // Assuming getTime() returns date as a String

        if (expenseModel.getType().equalsIgnoreCase("Expense")) {
            holder.itemView.setBackgroundResource(R.drawable.expense_background);
        } else if (expenseModel.getType().equalsIgnoreCase("Income")) {
            holder.itemView.setBackgroundResource(R.drawable.income_background);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemsClick.onClick(expenseModel);
            }
        });

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(expenseModel.getTime()));

        holder.date.setText(formattedDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemsClick.onClick(expenseModel);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showDeleteConfirmationDialog(expenseModel, position);
                return true;
            }
        });
    }


    @Override
    public int getItemCount() {
        return expenseModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView note, category, amount, date;

        public MyViewHolder(View itemView) {
            super(itemView);
            note = itemView.findViewById(R.id.note);
            category = itemView.findViewById(R.id.category);
            amount = itemView.findViewById(R.id.amount);
            date = itemView.findViewById(R.id.date);
        }


    }


    private void showDeleteConfirmationDialog(ExpenseModel expenseModel, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteItem(expenseModel, position);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }


    private void deleteItem(ExpenseModel expenseModel, int position) {
        expenseModelList.remove(position);
        notifyDataSetChanged();

        FirebaseFirestore.getInstance()
                .collection("expenses")
                .document(expenseModel.getExpenseId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show();
                        expenseModelList.add(position, expenseModel);
                        notifyDataSetChanged();
                    }
                });


    }

}
