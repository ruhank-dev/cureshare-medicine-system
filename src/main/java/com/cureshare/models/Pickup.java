package com.cureshare.models;

import java.time.LocalDate;

public class Pickup {
    public enum Status { SCHEDULED, EN_ROUTE, DONE, PENDING, CANCELLED }

    private String id, donorId, donorName, address, timeSlot, rider, notes, routeId, city;
    private LocalDate date;
    private int estimatedItems, actualItems;
    private Status status;

    public Pickup() { this.status = Status.PENDING; this.date = LocalDate.now(); }
    public Pickup(String id, String donorId, String donorName, String address, LocalDate date, String timeSlot, int estItems) {
        this(); this.id=id; this.donorId=donorId; this.donorName=donorName;
        this.address=address; this.date=date; this.timeSlot=timeSlot; this.estimatedItems=estItems;
    }

    public String getId()             { return id; }
    public String getDonorId()        { return donorId; }
    public String getDonorName()      { return donorName; }
    public String getAddress()        { return address; }
    public LocalDate getDate()        { return date; }
    public String getTimeSlot()       { return timeSlot; }
    public String getRider()          { return rider; }
    public String getNotes()          { return notes; }
    public String getRouteId()        { return routeId; }
    public String getCity()           { return city; }
    public int getEstimatedItems()    { return estimatedItems; }
    public int getActualItems()       { return actualItems; }
    public Status getStatus()         { return status; }

    public void setId(String v)          { id=v; }
    public void setDonorId(String v)     { donorId=v; }
    public void setDonorName(String v)   { donorName=v; }
    public void setAddress(String v)     { address=v; }
    public void setDate(LocalDate v)     { date=v; }
    public void setTimeSlot(String v)    { timeSlot=v; }
    public void setRider(String v)       { rider=v; }
    public void setNotes(String v)       { notes=v; }
    public void setRouteId(String v)     { routeId=v; }
    public void setCity(String v)        { city=v; }
    public void setEstimatedItems(int v) { estimatedItems=v; }
    public void setActualItems(int v)    { actualItems=v; }
    public void setStatus(Status v)      { status=v; }

    public String getStatusLabel() { return status.name().replace("_"," "); }
    public String getStatusBadge() {
        return switch(status){case DONE->"success";case EN_ROUTE->"info";case SCHEDULED->"warning";case CANCELLED->"danger";default->"pending";};
    }
    public String getItemsLabel() { return estimatedItems + " items"; }
}
