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
package org.steveleach.scoresheet;

import android.app.AlertDialog;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import org.jetbrains.annotations.NotNull;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowEnvironment;
import org.steveleach.scoresheet.model.ScoresheetModel;
import org.steveleach.scoresheet.ui.ScoresheetActivity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Abstract superclass for Scoresheet UI tests.
 *
 * @author Steve Leach
 */
public abstract class AbstractUITest {

    protected ScoresheetActivity activity = null;
    protected ScoresheetModel model;
    protected FakeFileManager fakeFileManager;


    protected void setup() {
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);

        // Create the UI
        activity = Robolectric.setupActivity(ScoresheetActivity.class);

        fakeFileManager =  new FakeFileManager();
        activity.setFileManager(fakeFileManager);

        // Get access to the underlying data model and validate its initial state
        model = activity.getModel();
    }

    protected void clickDialogButton(int buttonID) {
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        Button button = dialog.getButton(buttonID);
        assertNotNull(button);
        button.performClick();
        assertFalse(dialog.isShowing());
    }

    protected void clickMenuItem(int optionId) {
        activity.onOptionsItemSelected(new RoboMenuItem(optionId));
    }

    @NotNull
    protected AlertDialog verifyAlertDialogShowing(String expectedMessage) {
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(dialog);
        assertTrue(dialog.isShowing());
        ShadowAlertDialog shadow = Shadows.shadowOf(dialog);
        assertNotNull(shadow.getMessage());
        assertTrue("Dialog text: " + expectedMessage, shadow.getMessage().toString().contains(expectedMessage));
        return dialog;
    }

    protected void click(int id) {
        activity.findViewById(id).performClick();
    }

    protected Class<?> visibleFragmentClass() {
        return activity.getVisibleFragment().getClass();
    }

    protected void setField(int fieldId, String fieldValue) {
        ((EditText)activity.findViewById(fieldId)).setText(fieldValue);
    }
}
