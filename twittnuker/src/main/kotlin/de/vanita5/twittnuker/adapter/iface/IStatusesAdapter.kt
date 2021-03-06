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

package de.vanita5.twittnuker.adapter.iface

import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.util.MediaLoadingHandler
import de.vanita5.twittnuker.util.TwidereLinkify
import de.vanita5.twittnuker.view.CardMediaContainer
import de.vanita5.twittnuker.view.holder.iface.IStatusViewHolder

interface IStatusesAdapter<Data> : IContentCardAdapter, IGapSupportedAdapter {

    @TwidereLinkify.HighlightStyle
    val linkHighlightingStyle: Int

    @CardMediaContainer.PreviewStyle
    val mediaPreviewStyle: Int

    val statusCount: Int

    val rawStatusCount: Int

    val twidereLinkify: TwidereLinkify

    val mediaPreviewEnabled: Boolean

    val nameFirst: Boolean

    val sensitiveContentEnabled: Boolean

    val showAccountsColor: Boolean

    val useStarsForLikes: Boolean

    val mediaLoadingHandler: MediaLoadingHandler

    val statusClickListener: IStatusViewHolder.StatusClickListener?

    fun isCardActionsShown(position: Int): Boolean

    fun showCardActions(position: Int)

    fun setData(data: Data?): Boolean

    fun getStatus(position: Int): ParcelableStatus?

    fun getStatusId(position: Int): String?

    fun getStatusTimestamp(position: Int): Long

    fun getStatusPositionKey(position: Int): Long

    fun getAccountKey(position: Int): UserKey?

    fun findStatusById(accountKey: UserKey, statusId: String): ParcelableStatus?

}