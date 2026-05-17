package com.cureshare.views.shared;

import com.cureshare.utils.AnimationUtils;
import com.cureshare.utils.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.TextAlignment;

public class UIComponents {

    // ── GLASS CARDS ──────────────────────────────────────────────────────
    public static VBox glassCard() { VBox c=new VBox(); c.setStyle(Theme.glassCardStyle()); return c; }
    public static VBox glassCard(double pad) { VBox c=glassCard(); c.setPadding(new Insets(pad)); return c; }
    public static VBox glassDarkCard(double pad) { VBox c=new VBox(); c.setStyle(Theme.glassDarkStyle()); c.setPadding(new Insets(pad)); return c; }

    // ── STAT CARD ─────────────────────────────────────────────────────────
    public static VBox statCard(String emoji, String value, String label, String change, boolean pos) {
        VBox card=glassCard(20); card.setSpacing(7);
        Label icon=new Label(emoji);
        icon.setStyle("-fx-font-size:22px;-fx-background-color:rgba(0,176,144,0.10);-fx-background-radius:12;-fx-padding:7 9 7 9;");
        Label valLbl=new Label(value);
        valLbl.setStyle("-fx-font-family:'"+Theme.FONT_HEADING+"';-fx-font-size:26px;-fx-font-weight:800;-fx-text-fill:"+Theme.TEXT_PRIMARY+";");
        Label lbl=new Label(label); lbl.setStyle(Theme.labelStyle("12px",Theme.TEXT_SECONDARY,false));
        Label chg=new Label(change);
        chg.setStyle(pos
            ? "-fx-background-color:rgba(34,201,126,0.12);-fx-text-fill:#127a50;-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:20;-fx-padding:3 8 3 8;"
            : "-fx-background-color:rgba(224,82,82,0.12);-fx-text-fill:#b02020;-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:20;-fx-padding:3 8 3 8;");
        card.getChildren().addAll(icon,valLbl,lbl,chg);
        AnimationUtils.addHoverScale(card,1.02);
        return card;
    }

    // ── SECTION HEADER ────────────────────────────────────────────────────
    public static HBox sectionHeader(String title, String subtitle, Node... actions) {
        HBox h=new HBox(12); h.setAlignment(Pos.CENTER_LEFT); h.setPadding(new Insets(0,0,12,0));
        VBox titles=new VBox(3);
        Label t=new Label(title);
        t.setStyle("-fx-font-family:'"+Theme.FONT_HEADING+"';-fx-font-size:16px;-fx-font-weight:700;-fx-text-fill:"+Theme.TEXT_PRIMARY+";");
        titles.getChildren().add(t);
        if(subtitle!=null&&!subtitle.isBlank()){
            Label s=new Label(subtitle); s.setStyle(Theme.labelStyle("12px",Theme.TEXT_MUTED,false));
            titles.getChildren().add(s);
        }
        HBox.setHgrow(titles,Priority.ALWAYS); h.getChildren().add(titles);
        if(actions!=null){ for(Node a:actions){ if(a!=null) h.getChildren().add(a); } }
        return h;
    }

