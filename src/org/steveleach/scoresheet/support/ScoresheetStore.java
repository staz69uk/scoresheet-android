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
package org.steveleach.scoresheet.support;

import org.json.JSONException;
import org.steveleach.scoresheet.model.ScoresheetModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Data store for Ice Hockey scoresheet files.
 *
 * @author Steve Leach
 */
public class ScoresheetStore {

    private final FileManager fileManager;
    private final JsonCodec codec;
    private final SystemContext system;
    private File baseDir = new File(".");
    private String baseFileName = "gamedata";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    public ScoresheetStore(FileManager fileManager, JsonCodec jsonCodec, SystemContext system) {
        this.fileManager = fileManager;
        this.system = system;
        this.codec = jsonCodec;

        setBaseDirectory(system.getScoresheetFolder());
    }

    public StoreResult delete(String fileName) {
        File file = new File(baseDir,fileName);

        boolean success = fileManager.delete(file);

        return new StoreResult(success ? "Deleted " + fileName : "Could not delete " + fileName, success);
    }

    public class StoreResult {
        public String text = "Unknown";
        public boolean success = false;
        public Throwable error = null;

        public StoreResult(String text, boolean success) {
            this.text = text;
            this.success = success;
        }

        public StoreResult(String text, Throwable error) {
            this.text = text;
            this.error = error;
            this.success = false;
        }
    }

    private void checkFileSystemStatus() throws IOException {
        if (!system.isExternalStorageAvailable()) {
            throw new IOException("External storage not mounted for writing");
        }
    }

    public StoreResult save(ScoresheetModel model) {
        StoreResult result = null;
        String json = null;
        try {
            json = codec.toJson(model);
        } catch (JSONException e) {
            result = new StoreResult("Error building JSON : " + e.getMessage(), e);
        }

        if (json != null) {
            try {
                checkFileSystemStatus();
                ensureBaseDirectoryExists();
                File file = getMainFile(baseFileName);
                fileManager.writeTextFile(file, json);
                fileManager.copyFile(file, getLastFile(baseFileName));
                result = new StoreResult("Saved " + baseFileName, true);
            } catch (IOException e) {
                result = new StoreResult("Error saving " + baseFileName + " : " + e.getMessage(), e);
            }
        }
        return result;
    }

    private StoreResult loadInto(ScoresheetModel model, File file) {
        StoreResult result = null;
        if (fileManager.exists(file)) {
            String json = null;
            try {
                json = fileManager.readTextFileContent(file);
            } catch (IOException e) {
                result = new StoreResult("Error reading game data: " + e.getMessage(), e);
            }
            if (json != null) {
                try {
                    codec.fromJson(model,json);
                    result = new StoreResult("Loaded game data", true);
                } catch (JSONException e) {
                    result = new StoreResult("Error parsing game data : " + e.getMessage(), e);
                }
            }
        } else {
            result = new StoreResult("No exported data found", false);
        }
        return result;
    }


    public List<File> savedFiles() {
        List<File> files = new LinkedList<>();
        for (File file : fileManager.dirContents(baseDir)) {
            if (file.isFile()) {
                if (file.getName().startsWith(baseFileName)) {
                    files.add(file);
                }
            }
        }
        return files;
    }

    public StoreResult loadInto(ScoresheetModel model, String fileName) {
        File file = new File(system.getScoresheetFolder(), fileName);
        return loadInto(model, file);
    }

    public void ensureBaseDirectoryExists() {
        fileManager.ensureDirectoryExists(baseDir);
    }

    public void setBaseDirectory(File baseDir) {
        this.baseDir = baseDir;
    }

    public File getMainFile(String baseName) {
        String dateStr = DATE_FORMAT.format(new Date());
        String mainFileName = String.format("%s-%s.json", baseName, dateStr);
        return new File(baseDir,mainFileName);
    }

    public File getLastFile(String baseName) {
        return new File(baseDir,baseName+".json");
    }

}
