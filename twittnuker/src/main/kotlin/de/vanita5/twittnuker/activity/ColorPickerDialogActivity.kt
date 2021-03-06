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

package de.vanita5.twittnuker.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import de.vanita5.twittnuker.constant.IntentConstants.*
import de.vanita5.twittnuker.fragment.ColorPickerDialogFragment
import de.vanita5.twittnuker.fragment.ColorPickerDialogFragment.Callback

class ColorPickerDialogActivity : BaseActivity(), Callback {

    override fun onCancelled() {
        finish()
    }

    override fun onStart() {
        super.onStart()
        setVisible(true)
    }

    override fun onColorCleared() {
        setResult(RESULT_CLEARED)
        finish()
    }

    override fun onColorSelected(color: Int) {
        val intent = Intent()
        intent.putExtra(EXTRA_COLOR, color)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onDismissed() {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val intent = intent
            val f = ColorPickerDialogFragment()
            val args = Bundle()
            args.putInt(EXTRA_COLOR, intent.getIntExtra(EXTRA_COLOR, Color.WHITE))
            args.putBoolean(EXTRA_CLEAR_BUTTON, intent.getBooleanExtra(EXTRA_CLEAR_BUTTON, false))
            args.putBoolean(EXTRA_ALPHA_SLIDER, intent.getBooleanExtra(EXTRA_ALPHA_SLIDER, true))
            f.arguments = args
            f.show(supportFragmentManager, "color_picker_dialog")
        }
    }

    companion object {

        val RESULT_CLEARED = -2
    }

}