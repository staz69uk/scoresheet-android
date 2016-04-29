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
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import org.steveleach.ihscoresheet.R;
import org.steveleach.scoresheet.model.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Fragment code for team Players editor.
 *
 * @author Steve Leach
 */
public class PlayersFragment extends Fragment implements ModelAware {

    private ScoresheetModel model;
    private View view;
    private ListView list;
    private String team = null;
    private TextView title;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.playersfragment, container, false);

        title = (TextView)view.findViewById(R.id.playersHeader);

        list = (ListView) view.findViewById(R.id.playersList);
        list.setAdapter(new PlayersAdapter(model.getHomeTeam()));

        view.findViewById(R.id.btnAddPlayer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlayer();
            }
        });

        return view;
    }

    private void addPlayer() {
        model.getHomeTeam().addPlayer(0,"");
        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        model.getHomeTeam().addPlayer(41,"S Leach");
        refresh();
    }

    public void setTeam(String teamName) {
        team = teamName;
    }

    private void refresh() {
        title.setText(getString(R.string.playersHeader,team));
        ((PlayersAdapter)list.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void setModel(ScoresheetModel model) {
        this.model = model;
    }

    @Override
    public void onModelUpdated(ModelUpdate update) {}

    class PlayersAdapter extends BaseAdapter {

        private Team team = null;
        private LayoutInflater inflater;

        public PlayersAdapter(Team team) {
            this.team = team;
            inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return team.getPlayers().size();
        }

        @Override
        public Player getItem(int position) {
            List<Player> players = new LinkedList<>(team.getPlayers().values());

            return players.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = inflater.inflate(R.layout.playerlistentry, null);
            Player player = getItem(position);
            rowView.setTag(1,player);
            setField(rowView,R.id.fldPlayerNum,Integer.toString(player.getNumber()));
            setField(rowView,R.id.fldPlayerName,player.getName());
            ((Switch)rowView.findViewById(R.id.fldPlayerActive)).setChecked(player.isPlaying());
            return rowView;
        }

        private void setField(View rowView, int fieldId, String value) {
            TextView field = (TextView)rowView.findViewById(fieldId);
            field.setText(value);
            field.setTag(1,view.getTag(1));
            field.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (! hasFocus) {
                        Log.d("Staz","Updating row " + v.getTag(1));
                    }
                }
            });
        }
    }
}
