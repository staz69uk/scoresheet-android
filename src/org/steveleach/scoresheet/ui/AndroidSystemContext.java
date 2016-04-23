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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import org.steveleach.scoresheet.support.SystemContext;
import org.steveleach.ihscoresheet.R;

import java.io.File;

/**
 * Abstraction for system context, Android implementation.
 * <p>
 * This stuff is hard to test in isolation, so keep it to itself.
 *
 * @author Steve Leach
 */
public class AndroidSystemContext implements SystemContext {

    private final Context context;

    public AndroidSystemContext(Context context) {
        this.context = context;
    }

    public boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public File getScoresheetFolder() {
        File parent;
        try {
            parent = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        } catch (NoSuchFieldError e) {
            parent = new File(Environment.getExternalStorageDirectory(), "Documents");
        }
        return new File(parent, context.getString(R.string.scoresheetFolder));
    }

    @Override
    public String applicationVersion() {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "Unknown!";
        }
    }
}
