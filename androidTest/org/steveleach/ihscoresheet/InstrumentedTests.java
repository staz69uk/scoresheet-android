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
package org.steveleach.ihscoresheet;

import android.app.Fragment;
import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import org.steveleach.scoresheet.ui.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Instrumented UI tests for Scoresheet.
 */
public class InstrumentedTests  extends ActivityInstrumentationTestCase2<ScoresheetActivity> {
    private static final int DEFAULT_PAUSE_TIME = 800;
    private ScoresheetActivity activity;
    private Instrumentation instrumentation;
    private static final int FIFTY_MS = 50;

    public InstrumentedTests() {
        super(ScoresheetActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(true);
        activity = getActivity();
        instrumentation = getInstrumentation();
        assertNotNull(activity);
    }

    public void testUIFunctionality() throws Throwable {
        assertNotNull(activity.getModel());
        assertEquals(0, activity.getModel().getEvents().size());

        assertState(visibleFragment(HistoryFragment.class));
        assertState(listSize(R.id.historyList2, 0));

        send(click(R.id.menuRefresh));

        pause();

        send(menuSelection(R.id.menuHelp));
        waitFor(visibleFragment(HelpFragment.class), FIFTY_MS);

        pause();

        send(click(R.id.btnCloseHelp));
        waitFor(visibleFragment(HistoryFragment.class), FIFTY_MS);

        send(click(R.id.btnAwayGoal));
        waitFor(visibleFragment(GoalFragment.class), FIFTY_MS);

        send(keys("1525"));
        send(selectField(R.id.fldGoalType));
        send(keys("PP"));
        send(selectField(R.id.fldScoredBy));
        send(keys("41"));
        send(selectField(R.id.fldAssist1));
        send(keys("93"));
        send(selectField(R.id.fldAssist2));
        send(keys("28"));

        pause(1500);

        send(click(R.id.btnDone));
        waitFor(visibleFragment(HistoryFragment.class), FIFTY_MS);
        assertState(listSize(R.id.historyList2, 1));

        send(click(R.id.btnNextPeriod));

        waitFor(listSize(R.id.historyList2, 2), FIFTY_MS);

        pause(1500);

        send(click(R.id.btnAwayPen));
        waitFor(visibleFragment(PenaltyFragment.class), FIFTY_MS);

        send(selectField(R.id.fldPenaltyClock));
        send(keys("1843"));
        send(selectField(R.id.fldPenaltyCode));
        send(keys("HOOK"));
        send(selectField(R.id.fldPenaltyPlayer));
        send(keys("50"));

        pause(1500);

        send(click(R.id.btnPenaltyDone));
        waitFor(visibleFragment(HistoryFragment.class), FIFTY_MS);
        assertState(listSize(R.id.historyList2, 3));

        pause(1500);

        //============

        send(click(R.id.btnReport));
        waitFor(visibleFragment(ReportFragment.class), FIFTY_MS);

        pause(2000);

        send(click(R.id.btnCloseReport));
        waitFor(visibleFragment(HistoryFragment.class), FIFTY_MS);

        pause(3000);
    }

    private void pause() throws InterruptedException {
        pause(DEFAULT_PAUSE_TIME);
    }

    private void pause(int timeInMS) throws InterruptedException {
        instrumentation.waitForIdleSync();
        Thread.sleep(timeInMS);
    }

    private void send(UICommand command) throws Throwable {
        instrumentation.waitForIdleSync();
        if (command.isSync()) {
            command.run();
        } else {
            runTestOnUiThread(command);
        }
    }

    private void waitFor(ViewState viewState, long timeOutInMS) throws InterruptedException, TimeoutException {
        long start = System.currentTimeMillis();
        // Don't even bother checking until the UI thread is idle
        instrumentation.waitForIdleSync();
        while (!viewState.isInState()) {
            if ((System.currentTimeMillis() - start) > timeOutInMS) {
                throw new TimeoutException(String.format("Timeout out after %dms waiting for %s",timeOutInMS, viewState.expectedState()));
            }
            Thread.sleep(50);
        }
    }

    private void assertState(ViewState state) {
        if (! state.isInState()) {
            throw new AssertionError("State not matched: " + state.expectedState());
        }
    }

    interface ViewState {
        boolean isInState();
        String expectedState();
    }

    interface UICommand extends Runnable {
        boolean isSync();
    }

    private ViewState listSize(final int listId, final int listSize) {
        final ListView list = (ListView) activity.findViewById(listId);
        return new ViewState() {
            @Override
            public boolean isInState() {
                return list.getAdapter().getCount() == listSize;
            }

            @Override
            public String expectedState() {
                return String.format("List %d having %d elements",listId,listSize);
            }
        };
    }

    private ViewState visibleFragment(Class<? extends Fragment> fragmentClass) {
        return new ViewState() {
            @Override
            public boolean isInState() {
                return activity.getVisibleFragment().getClass().equals(fragmentClass);
            }

            @Override
            public String expectedState() {
                return "Visible fragment is a " + fragmentClass.getName();
            }
        };
    }

    private UICommand click(int id) {
        final View view = activity.findViewById(id);
        if (view == null) {
            throw popStackTrace(new RuntimeException("Could not locate view " + id));
        }
        return new UICommand() {
            @Override
            public boolean isSync() {
                return false;
            }

            @Override
            public void run() {
                view.performClick();
            }
        };
    }

    private UICommand selectField(int id) {
        final EditText field = (EditText) activity.findViewById(id);
        if (field == null) {
            throw popStackTrace(new RuntimeException("Could not locate field " + id));
        }
        return new UICommand() {
            @Override
            public boolean isSync() {
                return false;
            }

            @Override
            public void run() {
                field.requestFocus();
            }
        };
    }


    private UICommand keys(final String text) {
        return new UICommand() {
            @Override
            public boolean isSync() {
                return true;
            }

            @Override
            public void run() {
                instrumentation.sendStringSync(text);
            }
        };
    }

    private UICommand menuSelection(int menuOptionId) {
        return new UICommand() {
            @Override
            public boolean isSync() {
                return true;
            }

            @Override
            public void run() {
                activity.openOptionsMenu();
                instrumentation.invokeMenuActionSync(activity,menuOptionId,0);
            }
        };
    }

    private RuntimeException popStackTrace(RuntimeException t) {
        List<StackTraceElement> newTrace = new ArrayList<StackTraceElement>();
        for (int n = 1; n < t.getStackTrace().length; n++) {
            newTrace.add(t.getStackTrace()[n]);
        }
        t.setStackTrace(newTrace.toArray(new StackTraceElement[0]));
        return t;
    }
}
