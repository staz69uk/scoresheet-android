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
package org.steveleach.scoresheet.io;

import org.json.JSONException;
import org.steveleach.scoresheet.model.ScoresheetModel;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Data store for Ice Hockey scoresheet files on Android.
 *
 * @author Steve Leach
 */
public class AndroidScoresheetStore {

    private final FileManager fileManager;
    private final JsonCodec codec;
    private final SystemContext system;
    private String baseFileName = "gamedata";

    public AndroidScoresheetStore(FileManager fileManager, JsonCodec jsonCodec, SystemContext system) {
        this.fileManager = fileManager;
        this.system = system;
        this.codec = jsonCodec;

        fileManager.setBaseDirectory(system.getScoresheetFolder());
    }

    private void checkFileSystemStatus() throws IOException {
        if (!system.isExternalStorageAvailable()) {
            throw new IOException("External storage not mounted for writing");
        }
    }

    public String save(ScoresheetModel model) {
        String result = "Unknown";
        String json = null;
        try {
            json = codec.toJson(model);
        } catch (JSONException e) {
            result = "Error building JSON : " + e.getMessage();
        }

        if (json != null) {
            try {
                checkFileSystemStatus();
                fileManager.ensureBaseDirectoryExists();
                File file = fileManager.getMainFile(baseFileName);
                fileManager.writeTextFile(file, json);
                fileManager.copyFile(file, fileManager.getLastFile(baseFileName));
                result = "Saved " + baseFileName;
            } catch (IOException e) {
                result = "Error saving " + baseFileName + " : " + e.getMessage();
            }
        }
        return result;
    }

    public String loadInto(ScoresheetModel model) {
        File file = fileManager.getLastFile(baseFileName);
        return loadInto(model, file);
    }

    private String loadInto(ScoresheetModel model, File file) {
        String result = "Unknown";
        if (file.exists()) {
            String json = null;
            try {
                json = fileManager.readTextFileContent(file);
            } catch (IOException e) {
                result = "Error reading game data: " + e.getMessage();
            }
            if (json != null) {
                try {
                    new JsonCodec().fromJson(model,json);
                    result = "Loaded game data";
                } catch (JSONException e) {
                    result = "Error parsing game data : " + e.getMessage();
                }
            }
        } else {
            result = "No exported data found";
        }
        return result;
    }


    public List<File> savedFiles() {
        List<File> files = new LinkedList<>();
        for (File file : system.getScoresheetFolder().listFiles()) {
            if (file.isFile()) {
                if (file.getName().startsWith(baseFileName)) {
                    files.add(file);
                }
            }
        }
        return files;
    }

    public String loadInto(ScoresheetModel model, String fileName) {
        File file = new File(system.getScoresheetFolder(), fileName);
        return loadInto(model, file);
    }
}
