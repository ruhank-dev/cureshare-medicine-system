package com.cureshare.utils;

import com.cureshare.models.*;
import com.cureshare.models.Transaction.Type;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SQLiteDataStore — SQLite implementation for CureShare BMS.
 *
 * The database file is created automatically at:
 *   cureshare.db  (in the folder where you run the app)
 *
 * HOW IT WORKS:
 *   1. On first run → creates all tables + seeds demo data
 *   2. On later runs → just connects and loads existing data
 *   3. No server needed — it's just a file!
 */
public class SQLiteDataStore {

    // ── The single shared instance (Singleton pattern) ─────────────────
    private static SQLiteDataStore instance;

    // ── The database connection ─────────────────────────────────────────
    private Connection conn;

    // ── In-memory caches so the UI can bind to these lists ─────────────
    private final ObservableList<User>          users    = FXCollections.observableArrayList();
    private final ObservableList<Medicine>       meds     = FXCollections.observableArrayList();
    private final ObservableList<Transaction>    txns     = FXCollections.observableArrayList();
    private final ObservableList<Pickup>         pickups  = FXCollections.observableArrayList();
    private final ObservableList<CharityRequest> charReqs = FXCollections.observableArrayList();

    // ────────────────────────────────────────────────────────────────────
    //  CONSTRUCTOR — runs when the app first calls getInstance()
    // ────────────────────────────────────────────────────────────────────
    private SQLiteDataStore() {
        connect();       // Step 1: open/create the .db file
        createTables();  // Step 2: create tables if they don't exist
        loadAll();       // Step 3: load all data into memory
    }

    /** Call this anywhere in your app to get the database object */
    public static SQLiteDataStore getInstance() {
        if (instance == null) instance = new SQLiteDataStore();
        return instance;
    }

    // ────────────────────────────────────────────────────────────────────
    //  CONNECTION
    // ────────────────────────────────────────────────────────────────────

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            String dbPath = System.getProperty("user.home") + "/cureshare.db";
            
            // Set busy timeout BEFORE connecting via properties
            java.util.Properties props = new java.util.Properties();
            props.setProperty("busy_timeout", "10000");
            
            String url = "jdbc:sqlite:" + dbPath + "?busy_timeout=10000";
            System.out.println("[SQLite] Database path: " + dbPath);
            
            conn = DriverManager.getConnection(url);
            
