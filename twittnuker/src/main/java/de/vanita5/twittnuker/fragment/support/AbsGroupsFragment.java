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

package de.vanita5.twittnuker.fragment.support;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;

import de.vanita5.twittnuker.adapter.AbsGroupsAdapter;
import de.vanita5.twittnuker.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition;
import de.vanita5.twittnuker.loader.iface.IExtendedLoader;
import de.vanita5.twittnuker.loader.support.iface.ICursorSupportLoader;
import de.vanita5.twittnuker.model.ParcelableUserList;
import de.vanita5.twittnuker.util.KeyboardShortcutsHandler;
import de.vanita5.twittnuker.util.KeyboardShortcutsHandler.KeyboardShortcutCallback;
import de.vanita5.twittnuker.util.RecyclerViewNavigationHelper;
import de.vanita5.twittnuker.view.holder.GroupViewHolder;

abstract class AbsGroupsFragment<Data> extends AbsContentListRecyclerViewFragment<AbsGroupsAdapter<Data>>
        implements LoaderCallbacks<Data>, AbsGroupsAdapter.GroupAdapterListener, KeyboardShortcutCallback {

    private RecyclerViewNavigationHelper mNavigationHelper;

    private long mNextCursor;
    private long mPrevCursor;

    public final Data getData() {
        return getAdapter().getData();
    }

    @Override
    public boolean handleKeyboardShortcutSingle(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        return mNavigationHelper.handleKeyboardShortcutSingle(handler, keyCode, event, metaState);
    }

    @Override
    public boolean handleKeyboardShortcutRepeat(@NonNull KeyboardShortcutsHandler handler, int keyCode, int repeatCount, @NonNull KeyEvent event, int metaState) {
        return mNavigationHelper.handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState);
    }

    @Override
    public boolean isKeyboardShortcutHandled(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        return mNavigationHelper.isKeyboardShortcutHandled(handler, keyCode, event, metaState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final AbsGroupsAdapter<Data> adapter = getAdapter();
        final RecyclerView recyclerView = getRecyclerView();
        final LinearLayoutManager layoutManager = getLayoutManager();
        adapter.setListener(this);

        mNavigationHelper = new RecyclerViewNavigationHelper(recyclerView, layoutManager, adapter,
                this);
        final Bundle loaderArgs = new Bundle(getArguments());
        loaderArgs.putBoolean(EXTRA_FROM_USER, true);
        getLoaderManager().initLoader(0, loaderArgs, this);
    }

    @Override
    public final Loader<Data> onCreateLoader(int id, Bundle args) {
        final boolean fromUser = args.getBoolean(EXTRA_FROM_USER);
        args.remove(EXTRA_FROM_USER);
        return onCreateUserListsLoader(getActivity(), args, fromUser);
    }

    @Override
    public void onLoadFinished(Loader<Data> loader, Data data) {
        final AbsGroupsAdapter<Data> adapter = getAdapter();
        adapter.setData(data);
        if (!(loader instanceof IExtendedLoader) || ((IExtendedLoader) loader).isFromUser()) {
            adapter.setLoadMoreSupportedPosition(hasMoreData(data) ? IndicatorPosition.END : IndicatorPosition.NONE);
            setRefreshEnabled(true);
        }
        if (loader instanceof IExtendedLoader) {
            ((IExtendedLoader) loader).setFromUser(false);
        }
        if (loader instanceof ICursorSupportLoader) {
            mNextCursor = ((ICursorSupportLoader) loader).getNextCursor();
            mPrevCursor = ((ICursorSupportLoader) loader).getNextCursor();
        }
        showContent();
    }

    @Override
    public void onLoaderReset(Loader<Data> loader) {
        if (loader instanceof IExtendedLoader) {
            ((IExtendedLoader) loader).setFromUser(false);
        }
    }

    @Override
    public void onGroupClick(GroupViewHolder holder, int position) {

    }

    @Override
    public boolean onGroupLongClick(GroupViewHolder holder, int position) {
        return false;
    }

    public long getPrevCursor() {
        return mPrevCursor;
    }

    public long getNextCursor() {
        return mNextCursor;
    }


    protected ParcelableUserList getSelectedUserList() {
        //TODO return selected
        return null;
    }

    protected abstract boolean hasMoreData(Data data);

    protected abstract Loader<Data> onCreateUserListsLoader(Context context, Bundle args, boolean fromUser);

}