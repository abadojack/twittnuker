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

package de.vanita5.twittnuker.app

import android.app.Application
import android.content.*
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Color
import android.os.AsyncTask
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.multidex.MultiDex
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.ActionBarContextView
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.appthemeengine.ATE
import com.afollestad.appthemeengine.Config
import com.pnikosis.materialishprogress.ProgressWheel
import com.rengwuxian.materialedittext.MaterialEditText
import nl.komponents.kovenant.android.startKovenant
import nl.komponents.kovenant.android.stopKovenant
import nl.komponents.kovenant.task
import org.apache.commons.lang3.ArrayUtils
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.ktextension.configure
import org.mariotaku.restfu.http.RestHttpClient
import de.vanita5.twittnuker.BuildConfig
import de.vanita5.twittnuker.Constants
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.TwittnukerConstants.*
import de.vanita5.twittnuker.activity.AssistLauncherActivity
import de.vanita5.twittnuker.activity.MainActivity
import de.vanita5.twittnuker.constant.defaultFeatureLastUpdated
import de.vanita5.twittnuker.model.DefaultFeatures
import de.vanita5.twittnuker.service.RefreshService
import de.vanita5.twittnuker.util.*
import de.vanita5.twittnuker.util.content.TwidereSQLiteOpenHelper
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper
import de.vanita5.twittnuker.util.net.TwidereDns
import de.vanita5.twittnuker.util.theme.*
import de.vanita5.twittnuker.view.ProfileImageView
import de.vanita5.twittnuker.view.TabPagerIndicator
import de.vanita5.twittnuker.view.ThemedMultiValueSwitch
import de.vanita5.twittnuker.view.TimelineContentTextView
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TwittnukerApplication : Application(), Constants, OnSharedPreferenceChangeListener {

    @Inject
    lateinit internal var activityTracker: ActivityTracker
    @Inject
    lateinit internal var restHttpClient: RestHttpClient
    @Inject
    lateinit internal var dns: TwidereDns
    @Inject
    lateinit internal var defaultFeatures: DefaultFeatures
    @Inject
    lateinit internal var externalThemeManager: ExternalThemeManager
    @Inject
    lateinit internal var kPreferences: KPreferences

    var handler: Handler? = null
        private set

    private var profileImageViewViewProcessor: ProfileImageViewViewProcessor? = null
    private var fontFamilyTagProcessor: FontFamilyTagProcessor? = null

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    fun initKeyboardShortcuts() {
        val preferences = sharedPreferences
        if (!preferences.getBoolean(KEY_KEYBOARD_SHORTCUT_INITIALIZED, false)) {
            //            getApplicationModule().getKeyboardShortcutsHandler().reset();
            preferences.edit().putBoolean(KEY_KEYBOARD_SHORTCUT_INITIALIZED, true).apply()
        }
    }


    val sqLiteDatabase: SQLiteDatabase by lazy {
        StrictModeUtils.checkDiskIO()
        sqLiteOpenHelper.writableDatabase
    }

    val sqLiteOpenHelper: SQLiteOpenHelper by lazy {
        TwidereSQLiteOpenHelper(this, Constants.DATABASES_NAME, Constants.DATABASES_VERSION)
    }

    override fun onCreate() {
        instance = this
        if (BuildConfig.DEBUG) {
            StrictModeUtils.detectAllVmPolicy()
        }
        val preferences = sharedPreferences
        resetTheme(preferences)
        super.onCreate()
        startKovenant()
        initAppThemeEngine(preferences)
        initializeAsyncTask()
        initDebugMode()
        initBugReport()
        handler = Handler()

        Utils.startRefreshServiceIfNeeded(this)

        GeneralComponentHelper.build(this).inject(this)

        registerActivityLifecycleCallbacks(activityTracker)

        listenExternalThemeChange()

        loadDefaultFeatures()
    }

    private fun loadDefaultFeatures() {
        val lastUpdated = kPreferences[defaultFeatureLastUpdated]
        if (lastUpdated > 0 && TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - lastUpdated) < 12) {
            return
        }
        task {
            defaultFeatures.loadRemoteSettings(restHttpClient)
        }.success {
            if (BuildConfig.DEBUG) {
                Log.d(LOGTAG, "Loaded remote features")
            }
        }.fail {
            if (BuildConfig.DEBUG) {
                Log.w(LOGTAG, "Unable to load remote features", it)
            }
        }.always {
            kPreferences[defaultFeatureLastUpdated] = System.currentTimeMillis()
        }
    }

    private fun listenExternalThemeChange() {
        val packageFilter = IntentFilter()
        packageFilter.addAction(Intent.ACTION_PACKAGE_CHANGED)
        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        packageFilter.addAction(Intent.ACTION_PACKAGE_REPLACED)
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val uid = intent.getIntExtra(Intent.EXTRA_UID, -1)
                val packages = packageManager.getPackagesForUid(uid)
                val manager = externalThemeManager
                if (ArrayUtils.contains(packages, manager.emojiPackageName)) {
                    manager.reloadEmojiPreferences()
                }
            }
        }, packageFilter)
    }

    private fun initAppThemeEngine(preferences: SharedPreferences) {

        profileImageViewViewProcessor = configure(ProfileImageViewViewProcessor()) {
            setStyle(Utils.getProfileImageStyle(preferences))
        }
        fontFamilyTagProcessor = configure(FontFamilyTagProcessor()) {
            setFontFamily(ThemeUtils.getThemeFontFamily(preferences))
        }

        ATE.registerViewProcessor(TabPagerIndicator::class.java, TabPagerIndicatorViewProcessor())
        ATE.registerViewProcessor(FloatingActionButton::class.java, FloatingActionButtonViewProcessor())
        ATE.registerViewProcessor(ActionBarContextView::class.java, ActionBarContextViewViewProcessor())
        ATE.registerViewProcessor(SwipeRefreshLayout::class.java, SwipeRefreshLayoutViewProcessor())
        ATE.registerViewProcessor(TimelineContentTextView::class.java, TimelineContentTextViewViewProcessor())
        ATE.registerViewProcessor(TextView::class.java, TextViewViewProcessor())
        ATE.registerViewProcessor(ImageView::class.java, ImageViewViewProcessor())
        ATE.registerViewProcessor(MaterialEditText::class.java, MaterialEditTextViewProcessor())
        ATE.registerViewProcessor(ProgressWheel::class.java, ProgressWheelViewProcessor())
        ATE.registerViewProcessor(ProfileImageView::class.java, profileImageViewViewProcessor!!)
        ATE.registerTagProcessor(OptimalLinkColorTagProcessor.TAG, OptimalLinkColorTagProcessor())
        ATE.registerTagProcessor(FontFamilyTagProcessor.TAG, fontFamilyTagProcessor!!)
        ATE.registerTagProcessor(IconActionButtonTagProcessor.PREFIX_COLOR,
                IconActionButtonTagProcessor(IconActionButtonTagProcessor.PREFIX_COLOR))
        ATE.registerTagProcessor(IconActionButtonTagProcessor.PREFIX_COLOR_ACTIVATED,
                IconActionButtonTagProcessor(IconActionButtonTagProcessor.PREFIX_COLOR_ACTIVATED))
        ATE.registerTagProcessor(IconActionButtonTagProcessor.PREFIX_COLOR_DISABLED,
                IconActionButtonTagProcessor(IconActionButtonTagProcessor.PREFIX_COLOR_DISABLED))
        ATE.registerTagProcessor(ThemedMultiValueSwitch.PREFIX_TINT, ThemedMultiValueSwitch.TintTagProcessor())


        val themeColor = preferences.getInt(KEY_THEME_COLOR, ContextCompat.getColor(this,
                R.color.branding_color))
        if (!ATE.config(this, VALUE_THEME_NAME_LIGHT).isConfigured) {
            //noinspection WrongConstant
            ATE.config(this, VALUE_THEME_NAME_LIGHT).primaryColor(themeColor).accentColor(ThemeUtils.getOptimalAccentColor(themeColor, Color.BLACK)).coloredActionBar(true).coloredStatusBar(true).commit()
        }
        if (!ATE.config(this, VALUE_THEME_NAME_DARK).isConfigured) {
            ATE.config(this, VALUE_THEME_NAME_DARK).accentColor(ThemeUtils.getOptimalAccentColor(themeColor, Color.WHITE)).coloredActionBar(false).coloredStatusBar(true).statusBarColor(Color.BLACK).commit()
        }
        if (!ATE.config(this, null).isConfigured) {
            ATE.config(this, null).accentColor(ThemeUtils.getOptimalAccentColor(themeColor, Color.WHITE)).coloredActionBar(false).coloredStatusBar(false).commit()
        }
    }

    private fun initDebugMode() {
        DebugModeUtils.initForApplication(this)
    }

    private fun initBugReport() {
        val preferences = sharedPreferences
        if (!preferences.getBoolean(KEY_BUG_REPORTS, BuildConfig.DEBUG)) return
        BugReporter.setImplementation(TwidereBugReporter())
        BugReporter.init(this)
    }

    private val sharedPreferences: SharedPreferences by lazy {
        val prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener(this)
        prefs
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        when (key) {
            KEY_REFRESH_INTERVAL -> {
                stopService(Intent(this, RefreshService::class.java))
                Utils.startRefreshServiceIfNeeded(this)
            }
            KEY_ENABLE_PROXY, KEY_PROXY_HOST, KEY_PROXY_PORT, KEY_PROXY_TYPE, KEY_PROXY_USERNAME, KEY_PROXY_PASSWORD, KEY_CONNECTION_TIMEOUT, KEY_RETRY_ON_NETWORK_ISSUE -> {
                HttpClientFactory.reloadConnectivitySettings(this)
            }
            KEY_DNS_SERVER, KEY_TCP_DNS_QUERY, KEY_BUILTIN_DNS_RESOLVER -> {
                reloadDnsSettings()
            }
            KEY_CONSUMER_KEY, KEY_CONSUMER_SECRET, KEY_API_URL_FORMAT, KEY_AUTH_TYPE, KEY_SAME_OAUTH_SIGNING_URL -> {
                val editor = preferences.edit()
                editor.putLong(KEY_API_LAST_CHANGE, System.currentTimeMillis())
                editor.apply()
            }
            KEY_EMOJI_SUPPORT -> {
                externalThemeManager.reloadEmojiPreferences()
            }
            KEY_THEME -> {
                resetTheme(preferences)
                Config.markChanged(this, VALUE_THEME_NAME_LIGHT, VALUE_THEME_NAME_DARK)
            }
            KEY_THEME_BACKGROUND -> {
                Config.markChanged(this, VALUE_THEME_NAME_LIGHT, VALUE_THEME_NAME_DARK)
            }
            KEY_PROFILE_IMAGE_STYLE -> {
                Config.markChanged(this, VALUE_THEME_NAME_LIGHT, VALUE_THEME_NAME_DARK)
                profileImageViewViewProcessor!!.setStyle(Utils.getProfileImageStyle(preferences.getString(key, null)))
            }
            KEY_THEME_FONT_FAMILY -> {
                Config.markChanged(this, VALUE_THEME_NAME_LIGHT, VALUE_THEME_NAME_DARK)
                fontFamilyTagProcessor!!.setFontFamily(ThemeUtils.getThemeFontFamily(preferences))
            }
            KEY_THEME_COLOR -> {
                val themeColor = preferences.getInt(key, ContextCompat.getColor(this,
                        R.color.branding_color))
                //noinspection WrongConstant
                ATE.config(this, VALUE_THEME_NAME_LIGHT).primaryColor(themeColor).accentColor(ThemeUtils.getOptimalAccentColor(themeColor, Color.BLACK)).coloredActionBar(true).coloredStatusBar(true).commit()
                ATE.config(this, VALUE_THEME_NAME_DARK).accentColor(ThemeUtils.getOptimalAccentColor(themeColor, Color.WHITE)).coloredActionBar(false).coloredStatusBar(true).statusBarColor(Color.BLACK).commit()
                ATE.config(this, null).accentColor(ThemeUtils.getOptimalAccentColor(themeColor, Color.BLACK)).coloredActionBar(false).coloredStatusBar(false).commit()
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        stopKovenant()
    }

    private fun resetTheme(preferences: SharedPreferences) {
        when (ThemeUtils.getLocalNightMode(preferences)) {
            AppCompatDelegate.MODE_NIGHT_AUTO -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
            }
            AppCompatDelegate.MODE_NIGHT_YES -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun reloadDnsSettings() {
        dns.reloadDnsSettings()
    }


    private fun initializeAsyncTask() {
        // AsyncTask class needs to be loaded in UI thread.
        // So we load it here to comply the rule.
        try {
            Class.forName(AsyncTask::class.java.name)
        } catch (ignore: ClassNotFoundException) {
        }

    }

    companion object {

        private val KEY_KEYBOARD_SHORTCUT_INITIALIZED = "keyboard_shortcut_initialized"
        var instance: TwittnukerApplication? = null
            private set

        fun getInstance(context: Context): TwittnukerApplication {
            return context.applicationContext as TwittnukerApplication
        }
    }
}