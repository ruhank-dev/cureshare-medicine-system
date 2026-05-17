package com.cureshare.models;

import java.time.LocalDate;

public class Transaction {
    public enum Type { REVENUE, COST, DONATION }

    private String id, description, referenceId, status, notes;
    private Type type;
    private double amount;
    private LocalDate date;

    public Transaction() { this.date = LocalDate.now(); this.status = "Completed"; }
    public Transaction(String id, String description, Type type, double amount, LocalDate date, String status) {
        this(); this.id=id; this.description=description; this.type=type; this.amount=amount; this.date=date; this.status=status;
    }

    public String      getId()          { return id; }
    public String      getDescription() { return description; }
    public Type        getType()        { return type; }
    public double      getAmount()      { return amount; }
    public LocalDate   getDate()        { return date; }
    public String      getStatus()      { return status; }
    public String      getReferenceId() { return referenceId; }
    public String      getNotes()       { return notes; }

    public void setId(String v)          { id=v; }
    public void setDescription(String v) { description=v; }
    public void setType(Type v)          { type=v; }
    public void setAmount(double v)      { amount=v; }
    public void setDate(LocalDate v)     { date=v; }
    public void setStatus(String v)      { status=v; }
    public void setReferenceId(String v) { referenceId=v; }
    public void setNotes(String v)       { notes=v; }

    public String getTypeLabel() {
        return switch(type) { case REVENUE->"Revenue"; case COST->"Cost"; default->"Donation"; };
    }
    public String getAmountFormatted() {
        return switch(type) {
            case REVENUE -> String.format("+ ₨ %,.0f", amount);
            case COST    -> String.format("- ₨ %,.0f", amount);
            default      -> String.format("₨ %,.0f", amount);
        };
    }
}
