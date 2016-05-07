/*  Copyright 2016 Steve Leach

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package org.steveleach.scoresheet.ui;

import android.app.Fragment;
import android.content.res.Resources;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import org.steveleach.scoresheet.model.ModelAware;
import org.steveleach.scoresheet.model.ModelUpdate;
import org.steveleach.scoresheet.model.ScoresheetModel;

/**
 * Abstract base class for Fragments in the Scoresheet app.
 * @author Steve Leach
 */
public abstract class ScoresheetFragment extends Fragment implements ModelAware {
    protected ScoresheetModel model = null;

    @Override
    public void setModel(ScoresheetModel model) {
        this.model = model;
        model.addListener(this);
    }

    @Override
    public void onModelUpdated(ModelUpdate update) {
        Log.i(ScoresheetActivity.LOG_TAG, "ScoresheetFragment ignoring model update");
    }

    protected int contextMenuId() {
        return 0;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        //super.onCreateContextMenu(menu, v, menuInfo);
        if (contextMenuId() > 0) {
            getActivity().getMenuInflater().inflate(contextMenuId(), menu);
        }
    }

    protected boolean handleContextMenu(int menuId, int listPosition) {
        return true;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return handleContextMenu(item.getItemId(), ScoresheetActivity.listContextMenuPosition(item));
    }

    public static void setViewImage(ImageView view, int imageId) {
        try {
            view.setImageResource(imageId);
        } catch (Resources.NotFoundException e) {
            Log.e(ScoresheetActivity.LOG_TAG, "Error loading drawable" + imageId, e);
        }
    }
}