    // ── BUTTONS — all with proper hover ────────────────────────────────────
    public static Button primaryButton(String text) {
        Button b=new Button(text);
        b.setStyle(Theme.primaryButtonStyle());
        b.setOnMouseEntered(e->b.setStyle(Theme.primaryButtonHoverStyle()));
        b.setOnMouseExited(e->b.setStyle(Theme.primaryButtonStyle()));
        return b;
    }
    public static Button glassButton(String text) {
        Button b=new Button(text);
        b.setStyle(Theme.glassButtonStyle());
        b.setOnMouseEntered(e->b.setStyle(Theme.glassButtonHoverStyle()));
        b.setOnMouseExited(e->b.setStyle(Theme.glassButtonStyle()));
        return b;
    }
    public static Button dangerButton(String text) {
        String base="-fx-background-color:rgba(224,82,82,0.10);-fx-text-fill:#b02020;-fx-font-family:'"+Theme.FONT_BODY+"';-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:40;-fx-border-color:rgba(224,82,82,0.28);-fx-border-width:1;-fx-border-radius:40;-fx-padding:9 18 9 18;-fx-cursor:hand;";
        String hover="-fx-background-color:rgba(224,82,82,0.22);-fx-text-fill:#800000;-fx-font-family:'"+Theme.FONT_BODY+"';-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:40;-fx-border-color:rgba(224,82,82,0.50);-fx-border-width:1.5;-fx-border-radius:40;-fx-padding:9 18 9 18;-fx-cursor:hand;";
        Button b=new Button(text); b.setStyle(base);
        b.setOnMouseEntered(e->b.setStyle(hover)); b.setOnMouseExited(e->b.setStyle(base));
        return b;
    }
    public static Button smallButton(String text, boolean primary) {
        Button b=primary?primaryButton(text):glassButton(text);
        if(primary) {
            String base=Theme.primaryButtonStyle()+"-fx-font-size:11px;-fx-padding:5 12 5 12;";
            String hover=Theme.primaryButtonHoverStyle()+"-fx-font-size:11px;-fx-padding:5 12 5 12;";
            b.setStyle(base);
            b.setOnMouseEntered(e->b.setStyle(hover)); b.setOnMouseExited(e->b.setStyle(base));
        } else {
            String base=Theme.glassButtonStyle()+"-fx-font-size:11px;-fx-padding:5 12 5 12;";
            String hover=Theme.glassButtonHoverStyle()+"-fx-font-size:11px;-fx-padding:5 12 5 12;";
            b.setStyle(base);
            b.setOnMouseEntered(e->b.setStyle(hover)); b.setOnMouseExited(e->b.setStyle(base));
        }
        return b;
    }
    public static Button smallDangerButton(String text) {
        String base="-fx-background-color:rgba(224,82,82,0.10);-fx-text-fill:#b02020;-fx-font-family:'"+Theme.FONT_BODY+"';-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:40;-fx-border-color:rgba(224,82,82,0.28);-fx-border-width:1;-fx-border-radius:40;-fx-padding:5 12 5 12;-fx-cursor:hand;";
        String hover="-fx-background-color:rgba(224,82,82,0.22);-fx-text-fill:#800000;-fx-font-family:'"+Theme.FONT_BODY+"';-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:40;-fx-border-color:rgba(224,82,82,0.50);-fx-border-width:1.5;-fx-border-radius:40;-fx-padding:5 12 5 12;-fx-cursor:hand;";
        Button b=new Button(text); b.setStyle(base);
        b.setOnMouseEntered(e->b.setStyle(hover)); b.setOnMouseExited(e->b.setStyle(base));
        return b;
    }

    // ── INPUTS ────────────────────────────────────────────────────────────
    public static TextField styledTextField(String prompt) {
        TextField tf=new TextField(); tf.setPromptText(prompt); tf.setStyle(Theme.inputStyle());
        tf.focusedProperty().addListener((o,w,n)->tf.setStyle(n?Theme.inputFocusStyle():Theme.inputStyle())); return tf;
    }
    public static PasswordField styledPasswordField(String prompt) {
        PasswordField pf=new PasswordField(); pf.setPromptText(prompt); pf.setStyle(Theme.inputStyle());
        pf.focusedProperty().addListener((o,w,n)->pf.setStyle(n?Theme.inputFocusStyle():Theme.inputStyle())); return pf;
    }
    public static ComboBox<String> styledComboBox(String... items) {
        ComboBox<String> cb=new ComboBox<>();
        if(items!=null && items.length>0) { cb.getItems().addAll(items); cb.setValue(items[0]); }
        else { cb.getItems().add("—"); cb.setValue("—"); }
        cb.setStyle(Theme.inputStyle()+"-fx-cursor:hand;"); cb.setMaxWidth(Double.MAX_VALUE); return cb;
    }
    public static TextArea styledTextArea(String prompt) {
        TextArea ta=new TextArea(); ta.setPromptText(prompt); ta.setPrefRowCount(3);
        ta.setWrapText(true); ta.setStyle(Theme.inputStyle()); return ta;
    }

    // ── FORM ──────────────────────────────────────────────────────────────
    public static Label formLabel(String text) {
        Label l=new Label(text.toUpperCase());
        l.setStyle("-fx-font-family:'"+Theme.FONT_BODY+"';-fx-font-size:10.5px;-fx-font-weight:bold;-fx-text-fill:"+Theme.TEXT_SECONDARY+";");
        return l;
    }
    public static VBox formGroup(String label, Node field) {
        VBox g=new VBox(6); g.getChildren().add(formLabel(label));
        if(field!=null) g.getChildren().add(field); return g;
    }
    public static VBox formGroup(String label, Label valueLabel) {
        VBox g=new VBox(6); g.getChildren().add(formLabel(label));
        g.getChildren().add(valueLabel); return g;
    }

