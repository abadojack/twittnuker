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

package de.vanita5.twittnuker.library.twitter.template;

import org.mariotaku.restfu.annotation.param.KeyValue;
import org.mariotaku.restfu.annotation.param.Queries;

@Queries({@KeyValue(key = "include_my_retweet", valueKey = "include_my_retweet"),
        @KeyValue(key = "include_rts", valueKey = "include_entities"),
        @KeyValue(key = "include_entities", valueKey = "include_entities"),
        @KeyValue(key = "include_cards", valueKey = "include_cards"),
        @KeyValue(key = "cards_platform", valueKey = "cards_platform"),
        @KeyValue(key = "include_reply_count", valueKey = "include_reply_count"),
        @KeyValue(key = "include_descendent_reply_count", valueKey = "include_descendent_reply_count"),
        @KeyValue(key = "include_ext_alt_text", valueKey = "include_ext_alt_text"),
        @KeyValue(key = "tweet_mode", valueKey = "tweet_mode"),
        @KeyValue(key = "model_version", valueKey = "model_version"),
        @KeyValue(key = "include_blocking", value = "true"),
        @KeyValue(key = "include_blocked_by", value = "true")
})
public class StatusAnnotationTemplate {
}