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
import org.steveleach.scoresheet.FakeFileManager;
import org.steveleach.scoresheet.FastTestSuite;
import org.steveleach.scoresheet.support.*;
import org.steveleach.scoresheet.ui.AndroidSystemContext;
import org.steveleach.scoresheet.model.*;
import org.steveleach.scoresheet.ui.ScoresheetActivity;

import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.sql.Ref;
import java.util.Iterator;

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

        StoreResult status = store.save(model);

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
    public void testWeakSetCleanDead() {
        // Set up state so that first call to referenceQueue.poll returns non-null, second call returns null
        WeakReference<String> ref = new WeakReference<String>("Bob");
        final WeakReference<String>[] refs = new WeakReference[] {ref,null};
        WeakSet<String> list = new WeakSet<String>() {
            int index = 0;
            @Override
            Reference<?> pollQueue() {
                return refs[index++];
            }
        };
        // Make sure that removing the result of the first call to poll will succeed
        list.add(ref);

        // Then run the actual test
        list.removeDeadItems();

        assertEquals(0, list.size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWeakSetRemove() {
        WeakSet<String> list = new WeakSet<>();
        Iterator<?> iterator = list.iterator();
        iterator.remove();
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

        StoreResult result = store.loadInto(model, "test.json");

        assertEquals(true, result.success);

        assertEquals(7, model.getEvents().size());
    }

    private String testModelJson() {
        ScoresheetModel sourceModel = new ScoresheetModel();
        addTestEvents(sourceModel);
        return new JsonCodec().toJson(sourceModel);
    }

    @Test
    public void testStoreDelete() {
        when(fileManager.delete(any())).thenReturn(true);
        when(context.getScoresheetFolder()).thenReturn(new File("."));

        ScoresheetStore store = new ScoresheetStore(fileManager,null,context);

        StoreResult result = store.delete("somefile.json");
        assertTrue(result.success);
        assertEquals("Deleted somefile.json", result.text);
    }

    @Test
    public void testIsAutoFile() {
        ScoresheetStore store = new ScoresheetStore(null,null,null);

        assertTrue( store.isAutoFile(new File("gamedata-2016-04-23-03-04-05--2-00-00.json")) );
        assertTrue( store.isAutoFile(new File("gamedata-2016-04-23-03-04-05.json")) );
        assertFalse( store.isAutoFile(new File("gamedata.json")) );
        assertFalse( store.isAutoFile(new File("fred.json")) );
        assertFalse( store.isAutoFile(new File("gamedata.txt")) );
    }

    @Test
    public void testLoadMissingFile() throws IOException {
        when(fileManager.exists(any())).thenReturn(true);
        when(fileManager.readTextFileContent(any())).thenThrow(new IOException("Broken"));
        when(context.getScoresheetFolder()).thenReturn(new File("."));

        ScoresheetStore store = new ScoresheetStore(fileManager,null,context);

        ScoresheetModel model = new ScoresheetModel();

        StoreResult result = store.loadInto(model, "gamedata.json");

        assertFalse(result.success);
        assertNotNull(result.error);
        assertEquals(IOException.class, result.error.getClass());
    }

    @Test
    public void testStoreRename() throws IOException {
        when(context.getScoresheetFolder()).thenReturn(new File("."));
        FakeFileManager ffm = new FakeFileManager();

        ScoresheetStore store = new ScoresheetStore(ffm,new JsonCodec(),context);

        File oldFile = new File(store.getBaseDirectory(), "gamedata-1.json");
        File newFile = new File(store.getBaseDirectory(), "gamedata-2.json");

        ffm.writeTextFile(oldFile, "Content");

        store.renameFile(oldFile.getName(), newFile.getName());

        assertEquals("Content", ffm.getContentOf("gamedata-2.json"));
    }

    /**
     * Write a model to a file named after the current JSON model format.
     *
     * This will be used to test the ability to load it again with a later version of the code. Not actually a test
     * really, more preparation for future tests.
     */
    @Test
    public void writeModelToFile() throws IOException {
        FileManager fm = new FileManager();
        File testDataDir = new File("testdata");

        ScoresheetModel model = new ScoresheetModel();
        model.setHomeTeamName("Blues");
        model.setAwayTeamName("Reds");
        model.setGameLocation("Somewhere over the rainbow");
        FastTestSuite.addTestEvents(model);

        String json = new JsonCodec().toJson(model);
        String versionStr = JsonCodec.FORMAT_VERSION.replace('.','_');

        File targetFile = new File(testDataDir,"gamedata-v"+versionStr+".json");

        if (!targetFile.exists()) {
            fm.writeTextFile(targetFile, json);
        }
    }

    @Test
    public void testFileManagerRename() {
        File from = mock(File.class);
        File to = mock(File.class);

        when(from.renameTo(any())).thenReturn(true);

        boolean result = new FileManager().rename(from, to);

        assertTrue(result);
    }

    @Test
    public void testFileManagerListNull() {
        assertEquals(0, new FileManager().dirContents(null).size());
    }

    @Test
    public void testLoadTeamFromFile() throws IOException {
        String json = new FileManager().readTextFileContent(new File("./testdata/team-v1_0_0.json"));
        Team team = new JsonCodec().teamFromJson(json);

        assertEquals("Teeme", team.getName());
        assertEquals(5, team.getPlayers().size());
        Player p41 = team.getPlayers().get(41);
        assertEquals("J Smith", p41.getName());
        assertEquals(41, p41.getNumber());
        assertTrue(p41.isPlaying());
    }

    @Test
    public void testTeamJsonRoundTrip() {
        Team team1 = new Team();
        team1.setName("Teeme");
        team1.addPlayer(25,"J Smith");
        team1.addPlayer(39,"F Bloggs");

        JsonCodec codec = new JsonCodec();

        String json = codec.teamToJson(team1);

        Team team2 = codec.teamFromJson(json);

        assertEquals(team1.getName(), team2.getName());
        assertEquals(team1.getPlayers().size(), team2.getPlayers().size());
        assertEquals(team1.getPlayers().get(25).getName(), team2.getPlayers().get(25).getName());
    }

    @Test
    public void testJsonCodecMisc() {
        assertEquals(5, JsonCodec.getInt("5"));
        assertNull(JsonCodec.getDate("X"));
        assertNull(JsonCodec.getDate("1234567890"));
    }

    @Test(expected = JSONException.class)
    public void testJsonFromNull() {
        new JsonCodec().fromJson(new ScoresheetModel(), null);
    }
}
