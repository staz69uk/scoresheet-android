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

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import org.steveleach.ihscoresheet.*;
import org.steveleach.scoresheet.model.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Implementation code for the new Game History UI fragment.
 *
 * @author Steve Leach
 */
public class HistoryFragment extends Fragment implements ModelAware {
    private ScoresheetActivity activity = null;
    private HistoryAdapter adapter = null;
    private ScoresheetModel model = new ScoresheetModel();
    private Button nextPeriodButton;
    private TextView title = null;
    private ListView eventList = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.historyfragment, container, false);

        activity = (ScoresheetActivity) getActivity();
        eventList = (ListView)view.findViewById(R.id.historyList2);
        adapter = new HistoryAdapter(getActivity());
        eventList.setAdapter(adapter);

        nextPeriodButton = (Button)view.findViewById(R.id.btnNextPeriod);
        title = (TextView)view.findViewById(R.id.txtHistoryTitle);

        registerForContextMenu(eventList);

//        eventList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
//                return handleListItemLongClick(adapterView, position, (int) id);
//            }
//        });

        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.historycontextenu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        boolean result = super.onContextItemSelected(item);

        int position = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;
        handleContextMenu(item.getItemId(), position);

        return result;
    }

    public void handleContextMenu(int selection, int position) {
        GameEvent selectedItem = (GameEvent) eventList.getItemAtPosition(position);
        switch (selection) {
            case R.id.historyMenuEdit:
                editEvent(selectedItem);
                break;
            case R.id.historyMenuDelete:
                askToDelete(selectedItem);
                break;
        }
    }

    private void editEvent(GameEvent event) {
        if (event instanceof GoalEvent) {
            GoalFragment fragment = new GoalFragment();
            fragment.setModel(model);
            fragment.setEventToEdit((GoalEvent)event);
            fragment.setTeam(event.getTeam());
            activity.showFragment(fragment);
        }
    }

    public void askToDelete(GameEvent event) {
        String text = getString(R.string.deleteEventPrompt, event.toString());
        ((ScoresheetActivity)getActivity()).yesNoDialog(text, new Runnable() {
            @Override
            public void run() {
                model.removeEvent(event);
                Toast.makeText(getActivity().getApplicationContext(), "Deleting '" + event.toString() + "'", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    private void refreshList() {
        if (title != null) {
            String titleText = getActivity().getString(R.string.gameHistoryTitle);
            if (model.isChanged()) {
                titleText += " *";
            }
            title.setText(titleText);
        }
        if (nextPeriodButton != null) {
            nextPeriodButton.setEnabled(model.getPeriod() <= model.getRules().getRegulationPeriods());
        }
        if (adapter != null) {
            adapter.setEvents(model.getEvents());
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setModel(ScoresheetModel model) {
        this.model = model;
        model.addListener(this);
        refreshList();
    }

    @Override
    public void onModelUpdated(ModelUpdate update) {
        refreshList();
    }

    class HistoryAdapter extends BaseAdapter {
        private final ScoresheetActivity activity;
        private final LayoutInflater inflater;
        private List<GameEvent> events = new LinkedList<>();

        public HistoryAdapter(Activity activity) {
            this.activity = (ScoresheetActivity) activity;
            this.inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setEvents(List<GameEvent> events) {
            this.events = events;
        }

        @Override
        public int getCount() {
            return events.size();
        }

        @Override
        public GameEvent getItem(int position) {
            return events.get((int)getItemId(position));
        }

        @Override
        public long getItemId(int position) {
            return events.size()-1-position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GameEvent event = getItem(position);

            View rowView = inflater.inflate(R.layout.historylistentry, null);
            ((TextView)rowView.findViewById(R.id.txtEventPeriod)).setText(periodText(event));
            ((TextView)rowView.findViewById(R.id.txtGameTime)).setText(event.getGameTime());
            ((TextView)rowView.findViewById(R.id.txtSummary)).setText(getSummary(event));
            ((TextView)rowView.findViewById(R.id.txtEventDetail)).setText(getDetail(event));
            ((TextView)rowView.findViewById(R.id.txtScore)).setText(scoreLine(event));

            int imageId;
            int color;
            int teamColor = event.getTeam().equals(model.getHomeTeam().getName()) ?
                    Color.parseColor("#cfd8ff") : Color.parseColor("#ffd8dc");
            if (event instanceof GoalEvent) {
                imageId = R.drawable.goal48a;
                color = teamColor;
            } else if (event instanceof PenaltyEvent) {
                imageId = R.drawable.penalty48a;
                color = teamColor;
            } else {
                imageId = R.drawable.period48;
                color = Color.parseColor("#cfd8dc");
            }

            ImageView image = (ImageView) rowView.findViewById(R.id.idImage);
            try {
                image.setImageResource(imageId);
            } catch (Resources.NotFoundException e) {
                Log.e(ScoresheetActivity.LOG_TAG,"Error loading drawable" + imageId, e);
            }
            image.setColorFilter(color);

            return rowView;
        }

        private String scoreLine(GameEvent event) {
            if (event instanceof GoalEvent) {
                HomeAway<Integer> score = model.scoreAt(event.getGameTime());
                return String.format("%d : %d", score.getHome(), score.getAway());
            } else {
                return "";
            }
        }

        private String periodText(GameEvent event) {
            if (event instanceof PeriodEndEvent) {
                return "";
            }
            String[] periods = getString(R.string.periodAbbrevs).split(",");
            return periods[event.getPeriod()-1];
        }

        private String getDetail(GameEvent event) {
            if (event instanceof GoalEvent) {
                GoalEvent goal = (GoalEvent) event;
                switch (goal.assists()) {
                    case 2:
                        return activity.getString(R.string.historyListGoalDetail2a, goal.getPlayer(), goal.getAssist1(), goal.getAssist2(), goal.getSubType());
                    case 1:
                        return activity.getString(R.string.historyListGoalDetail1a, goal.getPlayer(), goal.getAssist1(), goal.getSubType());
                    default:
                        return activity.getString(R.string.historyListGoalDetail, goal.getPlayer(), goal.getSubType());
                }
            } else if (event instanceof PenaltyEvent) {
                PenaltyEvent penalty = (PenaltyEvent) event;
                String player = activity.playerNum(penalty);
                return activity.getString(R.string.historyListPenaltyDetail, player, ""+penalty.getMinutes(), penalty.getSubType());
            } else {
                return "";
            }
        }

        private String getSummary(GameEvent event) {
            String summary = "?";
            if (event instanceof GoalEvent) {
                summary = activity.getString(R.string.historyListGoal, event.getTeam());
            } else if (event instanceof PenaltyEvent) {
                summary = activity.getString(R.string.historyListPenalty, event.getTeam());
            } else if (event instanceof PeriodEndEvent) {
                summary = activity.getString(R.string.historyListPeriod, ""+event.getPeriod());
            }
            return summary;
        }
    }
}