    // ── BADGE ─────────────────────────────────────────────────────────────
    public static Label badge(String text, String type) {
        Label l=new Label("● "+text);
        switch(type) {
            case "success" -> l.setStyle(Theme.badgeSuccess());
            case "warning" -> l.setStyle(Theme.badgeWarning());
            case "danger"  -> l.setStyle(Theme.badgeDanger());
            case "pending" -> l.setStyle(Theme.badgePending());
            default        -> l.setStyle(Theme.badgeInfo());
        }
        return l;
    }

    // ── AVATAR ────────────────────────────────────────────────────────────
    public static StackPane avatar(String init, double size, String c1, String c2) {
        StackPane sp=new StackPane();
        Circle c=new Circle(size/2);
        c.setFill(new LinearGradient(0,0,1,1,true,CycleMethod.NO_CYCLE,new Stop(0,Color.web(c1)),new Stop(1,Color.web(c2))));
        Label lbl=new Label(init);
        lbl.setStyle("-fx-font-family:'"+Theme.FONT_BODY+"';-fx-font-size:"+(size*0.34)+"px;-fx-font-weight:bold;-fx-text-fill:white;");
        sp.getChildren().addAll(c,lbl); sp.setMinSize(size,size); sp.setMaxSize(size,size); return sp;
    }
    public static StackPane avatar(String init, double size) { return avatar(init,size,Theme.TEAL_300,Theme.TEAL_700); }

    // ── PROGRESS BAR ─────────────────────────────────────────────────────
    public static StackPane progressBar(double pct, double maxW) {
        StackPane wrap=new StackPane(); wrap.setAlignment(Pos.CENTER_LEFT);
        wrap.setMinHeight(7); wrap.setMaxHeight(7); wrap.setMaxWidth(maxW);
        wrap.setStyle("-fx-background-color:rgba(0,176,144,0.10);-fx-background-radius:20;");
        String col=pct<0.25?"linear-gradient(to right,#e05252,#b02020)":pct<0.5?"linear-gradient(to right,#f0a500,#b07a00)":"linear-gradient(to right,#1fbda0,#007056)";
        Region fill=new Region();
        fill.setStyle("-fx-background-color:"+col+";-fx-background-radius:20;");
        fill.setMinHeight(7); fill.setMaxHeight(7); fill.setPrefWidth(0);
        wrap.getChildren().add(fill); AnimationUtils.animateWidth(fill,maxW*pct,300); return wrap;
    }

    // ── DIVIDER ───────────────────────────────────────────────────────────
    public static Region divider() {
        Region r=new Region(); r.setPrefHeight(1); r.setMaxHeight(1);
        r.setStyle("-fx-background-color:rgba(0,176,144,0.12);"); return r;
    }

    // ── ALERT ITEM ────────────────────────────────────────────────────────
    public static HBox alertItem(String ico, String title, String msg, String borderColor) {
        HBox box=new HBox(14); box.setAlignment(Pos.TOP_LEFT); box.setPadding(new Insets(14,16,14,16));
        box.setStyle("-fx-background-color:rgba(255,255,255,0.60);-fx-background-radius:14;-fx-border-color:transparent transparent transparent "+borderColor+";-fx-border-width:0 0 0 3;-fx-border-radius:0;-fx-effect:dropshadow(gaussian,rgba(0,150,120,0.08),5,0,0,2);");
        Label i=new Label(ico); i.setStyle("-fx-font-size:18px;");
        VBox cnt=new VBox(3);
        Label ttl=new Label(title); ttl.setStyle(Theme.labelStyle("13px",Theme.TEXT_PRIMARY,true));
        Label m=new Label(msg); m.setStyle(Theme.labelStyle("12px",Theme.TEXT_MUTED,false)); m.setWrapText(true);
        cnt.getChildren().addAll(ttl,m); HBox.setHgrow(cnt,Priority.ALWAYS);
        box.getChildren().addAll(i,cnt); return box;
    }

