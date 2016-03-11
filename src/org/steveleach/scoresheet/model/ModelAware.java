package org.steveleach.scoresheet.model;

/**
 * Created by steve on 02/03/16.
 */
public interface ModelAware {
    void setModel(ScoresheetModel model);
    void onModelUpdated(ModelUpdate update);
}
