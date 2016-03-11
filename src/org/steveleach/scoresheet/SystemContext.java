package org.steveleach.scoresheet;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by steve on 11/03/16.
 */
public class SystemContext {

    private final Context context;

    public SystemContext(Context context) {
        this.context = context;
    }

    public boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public File getScoresheetFolder() {
        File parent = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        return new File(parent, context.getString(R.string.scoresheetFolder));
    }
}
