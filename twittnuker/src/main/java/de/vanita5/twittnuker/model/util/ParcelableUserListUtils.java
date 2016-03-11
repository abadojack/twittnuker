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

package de.vanita5.twittnuker.model.util;

import de.vanita5.twittnuker.api.twitter.model.User;
import de.vanita5.twittnuker.api.twitter.model.UserList;
import de.vanita5.twittnuker.model.AccountId;
import de.vanita5.twittnuker.model.ParcelableUserList;
import de.vanita5.twittnuker.util.TwitterContentUtils;

public class ParcelableUserListUtils {
    public static ParcelableUserList from(UserList list, AccountId accountId) {
        return from(list, accountId, 0, false);
    }

    public static ParcelableUserList from(UserList list, AccountId accountId, long position, boolean isFollowing) {
        ParcelableUserList obj = new ParcelableUserList();
        final User user = list.getUser();
        obj.position = position;
        obj.account_id = accountId.getId();
        obj.account_host = accountId.getHost();
        obj.id = list.getId();
        obj.is_public = UserList.Mode.PUBLIC.equals(list.getMode());
        obj.is_following = isFollowing;
        obj.name = list.getName();
        obj.description = list.getDescription();
        obj.user_id = user.getId();
        obj.user_name = user.getName();
        obj.user_screen_name = user.getScreenName();
        obj.user_profile_image_url = TwitterContentUtils.getProfileImageUrl(user);
        obj.members_count = list.getMemberCount();
        obj.subscribers_count = list.getSubscriberCount();
        return obj;
    }

    public static ParcelableUserList[] fromUserLists(UserList[] userLists, long accountId) {
        if (userLists == null) return null;
        int size = userLists.length;
        final ParcelableUserList[] result = new ParcelableUserList[size];
        for (int i = 0; i < size; i++) {
            result[i] = new ParcelableUserList(userLists[i], accountId);
        }
        return result;
    }
}