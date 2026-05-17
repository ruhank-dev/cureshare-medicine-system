package com.cureshare.models;

public class User {
    private String id, name, email, password, role, organization, phone, status, city, address;
    private int points;
    private String joinDate;

    public User() { this.status = "active"; this.joinDate = java.time.LocalDate.now().toString(); }
    public User(String id, String name, String email, String password, String role) {
        this(); this.id=id; this.name=name; this.email=email; this.password=password; this.role=role;
    }

    public String getId()           { return id; }
    public String getName()         { return name; }
    public String getEmail()        { return email; }
    public String getPassword()     { return password; }
    public String getRole()         { return role; }
    public String getOrganization() { return organization; }
    public String getPhone()        { return phone; }
    public int    getPoints()       { return points; }
    public String getStatus()       { return status; }
    public String getCity()         { return city; }
    public String getAddress()      { return address; }
    public String getJoinDate()     { return joinDate; }

    public void setId(String v)           { id=v; }
    public void setName(String v)         { name=v; }
    public void setEmail(String v)        { email=v; }
    public void setPassword(String v)     { password=v; }
    public void setRole(String v)         { role=v; }
    public void setOrganization(String v) { organization=v; }
    public void setPhone(String v)        { phone=v; }
    public void setPoints(int v)          { points=v; }
    public void setStatus(String v)       { status=v; }
    public void setCity(String v)         { city=v; }
    public void setAddress(String v)      { address=v; }
    public void setJoinDate(String v)     { joinDate=v; }

    public String getInitials() {
        if (name==null||name.isBlank()) return "?";
        String[] p = name.trim().split("\\s+");
        return p.length==1
            ? String.valueOf(p[0].charAt(0)).toUpperCase()
            : (String.valueOf(p[0].charAt(0))+String.valueOf(p[p.length-1].charAt(0))).toUpperCase();
    }
    public String getRoleLabel() {
        return switch(role==null?"":role) {
            case "admin"     -> "Admin / Manager";
            case "household" -> "Household User";
            case "pharmacy"  -> "Pharmacy / Hospital";
            case "charity"   -> "Charity / NGO";
            default -> role!=null?role:"User";
        };
    }
}
