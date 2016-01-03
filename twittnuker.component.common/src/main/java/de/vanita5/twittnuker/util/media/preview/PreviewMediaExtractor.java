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

package de.vanita5.twittnuker.util.media.preview;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import de.vanita5.twittnuker.model.ParcelableMedia;
import de.vanita5.twittnuker.util.HtmlLinkExtractor;
import de.vanita5.twittnuker.util.media.preview.provider.InstagramProvider;
import de.vanita5.twittnuker.util.media.preview.provider.Provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PreviewMediaExtractor {

    private static final Provider[] sProviders = {
            new InstagramProvider()
    };

    @Nullable
    public static ParcelableMedia fromLink(@Nullable String link) {
        if (TextUtils.isEmpty(link)) return null;
        for (Provider provider : sProviders) {
            if (provider.supportsAuthority(getAuthority(link))) {
                return provider.from(Uri.parse(link));
            }
        }
        return null;
    }

    @Nullable
    private static String getAuthority(@NonNull String link) {
        int start = link.indexOf("://");
        if (start < 0) return null;
        int end = link.indexOf('/', start + 3);
        if (end < 0) {
            end = link.length();
        }
        return link.substring(start + 3, end);
    }

    public static boolean isSupported(@Nullable String link) {
        if (TextUtils.isEmpty(link)) return false;
        for (Provider provider : sProviders) {
            if (provider.supportsAuthority(getAuthority(link))) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getSupportedLinksInStatus(final String statusString) {
        if (statusString == null) return Collections.emptyList();
        final List<String> links = new ArrayList<>();
        final HtmlLinkExtractor extractor = new HtmlLinkExtractor();
        for (final HtmlLinkExtractor.HtmlLink link : extractor.grabLinks(statusString)) {
            final String linkString = link.getLink();
            if (isSupported(linkString)) {
                links.add(linkString);
            }
        }
        return links;
    }
}