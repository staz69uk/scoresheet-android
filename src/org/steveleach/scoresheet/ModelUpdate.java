package org.steveleach.scoresheet;

/**
 * Created by steve on 02/03/16.
 */
public class ModelUpdate {
    private String summary = null;

    public ModelUpdate(String summary) {
        this.summary = summary;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
