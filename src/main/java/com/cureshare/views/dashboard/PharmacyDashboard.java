package com.cureshare.views.dashboard;

import com.cureshare.models.*;
import com.cureshare.utils.*;
import com.cureshare.views.shared.UIComponents;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.List;

public class PharmacyDashboard extends BaseLayout {

    private final DataStore db = DataStore.getInstance();

    public void show(Stage stage, User user) {
        initialize(stage, user);
        buildScene();
        showHomePage();
    }

    @Override protected void onAddButtonClicked() { showBulkPage(); }
    @Override protected void onAvatarClicked()    { showSettingsPage(); }

    @Override
    protected void buildNavItems(VBox nav) {
        addNavSection(nav, "Overview");
        addNavItem(nav, "📊", "Dashboard",        null, this::showHomePage);
        addNavSection(nav, "Medicine Management");
        addNavItem(nav, "📦", "Bulk Upload",       null, this::showBulkPage);
        addNavItem(nav, "💊", "My Submissions",    null, this::showSubmissionsPage);
        addNavItem(nav, "⏱️", "Expiry Tracker",    null, this::showExpiryPage);
        addNavItem(nav, "🔄", "Resale Agreements", null, this::showResalePage);
        addNavSection(nav, "Finance & Reports");
        addNavItem(nav, "💰", "Revenue",           null, this::showRevenuePage);
        addNavItem(nav, "📋", "Reports",           null, this::showReportsPage);
        addNavSection(nav, "Account");
        addNavItem(nav, "⚙️", "Settings",          null, this::showSettingsPage);
    }

