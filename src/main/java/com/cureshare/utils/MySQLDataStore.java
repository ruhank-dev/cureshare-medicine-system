package com.cureshare.utils;

import com.cureshare.models.*;
import com.cureshare.models.Transaction.Type;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MySQLDataStore — full MySQL-backed implementation.
 *
 * This class mirrors every method signature of DataStore exactly.
 * DataStore automatically delegates to this when DatabaseConfig.USE_DATABASE = true.
 *
 * Connection pooling note: for a professor demo, a single connection is fine.
 * For real production, swap getConnection() with HikariCP or c3p0 pool.
 */
public class MySQLDataStore {

    private static MySQLDataStore instance;
    private Connection conn;

    // Local caches so ObservableList bindings still work in the UI
    private final ObservableList<User>          users    = FXCollections.observableArrayList();
    private final ObservableList<Medicine>       meds     = FXCollections.observableArrayList();
    private final ObservableList<Transaction>    txns     = FXCollections.observableArrayList();
    private final ObservableList<Pickup>         pickups  = FXCollections.observableArrayList();
    private final ObservableList<CharityRequest> charReqs = FXCollections.observableArrayList();

    private MySQLDataStore() {
        connect();
        loadAll();
    }

    public static MySQLDataStore getInstance() {
        if (instance == null) instance = new MySQLDataStore();
        return instance;
    }

    // ─────────────────────────────────────────────────
    //  CONNECTION
    // ─────────────────────────────────────────────────
    private void connect() {
        try {
            conn = DriverManager.getConnection(
                DatabaseConfig.URL,
                DatabaseConfig.USERNAME,
                DatabaseConfig.PASSWORD);
            System.out.println("[CureShare] Connected to MySQL: " + DatabaseConfig.DATABASE);
        } catch (SQLException e) {
            System.err.println("[CureShare] MySQL connection failed: " + e.getMessage());
            System.err.println("[CureShare] Check DatabaseConfig.java — falling back to demo mode.");
            conn = null;
        }
    }

    private Connection getConn() {
        try {
            if (conn == null || conn.isClosed()) connect();
        } catch (SQLException ignored) {}
        return conn;
    }

    public boolean isConnected() {
        try { return conn != null && !conn.isClosed(); } catch (SQLException e) { return false; }
    }

    // ─────────────────────────────────────────────────
    //  LOAD ALL (populates Observable caches on startup)
    // ─────────────────────────────────────────────────
    private void loadAll() {
        if (!isConnected()) return;
        loadUsers();
        loadMedicines();
        loadTransactions();
        loadPickups();
        loadCharityRequests();
    }

