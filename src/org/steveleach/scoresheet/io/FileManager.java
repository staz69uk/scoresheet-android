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

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * File System helper.
 *
 * Created by steve on 11/03/16.
 */
public class FileManager {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private File baseDir = new File(".");

    public void writeTextFile(File file, String text) throws IOException {
        FileWriter writer = new FileWriter(file);
        try {
            writer.write(text);
        } finally {
            writer.close();
        }
    }

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
        return sb.toString();
    }

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

    public void ensureBaseDirectoryExists() {
        baseDir.mkdirs();
    }

}
