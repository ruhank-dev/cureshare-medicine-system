package com.cureshare.models;

import java.time.LocalDate;

public class CharityRequest {
    public enum Status { PENDING, APPROVED, REJECTED, DISPATCHED, FULFILLED }

    private String id, charityId, charityName, medicineCategory, notes, requiredBy;
    private int quantityRequested, quantityFulfilled;
    private Status status;
    private LocalDate requestDate;
    private String medicineId, assignedMedicineName;
    private String urgency;

    public CharityRequest() { this.status = Status.PENDING; this.requestDate = LocalDate.now(); }
    public CharityRequest(String id, String charityId, String charityName, String category, int qty) {
        this(); this.id=id; this.charityId=charityId; this.charityName=charityName;
        this.medicineCategory=category; this.quantityRequested=qty;
    }

    public String  getId()                  { return id; }
    public String  getCharityId()           { return charityId; }
    public String  getCharityName()         { return charityName; }
    public String  getMedicineCategory()    { return medicineCategory; }
    public String  getNotes()               { return notes; }
    public String  getRequiredBy()          { return requiredBy; }
    public int     getQuantityRequested()   { return quantityRequested; }
    public int     getQuantityFulfilled()   { return quantityFulfilled; }
    public Status  getStatus()              { return status; }
    public LocalDate getRequestDate()       { return requestDate; }
    public String  getMedicineId()          { return medicineId; }
    public String  getAssignedMedicineName(){ return assignedMedicineName; }
    public String  getUrgency()             { return urgency; }

    public void setId(String v)                    { id=v; }
    public void setCharityId(String v)             { charityId=v; }
    public void setCharityName(String v)           { charityName=v; }
    public void setMedicineCategory(String v)      { medicineCategory=v; }
    public void setNotes(String v)                 { notes=v; }
    public void setRequiredBy(String v)            { requiredBy=v; }
    public void setQuantityRequested(int v)        { quantityRequested=v; }
    public void setQuantityFulfilled(int v)        { quantityFulfilled=v; }
    public void setStatus(Status v)                { status=v; }
    public void setRequestDate(LocalDate v)        { requestDate=v; }
    public void setMedicineId(String v)            { medicineId=v; }
    public void setAssignedMedicineName(String v)  { assignedMedicineName=v; }
    public void setUrgency(String v)               { urgency=v; }

    public String getStatusLabel() { return status.name().replace("_"," "); }
    public String getStatusBadge() {
        return switch(status){case APPROVED,FULFILLED->"success";case DISPATCHED->"info";case REJECTED->"danger";default->"warning";};
    }
    public String getQuantityLabel() { return quantityRequested+" units"; }
}
