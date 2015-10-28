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

package de.vanita5.twittnuker.api.twitter;

import de.vanita5.twittnuker.api.twitter.api.DirectMessagesResources;
import de.vanita5.twittnuker.api.twitter.api.FavoritesResources;
import de.vanita5.twittnuker.api.twitter.api.FriendsFollowersResources;
import de.vanita5.twittnuker.api.twitter.api.HelpResources;
import de.vanita5.twittnuker.api.twitter.api.ListsResources;
import de.vanita5.twittnuker.api.twitter.api.PlacesGeoResources;
import de.vanita5.twittnuker.api.twitter.api.PrivateActivityResources;
import de.vanita5.twittnuker.api.twitter.api.PrivateDirectMessagesResources;
import de.vanita5.twittnuker.api.twitter.api.PrivateFriendsFollowersResources;
import de.vanita5.twittnuker.api.twitter.api.PrivateScheduleResources;
import de.vanita5.twittnuker.api.twitter.api.PrivateTimelinesResources;
import de.vanita5.twittnuker.api.twitter.api.PrivateTweetResources;
import de.vanita5.twittnuker.api.twitter.api.SavedSearchesResources;
import de.vanita5.twittnuker.api.twitter.api.SearchResource;
import de.vanita5.twittnuker.api.twitter.api.SpamReportingResources;
import de.vanita5.twittnuker.api.twitter.api.TimelinesResources;
import de.vanita5.twittnuker.api.twitter.api.TrendsResources;
import de.vanita5.twittnuker.api.twitter.api.TweetResources;
import de.vanita5.twittnuker.api.twitter.api.UsersResources;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.2.0
 */
public interface Twitter extends SearchResource, TimelinesResources,
		TweetResources, UsersResources, ListsResources, DirectMessagesResources, FriendsFollowersResources,
		FavoritesResources, SpamReportingResources, SavedSearchesResources, TrendsResources, PlacesGeoResources,
        HelpResources, PrivateActivityResources, PrivateTweetResources, PrivateTimelinesResources,
        PrivateFriendsFollowersResources, PrivateDirectMessagesResources, PrivateScheduleResources {
}