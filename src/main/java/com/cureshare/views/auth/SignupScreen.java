package com.cureshare.views.auth;

import com.cureshare.models.User;
import com.cureshare.utils.*;
import com.cureshare.views.shared.UIComponents;
import javafx.animation.PauseTransition;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SignupScreen {

    private String selectedRole = "household";

    public void show(Stage stage) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color:" + Theme.BG_PAGE + ";");

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;-fx-border-width:0;");

        VBox center = new VBox();
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(40, 20, 40, 20));

        VBox card = new VBox(18);
        card.setMaxWidth(540);
        card.setStyle("-fx-background-color:rgba(255,255,255,0.80);" +
                "-fx-background-radius:28;-fx-border-color:rgba(255,255,255,0.90);" +
                "-fx-border-width:1.5;-fx-border-radius:28;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,120,96,0.12),10,0,0,4);" +
                "-fx-padding:38;");

        // Back + title
        Label back = new Label("← Back to Login");
        back.setStyle(Theme.labelStyle("13px", Theme.TEAL_600, false) + "-fx-cursor:hand;");
        back.setOnMouseClicked(e -> new LoginScreen().show(stage));

        Label title = new Label("Create Account");
        title.setStyle("-fx-font-family:'" + Theme.FONT_HEADING + "';" +
                "-fx-font-size:24px;-fx-font-weight:800;-fx-text-fill:" + Theme.TEXT_PRIMARY + ";");
        Label sub = new Label("Join CureShare — select your role to get started");
        sub.setStyle(Theme.labelStyle("13px", Theme.TEXT_MUTED, false));

        // Role selector
        Label roleLabel = UIComponents.formLabel("I am a...");
        HBox roleGrid = buildRoleGrid();

        // Form
        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(14);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(50);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(c1, c2);

        TextField nameF  = UIComponents.styledTextField("Full Name");
        TextField emailF = UIComponents.styledTextField("Email Address");
        PasswordField passF  = UIComponents.styledPasswordField("Password (min 6 chars)");
        PasswordField pass2F = UIComponents.styledPasswordField("Confirm Password");
        TextField phoneF = UIComponents.styledTextField("Phone Number");
        TextField orgF   = UIComponents.styledTextField("Organization (optional)");

        grid.add(UIComponents.formGroup("Full Name *",     nameF),  0, 0, 2, 1);
        grid.add(UIComponents.formGroup("Email *",         emailF), 0, 1);
        grid.add(UIComponents.formGroup("Phone",           phoneF), 1, 1);
        grid.add(UIComponents.formGroup("Password *",      passF),  0, 2);
        grid.add(UIComponents.formGroup("Confirm Password *", pass2F), 1, 2);
        grid.add(UIComponents.formGroup("Organization",    orgF),   0, 3, 2, 1);

        CheckBox terms = new CheckBox("I agree to the Terms of Service and Privacy Policy");
        terms.setStyle("-fx-font-family:'" + Theme.FONT_BODY + "';" +
                "-fx-font-size:12px;-fx-text-fill:" + Theme.TEXT_SECONDARY + ";");

        Label errLbl = new Label("");
        errLbl.setStyle("-fx-text-fill:" + Theme.DANGER + ";-fx-font-size:12px;");
        errLbl.setVisible(false); errLbl.setManaged(false);

        Label successLbl = new Label("✅  Account created! Redirecting to login…");
        successLbl.setStyle("-fx-text-fill:" + Theme.SUCCESS + ";-fx-font-size:13px;-fx-font-weight:bold;");
        successLbl.setVisible(false); successLbl.setManaged(false);

        Button registerBtn = UIComponents.primaryButton("Create Account  →");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setStyle(registerBtn.getStyle() + "-fx-font-size:14px;-fx-padding:13 22 13 22;");

        registerBtn.setOnAction(e -> {
            errLbl.setVisible(false); errLbl.setManaged(false);
            String nm   = nameF.getText().trim();
            String em   = emailF.getText().trim();
            String pw   = passF.getText();
            String pw2  = pass2F.getText();
            String ph   = phoneF.getText().trim();
            String org  = orgF.getText().trim();

            if (nm.isBlank() || em.isBlank() || pw.isBlank()) {
                errLbl.setText("❌  Please fill in all required fields.");
                errLbl.setVisible(true); errLbl.setManaged(true); return;
            }
            if (!pw.equals(pw2)) {
                errLbl.setText("❌  Passwords do not match.");
                errLbl.setVisible(true); errLbl.setManaged(true); return;
            }
            if (pw.length() < 6) {
                errLbl.setText("❌  Password must be at least 6 characters.");
                errLbl.setVisible(true); errLbl.setManaged(true); return;
            }
            if (DataStore.getInstance().emailExists(em)) {
                errLbl.setText("❌  Email already registered.");
                errLbl.setVisible(true); errLbl.setManaged(true); return;
            }
            if (!terms.isSelected()) {
                errLbl.setText("❌  Please accept the Terms of Service.");
                errLbl.setVisible(true); errLbl.setManaged(true); return;
            }

            User user = new User();
            user.setName(nm); user.setEmail(em); user.setPassword(pw);
            user.setRole(selectedRole); user.setPhone(ph);
            if (!org.isBlank()) user.setOrganization(org);
            DataStore.getInstance().registerUser(user);

            successLbl.setVisible(true); successLbl.setManaged(true);
            AnimationUtils.fadeIn(successLbl, 300, 0);

            PauseTransition pt = new PauseTransition(Duration.millis(1800));
            pt.setOnFinished(ev -> new LoginScreen().show(stage));
            pt.play();
        });

        // Sign in link
        HBox signInRow = new HBox(6);
        signInRow.setAlignment(Pos.CENTER);
        Label already = new Label("Already have an account?");
        already.setStyle(Theme.labelStyle("13px", Theme.TEXT_MUTED, false));
        Label signInLink = new Label("Sign In");
        signInLink.setStyle(Theme.labelStyle("13px", Theme.TEAL_600, true) + "-fx-cursor:hand;");
        signInLink.setOnMouseClicked(e -> new LoginScreen().show(stage));
        signInRow.getChildren().addAll(already, signInLink);

        card.getChildren().addAll(back, title, sub, roleLabel, roleGrid,
                grid, terms, errLbl, successLbl, registerBtn, signInRow);

        center.getChildren().add(card);
        scroll.setContent(center);
        root.getChildren().add(scroll);

        stage.setScene(new Scene(root, 1366, 768));
        stage.show();
        AnimationUtils.scaleIn(card);
    }

    private HBox buildRoleGrid() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        String[][] roles = {
            {"admin",     "🔧", "Admin"},
            {"household", "🏠", "Household"},
            {"pharmacy",  "💊", "Pharmacy"},
            {"charity",   "❤️", "Charity"},
        };

        String inactiveCard = "-fx-background-color:rgba(255,255,255,0.65);" +
                "-fx-background-radius:12;-fx-border-color:rgba(0,176,144,0.22);" +
                "-fx-border-width:1;-fx-border-radius:12;-fx-cursor:hand;";
        String activeCard = "-fx-background-color:rgba(0,176,144,0.12);" +
                "-fx-background-radius:12;-fx-border-color:" + Theme.TEAL_500 + ";" +
                "-fx-border-width:2;-fx-border-radius:12;-fx-cursor:hand;";

        for (String[] r : roles) {
            VBox card = new VBox(6);
            card.setAlignment(Pos.CENTER);
            card.setPadding(new Insets(12, 14, 12, 14));
            card.setStyle(inactiveCard);

            Label icon = new Label(r[1]); icon.setStyle("-fx-font-size:20px;");
            Label name = new Label(r[2]);
            name.setStyle(Theme.labelStyle("11px", Theme.TEXT_SECONDARY, false));
            card.getChildren().addAll(icon, name);

            final String roleId = r[0];
            card.setOnMouseClicked(e -> {
                row.getChildren().forEach(n -> {
                    if (n instanceof VBox v) {
                        v.setStyle(inactiveCard);
                        v.getChildren().stream()
                            .filter(ch -> ch instanceof Label l && l.getText().length() > 2)
                            .forEach(ch -> ((Label) ch).setStyle(
                                Theme.labelStyle("11px", Theme.TEXT_SECONDARY, false)));
                    }
                });
                card.setStyle(activeCard);
                name.setStyle(Theme.labelStyle("11px", Theme.TEAL_700, true));
                selectedRole = roleId;
            });
            row.getChildren().add(card);
        }

        // Activate "household" by default (index 1) — direct style, no fire()
        if (row.getChildren().size() > 1 && row.getChildren().get(1) instanceof VBox hhCard) {
            hhCard.setStyle("-fx-background-color:rgba(0,176,144,0.12);" +
                    "-fx-background-radius:12;-fx-border-color:" + Theme.TEAL_500 + ";" +
                    "-fx-border-width:2;-fx-border-radius:12;-fx-cursor:hand;");
            hhCard.getChildren().stream()
                .filter(ch -> ch instanceof Label l && l.getText().length() > 2)
                .forEach(ch -> ((Label) ch).setStyle(
                    Theme.labelStyle("11px", Theme.TEAL_700, true)));
        }
        return row;
    }
}
