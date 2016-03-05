package com.example.myapp3;

/**
 * Created by steve on 05/03/16.
 */
public class GameClock {

    private int period = 1;

    public GameClock(int period) {
        this.period = period;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    /**
     * Converts clock time (counting down within period) to game time (counting up from start of game).
     *
     * @param clockTime time left in period in format "MMSS"
     * @return time played in game in format "MM:SS"
     */
    public String gameTimeFromClock(final String clockTime) {
        if (clockTime.length() != 4) {
            throw new IllegalArgumentException("Clock time must be 4 digits");
        }
        int mins = Integer.parseInt(clockTime.substring(0,2));
        int secs = Integer.parseInt(clockTime.substring(2,4));
        int remainingSecs = mins * 60 + secs;
        int totalSecsPlayed = 20 * 60 - remainingSecs + ((period-1)*20*60);
        int minsPlayed = totalSecsPlayed / 60;
        int secsPlayed = totalSecsPlayed % 60;
        return String.format("%02d:%02d",minsPlayed,secsPlayed);
    }
}
