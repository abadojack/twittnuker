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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import java.util.ArrayList;
import java.util.List;

@JsonObject
@ParcelablePlease
public class UserKey implements Comparable<UserKey>, Parcelable {

    public static final UserKey SELF_REFERENCE = new UserKey("#self#", "#self#");

    public static final Creator<UserKey> CREATOR = new Creator<UserKey>() {
        @Override
        public UserKey createFromParcel(Parcel source) {
            UserKey target = new UserKey();
            UserKeyParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public UserKey[] newArray(int size) {
            return new UserKey[size];
        }
    };

    @NonNull
    @JsonField(name = "id")
    @ParcelableThisPlease
    String id = "";
    @Nullable
    @JsonField(name = "host")
    @ParcelableThisPlease
    String host;

    public UserKey(@NonNull String id, @Nullable String host) {
        this.id = id;
        this.host = host;
    }

    UserKey() {

    }

    public boolean isSelfReference() {
        return equals(SELF_REFERENCE);
    }

    @NonNull
    public String getId() {
        return id;
    }

    @Nullable
    public String getHost() {
        return host;
    }

    @Override
    public String toString() {
        if (host != null) return escapeText(id) + "@" + escapeText(host);
        return id;
    }

    @Override
    public int compareTo(@NonNull UserKey another) {
        if (this.id.equals(another.id)) {
            if (this.host != null && another.host != null) {
                return this.host.compareTo(another.host);
            } else if (this.host != null) {
                return 1;
            } else if (another.host != null) {
                return -1;
            }
            return 0;
        }
        return this.id.compareTo(another.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserKey userKey = (UserKey) o;

        if (!id.equals(userKey.id)) return false;
        return !(host != null ? !host.equals(userKey.host) : userKey.host != null);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (host != null ? host.hashCode() : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        UserKeyParcelablePlease.writeToParcel(this, dest, flags);
    }

    public boolean check(String accountId, String accountHost) {
        return this.id.equals(accountId);
    }

    @Nullable
    public static UserKey valueOf(@Nullable String str) {
        if (str == null) return null;
        boolean escaping = false, idFinished = false;
        StringBuilder idBuilder = new StringBuilder(str.length()),
                hostBuilder = new StringBuilder(str.length());
        for (int i = 0, j = str.length(); i < j; i++) {
            final char ch = str.charAt(i);
            boolean append = false;
            if (escaping) {
                // accept all characters if is escaping
                append = true;
                escaping = false;
            } else if (ch == '\\') {
                escaping = true;
            } else if (ch == '@') {
                idFinished = true;
            } else if (ch == ',') {
                // end of item, just jump out
                break;
            } else {
                append = true;
            }
            if (append) {
                if (idFinished) {
                    hostBuilder.append(ch);
                } else {
                    idBuilder.append(ch);
                }
            }
        }
        if (hostBuilder.length() != 0) {
            return new UserKey(idBuilder.toString(), hostBuilder.toString());
        } else {
            return new UserKey(idBuilder.toString(), null);
        }
    }

    @Nullable
    public static UserKey[] arrayOf(@Nullable String str) {
        if (str == null) return null;
        List<String> split = split(str, ",");
        UserKey[] keys = new UserKey[split.size()];
        for (int i = 0, splitLength = split.size(); i < splitLength; i++) {
            keys[i] = valueOf(split.get(i));
            if (keys[i] == null) return null;
        }
        return keys;
    }

    public static String[] getIds(UserKey[] ids) {
        String[] result = new String[ids.length];
        for (int i = 0, idsLength = ids.length; i < idsLength; i++) {
            result[i] = ids[i].getId();
        }
        return result;
    }

    public static String escapeText(String host) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0, j = host.length(); i < j; i++) {
            final char ch = host.charAt(i);
            if (isSpecialChar(ch)) {
                sb.append('\\');
            }
            sb.append(ch);
        }
        return sb.toString();
    }


    private static boolean isSpecialChar(char ch) {
        return ch == '\\' || ch == '@' || ch == ',';
    }

    public boolean maybeEquals(@Nullable UserKey another) {
        return another != null && another.getId().equals(id);
    }

    public static List<String> split(String input, String delim) {
        List<String> l = new ArrayList<>();
        int offset = 0;

        while (true) {
            int index = input.indexOf(delim, offset);
            if (index == -1) {
                l.add(input.substring(offset));
                return l;
            } else {
                l.add(input.substring(offset, index));
                offset = index + delim.length();
            }
        }
    }
}