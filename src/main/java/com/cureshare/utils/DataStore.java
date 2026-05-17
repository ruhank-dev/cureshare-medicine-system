package com.cureshare.utils;

import com.cureshare.models.*;
import com.cureshare.models.Transaction.Type;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class DataStore {
    private static DataStore instance;

    // Database delegates
    private MySQLDataStore   mysql;
    private SQLiteDataStore  sqlite;

    // In-memory lists (used in demo mode only)
    private final ObservableList<User>           users        = FXCollections.observableArrayList();
    private final ObservableList<Medicine>       medicines    = FXCollections.observableArrayList();
    private final ObservableList<Transaction>    transactions = FXCollections.observableArrayList();
    private final ObservableList<Pickup>         pickups      = FXCollections.observableArrayList();
    private final ObservableList<CharityRequest> charReqs     = FXCollections.observableArrayList();

    private boolean fifoEnabled = true;

    private DataStore() {
        if (DatabaseConfig.USE_SQLITE) {
            sqlite = SQLiteDataStore.getInstance();
            if (!sqlite.isConnected()) {
                System.err.println("[CureShare] SQLite failed — using demo data.");
                sqlite = null;
                seedAll();
            } else if (sqlite.getAllUsers().isEmpty()) {
                System.out.println("[CureShare] First run — seeding demo data into SQLite...");
                seedAll();
                for (User u : users)               sqlite.registerUserHashed(u);
                for (Medicine m : medicines)       sqlite.addMedicineRaw(m);
                for (Transaction t : transactions) sqlite.addTransactionRaw(t);
                for (Pickup p : pickups)           sqlite.addPickupRaw(p);
                for (CharityRequest r : charReqs)  sqlite.addCharityRequestRaw(r);
                sqlite.reload();
                users.clear(); medicines.clear(); transactions.clear(); pickups.clear(); charReqs.clear();
                System.out.println("[CureShare] Seeding complete!");
            } else {
                users.clear(); medicines.clear(); transactions.clear(); pickups.clear(); charReqs.clear();
                System.out.println("[CureShare] SQLite loaded successfully.");
            }

        } else if (DatabaseConfig.USE_MYSQL) {
            mysql = MySQLDataStore.getInstance();
            if (!mysql.isConnected()) {
                System.err.println("[CureShare] MySQL unavailable — using demo data.");
                mysql = null;
                seedAll();
            } else {
                if (mysql.getAllUsers().isEmpty()) {
                    seedAll();
                    for (User u : users)               mysql.registerUserHashed(u);
                    for (Medicine m : medicines)       mysql.addMedicineRaw(m);
                    for (Transaction t : transactions) mysql.addTransactionRaw(t);
                    for (Pickup p : pickups)           mysql.addPickupRaw(p);
                    for (CharityRequest r : charReqs)  mysql.addCharityRequestRaw(r);
                    mysql.reload();
                }
                users.clear(); medicines.clear(); transactions.clear(); pickups.clear(); charReqs.clear();
            }

        } else {
            seedAll(); // demo mode — no database
        }
    }

    public static DataStore getInstance() {
        if (instance == null) instance = new DataStore();
        return instance;
    }

    public boolean isUsingDatabase() { return sqlite != null || mysql != null; }

    public String getDatabaseMode() {
        if (sqlite != null) return "SQLite";
        if (mysql  != null) return "MySQL";
        return "Demo (in-memory)";
    }

    // ── helper: which delegate to use ────────────────────────────────────
    // These two private helpers make every method below one clean line.
    private boolean hasSQLite() { return sqlite != null; }
    private boolean hasMySQL()  { return mysql  != null; }

    // ════════════════════════════════════════════════════════════
    //  AUTH
    // ════════════════════════════════════════════════════════════
    public User authenticate(String email, String password) {
        if (hasSQLite()) return sqlite.authenticate(email, password);
        if (hasMySQL())  return mysql.authenticate(email, password);
        return users.stream()
            .filter(u -> u.getEmail().equalsIgnoreCase(email)
                && PasswordUtil.verify(password, u.getPassword()))
            .findFirst().orElse(null);
    }

    public boolean emailExists(String email) {
        if (hasSQLite()) return sqlite.emailExists(email);
        if (hasMySQL())  return mysql.emailExists(email);
        return users.stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
    }

    public void registerUser(User user) {
        if (hasSQLite()) { sqlite.registerUser(user); return; }
        if (hasMySQL())  { mysql.registerUser(user);  return; }
        user.setId("U" + String.format("%03d", users.size() + 1));
        user.setPassword(PasswordUtil.hash(user.getPassword()));
        users.add(user);
        audit().log(cu(), cn(), "REGISTER_USER",
            "New user registered: " + user.getName() + " (" + user.getRole() + ")", "SYSTEM");
    }

    private AuditLog audit() { return AuditLog.getInstance(); }
    private String cu() {
        var s = SessionManager.getInstance();
        return s.isLoggedIn() ? s.getCurrentUser().getId() : "SYSTEM";
    }
    private String cn() {
        var s = SessionManager.getInstance();
        return s.isLoggedIn() ? s.getCurrentUser().getName() : "System";
    }

    // ════════════════════════════════════════════════════════════
    //  RATINGS
    // ════════════════════════════════════════════════════════════
    public static class Rating {
        public final String fromId, fromName, targetId, targetName, category;
        public final int stars;
        public final String comment;
        public final LocalDate date;
        public Rating(String fromId, String fromName, String targetId, String targetName,
                      String category, int stars, String comment) {
            this.fromId = fromId; this.fromName = fromName;
            this.targetId = targetId; this.targetName = targetName;
            this.category = category;
            this.stars = Math.max(1, Math.min(5, stars));
            this.comment = comment;
            this.date = LocalDate.now();
        }
        public String getStarDisplay() { return "★".repeat(stars) + "☆".repeat(5 - stars); }
    }

    private final List<Rating> ratings = new ArrayList<>();

    public void addRating(Rating r) {
        if (hasSQLite()) { sqlite.addRating(r); return; }
        if (hasMySQL())  { mysql.addRating(r);  return; }
        ratings.add(r);
        audit().log(cu(), cn(), "RATING_SUBMITTED",
            (r.fromName != null ? r.fromName : "Unknown") + " rated " + r.targetName
                + " " + r.stars + "/5 — " + r.category, "SYSTEM");
    }

    public List<Rating> getAllRatings() {
        if (hasSQLite()) return sqlite.getAllRatings();
        if (hasMySQL())  return mysql.getAllRatings();
        return new ArrayList<>(ratings);
    }

    public double getAvgRating(String targetId) {
        if (hasSQLite()) return sqlite.getAvgRating(targetId);
        if (hasMySQL())  return mysql.getAvgRating(targetId);
        return ratings.stream().filter(r -> targetId.equals(r.targetId))
            .mapToInt(r -> r.stars).average().orElse(0.0);
    }

    public double getOverallAvgRating() {
        if (hasSQLite()) return sqlite.getOverallAvgRating();
        if (hasMySQL())  return mysql.getOverallAvgRating();
        return ratings.stream().mapToInt(r -> r.stars).average().orElse(4.7);
    }

    // ════════════════════════════════════════════════════════════
    //  SETTINGS / FIFO
    // ════════════════════════════════════════════════════════════
    public boolean isFifoEnabled() {
        if (hasSQLite()) return sqlite.isFifoEnabled();
        if (hasMySQL())  return mysql.isFifoEnabled();
        return fifoEnabled;
    }

    public void setFifoEnabled(boolean v) {
        fifoEnabled = v;
        if (hasSQLite()) sqlite.setFifoEnabled(v);
        if (hasMySQL())  mysql.setFifoEnabled(v);
    }

    public boolean getSetting(String key, boolean defaultVal) {
        if (hasSQLite()) return sqlite.getSetting(key, defaultVal);
        if (hasMySQL())  return mysql.getSetting(key, defaultVal);
        return defaultVal;
    }

    public void setSetting(String key, boolean value) {
        if (hasSQLite()) sqlite.setSetting(key, value);
        if (hasMySQL())  mysql.setSetting(key, value);
        if ("fifo_enabled".equals(key)) fifoEnabled = value;
    }

    public Medicine getNextFifo(String category) {
        if (hasSQLite()) return sqlite.getNextFifo(category);
        if (hasMySQL())  return mysql.getNextFifo(category);
        var stream = getApproved().stream().filter(m -> m.getQuantity() > 0);
        if (category != null && !category.isBlank())
            stream = stream.filter(m -> category.equalsIgnoreCase(m.getCategory()));
        return stream.sorted(Comparator.comparing(
            m -> m.getSubmittedDate() != null ? m.getSubmittedDate() : LocalDate.MIN))
            .findFirst().orElse(null);
    }

    public double calculatePickupCost(Pickup p) {
        if (hasSQLite()) return sqlite.calculatePickupCost(p);
        if (hasMySQL())  return mysql.calculatePickupCost(p);
        if (p == null) return 2800.0;
        double base = 1500.0;
        double perItem = p.getEstimatedItems() * 20.0;
        double citySurcharge = switch (p.getCity() != null ? p.getCity() : "") {
            case "Lahore", "Karachi" -> 800.0;
            case "Rawalpindi"        -> 300.0;
            case "Faisalabad"        -> 600.0;
            default                  -> 0.0;
        };
        return base + perItem + citySurcharge;
    }

    public double getTotalLogisticsCosts() {
        if (hasSQLite()) return sqlite.getTotalLogisticsCosts();
        if (hasMySQL())  return mysql.getTotalLogisticsCosts();
        return getAllPickups().stream()
            .filter(p -> p.getStatus() == Pickup.Status.DONE)
            .mapToDouble(this::calculatePickupCost)
            .sum();
    }

    // ════════════════════════════════════════════════════════════
    //  USERS
    // ════════════════════════════════════════════════════════════
    public ObservableList<User> getAllUsers() {
        if (hasSQLite()) return sqlite.getAllUsers();
        if (hasMySQL())  return mysql.getAllUsers();
        return users;
    }

    public List<User> getUsersByRole(String role) {
        if (hasSQLite()) return sqlite.getUsersByRole(role);
        if (hasMySQL())  return mysql.getUsersByRole(role);
        return users.stream()
            .filter(u -> role.equalsIgnoreCase(u.getRole()))
            .collect(Collectors.toList());
    }

    public User getUserById(String id) {
        if (hasSQLite()) return sqlite.getUserById(id);
        if (hasMySQL())  return mysql.getUserById(id);
        return users.stream().filter(u -> id.equals(u.getId())).findFirst().orElse(null);
    }

    public void updateUser(User u) {
        if (hasSQLite()) { sqlite.updateUser(u); return; }
        if (hasMySQL())  { mysql.updateUser(u);  return; }
        // in-memory: object already updated by reference
    }

    public void updatePassword(String id, String newPlain) {
        if (hasSQLite()) { sqlite.updatePassword(id, newPlain); return; }
        if (hasMySQL())  { mysql.updatePassword(id, newPlain);  return; }
        users.stream().filter(u -> id.equals(u.getId())).findFirst()
            .ifPresent(u -> u.setPassword(PasswordUtil.hash(newPlain)));
    }

    public void deleteUser(String id) {
        if (hasSQLite()) { sqlite.deleteUser(id); return; }
        if (hasMySQL())  { mysql.deleteUser(id);  return; }
        users.stream().filter(u -> id.equals(u.getId())).findFirst()
            .ifPresent(u -> audit().log(cu(), cn(), "DELETE_USER",
                "User deleted: " + u.getName(), "SYSTEM"));
        users.removeIf(u -> id.equals(u.getId()));
    }

    // ════════════════════════════════════════════════════════════
    //  MEDICINES
    // ════════════════════════════════════════════════════════════
    public ObservableList<Medicine> getAllMedicines() {
        if (hasSQLite()) return sqlite.getAllMedicines();
        if (hasMySQL())  return mysql.getAllMedicines();
        return medicines;
    }

    public List<Medicine> getPending() {
        if (hasSQLite()) return sqlite.getPending();
        if (hasMySQL())  return mysql.getPending();
        return filterM(Medicine.Status.PENDING);
    }

    public List<Medicine> getApproved() {
        if (hasSQLite()) return sqlite.getApproved();
        if (hasMySQL())  return mysql.getApproved();
        return filterM(Medicine.Status.APPROVED);
    }

    public List<Medicine> getRejected() {
        if (hasSQLite()) return sqlite.getRejected();
        if (hasMySQL())  return mysql.getRejected();
        return filterM(Medicine.Status.REJECTED);
    }

    private List<Medicine> filterM(Medicine.Status s) {
        return medicines.stream().filter(m -> m.getStatus() == s).collect(Collectors.toList());
    }

    public List<Medicine> getExpiringSoon() {
        if (hasSQLite()) return sqlite.getExpiringSoon();
        if (hasMySQL())  return mysql.getExpiringSoon();
        return medicines.stream().filter(Medicine::isExpiringSoon).collect(Collectors.toList());
    }

    public List<Medicine> getLowStock(int threshold) {
        if (hasSQLite()) return sqlite.getLowStock(threshold);
        if (hasMySQL())  return mysql.getLowStock(threshold);
        return medicines.stream()
            .filter(m -> m.getQuantity() < threshold && m.getStatus() == Medicine.Status.APPROVED)
            .collect(Collectors.toList());
    }

    public List<Medicine> getByDonor(String donorId) {
        if (hasSQLite()) return sqlite.getByDonor(donorId);
        if (hasMySQL())  return mysql.getByDonor(donorId);
        return medicines.stream().filter(m -> donorId.equals(m.getDonorId())).collect(Collectors.toList());
    }

    public List<Medicine> searchMedicines(String query) {
        if (hasSQLite()) return sqlite.searchMedicines(query);
        if (hasMySQL())  return mysql.searchMedicines(query);
        String q = query.toLowerCase();
        return getAllMedicines().stream().filter(m ->
            (m.getName() != null && m.getName().toLowerCase().contains(q)) ||
            (m.getCategory() != null && m.getCategory().toLowerCase().contains(q)) ||
            (m.getBatchNumber() != null && m.getBatchNumber().toLowerCase().contains(q))
        ).collect(Collectors.toList());
    }

    public void addMedicine(Medicine m) {
        if (hasSQLite()) { sqlite.addMedicine(m); return; }
        if (hasMySQL())  { mysql.addMedicine(m);  return; }
        m.setId("M" + String.format("%03d", medicines.size() + 1));
        medicines.add(m);
        audit().log(cu(), cn(), "ADD_MEDICINE",
            "Medicine submitted: " + m.getName() + " (" + m.getId() + ")", "MEDICINE");
    }

    public void approveMedicine(String id) {
        if (hasSQLite()) { sqlite.approveMedicine(id); return; }
        if (hasMySQL())  { mysql.approveMedicine(id);  return; }
        medicines.stream().filter(m -> id.equals(m.getId())).findFirst().ifPresent(m -> {
            m.setStatus(Medicine.Status.APPROVED);
            addTransaction(new Transaction(nextTxId(), "Medicine Approved: " + m.getName(),
                Type.REVENUE, m.getQuantity() * 120.0, LocalDate.now(), "Completed"));
            audit().log(cu(), cn(), "APPROVE_MEDICINE",
                "Approved: " + m.getName() + " (" + id + ") qty=" + m.getQuantity(), "MEDICINE");
        });
    }

    public void rejectMedicine(String id) {
        if (hasSQLite()) { sqlite.rejectMedicine(id); return; }
        if (hasMySQL())  { mysql.rejectMedicine(id);  return; }
        medicines.stream().filter(m -> id.equals(m.getId())).findFirst().ifPresent(m -> {
            m.setStatus(Medicine.Status.REJECTED);
            audit().log(cu(), cn(), "REJECT_MEDICINE",
                "Rejected: " + m.getName() + " (" + id + ")", "MEDICINE");
        });
    }

    public void deleteMedicine(String id) {
        if (hasSQLite()) { sqlite.deleteMedicine(id); return; }
        if (hasMySQL())  { mysql.deleteMedicine(id);  return; }
        medicines.stream().filter(m -> id.equals(m.getId())).findFirst()
            .ifPresent(m -> audit().log(cu(), cn(), "DELETE_MEDICINE",
                "Deleted: " + m.getName() + " (" + id + ")", "MEDICINE"));
        medicines.removeIf(m -> id.equals(m.getId()));
    }

    public void updateMedicineQuantity(String id, int qty) {
        if (hasSQLite()) { sqlite.updateMedicineQuantity(id, qty); return; }
        if (hasMySQL())  { mysql.updateMedicineQuantity(id, qty);  return; }
        medicines.stream().filter(m -> id.equals(m.getId())).findFirst()
            .ifPresent(m -> m.setQuantity(qty));
    }

    public Medicine getMedicineById(String id) {
        if (hasSQLite()) return sqlite.getMedicineById(id);
        if (hasMySQL())  return mysql.getMedicineById(id);
        return medicines.stream().filter(m -> id.equals(m.getId())).findFirst().orElse(null);
    }

    // ════════════════════════════════════════════════════════════
    //  TRANSACTIONS
    // ════════════════════════════════════════════════════════════
    public ObservableList<Transaction> getAllTransactions() {
        if (hasSQLite()) return sqlite.getAllTransactions();
        if (hasMySQL())  return mysql.getAllTransactions();
        return transactions;
    }

    public void addTransaction(Transaction t) {
        if (hasSQLite()) { sqlite.addTransaction(t); return; }
        if (hasMySQL())  { mysql.addTransaction(t);  return; }
        if (t.getId() == null) t.setId(nextTxId());
        transactions.add(0, t);
    }

    public double getTotalRevenue() {
        return getAllTransactions().stream()
            .filter(t -> t.getType() == Type.REVENUE)
            .mapToDouble(Transaction::getAmount).sum();
    }

    public double getTotalCosts() {
        return getAllTransactions().stream()
            .filter(t -> t.getType() == Type.COST)
            .mapToDouble(Transaction::getAmount).sum();
    }

    public double getNetProfit() { return getTotalRevenue() - getTotalCosts(); }

    public List<Transaction> getRecentTransactions(int limit) {
        return getAllTransactions().stream().limit(limit).collect(Collectors.toList());
    }

    private String nextTxId() {
        return "#T" + String.format("%04d", transactions.size() + 1);
    }

    // ════════════════════════════════════════════════════════════
    //  PICKUPS
    // ════════════════════════════════════════════════════════════
    public ObservableList<Pickup> getAllPickups() {
        if (hasSQLite()) return sqlite.getAllPickups();
        if (hasMySQL())  return mysql.getAllPickups();
        return pickups;
    }

    public List<Pickup> getTodayPickups() {
        if (hasSQLite()) return sqlite.getTodayPickups();
        if (hasMySQL())  return mysql.getTodayPickups();
        return pickups.stream()
            .filter(p -> LocalDate.now().equals(p.getDate()))
            .collect(Collectors.toList());
    }

    public List<Pickup> getPickupsByDonor(String donorId) {
        if (hasSQLite()) return sqlite.getPickupsByDonor(donorId);
        if (hasMySQL())  return mysql.getPickupsByDonor(donorId);
        return pickups.stream().filter(p -> donorId.equals(p.getDonorId())).collect(Collectors.toList());
    }

    public void addPickup(Pickup p) {
        if (hasSQLite()) { sqlite.addPickup(p); return; }
        if (hasMySQL())  { mysql.addPickup(p);  return; }
        p.setId("P" + String.format("%03d", pickups.size() + 1));
        pickups.add(p);
        audit().log(cu(), cn(), "SCHEDULE_PICKUP",
            "Pickup scheduled for " + p.getDonorName() + " on " + p.getDate(), "PICKUP");
    }

    public void completePickup(String id) {
        if (hasSQLite()) { sqlite.completePickup(id); return; }
        if (hasMySQL())  { mysql.completePickup(id);  return; }
        pickups.stream().filter(p -> id.equals(p.getId())).findFirst().ifPresent(p -> {
            p.setStatus(Pickup.Status.DONE);
            double cost = calculatePickupCost(p);
            addTransaction(new Transaction(null,
                "Pickup completed: " + p.getDonorName() +
                " (" + p.getEstimatedItems() + " items, " + p.getCity() + ")",
                Type.COST, cost, LocalDate.now(), "Paid"));
            audit().log(cu(), cn(), "COMPLETE_PICKUP",
                "Pickup " + id + " completed for " + p.getDonorName() +
                " — cost ₨" + String.format("%.0f", cost), "PICKUP");
        });
    }

    public void cancelPickup(String id) {
        if (hasSQLite()) { sqlite.cancelPickup(id); return; }
        if (hasMySQL())  { mysql.cancelPickup(id);  return; }
        pickups.stream().filter(p -> id.equals(p.getId())).findFirst().ifPresent(p -> {
            p.setStatus(Pickup.Status.CANCELLED);
            audit().log(cu(), cn(), "CANCEL_PICKUP",
                "Pickup " + id + " cancelled for " + p.getDonorName(), "PICKUP");
        });
    }

    public void updatePickupStatus(String id, Pickup.Status s) {
        if (hasSQLite()) { sqlite.updatePickupStatus(id, s); return; }
        if (hasMySQL())  { mysql.updatePickupStatus(id, s);  return; }
        pickups.stream().filter(p -> id.equals(p.getId())).findFirst()
            .ifPresent(p -> p.setStatus(s));
    }

    public Pickup getPickupById(String id) {
        if (hasSQLite()) return sqlite.getPickupById(id);
        if (hasMySQL())  return mysql.getPickupById(id);
        return pickups.stream().filter(p -> id.equals(p.getId())).findFirst().orElse(null);
    }

    // ════════════════════════════════════════════════════════════
    //  CHARITY REQUESTS
    // ════════════════════════════════════════════════════════════
    public ObservableList<CharityRequest> getAllCharityRequests() {
        if (hasSQLite()) return sqlite.getAllCharityRequests();
        if (hasMySQL())  return mysql.getAllCharityRequests();
        return charReqs;
    }

    public List<CharityRequest> getCharityRequestsByCharity(String charityId) {
        if (hasSQLite()) return sqlite.getCharityRequestsByCharity(charityId);
        if (hasMySQL())  return mysql.getCharityRequestsByCharity(charityId);
        return charReqs.stream()
            .filter(r -> charityId.equals(r.getCharityId()))
            .collect(Collectors.toList());
    }

    public List<CharityRequest> getPendingCharityRequests() {
        if (hasSQLite()) return sqlite.getPendingCharityRequests();
        if (hasMySQL())  return mysql.getPendingCharityRequests();
        return charReqs.stream()
            .filter(r -> r.getStatus() == CharityRequest.Status.PENDING)
            .collect(Collectors.toList());
    }

    public void addCharityRequest(CharityRequest r) {
        if (hasSQLite()) { sqlite.addCharityRequest(r); return; }
        if (hasMySQL())  { mysql.addCharityRequest(r);  return; }
        r.setId("REQ" + String.format("%04d", charReqs.size() + 1));
        charReqs.add(r);
        audit().log(cu(), cn(), "CHARITY_REQUEST",
            "Request submitted by " + r.getCharityName() +
            " for " + r.getMedicineCategory() +
            " (" + r.getQuantityRequested() + " units)", "CHARITY");
    }

    public void approveCharityRequest(String id) {
        if (hasSQLite()) { sqlite.approveCharityRequest(id); return; }
        if (hasMySQL())  { mysql.approveCharityRequest(id);  return; }
        charReqs.stream().filter(r -> id.equals(r.getId())).findFirst().ifPresent(r -> {
            r.setStatus(CharityRequest.Status.APPROVED);
            addTransaction(new Transaction(null, "Charity allocation: " + r.getCharityName(),
                Type.DONATION, 0, LocalDate.now(), "Approved"));
            audit().log(cu(), cn(), "APPROVE_CHARITY",
                "Request " + id + " approved for " + r.getCharityName(), "CHARITY");
        });
    }

    public void rejectCharityRequest(String id) {
        if (hasSQLite()) { sqlite.rejectCharityRequest(id); return; }
        if (hasMySQL())  { mysql.rejectCharityRequest(id);  return; }
        charReqs.stream().filter(r -> id.equals(r.getId())).findFirst().ifPresent(r -> {
            r.setStatus(CharityRequest.Status.REJECTED);
            audit().log(cu(), cn(), "REJECT_CHARITY",
                "Request " + id + " rejected for " + r.getCharityName(), "CHARITY");
        });
    }

    public void dispatchCharityRequest(String id) {
        if (hasSQLite()) { sqlite.dispatchCharityRequest(id); return; }
        if (hasMySQL())  { mysql.dispatchCharityRequest(id);  return; }
        charReqs.stream().filter(r -> id.equals(r.getId())).findFirst().ifPresent(r -> {
            r.setStatus(CharityRequest.Status.DISPATCHED);
            audit().log(cu(), cn(), "DISPATCH_CHARITY",
                "Request " + id + " dispatched to " + r.getCharityName(), "CHARITY");
        });
    }

    public CharityRequest getRequestById(String id) {
        if (hasSQLite()) return sqlite.getRequestById(id);
        if (hasMySQL())  return mysql.getRequestById(id);
        return charReqs.stream().filter(r -> id.equals(r.getId())).findFirst().orElse(null);
    }

    // ════════════════════════════════════════════════════════════
    //  SUMMARY STATS
    // ════════════════════════════════════════════════════════════
    public int getTotalDonors() {
        if (hasSQLite()) return sqlite.getTotalDonors();
        if (hasMySQL())  return mysql.getTotalDonors();
        return (int) users.stream()
            .filter(u -> !"admin".equalsIgnoreCase(u.getRole())).count();
    }

    public int getHouseholdCount() { return getUsersByRole("household").size(); }
    public int getPharmacyCount()  { return getUsersByRole("pharmacy").size();  }
    public int getCharityCount()   { return getUsersByRole("charity").size();   }

    // ════════════════════════════════════════════════════════════
    //  LEGACY COMPAT
    // ════════════════════════════════════════════════════════════
    public List<Pickup> getPickupsAsArray() {
        return getTodayPickups().stream().limit(6).collect(Collectors.toList());
    }

    public List<CharityRequest> getCharityRequestsAsArray() {
        return getAllCharityRequests().stream().limit(6).collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════════════════
    //  SEED DATA (runs only in demo mode OR on first SQLite/MySQL run)
    // ════════════════════════════════════════════════════════════
    private void seedAll() {
        seedUsers();
        seedMedicines();
        seedTransactions();
        seedPickups();
        seedCharityRequests();
    }

    private void seedUsers() {
        addU("U001","Admin Manager",    "admin@cureshare.pk", "admin123","admin",    "CureShare Pakistan",          "+92 300 1234567","Islamabad",  0,   "2024-01-01");
        addU("U002","Ahmad Khan",       "ahmad@gmail.com",    "pass123", "household",null,                          "+92 311 9876543","Islamabad",  340, "2024-03-15");
        addU("U003","Sara Fatima",      "sara@email.com",     "pass456", "household",null,                          "+92 321 5554321","Lahore",     180, "2024-04-20");
        addU("U004","Raza Ahmed",       "raza@hotmail.com",   "raza789", "household",null,                          "+92 333 2223344","Islamabad",  90,  "2024-05-10");
        addU("U005","Faiza Malik",      "faiza@gmail.com",    "faiza123","household",null,                          "+92 345 8889900","Rawalpindi", 60,  "2024-06-01");
        addU("U006","Bilal Sheikh",     "bilal@yahoo.com",    "bilal456","household",null,                          "+92 300 5556677","Islamabad",  210, "2024-07-12");
        addU("U007","Nadia Hussain",    "nadia@gmail.com",    "nadia789","household",null,                          "+92 312 3334455","Lahore",     130, "2024-08-05");
        addU("U008","MedPlus Pharmacy", "medplus@pharmacy.pk","pharm123","pharmacy", "MedPlus Pharmaceuticals",    "+92 51 4441234", "Islamabad",  2100,"2024-02-01");
        addU("U009","HealthPlus Pharma","healthplus@pharma.pk","health456","pharmacy","HealthPlus Pharma Ltd.",    "+92 42 3339988", "Lahore",     1380,"2024-02-15");
        addU("U010","CityMed Store",    "citymed@store.pk",   "city789", "pharmacy", "CityMed Medical Store",      "+92 51 5556677", "Islamabad",  820, "2024-03-01");
        addU("U011","PharmaCare Plus",  "pharmacare@pk.com",  "pharma321","pharmacy","PharmaCare Plus Faisalabad", "+92 41 4443322", "Faisalabad", 560, "2024-04-01");
        addU("U012","Hope Foundation",  "hope@ngo.pk",        "hope123", "charity",  "Hope Foundation Pakistan",   "+92 42 3335678", "Lahore",     0,   "2024-01-20");
        addU("U013","Al-Shifa Trust",   "alshifa@trust.pk",   "shifa456","charity",  "Al-Shifa Trust Medical",     "+92 51 4448899", "Rawalpindi", 0,   "2024-02-10");
        addU("U014","Edhi Foundation",  "edhi@foundation.pk", "edhi789", "charity",  "Edhi Foundation Pakistan",   "+92 21 3334455", "Karachi",    0,   "2024-01-05");
        addU("U015","Child Aid Pakistan","childaid@ngo.pk",   "child321","charity",  "Child Aid Pakistan",         "+92 51 2223344", "Islamabad",  0,   "2024-03-20");
        addU("U016","Green Crescent",   "green@crescent.pk",  "green456","charity",  "Green Crescent Rural Health","+92 55 1112233", "Multan",     0,   "2024-04-15");
    }

    private void addU(String id, String name, String email, String plainPw, String role,
                      String org, String phone, String city, int pts, String joined) {
        User u = new User(id, name, email, PasswordUtil.hash(plainPw), role);
        u.setOrganization(org); u.setPhone(phone); u.setCity(city);
        u.setPoints(pts); u.setJoinDate(joined);
        users.add(u);
    }

    private void seedMedicines() {
        LocalDate now = LocalDate.now();
        addS("M001","Amoxicillin 500mg",  "Antibiotic",    "B2041",now.plusDays(8),  120,"Pharmacy","U008",Medicine.Status.PENDING,  "Shelf A-4", false,45.0, "Sealed");
        addS("M002","Metformin 850mg",    "Diabetes",      "B2038",now.plusDays(285), 18,"Pharmacy","U008",Medicine.Status.PENDING,  "Shelf B-2", false,35.0, "Sealed");
        addS("M003","Atorvastatin 20mg",  "Cardiac",       "B2035",now.plusDays(193),200,"Pharmacy","U008",Medicine.Status.APPROVED, "Shelf C-1", false,85.0, "Sealed");
        addS("M004","Paracetamol 500mg",  "Analgesic",     "B2031",now.minusDays(5), 500,"Pharmacy","U008",Medicine.Status.REJECTED, null,        false,15.0, "Partial");
        addS("M005","Omeprazole 20mg",    "Gastro",        "B2029",now.plusDays(164), 45,"Pharmacy","U008",Medicine.Status.APPROVED, "Shelf D-3", false,55.0, "Sealed");
        addS("M006","Lisinopril 10mg",    "Hypertension",  "B2027",now.plusDays(255),150,"Pharmacy","U008",Medicine.Status.PENDING,  "Shelf A-8", false,70.0, "Sealed");
        addS("M007","Insulin Glargine",   "Diabetes",      "B2045",now.plusDays(316), 72,"Pharmacy","U008",Medicine.Status.APPROVED, "Cold Unit 1",true,380.0,"Sealed");
        addS("M008","Cetirizine 10mg",    "Antihistamine", "B2049",now.plusDays(437),300,"Pharmacy","U008",Medicine.Status.PENDING,  "Shelf E-2", false,25.0, "Sealed");
        addS("M009","Azithromycin 500mg", "Antibiotic",    "B2051",now.plusDays(498), 22,"Pharmacy","U008",Medicine.Status.APPROVED, "Shelf A-2", false,120.0,"Sealed");
        addS("M010","Amlodipine 5mg",     "Cardiac",       "B2053",now.plusDays(590),180,"Pharmacy","U008",Medicine.Status.PENDING,  "Shelf C-3", false,60.0, "Sealed");
        addS("M011","Ciprofloxacin 500mg","Antibiotic",    "B3011",now.plusDays(21),  95,"Pharmacy","U009",Medicine.Status.APPROVED, "Shelf A-5", false,90.0, "Sealed");
        addS("M012","Pantoprazole 40mg",  "Gastro",        "B3014",now.plusDays(340), 60,"Pharmacy","U009",Medicine.Status.APPROVED, "Shelf D-1", false,65.0, "Sealed");
        addS("M013","Metoprolol 50mg",    "Cardiac",       "B3016",now.plusDays(12),  40,"Pharmacy","U009",Medicine.Status.PENDING,  "Shelf C-5", false,45.0, "Partial");
        addS("M014","Diclofenac 50mg",    "Analgesic",     "B3018",now.plusDays(378),220,"Pharmacy","U009",Medicine.Status.APPROVED, "Shelf E-4", false,30.0, "Sealed");
        addS("M015","Salbutamol Inhaler", "Respiratory",   "B3020",now.plusDays(180), 35,"Pharmacy","U009",Medicine.Status.APPROVED, "Shelf F-1", false,220.0,"Sealed");
        addS("M016","Clopidogrel 75mg",   "Cardiac",       "B4001",now.plusDays(420), 85,"Pharmacy","U010",Medicine.Status.APPROVED, "Shelf C-7", false,95.0, "Sealed");
        addS("M017","Sertraline 50mg",    "Psychiatry",    "B4003",now.plusDays(265), 25,"Pharmacy","U010",Medicine.Status.PENDING,  "Shelf G-1", false,140.0,"Sealed");
        addS("M018","Levothyroxine 50mcg","Thyroid",       "B4005",now.plusDays(510),110,"Pharmacy","U010",Medicine.Status.APPROVED, "Shelf H-2", false,55.0, "Sealed");
        addS("M019","Furosemide 40mg",    "Diuretic",      "B4007",now.plusDays(15),  50,"Pharmacy","U010",Medicine.Status.PENDING,  "Shelf B-6", false,20.0, "Partial");
        addS("M020","Warfarin 5mg",       "Anticoagulant", "B4009",now.plusDays(290), 30,"Pharmacy","U010",Medicine.Status.APPROVED, "Shelf C-9", false,75.0, "Sealed");
        addS("M021","Ibuprofen 400mg",    "Analgesic",     "B5001",now.plusDays(400),350,"Pharmacy","U011",Medicine.Status.APPROVED, "Shelf E-7", false,22.0, "Sealed");
        addS("M022","Tramadol 50mg",      "Analgesic",     "B5003",now.plusDays(220), 40,"Pharmacy","U011",Medicine.Status.APPROVED, "Shelf E-9", false,80.0, "Sealed");
        addS("M023","Ondansetron 4mg",    "Gastro",        "B5005",now.plusDays(350), 65,"Pharmacy","U011",Medicine.Status.APPROVED, "Shelf D-7", false,95.0, "Sealed");
        addS("M024","Montelukast 10mg",   "Respiratory",   "B5007",now.plusDays(28),  18,"Pharmacy","U011",Medicine.Status.PENDING,  "Shelf F-3", false,130.0,"Sealed");
        addS("M025","Vitamin D3 1000IU",  "Supplement",    "B5009",now.plusDays(540),500,"Pharmacy","U011",Medicine.Status.APPROVED, "Shelf I-1", false,18.0, "Sealed");
        addS("M026","Paracetamol 1000mg", "Analgesic",     "HH001",now.plusDays(120), 24,"Household","U002",Medicine.Status.APPROVED,"Shelf E-2", false,12.0, "Partial");
        addS("M027","Cetirizine 5mg",     "Antihistamine", "HH002",now.plusDays(200), 12,"Household","U002",Medicine.Status.PENDING, null,        false,20.0, "Partial");
        addS("M028","Vitamin C 500mg",    "Supplement",    "HH003",now.plusDays(365), 60,"Household","U002",Medicine.Status.APPROVED,"Shelf I-3", false,10.0, "Sealed");
        addS("M029","ORS Sachet",         "Electrolyte",   "HH004",now.plusDays(480),100,"Household","U003",Medicine.Status.APPROVED,"Shelf J-1", false,8.0,  "Sealed");
        addS("M030","Amoxicillin 250mg",  "Antibiotic",    "HH005",now.plusDays(18),  30,"Household","U003",Medicine.Status.PENDING, null,        false,35.0, "Partial");
        addS("M031","Metformin 500mg",    "Diabetes",      "HH006",now.plusDays(240), 45,"Household","U004",Medicine.Status.APPROVED,"Shelf B-8", false,25.0, "Sealed");
        addS("M032","Ibuprofen 200mg",    "Analgesic",     "HH007",now.plusDays(90),  36,"Household","U004",Medicine.Status.PENDING, null,        false,15.0, "Partial");
        addS("M033","Multivitamin",       "Supplement",    "HH008",now.plusDays(600), 80,"Household","U005",Medicine.Status.APPROVED,"Shelf I-5", false,12.0, "Sealed");
        addS("M034","Antacid Tablets",    "Gastro",        "HH009",now.plusDays(150), 50,"Household","U006",Medicine.Status.APPROVED,"Shelf D-9", false,10.0, "Partial");
        addS("M035","Doxycycline 100mg",  "Antibiotic",    "HH010",now.plusDays(22),  20,"Household","U007",Medicine.Status.PENDING, null,        false,55.0, "Partial");

        Map<String,String> nm = Map.of(
            "U008","MedPlus Pharmacy", "U009","HealthPlus Pharma",
            "U010","CityMed Store",    "U011","PharmaCare Plus",
            "U002","Ahmad Khan",       "U003","Sara Fatima",
            "U004","Raza Ahmed",       "U005","Faiza Malik",
            "U006","Bilal Sheikh",     "U007","Nadia Hussain");
        medicines.forEach(m -> { if (nm.containsKey(m.getDonorId())) m.setDonorName(nm.get(m.getDonorId())); });
    }

    private void addS(String id, String name, String cat, String batch, LocalDate expiry,
                      int qty, String src, String did, Medicine.Status st,
                      String loc, boolean cold, double price, String cond) {
        Medicine m = new Medicine(id, name, cat, batch, expiry, qty, src);
        m.setDonorId(did); m.setStatus(st); m.setStorageLocation(loc);
        m.setColdStorage(cold); m.setPrice(price); m.setCondition(cond);
        medicines.add(m);
    }

    private void seedTransactions() {
        LocalDate now = LocalDate.now();
        addTx(new Transaction("#T0001","Bulk sale — Atorvastatin 20mg (200 units)",   Type.REVENUE,  24000, now.minusDays(6),  "Completed"));
        addTx(new Transaction("#T0002","Pickup logistics — Route R-12",                Type.COST,      3200, now.minusDays(6),  "Paid"));
        addTx(new Transaction("#T0003","Resale — Paracetamol 500mg (100 units)",      Type.REVENUE,   8500, now.minusDays(7),  "Completed"));
        addTx(new Transaction("#T0004","NGO Donation — Hope Foundation (200 units)",   Type.DONATION,     0, now.minusDays(7),  "Dispatched"));
        addTx(new Transaction("#T0005","Staff salaries — March 2026",                  Type.COST,     12800, now.minusDays(8),  "Paid"));
        addTx(new Transaction("#T0006","Resale — Metformin 850mg (60 units)",          Type.REVENUE,   5400, now.minusDays(9),  "Completed"));
        addTx(new Transaction("#T0007","Bulk sale — Ciprofloxacin 500mg (95 units)",   Type.REVENUE,  18200, now.minusDays(10), "Completed"));
        addTx(new Transaction("#T0008","Cold storage electricity bill",                 Type.COST,      2400, now.minusDays(11), "Paid"));
        addTx(new Transaction("#T0009","Resale — Omeprazole 20mg (45 units)",          Type.REVENUE,   4200, now.minusDays(12), "Completed"));
        addTx(new Transaction("#T0010","Vehicle maintenance — Fleet 2",                Type.COST,      5800, now.minusDays(14), "Paid"));
        addTx(new Transaction("#T0011","Bulk sale — Ibuprofen 400mg (350 units)",      Type.REVENUE,  14700, now.minusDays(15), "Completed"));
        addTx(new Transaction("#T0012","Charity allocation — Al-Shifa Trust",          Type.DONATION,     0, now.minusDays(16), "Dispatched"));
        addTx(new Transaction("#T0013","Admin & IT services — March",                  Type.COST,      3200, now.minusDays(17), "Paid"));
        addTx(new Transaction("#T0014","Resale — Azithromycin 500mg (22 units)",       Type.REVENUE,   6600, now.minusDays(18), "Completed"));
        addTx(new Transaction("#T0015","CSR Grant — Unilever Pakistan",                Type.REVENUE,  15000, now.minusDays(20), "Completed"));
        addTx(new Transaction("#T0016","Storage rent — March 2026",                    Type.COST,      6500, now.minusDays(22), "Paid"));
        addTx(new Transaction("#T0017","Processing fees — Batch B2041",                Type.REVENUE,   2400, now.minusDays(24), "Completed"));
        addTx(new Transaction("#T0018","Marketing — Social Media March",               Type.COST,      1800, now.minusDays(26), "Paid"));
        addTx(new Transaction("#T0019","Bulk sale — Vitamin D3 (500 units)",           Type.REVENUE,   9000, now.minusDays(28), "Completed"));
        addTx(new Transaction("#T0020","Pickup logistics — Route R-11",                Type.COST,      2900, now.minusDays(30), "Paid"));
    }

    private void addTx(Transaction t) { transactions.add(t); }

    private void seedPickups() {
        LocalDate today = LocalDate.now();
        addPk("P001","U002","Ahmad Khan",      "House 12, Street 4, F-7/2, Islamabad", today,             "10:00 AM – 11:00 AM", 8,  "Ali Hassan",  "Route R-12", Pickup.Status.DONE,      "Islamabad");
        addPk("P002","U003","Sara Fatima",     "DHA Phase 2, Lahore",                  today,             "12:30 PM – 1:30 PM",  14, "Bilal Ahmed", "Route R-13", Pickup.Status.EN_ROUTE,  "Lahore");
        addPk("P003","U008","MedPlus Pharmacy","Blue Area, Islamabad",                 today,             "3:00 PM – 4:00 PM",   52, "Usman Khan",  "Route R-12", Pickup.Status.SCHEDULED, "Islamabad");
        addPk("P004","U006","Bilal Sheikh",    "G-9/3, Islamabad",                     today,             "4:30 PM – 5:30 PM",   120,"Ali Hassan",  "Route R-14", Pickup.Status.SCHEDULED, "Islamabad");
        addPk("P005","U004","Raza Ahmed",      "F-11 Markaz, Islamabad",               today,             "5:00 PM – 6:00 PM",   6,  "Auto-Assign", null,         Pickup.Status.PENDING,   "Islamabad");
        addPk("P006","U009","HealthPlus Pharma","Gulberg III, Lahore",                 today.plusDays(1), "9:00 AM – 11:00 AM",  38, "Bilal Ahmed", "Route R-13", Pickup.Status.SCHEDULED, "Lahore");
        addPk("P007","U005","Faiza Malik",     "Bahria Town Phase 7, Rawalpindi",      today.plusDays(1), "2:00 PM – 4:00 PM",   15, "Usman Khan",  null,         Pickup.Status.PENDING,   "Rawalpindi");
        addPk("P008","U007","Nadia Hussain",   "Gulberg III, Lahore",                  today.minusDays(1),"10:00 AM – 11:00 AM", 9,  "Bilal Ahmed", "Route R-11", Pickup.Status.DONE,      "Lahore");
        addPk("P009","U010","CityMed Store",   "F-6/4, Islamabad",                     today.minusDays(2),"2:00 PM – 4:00 PM",   65, "Ali Hassan",  "Route R-10", Pickup.Status.DONE,      "Islamabad");
        addPk("P010","U002","Ahmad Khan",      "House 12, F-7/2, Islamabad",           today.minusDays(4),"10:00 AM – 11:00 AM", 12, "Usman Khan",  "Route R-09", Pickup.Status.DONE,      "Islamabad");
    }

    private void addPk(String id, String did, String dn, String addr, LocalDate date,
                       String slot, int items, String rider, String route,
                       Pickup.Status status, String city) {
        Pickup p = new Pickup(id, did, dn, addr, date, slot, items);
        p.setRider(rider); p.setRouteId(route); p.setStatus(status); p.setCity(city);
        pickups.add(p);
    }

    private void seedCharityRequests() {
        LocalDate now = LocalDate.now();
        addReq("REQ0001","U012","Hope Foundation",    "Antibiotics",         200, CharityRequest.Status.APPROVED,  now.minusDays(7),  "Routine");
        addReq("REQ0002","U013","Al-Shifa Trust",     "Mixed Medicines",     500, CharityRequest.Status.PENDING,   now.minusDays(4),  "Moderate");
        addReq("REQ0003","U015","Child Aid Pakistan", "Pediatric Meds",      150, CharityRequest.Status.PENDING,   now.minusDays(3),  "Urgent");
        addReq("REQ0004","U016","Green Crescent",     "Chronic Disease Meds", 80, CharityRequest.Status.DISPATCHED,now.minusDays(10), "Routine");
        addReq("REQ0005","U014","Edhi Foundation",    "Analgesics",          300, CharityRequest.Status.FULFILLED, now.minusDays(15), "Routine");
        addReq("REQ0006","U012","Hope Foundation",    "Diabetes Medicines",  100, CharityRequest.Status.PENDING,   now.minusDays(1),  "Critical");
        addReq("REQ0007","U013","Al-Shifa Trust",     "Antibiotics",         120, CharityRequest.Status.APPROVED,  now.minusDays(12), "Routine");
        addReq("REQ0008","U015","Child Aid Pakistan", "Vitamins",            200, CharityRequest.Status.FULFILLED, now.minusDays(20), "Routine");
        addReq("REQ0009","U016","Green Crescent",     "Analgesics",           60, CharityRequest.Status.PENDING,   now,               "Urgent");
        addReq("REQ0010","U014","Edhi Foundation",    "Gastro Medicines",     80, CharityRequest.Status.DISPATCHED,now.minusDays(5),  "Moderate");
    }

    private void addReq(String id, String cid, String cname, String cat, int qty,
                        CharityRequest.Status status, LocalDate date, String urgency) {
        CharityRequest r = new CharityRequest(id, cid, cname, cat, qty);
        r.setStatus(status); r.setRequestDate(date); r.setUrgency(urgency);
        charReqs.add(r);
    }
}