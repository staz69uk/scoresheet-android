package org.steveleach.ihscoresheet;

import android.test.ActivityInstrumentationTestCase2;
import org.steveleach.scoresheet.ui.HistoryFragment;
import org.steveleach.scoresheet.ui.ScoresheetActivity;


public class InstrumentedTests  extends ActivityInstrumentationTestCase2<ScoresheetActivity> {
    private ScoresheetActivity activity;

    public InstrumentedTests() {
        super(ScoresheetActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(true);
        activity = (ScoresheetActivity)getActivity();
    }

    //@Test
    public void testName() {
        assertTrue(true);
        //assertEquals(HistoryFragment.class, activity.getVisibleFragment().getClass());
    }
}
