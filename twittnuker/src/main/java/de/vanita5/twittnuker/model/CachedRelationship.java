/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import de.vanita5.twittnuker.library.twitter.model.Relationship;
import de.vanita5.twittnuker.model.util.UserKeyCursorFieldConverter;
import de.vanita5.twittnuker.provider.TwidereDataStore;
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedRelationships;

@CursorObject(valuesCreator = true, tableInfo = true)
public class CachedRelationship {

    @CursorField(value = CachedRelationships.ACCOUNT_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey account_key;

    @CursorField(value = CachedRelationships.USER_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey user_key;

    @CursorField(CachedRelationships.FOLLOWING)
    public boolean following;

    @CursorField(CachedRelationships.FOLLOWED_BY)
    public boolean followed_by;

    @CursorField(CachedRelationships.BLOCKING)
    public boolean blocking;

    @CursorField(CachedRelationships.BLOCKED_BY)
    public boolean blocked_by;

    @CursorField(CachedRelationships.MUTING)
    public boolean muting;

    @CursorField(CachedRelationships.RETWEET_ENABLED)
    public boolean retweet_enabled;

    @CursorField(value = CachedRelationships._ID, excludeWrite = true, type = TwidereDataStore.TYPE_PRIMARY_KEY)
    public long _id;

    public CachedRelationship() {

    }

    public CachedRelationship(@Nullable Relationship relationship, @NonNull UserKey accountKey, @NonNull UserKey userKey) {
        account_key = accountKey;
        user_key = userKey;
        if (relationship != null) {
            following = relationship.isSourceFollowingTarget();
            followed_by = relationship.isSourceFollowedByTarget();
            blocking = relationship.isSourceBlockingTarget();
            blocked_by = relationship.isSourceBlockedByTarget();
            muting = relationship.isSourceMutingTarget();
            retweet_enabled = relationship.isSourceWantRetweetsFromTarget();
        }
    }
}