package me.jamino.wynnWanderer.features;

/**
 * Tracks the fade-in, display and fade-out timers of the title, as well as the
 * cooldown before a recently visited territory may be shown again. All times are in ticks.
 */
public class TitleAnimation {
    // Titles below this opacity are not worth drawing
    public static final int MIN_VISIBLE_OPACITY = 8;

    private int fadeInTime;
    private int displayTime;
    private int fadeOutTime;

    private int titleTimer = 0;
    private int cooldownTimer = 0;

    /**
     * Starts displaying a title. The cooldown starts immediately upon display.
     */
    public void start(int fadeInTime, int displayTime, int fadeOutTime, int cooldownTime) {
        this.fadeInTime = Math.max(0, fadeInTime);
        this.displayTime = Math.max(0, displayTime);
        this.fadeOutTime = Math.max(0, fadeOutTime);
        this.titleTimer = this.fadeInTime + this.displayTime + this.fadeOutTime;
        this.cooldownTimer = Math.max(0, cooldownTime);
    }

    /**
     * Updates title and cooldown timers
     *
     * @return true if the title finished displaying on this tick
     */
    public boolean tick() {
        // Tick down cooldown regardless of title display
        if (cooldownTimer > 0) {
            --cooldownTimer;
        }

        // Tick down title timer only if a title is active
        if (titleTimer > 0) {
            --titleTimer;
            return titleTimer == 0;
        }

        return false;
    }

    /**
     * Stops the current title. The cooldown is left running.
     */
    public void stop() {
        titleTimer = 0;
    }

    public boolean isActive() {
        return titleTimer > 0;
    }

    public boolean isOnCooldown() {
        return cooldownTimer > 0;
    }

    /**
     * Calculates the opacity (0-255) of the title
     *
     * @param partialTicks Partial tick time for smooth animations
     */
    public int getOpacity(float partialTicks) {
        if (titleTimer <= 0) return 0;

        // Calculate remaining time precisely
        float age = (float) titleTimer - partialTicks;
        float opacity = 255f;

        if (age > fadeOutTime + displayTime) {
            // Fade-in phase, fadeInTime is always > 0 here
            float fadeInProgress = (float) fadeInTime - (age - (displayTime + fadeOutTime));
            opacity = Math.clamp(fadeInProgress, 0f, (float) fadeInTime) * 255f / (float) fadeInTime;
        } else if (age <= fadeOutTime) {
            // Fade-out phase, age is the time remaining in the fade-out
            opacity = fadeOutTime > 0 ? Math.clamp(age, 0f, (float) fadeOutTime) * 255f / (float) fadeOutTime : 0f;
        }
        // Display phase: opacity remains 255

        return Math.clamp((int) opacity, 0, 255);
    }

    public boolean isVisible(float partialTicks) {
        return getOpacity(partialTicks) >= MIN_VISIBLE_OPACITY;
    }
}
