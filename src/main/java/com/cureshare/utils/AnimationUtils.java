package com.cureshare.utils;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class AnimationUtils {

    // ── Tuning constants ────────────────────────────────────────────
    // Shorter durations = smoother feel on lower-end hardware
    private static final double FADE_MS    = 220;  // was 350
    private static final double STAGGER_MS = 55;   // was 80
    private static final double PAGE_MS    = 80;   // was 110
    private static final double SLIDE_MS   = 280;  // was 420
    private static final double SCALE_MS   = 180;  // was 280
    private static final double HOVER_MS   = 100;  // was 160
    private static final double TRANSLATE_Y = 8;   // was 14 — less movement = smoother

    public static void fadeIn(Node node) { fadeIn(node, FADE_MS, 0); }

    public static void fadeIn(Node node, double ms, double delayMs) {
        if (node == null) return;
        node.setOpacity(0);
        node.setTranslateY(TRANSLATE_Y);
        FadeTransition f = new FadeTransition(Duration.millis(ms), node);
        f.setFromValue(0); f.setToValue(1);
        f.setDelay(Duration.millis(delayMs));
        f.setInterpolator(Interpolator.EASE_OUT);
        TranslateTransition t = new TranslateTransition(Duration.millis(ms), node);
        t.setFromY(TRANSLATE_Y); t.setToY(0);
        t.setDelay(Duration.millis(delayMs));
        t.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(f, t).play();
    }

    public static void staggerFadeIn(Node... nodes) {
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] != null) fadeIn(nodes[i], FADE_MS, i * STAGGER_MS);
        }
    }

    public static void scaleIn(Node node) {
        if (node == null) return;
        node.setOpacity(0); node.setScaleX(0.96); node.setScaleY(0.96); // was 0.90 — less scale = smoother
        FadeTransition f = new FadeTransition(Duration.millis(SCALE_MS), node);
        f.setFromValue(0); f.setToValue(1); f.setInterpolator(Interpolator.EASE_OUT);
        ScaleTransition s = new ScaleTransition(Duration.millis(SCALE_MS), node);
        s.setFromX(0.96); s.setToX(1.0); s.setFromY(0.96); s.setToY(1.0);
        s.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(f, s).play();
    }

    public static void slideInLeft(Node node) {
        if (node == null) return;
        node.setTranslateX(-Theme.SIDEBAR_WIDTH); node.setOpacity(0);
        TranslateTransition t = new TranslateTransition(Duration.millis(SLIDE_MS), node);
        t.setFromX(-Theme.SIDEBAR_WIDTH); t.setToX(0);
        t.setInterpolator(Interpolator.SPLINE(0.25, 0.1, 0.25, 1.0)); // cubic ease — smoother than EASE_OUT
        FadeTransition f = new FadeTransition(Duration.millis(SLIDE_MS), node);
        f.setFromValue(0); f.setToValue(1); f.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(t, f).play();
    }

    public static void addHoverScale(Node node, double scale) {
        if (node == null) return;
        // Use opacity-based hover instead of scale — much less GPU load, no stutter
        node.setOnMouseEntered(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(HOVER_MS), node);
            ft.setToValue(0.85); ft.setInterpolator(Interpolator.EASE_OUT); ft.play();
        });
        node.setOnMouseExited(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(HOVER_MS), node);
            ft.setToValue(1.0); ft.setInterpolator(Interpolator.EASE_OUT); ft.play();
        });
    }

    public static void pulse(Node node) {
        if (node == null) return;
        // Subtle opacity pulse instead of scale — much smoother, less CPU
        FadeTransition f = new FadeTransition(Duration.millis(1800), node);
        f.setFromValue(1.0); f.setToValue(0.70);
        f.setAutoReverse(true); f.setCycleCount(Animation.INDEFINITE);
        f.setInterpolator(Interpolator.EASE_BOTH); f.play();
    }

    public static void animateWidth(Region bar, double targetWidth, double delayMs) {
        if (bar == null) return;
        bar.setPrefWidth(0);
        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(bar.prefWidthProperty(), 0, Interpolator.EASE_IN)),
            new KeyFrame(Duration.millis(600), // was 900 — faster feels snappier
                new KeyValue(bar.prefWidthProperty(), targetWidth, Interpolator.SPLINE(0.25,0.1,0.25,1.0))));
        tl.setDelay(Duration.millis(delayMs));
        tl.play();
    }

    public static void animateCounter(Label label, int target, String prefix, String suffix) {
        if (label == null || target <= 0) { if(label!=null) label.setText(prefix+"0"+suffix); return; }
        final int[] cur = {0};
        int step = Math.max(1, target / 40); // fewer frames = less CPU
        Timeline tl = new Timeline();
        tl.setCycleCount(Timeline.INDEFINITE);
        tl.getKeyFrames().add(new KeyFrame(Duration.millis(24), e -> { // was 18ms (60fps) → 24ms (~42fps) fine for numbers
            cur[0] = Math.min(cur[0] + step, target);
            label.setText(prefix + String.format("%,d", cur[0]) + suffix);
            if (cur[0] >= target) tl.stop();
        }));
        tl.play();
    }

    public static void shake(Node node) {
        if (node == null) return;
        TranslateTransition t = new TranslateTransition(Duration.millis(50), node);
        t.setByX(6); t.setCycleCount(6); t.setAutoReverse(true); t.play();
    }

    public static void pageTransition(Node oldPage, Node newPage, StackPane area) {
        if (oldPage != null) {
            // Instant clear + fade in — eliminates the double-render stutter
            FadeTransition ft = new FadeTransition(Duration.millis(PAGE_MS), oldPage);
            ft.setToValue(0);
            ft.setOnFinished(e -> {
                area.getChildren().clear();
                if (newPage != null) {
                    newPage.setOpacity(0);
                    area.getChildren().add(newPage);
                    FadeTransition fi = new FadeTransition(Duration.millis(FADE_MS), newPage);
                    fi.setFromValue(0); fi.setToValue(1);
                    fi.setInterpolator(Interpolator.EASE_OUT);
                    fi.play();
                }
            });
            ft.play();
        } else {
            area.getChildren().clear();
            if (newPage != null) { area.getChildren().add(newPage); fadeIn(newPage, FADE_MS, 0); }
        }
    }
}
