/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.provider.TwidereDataStore;
import de.vanita5.twittnuker.provider.TwidereDataStore.UnreadCounts;

public class UnreadCountUtils implements Constants {

	public static int getUnreadCount(final Context context, final int position) {
		if (context == null || position < 0) return 0;
		final ContentResolver resolver = context.getContentResolver();
		final Uri.Builder builder = TwidereDataStore.UnreadCounts.CONTENT_URI.buildUpon();
		builder.appendPath(ParseUtils.parseString(position));
		final Uri uri = builder.build();
		final Cursor c = resolver.query(uri, new String[] { UnreadCounts.COUNT }, null, null, null);
		if (c == null) return 0;
		try {
			if (c.getCount() == 0) return 0;
			c.moveToFirst();
			return c.getInt(c.getColumnIndex(UnreadCounts.COUNT));
		} finally {
			c.close();
		}
	}

	public static int getUnreadCount(final Context context, final String type) {
		if (context == null || type == null) return 0;
		final ContentResolver resolver = context.getContentResolver();
		final Uri.Builder builder = TwidereDataStore.UnreadCounts.ByType.CONTENT_URI.buildUpon();
		builder.appendPath(type);
		final Uri uri = builder.build();
		final Cursor c = resolver.query(uri, new String[] { UnreadCounts.COUNT }, null, null, null);
		if (c == null) return 0;
		try {
			if (c.getCount() == 0) return 0;
			c.moveToFirst();
			return c.getInt(c.getColumnIndex(UnreadCounts.COUNT));
		} finally {
			c.close();
		}
	}
}
