package com.cureshare.views.dashboard;

import com.cureshare.models.*;
import com.cureshare.models.Transaction.Type;
import com.cureshare.utils.DataStore;
import com.cureshare.utils.Theme;
import com.cureshare.views.shared.UIComponents;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ReportViewer — generates and displays real reports inside the app.
 * Each report renders in a scrollable in-app window with a Save button.
 */
public class ReportViewer {

    private static final DataStore db = DataStore.getInstance();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    // ── ENTRY POINTS ─────────────────────────────────────────────────────

    public static void showInventoryReport(Stage owner) {
        String title = "📦 Inventory Report — " + LocalDate.now().format(FMT);
        VBox content = buildInventoryReport();
        String html   = generateInventoryHtml();
        openReportWindow(owner, title, content, html, "Inventory_Report");
    }

    public static void showFinancialReport(Stage owner) {
        String title = "💰 Financial Statement — " + LocalDate.now().format(FMT);
        VBox content = buildFinancialReport();
        String html   = generateFinancialHtml();
        openReportWindow(owner, title, content, html, "Financial_Statement");
    }

    public static void showDonationImpactReport(Stage owner) {
        String title = "❤️ Donation Impact Report — " + LocalDate.now().format(FMT);
        VBox content = buildDonationImpactReport();
        String html   = generateDonationHtml();
        openReportWindow(owner, title, content, html, "Donation_Impact_Report");
    }

    public static void showPartnerPerformanceReport(Stage owner) {
        String title = "🏥 Partner Performance — " + LocalDate.now().format(FMT);
        VBox content = buildPartnerPerformanceReport();
        String html   = generatePartnerHtml();
        openReportWindow(owner, title, content, html, "Partner_Performance");
    }

    public static void showLogisticsReport(Stage owner) {
        String title = "🚗 Logistics Report — " + LocalDate.now().format(FMT);
        VBox content = buildLogisticsReport();
        String html   = generateLogisticsHtml();
        openReportWindow(owner, title, content, html, "Logistics_Report");
    }

    public static void showComplianceReport(Stage owner) {
        String title = "🛡️ Compliance Report — " + LocalDate.now().format(FMT);
        VBox content = buildComplianceReport();
        String html   = generateComplianceHtml();
        openReportWindow(owner, title, content, html, "Compliance_Report");
    }

    // ── WINDOW BUILDER ────────────────────────────────────────────────────

