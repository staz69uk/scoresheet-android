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

import org.junit.Test;
import org.steveleach.scoresheet.model.ScoresheetModel;
import org.steveleach.scoresheet.support.FileManager;
import org.steveleach.scoresheet.support.JsonCodec;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Verify that the latest code can load various old game files.
 *
 * @author Steve Leach
 */
public class LoadOldGameFileTest {

    private FileManager fm = new FileManager();
    private File testDataDir = new File("testdata");

    @Test
    public void testFormat_v1_0_0() throws IOException {
        ScoresheetModel model = loadModel("gamedata-v1_0_0.json");
        assertEquals(31, model.getEvents().size());
        assertEquals("Home", model.homeTeamName());
        assertEquals(9, model.getHomeGoals());
        assertEquals(11, model.getAwayGoals());
        assertEquals(69, model.getEvents().get(0).getPlayer());
    }

    @Test
    public void testFormat_v1_0_1() throws IOException {
        ScoresheetModel model = loadModel("gamedata-v1_0_1.json");
        assertEquals(25, model.getEvents().size());
        assertEquals("Smoke", model.homeTeamName());
        assertEquals(8, model.getHomeGoals());
        assertEquals(7, model.getAwayGoals());
        assertEquals(93, model.getEvents().get(0).getPlayer());
    }

    @Test
    public void testFormat_v1_1_0() throws IOException {
        ScoresheetModel model = loadModel("gamedata-v1_1_0.json");
        assertEquals(25, model.getEvents().size());
        assertEquals("Smoke", model.homeTeamName());
        assertEquals("Guildford Spectrum", model.getGameLocation());
        assertEquals(8, model.getHomeGoals());
        assertEquals(7, model.getAwayGoals());
        assertEquals(93, model.getEvents().get(0).getPlayer());
    }

    @Test
    public void testLoadAll() throws IOException {
        for (String fileName : testDataDir.list()) {
            if (fileName.contains("gamedata")) {
                ScoresheetModel model = loadModel(fileName);
                assertTrue(model.getEvents().size() > 0);
            }
        }
    }

    private ScoresheetModel loadModel(String fileName) throws IOException {
        ScoresheetModel model = new ScoresheetModel();
        new JsonCodec().fromJson(model, fm.readTextFileContent(new File(testDataDir,fileName)));
        return model;
    }
}
