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

package de.vanita5.twittnuker.loader;

import android.content.Context;
import android.support.annotation.NonNull;

import de.vanita5.twittnuker.library.MicroBlog;
import de.vanita5.twittnuker.library.MicroBlogException;
import de.vanita5.twittnuker.library.twitter.model.PageableResponseList;
import de.vanita5.twittnuker.library.twitter.model.Paging;
import de.vanita5.twittnuker.library.twitter.model.User;
import de.vanita5.twittnuker.model.ParcelableCredentials;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.model.UserKey;

import java.util.List;

public class UserListSubscribersLoader extends CursorSupportUsersLoader {

    private final String mListId;
    private final UserKey mUserKey;
    private final String mScreenName, mListName;

    public UserListSubscribersLoader(final Context context, final UserKey accountKey, final String listId,
                                     final UserKey userKey, final String screenName, final String listName,
                                     final List<ParcelableUser> data, boolean fromUser) {
        super(context, accountKey, data, fromUser);
        mListId = listId;
        mUserKey = userKey;
        mScreenName = screenName;
        mListName = listName;
    }

    @NonNull
    @Override
    public PageableResponseList<User> getCursoredUsers(@NonNull final MicroBlog twitter, @NonNull ParcelableCredentials credentials, @NonNull final Paging paging)
            throws MicroBlogException {
        if (mListId != null)
            return twitter.getUserListSubscribers(mListId, paging);
        else if (mUserKey != null)
            return twitter.getUserListSubscribers(mListName.replace(' ', '-'), mUserKey.getId(), paging);
        else if (mScreenName != null)
            return twitter.getUserListSubscribersByScreenName(mListName.replace(' ', '-'), mScreenName, paging);
        throw new MicroBlogException("list_id or list_name and user_id (or screen_name) required");
    }

}