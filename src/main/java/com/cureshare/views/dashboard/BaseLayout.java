package com.cureshare.views.dashboard;

import com.cureshare.models.User;
import com.cureshare.utils.*;
import com.cureshare.views.auth.LoginScreen;
import com.cureshare.views.shared.UIComponents;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

public class BaseLayout {

    protected Stage stage;
    protected User  user;
    protected BorderPane root;
    protected StackPane  contentArea;
    protected Label      pageTitleLabel;
    protected Label      pageSubLabel;

    private final List<HBox> navItems = new ArrayList<>();
    private HBox activeNavItem;
    private Node currentPage;
    protected HBox firstNavItem;

    public void initialize(Stage stage, User user) {
        this.stage = stage;
        this.user  = user;
        root = new BorderPane();
        root.setStyle("-fx-background-color:" + Theme.BG_PAGE + ";");
        root.setLeft(buildSidebar());
        root.setTop(buildHeader());

        // ── Main content scroll ──
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color:transparent;");
        contentArea.setAlignment(Pos.TOP_LEFT);

        ScrollPane mainScroll = new ScrollPane(contentArea);
        mainScroll.setFitToWidth(true);
        mainScroll.setFitToHeight(false);
        mainScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;-fx-border-width:0;");
        root.setCenter(mainScroll);
    }

    protected void buildScene() {
        Scene scene = new Scene(root, 1366, 768);
        String css = getClass().getResource("/styles/app.css") != null
            ? getClass().getResource("/styles/app.css").toExternalForm() : "";
        if (!css.isEmpty()) scene.getStylesheets().add(css);
        stage.setScene(scene);
        stage.show();
        AnimationUtils.slideInLeft(root.getLeft());
        AnimationUtils.fadeIn(root.getTop(), 400, 100);
    }

    // ── SIDEBAR (fully scrollable) ────────────────────────────────────────
    private VBox buildSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setMinWidth(Theme.SIDEBAR_WIDTH);
        sidebar.setMaxWidth(Theme.SIDEBAR_WIDTH);
        sidebar.setPrefWidth(Theme.SIDEBAR_WIDTH);
        sidebar.setStyle("-fx-background-color:" + Theme.SIDEBAR_BG + ";");

        // Logo — fixed at top
        HBox logo = buildLogoRow();
        sidebar.getChildren().add(logo);

        // Nav — scrollable middle section
        VBox nav = new VBox(0);
        nav.setPadding(new Insets(8, 0, 8, 0));
        buildNavItems(nav);

        ScrollPane navScroll = new ScrollPane(nav);
        navScroll.setFitToWidth(true);
        navScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        navScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        navScroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;-fx-border-width:0;");
        VBox.setVgrow(navScroll, Priority.ALWAYS);

        // Footer — fixed at bottom
        HBox footer = buildSidebarFooter();

        sidebar.getChildren().addAll(navScroll, footer);