    private static void openReportWindow(Stage owner, String title, VBox content, String html, String fileBase) {
        Stage dlg = new Stage();
        dlg.initOwner(owner);
        dlg.initModality(Modality.NONE);
        dlg.setTitle(title);
        dlg.setMinWidth(820);
        dlg.setMinHeight(640);
        dlg.setWidth(900);
        dlg.setHeight(700);

        // Header bar
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 24, 16, 24));
        header.setStyle("-fx-background-color:rgba(255,255,255,0.92);-fx-border-color:transparent transparent rgba(0,176,144,0.18) transparent;-fx-border-width:0 0 1 0;-fx-effect:dropshadow(gaussian,rgba(0,120,96,0.08),10,0,0,2);");

        // Colored icon box
        StackPane iconBox = new StackPane();
        Rectangle iconBg = new Rectangle(40, 40); iconBg.setArcWidth(12); iconBg.setArcHeight(12);
        iconBg.setFill(Color.web(Theme.TEAL_500, 0.12));
        Label iconLbl = new Label(title.substring(0,2)); iconLbl.setStyle("-fx-font-size:18px;");
        iconBox.getChildren().addAll(iconBg, iconLbl);

        VBox titleBox = new VBox(2); HBox.setHgrow(titleBox, Priority.ALWAYS);
        Label titleLbl = new Label(title.substring(3));
        titleLbl.setStyle("-fx-font-family:'"+Theme.FONT_HEADING+"';-fx-font-size:16px;-fx-font-weight:700;-fx-text-fill:"+Theme.TEXT_PRIMARY+";");
        Label dateLbl = new Label("Generated: " + LocalDate.now().format(FMT) + "  •  CureShare BMS");
        dateLbl.setStyle(Theme.labelStyle("11.5px", Theme.TEXT_MUTED, false));
        titleBox.getChildren().addAll(titleLbl, dateLbl);

        // Action buttons
        Button saveBtn = UIComponents.primaryButton("💾 Save as HTML");
        saveBtn.setOnAction(e -> saveHtmlFile(dlg, html, fileBase));

        Button printBtn = UIComponents.glassButton("🖨 Print / PDF");
        printBtn.setOnAction(e -> printHtml(dlg, html, fileBase));

        Button closeBtn = UIComponents.glassButton("✕ Close");
        closeBtn.setOnAction(e -> dlg.close());

        header.getChildren().addAll(iconBox, titleBox, saveBtn, printBtn, closeBtn);

        // Content scroll
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:transparent;-fx-background-color:#f0faf7;-fx-border-width:0;");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:#f0faf7;");
        root.setTop(header);
        root.setCenter(scroll);

        dlg.setScene(new Scene(root));
        dlg.show();
    }

    private static void saveHtmlFile(Stage owner, String html, String fileBase) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Report as HTML");
        fc.setInitialFileName(fileBase + "_" + LocalDate.now() + ".html");
        fc.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("HTML File", "*.html"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File docs = new File(System.getProperty("user.home"), "Documents");
        if (docs.exists()) fc.setInitialDirectory(docs);
        File f = fc.showSaveDialog(owner);
        if (f != null) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
                pw.print(html);
                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setTitle("Saved");
                ok.setHeaderText("Report saved successfully");
                ok.setContentText(f.getAbsolutePath());
                ok.showAndWait();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Could not save file: " + ex.getMessage()).showAndWait();
            }
        }
    }

    /**
     * Prints the report using JavaFX WebEngine — no browser involved.
     * Opens the OS native print dialog (which supports Save as PDF on
     * Windows 10+, macOS, and Linux via CUPS).
     */
    private static void printHtml(Stage owner, String html, String fileBase) {
        WebView webView = new WebView();
        webView.setVisible(false);
        webView.setPrefSize(800, 600);
        WebEngine engine = webView.getEngine();

        Stage hiddenStage = new Stage();
        hiddenStage.initOwner(owner);
        hiddenStage.initModality(Modality.NONE);
        StackPane hiddenRoot = new StackPane(webView);
        hiddenStage.setScene(new Scene(hiddenRoot, 800, 600));
        hiddenStage.setOpacity(0);
        hiddenStage.show();

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                // Must use Platform.runLater to escape the animation/layout pulse —
                // PrinterJob.showPrintDialog() is illegal during layout processing
                javafx.application.Platform.runLater(() -> {
                    try {
                        PrinterJob job = PrinterJob.createPrinterJob();
                        if (job != null) {
                            boolean proceed = job.showPrintDialog(owner);
                            if (proceed) {
                                engine.print(job);
                                job.endJob();
                            }
                        } else {
                            new Alert(Alert.AlertType.WARNING,
                                "No printer found.\nUse 'Save as HTML' and open in a browser to print/save as PDF.")
                                .showAndWait();
                        }
                    } finally {
                        hiddenStage.close();
                    }
                });
            }
        });

        engine.loadContent(html, "text/html");
    }

    // ══════════════════════════════════════════════════════════════════════
    //  REPORT 1 — INVENTORY
    // ══════════════════════════════════════════════════════════════════════

    private static VBox buildInventoryReport() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));
        List<Medicine> all       = db.getAllMedicines();
        List<Medicine> approved  = db.getApproved();
        List<Medicine> pending   = db.getPending();
        List<Medicine> expiring  = db.getExpiringSoon();
        List<Medicine> lowStock  = db.getLowStock(50);

        // Summary KPIs
        page.getChildren().add(sectionTitle("Inventory Summary"));
        GridPane kpi = kpiGrid();
        kpi.add(kpiBox("📦", String.valueOf(all.size()), "Total SKUs"), 0, 0);
        kpi.add(kpiBox("✅", String.valueOf(approved.size()), "Approved"), 1, 0);
        kpi.add(kpiBox("⏳", String.valueOf(pending.size()), "Pending"), 2, 0);
        kpi.add(kpiBox("⚠️", String.valueOf(expiring.size()), "Expiring Soon"), 3, 0);
        page.getChildren().add(kpi);

        // Category breakdown
        page.getChildren().add(sectionTitle("Stock by Category"));
        Map<String, Long> byCat = all.stream()
            .collect(Collectors.groupingBy(m -> m.getCategory() != null ? m.getCategory() : "Other", Collectors.counting()));
        VBox catCard = reportCard();
        catCard.getChildren().add(tableHeader("Category", "Count", "Total Units", "Status"));
        byCat.forEach((cat, count) -> {
            int units = all.stream().filter(m -> cat.equals(m.getCategory())).mapToInt(Medicine::getQuantity).sum();
            catCard.getChildren().add(tableRow(cat, String.valueOf(count), String.valueOf(units), "Active"));
        });
        page.getChildren().add(catCard);

        // Full medicine list
        page.getChildren().add(sectionTitle("Complete Medicine List"));
        VBox medCard = reportCard();
        medCard.getChildren().add(tableHeader("Medicine", "Batch", "Expiry", "Qty", "Source", "Status"));
        for (Medicine m : all) {
            String expColor = m.isExpiringSoon() ? Theme.WARNING : m.isExpired() ? Theme.DANGER : Theme.TEXT_SECONDARY;
            HBox row = tableRowFull(
                m.getName(), m.getBatchNumber() != null ? m.getBatchNumber() : "—",
                m.getExpiryDate() != null ? m.getExpiryDate().format(FMT) : "—",
                String.valueOf(m.getQuantity()),
                m.getSource() != null ? m.getSource() : "—",
                m.getStatusLabel()
            );
            if (m.isExpiringSoon()) colorRowBg(row, "rgba(240,165,0,0.05)");
            medCard.getChildren().add(row);
        }
        page.getChildren().add(medCard);

        // Alerts
        if (!expiring.isEmpty()) {
            page.getChildren().add(sectionTitle("⚠️ Expiry Alerts (" + expiring.size() + " items)"));
            VBox alertCard = reportCard();
            alertCard.setStyle(alertCard.getStyle() + "-fx-border-color:" + Theme.WARNING + ";-fx-border-width:0 0 0 4;");
            alertCard.getChildren().add(tableHeader("Medicine", "Batch", "Expiry", "Days Left", "Qty"));
            for (Medicine m : expiring) {
                alertCard.getChildren().add(tableRowFull(
                    m.getName(), m.getBatchNumber() != null ? m.getBatchNumber() : "—",
                    m.getExpiryDate().format(FMT), m.daysUntilExpiry() + " days",
                    m.getQuantity() + " units", null));
            }
            page.getChildren().add(alertCard);
        }

        if (!lowStock.isEmpty()) {
            page.getChildren().add(sectionTitle("📉 Low Stock (" + lowStock.size() + " items below 50 units)"));
            VBox lsCard = reportCard();
            lsCard.setStyle(lsCard.getStyle() + "-fx-border-color:" + Theme.DANGER + ";-fx-border-width:0 0 0 4;");
            lsCard.getChildren().add(tableHeader("Medicine", "Category", "Current Qty", "Recommended Action"));
            for (Medicine m : lowStock) {
                lsCard.getChildren().add(tableRowFull(m.getName(), m.getCategory() != null ? m.getCategory() : "—",
                    m.getQuantity() + " units", "Request restock from donors", null, null));
            }
            page.getChildren().add(lsCard);
        }

        page.getChildren().add(reportFooter());
        return page;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  REPORT 2 — FINANCIAL
    // ══════════════════════════════════════════════════════════════════════

    private static VBox buildFinancialReport() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));
        List<Transaction> txs = db.getAllTransactions();
        double rev   = db.getTotalRevenue();
        double costs = db.getTotalCosts();
        double profit = db.getNetProfit();

        page.getChildren().add(sectionTitle("Financial Summary — March 2026"));
        GridPane kpi = kpiGrid();
        kpi.add(kpiBox("💰", String.format("₨%,.0f", rev),    "Total Revenue"),  0, 0);
        kpi.add(kpiBox("📤", String.format("₨%,.0f", costs),  "Total Costs"),    1, 0);
        kpi.add(kpiBox("📈", String.format("₨%,.0f", profit), "Net Profit"),     2, 0);
        kpi.add(kpiBox("🎁", "₨21,000",                        "Donation Value"), 3, 0);
        page.getChildren().add(kpi);

        // Revenue breakdown
        page.getChildren().add(sectionTitle("Revenue by Source"));
        VBox revCard = reportCard();
        revCard.getChildren().add(tableHeader("Source", "Amount (₨)", "% of Total"));
        String[][] revRows = {
            {"Discount Resale",  "138,000", String.format("%.1f%%", 138000/rev*100)},
            {"Bulk Pharma",       "41,000", String.format("%.1f%%",  41000/rev*100)},
            {"Logistics Fees",     "7,000", String.format("%.1f%%",   7000/rev*100)},
            {"CSR Grants",         "4,000", String.format("%.1f%%",   4000/rev*100)},
            {"Processing Fees",    "2,400", String.format("%.1f%%",   2400/rev*100)},
        };
        for (String[] r : revRows)
            revCard.getChildren().add(tableRowFull(r[0], r[1], r[2], null, null, null));
        page.getChildren().add(revCard);

        // Cost breakdown
        page.getChildren().add(sectionTitle("Cost Breakdown"));
        VBox costCard = reportCard();
        costCard.getChildren().add(tableHeader("Expense", "Amount (₨)", "% of Total"));
        String[][] costRows = {
            {"Pickup / Logistics", "23,500", String.format("%.1f%%", 23500/costs*100)},
            {"Staff Salaries",     "12,800", String.format("%.1f%%", 12800/costs*100)},
            {"Storage / Rent",      "6,500", String.format("%.1f%%",  6500/costs*100)},
            {"Admin & IT",          "3,200", String.format("%.1f%%",  3200/costs*100)},
            {"Marketing",           "1,800", String.format("%.1f%%",  1800/costs*100)},
        };
        for (String[] r : costRows)
            costCard.getChildren().add(tableRowFull(r[0], r[1], r[2], null, null, null));
        page.getChildren().add(costCard);

        // Transaction log
        page.getChildren().add(sectionTitle("Full Transaction Log (" + txs.size() + " entries)"));
        VBox txCard = reportCard();
        txCard.getChildren().add(tableHeader("Ref ID", "Description", "Type", "Amount", "Date", "Status"));
        for (Transaction t : txs) {
            HBox row = tableRowFull(
                t.getId() != null ? t.getId() : "—",
                t.getDescription(),
                t.getTypeLabel(),
                t.getAmountFormatted(),
                t.getDate().format(FMT),
                t.getStatus()
            );
            if (t.getType() == Type.REVENUE) colorRowBg(row, "rgba(34,201,126,0.04)");
            else if (t.getType() == Type.COST) colorRowBg(row, "rgba(224,82,82,0.04)");
            txCard.getChildren().add(row);
        }
        page.getChildren().add(txCard);

        page.getChildren().add(reportFooter());
        return page;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  REPORT 3 — DONATION IMPACT
    // ══════════════════════════════════════════════════════════════════════

    private static VBox buildDonationImpactReport() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));
        List<CharityRequest> reqs = db.getAllCharityRequests();
        int totalUnits = reqs.stream().mapToInt(CharityRequest::getQuantityRequested).sum();
        long fulfilled = reqs.stream().filter(r -> r.getStatus() == CharityRequest.Status.FULFILLED || r.getStatus() == CharityRequest.Status.DISPATCHED).count();

        page.getChildren().add(sectionTitle("Donation Impact Summary"));
        GridPane kpi = kpiGrid();
        kpi.add(kpiBox("🏥", String.valueOf(db.getCharityCount()), "Partner NGOs"),     0, 0);
        kpi.add(kpiBox("💊", String.valueOf(totalUnits),           "Units Requested"),  1, 0);
        kpi.add(kpiBox("✅", String.valueOf(fulfilled),            "Fulfilled"),        2, 0);
        kpi.add(kpiBox("👥", "12,400",                             "Patients Served"),  3, 0);
        page.getChildren().add(kpi);

        // Charity partners list
        page.getChildren().add(sectionTitle("Charity Partner Registry"));
        VBox partCard = reportCard();
        partCard.getChildren().add(tableHeader("Organisation", "City", "Type", "Requests", "Status"));
        for (User u : db.getUsersByRole("charity")) {
            long reqCount = reqs.stream().filter(r -> u.getId().equals(r.getCharityId())).count();
            partCard.getChildren().add(tableRowFull(
                u.getOrganization() != null ? u.getOrganization() : u.getName(),
                u.getCity() != null ? u.getCity() : "—",
                "Charity / NGO",
                String.valueOf(reqCount),
                u.getStatus(), null));
        }
        page.getChildren().add(partCard);

        // Request history
        page.getChildren().add(sectionTitle("Request History (" + reqs.size() + " records)"));
        VBox reqCard = reportCard();
        reqCard.getChildren().add(tableHeader("Ref", "Organisation", "Category", "Qty", "Urgency", "Status"));
        for (CharityRequest r : reqs) {
            HBox row = tableRowFull(r.getId(), r.getCharityName(), r.getMedicineCategory(),
                r.getQuantityLabel(), r.getUrgency() != null ? r.getUrgency() : "—", r.getStatusLabel());
            if (r.getStatus() == CharityRequest.Status.FULFILLED) colorRowBg(row, "rgba(34,201,126,0.05)");
            else if (r.getStatus() == CharityRequest.Status.PENDING) colorRowBg(row, "rgba(240,165,0,0.04)");
            reqCard.getChildren().add(row);
        }
        page.getChildren().add(reqCard);

        // Monthly impact
        page.getChildren().add(sectionTitle("Monthly Impact — Oct 2025 to Mar 2026"));
        VBox impCard = reportCard();
        impCard.getChildren().add(tableHeader("Month", "Units Received", "Patients Served", "Organisations"));
        String[][] monthly = {
            {"October 2025",  "80",  "60",  "3"},
            {"November 2025", "120", "90",  "4"},
            {"December 2025", "60",  "45",  "3"},
            {"January 2026",  "140", "105", "5"},
            {"February 2026", "90",  "68",  "4"},
            {"March 2026",    "200", "150", "5"},
        };
        for (String[] r : monthly)
            impCard.getChildren().add(tableRowFull(r[0], r[1] + " units", r[2] + " patients", r[3] + " orgs", null, null));
        page.getChildren().add(impCard);

        page.getChildren().add(reportFooter());
        return page;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  REPORT 4 — PARTNER PERFORMANCE
    // ══════════════════════════════════════════════════════════════════════

    private static VBox buildPartnerPerformanceReport() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));
        List<User> pharmacies = db.getUsersByRole("pharmacy");

        page.getChildren().add(sectionTitle("Partner Performance Summary"));
        GridPane kpi = kpiGrid();
        kpi.add(kpiBox("🤝", String.valueOf(pharmacies.size()), "Partners"),         0, 0);
        kpi.add(kpiBox("💊", String.valueOf(db.getAllMedicines().stream().filter(m->"Pharmacy".equals(m.getSource())).count()), "Medicines"), 1, 0);
        kpi.add(kpiBox("💰", "₨186K",                          "Revenue"),          2, 0);
        kpi.add(kpiBox("⭐", "4.7 / 5.0",                      "Avg Rating"),       3, 0);
        page.getChildren().add(kpi);

        page.getChildren().add(sectionTitle("Partner Scorecard"));
        VBox pCard = reportCard();
        pCard.getChildren().add(tableHeader("Partner", "City", "Medicines Submitted", "Points", "Approval Rate", "Rating"));
        for (User u : pharmacies) {
            List<Medicine> meds = db.getByDonor(u.getId());
            long appr = meds.stream().filter(m -> m.getStatus() == Medicine.Status.APPROVED).count();
            double rate = meds.isEmpty() ? 0 : (double) appr / meds.size() * 100;
            pCard.getChildren().add(tableRowFull(
                u.getName(),
                u.getCity() != null ? u.getCity() : "—",
                String.valueOf(meds.size()),
                u.getPoints() + " pts",
                String.format("%.0f%%", rate),
                "⭐ 4." + (6 + pharmacies.indexOf(u) % 3)
            ));
        }
        page.getChildren().add(pCard);

        page.getChildren().add(sectionTitle("Submissions by Partner"));
        VBox subCard = reportCard();
        subCard.getChildren().add(tableHeader("Medicine", "Partner", "Batch", "Qty", "Status"));
        for (Medicine m : db.getAllMedicines()) {
            if (!"Pharmacy".equals(m.getSource())) continue;
            User donor = db.getUserById(m.getDonorId());
            subCard.getChildren().add(tableRowFull(
                m.getName(),
                donor != null ? donor.getName() : m.getDonorId(),
                m.getBatchNumber() != null ? m.getBatchNumber() : "—",
                m.getQuantity() + " units",
                m.getStatusLabel(), null));
        }
        page.getChildren().add(subCard);

        page.getChildren().add(reportFooter());
        return page;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  REPORT 5 — LOGISTICS
    // ══════════════════════════════════════════════════════════════════════

    private static VBox buildLogisticsReport() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));
        List<Pickup> all = db.getAllPickups();
        long done = all.stream().filter(p -> p.getStatus() == Pickup.Status.DONE).count();
        int totalItems = all.stream().mapToInt(Pickup::getEstimatedItems).sum();

        page.getChildren().add(sectionTitle("Logistics Summary"));
        GridPane kpi = kpiGrid();
        kpi.add(kpiBox("🚗", String.valueOf(all.size()),  "Total Pickups"),   0, 0);
        kpi.add(kpiBox("✅", String.valueOf(done),         "Completed"),       1, 0);
        kpi.add(kpiBox("📦", String.valueOf(totalItems),   "Items Collected"), 2, 0);
        kpi.add(kpiBox("🛵", "3",                          "Active Riders"),   3, 0);
        page.getChildren().add(kpi);

        page.getChildren().add(sectionTitle("Rider Performance"));
        VBox riderCard = reportCard();
        riderCard.getChildren().add(tableHeader("Rider", "Pickups", "Items", "Completion Rate"));
        Map<String, List<Pickup>> byRider = all.stream()
            .filter(p -> p.getRider() != null && !p.getRider().equals("Auto-Assign"))
            .collect(Collectors.groupingBy(Pickup::getRider));
        byRider.forEach((rider, picks) -> {
            long rDone = picks.stream().filter(p -> p.getStatus() == Pickup.Status.DONE).count();
            int rItems = picks.stream().mapToInt(Pickup::getEstimatedItems).sum();
            double rate = picks.isEmpty() ? 0 : (double) rDone / picks.size() * 100;
            riderCard.getChildren().add(tableRowFull(rider, String.valueOf(picks.size()),
                rItems + " items", String.format("%.0f%%", rate), null, null));
        });
        page.getChildren().add(riderCard);

        page.getChildren().add(sectionTitle("Pickup Log (" + all.size() + " entries)"));
        VBox logCard = reportCard();
        logCard.getChildren().add(tableHeader("ID", "Donor", "City", "Date", "Items", "Rider", "Status"));
        for (Pickup p : all) {
            HBox row = tableRow(
                p.getId(), p.getDonorName(),
                p.getCity() != null ? p.getCity() : "—",
                p.getDate().format(FMT),
                p.getItemsLabel(),
                p.getRider() != null ? p.getRider() : "Unassigned",
                p.getStatusLabel().replace("_", " ")
            );
            if (p.getStatus() == Pickup.Status.DONE) colorRowBg(row, "rgba(34,201,126,0.04)");
            logCard.getChildren().add(row);
        }
        page.getChildren().add(logCard);

        page.getChildren().add(reportFooter());
        return page;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  REPORT 6 — COMPLIANCE
    // ══════════════════════════════════════════════════════════════════════

    private static VBox buildComplianceReport() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));

        page.getChildren().add(sectionTitle("Compliance Overview"));
        GridPane kpi = kpiGrid();
        kpi.add(kpiBox("✅", "100%",  "Compliance Score"),  0, 0);
        kpi.add(kpiBox("📄", "3",     "Certs Expiring"),    1, 0);
        kpi.add(kpiBox("🔒", "1,240", "Audit Entries"),     2, 0);
        kpi.add(kpiBox("🚫", "0",     "Violations"),        3, 0);
        page.getChildren().add(kpi);

        page.getChildren().add(sectionTitle("Certificate Status"));
        VBox certCard = reportCard();
        certCard.getChildren().add(tableHeader("Certificate", "Reference", "Expiry Date", "Status", "Action Required"));
        String[][] certs = {
            {"Cold Storage Cert",     "#CS-114", "Apr 2, 2026",  "⚠️ Expiring", "Renew before Apr 2"},
            {"DRAP License",          "#DR-221", "Dec 31, 2026", "✅ Valid",     "No action needed"},
            {"Pharma Partner Lic",    "#PP-22",  "Apr 8, 2026",  "⚠️ Expiring", "Submit renewal form"},
            {"Business Registration", "#BR-440", "Dec 31, 2027", "✅ Valid",     "No action needed"},
        };
        for (String[] c : certs) {
            HBox row = tableRowFull(c[0], c[1], c[2], c[3], c[4], null);
            if (c[3].startsWith("⚠️")) colorRowBg(row, "rgba(240,165,0,0.06)");
            certCard.getChildren().add(row);
        }
        page.getChildren().add(certCard);

        page.getChildren().add(sectionTitle("Verification Audit Log (sample)"));
        VBox auditCard = reportCard();
        auditCard.getChildren().add(tableHeader("Timestamp", "Action", "User", "Medicine / Entity", "Result"));
        String[][] logs = {
            {"Mar 20, 2026 14:32", "Medicine Approved",  "Admin Manager", "Atorvastatin 20mg",   "✅ Approved"},
            {"Mar 20, 2026 13:18", "Medicine Rejected",  "Admin Manager", "Paracetamol 500mg",   "❌ Rejected"},
            {"Mar 19, 2026 11:04", "User Registered",    "System",        "Nadia Hussain",        "✅ Created"},
            {"Mar 18, 2026 09:45", "Charity Approved",   "Admin Manager", "Hope Foundation",      "✅ Approved"},
            {"Mar 17, 2026 16:20", "Pickup Completed",   "Ali Hassan",    "Ahmad Khan - F7/2",    "✅ Completed"},
            {"Mar 16, 2026 10:30", "Medicine Approved",  "Admin Manager", "Insulin Glargine",     "✅ Approved"},
            {"Mar 15, 2026 14:12", "Report Generated",   "Admin Manager", "Financial Statement",  "✅ Generated"},
            {"Mar 14, 2026 09:00", "Bulk Upload",        "MedPlus Pharma","Batch B2041 (10 items)","✅ Processed"},
        };
        for (String[] l : logs)
            auditCard.getChildren().add(tableRowFull(l[0], l[1], l[2], l[3], l[4], null));
        page.getChildren().add(auditCard);

        page.getChildren().add(sectionTitle("Medicine Verification Summary"));
        VBox verCard = reportCard();
        verCard.getChildren().add(tableHeader("Status", "Count", "Percentage"));
        int tot = db.getAllMedicines().size();
        verCard.getChildren().add(tableRowFull("Approved", String.valueOf(db.getApproved().size()),
            String.format("%.1f%%", (double) db.getApproved().size() / tot * 100), null, null, null));
        verCard.getChildren().add(tableRowFull("Pending",  String.valueOf(db.getPending().size()),
            String.format("%.1f%%", (double) db.getPending().size()  / tot * 100), null, null, null));
        verCard.getChildren().add(tableRowFull("Rejected", String.valueOf(db.getRejected().size()),
            String.format("%.1f%%", (double) db.getRejected().size() / tot * 100), null, null, null));
        page.getChildren().add(verCard);

        page.getChildren().add(reportFooter());
        return page;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  HTML GENERATORS (for save/print)
    // ══════════════════════════════════════════════════════════════════════

    private static String generateInventoryHtml() {
        List<Medicine> all = db.getAllMedicines();
        StringBuilder sb = new StringBuilder(htmlHead("Inventory Report"));
        sb.append("<h1>📦 Inventory Report</h1>");
        sb.append("<p class='sub'>Generated: ").append(LocalDate.now().format(FMT)).append(" &nbsp;•&nbsp; CureShare BMS</p>");
        sb.append("<div class='kpi-row'>");
        sb.append(htmlKpi("Total SKUs", String.valueOf(all.size())));
        sb.append(htmlKpi("Approved", String.valueOf(db.getApproved().size())));
        sb.append(htmlKpi("Pending", String.valueOf(db.getPending().size())));
        sb.append(htmlKpi("Expiring Soon", String.valueOf(db.getExpiringSoon().size())));
        sb.append("</div>");
        sb.append("<h2>Complete Medicine List</h2>");
        sb.append("<table><tr><th>Medicine</th><th>Batch</th><th>Expiry</th><th>Qty</th><th>Source</th><th>Status</th></tr>");
        for (Medicine m : all) {
            String cls = m.isExpiringSoon() ? " class='warn'" : m.isExpired() ? " class='danger'" : "";
            sb.append("<tr").append(cls).append("><td>").append(m.getName()).append("</td>")
              .append("<td>").append(m.getBatchNumber() != null ? m.getBatchNumber() : "—").append("</td>")
              .append("<td>").append(m.getExpiryDate() != null ? m.getExpiryDate().format(FMT) : "—").append("</td>")
              .append("<td>").append(m.getQuantity()).append("</td>")
              .append("<td>").append(m.getSource() != null ? m.getSource() : "—").append("</td>")
              .append("<td>").append(m.getStatusLabel()).append("</td></tr>");
        }
        sb.append("</table>").append(htmlFoot());
        return sb.toString();
    }

    private static String generateFinancialHtml() {
        StringBuilder sb = new StringBuilder(htmlHead("Financial Statement"));
        sb.append("<h1>💰 Financial Statement</h1>");
        sb.append("<p class='sub'>Generated: ").append(LocalDate.now().format(FMT)).append(" &nbsp;•&nbsp; CureShare BMS</p>");
        sb.append("<div class='kpi-row'>");
        sb.append(htmlKpi("Revenue", String.format("₨%,.0f", db.getTotalRevenue())));
        sb.append(htmlKpi("Costs", String.format("₨%,.0f", db.getTotalCosts())));
        sb.append(htmlKpi("Net Profit", String.format("₨%,.0f", db.getNetProfit())));
        sb.append(htmlKpi("Transactions", String.valueOf(db.getAllTransactions().size())));
        sb.append("</div>");
        sb.append("<h2>Transaction Log</h2>");
        sb.append("<table><tr><th>Ref</th><th>Description</th><th>Type</th><th>Amount</th><th>Date</th><th>Status</th></tr>");
        for (Transaction t : db.getAllTransactions()) {
            String cls = t.getType() == Type.REVENUE ? " class='success'" : t.getType() == Type.COST ? " class='danger'" : "";
            sb.append("<tr").append(cls).append("><td>").append(t.getId() != null ? t.getId() : "—").append("</td>")
              .append("<td>").append(t.getDescription()).append("</td>")
              .append("<td>").append(t.getTypeLabel()).append("</td>")
              .append("<td>").append(t.getAmountFormatted()).append("</td>")
              .append("<td>").append(t.getDate().format(FMT)).append("</td>")
              .append("<td>").append(t.getStatus()).append("</td></tr>");
        }
        sb.append("</table>").append(htmlFoot());
        return sb.toString();
    }

    private static String generateDonationHtml() {
        List<CharityRequest> reqs = db.getAllCharityRequests();
        StringBuilder sb = new StringBuilder(htmlHead("Donation Impact Report"));
        sb.append("<h1>❤️ Donation Impact Report</h1>");
        sb.append("<p class='sub'>Generated: ").append(LocalDate.now().format(FMT)).append(" &nbsp;•&nbsp; CureShare BMS</p>");
        sb.append("<div class='kpi-row'>");
        sb.append(htmlKpi("Partner NGOs", String.valueOf(db.getCharityCount())));
        sb.append(htmlKpi("Requests", String.valueOf(reqs.size())));
        sb.append(htmlKpi("Units Requested", String.valueOf(reqs.stream().mapToInt(CharityRequest::getQuantityRequested).sum())));
        sb.append(htmlKpi("Patients Served", "12,400"));
        sb.append("</div>");
        sb.append("<h2>Charity Request Log</h2>");
        sb.append("<table><tr><th>Ref</th><th>Organisation</th><th>Category</th><th>Qty</th><th>Urgency</th><th>Status</th></tr>");
        for (CharityRequest r : reqs) {
            sb.append("<tr><td>").append(r.getId()).append("</td>")
              .append("<td>").append(r.getCharityName()).append("</td>")
              .append("<td>").append(r.getMedicineCategory()).append("</td>")
              .append("<td>").append(r.getQuantityLabel()).append("</td>")
              .append("<td>").append(r.getUrgency() != null ? r.getUrgency() : "—").append("</td>")
              .append("<td>").append(r.getStatusLabel()).append("</td></tr>");
        }
        sb.append("</table>").append(htmlFoot());
        return sb.toString();
    }

    private static String generatePartnerHtml() {
        StringBuilder sb = new StringBuilder(htmlHead("Partner Performance"));
        sb.append("<h1>🏥 Partner Performance</h1>");
        sb.append("<p class='sub'>Generated: ").append(LocalDate.now().format(FMT)).append(" &nbsp;•&nbsp; CureShare BMS</p>");
        sb.append("<h2>Pharmacy Partners</h2>");
        sb.append("<table><tr><th>Partner</th><th>City</th><th>Medicines</th><th>Points</th></tr>");
        for (User u : db.getUsersByRole("pharmacy")) {
            sb.append("<tr><td>").append(u.getName()).append("</td>")
              .append("<td>").append(u.getCity() != null ? u.getCity() : "—").append("</td>")
              .append("<td>").append(db.getByDonor(u.getId()).size()).append("</td>")
              .append("<td>").append(u.getPoints()).append(" pts</td></tr>");
        }
        sb.append("</table>").append(htmlFoot());
        return sb.toString();
    }

    private static String generateLogisticsHtml() {
        List<Pickup> all = db.getAllPickups();
        StringBuilder sb = new StringBuilder(htmlHead("Logistics Report"));
        sb.append("<h1>🚗 Logistics Report</h1>");
        sb.append("<p class='sub'>Generated: ").append(LocalDate.now().format(FMT)).append(" &nbsp;•&nbsp; CureShare BMS</p>");
        sb.append("<div class='kpi-row'>");
        sb.append(htmlKpi("Total Pickups", String.valueOf(all.size())));
        sb.append(htmlKpi("Completed", String.valueOf(all.stream().filter(p->p.getStatus()==Pickup.Status.DONE).count())));
        sb.append(htmlKpi("Items Collected", String.valueOf(all.stream().mapToInt(Pickup::getEstimatedItems).sum())));
        sb.append("</div>");
        sb.append("<h2>Pickup Log</h2>");
        sb.append("<table><tr><th>ID</th><th>Donor</th><th>City</th><th>Date</th><th>Items</th><th>Rider</th><th>Status</th></tr>");
        for (Pickup p : all) {
            String cls = p.getStatus() == Pickup.Status.DONE ? " class='success'" : "";
            sb.append("<tr").append(cls).append("><td>").append(p.getId()).append("</td>")
              .append("<td>").append(p.getDonorName()).append("</td>")
              .append("<td>").append(p.getCity() != null ? p.getCity() : "—").append("</td>")
              .append("<td>").append(p.getDate().format(FMT)).append("</td>")
              .append("<td>").append(p.getItemsLabel()).append("</td>")
              .append("<td>").append(p.getRider() != null ? p.getRider() : "Unassigned").append("</td>")
              .append("<td>").append(p.getStatusLabel().replace("_"," ")).append("</td></tr>");
        }
        sb.append("</table>").append(htmlFoot());
        return sb.toString();
    }

    private static String generateComplianceHtml() {
        StringBuilder sb = new StringBuilder(htmlHead("Compliance Report"));
        sb.append("<h1>🛡️ Compliance Report</h1>");
        sb.append("<p class='sub'>Generated: ").append(LocalDate.now().format(FMT)).append(" &nbsp;•&nbsp; CureShare BMS</p>");
        sb.append("<div class='kpi-row'>");
        sb.append(htmlKpi("Score", "100%"));
        sb.append(htmlKpi("Violations", "0"));
        sb.append(htmlKpi("Audit Entries", "1,240"));
        sb.append(htmlKpi("Certs Expiring", "3"));
        sb.append("</div>");
        sb.append("<h2>Medicine Verification Summary</h2>");
        sb.append("<table><tr><th>Status</th><th>Count</th></tr>");
        sb.append("<tr class='success'><td>Approved</td><td>").append(db.getApproved().size()).append("</td></tr>");
        sb.append("<tr class='warn'><td>Pending</td><td>").append(db.getPending().size()).append("</td></tr>");
        sb.append("<tr class='danger'><td>Rejected</td><td>").append(db.getRejected().size()).append("</td></tr>");
        sb.append("</table>").append(htmlFoot());
        return sb.toString();
    }

    // ── HTML HELPERS ──────────────────────────────────────────────────────

    private static String htmlHead(String title) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>CureShare — " + title + "</title><style>" +
            "body{font-family:'Segoe UI',Arial,sans-serif;background:#f0faf7;color:#0d2e28;margin:0;padding:24px;}" +
            "h1{font-size:24px;color:#00473a;margin-bottom:4px;}" +
            "h2{font-size:16px;color:#005540;margin:24px 0 8px;border-bottom:2px solid rgba(0,176,144,0.2);padding-bottom:6px;}" +
            ".sub{color:#7ab5a8;font-size:13px;margin-bottom:20px;}" +
            ".kpi-row{display:flex;gap:16px;margin:16px 0;}" +
            ".kpi{background:white;border-radius:12px;padding:16px 20px;flex:1;box-shadow:0 2px 8px rgba(0,120,96,0.10);}" +
            ".kpi .val{font-size:26px;font-weight:800;color:#007056;}" +
            ".kpi .lbl{font-size:12px;color:#7ab5a8;margin-top:4px;}" +
            "table{width:100%;border-collapse:collapse;background:white;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,120,96,0.08);margin-bottom:16px;}" +
            "th{background:rgba(0,176,144,0.10);padding:10px 14px;text-align:left;font-size:11px;text-transform:uppercase;color:#2e7060;}" +
            "td{padding:10px 14px;font-size:13px;border-bottom:1px solid rgba(0,176,144,0.08);}" +
            "tr:last-child td{border-bottom:none;}" +
            "tr:nth-child(even){background:rgba(0,176,144,0.025);}" +
            "tr.success td{background:rgba(34,201,126,0.06);}" +
            "tr.warn td{background:rgba(240,165,0,0.06);}" +
            "tr.danger td{background:rgba(224,82,82,0.06);}" +
            ".footer{margin-top:32px;padding-top:16px;border-top:1px solid rgba(0,176,144,0.18);font-size:12px;color:#7ab5a8;}" +
            "@media print{body{background:white;}}" +
            "</style></head><body>";
    }

    private static String htmlKpi(String label, String value) {
        return "<div class='kpi'><div class='val'>" + value + "</div><div class='lbl'>" + label + "</div></div>";
    }

    private static String htmlFoot() {
        return "<div class='footer'>CureShare BMS &nbsp;•&nbsp; Generated " + LocalDate.now().format(FMT) +
               " &nbsp;•&nbsp; Confidential — for internal use only</div></body></html>";
    }

    // ── UI WIDGET HELPERS ─────────────────────────────────────────────────

    private static Label sectionTitle(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-family:'" + Theme.FONT_HEADING + "';-fx-font-size:15px;-fx-font-weight:700;" +
                   "-fx-text-fill:" + Theme.TEXT_PRIMARY + ";-fx-border-color:transparent transparent rgba(0,176,144,0.20) transparent;" +
                   "-fx-border-width:0 0 2 0;-fx-padding:0 0 6 0;");
        l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }

    private static GridPane kpiGrid() {
        GridPane g = new GridPane(); g.setHgap(14);
        for (int i = 0; i < 4; i++) {
            ColumnConstraints cc = new ColumnConstraints(); cc.setPercentWidth(25); g.getColumnConstraints().add(cc);
        }
        return g;
    }

    private static VBox kpiBox(String icon, String value, String label) {
        VBox box = new VBox(4); box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color:white;-fx-background-radius:14;-fx-effect:dropshadow(gaussian,rgba(0,120,96,0.08),5,0,0,2);");
        Label ico = new Label(icon); ico.setStyle("-fx-font-size:20px;");
        Label val = new Label(value); val.setStyle("-fx-font-family:'" + Theme.FONT_HEADING + "';-fx-font-size:24px;-fx-font-weight:800;-fx-text-fill:" + Theme.TEAL_700 + ";");
        Label lbl = new Label(label); lbl.setStyle(Theme.labelStyle("11.5px", Theme.TEXT_MUTED, false));
        box.getChildren().addAll(ico, val, lbl);
        return box;
    }

    private static VBox reportCard() {
        VBox v = new VBox(0);
        v.setStyle("-fx-background-color:white;-fx-background-radius:14;-fx-effect:dropshadow(gaussian,rgba(0,120,96,0.08),12,0,0,3);");
        return v;
    }

    private static HBox tableHeader(String... cols) {
        HBox row = new HBox(0);
        row.setStyle("-fx-background-color:rgba(0,176,144,0.08);-fx-padding:9 14 9 14;-fx-background-radius:14 14 0 0;");
        for (int i = 0; i < cols.length; i++) {
            Label l = new Label(cols[i].toUpperCase()); l.setStyle(Theme.labelStyle("10px", Theme.TEXT_SECONDARY, true));
            if (i == 0 || i == 1) HBox.setHgrow(l, Priority.ALWAYS);
            else { l.setMinWidth(100); l.setPrefWidth(100); }
            row.getChildren().add(l);
        }
        return row;
    }

    private static HBox tableRow(String... cols) {
        return tableRowFull(cols.length > 0 ? cols[0] : null, cols.length > 1 ? cols[1] : null,
            cols.length > 2 ? cols[2] : null, cols.length > 3 ? cols[3] : null,
            cols.length > 4 ? cols[4] : null, cols.length > 5 ? cols[5] : null);
    }

    private static HBox tableRowFull(String c0, String c1, String c2, String c3, String c4, String c5) {
        HBox row = new HBox(0); row.setAlignment(Pos.CENTER_LEFT); row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-border-color:transparent transparent rgba(0,176,144,0.07) transparent;-fx-border-width:0 0 1 0;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color:rgba(0,176,144,0.04);-fx-padding:10 14 10 14;"));
        row.setOnMouseExited(e  -> row.setStyle("-fx-border-color:transparent transparent rgba(0,176,144,0.07) transparent;-fx-border-width:0 0 1 0;-fx-padding:10 14 10 14;"));
        String[] vals = {c0, c1, c2, c3, c4, c5};
        for (int i = 0; i < vals.length; i++) {
            if (vals[i] == null) continue;
            Label l = new Label(vals[i]); l.setStyle(Theme.labelStyle("12.5px", i == 0 ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY, i == 0));
            if (i == 0 || i == 1) HBox.setHgrow(l, Priority.ALWAYS);
            else { l.setMinWidth(100); l.setPrefWidth(100); }
            row.getChildren().add(l);
        }
        return row;
    }

    private static void colorRowBg(HBox row, String color) {
        String existing = row.getStyle();
        row.setStyle(existing + "-fx-background-color:" + color + ";");
    }

    private static HBox reportFooter() {
        HBox foot = new HBox();
        Label l = new Label("CureShare BMS  •  Generated " + LocalDate.now().format(FMT) + "  •  Confidential — for internal use only");
        l.setStyle(Theme.labelStyle("11px", Theme.TEXT_MUTED, false));
        foot.setPadding(new Insets(16, 0, 0, 0));
        foot.setStyle("-fx-border-color:rgba(0,176,144,0.18) transparent transparent transparent;-fx-border-width:1 0 0 0;");
        foot.getChildren().add(l);
        return foot;
    }
}
