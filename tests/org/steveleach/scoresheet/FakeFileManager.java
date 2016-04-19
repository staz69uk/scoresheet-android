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
package org.steveleach.scoresheet;

import org.steveleach.scoresheet.support.FileManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * A "fake" FileSystem implementation for testing purposes.
 *
 * The fake filesystem is backed by a hashmap of pathnames to file contents (strings).
 * It therefore only really supports text files currently.
 *
 * @author Steve Leach
 */
public class FakeFileManager extends FileManager {
    public Map<String,String> files = new HashMap<>();

    @Override
    public void writeTextFile(File file, String text) throws IOException {
        files.put(file.getAbsolutePath(), text);
    }

    @Override
    public String readTextFileContent(File file) throws IOException {
        return files.get(file.getAbsolutePath());
    }

    @Override
    public void copyFile(File src, File dst) throws IOException {
        String content = files.get(src.getAbsolutePath());
        files.put(dst.getAbsolutePath(), content);
    }

    public int fileCount() {
        return files.size();
    }

    public String getContentOf(String fileName) throws FileNotFoundException {
        for (String fullName : files.keySet()) {
            if (fullName.endsWith("/"+fileName)) {
                return files.get(fullName);
            }
        }
        throw new FileNotFoundException("Not found: " + fileName);
    }

    @Override
    public List<File> dirContents(File dir) {
        List<File> results = new LinkedList<>();
        for (String fileName : files.keySet()) {
            File f = new File(fileName);
            if (f.getParentFile().equals(dir)) {
                results.add(f);
            }
        }
        return results;
    }

    @Override
    public boolean exists(File file) {
        return files.containsKey(file.getAbsolutePath());
    }
}
