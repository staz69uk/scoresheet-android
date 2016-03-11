package org.steveleach.scoresheet.io;

import org.json.JSONException;
import org.steveleach.scoresheet.SystemContext;
import org.steveleach.scoresheet.model.ScoresheetModel;

import java.io.File;
import java.io.IOException;

/**
 * Created by steve on 11/03/16.
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
        String result = "Unknown";
        File file = fileManager.getLastFile(baseFileName);
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
}