        // Shadow
        sidebar.setEffect(new DropShadow(6, 2, 0, Color.web("rgba(0,40,30,0.18)")));
        return sidebar;
    }

    private HBox buildLogoRow() {
        HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(22, 20, 18, 20));
        row.setMinWidth(Theme.SIDEBAR_WIDTH); row.setMaxWidth(Theme.SIDEBAR_WIDTH);
        row.setStyle("-fx-background-color:" + Theme.SIDEBAR_BG + ";-fx-border-color:transparent transparent rgba(255,255,255,0.10) transparent;-fx-border-width:0 0 1 0;");

        StackPane logoBox = new StackPane();
        Rectangle bg = new Rectangle(42,42); bg.setArcWidth(14); bg.setArcHeight(14);
        bg.setFill(new LinearGradient(0,0,1,1,true,CycleMethod.NO_CYCLE,
            new Stop(0,Color.web("#00e0b8")), new Stop(1,Color.web("#008a6a"))));
        bg.setEffect(new DropShadow(5,0,2,Color.web("#00e0b8",0.40)));
        Label ico = new Label("💊"); ico.setStyle("-fx-font-size:20px;");
        logoBox.getChildren().addAll(bg, ico);
        AnimationUtils.pulse(logoBox);

        VBox text = new VBox(2);
        Label name = new Label("CureShare"); name.setStyle(Theme.sidebarHeadingStyle());
        Label sub = new Label("BMS PLATFORM");
        sub.setStyle("-fx-font-size:9px;-fx-text-fill:rgba(255,255,255,0.38);-fx-letter-spacing:1.4px;-fx-font-family:'" + Theme.FONT_BODY + "';");
        text.getChildren().addAll(name, sub);
        row.getChildren().addAll(logoBox, text);
        return row;
    }

    protected void buildNavItems(VBox nav) { /* overridden */ }

    protected void addNavSection(VBox nav, String title) {
        Label lbl = new Label(title.toUpperCase());
        lbl.setStyle("-fx-font-size:9.5px;-fx-text-fill:rgba(255,255,255,0.30);-fx-letter-spacing:1.5px;-fx-font-family:'" + Theme.FONT_BODY + "';-fx-padding:16 20 6 20;");
        nav.getChildren().add(lbl);
    }

    protected HBox addNavItem(VBox nav, String icon, String label, String badgeText, Runnable action) {
        HBox item = new HBox(12); item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10, 18, 10, 18)); item.setStyle("-fx-cursor:hand;");

        Region bar = new Region(); bar.setMinWidth(3); bar.setMaxWidth(3); bar.setPrefHeight(0);
        bar.setStyle("-fx-background-color:#00e0b8;-fx-background-radius:3;");

        StackPane iconBox = new StackPane(); iconBox.setMinSize(30,30); iconBox.setMaxSize(30,30);
        iconBox.setStyle("-fx-background-color:rgba(255,255,255,0.06);-fx-background-radius:8;");
        Label iLbl = new Label(icon); iLbl.setStyle("-fx-font-size:14px;");
        iconBox.getChildren().add(iLbl);

        Label txt = new Label(label);
        txt.setStyle("-fx-font-family:'" + Theme.FONT_BODY + "';-fx-font-size:13px;-fx-text-fill:rgba(255,255,255,0.65);-fx-font-weight:500;");
        HBox.setHgrow(txt, Priority.ALWAYS);
        item.getChildren().addAll(bar, iconBox, txt);

        if (badgeText != null && !badgeText.isBlank()) {
            Label badge = new Label(badgeText);
            badge.setStyle("-fx-background-color:#00e0b8;-fx-text-fill:#003328;-fx-font-size:10px;-fx-font-weight:bold;-fx-background-radius:20;-fx-padding:2 7 2 7;");
            item.getChildren().add(badge);
        }

        String inactiveStyle = "-fx-cursor:hand;-fx-padding:10 18 10 18;";
        String hoverStyle = "-fx-background-color:rgba(255,255,255,0.07);-fx-cursor:hand;-fx-padding:10 18 10 18;";
        String activeStyle = "-fx-background-color:rgba(0,224,184,0.15);-fx-cursor:hand;-fx-padding:10 18 10 18;";
        String inactiveTxt = "-fx-font-family:'" + Theme.FONT_BODY + "';-fx-font-size:13px;-fx-text-fill:rgba(255,255,255,0.65);-fx-font-weight:500;";
        String hoverTxt    = "-fx-font-family:'" + Theme.FONT_BODY + "';-fx-font-size:13px;-fx-text-fill:rgba(255,255,255,0.92);-fx-font-weight:500;";
        String activeTxt   = "-fx-font-family:'" + Theme.FONT_BODY + "';-fx-font-size:13px;-fx-text-fill:white;-fx-font-weight:bold;";

        item.setOnMouseEntered(e -> { if (item != activeNavItem) { item.setStyle(hoverStyle); txt.setStyle(hoverTxt); }});
        item.setOnMouseExited(e  -> { if (item != activeNavItem) { item.setStyle(inactiveStyle); txt.setStyle(inactiveTxt); }});
        item.setOnMouseClicked(e -> {
            // Deactivate old
            if (activeNavItem != null) { activeNavItem.setStyle(inactiveStyle); }
            // Activate new
            activeNavItem = item;
            item.setStyle(activeStyle);
            bar.setPrefHeight(28); bar.setStyle("-fx-background-color:#00e0b8;-fx-background-radius:3;-fx-min-height:28;-fx-max-height:28;");
            txt.setStyle(activeTxt);
            iconBox.setStyle("-fx-background-color:rgba(0,224,184,0.22);-fx-background-radius:8;");
            if (action != null) action.run();
        });

        navItems.add(item);
        if (firstNavItem == null) firstNavItem = item;
        nav.getChildren().add(item);
        return item;
    }

    private HBox buildSidebarFooter() {
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setMinWidth(Theme.SIDEBAR_WIDTH); footer.setMaxWidth(Theme.SIDEBAR_WIDTH);
        footer.setStyle("-fx-background-color:" + Theme.SIDEBAR_BG + ";-fx-border-color:rgba(255,255,255,0.10) transparent transparent transparent;-fx-border-width:1 0 0 0;-fx-padding:14 16 20 16;");

        StackPane av = UIComponents.avatar(user.getInitials(), 36);

        VBox info = new VBox(2);
        Label uName = new Label(user.getName());
        uName.setStyle("-fx-font-family:'" + Theme.FONT_BODY + "';-fx-font-size:12.5px;-fx-font-weight:bold;-fx-text-fill:white;");
        Label uRole = new Label(roleLabel(user.getRole()));
        uRole.setStyle("-fx-font-size:10.5px;-fx-text-fill:rgba(255,255,255,0.40);-fx-font-family:'" + Theme.FONT_BODY + "';");
        info.getChildren().addAll(uName, uRole);
        HBox.setHgrow(info, Priority.ALWAYS);

        // Logout button — visible, clickable
        Button logoutBtn = new Button("⏻ Logout");
        logoutBtn.setStyle("-fx-background-color:rgba(224,82,82,0.18);-fx-text-fill:rgba(255,180,180,0.9);-fx-font-family:'" + Theme.FONT_BODY + "';-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:8;-fx-padding:5 10 5 10;-fx-cursor:hand;-fx-border-width:0;");
        logoutBtn.setTooltip(new Tooltip("Log Out"));
        logoutBtn.setOnAction(e -> logout());
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle("-fx-background-color:rgba(224,82,82,0.35);-fx-text-fill:white;-fx-font-family:'" + Theme.FONT_BODY + "';-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:8;-fx-padding:5 10 5 10;-fx-cursor:hand;-fx-border-width:0;"));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle("-fx-background-color:rgba(224,82,82,0.18);-fx-text-fill:rgba(255,180,180,0.9);-fx-font-family:'" + Theme.FONT_BODY + "';-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:8;-fx-padding:5 10 5 10;-fx-cursor:hand;-fx-border-width:0;"));

        footer.getChildren().addAll(av, info, logoutBtn);
        return footer;
    }

    // ── HEADER (with working icons) ────────────────────────────────────────
    private HBox buildHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 24, 0, 28));
        header.setMinHeight(Theme.HEADER_HEIGHT);
        header.setMaxHeight(Theme.HEADER_HEIGHT);
        header.setStyle("-fx-background-color:rgba(255,255,255,0.70);-fx-border-color:transparent transparent rgba(255,255,255,0.75) transparent;-fx-border-width:0 0 1 0;-fx-effect:dropshadow(gaussian,rgba(0,120,96,0.06),4,0,0,1);");

        VBox titleBox = new VBox(2);
        pageTitleLabel = new Label("Dashboard");
        pageTitleLabel.setStyle("-fx-font-family:'" + Theme.FONT_HEADING + "';-fx-font-size:19px;-fx-font-weight:700;-fx-text-fill:" + Theme.TEXT_PRIMARY + ";");
        pageSubLabel = new Label("Welcome back");
        pageSubLabel.setStyle(Theme.labelStyle("12px", Theme.TEXT_MUTED, false));
        titleBox.getChildren().addAll(pageTitleLabel, pageSubLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        // Search bar
        HBox searchBar = new HBox(8); searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setStyle("-fx-background-color:rgba(255,255,255,0.78);-fx-border-color:rgba(0,176,144,0.20);-fx-border-width:1;-fx-border-radius:40;-fx-background-radius:40;-fx-padding:7 16 7 16;");
        searchBar.setMinWidth(220); searchBar.setMaxWidth(260);
        Label searchIco = new Label("🔍"); searchIco.setStyle("-fx-font-size:13px;");
        TextField searchField = new TextField(); searchField.setPromptText("Search medicines, donors…");
        searchField.setStyle("-fx-background-color:transparent;-fx-border-width:0;-fx-font-family:'" + Theme.FONT_BODY + "';-fx-font-size:13px;-fx-text-fill:" + Theme.TEXT_PRIMARY + ";");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchBar.getChildren().addAll(searchIco, searchField);

        // 🔔 Bell — opens notifications popup
        StackPane bellBtn = new StackPane();
        Circle bellBg = new Circle(18); bellBg.setFill(Color.web("#fff",0.78)); bellBg.setStroke(Color.web(Theme.TEAL_500,0.20)); bellBg.setStrokeWidth(1);
        Label bellIco = new Label("🔔"); bellIco.setStyle("-fx-font-size:15px;");
        Circle notifDot = new Circle(5); notifDot.setFill(Color.web(Theme.DANGER)); notifDot.setStroke(Color.WHITE); notifDot.setStrokeWidth(1.5);
        StackPane.setAlignment(notifDot, Pos.TOP_RIGHT); StackPane.setMargin(notifDot, new Insets(0,2,0,0));
        FadeTransition blink = new FadeTransition(Duration.millis(1200), notifDot);
        blink.setFromValue(1); blink.setToValue(0.2); blink.setAutoReverse(true); blink.setCycleCount(Timeline.INDEFINITE); blink.play();
        bellBtn.getChildren().addAll(bellBg, bellIco, notifDot);
        bellBtn.setMinSize(36,36); bellBtn.setMaxSize(36,36); bellBtn.setStyle("-fx-cursor:hand;");
        bellBtn.setOnMouseClicked(e -> UIComponents.showNotifications(stage));
        AnimationUtils.addHoverScale(bellBtn, 1.10);

        // 📅 Calendar — opens calendar popup
        StackPane calBtn = new StackPane();
        Circle calBg = new Circle(18); calBg.setFill(Color.web("#fff",0.78)); calBg.setStroke(Color.web(Theme.TEAL_500,0.20)); calBg.setStrokeWidth(1);
        Label calIco = new Label("📅"); calIco.setStyle("-fx-font-size:15px;");
        calBtn.getChildren().addAll(calBg, calIco);
        calBtn.setMinSize(36,36); calBtn.setMaxSize(36,36); calBtn.setStyle("-fx-cursor:hand;");
        calBtn.setOnMouseClicked(e -> UIComponents.showCalendar(stage));
        AnimationUtils.addHoverScale(calBtn, 1.10);

        // ＋ Add — overridable by subclasses
        StackPane addBtn = new StackPane();
        Circle addBg = new Circle(18); addBg.setFill(new LinearGradient(0,0,1,1,true,CycleMethod.NO_CYCLE, new Stop(0,Color.web("#00b090")), new Stop(1,Color.web("#007056"))));
        addBg.setEffect(new DropShadow(4,0,2,Color.web("#00b090",0.30)));
        Label addIco = new Label("＋"); addIco.setStyle("-fx-font-size:18px;-fx-text-fill:white;-fx-font-weight:bold;");
        addBtn.getChildren().addAll(addBg, addIco);
        addBtn.setMinSize(36,36); addBtn.setMaxSize(36,36); addBtn.setStyle("-fx-cursor:hand;");
        addBtn.setOnMouseClicked(e -> onAddButtonClicked());
        AnimationUtils.addHoverScale(addBtn, 1.10);

        // Avatar
        StackPane avatarBtn = UIComponents.avatar(user.getInitials(), 36);
        avatarBtn.setStyle("-fx-cursor:hand;");
        avatarBtn.setOnMouseClicked(e -> onAvatarClicked());
        AnimationUtils.addHoverScale(avatarBtn, 1.05);

        header.getChildren().addAll(titleBox, searchBar, bellBtn, calBtn, addBtn, avatarBtn);
        return header;
    }

    /** Override in subclasses to handle + button click */
    protected void onAddButtonClicked() { /* default: no-op */ }

    /** Override for avatar click */
    protected void onAvatarClicked() { /* default: no-op */ }

    // ── CONTENT SWITCH ────────────────────────────────────────────────────
    protected void showContent(Node content, String title, String subtitle) {
        if (pageTitleLabel != null) pageTitleLabel.setText(title);
        if (pageSubLabel  != null) pageSubLabel.setText(subtitle);
        AnimationUtils.pageTransition(currentPage, content, contentArea);
        currentPage = content;
    }

    // ── UTILS ─────────────────────────────────────────────────────────────
    private void logout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Log Out"); confirm.setHeaderText("Are you sure you want to log out?");
        confirm.setContentText("You will be returned to the login screen.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                SessionManager.getInstance().logout();
                new LoginScreen().show(stage);
            }
        });
    }

    private String roleLabel(String r) {
        return switch (r) {
            case "admin"     -> "Admin / Business Manager";
            case "household" -> "Household User";
            case "pharmacy"  -> "Pharmacy / Hospital";
            case "charity"   -> "Charity Organization";
            default -> r;
        };
    }
}