    private void loadUsers() {
        users.clear();
        String sql = "SELECT * FROM users ORDER BY join_date";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) users.add(mapUser(rs));
        } catch (SQLException e) { System.err.println("[DB] loadUsers: " + e.getMessage()); }
    }

    private void loadMedicines() {
        meds.clear();
        String sql = "SELECT * FROM medicines ORDER BY submitted_date DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) meds.add(mapMedicine(rs));
        } catch (SQLException e) { System.err.println("[DB] loadMedicines: " + e.getMessage()); }
    }

    private void loadTransactions() {
        txns.clear();
        String sql = "SELECT * FROM transactions ORDER BY txn_date DESC, id DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) txns.add(mapTransaction(rs));
        } catch (SQLException e) { System.err.println("[DB] loadTransactions: " + e.getMessage()); }
    }

    private void loadPickups() {
        pickups.clear();
        String sql = "SELECT * FROM pickups ORDER BY pickup_date DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) pickups.add(mapPickup(rs));
        } catch (SQLException e) { System.err.println("[DB] loadPickups: " + e.getMessage()); }
    }

    private void loadCharityRequests() {
        charReqs.clear();
        String sql = "SELECT * FROM charity_requests ORDER BY request_date DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) charReqs.add(mapCharityRequest(rs));
        } catch (SQLException e) { System.err.println("[DB] loadCharityRequests: " + e.getMessage()); }
    }

    // ─────────────────────────────────────────────────
    //  AUTH
    // ─────────────────────────────────────────────────
    public User authenticate(String email, String password) {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = mapUser(rs);
                // BCrypt verify
                if (PasswordUtil.verify(password, u.getPassword())) return u;
            }
        } catch (SQLException e) { System.err.println("[DB] authenticate: " + e.getMessage()); }
        return null;
    }

    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, email); ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { System.err.println("[DB] emailExists: " + e.getMessage()); }
        return false;
    }

    public void registerUser(User user) {
        // Generate next ID
        String nextId = nextId("users", "U");
        user.setId(nextId);
        // Hash password
        user.setPassword(PasswordUtil.hash(user.getPassword()));

        String sql = "INSERT INTO users (id,name,email,password,role,organisation,phone,city,address,points,status,join_date) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, user.getId());
            ps.setString(2, user.getName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getRole());
            ps.setString(6, user.getOrganization());
            ps.setString(7, user.getPhone());
            ps.setString(8, user.getCity());
            ps.setString(9, user.getAddress());
            ps.setInt(10, user.getPoints());
            ps.setString(11, user.getStatus());
            ps.setDate(12, java.sql.Date.valueOf(LocalDate.now()));
            ps.executeUpdate();
            users.add(user);
        } catch (SQLException e) { System.err.println("[DB] registerUser: " + e.getMessage()); }
    }

    // ─────────────────────────────────────────────────
    //  USERS
    // ─────────────────────────────────────────────────
    public ObservableList<User> getAllUsers() { return users; }

    public List<User> getUsersByRole(String role) {
        return users.stream().filter(u -> role.equalsIgnoreCase(u.getRole())).collect(Collectors.toList());
    }

    public User getUserById(String id) {
        return users.stream().filter(u -> id.equals(u.getId())).findFirst().orElse(null);
    }

    public void updateUser(User u) {
        String sql = "UPDATE users SET name=?,email=?,phone=?,city=?,organisation=?,points=?,status=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, u.getName());  ps.setString(2, u.getEmail());
            ps.setString(3, u.getPhone()); ps.setString(4, u.getCity());
            ps.setString(5, u.getOrganization()); ps.setInt(6, u.getPoints());
            ps.setString(7, u.getStatus()); ps.setString(8, u.getId());
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DB] updateUser: " + e.getMessage()); }
    }

    public void updatePassword(String id, String newPlainPassword) {
        String hashed = PasswordUtil.hash(newPlainPassword);
        String sql = "UPDATE users SET password=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, hashed); ps.setString(2, id);
            ps.executeUpdate();
            users.stream().filter(u -> id.equals(u.getId())).findFirst()
                .ifPresent(u -> u.setPassword(hashed));
        } catch (SQLException e) { System.err.println("[DB] updatePassword: " + e.getMessage()); }
    }

    public void deleteUser(String id) {
        String sql = "DELETE FROM users WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, id); ps.executeUpdate();
            users.removeIf(u -> id.equals(u.getId()));
        } catch (SQLException e) { System.err.println("[DB] deleteUser: " + e.getMessage()); }
    }

    // ─────────────────────────────────────────────────
    //  MEDICINES
    // ─────────────────────────────────────────────────
    public ObservableList<Medicine> getAllMedicines() { return meds; }

    public List<Medicine> getPending()   { return filterM(Medicine.Status.PENDING); }
    public List<Medicine> getApproved()  { return filterM(Medicine.Status.APPROVED); }
    public List<Medicine> getRejected()  { return filterM(Medicine.Status.REJECTED); }
    private List<Medicine> filterM(Medicine.Status s) {
        return meds.stream().filter(m->m.getStatus()==s).collect(Collectors.toList());
    }
    public List<Medicine> getExpiringSoon() {
        return meds.stream().filter(Medicine::isExpiringSoon).collect(Collectors.toList());
    }
    public List<Medicine> getLowStock(int threshold) {
        return meds.stream().filter(m->m.getQuantity()<threshold && m.getStatus()==Medicine.Status.APPROVED).collect(Collectors.toList());
    }
    public List<Medicine> getByDonor(String donorId) {
        return meds.stream().filter(m->donorId.equals(m.getDonorId())).collect(Collectors.toList());
    }

    public void addMedicine(Medicine m) {
        m.setId(nextId("medicines","M"));
        String sql = "INSERT INTO medicines (id,name,category,batch_number,expiry_date,quantity,source,donor_id,donor_name,status,storage_location,cold_storage,price,condition_info,notes,submitted_date) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, m.getId()); ps.setString(2, m.getName());
            ps.setString(3, m.getCategory()); ps.setString(4, m.getBatchNumber());
            ps.setDate(5, m.getExpiryDate()!=null?java.sql.Date.valueOf(m.getExpiryDate()):null);
            ps.setInt(6, m.getQuantity()); ps.setString(7, m.getSource());
            ps.setString(8, m.getDonorId()); ps.setString(9, m.getDonorName());
            ps.setString(10, m.getStatus().name());
            ps.setString(11, m.getStorageLocation()); ps.setBoolean(12, m.isColdStorage());
            ps.setDouble(13, m.getPrice()); ps.setString(14, m.getCondition());
            ps.setString(15, m.getNotes());
            ps.setDate(16, java.sql.Date.valueOf(m.getSubmittedDate()!=null?m.getSubmittedDate():LocalDate.now()));
            ps.executeUpdate();
            meds.add(0, m);
        } catch (SQLException e) { System.err.println("[DB] addMedicine: " + e.getMessage()); }
    }

    public void approveMedicine(String id) {
        updateMedicineStatus(id, "APPROVED");
        meds.stream().filter(m->id.equals(m.getId())).findFirst().ifPresent(m -> {
            m.setStatus(Medicine.Status.APPROVED);
            addTransaction(new Transaction(nextTxId(),"Medicine Approved: "+m.getName(),
                Type.REVENUE, m.getQuantity()*120.0, LocalDate.now(),"Completed"));
        });
    }

    public void rejectMedicine(String id) {
        updateMedicineStatus(id, "REJECTED");
        meds.stream().filter(m->id.equals(m.getId())).findFirst()
            .ifPresent(m->m.setStatus(Medicine.Status.REJECTED));
    }

    private void updateMedicineStatus(String id, String status) {
        String sql = "UPDATE medicines SET status=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, status); ps.setString(2, id); ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DB] updateMedicineStatus: " + e.getMessage()); }
    }

    public void deleteMedicine(String id) {
        String sql = "DELETE FROM medicines WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, id); ps.executeUpdate();
            meds.removeIf(m->id.equals(m.getId()));
        } catch (SQLException e) { System.err.println("[DB] deleteMedicine: " + e.getMessage()); }
    }

    public void updateMedicineQuantity(String id, int qty) {
        String sql = "UPDATE medicines SET quantity=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, qty); ps.setString(2, id); ps.executeUpdate();
            meds.stream().filter(m->id.equals(m.getId())).findFirst().ifPresent(m->m.setQuantity(qty));
        } catch (SQLException e) { System.err.println("[DB] updateMedicineQuantity: " + e.getMessage()); }
    }

    public Medicine getMedicineById(String id) {
        return meds.stream().filter(m->id.equals(m.getId())).findFirst().orElse(null);
    }

    // ─────────────────────────────────────────────────
    //  TRANSACTIONS
    // ─────────────────────────────────────────────────
    public ObservableList<Transaction> getAllTransactions() { return txns; }

    public void addTransaction(Transaction t) {
        if (t.getId()==null) t.setId(nextTxId());
        String sql = "INSERT INTO transactions (id,description,type,amount,txn_date,status,notes) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, t.getId()); ps.setString(2, t.getDescription());
            ps.setString(3, t.getType().name()); ps.setDouble(4, t.getAmount());
            ps.setDate(5, java.sql.Date.valueOf(t.getDate()!=null?t.getDate():LocalDate.now()));
            ps.setString(6, t.getStatus()); ps.setString(7, t.getNotes());
            ps.executeUpdate();
            txns.add(0, t);
        } catch (SQLException e) { System.err.println("[DB] addTransaction: " + e.getMessage()); }
    }

    public double getTotalRevenue() {
        return txns.stream().filter(t->t.getType()==Type.REVENUE).mapToDouble(Transaction::getAmount).sum();
    }
    public double getTotalCosts() {
        return txns.stream().filter(t->t.getType()==Type.COST).mapToDouble(Transaction::getAmount).sum();
    }
    public double getNetProfit() { return getTotalRevenue()-getTotalCosts(); }
    public List<Transaction> getRecentTransactions(int limit) {
        return txns.stream().limit(limit).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────
    //  PICKUPS
    // ─────────────────────────────────────────────────
    public ObservableList<Pickup> getAllPickups() { return pickups; }

    public List<Pickup> getTodayPickups() {
        return pickups.stream().filter(p->LocalDate.now().equals(p.getDate())).collect(Collectors.toList());
    }
    public List<Pickup> getPickupsByDonor(String donorId) {
        return pickups.stream().filter(p->donorId.equals(p.getDonorId())).collect(Collectors.toList());
    }

    public void addPickup(Pickup p) {
        p.setId(nextId("pickups","P"));
        String sql = "INSERT INTO pickups (id,donor_id,donor_name,address,city,pickup_date,time_slot,estimated_items,rider,route_id,status,notes) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, p.getId()); ps.setString(2, p.getDonorId());
            ps.setString(3, p.getDonorName()); ps.setString(4, p.getAddress());
            ps.setString(5, p.getCity());
            ps.setDate(6, java.sql.Date.valueOf(p.getDate()!=null?p.getDate():LocalDate.now()));
            ps.setString(7, p.getTimeSlot()); ps.setInt(8, p.getEstimatedItems());
            ps.setString(9, p.getRider()); ps.setString(10, p.getRouteId());
            ps.setString(11, p.getStatus().name()); ps.setString(12, p.getNotes());
            ps.executeUpdate();
            pickups.add(0, p);
        } catch (SQLException e) { System.err.println("[DB] addPickup: " + e.getMessage()); }
    }

    public void updatePickupStatus(String id, Pickup.Status status) {
        String sql = "UPDATE pickups SET status=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, status.name()); ps.setString(2, id); ps.executeUpdate();
            pickups.stream().filter(p->id.equals(p.getId())).findFirst().ifPresent(p->p.setStatus(status));
        } catch (SQLException e) { System.err.println("[DB] updatePickupStatus: " + e.getMessage()); }
    }

    public void completePickup(String id) {
        updatePickupStatus(id, Pickup.Status.DONE);
        pickups.stream().filter(p->id.equals(p.getId())).findFirst().ifPresent(p -> {
            double cost = calculatePickupCost(p);
            addTransaction(new Transaction(null,
                "Pickup completed: "+p.getDonorName()+" ("+p.getEstimatedItems()+" items, "+p.getCity()+")",
                Type.COST, cost, LocalDate.now(), "Paid"));
        });
    }

    public void cancelPickup(String id) { updatePickupStatus(id, Pickup.Status.CANCELLED); }

    public Pickup getPickupById(String id) {
        return pickups.stream().filter(p->id.equals(p.getId())).findFirst().orElse(null);
    }

    // ─────────────────────────────────────────────────
    //  CHARITY REQUESTS
    // ─────────────────────────────────────────────────
    public ObservableList<CharityRequest> getAllCharityRequests() { return charReqs; }

    public List<CharityRequest> getCharityRequestsByCharity(String charityId) {
        return charReqs.stream().filter(r->charityId.equals(r.getCharityId())).collect(Collectors.toList());
    }
    public List<CharityRequest> getPendingCharityRequests() {
        return charReqs.stream().filter(r->r.getStatus()==CharityRequest.Status.PENDING).collect(Collectors.toList());
    }

    public void addCharityRequest(CharityRequest r) {
        r.setId(nextCharityReqId());
        String sql = "INSERT INTO charity_requests (id,charity_id,charity_name,medicine_category,quantity_requested,urgency,status,request_date,required_by,notes) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, r.getId()); ps.setString(2, r.getCharityId());
            ps.setString(3, r.getCharityName()); ps.setString(4, r.getMedicineCategory());
            ps.setInt(5, r.getQuantityRequested()); ps.setString(6, r.getUrgency());
            ps.setString(7, r.getStatus().name());
            ps.setDate(8, java.sql.Date.valueOf(r.getRequestDate()!=null?r.getRequestDate():LocalDate.now()));
            ps.setString(9, r.getRequiredBy()); ps.setString(10, r.getNotes());
            ps.executeUpdate();
            charReqs.add(0, r);
        } catch (SQLException e) { System.err.println("[DB] addCharityRequest: " + e.getMessage()); }
    }

    public void approveCharityRequest(String id) { updateCharityStatus(id, CharityRequest.Status.APPROVED); }
    public void rejectCharityRequest(String id)  { updateCharityStatus(id, CharityRequest.Status.REJECTED); }
    public void dispatchCharityRequest(String id){ updateCharityStatus(id, CharityRequest.Status.DISPATCHED); }

    private void updateCharityStatus(String id, CharityRequest.Status status) {
        String sql = "UPDATE charity_requests SET status=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, status.name()); ps.setString(2, id); ps.executeUpdate();
            charReqs.stream().filter(r->id.equals(r.getId())).findFirst().ifPresent(r->r.setStatus(status));
        } catch (SQLException e) { System.err.println("[DB] updateCharityStatus: " + e.getMessage()); }
    }

    public CharityRequest getRequestById(String id) {
        return charReqs.stream().filter(r->id.equals(r.getId())).findFirst().orElse(null);
    }

    public List<Medicine> searchMedicines(String query) {
        String q = query.toLowerCase();
        return meds.stream().filter(m ->
            (m.getName()!=null && m.getName().toLowerCase().contains(q)) ||
            (m.getCategory()!=null && m.getCategory().toLowerCase().contains(q)) ||
            (m.getBatchNumber()!=null && m.getBatchNumber().toLowerCase().contains(q))
        ).collect(Collectors.toList());
    }

    public Medicine getNextFifo(String category) {
        java.util.stream.Stream<Medicine> stream = getApproved().stream()
            .filter(m -> m.getQuantity() > 0);
        if (category != null && !category.isBlank())
            stream = stream.filter(m -> category.equalsIgnoreCase(m.getCategory()));
        return stream.sorted(java.util.Comparator.comparing(
            m -> m.getSubmittedDate() != null ? m.getSubmittedDate() : LocalDate.MIN))
            .findFirst().orElse(null);
    }

    // ── LOGISTICS COST ────────────────────────────────
    public double calculatePickupCost(Pickup p) {
        if (p == null) return 2800.0;
        double base = 1500.0;
        double perItem = p.getEstimatedItems() * 20.0;
        double citySurcharge = switch(p.getCity() != null ? p.getCity() : "") {
            case "Lahore", "Karachi" -> 800.0;
            case "Rawalpindi"        -> 300.0;
            case "Faisalabad"        -> 600.0;
            default                  -> 0.0;
        };
        return base + perItem + citySurcharge;
    }

    public double getTotalLogisticsCosts() {
        return pickups.stream()
            .filter(p -> p.getStatus() == Pickup.Status.DONE)
            .mapToDouble(this::calculatePickupCost)
            .sum();
    }

    // ── RATINGS — persisted to MySQL ────────────────────
    public void addRating(DataStore.Rating r) {
        String sql = "INSERT INTO ratings (id,from_id,from_name,target_id,target_name,category,stars,comment,rating_date) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, "RAT" + System.currentTimeMillis());
            ps.setString(2, r.fromId);   ps.setString(3, r.fromName);
            ps.setString(4, r.targetId); ps.setString(5, r.targetName);
            ps.setString(6, r.category); ps.setInt(7, r.stars);
            ps.setString(8, r.comment);
            ps.setDate(9, java.sql.Date.valueOf(LocalDate.now()));
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DB] addRating: " + e.getMessage()); }
    }

    public java.util.List<DataStore.Rating> getAllRatings() {
        java.util.List<DataStore.Rating> list = new ArrayList<>();
        String sql = "SELECT * FROM ratings ORDER BY rating_date DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new DataStore.Rating(
                    rs.getString("from_id"), rs.getString("from_name"),
                    rs.getString("target_id"), rs.getString("target_name"),
                    rs.getString("category"), rs.getInt("stars"),
                    rs.getString("comment")));
            }
        } catch (SQLException e) { System.err.println("[DB] getAllRatings: " + e.getMessage()); }
        return list;
    }

    public double getAvgRating(String targetId) {
        String sql = "SELECT AVG(stars) FROM ratings WHERE target_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, targetId); ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { System.err.println("[DB] getAvgRating: " + e.getMessage()); }
        return 0.0;
    }

    public double getOverallAvgRating() {
        String sql = "SELECT AVG(stars) FROM ratings";
        try (PreparedStatement ps = getConn().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) { double v = rs.getDouble(1); return v > 0 ? v : 4.7; }
        } catch (SQLException e) { System.err.println("[DB] getOverallAvgRating: " + e.getMessage()); }
        return 4.7;
    }

    // ── AUDIT LOG — persisted to MySQL ───────────────────
    public void logAudit(String userId, String userName, String action, String detail, String category) {
        String sql = "INSERT INTO audit_log (id,log_time,user_id,user_name,action,detail,category) VALUES (?,NOW(),?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, "AL" + System.currentTimeMillis());
            ps.setString(2, userId);   ps.setString(3, userName);
            ps.setString(4, action);   ps.setString(5, detail);
            ps.setString(6, category);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DB] logAudit: " + e.getMessage()); }
    }

    public java.util.List<AuditLog.Entry> getAuditLog(int limit) {
        java.util.List<AuditLog.Entry> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_log ORDER BY log_time DESC LIMIT ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, limit); ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new AuditLog.Entry(
                    rs.getString("user_id"), rs.getString("user_name"),
                    rs.getString("action"),  rs.getString("detail"),
                    rs.getString("category")));
            }
        } catch (SQLException e) { System.err.println("[DB] getAuditLog: " + e.getMessage()); }
        return list;
    }

    public int getAuditCount() {
        String sql = "SELECT COUNT(*) FROM audit_log";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println("[DB] getAuditCount: " + e.getMessage()); }
        return 0;
    }

    // ── SYSTEM SETTINGS — persisted to MySQL ─────────────
    private boolean fifoEnabled = true; // local cache of DB setting
    public boolean getSetting(String key, boolean defaultVal) {
        String sql = "SELECT setting_value FROM system_settings WHERE setting_key=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, key); ResultSet rs = ps.executeQuery();
            if (rs.next()) return Boolean.parseBoolean(rs.getString(1));
        } catch (SQLException e) { System.err.println("[DB] getSetting: " + e.getMessage()); }
        return defaultVal;
    }

    public void setSetting(String key, boolean value) {
        String sql = "INSERT INTO system_settings (setting_key,setting_value,updated_at) VALUES (?,?,NOW()) " +
                     "ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value), updated_at=NOW()";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            String v = String.valueOf(value);
            ps.setString(1, key); ps.setString(2, v);
            ps.executeUpdate();
            if ("fifo_enabled".equals(key)) fifoEnabled = value;
        } catch (SQLException e) { System.err.println("[DB] setSetting: " + e.getMessage()); }
    }

    public boolean isFifoEnabled()       { return getSetting("fifo_enabled", true); }
    public void setFifoEnabled(boolean v) { fifoEnabled = v; setSetting("fifo_enabled", v); }

    // ─────────────────────────────────────────────────
    //  SUMMARY STATS
    // ─────────────────────────────────────────────────
    public int getTotalDonors() { return (int)users.stream().filter(u->!"admin".equalsIgnoreCase(u.getRole())).count(); }
    public int getHouseholdCount()  { return getUsersByRole("household").size(); }
    public int getPharmacyCount()   { return getUsersByRole("pharmacy").size(); }
    public int getCharityCount()    { return getUsersByRole("charity").size(); }

    // ─────────────────────────────────────────────────
    //  LEGACY COMPAT
    // ─────────────────────────────────────────────────
    public List<Pickup> getPickupsAsArray() {
        return getTodayPickups().stream().limit(6).collect(Collectors.toList());
    }
    public List<CharityRequest> getCharityRequestsAsArray() {
        return charReqs.stream().limit(6).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────
    //  RESULT SET MAPPERS
    // ─────────────────────────────────────────────────
    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getString("id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password")); // already hashed
        u.setRole(rs.getString("role"));
        u.setOrganization(rs.getString("organisation"));
        u.setPhone(rs.getString("phone"));
        u.setCity(rs.getString("city"));
        u.setAddress(rs.getString("address"));
        u.setPoints(rs.getInt("points"));
        u.setStatus(rs.getString("status"));
        java.sql.Date jd = rs.getDate("join_date");
        if (jd != null) u.setJoinDate(jd.toLocalDate().toString());
        return u;
    }

    private Medicine mapMedicine(ResultSet rs) throws SQLException {
        Medicine m = new Medicine();
        m.setId(rs.getString("id"));
        m.setName(rs.getString("name"));
        m.setCategory(rs.getString("category"));
        m.setBatchNumber(rs.getString("batch_number"));
        java.sql.Date expiry = rs.getDate("expiry_date");
        if (expiry != null) m.setExpiryDate(expiry.toLocalDate());
        m.setQuantity(rs.getInt("quantity"));
        m.setSource(rs.getString("source"));
        m.setDonorId(rs.getString("donor_id"));
        m.setDonorName(rs.getString("donor_name"));
        try { m.setStatus(Medicine.Status.valueOf(rs.getString("status"))); }
        catch (Exception e) { m.setStatus(Medicine.Status.PENDING); }
        m.setStorageLocation(rs.getString("storage_location"));
        m.setColdStorage(rs.getBoolean("cold_storage"));
        m.setPrice(rs.getDouble("price"));
        m.setCondition(rs.getString("condition_info"));
        m.setNotes(rs.getString("notes"));
        java.sql.Date sub = rs.getDate("submitted_date");
        if (sub != null) m.setSubmittedDate(sub.toLocalDate());
        return m;
    }

    private Transaction mapTransaction(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setId(rs.getString("id"));
        t.setDescription(rs.getString("description"));
        try { t.setType(Type.valueOf(rs.getString("type"))); }
        catch (Exception e) { t.setType(Type.REVENUE); }
        t.setAmount(rs.getDouble("amount"));
        java.sql.Date d = rs.getDate("txn_date");
        if (d != null) t.setDate(d.toLocalDate());
        t.setStatus(rs.getString("status"));
        t.setNotes(rs.getString("notes"));
        return t;
    }

    private Pickup mapPickup(ResultSet rs) throws SQLException {
        Pickup p = new Pickup();
        p.setId(rs.getString("id"));
        p.setDonorId(rs.getString("donor_id"));
        p.setDonorName(rs.getString("donor_name"));
        p.setAddress(rs.getString("address"));
        p.setCity(rs.getString("city"));
        java.sql.Date d = rs.getDate("pickup_date");
        if (d != null) p.setDate(d.toLocalDate());
        p.setTimeSlot(rs.getString("time_slot"));
        p.setEstimatedItems(rs.getInt("estimated_items"));
        p.setActualItems(rs.getInt("actual_items"));
        p.setRider(rs.getString("rider"));
        p.setRouteId(rs.getString("route_id"));
        try { p.setStatus(Pickup.Status.valueOf(rs.getString("status"))); }
        catch (Exception e) { p.setStatus(Pickup.Status.PENDING); }
        p.setNotes(rs.getString("notes"));
        return p;
    }

    private CharityRequest mapCharityRequest(ResultSet rs) throws SQLException {
        CharityRequest r = new CharityRequest();
        r.setId(rs.getString("id"));
        r.setCharityId(rs.getString("charity_id"));
        r.setCharityName(rs.getString("charity_name"));
        r.setMedicineCategory(rs.getString("medicine_category"));
        r.setQuantityRequested(rs.getInt("quantity_requested"));
        r.setQuantityFulfilled(rs.getInt("quantity_fulfilled"));
        r.setUrgency(rs.getString("urgency"));
        try { r.setStatus(CharityRequest.Status.valueOf(rs.getString("status"))); }
        catch (Exception e) { r.setStatus(CharityRequest.Status.PENDING); }
        java.sql.Date d = rs.getDate("request_date");
        if (d != null) r.setRequestDate(d.toLocalDate());
        r.setRequiredBy(rs.getString("required_by"));
        r.setMedicineId(rs.getString("medicine_id"));
        r.setAssignedMedicineName(rs.getString("assigned_medicine"));
        r.setNotes(rs.getString("notes"));
        return r;
    }

    // ─────────────────────────────────────────────────
    //  SEED HELPERS (used for first-time auto-seed)
    // ─────────────────────────────────────────────────
    /** Reloads all caches from DB — called after auto-seeding */
    public void reload() {
        loadAll();
    }

    /** Insert user with already-hashed password (for seeding) */
    public void registerUserHashed(User user) {
        String sql = "INSERT IGNORE INTO users (id,name,email,password,role,organisation,phone,city,address,points,status,join_date) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, user.getId());    ps.setString(2, user.getName());
            ps.setString(3, user.getEmail()); ps.setString(4, user.getPassword()); // already hashed
            ps.setString(5, user.getRole());  ps.setString(6, user.getOrganization());
            ps.setString(7, user.getPhone()); ps.setString(8, user.getCity());
            ps.setString(9, user.getAddress()); ps.setInt(10, user.getPoints());
            ps.setString(11, user.getStatus() != null ? user.getStatus() : "active");
            ps.setDate(12, java.sql.Date.valueOf(LocalDate.now()));
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DB] registerUserHashed: " + e.getMessage()); }
    }

    /** Insert medicine directly (for seeding — bypasses ID generation) */
    public void addMedicineRaw(Medicine m) {
        String sql = "INSERT IGNORE INTO medicines (id,name,category,batch_number,expiry_date,quantity,source,donor_id,donor_name,status,storage_location,cold_storage,price,condition_info,notes,submitted_date) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, m.getId()); ps.setString(2, m.getName());
            ps.setString(3, m.getCategory()); ps.setString(4, m.getBatchNumber());
            ps.setDate(5, m.getExpiryDate()!=null ? java.sql.Date.valueOf(m.getExpiryDate()) : null);
            ps.setInt(6, m.getQuantity()); ps.setString(7, m.getSource());
            ps.setString(8, m.getDonorId()); ps.setString(9, m.getDonorName());
            ps.setString(10, m.getStatus().name()); ps.setString(11, m.getStorageLocation());
            ps.setBoolean(12, m.isColdStorage()); ps.setDouble(13, m.getPrice());
            ps.setString(14, m.getCondition()); ps.setString(15, m.getNotes());
            ps.setDate(16, java.sql.Date.valueOf(m.getSubmittedDate()!=null ? m.getSubmittedDate() : LocalDate.now()));
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DB] addMedicineRaw: " + e.getMessage()); }
    }

    /** Insert transaction directly (for seeding) */
    public void addTransactionRaw(Transaction t) {
        String sql = "INSERT IGNORE INTO transactions (id,description,type,amount,txn_date,status,notes) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, t.getId()); ps.setString(2, t.getDescription());
            ps.setString(3, t.getType().name()); ps.setDouble(4, t.getAmount());
            ps.setDate(5, java.sql.Date.valueOf(t.getDate()!=null ? t.getDate() : LocalDate.now()));
            ps.setString(6, t.getStatus()); ps.setString(7, t.getNotes());
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DB] addTransactionRaw: " + e.getMessage()); }
    }

    /** Insert pickup directly (for seeding) */
    public void addPickupRaw(Pickup p) {
        String sql = "INSERT IGNORE INTO pickups (id,donor_id,donor_name,address,city,pickup_date,time_slot,estimated_items,rider,route_id,status,notes) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, p.getId()); ps.setString(2, p.getDonorId());
            ps.setString(3, p.getDonorName()); ps.setString(4, p.getAddress());
            ps.setString(5, p.getCity());
            ps.setDate(6, java.sql.Date.valueOf(p.getDate()!=null ? p.getDate() : LocalDate.now()));
            ps.setString(7, p.getTimeSlot()); ps.setInt(8, p.getEstimatedItems());
            ps.setString(9, p.getRider()); ps.setString(10, p.getRouteId());
            ps.setString(11, p.getStatus().name()); ps.setString(12, p.getNotes());
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DB] addPickupRaw: " + e.getMessage()); }
    }

    /** Insert charity request directly (for seeding) */
    public void addCharityRequestRaw(CharityRequest r) {
        String sql = "INSERT IGNORE INTO charity_requests (id,charity_id,charity_name,medicine_category,quantity_requested,urgency,status,request_date,required_by,notes) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, r.getId()); ps.setString(2, r.getCharityId());
            ps.setString(3, r.getCharityName()); ps.setString(4, r.getMedicineCategory());
            ps.setInt(5, r.getQuantityRequested()); ps.setString(6, r.getUrgency());
            ps.setString(7, r.getStatus().name());
            ps.setDate(8, java.sql.Date.valueOf(r.getRequestDate()!=null ? r.getRequestDate() : LocalDate.now()));
            ps.setString(9, r.getRequiredBy()); ps.setString(10, r.getNotes());
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DB] addCharityRequestRaw: " + e.getMessage()); }
    }

    // ─────────────────────────────────────────────────
    //  ID GENERATORS
    // ─────────────────────────────────────────────────
    private String nextId(String table, String prefix) {
        String sql = "SELECT COUNT(*) FROM " + table;
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return prefix + String.format("%03d", rs.getInt(1) + 1);
        } catch (SQLException e) { System.err.println("[DB] nextId: " + e.getMessage()); }
        return prefix + System.currentTimeMillis();
    }

    private String nextTxId() {
        String sql = "SELECT COUNT(*) FROM transactions";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return "#T" + String.format("%04d", rs.getInt(1) + 1);
        } catch (SQLException e) { System.err.println("[DB] nextTxId: " + e.getMessage()); }
        return "#T" + System.currentTimeMillis();
    }

    private String nextCharityReqId() {
        String sql = "SELECT COUNT(*) FROM charity_requests";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return "REQ" + String.format("%04d", rs.getInt(1) + 1);
        } catch (SQLException e) { System.err.println("[DB] nextCharityReqId: " + e.getMessage()); }
        return "REQ" + System.currentTimeMillis();
    }
}
