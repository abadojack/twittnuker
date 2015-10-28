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

package de.vanita5.twittnuker.loader.support;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import de.vanita5.twittnuker.model.ParcelableActivity;

import java.util.List;

import de.vanita5.twittnuker.api.twitter.model.Activity;
import de.vanita5.twittnuker.api.twitter.model.Paging;
import de.vanita5.twittnuker.api.twitter.Twitter;
import de.vanita5.twittnuker.api.twitter.TwitterException;

public class ActivitiesAboutMeLoader extends TwitterAPIActivitiesLoader {

    public ActivitiesAboutMeLoader(final Context context, final long accountId, long sinceId,
                                   long maxId, final List<ParcelableActivity> data,
                                   final String[] saveFileArgs, final int position) {
        super(context, accountId, sinceId, maxId, data, saveFileArgs, position);
	}

	@Override
	protected List<Activity> getActivities(final Twitter twitter, final Paging paging) throws TwitterException {
		if (twitter == null) return null;
		return twitter.getActivitiesAboutMe(paging);
    }

    @Override
    protected boolean shouldFilterActivity(SQLiteDatabase database, ParcelableActivity activity) {
        return false;
	}

}