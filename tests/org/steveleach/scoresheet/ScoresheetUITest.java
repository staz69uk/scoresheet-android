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

import android.app.Activity;
import android.os.Environment;
import android.widget.EditText;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowEnvironment;
import org.steveleach.scoresheet.model.GoalEvent;
import org.steveleach.scoresheet.model.PenaltyEvent;
import org.steveleach.scoresheet.model.ScoresheetModel;
import org.steveleach.scoresheet.ui.GoalFragment;
import org.steveleach.scoresheet.ui.HistoryFragment;
import org.steveleach.scoresheet.ui.PenaltyFragment;
import org.steveleach.scoresheet.ui.ScoresheetActivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for UI methods.
 *
 * @author Steve Leach
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 18)
public class ScoresheetUITest {

    @Test
    public void basicIntegrationTest() {
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);

        // Create the UI
        ScoresheetActivity activity = Robolectric.setupActivity(ScoresheetActivity.class);
        assertNotNull(activity);

        // Get access to the underlying data model and validate its initial state
        ScoresheetModel model = activity.getModel();
        assertEquals(0, model.getEvents().size());
        assertEquals(1, model.getPeriod());
        assertEquals(0, model.getHomeGoals());

        // Make sure the history fragment is shown by default
        activity.showDefaultFragment();
        assertTrue( "History fragment should be visible", activity.getVisibleFragment() instanceof HistoryFragment );

        // Click the "new period" button and validate the effects on the model
        activity.periodButtonClicked(null);
        assertEquals(1, model.getEvents().size());
        assertEquals(2, model.getPeriod());

        // Click the "home goal" button and add goal details
        activity.findViewById(R.id.btnHomeGoal).performClick();
        assertTrue( "Goal fragment should be visible", activity.getVisibleFragment() instanceof GoalFragment );

        setField(activity,R.id.fldClock,"1234");
        setField(activity,R.id.fldScoredBy,"41");
        activity.findViewById(R.id.btnDone).performClick();

        // After adding the goal, validate the UI and model state
        assertTrue( "History fragment should be visible", activity.getVisibleFragment() instanceof HistoryFragment );
        assertEquals(2, model.getEvents().size());

        GoalEvent event = (GoalEvent) model.getEvents().get(1);
        assertEquals(2, event.getPeriod());
        assertEquals("27:26", event.getGameTime());
        assertEquals("41", event.getPlayer());
        assertEquals(model.getHomeTeam().getName(), event.getTeam());
        assertEquals(1, model.getHomeGoals());
        assertEquals(0, model.getAwayGoals());

        // Click the "away penalty" button and add penalty details
        activity.findViewById(R.id.btnAwayPen).performClick();
        assertTrue( "Penalty fragment should be visible", activity.getVisibleFragment() instanceof PenaltyFragment);

        setField(activity,R.id.fldPenaltyClock,"430");
        setField(activity,R.id.fldPenaltyPlayer,"25");
        setField(activity,R.id.fldPenaltyMins,"5");
        setField(activity,R.id.fldPenaltyCode,"FIGHT");
        activity.findViewById(R.id.btnPenaltyDone).performClick();

        // After adding the penalty, validate the UI and model state
        assertTrue( "History fragment should be visible", activity.getVisibleFragment() instanceof HistoryFragment );
        assertEquals(3, model.getEvents().size());

        PenaltyEvent penalty = (PenaltyEvent) model.getEvents().get(2);
        assertEquals(2, penalty.getPeriod());
        assertEquals("35:30", penalty.getGameTime());
        assertEquals("25", penalty.getPlayer());
        assertEquals(model.getAwayTeam().getName(), penalty.getTeam());

    }

    private void setField(Activity activity, int fieldId, String fieldValue) {
        ((EditText)activity.findViewById(fieldId)).setText(fieldValue);
    }
}
