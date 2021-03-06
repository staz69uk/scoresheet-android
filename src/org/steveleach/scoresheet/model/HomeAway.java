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
 * Holds values for home and away teams.
 *
 * @author Steve Leach
 */
public class HomeAway<T> {

    private final T home;
    private final T away;

    public HomeAway(T home, T away) {
        this.home = home;
        this.away = away;
    }

    public T getAway() {
        return away;
    }

    public T getHome() {

        return home;
    }

    @Override
    public String toString() {
        return String.format("(%s,%s)",getHome().toString(),getAway().toString());
    }
}
