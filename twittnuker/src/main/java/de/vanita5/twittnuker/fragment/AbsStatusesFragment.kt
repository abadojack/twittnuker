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

package de.vanita5.twittnuker.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.Loader
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnScrollListener
import android.text.TextUtils
import android.util.Log
import android.view.*
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_content_recyclerview.*
import de.vanita5.twittnuker.BuildConfig
import de.vanita5.twittnuker.Constants
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.TwittnukerConstants
import de.vanita5.twittnuker.adapter.ParcelableStatusesAdapter
import de.vanita5.twittnuker.adapter.decorator.DividerItemDecoration
import de.vanita5.twittnuker.adapter.iface.ILoadMoreSupportAdapter
import de.vanita5.twittnuker.annotation.ReadPositionTag
import de.vanita5.twittnuker.annotation.Referral
import de.vanita5.twittnuker.constant.IntentConstants
import de.vanita5.twittnuker.constant.KeyboardShortcutConstants
import de.vanita5.twittnuker.constant.SharedPreferenceConstants
import de.vanita5.twittnuker.graphic.like.LikeAnimationDrawable
import de.vanita5.twittnuker.loader.iface.IExtendedLoader
import de.vanita5.twittnuker.model.*
import de.vanita5.twittnuker.model.message.StatusListChangedEvent
import de.vanita5.twittnuker.util.*
import de.vanita5.twittnuker.util.KeyboardShortcutsHandler.KeyboardShortcutCallback
import de.vanita5.twittnuker.util.imageloader.PauseRecyclerViewOnScrollListener
import de.vanita5.twittnuker.view.ExtendedRecyclerView
import de.vanita5.twittnuker.view.holder.GapViewHolder
import de.vanita5.twittnuker.view.holder.StatusViewHolder
import de.vanita5.twittnuker.view.holder.iface.IStatusViewHolder
import java.util.*

abstract class AbsStatusesFragment protected constructor() : AbsContentListRecyclerViewFragment<ParcelableStatusesAdapter>(), LoaderCallbacks<List<ParcelableStatus>?>, IStatusViewHolder.StatusClickListener, KeyboardShortcutCallback {

