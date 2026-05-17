package com.cureshare.views.dashboard;

import com.cureshare.models.*;
import com.cureshare.utils.*;
import com.cureshare.views.shared.UIComponents;
import javafx.animation.PauseTransition;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.Duration;

import java.time.LocalDate;
import java.util.List;

public class HouseholdDashboard extends BaseLayout {

    private final DataStore db = DataStore.getInstance();

    public void show(Stage stage, User user) {
        initialize(stage, user);
        buildScene();
        showHomePage();
    }

    @Override protected void onAddButtonClicked() { openSubmitMedicineDialog(); }

    @Override
    protected void buildNavItems(VBox nav) {
        addNavSection(nav,"My Account");
        addNavItem(nav,"🏠","Home",                null, this::showHomePage);
        addNavItem(nav,"💊","Submit Medicine",     null, this::showSubmitPage);
        addNavItem(nav,"📦","My Submissions",       null, this::showSubmissionsPage);
        addNavItem(nav,"🚗","Schedule Pickup",      null, this::showSchedulePickupPage);
        addNavSection(nav,"Rewards");
        addNavItem(nav,"⭐","My Points & Credits", null, this::showPointsPage);
        addNavItem(nav,"📜","Donation History",    null, this::showHistoryPage);
        addNavSection(nav,"Support");
        addNavItem(nav,"💬","Feedback & Support",  null, this::showSupportPage);
        addNavItem(nav,"⚙️","Account Settings",    null, this::showSettingsPage);
    }

