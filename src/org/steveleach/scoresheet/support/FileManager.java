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

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * File System helper.
 * <p>
 * Application code should interact with the FileManager rather than directly, as it can
 * be mocked for unit testing.
 *
 * @author Steve Leach
 */
public class FileManager {

    /**
     * Writes text to a file.
     */
    public void writeTextFile(File file, String text) throws IOException {
        FileWriter writer = new FileWriter(file);
        try {
            writer.write(text);
        } finally {
            writer.close();
        }
    }

    /**
     * Reads and returns the contents of a text file.
     */
    public String readTextFileContent(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } finally {
            reader.close();
        }
        return sb.toString().trim();
    }

    /**
     * Returns a directory that can be used for storing temporary files.
     */
    public File tempDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }

    /**
     * Copies a file.
     */
    public void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        try {
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            in.close();
            out.close();
        }
    }

    /**
     * Deletes a file.
     */
    public boolean delete(File file) {
        return file.delete();
    }

    /**
     * Creates a directory if it does not exist, including all parent directories as necessary.
     */
    public void ensureDirectoryExists(File dir) {
        dir.mkdirs();
    }

    /**
     * Returns true if the specified file exists.
     */
    public boolean exists(File file) {
        return file.exists();
    }

    /**
     * Returns a list of the contents of a directory.
     */
    public List<File> dirContents(File dir) {
        if ((dir == null) || (dir.listFiles() == null)) {
            return new ArrayList<>();
        } else {
            return Arrays.asList(dir.listFiles());
        }
    }

    /**
     * Renames a file.
     */
    public boolean rename(File oldFile, File newFile) {
        return oldFile.renameTo(newFile);
    }
}
