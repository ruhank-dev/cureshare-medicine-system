package com.cureshare.views.dashboard;

import com.cureshare.models.*;
import com.cureshare.utils.*;
import com.cureshare.views.shared.UIComponents;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.util.List;
import java.time.LocalDate;
import java.io.FileWriter;
import java.io.PrintWriter;

public class CharityDashboard extends BaseLayout {

    private final DataStore db = DataStore.getInstance();

    public void show(Stage stage, User user) {
        initialize(stage, user);
        buildScene();
        showHomePage();
    }

    @Override protected void onAddButtonClicked() { showNewRequestPage(); }

    @Override
    protected void buildNavItems(VBox nav) {
        addNavSection(nav,"Overview");
        addNavItem(nav,"📊","Dashboard",          null, this::showHomePage);
        addNavSection(nav,"Medicines");
        addNavItem(nav,"🔍","Available Medicines", null, this::showAvailablePage);
        addNavItem(nav,"📋","My Requests",         null, this::showRequestsPage);
        addNavItem(nav,"➕","New Request",         null, this::showNewRequestPage);
        addNavSection(nav,"Tracking");
        addNavItem(nav,"📦","Allocations",         null, this::showAllocationsPage);
        addNavItem(nav,"📈","Impact Report",       null, this::showImpactPage);
        addNavSection(nav,"Account");
        addNavItem(nav,"⚙️","Settings",            null, this::showSettingsPage);
    }

