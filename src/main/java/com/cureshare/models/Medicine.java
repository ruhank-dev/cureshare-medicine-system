package com.cureshare.models;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Medicine {
    public enum Status { PENDING, APPROVED, REJECTED, DISPOSED }

    private String id, name, category, batchNumber, source, donorId, storageLocation, notes, donorName;
    private LocalDate expiryDate, submittedDate;
    private int quantity;
    private Status status;
    private boolean coldStorage;
    private double price;
    private String condition;

    public Medicine() { this.submittedDate = LocalDate.now(); this.status = Status.PENDING; }
    public Medicine(String id, String name, String category, String batch,
                    LocalDate expiry, int qty, String source) {
        this(); this.id=id; this.name=name; this.category=category;
        this.batchNumber=batch; this.expiryDate=expiry; this.quantity=qty; this.source=source;
    }

    public long daysUntilExpiry()   { if(expiryDate==null) return 999; return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate); }
    public boolean isExpiringSoon() { return daysUntilExpiry() >= 0 && daysUntilExpiry() <= 30; }
    public boolean isExpired()      { return daysUntilExpiry() < 0; }
    public String  getStatusLabel() { return status.name(); }

    public String    getId()              { return id; }
    public String    getName()            { return name; }
    public String    getCategory()        { return category; }
    public String    getBatchNumber()     { return batchNumber; }
    public LocalDate getExpiryDate()      { return expiryDate; }
    public int       getQuantity()        { return quantity; }
    public String    getSource()          { return source; }
    public String    getDonorId()         { return donorId; }
    public Status    getStatus()          { return status; }
    public String    getStorageLocation() { return storageLocation; }
    public boolean   isColdStorage()      { return coldStorage; }
    public String    getNotes()           { return notes; }
    public LocalDate getSubmittedDate()   { return submittedDate; }
    public double    getPrice()           { return price; }
    public String    getCondition()       { return condition; }
    public String    getDonorName()       { return donorName; }

    public void setId(String v)              { id=v; }
    public void setName(String v)            { name=v; }
    public void setCategory(String v)        { category=v; }
    public void setBatchNumber(String v)     { batchNumber=v; }
    public void setExpiryDate(LocalDate v)   { expiryDate=v; }
    public void setQuantity(int v)           { quantity=v; }
    public void setSource(String v)          { source=v; }
    public void setDonorId(String v)         { donorId=v; }
    public void setStatus(Status v)          { status=v; }
    public void setStorageLocation(String v) { storageLocation=v; }
    public void setColdStorage(boolean v)    { coldStorage=v; }
    public void setNotes(String v)           { notes=v; }
    public void setSubmittedDate(LocalDate v){ submittedDate=v; }
    public void setPrice(double v)           { price=v; }
    public void setCondition(String v)       { condition=v; }
    public void setDonorName(String v)       { donorName=v; }
}
