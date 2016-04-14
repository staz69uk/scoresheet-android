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
package org.steveleach.scoresheet.model;

/**
 * Describes an update to a ScoresheetModel.
 *
 * @author Steve Leach
 */
public class ModelUpdate {

    private String summary = null;

    public ModelUpdate(String summary) {
        setSummary(summary);
    }
    public ModelUpdate() { this(null); }

    public static final ModelUpdate EVENT_ADDED = new ModelUpdate("Event added");
    public static final ModelUpdate EVENT_REMOVED = new ModelUpdate("Event removed");
    public static final ModelUpdate EVENTS_CLEARED = new ModelUpdate("Events cleared");
    public static final ModelUpdate ALL_CHANGED = new ModelUpdate("All changed");

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
