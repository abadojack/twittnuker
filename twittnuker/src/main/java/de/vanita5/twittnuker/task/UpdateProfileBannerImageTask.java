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

package de.vanita5.twittnuker.task;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.api.twitter.Twitter;
import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.User;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.model.SingleResponse;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.model.message.ProfileUpdatedEvent;
import de.vanita5.twittnuker.model.util.ParcelableUserUtils;
import de.vanita5.twittnuker.util.TwitterAPIFactory;
import de.vanita5.twittnuker.util.TwitterWrapper;
import de.vanita5.twittnuker.util.Utils;

import java.io.FileNotFoundException;

public class UpdateProfileBannerImageTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableUser>> {

    private final UserKey mAccountKey;
    private final Uri mImageUri;
    private final boolean mDeleteImage;
    private final Context mContext;

    public UpdateProfileBannerImageTask(final Context context, final UserKey accountKey,
                                        final Uri imageUri, final boolean deleteImage) {
        super(context);
        mContext = context;
        mAccountKey = accountKey;
        mImageUri = imageUri;
        mDeleteImage = deleteImage;
    }

    @Override
    protected void onPostExecute(final SingleResponse<ParcelableUser> result) {
        super.onPostExecute(result);
        if (result.hasData()) {
            Utils.showOkMessage(mContext, R.string.profile_banner_image_updated, false);
            bus.post(new ProfileUpdatedEvent(result.getData()));
        } else {
            Utils.showErrorMessage(mContext, R.string.action_updating_profile_banner_image, result.getException(),
                    true);
        }
    }

    @Override
    protected SingleResponse<ParcelableUser> doInBackground(final Object... params) {
        try {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountKey,
                    true);
            TwitterWrapper.updateProfileBannerImage(mContext, twitter, mImageUri, mDeleteImage);
            // Wait for 5 seconds, see
            // https://dev.twitter.com/docs/api/1.1/post/account/update_profile_image
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                Log.w(LOGTAG, e);
            }
            final User user = TwitterWrapper.tryShowUser(twitter, mAccountKey.getId(), null);
            return SingleResponse.getInstance(ParcelableUserUtils.fromUser(user, mAccountKey));
        } catch (TwitterException | FileNotFoundException e) {
            return SingleResponse.getInstance(e);
        }
    }


}