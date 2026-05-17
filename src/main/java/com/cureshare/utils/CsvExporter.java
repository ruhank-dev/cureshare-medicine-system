package com.cureshare.utils;

import com.cureshare.models.*;
import com.cureshare.utils.AuditLog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * CsvExporter — writes real CSV files to disk via a FileChooser dialog.
 * Every method opens a Save dialog, lets the user pick a path, writes the
 * file, and returns the path (or null if the user cancelled).
 */
public class CsvExporter {

    private static final DataStore db = DataStore.getInstance();

    // ── PUBLIC EXPORT METHODS ─────────────────────────────────────────────

    /** Exports all medicines to CSV. Returns saved path or null. */
    public static String exportMedicines(Stage owner) {
        File f = chooseSave(owner, "medicines_export_" + today(), "Medicines CSV");
        if (f == null) return null;
        try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            pw.println("ID,Name,Category,Batch Number,Expiry Date,Quantity,Source,Donor,Status,Storage Location,Cold Storage,Price,Submitted Date");
            for (Medicine m : db.getAllMedicines()) {
                pw.println(csv(
                    m.getId(), m.getName(), m.getCategory(), m.getBatchNumber(),
                    m.getExpiryDate() != null ? m.getExpiryDate().toString() : "",
                    String.valueOf(m.getQuantity()), m.getSource(),
                    m.getDonorName() != null ? m.getDonorName() : m.getDonorId(),
                    m.getStatusLabel(),
                    m.getStorageLocation() != null ? m.getStorageLocation() : "",
                    m.isColdStorage() ? "Yes" : "No",
                    m.getPrice() > 0 ? String.format("%.2f", m.getPrice()) : "",
                    m.getSubmittedDate() != null ? m.getSubmittedDate().toString() : ""
                ));
            }
            return f.getAbsolutePath();
        } catch (Exception e) {
            showError("Could not write file: " + e.getMessage());
            return null;
        }
    }

    /** Exports inventory (approved medicines) to CSV. */
    public static String exportInventory(Stage owner) {
        File f = chooseSave(owner, "inventory_" + today(), "Inventory CSV");
        if (f == null) return null;
        try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            pw.println("ID,Name,Category,Batch,Expiry,Days Until Expiry,Quantity,Storage Location,Cold Storage,Status,Price/Unit");
            for (Medicine m : db.getAllMedicines()) {
                pw.println(csv(
                    m.getId(), m.getName(),
                    m.getCategory() != null ? m.getCategory() : "",
                    m.getBatchNumber() != null ? m.getBatchNumber() : "",
                    m.getExpiryDate() != null ? m.getExpiryDate().toString() : "",
                    String.valueOf(m.daysUntilExpiry()),
                    String.valueOf(m.getQuantity()),
                    m.getStorageLocation() != null ? m.getStorageLocation() : "Unassigned",
                    m.isColdStorage() ? "Yes" : "No",
                    m.getStatusLabel(),
                    m.getPrice() > 0 ? String.format("%.2f", m.getPrice()) : "0.00"
                ));
            }
            return f.getAbsolutePath();
        } catch (Exception e) {
            showError("Could not write file: " + e.getMessage());
            return null;
        }
    }

    /** Exports all financial transactions to CSV. */
    public static String exportTransactions(Stage owner) {
        File f = chooseSave(owner, "transactions_" + today(), "Transactions CSV");
        if (f == null) return null;
        try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            pw.println("Ref ID,Description,Type,Amount (PKR),Date,Status");
            for (Transaction t : db.getAllTransactions()) {
                double signed = t.getType() == Transaction.Type.COST ? -t.getAmount() : t.getAmount();
                pw.println(csv(
                    t.getId() != null ? t.getId() : "",
                    t.getDescription(),
                    t.getTypeLabel(),
                    String.format("%.2f", signed),
                    t.getDate().toString(),
                    t.getStatus()
                ));
            }
            return f.getAbsolutePath();
        } catch (Exception e) {
            showError("Could not write file: " + e.getMessage());
            return null;
        }
    }

    /** Exports all users (donors, pharmacies, charities) to CSV. */
    public static String exportUsers(Stage owner) {
        File f = chooseSave(owner, "users_" + today(), "Users CSV");
        if (f == null) return null;
        try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            pw.println("ID,Name,Email,Role,Phone,City,Organisation,Points,Status,Joined");
            for (User u : db.getAllUsers()) {
                pw.println(csv(
                    u.getId(), u.getName(), u.getEmail(), u.getRole(),
                    u.getPhone() != null ? u.getPhone() : "",
                    u.getCity() != null ? u.getCity() : "",
                    u.getOrganization() != null ? u.getOrganization() : "",
                    String.valueOf(u.getPoints()),
                    u.getStatus(),
                    u.getJoinDate() != null ? u.getJoinDate() : ""
                ));
            }
            return f.getAbsolutePath();
        } catch (Exception e) {
            showError("Could not write file: " + e.getMessage());
            return null;
        }
    }

    /** Exports all pickups to CSV. */
    public static String exportPickups(Stage owner) {
        File f = chooseSave(owner, "pickups_" + today(), "Pickups CSV");
        if (f == null) return null;
        try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            pw.println("ID,Donor,Address,City,Date,Time Slot,Estimated Items,Rider,Route,Status");
            for (Pickup p : db.getAllPickups()) {
                pw.println(csv(
                    p.getId(), p.getDonorName(), p.getAddress(),
                    p.getCity() != null ? p.getCity() : "",
                    p.getDate().toString(), p.getTimeSlot(),
                    String.valueOf(p.getEstimatedItems()),
                    p.getRider() != null ? p.getRider() : "Unassigned",
                    p.getRouteId() != null ? p.getRouteId() : "",
                    p.getStatusLabel().replace("_", " ")
                ));
            }
            return f.getAbsolutePath();
        } catch (Exception e) {
            showError("Could not write file: " + e.getMessage());
            return null;
        }
    }

    /** Exports all charity requests to CSV. */
    public static String exportCharityRequests(Stage owner) {
        File f = chooseSave(owner, "charity_requests_" + today(), "Charity Requests CSV");
        if (f == null) return null;
        try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            pw.println("ID,Organisation,Medicine Category,Quantity,Urgency,Request Date,Status");
            for (CharityRequest r : db.getAllCharityRequests()) {
                pw.println(csv(
                    r.getId(), r.getCharityName(), r.getMedicineCategory(),
                    String.valueOf(r.getQuantityRequested()),
                    r.getUrgency() != null ? r.getUrgency() : "",
                    r.getRequestDate().toString(),
                    r.getStatusLabel()
                ));
            }
            return f.getAbsolutePath();
        } catch (Exception e) {
            showError("Could not write file: " + e.getMessage());
            return null;
        }
    }

    /** Exports medicines for a specific donor (Household/Pharmacy view). */
    public static String exportMySubmissions(Stage owner, String donorId) {
        File f = chooseSave(owner, "my_submissions_" + today(), "My Submissions CSV");
        if (f == null) return null;
        try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            pw.println("ID,Name,Category,Batch,Expiry,Quantity,Status,Submitted Date");
            for (Medicine m : db.getByDonor(donorId)) {
                pw.println(csv(
                    m.getId(), m.getName(),
                    m.getCategory() != null ? m.getCategory() : "",
                    m.getBatchNumber() != null ? m.getBatchNumber() : "",
                    m.getExpiryDate() != null ? m.getExpiryDate().toString() : "",
                    String.valueOf(m.getQuantity()),
                    m.getStatusLabel(),
                    m.getSubmittedDate() != null ? m.getSubmittedDate().toString() : ""
                ));
            }
            return f.getAbsolutePath();
        } catch (Exception e) {
            showError("Could not write file: " + e.getMessage());
            return null;
        }
    }

    /** Exports charity requests for a specific charity user. */
    public static String exportMyRequests(Stage owner, String charityId) {
        File f = chooseSave(owner, "my_requests_" + today(), "My Requests CSV");
        if (f == null) return null;
        try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            pw.println("ID,Category,Quantity,Urgency,Request Date,Status");
            for (CharityRequest r : db.getCharityRequestsByCharity(charityId)) {
                pw.println(csv(
                    r.getId(), r.getMedicineCategory(),
                    String.valueOf(r.getQuantityRequested()),
                    r.getUrgency() != null ? r.getUrgency() : "",
                    r.getRequestDate().toString(),
                    r.getStatusLabel()
                ));
            }
            return f.getAbsolutePath();
        } catch (Exception e) {
            showError("Could not write file: " + e.getMessage());
            return null;
        }
    }

    // ── RECEIPT ──────────────────────────────────────────────────────────

    /** Saves a pickup receipt as a plain-text .txt file. */
    public static String savePickupReceipt(Stage owner, Pickup p) {
        File f = chooseSaveTxt(owner, "pickup_receipt_" + p.getId() + "_" + today());
        if (f == null) return null;
        try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            pw.println("===============================================");
            pw.println("         CURESHARE BMS — PICKUP RECEIPT");
            pw.println("===============================================");
            pw.println("Receipt No : RCP-" + p.getId() + "-" + today());
            pw.println("Date       : " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
            pw.println("-----------------------------------------------");
            pw.println("Donor      : " + p.getDonorName());
            pw.println("Address    : " + p.getAddress());
            pw.println("City       : " + (p.getCity() != null ? p.getCity() : "—"));
            pw.println("-----------------------------------------------");
            pw.println("Pickup ID  : " + p.getId());
            pw.println("Date       : " + p.getDate());
            pw.println("Time Slot  : " + p.getTimeSlot());
            pw.println("Items      : " + p.getEstimatedItems());
            pw.println("Rider      : " + (p.getRider() != null ? p.getRider() : "—"));
            pw.println("Route      : " + (p.getRouteId() != null ? p.getRouteId() : "—"));
            pw.println("Status     : " + p.getStatusLabel().replace("_", " "));
            pw.println("-----------------------------------------------");
            pw.println("Thank you for contributing to CureShare!");
            pw.println("Your donation helps communities in need.");
            pw.println("===============================================");
            return f.getAbsolutePath();
        } catch (Exception e) {
            showError("Could not write receipt: " + e.getMessage());
            return null;
        }
    }

    // ── HELPERS ───────────────────────────────────────────────────────────

    private static File chooseSave(Stage owner, String defaultName, String description) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save " + description);
        fc.setInitialFileName(defaultName + ".csv");
        fc.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        // Default to user's home/Documents if available
        File docs = new File(System.getProperty("user.home"), "Documents");
        if (docs.exists()) fc.setInitialDirectory(docs);
        return fc.showSaveDialog(owner);
    }

    private static File chooseSaveTxt(Stage owner, String defaultName) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Receipt");
        fc.setInitialFileName(defaultName + ".txt");
        fc.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Text Files", "*.txt"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File docs = new File(System.getProperty("user.home"), "Documents");
        if (docs.exists()) fc.setInitialDirectory(docs);
        return fc.showSaveDialog(owner);
    }

    /** Export the live audit log to CSV */
    public static String exportAuditLog(Stage owner, java.util.List<AuditLog.Entry> entries) {
        File file = chooseSave(owner, "audit_log_" + today() + ".csv", "Audit Log CSV");
        if (file == null) return null;
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("AuditLog ID,Timestamp,User ID,User Name,Action,Detail,Category");
            for (AuditLog.Entry e : entries) {
                pw.println(csv(e.getId(), e.getTimestamp(), e.getUserId(),
                    e.getUserName(), e.getAction(), e.getDetail(), e.getCategory()));
            }
            return file.getAbsolutePath();
        } catch (Exception ex) {
            showError("Could not save audit log: " + ex.getMessage());
            return null;
        }
    }

    /** Wraps fields in quotes, escaping any internal quotes. */
    private static String csv(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append(',');
            String f = fields[i] != null ? fields[i] : "";
            if (f.contains(",") || f.contains("\"") || f.contains("\n")) {
                sb.append('"').append(f.replace("\"", "\"\"")).append('"');
            } else {
                sb.append(f);
            }
        }
        return sb.toString();
    }

    private static String today() {
        return LocalDate.now().toString();
    }

    private static void showError(String msg) {
        javafx.application.Platform.runLater(() ->
            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR,
                msg, javafx.scene.control.ButtonType.OK).showAndWait()
        );
    }
}