    // ── TABS BAR ──────────────────────────────────────────────────────────
    public static HBox tabsBar(Runnable[] actions, String... names) {
        HBox bar=new HBox(4);
        bar.setStyle("-fx-background-color:rgba(255,255,255,0.52);-fx-background-radius:40;-fx-border-color:rgba(255,255,255,0.72);-fx-border-width:1;-fx-border-radius:40;-fx-padding:4;");
        bar.setAlignment(Pos.CENTER);
        for(int i=0;i<names.length;i++){
            final Label tab=new Label(names[i]);
            final Runnable action=(actions!=null&&i<actions.length)?actions[i]:null;
            if(i==0) activateTab(tab); else deactivateTab(tab);
            tab.setOnMouseClicked(e->{
                bar.getChildren().forEach(n->{ if(n instanceof Label l) deactivateTab(l); });
                activateTab(tab);
                if(action!=null) action.run();
            });
            bar.getChildren().add(tab);
        }
        return bar;
    }
    public static HBox tabsBar(String... names) { return tabsBar(null,names); }
    private static void activateTab(Label t) {
        t.setStyle("-fx-background-color:linear-gradient(to right,#00b090,#007056);-fx-text-fill:white;-fx-background-radius:40;-fx-padding:7 18 7 18;-fx-cursor:hand;-fx-font-family:'"+Theme.FONT_BODY+"';-fx-font-size:13px;-fx-font-weight:bold;");
    }
    private static void deactivateTab(Label t) {
        t.setStyle("-fx-background-radius:40;-fx-padding:7 18 7 18;-fx-cursor:hand;-fx-font-family:'"+Theme.FONT_BODY+"';-fx-font-size:13px;-fx-text-fill:"+Theme.TEXT_SECONDARY+";");
    }

