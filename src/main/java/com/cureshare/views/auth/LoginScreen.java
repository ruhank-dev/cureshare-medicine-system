package com.cureshare.views.auth;

import com.cureshare.models.User;
import com.cureshare.utils.*;
import com.cureshare.views.dashboard.*;
import com.cureshare.views.shared.UIComponents;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginScreen {

    public void show(Stage stage) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color:" + Theme.BG_PAGE + ";");
        root.getChildren().add(buildBg());

        HBox content = new HBox(0);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(1100);

        VBox left = buildBrandPanel();
        HBox.setHgrow(left, Priority.ALWAYS);

        VBox right = buildLoginForm(stage);
        right.setMinWidth(440); right.setMaxWidth(440);

        // Wrap right in scroll so demo box always visible on small screens
        ScrollPane rightScroll = new ScrollPane(right);
        rightScroll.setFitToWidth(true);
        rightScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        rightScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rightScroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;-fx-border-width:0;");
        rightScroll.setMinWidth(440); rightScroll.setMaxWidth(440);

        content.getChildren().addAll(left, rightScroll);
        root.getChildren().add(content);

        Scene scene = new Scene(root, 1366, 768);
        String css = getClass().getResource("/styles/app.css") != null
            ? getClass().getResource("/styles/app.css").toExternalForm() : "";
        if (!css.isEmpty()) scene.getStylesheets().add(css);
        stage.setScene(scene);
        stage.show();

        AnimationUtils.staggerFadeIn(left, right);
    }

    // ── Brand left panel ─────────────────────────────────────────────────
    private VBox buildBrandPanel() {
        VBox panel = new VBox(28);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(60, 60, 60, 80));

        // Logo row
        HBox logoRow = new HBox(14);
        logoRow.setAlignment(Pos.CENTER_LEFT);
        StackPane logoIcon = buildLogoIcon();
        VBox logoText = new VBox(2);
        Label name = new Label("CureShare");
        name.setStyle("-fx-font-family:'" + Theme.FONT_HEADING + "';" +
                "-fx-font-size:32px;-fx-font-weight:800;-fx-text-fill:" + Theme.TEXT_PRIMARY + ";");
        Label tagline = new Label("Smart Medicine Redistribution");
        tagline.setStyle(Theme.labelStyle("13px", Theme.TEXT_MUTED, false));
        logoText.getChildren().addAll(name, tagline);
        logoRow.getChildren().addAll(logoIcon, logoText);

        // Headline
        Label headline = new Label("Bridging\nMedicine\nWaste & Need.");
        headline.setStyle("-fx-font-family:'" + Theme.FONT_HEADING + "';" +
                "-fx-font-size:52px;-fx-font-weight:800;" +
                "-fx-text-fill:" + Theme.TEXT_PRIMARY + ";-fx-line-spacing:3;");

        Label sub = new Label("Collect, verify and redistribute unused medicines\nto communities that need them most.");
        sub.setStyle(Theme.labelStyle("15px", Theme.TEXT_SECONDARY, false));

        // Stats
        HBox stats = new HBox(24);
        stats.setAlignment(Pos.CENTER_LEFT);
        stats.setPadding(new Insets(14, 0, 0, 0));
        stats.getChildren().addAll(
            miniStat("4,821", "Medicines\nCollected"),
            statDiv(),
            miniStat("28",    "Charity\nPartners"),
            statDiv(),
            miniStat("₨186K", "Revenue\nGenerated")
        );
        panel.getChildren().addAll(logoRow, headline, sub, stats);
        return panel;
    }

    private StackPane buildLogoIcon() {
        StackPane sp = new StackPane();
        Rectangle bg = new Rectangle(52, 52);
        bg.setArcWidth(16); bg.setArcHeight(16);
        bg.setFill(new LinearGradient(0,0,1,1,true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#00e0b8")), new Stop(1, Color.web("#008a6a"))));
        bg.setEffect(new DropShadow(6, 0, 2, Color.web("#00e0b8", 0.35)));
        Label ico = new Label("💊"); ico.setStyle("-fx-font-size:24px;");
        sp.getChildren().addAll(bg, ico);
        AnimationUtils.pulse(sp);
        return sp;
    }

    private VBox miniStat(String val, String lbl) {
        VBox v = new VBox(3);
        Label vl = new Label(val);
        vl.setStyle("-fx-font-family:'" + Theme.FONT_HEADING + "';" +
                "-fx-font-size:22px;-fx-font-weight:800;-fx-text-fill:" + Theme.TEAL_600 + ";");
        Label ll = new Label(lbl);
        ll.setStyle(Theme.labelStyle("11px", Theme.TEXT_MUTED, false));
        v.getChildren().addAll(vl, ll);
        return v;
    }

    private Region statDiv() {
        Region r = new Region();
        r.setMinWidth(1); r.setMaxWidth(1);
        r.setMinHeight(32); r.setMaxHeight(32);
        r.setStyle("-fx-background-color:rgba(0,176,144,0.18);");
        return r;
    }

    // ── Login form ────────────────────────────────────────────────────────
    private VBox buildLoginForm(Stage stage) {
        VBox wrapper = new VBox(0);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPadding(new Insets(40, 20, 40, 20));

        VBox card = new VBox(20);
        card.setStyle("-fx-background-color:rgba(255,255,255,0.78);" +
                "-fx-background-radius:28;" +
                "-fx-border-color:rgba(255,255,255,0.90);" +
                "-fx-border-width:1.5;-fx-border-radius:28;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,120,96,0.12),10,0,0,4);" +
                "-fx-padding:38;");
        card.setMaxWidth(380);

        // Header
        Label welcome = new Label("Welcome back 👋");
        welcome.setStyle("-fx-font-family:'" + Theme.FONT_HEADING + "';" +
                "-fx-font-size:25px;-fx-font-weight:800;-fx-text-fill:" + Theme.TEXT_PRIMARY + ";");
        Label sub = new Label("Sign in to CureShare BMS");
        sub.setStyle(Theme.labelStyle("13px", Theme.TEXT_MUTED, false));
        VBox header = new VBox(5);
        header.getChildren().addAll(welcome, sub);

        // Role selector — label + row
        Label roleLabel = UIComponents.formLabel("Sign in as");
        HBox roleRow = buildRoleRow();
        VBox roleGroup = new VBox(6);
        roleGroup.getChildren().addAll(roleLabel, roleRow);

        // Fields
        TextField emailField = UIComponents.styledTextField("your@email.com");
        emailField.setText("admin@cureshare.pk");
        VBox emailGroup = UIComponents.formGroup("Email Address", emailField);

        PasswordField passField = UIComponents.styledPasswordField("Password");
        passField.setText("admin123");
        VBox passGroup = UIComponents.formGroup("Password", passField);

        // Forgot
        HBox forgotRow = new HBox();
        forgotRow.setAlignment(Pos.CENTER_RIGHT);
        Label forgot = new Label("Forgot password?");
        forgot.setStyle(Theme.labelStyle("12px", Theme.TEAL_600, false) + "-fx-cursor:hand;");
        forgotRow.getChildren().add(forgot);

        // Error
        Label errorLbl = new Label("");
        errorLbl.setStyle("-fx-text-fill:" + Theme.DANGER + ";" +
                          "-fx-font-size:12px;-fx-font-family:'" + Theme.FONT_BODY + "';");
        errorLbl.setVisible(false);
        errorLbl.setManaged(false);

        // Sign in button
        Button signInBtn = UIComponents.primaryButton("Sign In  →");
        signInBtn.setMaxWidth(Double.MAX_VALUE);
        signInBtn.setStyle(signInBtn.getStyle() +
                "-fx-font-size:14px;-fx-padding:13 22 13 22;");

        signInBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            String pass  = passField.getText();
            if (email.isBlank() || pass.isBlank()) {
                errorLbl.setText("❌  Please enter email and password.");
                errorLbl.setVisible(true); errorLbl.setManaged(true); return;
            }
            // Disable button and show loading state while BCrypt runs in background
            signInBtn.setText("Signing in…");
            signInBtn.setDisable(true);
            errorLbl.setVisible(false); errorLbl.setManaged(false);

            // Run BCrypt on a background thread so UI never freezes
            Thread authThread = new Thread(() -> {
                User u = DataStore.getInstance().authenticate(email, pass);
                javafx.application.Platform.runLater(() -> {
                    signInBtn.setText("Sign In  →");
                    signInBtn.setDisable(false);
                    if (u != null) {
                        SessionManager.getInstance().login(u);
                        routeToDashboard(u, stage);
                    } else {
                        errorLbl.setText("❌  Invalid email or password.");
                        errorLbl.setVisible(true); errorLbl.setManaged(true);
                        AnimationUtils.shake(card);
                    }
                });
            });
            authThread.setDaemon(true);
            authThread.start();
        });
        passField.setOnAction(e  -> signInBtn.fire());
        emailField.setOnAction(e -> passField.requestFocus());

        // Divider
        HBox divRow = new HBox(10);
        divRow.setAlignment(Pos.CENTER);
        Region d1 = new Region(); HBox.setHgrow(d1, Priority.ALWAYS);
        d1.setStyle("-fx-background-color:rgba(0,176,144,0.14);-fx-max-height:1;");
        Label or = new Label("or");
        or.setStyle(Theme.labelStyle("12px", Theme.TEXT_MUTED, false));
        Region d2 = new Region(); HBox.setHgrow(d2, Priority.ALWAYS);
        d2.setStyle("-fx-background-color:rgba(0,176,144,0.14);-fx-max-height:1;");
        divRow.getChildren().addAll(d1, or, d2);

        // Sign up link
        HBox signUpRow = new HBox(6);
        signUpRow.setAlignment(Pos.CENTER);
        Label noAcc = new Label("Don't have an account?");
        noAcc.setStyle(Theme.labelStyle("13px", Theme.TEXT_MUTED, false));
        Label signUpLink = new Label("Create Account");
        signUpLink.setStyle(Theme.labelStyle("13px", Theme.TEAL_600, true) + "-fx-cursor:hand;");
        signUpLink.setOnMouseClicked(e -> new SignupScreen().show(stage));
        signUpRow.getChildren().addAll(noAcc, signUpLink);

        // Demo hints — compact 2-column grid style
        VBox demoBox = new VBox(6);
        demoBox.setStyle("-fx-background-color:rgba(0,176,144,0.07);" +
                "-fx-background-radius:10;-fx-border-color:rgba(0,176,144,0.15);" +
                "-fx-border-width:1;-fx-border-radius:10;-fx-padding:10 12 10 12;");
        Label demoTitle = UIComponents.formLabel("🔑 Quick Login Credentials");
        GridPane credGrid = new GridPane(); credGrid.setHgap(8); credGrid.setVgap(4);
        String[][] creds = {
            {"Admin:",     "admin@cureshare.pk",  "admin123"},
            {"Household:", "ahmad@gmail.com",      "pass123"},
            {"Pharmacy:",  "medplus@pharmacy.pk",  "pharm123"},
            {"Charity:",   "hope@ngo.pk",           "hope123"},
        };
        String roleStyle  = Theme.labelStyle("10.5px", Theme.TEXT_SECONDARY, true);
        String emailStyle = Theme.labelStyle("10.5px", Theme.TEXT_MUTED, false);
        String passStyle  = Theme.labelStyle("10.5px", Theme.TEAL_600, false);
        for (int ci = 0; ci < creds.length; ci++) {
            Label roleLbl  = new Label(creds[ci][0]); roleLbl.setStyle(roleStyle);
            Label emailLbl = new Label(creds[ci][1]); emailLbl.setStyle(emailStyle+"-fx-cursor:hand;");
            Label passLbl  = new Label(creds[ci][2]); passLbl.setStyle(passStyle+"-fx-cursor:hand;");
            credGrid.add(roleLbl,  0, ci); credGrid.add(emailLbl, 1, ci); credGrid.add(passLbl, 2, ci);
            final String em = creds[ci][1], pw = creds[ci][2];
            emailLbl.setOnMouseClicked(e -> { emailField.setText(em); passField.setText(pw); });
            passLbl.setOnMouseClicked(e  -> { emailField.setText(em); passField.setText(pw); });
        }
        Label clickHint = new Label("↑ Click any email or password to auto-fill");
        clickHint.setStyle(Theme.labelStyle("9.5px", Theme.TEXT_MUTED, false));
        demoBox.getChildren().addAll(demoTitle, credGrid, clickHint);

        card.getChildren().addAll(header, roleGroup, emailGroup, passGroup,
                forgotRow, errorLbl, signInBtn, divRow, signUpRow, demoBox);
        wrapper.getChildren().add(card);
        return wrapper;
    }

    private HBox buildRoleRow() {
        String[] roles = {"Admin", "Household", "Pharmacy", "Charity"};
        HBox row = new HBox(6);

        String inactive = "-fx-background-color:rgba(255,255,255,0.65);" +
                "-fx-border-color:rgba(0,176,144,0.22);" +
                "-fx-border-width:1;-fx-border-radius:8;-fx-background-radius:8;" +
                "-fx-padding:5 10 5 10;-fx-font-size:11.5px;" +
                "-fx-font-family:'" + Theme.FONT_BODY + "';" +
                "-fx-cursor:hand;-fx-text-fill:" + Theme.TEXT_SECONDARY + ";";
        String active = "-fx-background-color:linear-gradient(to right,#00b090,#007056);" +
                "-fx-background-radius:8;-fx-border-radius:8;" +
                "-fx-padding:5 10 5 10;-fx-font-size:11.5px;" +
                "-fx-font-family:'" + Theme.FONT_BODY + "';" +
                "-fx-cursor:hand;-fx-text-fill:white;-fx-font-weight:bold;";

        for (String role : roles) {
            Label btn = new Label(role);
            btn.setStyle(inactive);
            btn.setOnMouseClicked(e -> {
                row.getChildren().forEach(n -> { if (n instanceof Label l) l.setStyle(inactive); });
                btn.setStyle(active);
            });
            row.getChildren().add(btn);
        }

        // Activate first item directly — no fire(), just set style
        if (!row.getChildren().isEmpty() && row.getChildren().get(0) instanceof Label first) {
            first.setStyle(active);
        }
        return row;
    }

    private Pane buildBg() {
        Pane p = new Pane();
        p.setMouseTransparent(true);
        double[][] data = {
            {-60,-60,400,0.15},{750,550,500,0.11},
            {350,40,260,0.14},{1000,80,380,0.09}
        };
        for (double[] d : data) {
            Circle c = new Circle(d[2]);
            c.setTranslateX(d[0]); c.setTranslateY(d[1]);
            c.setFill(new RadialGradient(0,0,.5,.5,1,true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(Theme.TEAL_200, d[3])),
                new Stop(1, Color.TRANSPARENT)));
            TranslateTransition tt = new TranslateTransition(
                Duration.millis(5000 + Math.random()*3000), c);
            tt.setByY(-16 - Math.random()*12);
            tt.setAutoReverse(true); tt.setCycleCount(Animation.INDEFINITE);
            tt.setInterpolator(Interpolator.EASE_BOTH);
            tt.setDelay(Duration.millis(Math.random()*1500));
            tt.play();
            p.getChildren().add(c);
        }
        return p;
    }

    private void routeToDashboard(User u, Stage stage) {
        switch (u.getRole()) {
            case "admin"     -> new AdminDashboard().show(stage, u);
            case "household" -> new HouseholdDashboard().show(stage, u);
            case "pharmacy"  -> new PharmacyDashboard().show(stage, u);
            case "charity"   -> new CharityDashboard().show(stage, u);
            default          -> new AdminDashboard().show(stage, u);
        }
    }
}
