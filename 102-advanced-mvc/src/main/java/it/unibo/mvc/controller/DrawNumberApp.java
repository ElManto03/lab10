package it.unibo.mvc.controller;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import it.unibo.mvc.Configuration;
import it.unibo.mvc.Configuration.Builder;
import it.unibo.mvc.DrawResult;
import it.unibo.mvc.model.DrawNumber;
import it.unibo.mvc.model.DrawNumberImpl;
import it.unibo.mvc.view.DrawNumberView;
import it.unibo.mvc.view.DrawNumberViewImpl;
import it.unibo.mvc.view.PrintStreamView;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {

    private static final String MIN = "minimum";
    private static final String MAX = "maximum";
    private static final String ATTEMPTS = "attempts";
    private static final String PATH = "config.yml";

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view : views) {
            view.setObserver(this);
            view.start();
        }
        this.model = new DrawNumberImpl(buildView(new Builder()));
    }

    private Configuration buildView(final Builder builder) {
        final InputStream in = ClassLoader.getSystemResourceAsStream(PATH);
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) { // NOPMD
                final StringTokenizer splitter = new StringTokenizer(line);
                if (splitter.countTokens() == 2) {
                    setBuild(splitter.nextToken(": "),
                            splitter.nextToken(": "),
                            builder);
                }
            }
        } catch (final IOException e) {
            for (final DrawNumberView view : views) {
                view.displayError("Error with file, default values are set instead");
            }
        }
        return builder.build();
    }

    private void setBuild(final String type, final String value, final Builder builder) {
        try {
            final int intValue = Integer.parseInt(value);
            switch (type) {
                case MIN -> builder.setMin(intValue);
                case MAX -> builder.setMax(intValue);
                case ATTEMPTS -> builder.setAttempts(intValue);
                default -> { }
            }
        } catch (final NumberFormatException e) {
            for (final DrawNumberView view : views) {
                view.displayError("Invalid argument in config file, default value is set instead");
            }
        }
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view : views) {
                view.result(result);
            }
        } catch (final IllegalArgumentException e) {
            for (final DrawNumberView view : views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *             ignored
     * @throws FileNotFoundException
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl(),
                new DrawNumberViewImpl(),
                new PrintStreamView("log.txt"),
                new PrintStreamView(System.out));
    }
}