    // ── BAR CHART ─────────────────────────────────────────────────────────
    public static HBox barChart(String[] labels, double[] values, double maxVal, double height) {
        HBox chart=new HBox(8); chart.setAlignment(Pos.BOTTOM_LEFT); chart.setPrefHeight(height);
        for(int i=0;i<labels.length;i++){
            double pct=values[i]/maxVal;
            VBox col=new VBox(5); col.setAlignment(Pos.BOTTOM_CENTER); HBox.setHgrow(col,Priority.ALWAYS);
            Region bar=new Region(); bar.setPrefWidth(Double.MAX_VALUE); bar.setMinHeight(0); bar.setPrefHeight(0);
            boolean isLast=(i==labels.length-1);
            String color=isLast?"linear-gradient(to top,rgba(0,112,86,0.85),rgba(0,224,184,1))":"linear-gradient(to top,rgba(0,176,144,0.45),rgba(0,224,184,0.65))";
            bar.setStyle("-fx-background-color:"+color+";-fx-background-radius:6 6 0 0;-fx-border-color:rgba(0,224,184,0.25);-fx-border-width:1;-fx-border-radius:6 6 0 0;");
            final double tH=height*0.85*pct; final int idx=i;
            javafx.animation.Timeline tl=new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(900+idx*60),
                    new javafx.animation.KeyValue(bar.prefHeightProperty(),tH,javafx.animation.Interpolator.SPLINE(0.22,1,0.36,1))));
            tl.setDelay(javafx.util.Duration.millis(300)); tl.play();
            Label lbl=new Label(labels[i]); lbl.setStyle(Theme.labelStyle("10px",Theme.TEXT_MUTED,false));
            StackPane bw=new StackPane(bar); bw.setAlignment(Pos.BOTTOM_CENTER); bw.setPrefHeight(height*0.85);
            col.getChildren().addAll(bw,lbl); chart.getChildren().add(col);
        }
        return chart;
    }

    // ── HORIZONTAL BAR ────────────────────────────────────────────────────
    public static VBox hBarChart(String[] labels, double[] values, String[] colors) {
        VBox box=new VBox(10); double maxV=0;
        for(double v:values) if(v>maxV) maxV=v;
        for(int i=0;i<labels.length;i++){
            double pct=values[i]/maxV;
            HBox row=new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
            Label lbl=new Label(labels[i]); lbl.setStyle(Theme.labelStyle("12px",Theme.TEXT_MUTED,false)); lbl.setMinWidth(120); lbl.setMaxWidth(120);
            StackPane bg=new StackPane(); bg.setAlignment(Pos.CENTER_LEFT);
            bg.setMinHeight(9); bg.setMaxHeight(9);
            bg.setStyle("-fx-background-color:rgba(0,176,144,0.10);-fx-background-radius:20;"); HBox.setHgrow(bg,Priority.ALWAYS);
            Region fill=new Region(); fill.setMinHeight(9); fill.setMaxHeight(9);
            String col=(colors!=null&&i<colors.length)?colors[i]:"linear-gradient(to right,#1fbda0,#007056)";
            fill.setStyle("-fx-background-color:"+col+";-fx-background-radius:20;"); fill.setPrefWidth(0);
            bg.getChildren().add(fill);
            String vStr=values[i]>=1000?String.format("%.1fK",values[i]/1000):String.valueOf((int)values[i]);
            Label vLbl=new Label(vStr); vLbl.setStyle(Theme.labelStyle("12px",Theme.TEXT_SECONDARY,true)); vLbl.setMinWidth(45);
            row.getChildren().addAll(lbl,bg,vLbl); box.getChildren().add(row);
            AnimationUtils.animateWidth(fill,200*pct,300+i*80);
        }
        return box;
    }

    // ── DONUT CHART ───────────────────────────────────────────────────────
    public static StackPane donutChart(double fillPct, String cVal, String cLabel) {
        double r=46, circ=2*Math.PI*r;
        StackPane sp=new StackPane(); sp.setMinSize(110,110); sp.setMaxSize(110,110);
        Circle bg=new Circle(r); bg.setFill(Color.TRANSPARENT); bg.setStroke(Color.web(Theme.TEAL_500,0.12)); bg.setStrokeWidth(10);
        Circle arc=new Circle(r); arc.setFill(Color.TRANSPARENT);
        arc.setStroke(new LinearGradient(0,0,1,0,true,CycleMethod.NO_CYCLE,new Stop(0,Color.web("#00e0b8")),new Stop(1,Color.web(Theme.TEAL_700))));
        arc.setStrokeWidth(10); arc.setStrokeLineCap(StrokeLineCap.ROUND);
        arc.getStrokeDashArray().setAll(circ,circ); arc.setStrokeDashOffset(circ); arc.setRotate(-90);
        javafx.animation.Timeline tl=new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(1200),
                new javafx.animation.KeyValue(arc.strokeDashOffsetProperty(),circ*(1-fillPct),javafx.animation.Interpolator.EASE_OUT)));
        tl.setDelay(javafx.util.Duration.millis(400)); tl.play();
        VBox center=new VBox(2); center.setAlignment(Pos.CENTER);
        Label val=new Label(cVal); val.setStyle("-fx-font-family:'"+Theme.FONT_HEADING+"';-fx-font-size:19px;-fx-font-weight:800;-fx-text-fill:"+Theme.TEXT_PRIMARY+";");
        Label sub=new Label(cLabel); sub.setStyle(Theme.labelStyle("9.5px",Theme.TEXT_MUTED,false));
        center.getChildren().addAll(val,sub); sp.getChildren().addAll(bg,arc,center); return sp;
    }

    // ── TABLE UTILS ───────────────────────────────────────────────────────
    public static HBox tableHeader(String[] cols, double[] widths) {
        HBox head=new HBox(0);
        head.setStyle("-fx-background-color:rgba(0,176,144,0.08);-fx-padding:9 14 9 14;-fx-border-color:transparent transparent rgba(0,176,144,0.12) transparent;-fx-border-width:0 0 1 0;");
        for(int i=0;i<cols.length;i++){
            Label l=new Label(cols[i].toUpperCase()); l.setStyle(Theme.labelStyle("10px",Theme.TEXT_SECONDARY,true));
            if(i==cols.length-1) HBox.setHgrow(l,Priority.ALWAYS);
            else { l.setMinWidth(widths[i]); l.setPrefWidth(widths[i]); }
            head.getChildren().add(l);
        }
        return head;
    }
    public static HBox tableRow(boolean alt) {
        HBox row=new HBox(0); row.setAlignment(Pos.CENTER_LEFT); row.setPadding(new Insets(11,14,11,14));
        String base=(alt?"-fx-background-color:rgba(0,176,144,0.025);":"")+"-fx-border-color:transparent transparent rgba(255,255,255,0.55) transparent;-fx-border-width:0 0 1 0;";
        row.setStyle(base);
        row.setOnMouseEntered(e->row.setStyle("-fx-background-color:rgba(0,176,144,0.06);-fx-padding:11 14 11 14;-fx-border-color:transparent transparent rgba(255,255,255,0.55) transparent;-fx-border-width:0 0 1 0;"));
        row.setOnMouseExited(e->row.setStyle(base+"-fx-padding:11 14 11 14;")); return row;
    }
    public static Label cell(String text, double width, boolean bold, String color) {
        Label l=new Label(text!=null?text:"—"); l.setStyle(Theme.labelStyle("12.5px",color!=null?color:Theme.TEXT_PRIMARY,bold));
        if(width>0){ l.setMinWidth(width); l.setPrefWidth(width); } return l;
    }
    public static Label cell(String text, double width) { return cell(text,width,false,Theme.TEXT_SECONDARY); }

    // ── METRIC ROW ────────────────────────────────────────────────────────
    public static HBox metricRow(String k, String v, String color) {
        HBox r=new HBox(); r.setAlignment(Pos.CENTER_LEFT);
        Label kl=new Label(k); kl.setStyle(Theme.labelStyle("12px",Theme.TEXT_MUTED,false)); HBox.setHgrow(kl,Priority.ALWAYS);
        Label vl=new Label(v!=null?v:"—"); vl.setStyle(Theme.labelStyle("13px",color!=null?color:Theme.TEXT_PRIMARY,true));
        r.getChildren().addAll(kl,vl); return r;
    }

    // ── TOGGLE ────────────────────────────────────────────────────────────
    /** Toggle with a callback fired when state changes */
    public static HBox toggleWithCallback(boolean on, String label, java.util.function.Consumer<Boolean> onChange) {
        final boolean[] state={on};
        HBox track=new HBox(); track.setAlignment(Pos.CENTER_LEFT); track.setPadding(new Insets(3));
        track.setMinSize(42,22); track.setMaxSize(42,22);
        Region knob=new Region(); knob.setMinSize(16,16); knob.setMaxSize(16,16);
        knob.setStyle("-fx-background-color:white;-fx-background-radius:50;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.10),2,0,0,1);");
        if(on){ Region spacer=new Region(); spacer.setMinWidth(20); spacer.setMaxWidth(20); track.getChildren().addAll(spacer,knob); }
        else track.getChildren().add(knob);
        updateTrack(track,state[0]);
        track.setOnMouseClicked(e->{
            state[0]=!state[0];
            track.getChildren().clear();
            if(state[0]){ Region sp=new Region(); sp.setMinWidth(20); sp.setMaxWidth(20); track.getChildren().addAll(sp,knob); }
            else track.getChildren().add(knob);
            updateTrack(track,state[0]);
            if(onChange!=null) onChange.accept(state[0]);
        });
        HBox wrapper=new HBox(10); wrapper.setAlignment(Pos.CENTER_LEFT);
        Label lbl=new Label(label); lbl.setStyle(Theme.labelStyle("13px",Theme.TEXT_PRIMARY,false));
        wrapper.getChildren().addAll(track,lbl); return wrapper;
    }

    public static HBox toggle(boolean on, String label) {
        final boolean[] state={on};
        HBox track=new HBox(); track.setAlignment(Pos.CENTER_LEFT); track.setPadding(new Insets(3));
        track.setMinSize(42,22); track.setMaxSize(42,22);
        Region knob=new Region(); knob.setMinSize(16,16); knob.setMaxSize(16,16);
        knob.setStyle("-fx-background-color:white;-fx-background-radius:50;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.10),2,0,0,1);");
        if(on){ Region spacer=new Region(); spacer.setMinWidth(20); spacer.setMaxWidth(20); track.getChildren().addAll(spacer,knob); }
        else track.getChildren().add(knob);
        updateTrack(track,state[0]);
        track.setOnMouseClicked(e->{
            state[0]=!state[0];
            track.getChildren().clear();
            if(state[0]){ Region sp=new Region(); sp.setMinWidth(20); sp.setMaxWidth(20); track.getChildren().addAll(sp,knob); }
            else track.getChildren().add(knob);
            updateTrack(track,state[0]);
        });
        HBox wrapper=new HBox(10); wrapper.setAlignment(Pos.CENTER_LEFT);
        Label lbl=new Label(label); lbl.setStyle(Theme.labelStyle("13px",Theme.TEXT_PRIMARY,false));
        wrapper.getChildren().addAll(track,lbl); return wrapper;
    }
    private static void updateTrack(HBox t, boolean on) {
        t.setStyle((on?"-fx-background-color:"+Theme.TEAL_500+";":"-fx-background-color:rgba(0,176,144,0.20);")+"-fx-background-radius:20;-fx-cursor:hand;-fx-padding:3;");
    }

    // ── PICKUP CARD ───────────────────────────────────────────────────────
    public static HBox pickupCard(String init, String c1, String c2, String name, String addr, String time, String items, String status, String statusType) {
        HBox row=new HBox(12); row.setAlignment(Pos.CENTER_LEFT); row.setPadding(new Insets(13,16,13,16)); row.setStyle(Theme.glassDarkStyle());
        StackPane av=avatar(init,40,c1,c2);
        VBox info=new VBox(2);
        Label nm=new Label(name); nm.setStyle(Theme.labelStyle("13.5px",Theme.TEXT_PRIMARY,true));
        Label ad=new Label("📍 "+addr); ad.setStyle(Theme.labelStyle("11px",Theme.TEXT_MUTED,false));
        info.getChildren().addAll(nm,ad); HBox.setHgrow(info,Priority.ALWAYS);
        VBox meta=new VBox(3); meta.setAlignment(Pos.CENTER_RIGHT);
        Label tm=new Label(time); tm.setStyle(Theme.labelStyle("12px",Theme.TEAL_600,true));
        Label it=new Label(items); it.setStyle(Theme.labelStyle("11px",Theme.TEXT_MUTED,false));
        meta.getChildren().addAll(tm,it,badge(status,statusType));
        row.getChildren().addAll(av,info,meta); return row;
    }

    // ── INFO BOX ──────────────────────────────────────────────────────────
    public static VBox infoBox(String val, String label, String bg, String fg) {
        VBox box=new VBox(4); box.setAlignment(Pos.CENTER); box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color:"+bg+";-fx-background-radius:14;-fx-border-color:rgba(0,176,144,0.15);-fx-border-width:1;-fx-border-radius:14;");
        Label v=new Label(val); v.setStyle("-fx-font-family:'"+Theme.FONT_HEADING+"';-fx-font-size:22px;-fx-font-weight:800;-fx-text-fill:"+fg+";");
        Label l=new Label(label); l.setStyle(Theme.labelStyle("11px",Theme.TEXT_MUTED,false)); l.setWrapText(true); l.setTextAlignment(TextAlignment.CENTER);
        box.getChildren().addAll(v,l); return box;
    }

    // ── CHIP ──────────────────────────────────────────────────────────────
    public static Label chip(String text) {
        Label l=new Label(text);
        l.setStyle("-fx-background-color:rgba(0,176,144,0.10);-fx-background-radius:20;-fx-padding:3 8 3 8;-fx-font-size:11px;-fx-font-family:'"+Theme.FONT_BODY+"';-fx-text-fill:"+Theme.TEXT_SECONDARY+";");
        return l;
    }

    // ── NOTIFICATION DIALOG ───────────────────────────────────────────────
    public static void showNotifications(javafx.stage.Stage owner) {
        javafx.stage.Stage dlg=new javafx.stage.Stage();
        dlg.initOwner(owner); dlg.initModality(javafx.stage.Modality.NONE);
        dlg.setTitle("Notifications"); dlg.setResizable(false);
        VBox root=new VBox(0); root.setStyle("-fx-background-color:rgba(255,255,255,0.97);"); root.setPrefWidth(380);
        Label title=new Label("🔔  Notifications");
        title.setStyle("-fx-font-family:'"+Theme.FONT_HEADING+"';-fx-font-size:16px;-fx-font-weight:700;-fx-text-fill:"+Theme.TEXT_PRIMARY+";-fx-padding:16 20 12 20;-fx-border-color:transparent transparent rgba(0,176,144,0.12) transparent;-fx-border-width:0 0 1 0;");
        root.getChildren().add(title);
        String[][] notifs={
            {"⚠️","Expiry Alert","Amoxicillin 500mg expires in 8 days — redistribute now","danger","2 min ago"},
            {"📉","Low Stock","Metformin 850mg — only 18 units remaining","warning","18 min ago"},
            {"✅","Request Approved","Hope Foundation allocation confirmed (200 units)","success","1 hr ago"},
            {"🚗","Pickup Complete","Route R-12: 8 items collected from Ahmad Khan","info","3 hrs ago"},
            {"💊","New Submission","35 medicines submitted by MedPlus Pharmacy","info","5 hrs ago"},
            {"📋","Report Ready","March 2026 financial report generated","info","Yesterday"},
        };
        for(String[] n:notifs){
            HBox item=new HBox(12); item.setAlignment(Pos.TOP_LEFT); item.setPadding(new Insets(12,20,12,20));
            String base="-fx-border-color:transparent transparent rgba(0,176,144,0.08) transparent;-fx-border-width:0 0 1 0;-fx-cursor:hand;";
            item.setStyle(base);
            item.setOnMouseEntered(e->item.setStyle("-fx-background-color:rgba(0,176,144,0.04);"+base));
            item.setOnMouseExited(e->item.setStyle(base));
            Label ico=new Label(n[0]); ico.setStyle("-fx-font-size:18px;");
            VBox cnt=new VBox(2);
            Label nt=new Label(n[1]); nt.setStyle(Theme.labelStyle("13px",Theme.TEXT_PRIMARY,true));
            Label nm=new Label(n[2]); nm.setStyle(Theme.labelStyle("12px",Theme.TEXT_MUTED,false)); nm.setWrapText(true);
            Label ntime=new Label(n[4]); ntime.setStyle(Theme.labelStyle("10.5px",Theme.TEXT_MUTED,false));
            cnt.getChildren().addAll(nt,nm,ntime); HBox.setHgrow(cnt,Priority.ALWAYS);
            Circle dot=new Circle(4);
            dot.setFill(Color.web(n[3].equals("danger")?Theme.DANGER:n[3].equals("warning")?Theme.WARNING:n[3].equals("success")?Theme.SUCCESS:Theme.TEAL_500));
            item.getChildren().addAll(ico,cnt,dot); root.getChildren().add(item);
        }
        Button closeBtn=primaryButton("Close"); closeBtn.setMaxWidth(Double.MAX_VALUE);
        closeBtn.setOnAction(e->dlg.close());
        VBox footer=new VBox(); footer.setPadding(new Insets(12,20,16,20)); footer.getChildren().add(closeBtn);
        root.getChildren().add(footer);
        dlg.setScene(new javafx.scene.Scene(root)); dlg.sizeToScene(); dlg.show();
    }

    // ── CALENDAR DIALOG ───────────────────────────────────────────────────
    public static void showCalendar(javafx.stage.Stage owner) {
        javafx.stage.Stage dlg=new javafx.stage.Stage();
        dlg.initOwner(owner); dlg.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dlg.setTitle("Calendar — March 2026"); dlg.setResizable(false);
        VBox root=new VBox(16); root.setPadding(new Insets(24)); root.setStyle("-fx-background-color:#f0faf7;"); root.setPrefWidth(360);
        Label title=new Label("📅  March 2026"); title.setStyle("-fx-font-family:'"+Theme.FONT_HEADING+"';-fx-font-size:18px;-fx-font-weight:800;-fx-text-fill:"+Theme.TEXT_PRIMARY+";");
        GridPane cal=new GridPane(); cal.setHgap(8); cal.setVgap(8);
        String[] days={"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        for(int i=0;i<days.length;i++){
            Label dl=new Label(days[i]); dl.setStyle(Theme.labelStyle("11px",Theme.TEXT_MUTED,true)); dl.setMinWidth(36); dl.setAlignment(Pos.CENTER); cal.add(dl,i,0);
        }
        int[] dates={0,0,0,0,0,0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31};
        int[] pickupDays={5,10,14,18,22,26};
        int row2=1,col=0;
        for(int d:dates){
            Label dl=new Label(d==0?"":String.valueOf(d));
            boolean isPickup=false; for(int p:pickupDays) if(p==d) isPickup=true;
            boolean today=d==20;
            dl.setMinSize(36,36); dl.setMaxSize(36,36); dl.setAlignment(Pos.CENTER);
            if(today) dl.setStyle("-fx-background-color:"+Theme.TEAL_500+";-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:50;-fx-font-family:'"+Theme.FONT_BODY+"';-fx-font-size:13px;");
            else if(isPickup&&d>0) dl.setStyle("-fx-background-color:rgba(0,176,144,0.15);-fx-text-fill:"+Theme.TEAL_700+";-fx-font-weight:bold;-fx-background-radius:50;-fx-font-family:'"+Theme.FONT_BODY+"';-fx-font-size:13px;");
            else dl.setStyle(Theme.labelStyle("13px",d==0?Theme.TEXT_MUTED:Theme.TEXT_PRIMARY,false));
            cal.add(dl,col,row2); col++; if(col==7){col=0;row2++;}
        }
        VBox legendBox=new VBox(6);
        legendBox.getChildren().addAll(legendRow(Theme.TEAL_500,"Today — March 20"),legendRow("rgba(0,176,144,0.50)","Pickup scheduled"));
        Button closeBtn=primaryButton("Close"); closeBtn.setOnAction(e->dlg.close());
        root.getChildren().addAll(title,cal,legendBox,closeBtn);
        dlg.setScene(new javafx.scene.Scene(root)); dlg.sizeToScene(); dlg.show();
    }
    private static HBox legendRow(String color,String text) {
        HBox h=new HBox(8); h.setAlignment(Pos.CENTER_LEFT);
        Circle c=new Circle(7); c.setFill(Color.web(color));
        Label l=new Label(text); l.setStyle(Theme.labelStyle("12px",Theme.TEXT_SECONDARY,false));
        h.getChildren().addAll(c,l); return h;
    }

    // ── COLUMN CONSTRAINT HELPER ─────────────────────────────────────────
    public static ColumnConstraints pct(double p) {
        ColumnConstraints cc=new ColumnConstraints(); cc.setPercentWidth(p); return cc;
    }
}