    private void showHomePage() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));

        // Banner
        VBox banner = UIComponents.glassCard(24); banner.setSpacing(10);
        banner.setStyle(banner.getStyle().replace("rgba(255,255,255,0.60)","linear-gradient(to right,rgba(0,176,144,0.14),rgba(0,120,96,0.08))"));
        Label org = new Label("❤️  "+(user.getOrganization()!=null?user.getOrganization():user.getName()));
        org.setStyle("-fx-font-family:'"+Theme.FONT_HEADING+"';-fx-font-size:20px;-fx-font-weight:800;-fx-text-fill:"+Theme.TEXT_PRIMARY+";");
        Label sub = new Label("You have received 200 medicine units this month, estimated to serve 150 patients. Thank you for your impact!");
        sub.setStyle(Theme.labelStyle("13px",Theme.TEXT_SECONDARY,false)); sub.setWrapText(true);
        Button reqBtn = UIComponents.primaryButton("+ Request Medicines");
        reqBtn.setOnAction(e->showNewRequestPage());
        banner.getChildren().addAll(org,sub,reqBtn);

        GridPane stats = new GridPane(); stats.setHgap(16);
        stats.getColumnConstraints().addAll(UIComponents.pct(25),UIComponents.pct(25),UIComponents.pct(25),UIComponents.pct(25));
        stats.add(UIComponents.statCard("📋","4","Active Requests","Awaiting fulfillment",true),0,0);
        stats.add(UIComponents.statCard("📦","200","Units Received","This month",true),1,0);
        stats.add(UIComponents.statCard("👥","150","Patients Served","Estimated impact",true),2,0);
        stats.add(UIComponents.statCard("✅","12","Fulfilled Requests","All time",true),3,0);

        // Pending requests
        VBox reqCard = UIComponents.glassCard(22); reqCard.setSpacing(12);
        Button newReqBtn = UIComponents.primaryButton("+ New Request");
        newReqBtn.setOnAction(e->showNewRequestPage());
        reqCard.getChildren().add(UIComponents.sectionHeader("My Pending Requests",null,newReqBtn));
        String[][] reqs = {
            {"Antibiotics (General)","150 units","Mar 12","warning","Pending"},
            {"Paracetamol 500mg","200 units","Mar 8","success","Approved"},
            {"ORS Sachets","50 packs","Mar 1","info","Dispatched"},
            {"Insulin (Diabetes Support)","30 units","Feb 28","success","Fulfilled"},
        };
        for (String[] r : reqs) {
            HBox row = new HBox(14); row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(12,14,12,14)); row.setStyle(Theme.glassDarkStyle());
            Label nm = new Label(r[0]); nm.setStyle(Theme.labelStyle("13.5px",Theme.TEXT_PRIMARY,true)); HBox.setHgrow(nm,Priority.ALWAYS);
            Label qty = new Label(r[1]); qty.setStyle(Theme.labelStyle("12px",Theme.TEAL_600,true));
            Label dt = new Label(r[2]); dt.setStyle(Theme.labelStyle("12px",Theme.TEXT_MUTED,false));
            Button viewBtn = UIComponents.smallButton("Details",false);
            final String[] rc = r;
            viewBtn.setOnAction(e->showInfo("Request Details","Medicine: "+rc[0]+"\nQuantity: "+rc[1]+"\nDate: "+rc[2]+"\nStatus: "+rc[4]));
            row.getChildren().addAll(nm,qty,dt,UIComponents.badge(r[4],r[3]),viewBtn);
            reqCard.getChildren().add(row);
        }
        page.getChildren().addAll(banner,stats,reqCard);
        AnimationUtils.staggerFadeIn(banner,stats,reqCard);
        showContent(page,"Charity Dashboard","Welcome, "+(user.getOrganization()!=null?user.getOrganization():user.getName()));
    }

    private void showAvailablePage() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));
        List<Medicine> avail = db.getApproved();
        HBox toolbar = new HBox(12); toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getChildren().addAll(UIComponents.tabsBar("All","Antibiotics","Diabetes","Cardiac","Analgesic"),
            new Region(){{HBox.setHgrow(this,Priority.ALWAYS);}},
            UIComponents.glassButton("🔽 Filter by Category"));

        FlowPane grid = new FlowPane(16,16); grid.setPrefWrapLength(Double.MAX_VALUE);
        for (Medicine m : avail) {
            VBox card = UIComponents.glassCard(18); card.setSpacing(8); card.setMinWidth(240); card.setMaxWidth(260);
            Label nm = new Label(m.getName()); nm.setStyle(Theme.labelStyle("13.5px",Theme.TEXT_PRIMARY,true));
            Label cat = new Label(m.getCategory()!=null?m.getCategory():"—"); cat.setStyle(Theme.labelStyle("11px",Theme.TEXT_MUTED,false));
            Label qty = new Label("Available: "+m.getQuantity()+" units"); qty.setStyle(Theme.labelStyle("12px",Theme.TEAL_600,true));
            Label exp = new Label("Expiry: "+m.getExpiryDate()); exp.setStyle(Theme.labelStyle("11px",m.isExpiringSoon()?Theme.WARNING:Theme.TEXT_MUTED,false));
            Button reqBtn = UIComponents.primaryButton("Request →");
            reqBtn.setMaxWidth(Double.MAX_VALUE);
            reqBtn.setOnAction(e->openRequestDialog(m.getName()));
            card.getChildren().addAll(nm,cat,qty,exp,reqBtn);
            AnimationUtils.addHoverScale(card,1.02); grid.getChildren().add(card);
        }
        if (avail.isEmpty()) {
            Label empty = new Label("No approved medicines available at this time. Check back soon!");
            empty.setStyle(Theme.labelStyle("14px",Theme.TEXT_MUTED,false)); grid.getChildren().add(empty);
        }
        page.getChildren().addAll(toolbar,grid);
        AnimationUtils.staggerFadeIn(toolbar,grid);
        showContent(page,"Available Medicines","Browse approved medicines ready for allocation");
    }

    private void showNewRequestPage() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));
        VBox card = UIComponents.glassCard(28); card.setSpacing(18); card.setMaxWidth(600);
        Label title2=new Label("➕ New Medicine Request");title2.setStyle("-fx-font-family:'"+Theme.FONT_HEADING+"';-fx-font-size:20px;-fx-font-weight:800;-fx-text-fill:"+Theme.TEXT_PRIMARY+";");
        Label sub=new Label("Submit a request for medicines. Our admin team will review and allocate available stock within 48 hours.");sub.setStyle(Theme.labelStyle("13px",Theme.TEXT_MUTED,false));sub.setWrapText(true);

        GridPane grid=new GridPane();grid.setHgap(14);grid.setVgap(14);
        grid.getColumnConstraints().addAll(UIComponents.pct(50),UIComponents.pct(50));
        TextField medF=UIComponents.styledTextField("e.g. Antibiotics, Paracetamol, Insulin…");
        ComboBox<String> catF=UIComponents.styledComboBox("Antibiotic","Analgesic","Cardiac","Diabetes","Vitamins","Gastro","Pediatric","Emergency","Other");
        TextField qtyF=UIComponents.styledTextField("Number of units needed");
        ComboBox<String> urgF=UIComponents.styledComboBox("Routine (7+ days)","Moderate (3-7 days)","Urgent (1-3 days)","Critical (same day)");
        TextField dateF=UIComponents.styledTextField("Required by (YYYY-MM-DD)");
        TextField patF=UIComponents.styledTextField("Estimated patients to serve");
        TextArea justF=UIComponents.styledTextArea("Why do you need these medicines? Who will benefit? Any clinical details…");justF.setPrefRowCount(4);
        grid.add(UIComponents.formGroup("Medicine Required *",medF),0,0,2,1);
        grid.add(UIComponents.formGroup("Category",catF),0,1);
        grid.add(UIComponents.formGroup("Quantity Needed *",qtyF),1,1);
        grid.add(UIComponents.formGroup("Urgency Level",urgF),0,2);
        grid.add(UIComponents.formGroup("Required By Date",dateF),1,2);
        grid.add(UIComponents.formGroup("Patients to Serve",patF),0,3,2,1);
        grid.add(UIComponents.formGroup("Justification *",justF),0,4,2,1);

        Label errLbl=new Label("");errLbl.setStyle(Theme.labelStyle("12px",Theme.DANGER,false));
        Label successLbl=new Label("");successLbl.setStyle(Theme.labelStyle("13px",Theme.SUCCESS,true));

        HBox btns=new HBox(12);btns.setAlignment(Pos.CENTER_RIGHT);
        Button clearBtn=UIComponents.glassButton("Clear");
        clearBtn.setOnAction(e->{medF.clear();qtyF.clear();dateF.clear();patF.clear();justF.clear();errLbl.setText("");successLbl.setText("");});
        Button submitBtn=UIComponents.primaryButton("Submit Request →");
        submitBtn.setOnAction(e->{
            if(medF.getText().isBlank()||qtyF.getText().isBlank()||justF.getText().isBlank()){errLbl.setText("❌ Please fill all required fields.");return;}
            successLbl.setText("✅ Request submitted successfully!\nRef: #REQ-"+String.format("%04d",(int)(Math.random()*9999)+1)+"\nYou will be notified when approved. Urgency: "+urgF.getValue()+".");
            errLbl.setText("");AnimationUtils.fadeIn(successLbl,300,0);
            medF.clear();qtyF.clear();dateF.clear();patF.clear();justF.clear();
        });
        btns.getChildren().addAll(clearBtn,submitBtn);
        card.getChildren().addAll(title2,sub,grid,errLbl,successLbl,btns);
        page.getChildren().add(card);AnimationUtils.fadeIn(card,350,0);
        showContent(page,"New Medicine Request","Request medicines for your organisation");
    }

    private void showRequestsPage() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));
        VBox card = UIComponents.glassCard(22); card.setSpacing(12);
        Button newBtn=UIComponents.primaryButton("+ New Request");newBtn.setOnAction(e->showNewRequestPage());
        Button exportBtn=UIComponents.glassButton("📤 Export CSV");
        exportBtn.setOnAction(e->{ String p=CsvExporter.exportMyRequests(stage,user.getId()); if(p!=null) showToast("✅ Saved: "+p); });
        HBox hdBtns=new HBox(8); hdBtns.getChildren().addAll(exportBtn,newBtn);
        card.getChildren().add(UIComponents.sectionHeader("All My Requests","Complete request history",hdBtns));
        card.getChildren().add(UIComponents.tableHeader(new String[]{"Medicine","Qty","Date","Urgency","Status","Action"},new double[]{0,80,100,110,100,110}));
        String[][] reqs={
            {"Antibiotics (General)","150 units","Mar 12","Moderate","Pending"},
            {"Paracetamol 500mg","200 units","Mar 8","Routine","Approved"},
            {"ORS Sachets","50 packs","Mar 1","Urgent","Dispatched"},
            {"Insulin (Diabetes Support)","30 units","Feb 28","Routine","Fulfilled"},
            {"Vitamin D Supplements","100 units","Feb 20","Routine","Fulfilled"},
        };
        for(int i=0;i<reqs.length;i++){
            String[] r=reqs[i];HBox row=UIComponents.tableRow(i%2==1);
            Label nm=new Label(r[0]);nm.setStyle(Theme.labelStyle("13px",Theme.TEXT_PRIMARY,true));HBox.setHgrow(nm,Priority.ALWAYS);row.getChildren().add(nm);
            row.getChildren().add(UIComponents.cell(r[1],80));row.getChildren().add(UIComponents.cell(r[2],100));row.getChildren().add(UIComponents.cell(r[3],110));
            String bt=r[4].equals("Approved")||r[4].equals("Fulfilled")?"success":r[4].equals("Dispatched")?"info":"warning";
            row.getChildren().add(UIComponents.badge(r[4],bt));
            HBox acts=new HBox(6);acts.setAlignment(Pos.CENTER_LEFT);acts.setMinWidth(110);
            Button viewBtn=UIComponents.smallButton("Details",false);final String[]rc=r;
            viewBtn.setOnAction(e->showInfo("Request: "+rc[0],"Quantity: "+rc[1]+"\nDate: "+rc[2]+"\nUrgency: "+rc[3]+"\nStatus: "+rc[4]));
            if(r[4].equals("Pending")){Button cancelBtn=UIComponents.dangerButton("Cancel");cancelBtn.setStyle(cancelBtn.getStyle()+"-fx-font-size:11px;-fx-padding:5 9 5 9;");cancelBtn.setOnAction(e->showToast("✅ Request cancelled."));acts.getChildren().add(cancelBtn);}
            acts.getChildren().add(0,viewBtn);row.getChildren().add(acts);
            card.getChildren().add(row);AnimationUtils.fadeIn(row,260,i*40);
        }
        page.getChildren().add(card);showContent(page,"My Requests","Track all your medicine requests");
    }

    private void showAllocationsPage() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));
        VBox card = UIComponents.glassCard(22); card.setSpacing(12);
        card.getChildren().add(UIComponents.sectionHeader("Medicine Allocations","Medicines allocated to your organisation"));
        card.getChildren().add(UIComponents.tableHeader(new String[]{"Medicine","Qty","Date","Status","Action"},new double[]{0,100,100,100,130}));
        String[][] allocs={{"Amoxicillin 500mg","200 units","Mar 13, 2026","Dispatched","info"},{"ORS Sachets","50 packs","Mar 1, 2026","Delivered","success"},{"Vitamin D","100 units","Feb 22, 2026","Delivered","success"}};
        for(int i=0;i<allocs.length;i++){
            String[] a=allocs[i]; final int idx=i; HBox row=UIComponents.tableRow(i%2==1);
            Label nm=new Label(a[0]);nm.setStyle(Theme.labelStyle("13px",Theme.TEXT_PRIMARY,true));HBox.setHgrow(nm,Priority.ALWAYS);row.getChildren().add(nm);
            row.getChildren().add(UIComponents.cell(a[1],100));row.getChildren().add(UIComponents.cell(a[2],100));
            row.getChildren().add(UIComponents.badge(a[3],a[4]));
            HBox acts=new HBox(6);acts.setAlignment(Pos.CENTER_LEFT);acts.setMinWidth(130);
            Button ackBtn=UIComponents.smallButton("✓ Acknowledge",true);
            ackBtn.setOnAction(e->showToast("✅ Receipt acknowledged for "+a[0]));
            Button rcptBtn=UIComponents.smallButton("🧾 Receipt",false);
            rcptBtn.setOnAction(e -> {
                javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
                fc.setTitle("Save Delivery Receipt");
                fc.setInitialFileName("receipt_alloc_"+String.format("%04d",idx)+"_"+java.time.LocalDate.now()+".txt");
                fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Text Files","*.txt"));
                java.io.File docs = new java.io.File(System.getProperty("user.home"),"Documents");
                if(docs.exists()) fc.setInitialDirectory(docs);
                java.io.File f = fc.showSaveDialog(stage);
                if(f != null) {
                    try(java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(f))) {
                        pw.println("===============================================");
                        pw.println("     CURESHARE BMS — DELIVERY RECEIPT");
                        pw.println("===============================================");
                        pw.println("Receipt No : ALLOC-"+String.format("%04d",idx));
                        pw.println("Date       : "+java.time.LocalDate.now());
                        pw.println("-----------------------------------------------");
                        pw.println("Organisation : "+(user.getOrganization()!=null?user.getOrganization():user.getName()));
                        pw.println("-----------------------------------------------");
                        pw.println("Medicine  : "+a[0]);
                        pw.println("Quantity  : "+a[1]);
                        pw.println("Date      : "+a[2]);
                        pw.println("Status    : "+a[3]);
                        pw.println("-----------------------------------------------");
                        pw.println("Thank you for your continued partnership.");
                        pw.println("CureShare — Smart Medicine Redistribution BMS");
                        pw.println("===============================================");
                        showToast("🧾 Receipt saved: "+f.getAbsolutePath());
                    } catch(Exception ex) { showToast("❌ Could not save receipt: "+ex.getMessage()); }
                }
            });
            acts.getChildren().addAll(ackBtn,rcptBtn);row.getChildren().add(acts);
            card.getChildren().add(row);AnimationUtils.fadeIn(row,260,idx*40);
        }
        page.getChildren().add(card);showContent(page,"Allocations","Medicines dispatched to your organisation");
    }

    private void showImpactPage() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));
        GridPane stats = new GridPane(); stats.setHgap(16);
        stats.getColumnConstraints().addAll(UIComponents.pct(25),UIComponents.pct(25),UIComponents.pct(25),UIComponents.pct(25));
        stats.add(UIComponents.statCard("💊","200","Units Received","This month",true),0,0);
        stats.add(UIComponents.statCard("👥","150","Patients Served","Estimated",true),1,0);
        stats.add(UIComponents.statCard("₨","₨21K","Value Received","Medicine value",true),2,0);
        stats.add(UIComponents.statCard("📅","6","Months Active","As partner",true),3,0);

        VBox card = UIComponents.glassCard(22); card.setSpacing(14);
        card.getChildren().add(UIComponents.sectionHeader("Monthly Impact","Units received per month"));
        card.getChildren().add(UIComponents.barChart(new String[]{"Oct","Nov","Dec","Jan","Feb","Mar"},new double[]{80,120,60,140,90,200},200,120));

        VBox detailCard = UIComponents.glassCard(22); detailCard.setSpacing(10);
        detailCard.getChildren().add(UIComponents.sectionHeader("Impact Breakdown",null));
        detailCard.getChildren().addAll(
            UIComponents.metricRow("Total patients served","430 (all time)",Theme.TEXT_PRIMARY),
            UIComponents.metricRow("Medicine categories received","6 types",null),
            UIComponents.metricRow("Avg turnaround time","2.3 days",null),
            UIComponents.metricRow("Satisfaction rating","4.8 / 5.0",Theme.SUCCESS),
            UIComponents.metricRow("Partner since","October 2025",null));

        Button genReportBtn = UIComponents.primaryButton("📋 Generate Full Impact Report");
        genReportBtn.setOnAction(e -> ReportViewer.showDonationImpactReport(stage));
        detailCard.getChildren().add(genReportBtn);

        // Charity feedback & rating form
        VBox rateCard = UIComponents.glassCard(22); rateCard.setSpacing(12);
        Label rTitle = new Label("⭐ Rate CureShare Service");
        rTitle.setStyle("-fx-font-family:'"+Theme.FONT_HEADING+"';-fx-font-size:16px;-fx-font-weight:800;-fx-text-fill:"+Theme.TEXT_PRIMARY+";");
        Label rSub = new Label("Help us improve — rate your allocation experience.");
        rSub.setStyle(Theme.labelStyle("12px",Theme.TEXT_MUTED,false));
        HBox starRow2 = new HBox(8); starRow2.setAlignment(Pos.CENTER_LEFT);
        final int[] selStars = {0};
        javafx.scene.control.Label[] sBtns = new javafx.scene.control.Label[5];
        for (int si=0;si<5;si++) {
            final int sv = si+1;
            final int idx = si;
            javafx.scene.control.Label s2 = new javafx.scene.control.Label("☆");
            s2.setStyle("-fx-font-size:26px;-fx-cursor:hand;-fx-text-fill:"+Theme.TEXT_MUTED+";");
            sBtns[si]=s2;
            s2.setOnMouseClicked(ev->{ selStars[0]=sv; for(int j=0;j<5;j++){sBtns[j].setText(j<sv?"★":"☆");sBtns[j].setStyle("-fx-font-size:26px;-fx-cursor:hand;-fx-text-fill:"+(j<sv?Theme.WARNING:Theme.TEXT_MUTED)+";");} });
            s2.setOnMouseEntered(ev->{ for(int j=0;j<5;j++){sBtns[j].setText(j<=idx?"★":"☆");sBtns[j].setStyle("-fx-font-size:26px;-fx-cursor:hand;-fx-text-fill:"+(j<=idx?Theme.WARNING:Theme.TEXT_MUTED)+";");} });
            s2.setOnMouseExited(ev->{ for(int j=0;j<5;j++){sBtns[j].setText(j<selStars[0]?"★":"☆");sBtns[j].setStyle("-fx-font-size:26px;-fx-cursor:hand;-fx-text-fill:"+(j<selStars[0]?Theme.WARNING:Theme.TEXT_MUTED)+";");} });
            starRow2.getChildren().add(s2);
        }
        ComboBox<String> aspectF2 = UIComponents.styledComboBox("Overall Service","Medicine Quality","Delivery Speed","Quantity Accuracy","Communication");
        TextArea rComment = UIComponents.styledTextArea("Any feedback for the CureShare team…"); rComment.setPrefRowCount(3);
        Label rMsg2 = new Label(""); rMsg2.setStyle(Theme.labelStyle("12px",Theme.SUCCESS,true));
        Button submitR = UIComponents.primaryButton("Submit Feedback →"); submitR.setMaxWidth(Double.MAX_VALUE);
        submitR.setOnAction(e->{ if(selStars[0]==0){rMsg2.setText("❌ Select a rating.");rMsg2.setStyle(Theme.labelStyle("12px",Theme.DANGER,true));return;}
            db.addRating(new DataStore.Rating(user.getId(),user.getOrganization()!=null?user.getOrganization():user.getName(),"CURESHARE","CureShare BMS",aspectF2.getValue(),selStars[0],rComment.getText().trim()));
            rMsg2.setText("✅ Thank you! Your "+selStars[0]+"-star feedback has been recorded.");
            rMsg2.setStyle(Theme.labelStyle("12px",Theme.SUCCESS,true));
            AnimationUtils.fadeIn(rMsg2,300,0);
            for(javafx.scene.control.Label sb:sBtns){sb.setText("☆");sb.setStyle("-fx-font-size:26px;-fx-cursor:hand;-fx-text-fill:"+Theme.TEXT_MUTED+";");}
            selStars[0]=0; rComment.clear(); });
        rateCard.getChildren().addAll(rTitle,rSub,UIComponents.formGroup("Stars",starRow2),UIComponents.formGroup("Aspect",aspectF2),UIComponents.formGroup("Comments",rComment),rMsg2,submitR);

        page.getChildren().addAll(stats,card,detailCard,rateCard);
        AnimationUtils.staggerFadeIn(stats,card,detailCard);
        showContent(page,"Impact Report","Your organisation's contribution metrics");
    }

    private void showSettingsPage() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));
        VBox card = UIComponents.glassCard(28); card.setSpacing(16); card.setMaxWidth(560);
        Label title2=new Label("⚙️ Organisation Settings");title2.setStyle("-fx-font-family:'"+Theme.FONT_HEADING+"';-fx-font-size:20px;-fx-font-weight:800;-fx-text-fill:"+Theme.TEXT_PRIMARY+";");
        TextField orgF=UIComponents.styledTextField("Organisation name");if(user.getOrganization()!=null)orgF.setText(user.getOrganization());
        TextField emailF=UIComponents.styledTextField("Email");emailF.setText(user.getEmail());
        TextField phoneF=UIComponents.styledTextField("Phone");if(user.getPhone()!=null)phoneF.setText(user.getPhone());
        TextField cityF=UIComponents.styledTextField("City");cityF.setText("Islamabad");
        TextField regF=UIComponents.styledTextField("NGO Registration Number");regF.setText("NGO-2024-0441");
        Label saveMsg=new Label("");saveMsg.setStyle(Theme.labelStyle("12px",Theme.SUCCESS,true));
        Button saveBtn=UIComponents.primaryButton("💾 Save Changes");saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e->{user.setOrganization(orgF.getText().trim());user.setEmail(emailF.getText().trim());user.setPhone(phoneF.getText().trim());saveMsg.setText("✅ Settings saved!");AnimationUtils.fadeIn(saveMsg,300,0);});
        card.getChildren().addAll(title2,UIComponents.formGroup("Organisation Name",orgF),UIComponents.formGroup("Email",emailF),UIComponents.formGroup("Phone",phoneF),UIComponents.formGroup("City",cityF),UIComponents.formGroup("Registration No.",regF),saveMsg,saveBtn);
        page.getChildren().add(card);showContent(page,"Settings","Organisation account settings");
    }

    private void openRequestDialog(String medicineName) {
        Stage dlg=new Stage();dlg.initOwner(stage);dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Request: "+medicineName);dlg.setMinWidth(440);dlg.setMinHeight(340);
        VBox card=new VBox(16);card.setPadding(new Insets(28));card.setStyle("-fx-background-color:rgba(255,255,255,0.97);");
        Label title2=new Label("Request: "+medicineName);title2.setStyle("-fx-font-family:'"+Theme.FONT_HEADING+"';-fx-font-size:18px;-fx-font-weight:800;-fx-text-fill:"+Theme.TEXT_PRIMARY+";");
        TextField qtyF=UIComponents.styledTextField("Quantity needed");
        ComboBox<String> urgF=UIComponents.styledComboBox("Routine","Moderate","Urgent","Critical");
        TextField dateF=UIComponents.styledTextField("Required by (YYYY-MM-DD)");
        TextArea justF=UIComponents.styledTextArea("Justification…");justF.setPrefRowCount(3);
        Label errLbl=new Label("");errLbl.setStyle(Theme.labelStyle("12px",Theme.DANGER,false));
        HBox btns=new HBox(10);btns.setAlignment(Pos.CENTER_RIGHT);
        Button cancel=UIComponents.glassButton("Cancel");cancel.setOnAction(e->dlg.close());
        Button submit=UIComponents.primaryButton("Submit Request");
        submit.setOnAction(e->{if(qtyF.getText().isBlank()){errLbl.setText("❌ Enter quantity.");return;}showToast("✅ Request for "+qtyF.getText()+" units of "+medicineName+" submitted!");dlg.close();});
        btns.getChildren().addAll(cancel,submit);
        card.getChildren().addAll(title2,UIComponents.formGroup("Quantity *",qtyF),UIComponents.formGroup("Urgency",urgF),UIComponents.formGroup("Required By",dateF),UIComponents.formGroup("Justification",justF),errLbl,btns);
        dlg.setScene(new Scene(card));dlg.show();AnimationUtils.scaleIn(card);
    }

    private void showInfo(String t,String m){Alert a=new Alert(Alert.AlertType.INFORMATION);a.setTitle(t);a.setHeaderText(null);a.setContentText(m);a.showAndWait();}
    private void showToast(String msg){ javafx.stage.Stage toast=new javafx.stage.Stage(); toast.initOwner(stage); toast.initStyle(javafx.stage.StageStyle.UNDECORATED); toast.initModality(javafx.stage.Modality.NONE); javafx.scene.control.Label lbl=new javafx.scene.control.Label(msg); lbl.setStyle("-fx-background-color:rgba(0,60,50,0.92);-fx-text-fill:white;-fx-font-family:'DM Sans';-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:12;-fx-padding:14 22 14 22;"); lbl.setWrapText(true); lbl.setMaxWidth(400); javafx.scene.layout.StackPane root=new javafx.scene.layout.StackPane(lbl); root.setStyle("-fx-background-color:transparent;"); toast.setScene(new javafx.scene.Scene(root)); toast.setX(stage.getX()+stage.getWidth()-460); toast.setY(stage.getY()+stage.getHeight()-90); toast.show(); javafx.animation.PauseTransition pt=new javafx.animation.PauseTransition(javafx.util.Duration.millis(2400)); pt.setOnFinished(e->toast.close()); pt.play(); }
}