    // ── HOME ──────────────────────────────────────────────────────────────
    private void showHomePage() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));
        List<Medicine> mine = db.getAllMedicines().stream().filter(m -> user.getId().equals(m.getDonorId())).toList();
        long approved = mine.stream().filter(m -> m.getStatus() == Medicine.Status.APPROVED).count();
        long expiring = mine.stream().filter(Medicine::isExpiringSoon).count();
        double estRev = mine.stream().filter(m -> m.getStatus() == Medicine.Status.APPROVED)
            .mapToDouble(m -> (m.getPrice() > 0 ? m.getPrice() : 85.0) * m.getQuantity() * 0.7).sum();

        GridPane stats = new GridPane(); stats.setHgap(16);
        stats.getColumnConstraints().addAll(UIComponents.pct(25),UIComponents.pct(25),UIComponents.pct(25),UIComponents.pct(25));
        stats.add(UIComponents.statCard("📦", String.valueOf(mine.size()), "Units Submitted", "This quarter", true), 0, 0);
        stats.add(UIComponents.statCard("✅", String.valueOf(approved),
            "Approved", String.format("%.0f%%", (double)approved / Math.max(1, mine.size()) * 100) + " rate", true), 1, 0);
        stats.add(UIComponents.statCard("⚠️", String.valueOf(expiring), "Expiry Alerts",
            expiring == 0 ? "All clear" : "Act now", expiring == 0), 2, 0);
        stats.add(UIComponents.statCard("💰", String.format("₨%,.0f", estRev > 0 ? estRev : 42000),
            "Est. Revenue", "From approved stock", true), 3, 0);

        VBox batchCard = UIComponents.glassCard(22); batchCard.setSpacing(12);
        Button uploadBtn = UIComponents.primaryButton("+ Upload Batch");
        uploadBtn.setOnAction(e -> showBulkPage());
        batchCard.getChildren().add(UIComponents.sectionHeader("Recent Submissions", null, uploadBtn));

        if (mine.isEmpty()) {
            Label empty = new Label("No submissions yet. Click '+ Upload Batch' to get started.");
            empty.setStyle(Theme.labelStyle("13px", Theme.TEXT_MUTED, false)); empty.setPadding(new Insets(16));
            batchCard.getChildren().add(empty);
        } else {
            batchCard.getChildren().add(UIComponents.tableHeader(
                new String[]{"Medicine","Batch","Qty","Expiry","Status","Actions"}, new double[]{0,80,60,95,95,120}));
            List<Medicine> recent = mine.subList(0, Math.min(6, mine.size()));
            for (int i = 0; i < recent.size(); i++) {
                Medicine m = recent.get(i);
                HBox row = UIComponents.tableRow(i % 2 == 1);
                Label nm = new Label(m.getName()); nm.setStyle(Theme.labelStyle("13px",Theme.TEXT_PRIMARY,true));
                HBox.setHgrow(nm, Priority.ALWAYS); row.getChildren().add(nm);
                row.getChildren().add(UIComponents.cell(m.getBatchNumber()!=null?m.getBatchNumber():"—",80));
                row.getChildren().add(UIComponents.cell(m.getQuantity()+" units",60));
                row.getChildren().add(UIComponents.cell(m.getExpiryDate()!=null?m.getExpiryDate().toString():"—",95,false,m.isExpiringSoon()?Theme.WARNING:Theme.TEXT_SECONDARY));
                String bt = switch(m.getStatus()){case APPROVED->"success";case REJECTED->"danger";default->"warning";};
                row.getChildren().add(UIComponents.badge(m.getStatusLabel(), bt));
                HBox acts = new HBox(6); acts.setAlignment(Pos.CENTER_LEFT); acts.setMinWidth(120);
                Button view = UIComponents.smallButton("Details", false); view.setOnAction(e -> openMedDetailDialog(m));
                acts.getChildren().add(view); row.getChildren().add(acts);
                batchCard.getChildren().add(row); AnimationUtils.fadeIn(row, 260, i * 40);
            }
        }
        page.getChildren().addAll(stats, batchCard);
        AnimationUtils.staggerFadeIn(stats, batchCard);
        showContent(page, "Pharmacy Dashboard", "Welcome, " + user.getName() + " — manage your submissions");
    }

    // ── BULK UPLOAD ───────────────────────────────────────────────────────
    private void showBulkPage() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));
        VBox card = UIComponents.glassCard(28); card.setSpacing(18); card.setMaxWidth(660);
        Label title2 = new Label("📦 Bulk Medicine Upload");
        title2.setStyle("-fx-font-family:'"+Theme.FONT_HEADING+"';-fx-font-size:20px;-fx-font-weight:800;-fx-text-fill:"+Theme.TEXT_PRIMARY+";");
        Label sub = new Label("Upload a CSV file or add medicines manually. Each item goes through admin verification.");
        sub.setStyle(Theme.labelStyle("13px", Theme.TEXT_MUTED, false));

        VBox uploadZone = new VBox(12); uploadZone.setAlignment(Pos.CENTER); uploadZone.setPadding(new Insets(28));
        uploadZone.setStyle("-fx-background-color:rgba(0,176,144,0.06);-fx-background-radius:16;"+
            "-fx-border-color:rgba(0,176,144,0.28);-fx-border-width:2;-fx-border-style:dashed;-fx-border-radius:16;");
        Label uploadIco = new Label("📂"); uploadIco.setStyle("-fx-font-size:36px;");
        Label uploadLbl = new Label("Click Browse to select a CSV file");
        uploadLbl.setStyle(Theme.labelStyle("14px", Theme.TEXT_SECONDARY, false));
        Label uploadFmt = new Label("Required columns: Name, Category, Batch, Expiry (YYYY-MM-DD), Quantity  |  Optional: Price");
        uploadFmt.setStyle(Theme.labelStyle("11px", Theme.TEXT_MUTED, false));
        Label uploadResult = new Label(""); uploadResult.setStyle(Theme.labelStyle("12px", Theme.SUCCESS, true));
        uploadResult.setWrapText(true); uploadResult.setMaxWidth(520);
        Button browseBtn = UIComponents.primaryButton("📁 Browse CSV File");
        browseBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select CSV batch file");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File docs = new File(System.getProperty("user.home"), "Documents");
            if (docs.exists()) fc.setInitialDirectory(docs);
            File f = fc.showOpenDialog(stage);
            if (f != null) processCsvUpload(f, uploadResult);
        });
        uploadZone.getChildren().addAll(uploadIco, uploadLbl, uploadFmt, browseBtn, uploadResult);

        Label orLbl = new Label("— or add a single medicine manually —");
        orLbl.setStyle(Theme.labelStyle("12px", Theme.TEXT_MUTED, false));

        GridPane grid = new GridPane(); grid.setHgap(14); grid.setVgap(14);
        grid.getColumnConstraints().addAll(UIComponents.pct(40),UIComponents.pct(20),UIComponents.pct(20),UIComponents.pct(20));
        TextField nameF  = UIComponents.styledTextField("Medicine Name");
        ComboBox<String> catF = UIComponents.styledComboBox("Antibiotic","Analgesic","Cardiac","Diabetes",
            "Gastro","Hypertension","Supplement","Respiratory","Antihistamine","Other");
        TextField batchF = UIComponents.styledTextField("Batch #");
        TextField qtyF   = UIComponents.styledTextField("Qty");
        TextField expiryF= UIComponents.styledTextField("Expiry YYYY-MM-DD");
        TextField priceF = UIComponents.styledTextField("Price/unit ₨");
        grid.add(UIComponents.formGroup("Medicine Name *", nameF), 0, 0);
        grid.add(UIComponents.formGroup("Category", catF), 1, 0);
        grid.add(UIComponents.formGroup("Batch #", batchF), 2, 0);
        grid.add(UIComponents.formGroup("Qty *", qtyF), 3, 0);
        grid.add(UIComponents.formGroup("Expiry Date *", expiryF), 0, 1);
        grid.add(UIComponents.formGroup("Price/Unit (₨)", priceF), 1, 1);

        Label errLbl     = new Label(""); errLbl.setStyle(Theme.labelStyle("12px", Theme.DANGER, false));
        Label successLbl = new Label(""); successLbl.setStyle(Theme.labelStyle("13px", Theme.SUCCESS, true));

        Button submitBtn = UIComponents.primaryButton("＋ Submit Medicine →");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(e -> {
            if (nameF.getText().isBlank()||qtyF.getText().isBlank()||expiryF.getText().isBlank()) {
                errLbl.setText("❌ Name, quantity and expiry are required."); return;
            }
            try {
                Medicine med = new Medicine();
                med.setName(nameF.getText().trim()); med.setCategory(catF.getValue());
                med.setBatchNumber(batchF.getText().isBlank() ? "N/A" : batchF.getText().trim());
                med.setExpiryDate(LocalDate.parse(expiryF.getText().trim()));
                med.setQuantity(Integer.parseInt(qtyF.getText().trim()));
                med.setSource("Pharmacy"); med.setDonorId(user.getId()); med.setDonorName(user.getName());
                if (!priceF.getText().isBlank()) med.setPrice(Double.parseDouble(priceF.getText().trim()));
                db.addMedicine(med);
                user.setPoints(user.getPoints() + 15);
                successLbl.setText("✅ " + med.getName() + " (" + med.getQuantity() + " units) submitted! +15 pts earned");
                errLbl.setText("");
                nameF.clear(); batchF.clear(); qtyF.clear(); expiryF.clear(); priceF.clear();
                AnimationUtils.fadeIn(successLbl, 300, 0);
            } catch (Exception ex) { errLbl.setText("❌ Invalid date (YYYY-MM-DD) or quantity."); }
        });

        card.getChildren().addAll(title2, sub, uploadZone, orLbl, grid, errLbl, successLbl, submitBtn);
        page.getChildren().add(card); AnimationUtils.fadeIn(card, 350, 0);
        showContent(page, "Bulk Upload", "Submit medicines individually or via CSV");
    }

    private void processCsvUpload(File f, Label resultLabel) {
        int added = 0, skipped = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] cols = line.split(",", -1);
                if (cols.length < 5) { skipped++; continue; }
                try {
                    Medicine med = new Medicine();
                    med.setName(cols[0].trim().replace("\"",""));
                    med.setCategory(cols.length > 1 ? cols[1].trim().replace("\"","") : "Other");
                    med.setBatchNumber(cols.length > 2 && !cols[2].isBlank() ? cols[2].trim() : "CSV");
                    med.setExpiryDate(LocalDate.parse(cols[3].trim().replace("\"","")));
                    med.setQuantity(Integer.parseInt(cols[4].trim().replace("\"","")));
                    med.setSource("Pharmacy"); med.setDonorId(user.getId()); med.setDonorName(user.getName());
                    if (cols.length > 5 && !cols[5].isBlank()) {
                        try { med.setPrice(Double.parseDouble(cols[5].trim())); } catch (Exception ignored) {}
                    }
                    db.addMedicine(med); added++;
                } catch (Exception ex) { skipped++; }
            }
        } catch (Exception ex) {
            resultLabel.setStyle(Theme.labelStyle("12px", Theme.DANGER, true));
            resultLabel.setText("❌ Could not read file: " + ex.getMessage()); return;
        }
        user.setPoints(user.getPoints() + added * 15);
        resultLabel.setStyle(Theme.labelStyle("12px", Theme.SUCCESS, true));
        resultLabel.setText("✅ Imported " + added + " medicines from " + f.getName() +
            (skipped > 0 ? "  •  ⚠️ " + skipped + " rows skipped (check format)" : "") +
            "  •  +" + (added * 15) + " pts earned");
        AnimationUtils.fadeIn(resultLabel, 300, 0);
    }

    // ── MY SUBMISSIONS ────────────────────────────────────────────────────
    private void showSubmissionsPage() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));
        List<Medicine> mine = db.getAllMedicines().stream().filter(m -> user.getId().equals(m.getDonorId())).toList();
        VBox card = UIComponents.glassCard(22); card.setSpacing(12);
        Button addBtn    = UIComponents.primaryButton("+ Submit More"); addBtn.setOnAction(e -> showBulkPage());
        Button exportBtn = UIComponents.glassButton("📤 Export CSV");
        exportBtn.setOnAction(e -> { String p = CsvExporter.exportMySubmissions(stage, user.getId()); if (p!=null) showToast("✅ Saved: "+p); });
        HBox hdBtns = new HBox(8); hdBtns.getChildren().addAll(exportBtn, addBtn);
        long appr = mine.stream().filter(m->m.getStatus()==Medicine.Status.APPROVED).count();
        long pend = mine.stream().filter(m->m.getStatus()==Medicine.Status.PENDING).count();
        card.getChildren().add(UIComponents.sectionHeader("My Submitted Medicines",
            mine.size()+" total  •  "+appr+" approved  •  "+pend+" pending", hdBtns));
        if (mine.isEmpty()) {
            Label e2 = new Label("No submissions yet."); e2.setStyle(Theme.labelStyle("13px",Theme.TEXT_MUTED,false)); e2.setPadding(new Insets(16));
            card.getChildren().add(e2);
        } else {
            card.getChildren().add(UIComponents.tableHeader(
                new String[]{"Medicine","Category","Batch","Qty","Expiry","Status","Action"},
                new double[]{0,100,85,60,95,95,110}));
            for (int i = 0; i < mine.size(); i++) {
                Medicine m = mine.get(i);
                HBox row = UIComponents.tableRow(i%2==1);
                Label nm = new Label(m.getName()); nm.setStyle(Theme.labelStyle("13px",Theme.TEXT_PRIMARY,true));
                HBox.setHgrow(nm,Priority.ALWAYS); row.getChildren().add(nm);
                row.getChildren().add(UIComponents.cell(m.getCategory()!=null?m.getCategory():"—",100));
                row.getChildren().add(UIComponents.cell(m.getBatchNumber()!=null?m.getBatchNumber():"—",85));
                row.getChildren().add(UIComponents.cell(m.getQuantity()+" units",60));
                row.getChildren().add(UIComponents.cell(m.getExpiryDate()!=null?m.getExpiryDate().toString():"—",95,false,m.isExpiringSoon()?Theme.WARNING:Theme.TEXT_SECONDARY));
                String bt = switch(m.getStatus()){case APPROVED->"success";case REJECTED->"danger";default->"warning";};
                row.getChildren().add(UIComponents.badge(m.getStatusLabel(),bt));
                Button vw = UIComponents.smallButton("View",false); vw.setOnAction(e -> openMedDetailDialog(m));
                HBox acts = new HBox(6); acts.setMinWidth(110); acts.getChildren().add(vw); row.getChildren().add(acts);
                card.getChildren().add(row); AnimationUtils.fadeIn(row,260,i*35);
            }
        }
        page.getChildren().add(card);
        showContent(page, "My Submissions", "Track your submitted medicines");
    }

    // ── EXPIRY TRACKER ────────────────────────────────────────────────────
    private void showExpiryPage() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));
        List<Medicine> allExpiring = db.getExpiringSoon();
        VBox card = UIComponents.glassCard(22); card.setSpacing(12);
        card.getChildren().add(UIComponents.sectionHeader("Expiry Tracker",
            allExpiring.size()+" medicines expiring within 30 days",
            UIComponents.badge(allExpiring.size()+" items", allExpiring.isEmpty()?"success":"warning")));
        if (allExpiring.isEmpty()) {
            Label ok = new Label("✅ No medicines expiring in the next 30 days!");
            ok.setStyle(Theme.labelStyle("14px",Theme.SUCCESS,false)); ok.setPadding(new Insets(20));
            card.getChildren().add(ok);
        } else {
            card.getChildren().add(UIComponents.tableHeader(
                new String[]{"Medicine","Batch","Source","Days Left","Qty","Action"},
                new double[]{0,90,90,100,60,160}));
            for (int i = 0; i < allExpiring.size(); i++) {
                Medicine m = allExpiring.get(i);
                long days = m.daysUntilExpiry();
                HBox row = UIComponents.tableRow(i%2==1);
                Label nm = new Label(m.getName()); nm.setStyle(Theme.labelStyle("13px",Theme.TEXT_PRIMARY,true));
                HBox.setHgrow(nm,Priority.ALWAYS); row.getChildren().add(nm);
                row.getChildren().add(UIComponents.cell(m.getBatchNumber()!=null?m.getBatchNumber():"—",90));
                row.getChildren().add(UIComponents.cell(m.getSource()!=null?m.getSource():"—",90));
                row.getChildren().add(UIComponents.cell(days+" days",100,true,days<7?Theme.DANGER:Theme.WARNING));
                row.getChildren().add(UIComponents.cell(m.getQuantity()+" units",60));
                HBox acts = new HBox(6); acts.setAlignment(Pos.CENTER_LEFT); acts.setMinWidth(160);
                Button redist = UIComponents.smallButton("🔄 Redistribute",true);
                redist.setOnAction(e -> {
                    db.addTransaction(new Transaction(null,"Redistribution request: "+m.getName(),Transaction.Type.COST,0,LocalDate.now(),"Pending"));
                    showToast("✅ Redistribution request sent for "+m.getName()+".");
                });
                Button disp = UIComponents.smallButton("🗑 Dispose",false);
                disp.setOnAction(e -> {
                    db.deleteMedicine(m.getId()); showExpiryPage();
                    showToast("🗑 Disposal request submitted for "+m.getName()+".");
                });
                acts.getChildren().addAll(redist, disp); row.getChildren().add(acts);
                card.getChildren().add(row); AnimationUtils.fadeIn(row,260,i*40);
            }
        }
        page.getChildren().add(card);
        showContent(page, "Expiry Tracker", "Monitor medicines expiring within 30 days");
    }

    // ── RESALE AGREEMENTS ─────────────────────────────────────────────────
    private void showResalePage() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));
        GridPane stats = new GridPane(); stats.setHgap(16);
        stats.getColumnConstraints().addAll(UIComponents.pct(25),UIComponents.pct(25),UIComponents.pct(25),UIComponents.pct(25));
        stats.add(UIComponents.statCard("🤝","3","Active Agreements","Current contracts",true),0,0);
        stats.add(UIComponents.statCard("💊","470","Units Under Agreement","Committed stock",true),1,0);
        stats.add(UIComponents.statCard("💰","₨38K","Pending Revenue","Awaiting dispatch",true),2,0);
        stats.add(UIComponents.statCard("📅","45 days","Avg Contract Duration","This quarter",true),3,0);

        VBox tableCard = UIComponents.glassCard(22); tableCard.setSpacing(0);
        Button addBtn = UIComponents.primaryButton("＋ New Agreement");
        addBtn.setOnAction(e -> openNewResaleAgreementDialog());
        tableCard.getChildren().add(UIComponents.sectionHeader("Resale Agreements","Wholesale & discount contracts",addBtn));
        tableCard.getChildren().add(UIComponents.tableHeader(
            new String[]{"Ref","Medicine","Qty","Discount","Buyer","Start","End","Status","Actions"},
            new double[]{70,0,55,70,110,80,80,90,120}));
        String[][] agreements = {
            {"#AGR-001","Atorvastatin 20mg", "200","40%","City Dispensary","Mar 1","Mar 31","Active"},
            {"#AGR-002","Omeprazole 20mg",  "45", "55%","Hope Clinic",    "Mar 5","Apr 5", "Active"},
            {"#AGR-003","Azithromycin 500mg","22","45%","Al-Shifa Trust", "Mar 10","Apr 10","Active"},
            {"#AGR-004","Metformin 850mg",  "18", "50%","Rural Health",   "Feb 15","Mar 15","Expired"},
            {"#AGR-005","Cetirizine 10mg",  "185","35%","HealthPlus","Mar 15","Apr 15","Active"},
        };
        for (int i = 0; i < agreements.length; i++) {
            String[] ag = agreements[i];
            HBox row = UIComponents.tableRow(i%2==1);
            row.getChildren().add(UIComponents.cell(ag[0],70,false,Theme.TEXT_MUTED));
            Label nm = new Label(ag[1]); nm.setStyle(Theme.labelStyle("13px",Theme.TEXT_PRIMARY,true));
            HBox.setHgrow(nm,Priority.ALWAYS); row.getChildren().add(nm);
            row.getChildren().add(UIComponents.cell(ag[2],55));
            row.getChildren().add(UIComponents.cell(ag[3],70,false,Theme.SUCCESS));
            row.getChildren().add(UIComponents.cell(ag[4],110));
            row.getChildren().add(UIComponents.cell(ag[5],80));
            row.getChildren().add(UIComponents.cell(ag[6],80));
            row.getChildren().add(UIComponents.badge(ag[7],ag[7].equals("Active")?"success":"danger"));
            HBox acts = new HBox(6); acts.setMinWidth(120); acts.setAlignment(Pos.CENTER_LEFT);
            Button view = UIComponents.smallButton("Details",false); final String[] agc = ag;
            view.setOnAction(e -> openAgreementDetailDialog(agc));
            acts.getChildren().add(view);
            if (ag[7].equals("Active")) {
                Button renew = UIComponents.smallButton("Renew",true);
                renew.setOnAction(e -> showToast("✅ Renewal request submitted for "+agc[0]));
                acts.getChildren().add(renew);
            }
            row.getChildren().add(acts);
            tableCard.getChildren().add(row); AnimationUtils.fadeIn(row,260,i*35);
        }
        page.getChildren().addAll(stats, tableCard);
        AnimationUtils.staggerFadeIn(stats, tableCard);
        showContent(page, "Resale Agreements", "Wholesale & discount medicine contracts");
    }

    // ── REVENUE ───────────────────────────────────────────────────────────
    private void showRevenuePage() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));
        List<Medicine> mine = db.getAllMedicines().stream().filter(m -> user.getId().equals(m.getDonorId())).toList();
        long approvedCount = mine.stream().filter(m->m.getStatus()==Medicine.Status.APPROVED).count();
        double estRevenue = mine.stream().filter(m->m.getStatus()==Medicine.Status.APPROVED)
            .mapToDouble(m -> (m.getPrice()>0?m.getPrice():85.0)*m.getQuantity()*0.7).sum();

        GridPane stats = new GridPane(); stats.setHgap(16);
        stats.getColumnConstraints().addAll(UIComponents.pct(25),UIComponents.pct(25),UIComponents.pct(25),UIComponents.pct(25));
        stats.add(UIComponents.statCard("💰",String.format("₨%,.0f",estRevenue>0?estRevenue:42000),"Est. Revenue","From approved stock",true),0,0);
        stats.add(UIComponents.statCard("✅",String.valueOf(approvedCount),"Approved Medicines","Revenue-eligible",true),1,0);
        stats.add(UIComponents.statCard("⭐",user.getPoints()+" pts","Points Earned","Redeem for cashback",true),2,0);
        stats.add(UIComponents.statCard("📈","70%","Avg Resale Rate","vs market price",true),3,0);

        VBox revCard = UIComponents.glassCard(22); revCard.setSpacing(0);
        Button exportBtn = UIComponents.glassButton("📤 Export CSV");
        exportBtn.setOnAction(e -> { String p = CsvExporter.exportMySubmissions(stage,user.getId()); if(p!=null) showToast("✅ Saved: "+p); });
        revCard.getChildren().add(UIComponents.sectionHeader("Revenue by Medicine","Approved medicines",exportBtn));
        revCard.getChildren().add(UIComponents.tableHeader(
            new String[]{"Medicine","Qty","Price/Unit","Est. Revenue","Status"},new double[]{0,60,90,110,95}));
        for (int i=0;i<mine.size();i++) {
            Medicine m = mine.get(i);
            if (m.getStatus()!=Medicine.Status.APPROVED) continue;
            double unitP = m.getPrice()>0?m.getPrice()*0.7:85.0;
            HBox row = UIComponents.tableRow(i%2==1);
            Label nm = new Label(m.getName()); nm.setStyle(Theme.labelStyle("13px",Theme.TEXT_PRIMARY,true));
            HBox.setHgrow(nm,Priority.ALWAYS); row.getChildren().add(nm);
            row.getChildren().add(UIComponents.cell(String.valueOf(m.getQuantity()),60));
            row.getChildren().add(UIComponents.cell(String.format("₨%.0f",unitP),90));
            row.getChildren().add(UIComponents.cell(String.format("₨%,.0f",unitP*m.getQuantity()),110,true,Theme.TEAL_600));
            row.getChildren().add(UIComponents.badge("Approved","success"));
            revCard.getChildren().add(row);
        }
        VBox chartCard = UIComponents.glassCard(22); chartCard.setSpacing(14);
        chartCard.getChildren().add(UIComponents.sectionHeader("Monthly Revenue Trend","Last 6 months"));
        chartCard.getChildren().add(UIComponents.barChart(
            new String[]{"Oct","Nov","Dec","Jan","Feb","Mar"},
            new double[]{6200,8400,5100,9800,7300,11200},11200,140));
        page.getChildren().addAll(stats, revCard, chartCard);
        AnimationUtils.staggerFadeIn(stats, revCard, chartCard);
        showContent(page, "Revenue", "Your earnings from medicine contributions");
    }

    // ── REPORTS ───────────────────────────────────────────────────────────
    private void showReportsPage() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));
        HBox banner = new HBox(12); banner.setAlignment(Pos.CENTER_LEFT); banner.setPadding(new Insets(14,18,14,18));
        banner.setStyle("-fx-background-color:rgba(0,176,144,0.08);-fx-background-radius:12;-fx-border-color:rgba(0,176,144,0.20);-fx-border-width:1;-fx-border-radius:12;");
        Label ico2 = new Label("ℹ️"); ico2.setStyle("-fx-font-size:18px;");
        Label txt = new Label("Click Generate to open a full report in-app. Use Save as HTML to export, or Print / PDF for a printable copy.");
        txt.setStyle(Theme.labelStyle("13px",Theme.TEXT_SECONDARY,false)); txt.setWrapText(true); HBox.setHgrow(txt,Priority.ALWAYS);
        banner.getChildren().addAll(ico2, txt);

        FlowPane grid = new FlowPane(16,16); grid.setPrefWrapLength(Double.MAX_VALUE);
        Object[][] reports = {
            {"💊","My Submissions Report","All your submitted medicines — status, batch, expiry"},
            {"🏥","Partner Performance",  "Your approval rate and contribution vs other partners"},
            {"📦","Full Inventory Report","Complete inventory across all medicines in the system"},
            {"💰","Financial Statement",  "Revenue breakdown and full transaction log"},
        };
        for (Object[] r : reports) {
            VBox rCard = UIComponents.glassCard(20); rCard.setSpacing(10); rCard.setMinWidth(280); rCard.setMaxWidth(330);
            Label rIco   = new Label((String)r[0]); rIco.setStyle("-fx-font-size:26px;");
            Label rTitle = new Label((String)r[1]); rTitle.setStyle(Theme.labelStyle("14px",Theme.TEXT_PRIMARY,true));
            Label rDesc  = new Label((String)r[2]); rDesc.setStyle(Theme.labelStyle("12px",Theme.TEXT_MUTED,false)); rDesc.setWrapText(true);
            Button genBtn = UIComponents.primaryButton("📊 Generate & View");
            genBtn.setMaxWidth(Double.MAX_VALUE);
            final String rName = (String)r[1];
            genBtn.setOnAction(e -> {
                switch(rName) {
                    case "My Submissions Report","Partner Performance" -> ReportViewer.showPartnerPerformanceReport(stage);
                    case "Full Inventory Report"                       -> ReportViewer.showInventoryReport(stage);
                    case "Financial Statement"                         -> ReportViewer.showFinancialReport(stage);
                }
            });
            Button csvBtn = UIComponents.glassButton("📤 Export CSV");
            csvBtn.setOnAction(e -> {
                String path = rName.contains("Submission")
                    ? CsvExporter.exportMySubmissions(stage, user.getId())
                    : CsvExporter.exportInventory(stage);
                if (path != null) showToast("✅ Saved: " + path);
            });
            HBox btns = new HBox(8); btns.getChildren().addAll(genBtn, csvBtn);
            rCard.getChildren().addAll(rIco, rTitle, rDesc, btns);
            AnimationUtils.addHoverScale(rCard, 1.02); grid.getChildren().add(rCard);
        }
        page.getChildren().addAll(banner, grid);
        AnimationUtils.staggerFadeIn(banner, grid);
        showContent(page, "Reports", "Generate and export your pharmacy reports");
    }

    // ── SETTINGS ──────────────────────────────────────────────────────────
    private void showSettingsPage() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));
        VBox card = UIComponents.glassCard(28); card.setSpacing(16); card.setMaxWidth(560);
        Label title2 = new Label("⚙️ Organisation Settings");
        title2.setStyle("-fx-font-family:'"+Theme.FONT_HEADING+"';-fx-font-size:20px;-fx-font-weight:800;-fx-text-fill:"+Theme.TEXT_PRIMARY+";");
        GridPane grid = new GridPane(); grid.setHgap(14); grid.setVgap(14);
        grid.getColumnConstraints().addAll(UIComponents.pct(50),UIComponents.pct(50));
        TextField nameF = UIComponents.styledTextField("Contact Name"); nameF.setText(user.getName());
        TextField emailF = UIComponents.styledTextField("Email"); emailF.setText(user.getEmail());
        TextField orgF = UIComponents.styledTextField("Organisation"); if(user.getOrganization()!=null) orgF.setText(user.getOrganization());
        TextField phoneF = UIComponents.styledTextField("Phone"); if(user.getPhone()!=null) phoneF.setText(user.getPhone());
        TextField cityF = UIComponents.styledTextField("City"); if(user.getCity()!=null) cityF.setText(user.getCity());
        PasswordField passF = UIComponents.styledPasswordField("New password");
        PasswordField pass2F = UIComponents.styledPasswordField("Confirm password");
        grid.add(UIComponents.formGroup("Contact Name",nameF),0,0);
        grid.add(UIComponents.formGroup("Email",emailF),1,0);
        grid.add(UIComponents.formGroup("Organisation",orgF),0,1,2,1);
        grid.add(UIComponents.formGroup("Phone",phoneF),0,2);
        grid.add(UIComponents.formGroup("City",cityF),1,2);
        grid.add(UIComponents.formGroup("New Password",passF),0,3);
        grid.add(UIComponents.formGroup("Confirm Password",pass2F),1,3);
        VBox notif = new VBox(10); notif.getChildren().add(UIComponents.sectionHeader("Notifications",null));
        notif.getChildren().addAll(
            UIComponents.toggle(true, "Email when submission is approved/rejected"),
            UIComponents.toggle(true, "SMS alert for medicines expiring within 7 days"),
            UIComponents.toggle(false,"Weekly summary email"));
        Label saveMsg = new Label(""); saveMsg.setStyle(Theme.labelStyle("12px",Theme.SUCCESS,true));
        Button saveBtn = UIComponents.primaryButton("💾 Save Changes"); saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> {
            if (!passF.getText().isBlank() && !passF.getText().equals(pass2F.getText())) {
                saveMsg.setText("❌ Passwords do not match."); saveMsg.setStyle(Theme.labelStyle("12px",Theme.DANGER,true)); return;
            }
            user.setName(nameF.getText().trim()); user.setEmail(emailF.getText().trim());
            user.setOrganization(orgF.getText().trim()); user.setPhone(phoneF.getText().trim()); user.setCity(cityF.getText().trim());
            if (!passF.getText().isBlank()) db.updatePassword(user.getId(), passF.getText());
            saveMsg.setText("✅ Settings saved!"); saveMsg.setStyle(Theme.labelStyle("12px",Theme.SUCCESS,true));
            AnimationUtils.fadeIn(saveMsg,300,0);
        });
        card.getChildren().addAll(title2, grid, notif, saveMsg, saveBtn);
        page.getChildren().add(card);
        showContent(page, "Settings", "Organisation account settings");
    }

    // ── DIALOGS ───────────────────────────────────────────────────────────
    private void openMedDetailDialog(Medicine m) {
        Stage dlg = new Stage(); dlg.initOwner(stage); dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Medicine Details — "+m.getName()); dlg.setMinWidth(420); dlg.setMinHeight(340); dlg.setResizable(false);
        VBox body = new VBox(14); body.setPadding(new Insets(24)); body.setStyle("-fx-background-color:#f0faf7;");
        VBox info = UIComponents.glassDarkCard(14); info.setSpacing(10);
        info.getChildren().addAll(
            UIComponents.metricRow("Name",    m.getName(),Theme.TEXT_PRIMARY),
            UIComponents.metricRow("Batch",   m.getBatchNumber()!=null?m.getBatchNumber():"—",null),
            UIComponents.metricRow("Expiry",  m.getExpiryDate()!=null?m.getExpiryDate().toString():"—",m.isExpiringSoon()?Theme.DANGER:Theme.TEXT_SECONDARY),
            UIComponents.metricRow("Qty",     m.getQuantity()+" units",null),
            UIComponents.metricRow("Price",   m.getPrice()>0?"₨"+m.getPrice()+"/unit":"—",null),
            UIComponents.metricRow("Status",  m.getStatusLabel(),m.getStatus()==Medicine.Status.APPROVED?Theme.SUCCESS:m.getStatus()==Medicine.Status.REJECTED?Theme.DANGER:Theme.WARNING),
            UIComponents.metricRow("Submitted",m.getSubmittedDate().toString(),null));
        Button closeBtn = UIComponents.glassButton("Close"); closeBtn.setOnAction(e -> dlg.close());
        body.getChildren().addAll(info, new HBox(){{getChildren().add(closeBtn);}});
        dlg.setScene(new Scene(body)); dlg.show(); AnimationUtils.scaleIn(body);
    }

    private void openNewResaleAgreementDialog() {
        Stage dlg = new Stage(); dlg.initOwner(stage); dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("New Resale Agreement"); dlg.setMinWidth(460); dlg.setMinHeight(380); dlg.setResizable(false);
        VBox body = new VBox(16); body.setPadding(new Insets(28)); body.setStyle("-fx-background-color:#f0faf7;");
        Label t2 = new Label("🔄 New Resale Agreement");
        t2.setStyle("-fx-font-family:'"+Theme.FONT_HEADING+"';-fx-font-size:18px;-fx-font-weight:800;-fx-text-fill:"+Theme.TEXT_PRIMARY+";");
        GridPane grid = new GridPane(); grid.setHgap(14); grid.setVgap(14);
        grid.getColumnConstraints().addAll(UIComponents.pct(50),UIComponents.pct(50));
        String[] medNames = db.getApproved().stream().filter(m->user.getId().equals(m.getDonorId())).map(Medicine::getName).distinct().toArray(String[]::new);
        ComboBox<String> medF = medNames.length>0?UIComponents.styledComboBox(medNames):UIComponents.styledComboBox("No approved medicines");
        TextField qtyF   = UIComponents.styledTextField("Units");
        ComboBox<String> discF = UIComponents.styledComboBox("30% off","40% off","50% off","60% off");
        TextField buyerF = UIComponents.styledTextField("Buyer name / organisation");
        TextField startF = UIComponents.styledTextField("Start YYYY-MM-DD"); startF.setText(LocalDate.now().toString());
        TextField endF   = UIComponents.styledTextField("End YYYY-MM-DD");   endF.setText(LocalDate.now().plusMonths(1).toString());
        grid.add(UIComponents.formGroup("Medicine *",medF),0,0,2,1);
        grid.add(UIComponents.formGroup("Quantity *",qtyF),0,1);
        grid.add(UIComponents.formGroup("Discount",discF),1,1);
        grid.add(UIComponents.formGroup("Buyer *",buyerF),0,2,2,1);
        grid.add(UIComponents.formGroup("Start Date",startF),0,3);
        grid.add(UIComponents.formGroup("End Date",endF),1,3);
        Label errLbl = new Label(""); errLbl.setStyle(Theme.labelStyle("12px",Theme.DANGER,false));
        HBox btns = new HBox(10); btns.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = UIComponents.glassButton("Cancel"); cancel.setOnAction(e -> dlg.close());
        Button save = UIComponents.primaryButton("Create Agreement");
        save.setOnAction(e -> {
            if(buyerF.getText().isBlank()||qtyF.getText().isBlank()){errLbl.setText("❌ Fill required fields.");return;}
            try { Integer.parseInt(qtyF.getText().trim()); } catch(Exception ex){errLbl.setText("❌ Invalid quantity.");return;}
            db.addTransaction(new Transaction(null,
                "Resale agreement: "+medF.getValue()+" × "+qtyF.getText()+" @ "+discF.getValue()+" — "+buyerF.getText(),
                Transaction.Type.REVENUE, Double.parseDouble(qtyF.getText())*85*0.6, LocalDate.now(),"Active"));
            showToast("✅ Agreement created for "+buyerF.getText()+" — "+qtyF.getText()+" units of "+medF.getValue());
            dlg.close(); showResalePage();
        });
        btns.getChildren().addAll(cancel, save);
        body.getChildren().addAll(t2, grid, errLbl, btns);
        dlg.setScene(new Scene(body)); dlg.show(); AnimationUtils.scaleIn(body);
    }

    private void openAgreementDetailDialog(String[] ag) {
        Stage dlg = new Stage(); dlg.initOwner(stage); dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Agreement "+ag[0]); dlg.setMinWidth(400); dlg.setMinHeight(300); dlg.setResizable(false);
        VBox body = new VBox(14); body.setPadding(new Insets(24)); body.setStyle("-fx-background-color:#f0faf7;");
        VBox info = UIComponents.glassDarkCard(14); info.setSpacing(10);
        info.getChildren().addAll(
            UIComponents.metricRow("Agreement",ag[0],Theme.TEXT_PRIMARY),
            UIComponents.metricRow("Medicine",ag[1],null),
            UIComponents.metricRow("Quantity",ag[2]+" units",null),
            UIComponents.metricRow("Discount",ag[3],Theme.SUCCESS),
            UIComponents.metricRow("Buyer",ag[4],null),
            UIComponents.metricRow("Period",ag[5]+" → "+ag[6],null),
            UIComponents.metricRow("Status",ag[7],ag[7].equals("Active")?Theme.SUCCESS:Theme.DANGER));
        Button closeBtn = UIComponents.glassButton("Close"); closeBtn.setOnAction(e -> dlg.close());
        body.getChildren().addAll(info, new HBox(){{getChildren().add(closeBtn);}});
        dlg.setScene(new Scene(body)); dlg.show(); AnimationUtils.scaleIn(body);
    }

    private void showToast(String msg) {
        javafx.stage.Stage toast = new javafx.stage.Stage();
        toast.initOwner(stage); toast.initStyle(StageStyle.UNDECORATED); toast.initModality(Modality.NONE);
        javafx.scene.control.Label lbl = new javafx.scene.control.Label(msg);
        lbl.setStyle("-fx-background-color:rgba(0,60,50,0.92);-fx-text-fill:white;-fx-font-family:'DM Sans';-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:12;-fx-padding:14 22 14 22;");
        lbl.setWrapText(true); lbl.setMaxWidth(420);
        javafx.scene.layout.StackPane root = new javafx.scene.layout.StackPane(lbl);
        root.setStyle("-fx-background-color:transparent;");
        toast.setScene(new javafx.scene.Scene(root));
        toast.setX(stage.getX()+stage.getWidth()-460); toast.setY(stage.getY()+stage.getHeight()-90);
        toast.show();
        javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(2400));
        pt.setOnFinished(e -> toast.close()); pt.play();
        AnimationUtils.fadeIn(lbl, 250, 0);
    }
}
