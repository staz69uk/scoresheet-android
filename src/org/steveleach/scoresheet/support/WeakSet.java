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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A set of weak references to other objects.
 *
 * Being in this collection will not prevent the item being garbage collected.
 *
 * Note that this class does not actually implement the Java collections Set interface.
 *
 * @author Steve Leach
 */
public class WeakSet<T> implements Iterable<T> {
    private List<WeakReference<T>> references = new LinkedList<>();
    private ReferenceQueue<T> referenceQueue = new ReferenceQueue<>();

    /**
     * Adds a new item to this collection.
     *
     * Items are held via weak references, and so the collection
     * will not prevent the item being garbage collected.
     *
     * If the item is already in the list then no action is taken.
     */
    public void add(T item) {
        if (! containsItem(item)) {
            add(new WeakReference<>(item, referenceQueue));
        }
    }

    void add(WeakReference<T> ref) {
        references.add(ref);
    }

    public boolean containsItem(T target) {
        for (WeakReference<T> item : references) {
            if ((item != null) && (item.get() != null) && (item.get() == target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove any list items that have been garbage collected.
     */
    public void removeDeadItems() {
        Reference<?> ref = pollQueue();
        while (ref != null) {
            references.remove(ref);
            ref = pollQueue();
        }
    }

    Reference<?> pollQueue() {
        return referenceQueue.poll();
    }

    /**
     * Allows the caller to iterate across the referenced items in a foreach loop.
     *
     * Any "dead" items should have been removed from the collection before the
     * iterator is returned, but it is still possible for the iterator to return
     * a null object in some situations.
     */
    @Override
    public Iterator<T> iterator() {
        removeDeadItems();

        final Iterator<WeakReference<T>> iterator = references.iterator();
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
                throw new UnsupportedOperationException("Cannot remove items with this iterator");
            }
        };
    }

    public int size() {
        return references.size();
    }
}
