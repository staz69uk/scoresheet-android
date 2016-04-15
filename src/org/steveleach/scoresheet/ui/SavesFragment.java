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
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import org.steveleach.ihscoresheet.R;
import org.steveleach.scoresheet.model.ModelUpdate;
import org.steveleach.scoresheet.support.ScoresheetStore;
import org.steveleach.scoresheet.model.ScoresheetModel;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation code for the List Saved Games UI fragment.
 *
 * @author Steve Leach
 */
public class SavesFragment extends Fragment {

    private ScoresheetModel model = null;
    private ArrayAdapter<String> adapter = null;
    private ScoresheetStore store = null;
    private ListView savesList = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.savesfragment, container, false);

        savesList = (ListView) view.findViewById(R.id.gameSavesList);

        adapter = new ArrayAdapter<String>(getActivity(), R.layout.basiclistentry, new LinkedList<>());
        savesList.setAdapter(adapter);

        savesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                askToLoad((String) parent.getItemAtPosition(position));
            }
        });

        registerForContextMenu(savesList);

        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.filescontextmenu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        boolean result = super.onContextItemSelected(item);
        int position = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;
        String selectedItem = (String) savesList.getItemAtPosition(position);
        if (item.getItemId() == R.id.fileMenuOpen) {
            loadSavedData(selectedItem);
        } else if (item.getItemId() == R.id.fileMenuDelete) {
            askToDelete(selectedItem);
        } else {
            ((ScoresheetActivity) getActivity()).toast("Not yet implemented: " + item.getTitle() + " " + selectedItem);
        }

        return result;
    }

    private void askToDelete(String item) {
        ScoresheetActivity activity = (ScoresheetActivity) getActivity();
        activity.yesNoDialog("Delete " + item + "?", new Runnable() {
            @Override
            public void run() {
                ScoresheetStore.StoreResult result = store.delete(item);
                activity.toast(result.text);
                if (result.success) {
                    refreshList();
                }
            }
        });
    }

    public boolean askToLoad(String item) {
        ((ScoresheetActivity)getActivity()).yesNoDialog("Load '" + item + "'?", new Runnable() {
            @Override
            public void run() {
                loadSavedData(item);
            }
        });
        return false;
    }

    private void loadSavedData(String item) {
        ScoresheetStore.StoreResult result = store.loadInto(model, item);
        model.setChanged(false);
        model.notifyListeners(new ModelUpdate("Model loaded"));
        if (result.success) {
            Toast.makeText(getActivity().getApplicationContext(), "Loaded " + item, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Error " + result.text, Toast.LENGTH_LONG).show();
        }
        ((DefaultFragmentActivity)getActivity()).showDefaultFragment();
    }

    public void configure(ScoresheetModel model, ScoresheetStore store) {
        this.model = model;
        this.store = store;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    private void refreshList() {
        if (adapter != null) {
            adapter.clear();

            List<File> files = store.savedFiles();
            sortFilesByNameDescending(files);

            for (File file : files) {
                adapter.add(file.getName());
            }

            adapter.notifyDataSetChanged();
        }
    }

    private void sortFilesByNameDescending(List<File> files) {
        // Sort by name, descending order to get most recent at the top
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return f2.getName().compareTo(f1.getName());
            }
        });
    }
}
