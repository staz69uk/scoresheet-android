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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Maintains a list of weak references to other objects.
 *
 * Created by steve on 23/03/16.
 */
public class WeakList<T> implements Iterable<T> {
    private List<WeakReference<T>> references = new LinkedList<>();
    private ReferenceQueue<T> referenceQueue = new ReferenceQueue<>();

    /**
     * Adds a new item to this list.
     *
     * Items are held via weak references, and so the  list
     * will not prevent the item being garbage collected.
     */
    public void add(T item) {
        references.add(new WeakReference<>(item,referenceQueue));
    }

    /**
     * Remove any list items that have been garbage collected.
     */
    public void removeDeadItems() {
        Reference<?> ref = referenceQueue.poll();
        while (ref != null) {
            references.remove(ref);
            ref = referenceQueue.poll();
        }
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<WeakReference<T>> iterator = references.iterator();
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return iterator.next().get();
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }
}
