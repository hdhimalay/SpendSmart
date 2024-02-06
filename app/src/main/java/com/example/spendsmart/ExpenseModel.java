package com.example.spendsmart;

import java.io.Serializable;

public class ExpenseModel implements Serializable {
    private String expenseId;

    private long amount;
    private long time;
    private String type;
    private String notes;
    private String category;
    private String  uid;


    public ExpenseModel() {

    }

    public ExpenseModel(String expenseId, long amount, long time, String type, String notes, String category, String uid) {
        this.expenseId=expenseId;
        this.amount = amount;
        this.time = time;
        this.type = type;
        this.notes = notes;
        this.category = category;
        this.uid=uid;
    }



    public long getAmount() {
        return amount;
    }
    public String getExpenseId() {
        return expenseId;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
    public void setExpenseId(String expenseId) {
        this.expenseId = expenseId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}