            // These pragmas prevent locking issues
            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA journal_mode=WAL;");
                st.execute("PRAGMA foreign_keys=ON;");
                st.execute("PRAGMA busy_timeout=10000;");
                st.execute("PRAGMA synchronous=NORMAL;");
                st.execute("PRAGMA cache_size=1000;");
                st.execute("PRAGMA temp_store=MEMORY;");
            }
            
            conn.setAutoCommit(true);
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try { 
                    if (conn != null && !conn.isClosed()) {
                        conn.createStatement().execute("PRAGMA wal_checkpoint(FULL);");
                        conn.close();
                        System.out.println("[SQLite] Connection closed cleanly.");
                    }
                } catch (SQLException ignored) {}
            }));

            System.out.println("[SQLite] Connected successfully!");
            
        } catch (Exception e) {
            System.err.println("[SQLite] ERROR connecting: " + e.getMessage());
            conn = null;
        }
    }

    public boolean isConnected() {
        try {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // ────────────────────────────────────────────────────────────────────
    //  CREATE TABLES
    //  SQL "IF NOT EXISTS" means this is safe to run every time —
    //  it only creates tables on the first run.
    // ────────────────────────────────────────────────────────────────────

    private void createTables() {
        try (Statement st = conn.createStatement()) {

            // USERS table
            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id          TEXT PRIMARY KEY,
                    name        TEXT NOT NULL,
                    email       TEXT UNIQUE NOT NULL,
                    password    TEXT NOT NULL,
                    role        TEXT NOT NULL,
                    organisation TEXT,
                    phone       TEXT,
                    city        TEXT,
                    address     TEXT,
                    points      INTEGER DEFAULT 0,
                    status      TEXT DEFAULT 'active',
                    join_date   TEXT
                )
            """);

            // MEDICINES table
            st.execute("""
                CREATE TABLE IF NOT EXISTS medicines (
                    id               TEXT PRIMARY KEY,
                    name             TEXT NOT NULL,
                    category         TEXT,
                    batch_number     TEXT,
                    expiry_date      TEXT,
                    quantity         INTEGER DEFAULT 0,
                    source           TEXT,
                    donor_id         TEXT,
                    donor_name       TEXT,
                    status           TEXT DEFAULT 'PENDING',
                    storage_location TEXT,
                    cold_storage     INTEGER DEFAULT 0,
                    price            REAL DEFAULT 0,
                    condition_info   TEXT,
                    notes            TEXT,
                    submitted_date   TEXT
                )
            """);

            // TRANSACTIONS table
            st.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id          TEXT PRIMARY KEY,
                    description TEXT,
                    type        TEXT,
                    amount      REAL DEFAULT 0,
                    txn_date    TEXT,
                    status      TEXT,
                    notes       TEXT
                )
            """);

            // PICKUPS table
            st.execute("""
                CREATE TABLE IF NOT EXISTS pickups (
                    id              TEXT PRIMARY KEY,
                    donor_id        TEXT,
                    donor_name      TEXT,
                    address         TEXT,
                    city            TEXT,
                    pickup_date     TEXT,
                    time_slot       TEXT,
                    estimated_items INTEGER DEFAULT 0,
                    actual_items    INTEGER DEFAULT 0,
                    rider           TEXT,
                    route_id        TEXT,
                    status          TEXT DEFAULT 'PENDING',
                    notes           TEXT
                )
            """);

            // CHARITY REQUESTS table
            st.execute("""
                CREATE TABLE IF NOT EXISTS charity_requests (
                    id                 TEXT PRIMARY KEY,
                    charity_id         TEXT,
                    charity_name       TEXT,
                    medicine_category  TEXT,
                    quantity_requested INTEGER DEFAULT 0,
                    quantity_fulfilled INTEGER DEFAULT 0,
                    urgency            TEXT,
                    status             TEXT DEFAULT 'PENDING',
                    request_date       TEXT,
                    required_by        TEXT,
                    medicine_id        TEXT,
                    assigned_medicine  TEXT,
                    notes              TEXT
                )
            """);

            // RATINGS table
            st.execute("""
                CREATE TABLE IF NOT EXISTS ratings (
                    id          TEXT PRIMARY KEY,
                    from_id     TEXT,
                    from_name   TEXT,
                    target_id   TEXT,
                    target_name TEXT,
                    category    TEXT,
                    stars       INTEGER,
                    comment     TEXT,
                    rating_date TEXT
                )
            """);

            // AUDIT LOG table
            st.execute("""
                CREATE TABLE IF NOT EXISTS audit_log (
                    id        TEXT PRIMARY KEY,
                    log_time  TEXT,
                    user_id   TEXT,
                    user_name TEXT,
                    action    TEXT,
                    detail    TEXT,
                    category  TEXT
                )
            """);

            // SYSTEM SETTINGS table
            st.execute("""
                CREATE TABLE IF NOT EXISTS system_settings (
                    setting_key   TEXT PRIMARY KEY,
                    setting_value TEXT,
                    updated_at    TEXT
                )
            """);

            System.out.println("[SQLite] All tables ready.");
        } catch (SQLException e) {
            System.err.println("[SQLite] ERROR creating tables: " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────────────────────────
    //  LOAD ALL DATA INTO MEMORY
    // ────────────────────────────────────────────────────────────────────

    public void loadAll() {
        loadUsers();
        loadMedicines();
        loadTransactions();
        loadPickups();
        loadCharityRequests();
    }

    private void loadUsers() {
        users.clear();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM users ORDER BY join_date");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) users.add(mapUser(rs));
        } catch (SQLException e) {
            System.err.println("[SQLite] loadUsers: " + e.getMessage());
        }
    }

    private void loadMedicines() {
        meds.clear();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM medicines ORDER BY submitted_date DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) meds.add(mapMedicine(rs));
        } catch (SQLException e) {
            System.err.println("[SQLite] loadMedicines: " + e.getMessage());
        }
    }

    private void loadTransactions() {
        txns.clear();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM transactions ORDER BY txn_date DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) txns.add(mapTransaction(rs));
        } catch (SQLException e) {
            System.err.println("[SQLite] loadTransactions: " + e.getMessage());
        }
    }

    private void loadPickups() {
        pickups.clear();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM pickups ORDER BY pickup_date DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) pickups.add(mapPickup(rs));
        } catch (SQLException e) {
            System.err.println("[SQLite] loadPickups: " + e.getMessage());
        }
    }

    private void loadCharityRequests() {
        charReqs.clear();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM charity_requests ORDER BY request_date DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) charReqs.add(mapCharityRequest(rs));
        } catch (SQLException e) {
            System.err.println("[SQLite] loadCharityRequests: " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────────────────────────
    //  AUTH (Login / Register)
    // ────────────────────────────────────────────────────────────────────

    public User authenticate(String email, String password) {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = mapUser(rs);
                // BCrypt check — compares plain password with the hash stored in DB
                if (PasswordUtil.verify(password, u.getPassword())) return u;
            }
        } catch (SQLException e) {
            System.err.println("[SQLite] authenticate: " + e.getMessage());
        }
        return null; // null means wrong email or password
    }

    public boolean emailExists(String email) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?)")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("[SQLite] emailExists: " + e.getMessage());
        }
        return false;
    }

    public void registerUser(User user) {
        user.setId(nextId("users", "U"));
        user.setPassword(PasswordUtil.hash(user.getPassword())); // ALWAYS hash before storing!

        String sql = "INSERT INTO users (id,name,email,password,role,organisation,phone,city,address,points,status,join_date) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getId());
            ps.setString(2, user.getName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getRole());
            ps.setString(6, user.getOrganization());
            ps.setString(7, user.getPhone());
            ps.setString(8, user.getCity());
            ps.setString(9, user.getAddress());
            ps.setInt   (10, user.getPoints());
            ps.setString(11, user.getStatus() != null ? user.getStatus() : "active");
            ps.setString(12, LocalDate.now().toString());
            ps.executeUpdate();
            users.add(user); // also add to in-memory cache
        } catch (SQLException e) {
            System.err.println("[SQLite] registerUser: " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────────────────────────
    //  USERS
    // ────────────────────────────────────────────────────────────────────

    public ObservableList<User> getAllUsers() { return users; }

    public List<User> getUsersByRole(String role) {
        return users.stream()
                .filter(u -> role.equalsIgnoreCase(u.getRole()))
                .collect(Collectors.toList());
    }

    public User getUserById(String id) {
        return users.stream().filter(u -> id.equals(u.getId())).findFirst().orElse(null);
    }

    public void updateUser(User u) {
        String sql = "UPDATE users SET name=?,email=?,phone=?,city=?,organisation=?,points=?,status=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPhone());
            ps.setString(4, u.getCity());
            ps.setString(5, u.getOrganization());
            ps.setInt   (6, u.getPoints());
            ps.setString(7, u.getStatus());
            ps.setString(8, u.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[SQLite] updateUser: " + e.getMessage());
        }
    }

    public void updatePassword(String id, String newPlainPassword) {
        String hashed = PasswordUtil.hash(newPlainPassword);
        try (PreparedStatement ps = conn.prepareStatement("UPDATE users SET password=? WHERE id=?")) {
            ps.setString(1, hashed);
            ps.setString(2, id);
            ps.executeUpdate();
            // Also update in-memory cache
            users.stream().filter(u -> id.equals(u.getId())).findFirst()
                    .ifPresent(u -> u.setPassword(hashed));
        } catch (SQLException e) {
            System.err.println("[SQLite] updatePassword: " + e.getMessage());
        }
    }

    public void deleteUser(String id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id=?")) {
            ps.setString(1, id);
            ps.executeUpdate();
            users.removeIf(u -> id.equals(u.getId())); // remove from cache too
        } catch (SQLException e) {
            System.err.println("[SQLite] deleteUser: " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────────────────────────
    //  MEDICINES
    // ────────────────────────────────────────────────────────────────────

    public ObservableList<Medicine> getAllMedicines() { return meds; }

    public List<Medicine> getPending()  { return filterByStatus(Medicine.Status.PENDING);  }
    public List<Medicine> getApproved() { return filterByStatus(Medicine.Status.APPROVED); }
    public List<Medicine> getRejected() { return filterByStatus(Medicine.Status.REJECTED); }

    private List<Medicine> filterByStatus(Medicine.Status s) {
        return meds.stream().filter(m -> m.getStatus() == s).collect(Collectors.toList());
    }

    public List<Medicine> getExpiringSoon() {
        return meds.stream().filter(Medicine::isExpiringSoon).collect(Collectors.toList());
    }

    public List<Medicine> getLowStock(int threshold) {
        return meds.stream()
                .filter(m -> m.getQuantity() < threshold && m.getStatus() == Medicine.Status.APPROVED)
                .collect(Collectors.toList());
    }

    public List<Medicine> getByDonor(String donorId) {
        return meds.stream().filter(m -> donorId.equals(m.getDonorId())).collect(Collectors.toList());
    }

    public List<Medicine> searchMedicines(String query) {
        String q = query.toLowerCase();
        return meds.stream().filter(m ->
            (m.getName() != null && m.getName().toLowerCase().contains(q)) ||
            (m.getCategory() != null && m.getCategory().toLowerCase().contains(q)) ||
            (m.getBatchNumber() != null && m.getBatchNumber().toLowerCase().contains(q))
        ).collect(Collectors.toList());
    }

    public Medicine getMedicineById(String id) {
        return meds.stream().filter(m -> id.equals(m.getId())).findFirst().orElse(null);
    }

    public void addMedicine(Medicine m) {
        m.setId(nextId("medicines", "M"));
        if (m.getSubmittedDate() == null) m.setSubmittedDate(LocalDate.now());

        String sql = """
            INSERT INTO medicines
            (id,name,category,batch_number,expiry_date,quantity,source,donor_id,donor_name,
             status,storage_location,cold_storage,price,condition_info,notes,submitted_date)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString (1,  m.getId());
            ps.setString (2,  m.getName());
            ps.setString (3,  m.getCategory());
            ps.setString (4,  m.getBatchNumber());
            ps.setString (5,  m.getExpiryDate() != null ? m.getExpiryDate().toString() : null);
            ps.setInt    (6,  m.getQuantity());
            ps.setString (7,  m.getSource());
            ps.setString (8,  m.getDonorId());
            ps.setString (9,  m.getDonorName());
            ps.setString (10, m.getStatus().name());
            ps.setString (11, m.getStorageLocation());
            ps.setInt    (12, m.isColdStorage() ? 1 : 0);
            ps.setDouble (13, m.getPrice());
            ps.setString (14, m.getCondition());
            ps.setString (15, m.getNotes());
            ps.setString (16, m.getSubmittedDate().toString());
            ps.executeUpdate();
            meds.add(0, m); // add at top of the list
        } catch (SQLException e) {
            System.err.println("[SQLite] addMedicine: " + e.getMessage());
        }
    }

    public void approveMedicine(String id) {
        updateMedicineStatus(id, "APPROVED");
        meds.stream().filter(m -> id.equals(m.getId())).findFirst().ifPresent(m -> {
            m.setStatus(Medicine.Status.APPROVED);
            // Record it as revenue in transactions
            addTransaction(new Transaction(
                nextTxId(),
                "Medicine Approved: " + m.getName(),
                Type.REVENUE,
                m.getQuantity() * 120.0,
                LocalDate.now(),
                "Completed"
            ));
        });
    }

    public void rejectMedicine(String id) {
        updateMedicineStatus(id, "REJECTED");
        meds.stream().filter(m -> id.equals(m.getId())).findFirst()
                .ifPresent(m -> m.setStatus(Medicine.Status.REJECTED));
    }

    private void updateMedicineStatus(String id, String status) {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE medicines SET status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setString(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[SQLite] updateMedicineStatus: " + e.getMessage());
        }
    }

    public void deleteMedicine(String id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM medicines WHERE id=?")) {
            ps.setString(1, id);
            ps.executeUpdate();
            meds.removeIf(m -> id.equals(m.getId()));
        } catch (SQLException e) {
            System.err.println("[SQLite] deleteMedicine: " + e.getMessage());
        }
    }

    public void updateMedicineQuantity(String id, int qty) {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE medicines SET quantity=? WHERE id=?")) {
            ps.setInt   (1, qty);
            ps.setString(2, id);
            ps.executeUpdate();
            meds.stream().filter(m -> id.equals(m.getId())).findFirst()
                    .ifPresent(m -> m.setQuantity(qty));
        } catch (SQLException e) {
            System.err.println("[SQLite] updateMedicineQuantity: " + e.getMessage());
        }
    }

    public Medicine getNextFifo(String category) {
        // FIFO = First In First Out → return the oldest approved medicine first
        var stream = getApproved().stream().filter(m -> m.getQuantity() > 0);
        if (category != null && !category.isBlank())
            stream = stream.filter(m -> category.equalsIgnoreCase(m.getCategory()));
        return stream.sorted(java.util.Comparator.comparing(
                m -> m.getSubmittedDate() != null ? m.getSubmittedDate() : LocalDate.MIN))
                .findFirst().orElse(null);
    }

    // ────────────────────────────────────────────────────────────────────
    //  TRANSACTIONS
    // ────────────────────────────────────────────────────────────────────

    public ObservableList<Transaction> getAllTransactions() { return txns; }

    public void addTransaction(Transaction t) {
        if (t.getId() == null) t.setId(nextTxId());
        String sql = "INSERT INTO transactions (id,description,type,amount,txn_date,status,notes) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getId());
            ps.setString(2, t.getDescription());
            ps.setString(3, t.getType().name());
            ps.setDouble(4, t.getAmount());
            ps.setString(5, t.getDate() != null ? t.getDate().toString() : LocalDate.now().toString());
            ps.setString(6, t.getStatus());
            ps.setString(7, t.getNotes());
            ps.executeUpdate();
            txns.add(0, t);
        } catch (SQLException e) {
            System.err.println("[SQLite] addTransaction: " + e.getMessage());
        }
    }

    public double getTotalRevenue() {
        return txns.stream().filter(t -> t.getType() == Type.REVENUE).mapToDouble(Transaction::getAmount).sum();
    }

    public double getTotalCosts() {
        return txns.stream().filter(t -> t.getType() == Type.COST).mapToDouble(Transaction::getAmount).sum();
    }

    public double getNetProfit() { return getTotalRevenue() - getTotalCosts(); }

    public List<Transaction> getRecentTransactions(int limit) {
        return txns.stream().limit(limit).collect(Collectors.toList());
    }

    // ────────────────────────────────────────────────────────────────────
    //  PICKUPS
    // ────────────────────────────────────────────────────────────────────

    public ObservableList<Pickup> getAllPickups() { return pickups; }

    public List<Pickup> getTodayPickups() {
        return pickups.stream()
                .filter(p -> LocalDate.now().toString().equals(
                        p.getDate() != null ? p.getDate().toString() : ""))
                .collect(Collectors.toList());
    }

    public List<Pickup> getPickupsByDonor(String donorId) {
        return pickups.stream().filter(p -> donorId.equals(p.getDonorId())).collect(Collectors.toList());
    }

    public void addPickup(Pickup p) {
        p.setId(nextId("pickups", "P"));
        String sql = """
            INSERT INTO pickups
            (id,donor_id,donor_name,address,city,pickup_date,time_slot,estimated_items,rider,route_id,status,notes)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,  p.getId());
            ps.setString(2,  p.getDonorId());
            ps.setString(3,  p.getDonorName());
            ps.setString(4,  p.getAddress());
            ps.setString(5,  p.getCity());
            ps.setString(6,  p.getDate() != null ? p.getDate().toString() : LocalDate.now().toString());
            ps.setString(7,  p.getTimeSlot());
            ps.setInt   (8,  p.getEstimatedItems());
            ps.setString(9,  p.getRider());
            ps.setString(10, p.getRouteId());
            ps.setString(11, p.getStatus().name());
            ps.setString(12, p.getNotes());
            ps.executeUpdate();
            pickups.add(0, p);
        } catch (SQLException e) {
            System.err.println("[SQLite] addPickup: " + e.getMessage());
        }
    }

    public void completePickup(String id) {
        updatePickupStatus(id, Pickup.Status.DONE);
        pickups.stream().filter(p -> id.equals(p.getId())).findFirst().ifPresent(p -> {
            p.setStatus(Pickup.Status.DONE);
            double cost = calculatePickupCost(p);
            addTransaction(new Transaction(null,
                "Pickup completed: " + p.getDonorName() + " (" + p.getEstimatedItems() + " items, " + p.getCity() + ")",
                Type.COST, cost, LocalDate.now(), "Paid"));
        });
    }

    public void cancelPickup(String id) { updatePickupStatus(id, Pickup.Status.CANCELLED); }

    public void updatePickupStatus(String id, Pickup.Status status) {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE pickups SET status=? WHERE id=?")) {
            ps.setString(1, status.name());
            ps.setString(2, id);
            ps.executeUpdate();
            pickups.stream().filter(p -> id.equals(p.getId())).findFirst()
                    .ifPresent(p -> p.setStatus(status));
        } catch (SQLException e) {
            System.err.println("[SQLite] updatePickupStatus: " + e.getMessage());
        }
    }

    public Pickup getPickupById(String id) {
        return pickups.stream().filter(p -> id.equals(p.getId())).findFirst().orElse(null);
    }

    public double calculatePickupCost(Pickup p) {
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
        return pickups.stream()
                .filter(p -> p.getStatus() == Pickup.Status.DONE)
                .mapToDouble(this::calculatePickupCost)
                .sum();
    }

    // ────────────────────────────────────────────────────────────────────
    //  CHARITY REQUESTS
    // ────────────────────────────────────────────────────────────────────

    public ObservableList<CharityRequest> getAllCharityRequests() { return charReqs; }

    public List<CharityRequest> getCharityRequestsByCharity(String charityId) {
        return charReqs.stream().filter(r -> charityId.equals(r.getCharityId())).collect(Collectors.toList());
    }

    public List<CharityRequest> getPendingCharityRequests() {
        return charReqs.stream()
                .filter(r -> r.getStatus() == CharityRequest.Status.PENDING)
                .collect(Collectors.toList());
    }

    public void addCharityRequest(CharityRequest r) {
        r.setId(nextCharityReqId());
        if (r.getRequestDate() == null) r.setRequestDate(LocalDate.now());

        String sql = """
            INSERT INTO charity_requests
            (id,charity_id,charity_name,medicine_category,quantity_requested,urgency,status,request_date,required_by,notes)
            VALUES (?,?,?,?,?,?,?,?,?,?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,  r.getId());
            ps.setString(2,  r.getCharityId());
            ps.setString(3,  r.getCharityName());
            ps.setString(4,  r.getMedicineCategory());
            ps.setInt   (5,  r.getQuantityRequested());
            ps.setString(6,  r.getUrgency());
            ps.setString(7,  r.getStatus().name());
            ps.setString(8,  r.getRequestDate().toString());
            ps.setString(9,  r.getRequiredBy());
            ps.setString(10, r.getNotes());
            ps.executeUpdate();
            charReqs.add(0, r);
        } catch (SQLException e) {
            System.err.println("[SQLite] addCharityRequest: " + e.getMessage());
        }
    }

    public void approveCharityRequest(String id)  { updateCharityStatus(id, CharityRequest.Status.APPROVED);  }
    public void rejectCharityRequest(String id)   { updateCharityStatus(id, CharityRequest.Status.REJECTED);  }
    public void dispatchCharityRequest(String id) { updateCharityStatus(id, CharityRequest.Status.DISPATCHED); }

    private void updateCharityStatus(String id, CharityRequest.Status status) {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE charity_requests SET status=? WHERE id=?")) {
            ps.setString(1, status.name());
            ps.setString(2, id);
            ps.executeUpdate();
            charReqs.stream().filter(r -> id.equals(r.getId())).findFirst()
                    .ifPresent(r -> r.setStatus(status));
        } catch (SQLException e) {
            System.err.println("[SQLite] updateCharityStatus: " + e.getMessage());
        }
    }

    public CharityRequest getRequestById(String id) {
        return charReqs.stream().filter(r -> id.equals(r.getId())).findFirst().orElse(null);
    }

    // ────────────────────────────────────────────────────────────────────
    //  RATINGS
    // ────────────────────────────────────────────────────────────────────

    public void addRating(DataStore.Rating r) {
        String sql = """
            INSERT INTO ratings (id,from_id,from_name,target_id,target_name,category,stars,comment,rating_date)
            VALUES (?,?,?,?,?,?,?,?,?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "RAT" + System.currentTimeMillis());
            ps.setString(2, r.fromId);
            ps.setString(3, r.fromName);
            ps.setString(4, r.targetId);
            ps.setString(5, r.targetName);
            ps.setString(6, r.category);
            ps.setInt   (7, r.stars);
            ps.setString(8, r.comment);
            ps.setString(9, LocalDate.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[SQLite] addRating: " + e.getMessage());
        }
    }

    public List<DataStore.Rating> getAllRatings() {
        List<DataStore.Rating> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM ratings ORDER BY rating_date DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new DataStore.Rating(
                    rs.getString("from_id"),   rs.getString("from_name"),
                    rs.getString("target_id"), rs.getString("target_name"),
                    rs.getString("category"),  rs.getInt("stars"),
                    rs.getString("comment")));
            }
        } catch (SQLException e) {
            System.err.println("[SQLite] getAllRatings: " + e.getMessage());
        }
        return list;
    }

    public double getAvgRating(String targetId) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT AVG(stars) FROM ratings WHERE target_id=?")) {
            ps.setString(1, targetId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("[SQLite] getAvgRating: " + e.getMessage());
        }
        return 0.0;
    }

    public double getOverallAvgRating() {
        try (PreparedStatement ps = conn.prepareStatement("SELECT AVG(stars) FROM ratings");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) { double v = rs.getDouble(1); return v > 0 ? v : 4.7; }
        } catch (SQLException e) {
            System.err.println("[SQLite] getOverallAvgRating: " + e.getMessage());
        }
        return 4.7;
    }

    // ────────────────────────────────────────────────────────────────────
    //  AUDIT LOG
    // ────────────────────────────────────────────────────────────────────

    public void logAudit(String userId, String userName, String action, String detail, String category) {
        String sql = "INSERT INTO audit_log (id,log_time,user_id,user_name,action,detail,category) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "AL" + System.currentTimeMillis());
            ps.setString(2, LocalDate.now().toString());
            ps.setString(3, userId);
            ps.setString(4, userName);
            ps.setString(5, action);
            ps.setString(6, detail);
            ps.setString(7, category);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[SQLite] logAudit: " + e.getMessage());
        }
    }

    public List<AuditLog.Entry> getAuditLog(int limit) {
        List<AuditLog.Entry> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM audit_log ORDER BY log_time DESC LIMIT ?")) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new AuditLog.Entry(
                    rs.getString("user_id"),   rs.getString("user_name"),
                    rs.getString("action"),    rs.getString("detail"),
                    rs.getString("category")));
            }
        } catch (SQLException e) {
            System.err.println("[SQLite] getAuditLog: " + e.getMessage());
        }
        return list;
    }

    public int getAuditCount() {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM audit_log")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[SQLite] getAuditCount: " + e.getMessage());
        }
        return 0;
    }

    // ────────────────────────────────────────────────────────────────────
    //  SYSTEM SETTINGS
    // ────────────────────────────────────────────────────────────────────

    public boolean getSetting(String key, boolean defaultVal) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT setting_value FROM system_settings WHERE setting_key=?")) {
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Boolean.parseBoolean(rs.getString(1));
        } catch (SQLException e) {
            System.err.println("[SQLite] getSetting: " + e.getMessage());
        }
        return defaultVal;
    }

    public void setSetting(String key, boolean value) {
        // "INSERT OR REPLACE" = insert if new, update if already exists
        String sql = "INSERT OR REPLACE INTO system_settings (setting_key,setting_value,updated_at) VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, String.valueOf(value));
            ps.setString(3, LocalDate.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[SQLite] setSetting: " + e.getMessage());
        }
    }

    public boolean isFifoEnabled()        { return getSetting("fifo_enabled", true); }
    public void setFifoEnabled(boolean v)  { setSetting("fifo_enabled", v); }

    // ────────────────────────────────────────────────────────────────────
    //  SUMMARY STATS
    // ────────────────────────────────────────────────────────────────────

    public int getTotalDonors()    { return (int) users.stream().filter(u -> !"admin".equalsIgnoreCase(u.getRole())).count(); }
    public int getHouseholdCount() { return getUsersByRole("household").size(); }
    public int getPharmacyCount()  { return getUsersByRole("pharmacy").size();  }
    public int getCharityCount()   { return getUsersByRole("charity").size();   }

    // ────────────────────────────────────────────────────────────────────
    //  LEGACY COMPAT (used in dashboard cards)
    // ────────────────────────────────────────────────────────────────────

    public List<Pickup>         getPickupsAsArray()         { return getTodayPickups().stream().limit(6).collect(Collectors.toList()); }
    public List<CharityRequest> getCharityRequestsAsArray() { return charReqs.stream().limit(6).collect(Collectors.toList()); }

    // ────────────────────────────────────────────────────────────────────
    //  RESULT SET MAPPERS — convert database row → Java object
    // ────────────────────────────────────────────────────────────────────

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getString("id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setRole(rs.getString("role"));
        u.setOrganization(rs.getString("organisation"));
        u.setPhone(rs.getString("phone"));
        u.setCity(rs.getString("city"));
        u.setAddress(rs.getString("address"));
        u.setPoints(rs.getInt("points"));
        u.setStatus(rs.getString("status"));
        u.setJoinDate(rs.getString("join_date"));
        return u;
    }

    private Medicine mapMedicine(ResultSet rs) throws SQLException {
        Medicine m = new Medicine();
        m.setId(rs.getString("id"));
        m.setName(rs.getString("name"));
        m.setCategory(rs.getString("category"));
        m.setBatchNumber(rs.getString("batch_number"));
        String exp = rs.getString("expiry_date");
        if (exp != null) m.setExpiryDate(LocalDate.parse(exp));
        m.setQuantity(rs.getInt("quantity"));
        m.setSource(rs.getString("source"));
        m.setDonorId(rs.getString("donor_id"));
        m.setDonorName(rs.getString("donor_name"));
        try { m.setStatus(Medicine.Status.valueOf(rs.getString("status"))); }
        catch (Exception e) { m.setStatus(Medicine.Status.PENDING); }
        m.setStorageLocation(rs.getString("storage_location"));
        m.setColdStorage(rs.getInt("cold_storage") == 1);
        m.setPrice(rs.getDouble("price"));
        m.setCondition(rs.getString("condition_info"));
        m.setNotes(rs.getString("notes"));
        String sub = rs.getString("submitted_date");
        if (sub != null) m.setSubmittedDate(LocalDate.parse(sub));
        return m;
    }

    private Transaction mapTransaction(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setId(rs.getString("id"));
        t.setDescription(rs.getString("description"));
        try { t.setType(Type.valueOf(rs.getString("type"))); }
        catch (Exception e) { t.setType(Type.REVENUE); }
        t.setAmount(rs.getDouble("amount"));
        String d = rs.getString("txn_date");
        if (d != null) t.setDate(LocalDate.parse(d));
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
        String d = rs.getString("pickup_date");
        if (d != null) p.setDate(LocalDate.parse(d));
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
        String d = rs.getString("request_date");
        if (d != null) r.setRequestDate(LocalDate.parse(d));
        r.setRequiredBy(rs.getString("required_by"));
        r.setMedicineId(rs.getString("medicine_id"));
        r.setAssignedMedicineName(rs.getString("assigned_medicine"));
        r.setNotes(rs.getString("notes"));
        return r;
    }

    // ────────────────────────────────────────────────────────────────────
    //  ID GENERATORS
    //  These create IDs like U001, M001, P001 etc.
    // ────────────────────────────────────────────────────────────────────

    private String nextId(String table, String prefix) {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            if (rs.next()) return prefix + String.format("%03d", rs.getInt(1) + 1);
        } catch (SQLException e) {
            System.err.println("[SQLite] nextId: " + e.getMessage());
        }
        return prefix + System.currentTimeMillis();
    }

    private String nextTxId() {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM transactions")) {
            if (rs.next()) return "#T" + String.format("%04d", rs.getInt(1) + 1);
        } catch (SQLException e) {
            System.err.println("[SQLite] nextTxId: " + e.getMessage());
        }
        return "#T" + System.currentTimeMillis();
    }

    private String nextCharityReqId() {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM charity_requests")) {
            if (rs.next()) return "REQ" + String.format("%04d", rs.getInt(1) + 1);
        } catch (SQLException e) {
            System.err.println("[SQLite] nextCharityReqId: " + e.getMessage());
        }
        return "REQ" + System.currentTimeMillis();
    }

    // ────────────────────────────────────────────────────────────────────
    //  SEEDING (used by DataStore when DB is empty)
    // ────────────────────────────────────────────────────────────────────

    /** Insert user with already-hashed password (used when seeding demo data) */
    public void registerUserHashed(User user) {
        String sql = "INSERT OR IGNORE INTO users (id,name,email,password,role,organisation,phone,city,address,points,status,join_date) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,  user.getId());
            ps.setString(2,  user.getName());
            ps.setString(3,  user.getEmail());
            ps.setString(4,  user.getPassword());
            ps.setString(5,  user.getRole());
            ps.setString(6,  user.getOrganization());
            ps.setString(7,  user.getPhone());
            ps.setString(8,  user.getCity());
            ps.setString(9,  user.getAddress());
            ps.setInt   (10, user.getPoints());
            ps.setString(11, user.getStatus() != null ? user.getStatus() : "active");
            ps.setString(12, LocalDate.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[SQLite] registerUserHashed: " + e.getMessage());
        }
    }

    public void addMedicineRaw(Medicine m) {
        String sql = "INSERT OR IGNORE INTO medicines (id,name,category,batch_number,expiry_date,quantity,source,donor_id,donor_name,status,storage_location,cold_storage,price,condition_info,notes,submitted_date) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString (1,  m.getId());
            ps.setString (2,  m.getName());
            ps.setString (3,  m.getCategory());
            ps.setString (4,  m.getBatchNumber());
            ps.setString (5,  m.getExpiryDate() != null ? m.getExpiryDate().toString() : null);
            ps.setInt    (6,  m.getQuantity());
            ps.setString (7,  m.getSource());
            ps.setString (8,  m.getDonorId());
            ps.setString (9,  m.getDonorName());
            ps.setString (10, m.getStatus().name());
            ps.setString (11, m.getStorageLocation());
            ps.setInt    (12, m.isColdStorage() ? 1 : 0);
            ps.setDouble (13, m.getPrice());
            ps.setString (14, m.getCondition());
            ps.setString (15, m.getNotes());
            ps.setString (16, m.getSubmittedDate() != null ? m.getSubmittedDate().toString() : LocalDate.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[SQLite] addMedicineRaw: " + e.getMessage());
        }
    }

    public void addTransactionRaw(Transaction t) {
        String sql = "INSERT OR IGNORE INTO transactions (id,description,type,amount,txn_date,status,notes) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getId());
            ps.setString(2, t.getDescription());
            ps.setString(3, t.getType().name());
            ps.setDouble(4, t.getAmount());
            ps.setString(5, t.getDate() != null ? t.getDate().toString() : LocalDate.now().toString());
            ps.setString(6, t.getStatus());
            ps.setString(7, t.getNotes());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[SQLite] addTransactionRaw: " + e.getMessage());
        }
    }

    public void addPickupRaw(Pickup p) {
        String sql = "INSERT OR IGNORE INTO pickups (id,donor_id,donor_name,address,city,pickup_date,time_slot,estimated_items,rider,route_id,status,notes) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,  p.getId());
            ps.setString(2,  p.getDonorId());
            ps.setString(3,  p.getDonorName());
            ps.setString(4,  p.getAddress());
            ps.setString(5,  p.getCity());
            ps.setString(6,  p.getDate() != null ? p.getDate().toString() : LocalDate.now().toString());
            ps.setString(7,  p.getTimeSlot());
            ps.setInt   (8,  p.getEstimatedItems());
            ps.setString(9,  p.getRider());
            ps.setString(10, p.getRouteId());
            ps.setString(11, p.getStatus().name());
            ps.setString(12, p.getNotes());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[SQLite] addPickupRaw: " + e.getMessage());
        }
    }

    public void addCharityRequestRaw(CharityRequest r) {
        String sql = "INSERT OR IGNORE INTO charity_requests (id,charity_id,charity_name,medicine_category,quantity_requested,urgency,status,request_date,required_by,notes) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,  r.getId());
            ps.setString(2,  r.getCharityId());
            ps.setString(3,  r.getCharityName());
            ps.setString(4,  r.getMedicineCategory());
            ps.setInt   (5,  r.getQuantityRequested());
            ps.setString(6,  r.getUrgency());
            ps.setString(7,  r.getStatus().name());
            ps.setString(8,  r.getRequestDate() != null ? r.getRequestDate().toString() : LocalDate.now().toString());
            ps.setString(9,  r.getRequiredBy());
            ps.setString(10, r.getNotes());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[SQLite] addCharityRequestRaw: " + e.getMessage());
        }
    }

    public void reload() { loadAll(); }
}
