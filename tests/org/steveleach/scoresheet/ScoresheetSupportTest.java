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

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.steveleach.scoresheet.android.AndroidSystemContext;
import org.steveleach.scoresheet.support.ScoresheetStore;
import org.steveleach.scoresheet.support.FileManager;
import org.steveleach.scoresheet.support.JsonCodec;
import org.steveleach.scoresheet.model.*;
import org.steveleach.scoresheet.support.WeakSet;

import java.io.File;
import java.io.IOException;

import static org.steveleach.scoresheet.FastTestSuite.*;

/**
 * Unit tests for the Ice Hockey Scoresheet app.
 * <p>
 * Most of the unit tests are focussed on the model package.
 *
 * @author Steve Leach
 */
@RunWith(MockitoJUnitRunner.class)
public class ScoresheetSupportTest {

    private ScoresheetModel model = null;

    @Mock
    FileManager fileManager;

    @Mock
    JsonCodec jsonCodec;

    @Mock
    AndroidSystemContext context;

    @Before
    public void setup() {
        model = new ScoresheetModel();
    }

    @Test
    public void validateScoresheetStore() throws JSONException {
        when(context.isExternalStorageAvailable()).thenReturn(true);
        when(context.getScoresheetFolder()).thenReturn(new File("."));
        when(jsonCodec.toJson(anyObject())).thenReturn("{a}");

        ScoresheetStore store = new ScoresheetStore(fileManager,jsonCodec,context);

        ScoresheetStore.StoreResult status = store.save(model);

        assertEquals("Saved gamedata", status.text);
    }

    @Test
    public void testJsonRoundTrip() throws JSONException {
        JsonCodec codec = new JsonCodec();
        ScoresheetModel model1 = new ScoresheetModel();
        model1.setAwayTeam(new Team("Badguys"));
        addTestEvents(model1);

        assertNotEquals(0, model1.getEvents().size());

        String json = codec.toJson(model1);
        assertNotNull(json);
        assertTrue(json.startsWith("{"));
        assertTrue(json.contains("04:45"));
        assertTrue(json.contains("Hook"));
        assertTrue(json.endsWith("}"));

        ScoresheetModel model2 = new ScoresheetModel();
        codec.fromJson(model2, json);

        assertEquals(model1.getEvents().size(), model2.getEvents().size());
        assertEquals(model1.getAwayGoals(), model2.getAwayGoals());
        assertEquals(model1.getAwayTeam().getName(), model2.getAwayTeam().getName());
    }

    @Test
    public void verifyBasicWeakListFunctionality() {
        WeakSet<String> list = new WeakSet<>();
        String bob = "Bob";
        list.add(bob);
        assertTrue(list.containsItem(bob));
        assertFalse(list.containsItem("Fred"));
    }

    @Test
    public void verifyBasicFileManagerFunctionality() throws IOException {
        final String testText = "Testing";
        FileManager fileManager = new FileManager();
        File tempFile = new File(fileManager.tempDir(),"temp.txt");
        fileManager.writeTextFile(tempFile, testText);
        String content = fileManager.readTextFileContent(tempFile);
        assertEquals(testText,content);

        File tempFile2 = new File(fileManager.tempDir(),"temp2.txt");
        fileManager.copyFile(tempFile,tempFile2);
        String content2 = fileManager.readTextFileContent(tempFile2);
        assertEquals(testText,content2);

        fileManager.delete(tempFile);
        fileManager.delete(tempFile2);

        assertTrue(fileManager.exists(fileManager.tempDir()));
        fileManager.ensureDirectoryExists(fileManager.tempDir());
        assertNotNull(fileManager.dirContents(fileManager.tempDir()));
    }

    @Test
    public void verifyModelSaveAndLoad() throws IOException {
        when(fileManager.readTextFileContent(any())).thenReturn(testModelJson());
        when(fileManager.exists(any())).thenReturn(true);
        when(context.getScoresheetFolder()).thenReturn(new File("."));

        ScoresheetStore store = new ScoresheetStore(fileManager,new JsonCodec(),context);

        assertEquals(0, model.getEvents().size());

        ScoresheetStore.StoreResult result = store.loadInto(model, "test.json");

        assertEquals(true, result.success);

        assertEquals(7, model.getEvents().size());
    }

    private String testModelJson() {
        ScoresheetModel sourceModel = new ScoresheetModel();
        addTestEvents(sourceModel);
        return new JsonCodec().toJson(sourceModel);
    }
}