    private val statusesBusCallback: Any
    private val onScrollListener = object : OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val layoutManager = layoutManager
                saveReadPosition(layoutManager!!.findFirstVisibleItemPosition())
            }
        }
    }
    private var navigationHelper: RecyclerViewNavigationHelper? = null
    private var pauseOnScrollListener: OnScrollListener? = null

    init {
        statusesBusCallback = createMessageBusCallback()
    }

    abstract fun getStatuses(param: RefreshTaskParam): Boolean

    override fun handleKeyboardShortcutSingle(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        var action = handler.getKeyAction(KeyboardShortcutConstants.CONTEXT_TAG_NAVIGATION, keyCode, event, metaState)
        if (KeyboardShortcutConstants.ACTION_NAVIGATION_REFRESH == action) {
            triggerRefresh()
            return true
        }
        val layoutManager = layoutManager
        if (recyclerView == null || layoutManager == null) return false
        val focusedChild = RecyclerViewUtils.findRecyclerViewChild(recyclerView,
                layoutManager.focusedChild)
        var position = -1
        if (focusedChild != null && focusedChild.parent === recyclerView) {
            position = recyclerView.getChildLayoutPosition(focusedChild)
        }
        if (position != -1) {
            val status = adapter!!.getStatus(position) ?: return false
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                IntentUtils.openStatus(activity, status, null)
                return true
            }
            if (action == null) {
                action = handler.getKeyAction(KeyboardShortcutConstants.CONTEXT_TAG_STATUS, keyCode, event, metaState)
            }
            if (action == null) return false
            when (action) {
                KeyboardShortcutConstants.ACTION_STATUS_REPLY -> {
                    val intent = Intent(IntentConstants.INTENT_ACTION_REPLY)
                    intent.putExtra(IntentConstants.EXTRA_STATUS, status)
                    startActivity(intent)
                    return true
                }
                KeyboardShortcutConstants.ACTION_STATUS_RETWEET -> {
                    RetweetQuoteDialogFragment.show(fragmentManager, status)
                    return true
                }
                KeyboardShortcutConstants.ACTION_STATUS_FAVORITE -> {
                    val twitter = twitterWrapper
                    if (status.is_favorite) {
                        twitter.destroyFavoriteAsync(status.account_key, status.id)
                    } else {
                        val holder = recyclerView.findViewHolderForLayoutPosition(position) as StatusViewHolder
                        holder.playLikeAnimation(DefaultOnLikedListener(twitter, status))
                    }
                    return true
                }
            }
        }
        return navigationHelper!!.handleKeyboardShortcutSingle(handler, keyCode, event, metaState)
    }

    override fun isKeyboardShortcutHandled(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        var action = handler.getKeyAction(KeyboardShortcutConstants.CONTEXT_TAG_NAVIGATION, keyCode, event, metaState)
        if (KeyboardShortcutConstants.ACTION_NAVIGATION_REFRESH == action) {
            return true
        }
        if (action == null) {
            action = handler.getKeyAction(KeyboardShortcutConstants.CONTEXT_TAG_STATUS, keyCode, event, metaState)
        }
        if (action == null) return false
        when (action) {
            KeyboardShortcutConstants.ACTION_STATUS_REPLY, KeyboardShortcutConstants.ACTION_STATUS_RETWEET, KeyboardShortcutConstants.ACTION_STATUS_FAVORITE -> return true
        }
        return navigationHelper!!.isKeyboardShortcutHandled(handler, keyCode, event, metaState)
    }

    override fun handleKeyboardShortcutRepeat(handler: KeyboardShortcutsHandler, keyCode: Int, repeatCount: Int,
                                              event: KeyEvent, metaState: Int): Boolean {
        return navigationHelper!!.handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState)
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<List<ParcelableStatus>?> {
        val fromUser = args.getBoolean(IntentConstants.EXTRA_FROM_USER)
        args.remove(IntentConstants.EXTRA_FROM_USER)
        return onCreateStatusesLoader(activity, args, fromUser)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if (isVisibleToUser) {
            saveReadPosition()
        }
    }

    override fun onLoadFinished(loader: Loader<List<ParcelableStatus>?>, data: List<ParcelableStatus>?) {
        val adapter = adapter ?: return
        val rememberPosition = preferences.getBoolean(SharedPreferenceConstants.KEY_REMEMBER_POSITION, false)
        val readFromBottom = preferences.getBoolean(SharedPreferenceConstants.KEY_READ_FROM_BOTTOM, false)
        var lastReadPositionKey: Long
        val lastVisiblePos: Int
        val lastVisibleTop: Int
        val tag = currentReadPositionTag
        val layoutManager = layoutManager
        if (readFromBottom) {
            lastVisiblePos = layoutManager!!.findLastVisibleItemPosition()
        } else {
            lastVisiblePos = layoutManager!!.findFirstVisibleItemPosition()
        }
        if (lastVisiblePos != RecyclerView.NO_POSITION && lastVisiblePos < adapter.itemCount) {
            val statusStartIndex = adapter.statusStartIndex
            val statusEndIndex = statusStartIndex + adapter.statusCount
            val lastItemIndex = Math.min(statusEndIndex, lastVisiblePos)
            lastReadPositionKey = adapter.getStatusPositionKey(lastItemIndex)
            val positionView = layoutManager.findViewByPosition(lastItemIndex)
            lastVisibleTop = if (positionView != null) positionView.top else 0
        } else if (rememberPosition && tag != null) {
            lastReadPositionKey = readStateManager.getPosition(tag)
            lastVisibleTop = 0
        } else {
            lastReadPositionKey = -1
            lastVisibleTop = 0
        }
        adapter.setData(data)
        val statusStartIndex = adapter.statusStartIndex
        // The last status is statusEndExclusiveIndex - 1
        val statusEndExclusiveIndex = statusStartIndex + adapter.statusCount
        if (statusEndExclusiveIndex >= 0 && rememberPosition && tag != null) {
            val lastPositionKey = adapter.getStatusPositionKey(statusEndExclusiveIndex - 1)
            // Status corresponds to last read id was deleted, use last item id instead
            if (lastPositionKey != -1L && lastReadPositionKey > 0 && lastReadPositionKey < lastPositionKey) {
                lastReadPositionKey = lastPositionKey
            }
        }
        refreshEnabled = true
        if (loader !is IExtendedLoader || loader.isFromUser) {
            if (hasMoreData(data)) {
                adapter.loadMoreSupportedPosition = ILoadMoreSupportAdapter.END
                onHasMoreDataChanged(true)
            } else {
                adapter.loadMoreSupportedPosition = ILoadMoreSupportAdapter.NONE
                onHasMoreDataChanged(false)
            }
            var pos = -1
            for (i in statusStartIndex..statusEndExclusiveIndex - 1) {
                // Assume statuses are descend sorted by id, so break at first status with id
                // lesser equals than read position
                if (lastReadPositionKey != -1L && adapter.getStatusPositionKey(i) <= lastReadPositionKey) {
                    pos = i
                    break
                }
            }
            if (pos != -1 && adapter.isStatus(pos) && (readFromBottom || lastVisiblePos != 0)) {
                if (layoutManager.height == 0) {
                    // RecyclerView has not currently laid out, ignore padding.
                    layoutManager.scrollToPositionWithOffset(pos, lastVisibleTop)
                } else {
                    layoutManager.scrollToPositionWithOffset(pos, lastVisibleTop - layoutManager.paddingTop)
                }
            }
        } else {
            onHasMoreDataChanged(false)
        }
        if (loader is IExtendedLoader) {
            loader.isFromUser = false
        }
        onStatusesLoaded(loader, data)
    }

    override fun onLoaderReset(loader: Loader<List<ParcelableStatus>?>) {
        if (loader is IExtendedLoader) {
            loader.isFromUser = false
        }
    }

    override fun onGapClick(holder: GapViewHolder, position: Int) {
        val adapter = adapter
        val status = adapter!!.getStatus(position)
        if (BuildConfig.DEBUG) {
            Log.v(TwittnukerConstants.LOGTAG, "Load activity gap " + status!!)
        }
        if (status == null) return
        val accountIds = arrayOf(status.account_key)
        val maxIds = arrayOf(status.id)
        val maxSortIds = longArrayOf(status.sort_id)
        getStatuses(BaseRefreshTaskParam(accountIds, maxIds, null, maxSortIds, null))
    }

    override fun onMediaClick(holder: IStatusViewHolder, view: View, media: ParcelableMedia, statusPosition: Int) {
        val adapter = adapter ?: return
        val status = adapter.getStatus(statusPosition) ?: return
        IntentUtils.openMedia(activity, status, media, null,
                preferences.getBoolean(SharedPreferenceConstants.KEY_NEW_DOCUMENT_API))
    }

    override fun onItemActionClick(holder: RecyclerView.ViewHolder, id: Int, position: Int) {
        val context = context ?: return
        val adapter = adapter
        val status = adapter!!.getStatus(position) ?: return
        handleStatusActionClick(context, fragmentManager, twitterWrapper,
                holder as StatusViewHolder, status, id)
    }


    override fun createItemDecoration(context: Context, recyclerView: RecyclerView, layoutManager: LinearLayoutManager): RecyclerView.ItemDecoration? {
        val adapter = adapter
        val itemDecoration = DividerItemDecoration(context,
                (recyclerView.layoutManager as LinearLayoutManager).orientation)
        val res = context.resources
        if (adapter!!.profileImageEnabled) {
            val decorPaddingLeft = res.getDimensionPixelSize(R.dimen.element_spacing_normal) * 2 + res.getDimensionPixelSize(R.dimen.icon_size_status_profile_image)
            itemDecoration.setPadding { position, rect ->
                val itemViewType = adapter.getItemViewType(position)
                var nextItemIsStatus = false
                if (position < adapter.itemCount - 1) {
                    nextItemIsStatus = adapter.getItemViewType(position + 1) == ParcelableStatusesAdapter.ITEM_VIEW_TYPE_STATUS
                }
                if (nextItemIsStatus && itemViewType == ParcelableStatusesAdapter.ITEM_VIEW_TYPE_STATUS) {
                    rect.left = decorPaddingLeft
                } else {
                    rect.left = 0
                }
                true
            }
        }
        itemDecoration.setDecorationEndOffset(1)
        return itemDecoration
    }

    protected fun saveReadPosition() {
        val layoutManager = layoutManager
        if (layoutManager != null) {
            saveReadPosition(layoutManager.findFirstVisibleItemPosition())
        }
    }

    protected open fun onHasMoreDataChanged(hasMoreData: Boolean) {
    }

    override fun onStatusClick(holder: IStatusViewHolder, position: Int) {
        val adapter = adapter
        IntentUtils.openStatus(activity, adapter!!.getStatus(position)!!, null)
    }

    override fun onStatusLongClick(holder: IStatusViewHolder, position: Int): Boolean {
        //TODO handle long click event
        return true
    }

    override fun onItemMenuClick(holder: RecyclerView.ViewHolder, menuView: View, position: Int) {
        if (activity == null) return
        val view = layoutManager?.findViewByPosition(position) ?: return
        recyclerView.showContextMenuForChild(view)
    }

    override fun onUserProfileClick(holder: IStatusViewHolder, position: Int) {
        val status = adapter!!.getStatus(position)
        val intent = IntentUtils.userProfile(status!!.account_key, status.user_key,
                status.user_screen_name, Referral.TIMELINE_STATUS,
                status.extras.user_statusnet_profile_url)
        IntentUtils.applyNewDocument(intent, preferences.getBoolean(SharedPreferenceConstants.KEY_NEW_DOCUMENT_API))
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        recyclerView.addOnScrollListener(onScrollListener)
        recyclerView.addOnScrollListener(pauseOnScrollListener)
        bus.register(statusesBusCallback)
    }

    override fun onStop() {
        bus.unregister(statusesBusCallback)
        recyclerView.removeOnScrollListener(pauseOnScrollListener)
        recyclerView.removeOnScrollListener(onScrollListener)
        if (userVisibleHint) {
            saveReadPosition()
        }
        super.onStop()
    }

    override fun onDestroy() {
        val adapter = adapter
        adapter!!.statusClickListener = null
        super.onDestroy()
    }

    override fun scrollToStart(): Boolean {
        val result = super.scrollToStart()
        if (result) {
            saveReadPosition(0)
        }
        return result
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //no reverse
        //        scrollListener?.setReversed(preferences.getBoolean(SharedPreferenceConstants.KEY_READ_FROM_BOTTOM))
        val adapter = adapter!!
        adapter.statusClickListener = this
        registerForContextMenu(recyclerView)
        navigationHelper = RecyclerViewNavigationHelper(recyclerView, layoutManager!!, adapter, this)
        pauseOnScrollListener = PauseRecyclerViewOnScrollListener(adapter.mediaLoader.imageLoader, false, true)

        val loaderArgs = Bundle(arguments)
        loaderArgs.putBoolean(IntentConstants.EXTRA_FROM_USER, true)
        loaderManager.initLoader(0, loaderArgs, this)
        showProgress()
    }

    protected open fun createMessageBusCallback(): Any {
        return StatusesBusCallback()
    }

    protected abstract val accountKeys: Array<UserKey>

    protected var adapterData: List<ParcelableStatus>?
        get() {
            return adapter?.getData()
        }
        set(data) {
            adapter?.setData(data)
        }

    protected open val readPositionTag: String?
        @ReadPositionTag
        get() = null

    protected open val readPositionTagWithArguments: String?
        get() = readPositionTag

    protected abstract fun hasMoreData(data: List<ParcelableStatus>?): Boolean

    protected abstract fun onCreateStatusesLoader(context: Context, args: Bundle,
                                                  fromUser: Boolean): Loader<List<ParcelableStatus>?>

    protected abstract fun onStatusesLoaded(loader: Loader<List<ParcelableStatus>?>, data: List<ParcelableStatus>?)

    protected fun saveReadPosition(position: Int) {
        val readPositionTag = readPositionTagWithAccounts ?: return
        if (position == RecyclerView.NO_POSITION) return
        val adapter = adapter
        val status = adapter!!.getStatus(position) ?: return
        val positionKey = if (status.position_key > 0) status.position_key else status.timestamp
        readStateManager.setPosition(readPositionTag, positionKey)
        val accountKeys = accountKeys
        if (accountKeys.size > 1) {
            for (accountKey in accountKeys) {
                val tag = Utils.getReadPositionTagWithAccounts(readPositionTagWithArguments,
                        accountKey)
                readStateManager.setPosition(tag, positionKey)
            }
        }
        readStateManager.setPosition(currentReadPositionTag, positionKey, true)
    }

    override val extraContentPadding: Rect
        get() {
            val paddingVertical = resources.getDimensionPixelSize(R.dimen.element_spacing_small)
            return Rect(0, paddingVertical, 0, paddingVertical)
        }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        if (!userVisibleHint || menuInfo == null) return
        val adapter = adapter
        val inflater = MenuInflater(context)
        val contextMenuInfo = menuInfo as ExtendedRecyclerView.ContextMenuInfo?
        val status = adapter!!.getStatus(contextMenuInfo!!.position)
        inflater.inflate(R.menu.action_status, menu)
        MenuUtils.setupForStatus(context, preferences, menu, status!!, twitterWrapper)
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        if (!userVisibleHint) return false
        val contextMenuInfo = item!!.menuInfo as ExtendedRecyclerView.ContextMenuInfo
        val status = adapter!!.getStatus(contextMenuInfo.position) ?: return false
        if (item.itemId == R.id.share) {
            val shareIntent = Utils.createStatusShareIntent(activity, status)
            val chooser = Intent.createChooser(shareIntent, getString(R.string.share_status))
            Utils.addCopyLinkIntent(context, chooser, LinkCreator.getStatusWebLink(status))
            startActivity(chooser)
            return true
        }
        return MenuUtils.handleStatusClick(activity, this, fragmentManager,
                userColorNameManager, twitterWrapper, status, item)
    }

    private val currentReadPositionTag: String?
        get() {
            val tag = readPositionTagWithAccounts ?: return null
            return tag + "_current"
        }

    private val readPositionTagWithAccounts: String?
        get() = Utils.getReadPositionTagWithAccounts(readPositionTagWithArguments, *accountKeys)

    class DefaultOnLikedListener(
            private val twitter: AsyncTwitterWrapper,
            private val status: ParcelableStatus
    ) : LikeAnimationDrawable.OnLikedListener {

        override fun onLiked(): Boolean {
            if (status.is_favorite) return false
            twitter.createFavoriteAsync(status.account_key, status.id)
            return true
        }
    }

    protected inner class StatusesBusCallback {

        @Subscribe
        fun notifyStatusListChanged(event: StatusListChangedEvent) {
            val adapter = adapter
            adapter!!.notifyDataSetChanged()
        }

    }

    companion object {

        fun handleStatusActionClick(context: Context, fm: FragmentManager,
                                    twitter: AsyncTwitterWrapper?, holder: StatusViewHolder,
                                    status: ParcelableStatus?, id: Int) {

            if (status == null) return
            when (id) {
                R.id.reply -> {
                    val intent = Intent(IntentConstants.INTENT_ACTION_REPLY)
                    intent.`package` = context.packageName
                    intent.putExtra(IntentConstants.EXTRA_STATUS, status)
                    context.startActivity(intent)
                }
                R.id.retweet -> {
                    RetweetQuoteDialogFragment.show(fm, status)
                }
                R.id.favorite -> {
                    if (twitter == null) return
                    if (status.is_favorite) {
                        twitter.destroyFavoriteAsync(status.account_key, status.id)
                    } else {
                        holder.playLikeAnimation(DefaultOnLikedListener(twitter,
                                status))
                    }
                }
            }
        }
    }
}