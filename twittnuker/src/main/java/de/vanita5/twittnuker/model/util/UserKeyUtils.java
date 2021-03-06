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

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import de.vanita5.twittnuker.library.twitter.model.User;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.provider.TwidereDataStore.Accounts;
import de.vanita5.twittnuker.util.DataStoreUtils;
import de.vanita5.twittnuker.util.UriUtils;

import java.util.ArrayList;

import static de.vanita5.twittnuker.TwittnukerConstants.USER_TYPE_FANFOU_COM;
import static de.vanita5.twittnuker.TwittnukerConstants.USER_TYPE_TWITTER_COM;

public class UserKeyUtils {

    private UserKeyUtils() {
    }

    @Nullable
    public static UserKey findById(Context context, String id) {
        final String[] projection = {Accounts.ACCOUNT_KEY};
        final Cursor cur = DataStoreUtils.findAccountCursorsById(context, projection, id);
        if (cur == null) return null;
        try {
            if (cur.moveToFirst()) return UserKey.valueOf(cur.getString(0));
        } finally {
            cur.close();
        }
        return null;
    }

    @NonNull
    public static UserKey[] findByIds(Context context, String... id) {
        final String[] projection = {Accounts.ACCOUNT_KEY};
        final Cursor cur = DataStoreUtils.findAccountCursorsById(context, projection, id);
        if (cur == null) return new UserKey[0];
        try {
            final ArrayList<UserKey> accountKeys = new ArrayList<>();
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                accountKeys.add(UserKey.valueOf(cur.getString(0)));
                cur.moveToNext();
            }
            return accountKeys.toArray(new UserKey[accountKeys.size()]);
        } finally {
            cur.close();
        }
    }

    public static UserKey fromUser(User user) {
        return new UserKey(user.getId(), getUserHost(user));
    }

    public static String getUserHost(User user) {
        if (isFanfouUser(user)) return USER_TYPE_FANFOU_COM;
        return getUserHost(user.getStatusnetProfileUrl(), USER_TYPE_TWITTER_COM);
    }

    public static String getUserHost(ParcelableUser user) {
        if (isFanfouUser(user)) return USER_TYPE_FANFOU_COM;
        if (user.extras == null) return USER_TYPE_TWITTER_COM;

        return getUserHost(user.extras.statusnet_profile_url, USER_TYPE_TWITTER_COM);
    }

    public static boolean isFanfouUser(User user) {
        return user.getUniqueId() != null && user.getProfileImageUrlLarge() != null;
    }

    public static boolean isFanfouUser(ParcelableUser user) {
        return USER_TYPE_FANFOU_COM.equals(user.key.getHost());
    }


    @NonNull
    public static String getUserHost(@Nullable String uri, @Nullable String def) {
        if (def == null) {
            def = USER_TYPE_TWITTER_COM;
        }
        if (uri == null) return def;
        final String authority = UriUtils.getAuthority(uri);
        if (authority == null) return def;
        return authority.replaceAll("[^\\w\\d\\.]", "-");
    }

    public static boolean isSameHost(UserKey accountKey, UserKey userKey) {
        return isSameHost(accountKey.getHost(), userKey.getHost());
    }

    public static boolean isSameHost(String a, String b) {
        if (TextUtils.isEmpty(a) || TextUtils.isEmpty(b)) return true;
        return TextUtils.equals(a, b);
    }
}