    // ── HOME ──────────────────────────────────────────────────────────────
    private void showHomePage() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));

        List<Medicine> mine = db.getByDonor(user.getId());
        long approved = mine.stream().filter(m->m.getStatus()==Medicine.Status.APPROVED).count();
        long pending  = mine.stream().filter(m->m.getStatus()==Medicine.Status.PENDING).count();

        // Welcome banner
        VBox banner = UIComponents.glassCard(24); banner.setSpacing(10);
        banner.setStyle(banner.getStyle().replace("rgba(255,255,255,0.60)","linear-gradient(to right,rgba(0,176,144,0.14),rgba(0,120,96,0.08))"));
        HBox bannerRow = new HBox(20); bannerRow.setAlignment(Pos.CENTER_LEFT);
        VBox bannerTxt = new VBox(8);
        Label hi = new Label("Welcome back, "+user.getName().split(" ")[0]+"! 👋");
        hi.setStyle("-fx-font-family:'"+Theme.FONT_HEADING+"';-fx-font-size:22px;-fx-font-weight:800;-fx-text-fill:"+Theme.TEXT_PRIMARY+";");
        Label sub = new Label("You've donated "+mine.size()+" medicines and earned "+user.getPoints()+" points so far. Every donation matters!");
        sub.setStyle(Theme.labelStyle("13px",Theme.TEXT_SECONDARY,false)); sub.setWrapText(true);
        Button submitBtn = UIComponents.primaryButton("+ Submit New Medicine");
        submitBtn.setOnAction(e->openSubmitMedicineDialog());
        bannerTxt.getChildren().addAll(hi,sub,submitBtn); HBox.setHgrow(bannerTxt,Priority.ALWAYS);
        Label bannerIco = new Label("💊"); bannerIco.setStyle("-fx-font-size:64px;-fx-opacity:0.45;");
        bannerRow.getChildren().addAll(bannerTxt,bannerIco); banner.getChildren().add(bannerRow);

        // Stats
        GridPane stats = new GridPane(); stats.setHgap(16);
        stats.getColumnConstraints().addAll(UIComponents.pct(25),UIComponents.pct(25),UIComponents.pct(25),UIComponents.pct(25));
        stats.add(UIComponents.statCard("💊",String.valueOf(mine.size()),"Medicines Submitted","Total",true),0,0);
        stats.add(UIComponents.statCard("✅",String.valueOf(approved),"Approved","Successfully verified",true),1,0);
        stats.add(UIComponents.statCard("⏳",String.valueOf(pending),"Pending Review","Awaiting verification",pending==0),2,0);
        stats.add(UIComponents.statCard("⭐",String.valueOf(user.getPoints()),"Points Earned","Redeem for cashback",true),3,0);

        // Recent submissions
        VBox recentCard = UIComponents.glassCard(22); recentCard.setSpacing(12);
        Button viewAllBtn = UIComponents.glassButton("View All");
        viewAllBtn.setOnAction(e->showSubmissionsPage());
        recentCard.getChildren().add(UIComponents.sectionHeader("My Recent Submissions",null,viewAllBtn));

        List<Medicine> recent = mine.subList(0, Math.min(4, mine.size()));
        for (Medicine m : recent) {
            HBox row = new HBox(14); row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(12,14,12,14)); row.setStyle(Theme.glassDarkStyle());
            Label ico = new Label("💊"); ico.setStyle("-fx-font-size:20px;");
            VBox info = new VBox(2);
            Label nm = new Label(m.getName()); nm.setStyle(Theme.labelStyle("13.5px",Theme.TEXT_PRIMARY,true));
            Label dt = new Label("Submitted "+m.getSubmittedDate().toString()); dt.setStyle(Theme.labelStyle("11px",Theme.TEXT_MUTED,false));
            info.getChildren().addAll(nm,dt); HBox.setHgrow(info,Priority.ALWAYS);
            String bt = switch(m.getStatus()){case APPROVED->"success";case REJECTED->"danger";default->"warning";};
            Button viewBtn = UIComponents.smallButton("Details",false);
            viewBtn.setOnAction(e->openMedicineDetailDialog(m));
            row.getChildren().addAll(ico,info,UIComponents.badge(m.getStatusLabel(),bt),viewBtn);
            recentCard.getChildren().add(row);
        }
        if (recent.isEmpty()) {
            Label noSub = new Label("No submissions yet. Click 'Submit New Medicine' to get started!");
            noSub.setStyle(Theme.labelStyle("13px",Theme.TEXT_MUTED,false)); noSub.setPadding(new Insets(12));
            recentCard.getChildren().add(noSub);
        }

        page.getChildren().addAll(banner,stats,recentCard);
        AnimationUtils.staggerFadeIn(banner,stats,recentCard);
        showContent(page,"Home","Welcome back, "+user.getName());
    }

    // ── SUBMIT MEDICINE ───────────────────────────────────────────────────
    private void showSubmitPage() { openSubmitMedicineDialog(); showHomePage(); }

    private void openSubmitMedicineDialog() {
        Stage dlg = new Stage(); dlg.initOwner(stage); dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("💊 Submit Unused Medicine"); dlg.setMinWidth(560); dlg.setMinHeight(540);
        VBox card = new VBox(18); card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color:rgba(255,255,255,0.97);");

        Label title = new Label("💊  Submit Unused Medicine");
        title.setStyle("-fx-font-family:'"+Theme.FONT_HEADING+"';-fx-font-size:20px;-fx-font-weight:800;-fx-text-fill:"+Theme.TEXT_PRIMARY+";");
        Label subtitle = new Label("Fill in the details — our team will verify and collect your medicine within 24 hours.");
        subtitle.setStyle(Theme.labelStyle("13px",Theme.TEXT_MUTED,false)); subtitle.setWrapText(true);

        GridPane grid = new GridPane(); grid.setHgap(14); grid.setVgap(14);
        grid.getColumnConstraints().addAll(UIComponents.pct(50),UIComponents.pct(50));
        TextField nameF   = UIComponents.styledTextField("e.g. Paracetamol 500mg");
        ComboBox<String> catF = UIComponents.styledComboBox("Antibiotic","Analgesic","Cardiac","Diabetes","Vitamins","Gastro","Other");
        TextField batchF  = UIComponents.styledTextField("Batch number (check the box)");
        TextField expiryF = UIComponents.styledTextField("YYYY-MM-DD");
        TextField qtyF    = UIComponents.styledTextField("Number of units / tablets");
        ComboBox<String> condF = UIComponents.styledComboBox("Sealed / Unopened","Partial Pack","Single Dose Strip");
        TextArea notesF   = UIComponents.styledTextArea("Any additional information about the medicine…"); notesF.setPrefRowCount(3);
        CheckBox pickupBox = new CheckBox("Request home pickup (we'll schedule automatically)");
        pickupBox.setStyle("-fx-font-family:'"+Theme.FONT_BODY+"';-fx-font-size:13px;-fx-text-fill:"+Theme.TEXT_PRIMARY+";");
        pickupBox.setSelected(true);

        grid.add(UIComponents.formGroup("Medicine Name *",nameF),0,0,2,1);
        grid.add(UIComponents.formGroup("Category",catF),0,1);
        grid.add(UIComponents.formGroup("Batch Number",batchF),1,1);
        grid.add(UIComponents.formGroup("Expiry Date *",expiryF),0,2);
        grid.add(UIComponents.formGroup("Quantity *",qtyF),1,2);
        grid.add(UIComponents.formGroup("Condition",condF),0,3,2,1);
        grid.add(UIComponents.formGroup("Notes",notesF),0,4,2,1);
        grid.add(pickupBox,0,5,2,1);

        Label errLbl = new Label(""); errLbl.setStyle(Theme.labelStyle("12px",Theme.DANGER,false));
        Label successLbl = new Label(""); successLbl.setStyle(Theme.labelStyle("13px",Theme.SUCCESS,true));

        HBox btns = new HBox(10); btns.setAlignment(Pos.CENTER_RIGHT);
        Button clearBtn = UIComponents.glassButton("Clear Form");
        clearBtn.setOnAction(e->{nameF.clear();expiryF.clear();qtyF.clear();batchF.clear();notesF.clear();errLbl.setText("");successLbl.setText("");});
        Button submitBtn = UIComponents.primaryButton("Submit Medicine →");
        submitBtn.setOnAction(e -> {
            if(nameF.getText().isBlank()||expiryF.getText().isBlank()||qtyF.getText().isBlank()){
                errLbl.setText("❌ Please fill all required fields (marked with *)."); successLbl.setText(""); return;
            }
            try {
                Medicine med = new Medicine();
                med.setName(nameF.getText().trim()); med.setCategory(catF.getValue());
                med.setBatchNumber(batchF.getText().isBlank()?"N/A":batchF.getText().trim());
                med.setExpiryDate(LocalDate.parse(expiryF.getText().trim()));
                med.setQuantity(Integer.parseInt(qtyF.getText().trim()));
                med.setSource("Household"); med.setDonorId(user.getId()); med.setNotes(notesF.getText());
                db.addMedicine(med);
                user.setPoints(user.getPoints()+10);
                successLbl.setText("✅ Submitted! +"+(10)+" points earned."+(pickupBox.isSelected()?" Pickup will be scheduled.":""));
                errLbl.setText("");
                AnimationUtils.fadeIn(successLbl,300,0);
                PauseTransition pt = new PauseTransition(Duration.millis(2000));
                pt.setOnFinished(ev->{dlg.close();showHomePage();});
                pt.play();
            } catch(Exception ex){errLbl.setText("❌ Invalid date (use YYYY-MM-DD) or quantity.");}
        });
        btns.getChildren().addAll(clearBtn,submitBtn);
        card.getChildren().addAll(title,subtitle,grid,errLbl,successLbl,btns);
        StackPane root = new StackPane(card); root.setStyle("-fx-background-color:#f0faf7;");
        ScrollPane scroll = new ScrollPane(root); scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:transparent;-fx-background-color:#f0faf7;-fx-border-width:0;");
        dlg.setScene(new Scene(scroll)); dlg.show(); AnimationUtils.scaleIn(card);
    }

    // ── MY SUBMISSIONS ────────────────────────────────────────────────────
    private void showSubmissionsPage() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));
        List<Medicine> mine = db.getByDonor(user.getId());

        VBox card = UIComponents.glassCard(22); card.setSpacing(12);
        Button addBtn = UIComponents.primaryButton("+ Submit New");
        addBtn.setOnAction(e->openSubmitMedicineDialog());
        Button exportBtn = UIComponents.glassButton("📤 Export CSV");
        exportBtn.setOnAction(e->{ String p=CsvExporter.exportMySubmissions(stage,user.getId()); if(p!=null) showToast2("✅ Saved: "+p); });
        HBox hdBtns = new HBox(8); hdBtns.getChildren().addAll(exportBtn,addBtn);
        card.getChildren().add(UIComponents.sectionHeader("My Submissions","All medicines you have submitted",hdBtns));

        if (mine.isEmpty()) {
            Label empty = new Label("No submissions yet. Start by clicking '+ Submit New'!");
            empty.setStyle(Theme.labelStyle("13px",Theme.TEXT_MUTED,false)); empty.setPadding(new Insets(20));
            card.getChildren().add(empty);
        } else {
            card.getChildren().add(UIComponents.tableHeader(new String[]{"Medicine","Category","Batch","Expiry","Qty","Status","Actions"},new double[]{0,100,80,90,55,95,120}));
            for (int i=0;i<mine.size();i++) {
                Medicine m = mine.get(i);
                HBox row = UIComponents.tableRow(i%2==1);
                Label nm = new Label(m.getName()); nm.setStyle(Theme.labelStyle("13px",Theme.TEXT_PRIMARY,true)); HBox.setHgrow(nm,Priority.ALWAYS); row.getChildren().add(nm);
                row.getChildren().add(UIComponents.cell(m.getCategory()!=null?m.getCategory():"—",100));
                row.getChildren().add(UIComponents.cell(m.getBatchNumber()!=null?m.getBatchNumber():"—",80));
                row.getChildren().add(UIComponents.cell(m.getExpiryDate()!=null?m.getExpiryDate().toString():"—",90,false,m.isExpiringSoon()?Theme.WARNING:Theme.TEXT_SECONDARY));
                row.getChildren().add(UIComponents.cell(String.valueOf(m.getQuantity()),55));
                String bt=switch(m.getStatus()){case APPROVED->"success";case REJECTED->"danger";default->"warning";};
                row.getChildren().add(UIComponents.badge(m.getStatusLabel(),bt));
                HBox acts = new HBox(6); acts.setAlignment(Pos.CENTER_LEFT); acts.setMinWidth(120);
                Button viewBtn = UIComponents.smallButton("Details",false);
                viewBtn.setOnAction(e->openMedicineDetailDialog(m));
                acts.getChildren().add(viewBtn);
                if(m.getStatus()==Medicine.Status.REJECTED){
                    Button resubBtn = UIComponents.smallButton("Re-submit",true);
                    resubBtn.setOnAction(e->openSubmitMedicineDialog());
                    acts.getChildren().add(resubBtn);
                }
                row.getChildren().add(acts);
                card.getChildren().add(row); AnimationUtils.fadeIn(row,260,i*40);
            }
        }
        page.getChildren().add(card);
        AnimationUtils.fadeIn(card,350,0);
        showContent(page,"My Submissions","Track your donated medicines");
    }

    // ── SCHEDULE PICKUP ───────────────────────────────────────────────────
    private void showSchedulePickupPage() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));
        VBox card = UIComponents.glassCard(28); card.setSpacing(18); card.setMaxWidth(560);
        Label title2 = new Label("🚗 Schedule a Pickup");
        title2.setStyle("-fx-font-family:'"+Theme.FONT_HEADING+"';-fx-font-size:20px;-fx-font-weight:800;-fx-text-fill:"+Theme.TEXT_PRIMARY+";");
        Label sub = new Label("We'll send a rider to collect your medicines at your convenience.");
        sub.setStyle(Theme.labelStyle("13px",Theme.TEXT_MUTED,false));

        GridPane grid = new GridPane(); grid.setHgap(14); grid.setVgap(14);
        grid.getColumnConstraints().addAll(UIComponents.pct(50),UIComponents.pct(50));
        TextField addrF = UIComponents.styledTextField("Your full address");
        TextField dateF = UIComponents.styledTextField("YYYY-MM-DD (e.g. 2026-03-25)");
        ComboBox<String> slotF = UIComponents.styledComboBox("9:00 AM – 11:00 AM","11:00 AM – 1:00 PM","2:00 PM – 4:00 PM","4:00 PM – 6:00 PM");
        TextField itemsF = UIComponents.styledTextField("Approx. number of medicine items");
        TextArea instrF = UIComponents.styledTextArea("Special instructions (e.g. call before arrival, gate code)…"); instrF.setPrefRowCount(3);

        grid.add(UIComponents.formGroup("Pickup Address *",addrF),0,0,2,1);
        grid.add(UIComponents.formGroup("Date *",dateF),0,1);
        grid.add(UIComponents.formGroup("Time Slot *",slotF),1,1);
        grid.add(UIComponents.formGroup("Estimated Items",itemsF),0,2,2,1);
        grid.add(UIComponents.formGroup("Instructions",instrF),0,3,2,1);

        Label errLbl = new Label(""); errLbl.setStyle(Theme.labelStyle("12px",Theme.DANGER,false));
        Label successLbl = new Label(""); successLbl.setStyle(Theme.labelStyle("13px",Theme.SUCCESS,true));

        Button schedBtn = UIComponents.primaryButton("📅 Schedule Pickup →");
        schedBtn.setMaxWidth(Double.MAX_VALUE);
        schedBtn.setOnAction(e->{
            if(addrF.getText().isBlank()||dateF.getText().isBlank()){errLbl.setText("❌ Address and date are required.");return;}
            successLbl.setText("✅  Pickup scheduled for "+dateF.getText()+" at "+slotF.getValue()+".\nOur rider will contact you shortly on your registered number.");
            errLbl.setText(""); AnimationUtils.fadeIn(successLbl,300,0);
        });

        card.getChildren().addAll(title2,sub,grid,errLbl,successLbl,schedBtn);
        page.getChildren().add(card);
        AnimationUtils.fadeIn(card,350,0);
        showContent(page,"Schedule Pickup","Book a medicine collection from your home");
    }

    // ── POINTS ────────────────────────────────────────────────────────────
    private void showPointsPage() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));
        VBox card = UIComponents.glassCard(28); card.setSpacing(16); card.setMaxWidth(560);

        Label title2 = new Label("⭐ My Points & Credits");
        title2.setStyle("-fx-font-family:'"+Theme.FONT_HEADING+"';-fx-font-size:20px;-fx-font-weight:800;-fx-text-fill:"+Theme.TEXT_PRIMARY+";");

        // Big points display
        VBox pointsBox = new VBox(6); pointsBox.setAlignment(Pos.CENTER); pointsBox.setPadding(new Insets(28));
        pointsBox.setStyle("-fx-background-color:linear-gradient(to bottom right,rgba(0,176,144,0.12),rgba(0,120,96,0.08));-fx-background-radius:16;-fx-border-color:rgba(0,176,144,0.20);-fx-border-width:1;-fx-border-radius:16;");
        Label pts = new Label(String.valueOf(user.getPoints()));
        pts.setStyle("-fx-font-family:'"+Theme.FONT_HEADING+"';-fx-font-size:60px;-fx-font-weight:800;-fx-text-fill:"+Theme.TEAL_600+";");
        AnimationUtils.animateCounter(pts, user.getPoints(), "", "");
        Label ptsSub = new Label("CureShare Points"); ptsSub.setStyle(Theme.labelStyle("14px",Theme.TEXT_SECONDARY,false));
        Label ptsEq = new Label("≈ ₨ "+(user.getPoints()/2)+" cashback available"); ptsEq.setStyle(Theme.labelStyle("12px",Theme.TEXT_MUTED,false));
        pointsBox.getChildren().addAll(pts,ptsSub,ptsEq);

        // How to earn
        VBox howCard = UIComponents.glassDarkCard(16); howCard.setSpacing(8);
        Label howTitle = new Label("How to earn more points"); howTitle.setStyle(Theme.labelStyle("13px",Theme.TEXT_PRIMARY,true));
        howCard.getChildren().addAll(howTitle,
            UIComponents.metricRow("Submit a medicine","+ 10 points",Theme.TEAL_600),
            UIComponents.metricRow("Approved submission","+ 20 points",Theme.SUCCESS),
            UIComponents.metricRow("Scheduled pickup","+ 5 points",Theme.TEAL_600),
            UIComponents.metricRow("Refer a donor","+ 50 points",Theme.SUCCESS));

        // Redeem button
        Button redeemBtn = UIComponents.primaryButton("🎁 Redeem "+user.getPoints()+" Points for ₨"+user.getPoints()/2+" Cashback");
        redeemBtn.setMaxWidth(Double.MAX_VALUE);
        redeemBtn.setOnAction(e->{
            if(user.getPoints()<50){Alert a=new Alert(Alert.AlertType.INFORMATION);a.setHeaderText(null);a.setContentText("You need at least 50 points to redeem. Keep donating!");a.showAndWait();return;}
            Alert a = new Alert(Alert.AlertType.CONFIRMATION); a.setHeaderText(null);
            a.setContentText("Redeem "+user.getPoints()+" points for ₨"+user.getPoints()/2+" cashback? This will be transferred to your registered account.");
            a.showAndWait().ifPresent(b->{if(b==ButtonType.OK){int old=user.getPoints();user.setPoints(0);showToast2("✅ ₨"+(old/2)+" cashback requested! Processing in 2-3 business days.");showPointsPage();}});
        });

        card.getChildren().addAll(title2,pointsBox,howCard,redeemBtn);
        page.getChildren().add(card);
        AnimationUtils.fadeIn(card,350,0);
        showContent(page,"Points & Credits","Earn rewards for every donation");
    }

    // ── HISTORY ───────────────────────────────────────────────────────────
    private void showHistoryPage() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));
        VBox card = UIComponents.glassCard(22); card.setSpacing(12);
        card.getChildren().add(UIComponents.sectionHeader("Donation History","Your complete contribution record"));

        List<Medicine> all = db.getByDonor(user.getId());
        if (all.isEmpty()) {
            Label e = new Label("No donation history yet. Submit your first medicine!"); e.setStyle(Theme.labelStyle("13px",Theme.TEXT_MUTED,false)); e.setPadding(new Insets(20));
            card.getChildren().add(e);
        } else {
            card.getChildren().add(UIComponents.tableHeader(new String[]{"Medicine","Date","Qty","Status"},new double[]{0,100,55,100}));
            for (int i=0;i<all.size();i++){
                Medicine m=all.get(i);
                HBox row=UIComponents.tableRow(i%2==1);
                Label nm=new Label(m.getName());nm.setStyle(Theme.labelStyle("13px",Theme.TEXT_PRIMARY,true));HBox.setHgrow(nm,Priority.ALWAYS);row.getChildren().add(nm);
                row.getChildren().add(UIComponents.cell(m.getSubmittedDate().toString(),100));
                row.getChildren().add(UIComponents.cell(m.getQuantity()+" units",55));
                String bt=switch(m.getStatus()){case APPROVED->"success";case REJECTED->"danger";default->"warning";};
                row.getChildren().add(UIComponents.badge(m.getStatusLabel(),bt));
                card.getChildren().add(row); AnimationUtils.fadeIn(row,260,i*40);
            }
        }
        page.getChildren().add(card);
        showContent(page,"Donation History","Your complete contribution record");
    }

    // ── SUPPORT ───────────────────────────────────────────────────────────
    private void showSupportPage() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));
        VBox card = UIComponents.glassCard(28); card.setSpacing(16); card.setMaxWidth(560);
        Label title2 = new Label("💬 Feedback & Support");
        title2.setStyle("-fx-font-family:'"+Theme.FONT_HEADING+"';-fx-font-size:20px;-fx-font-weight:800;-fx-text-fill:"+Theme.TEXT_PRIMARY+";");

        ComboBox<String> typeF = UIComponents.styledComboBox("General Feedback","Bug Report","Medicine Question","Pickup Issue","Points / Rewards","Other");
        TextField subjectF = UIComponents.styledTextField("Brief subject line");
        TextArea msgArea = UIComponents.styledTextArea("Describe your issue or feedback in detail…"); msgArea.setPrefRowCount(6);

        Label successMsg = new Label(""); successMsg.setStyle(Theme.labelStyle("13px",Theme.SUCCESS,true));

        Button sendBtn = UIComponents.primaryButton("📨 Send Message");
        sendBtn.setMaxWidth(Double.MAX_VALUE);
        sendBtn.setOnAction(e->{
            if(msgArea.getText().isBlank()){
                successMsg.setText("❌ Please enter a message."); successMsg.setStyle(Theme.labelStyle("13px",Theme.DANGER,true)); return;
            }
            successMsg.setText("✅ Message sent! Our team will respond within 24 hours. Reference: #TKT-"+String.format("%04d",(int)(Math.random()*9999)+1));
            successMsg.setStyle(Theme.labelStyle("13px",Theme.SUCCESS,true));
            AnimationUtils.fadeIn(successMsg,300,0);
            msgArea.clear(); subjectF.clear();
        });

        // FAQ
        VBox faqCard = UIComponents.glassDarkCard(14); faqCard.setSpacing(10);
        Label faqTitle = new Label("Frequently Asked Questions"); faqTitle.setStyle(Theme.labelStyle("13px",Theme.TEXT_PRIMARY,true));
        faqCard.getChildren().add(faqTitle);
        String[][] faqs = {{"How long does verification take?","Typically 24-48 hours after submission."},{"When will my pickup be scheduled?","Within 24 hours of request, usually next business day."},{"How do I earn more points?","Submit medicines, refer donors, and complete pickups."},{"Can I cancel a submission?","Contact support before verification is complete."}};
        for (String[] f:faqs){
            VBox fItem=new VBox(3);
            Label fQ=new Label("Q: "+f[0]);fQ.setStyle(Theme.labelStyle("12.5px",Theme.TEXT_PRIMARY,true));fQ.setWrapText(true);
            Label fA=new Label("A: "+f[1]);fA.setStyle(Theme.labelStyle("12px",Theme.TEXT_MUTED,false));fA.setWrapText(true);
            fItem.getChildren().addAll(fQ,fA); faqCard.getChildren().add(fItem);
        }

        card.getChildren().addAll(title2,UIComponents.formGroup("Type",typeF),UIComponents.formGroup("Subject",subjectF),UIComponents.formGroup("Message *",msgArea),successMsg,sendBtn,faqCard);

        // Rate your last pickup experience
        VBox rateCard = UIComponents.glassCard(20); rateCard.setSpacing(12); rateCard.setMaxWidth(560);
        Label rateTitle = new Label("⭐ Rate Your Last Pickup");
        rateTitle.setStyle("-fx-font-family:'"+Theme.FONT_HEADING+"';-fx-font-size:16px;-fx-font-weight:800;-fx-text-fill:"+Theme.TEXT_PRIMARY+";");
        Label rateSub = new Label("Help us improve — rate your last collection experience.");
        rateSub.setStyle(Theme.labelStyle("12px",Theme.TEXT_MUTED,false));

        // Star buttons
        HBox starRow = new HBox(8); starRow.setAlignment(Pos.CENTER_LEFT);
        final int[] selectedStars = {0};
        Label[] starBtns = new Label[5];
        for (int si=0;si<5;si++) {
            final int starVal = si+1;
            final int idx = si;
            Label star = new Label("☆"); star.setStyle("-fx-font-size:28px;-fx-cursor:hand;-fx-text-fill:"+Theme.TEXT_MUTED+";");
            starBtns[si] = star;
            star.setOnMouseClicked(e2 -> {
                selectedStars[0] = starVal;
                for (int j=0;j<5;j++) {
                    starBtns[j].setText(j<starVal?"★":"☆");
                    starBtns[j].setStyle("-fx-font-size:28px;-fx-cursor:hand;-fx-text-fill:"+(j<starVal?Theme.WARNING:Theme.TEXT_MUTED)+";");
                }
            });
            star.setOnMouseEntered(e2 -> {
                for (int j=0;j<5;j++) {
                    starBtns[j].setText(j<=idx?"★":"☆");
                    starBtns[j].setStyle("-fx-font-size:28px;-fx-cursor:hand;-fx-text-fill:"+(j<=idx?Theme.WARNING:Theme.TEXT_MUTED)+";");
                }
            });
            star.setOnMouseExited(e2 -> {
                for (int j=0;j<5;j++) {
                    starBtns[j].setText(j<selectedStars[0]?"★":"☆");
                    starBtns[j].setStyle("-fx-font-size:28px;-fx-cursor:hand;-fx-text-fill:"+(j<selectedStars[0]?Theme.WARNING:Theme.TEXT_MUTED)+";");
                }
            });
            starRow.getChildren().add(star);
        }
        ComboBox<String> aspectF = UIComponents.styledComboBox("Overall Experience","Rider Punctuality","Medicine Handling","Communication","Pickup Speed");
        TextField rateCommentF = UIComponents.styledTextField("Optional comment about your pickup…");
        Label rateMsg = new Label(""); rateMsg.setStyle(Theme.labelStyle("12px",Theme.SUCCESS,true));
        Button submitRateBtn = UIComponents.primaryButton("Submit Rating →");
        submitRateBtn.setMaxWidth(Double.MAX_VALUE);
        submitRateBtn.setOnAction(e -> {
            if (selectedStars[0]==0) { rateMsg.setText("❌ Please select a star rating."); rateMsg.setStyle(Theme.labelStyle("12px",Theme.DANGER,true)); return; }
            db.addRating(new DataStore.Rating(user.getId(),user.getName(),"CURESHARE","CureShare Service",aspectF.getValue(),selectedStars[0],rateCommentF.getText().trim()));
            rateMsg.setText("✅ Thank you! Your "+selectedStars[0]+"-star rating has been submitted.");
            rateMsg.setStyle(Theme.labelStyle("12px",Theme.SUCCESS,true));
            AnimationUtils.fadeIn(rateMsg,300,0);
            for (Label s:starBtns){s.setText("☆");s.setStyle("-fx-font-size:28px;-fx-cursor:hand;-fx-text-fill:"+Theme.TEXT_MUTED+";");}
            selectedStars[0]=0; rateCommentF.clear();
        });
        rateCard.getChildren().addAll(rateTitle,rateSub,
            UIComponents.formGroup("Stars",starRow),
            UIComponents.formGroup("Rate aspect",aspectF),
            UIComponents.formGroup("Comment",rateCommentF),
            rateMsg,submitRateBtn);

        page.getChildren().addAll(card,rateCard);
        AnimationUtils.fadeIn(card,350,0);
        showContent(page,"Feedback & Support","We're here to help");
    }

    // ── SETTINGS ──────────────────────────────────────────────────────────
    private void showSettingsPage() {
        VBox page = new VBox(20); page.setPadding(new Insets(28));
        VBox card = UIComponents.glassCard(28); card.setSpacing(18); card.setMaxWidth(560);
        Label title2 = new Label("⚙️ Account Settings");
        title2.setStyle("-fx-font-family:'"+Theme.FONT_HEADING+"';-fx-font-size:20px;-fx-font-weight:800;-fx-text-fill:"+Theme.TEXT_PRIMARY+";");

        GridPane grid = new GridPane(); grid.setHgap(14); grid.setVgap(14);
        grid.getColumnConstraints().addAll(UIComponents.pct(50),UIComponents.pct(50));
        TextField nameF  = UIComponents.styledTextField("Full Name"); nameF.setText(user.getName());
        TextField emailF = UIComponents.styledTextField("Email"); emailF.setText(user.getEmail());
        TextField phoneF = UIComponents.styledTextField("Phone"); if(user.getPhone()!=null) phoneF.setText(user.getPhone());
        PasswordField passF  = UIComponents.styledPasswordField("New Password (leave blank to keep)");
        PasswordField pass2F = UIComponents.styledPasswordField("Confirm New Password");
        grid.add(UIComponents.formGroup("Full Name",nameF),0,0);
        grid.add(UIComponents.formGroup("Email",emailF),1,0);
        grid.add(UIComponents.formGroup("Phone",phoneF),0,1,2,1);
        grid.add(UIComponents.formGroup("New Password",passF),0,2);
        grid.add(UIComponents.formGroup("Confirm Password",pass2F),1,2);

        VBox prefSection = new VBox(8);
        prefSection.getChildren().addAll(
            UIComponents.toggle(true,"Email me when my submission is approved"),
            UIComponents.toggle(true,"SMS notification when pickup is scheduled"),
            UIComponents.toggle(false,"Weekly donation summary email"));

        Label saveMsg = new Label(""); saveMsg.setStyle(Theme.labelStyle("12px",Theme.SUCCESS,true));
        Button saveBtn = UIComponents.primaryButton("💾 Save Changes");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e->{
            if(!passF.getText().isBlank()&&!passF.getText().equals(pass2F.getText())){saveMsg.setText("❌ Passwords do not match.");saveMsg.setStyle(Theme.labelStyle("12px",Theme.DANGER,true));return;}
            user.setName(nameF.getText().trim());user.setEmail(emailF.getText().trim());user.setPhone(phoneF.getText().trim());
            if(!passF.getText().isBlank()) db.updatePassword(user.getId(), passF.getText());
            saveMsg.setText("✅ Settings saved!");saveMsg.setStyle(Theme.labelStyle("12px",Theme.SUCCESS,true));AnimationUtils.fadeIn(saveMsg,300,0);
        });

        card.getChildren().addAll(title2,grid,UIComponents.sectionHeader("Notifications",null),prefSection,saveMsg,saveBtn);
        page.getChildren().add(card);
        AnimationUtils.fadeIn(card,350,0);
        showContent(page,"Account Settings","Manage your account");
    }

    // ── DETAIL DIALOG ─────────────────────────────────────────────────────
    private void openMedicineDetailDialog(Medicine m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Medicine Details"); a.setHeaderText(m.getName());
        a.setContentText("Category: "+m.getCategory()+"\nBatch: "+m.getBatchNumber()+"\nExpiry: "+m.getExpiryDate()+"\nQuantity: "+m.getQuantity()+" units\nStatus: "+m.getStatusLabel()+"\nSubmitted: "+m.getSubmittedDate());
        a.showAndWait();
    }

    private void showToast2(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
