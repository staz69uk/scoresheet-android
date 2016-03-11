package com.example.myapp3;

import org.steveleach.scoresheet.ScoresheetModel;

/**
 * Created by steve on 02/03/16.
 */
public interface ModelAware {
    void setModel(ScoresheetModel model);
    void onModelUpdated(ModelUpdate update);
}
