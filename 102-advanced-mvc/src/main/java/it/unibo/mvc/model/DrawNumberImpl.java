package it.unibo.mvc.model;

import java.util.Random;

import it.unibo.mvc.Configuration;
import it.unibo.mvc.DrawResult;

/**
 *
 */
public final class DrawNumberImpl implements DrawNumber {

    private int choice;
    private final int min;
    private final int max;
    private final int attempts;
    private int remainingAttempts;
    private final Random random = new Random();

    /**
     * @param config the builded configuration
     * 
     * @throws IllegalStateException if the configuration is not consistent
     */
    public DrawNumberImpl(final Configuration config) {
        if (!config.isConsistent()) {
            throw new IllegalStateException("The builder can only be used once");
        }
        this.min = config.getMin();
        this.max = config.getMax();
        this.attempts = config.getAttempts();
        this.reset();
    }

    @Override
    public void reset() {
        this.remainingAttempts = this.attempts;
        this.choice = this.min + random.nextInt(this.max - this.min + 1);
    }

    @Override
    public DrawResult attempt(final int n) {
        if (this.remainingAttempts <= 0) {
            return DrawResult.YOU_LOST;
        }
        if (n < this.min || n > this.max) {
            throw new IllegalArgumentException("The number is outside boundaries");
        }
        remainingAttempts--;
        if (n > this.choice) {
            return DrawResult.YOURS_HIGH;
        }
        if (n < this.choice) {
            return DrawResult.YOURS_LOW;
        }
        return DrawResult.YOU_WON;
    }

}
