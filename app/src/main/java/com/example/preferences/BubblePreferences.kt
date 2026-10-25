package com.example.preferences

import android.content.Context
import android.content.SharedPreferences

class BubblePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("music_bubble_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_BUBBLE_SIZE = "bubble_size_dp"
        private const val KEY_BUBBLE_ALPHA = "bubble_alpha"
        private const val KEY_PANEL_ALPHA = "panel_alpha"
        private const val KEY_AUTO_SNAP = "auto_snap"
        private const val KEY_VIBRATE = "vibrate_on_tap"
        private const val KEY_LAST_X = "last_pos_x"
        private const val KEY_LAST_Y = "last_pos_y"

        const val DEFAULT_BUBBLE_SIZE = 60
        const val DEFAULT_BUBBLE_ALPHA = 0.95f
        const val DEFAULT_PANEL_ALPHA = 0.92f
    }

    var bubbleSizeDp: Int
        get() = prefs.getInt(KEY_BUBBLE_SIZE, DEFAULT_BUBBLE_SIZE)
        set(value) = prefs.edit().putInt(KEY_BUBBLE_SIZE, value).apply()

    var bubbleAlpha: Float
        get() = prefs.getFloat(KEY_BUBBLE_ALPHA, DEFAULT_BUBBLE_ALPHA)
        set(value) = prefs.edit().putFloat(KEY_BUBBLE_ALPHA, value).apply()

    var panelAlpha: Float
        get() = prefs.getFloat(KEY_PANEL_ALPHA, DEFAULT_PANEL_ALPHA)
        set(value) = prefs.edit().putFloat(KEY_PANEL_ALPHA, value).apply()

    var autoSnap: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SNAP, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SNAP, value).apply()

    var vibrateOnTap: Boolean
        get() = prefs.getBoolean(KEY_VIBRATE, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATE, value).apply()

    var lastX: Int
        get() = prefs.getInt(KEY_LAST_X, 100)
        set(value) = prefs.edit().putInt(KEY_LAST_X, value).apply()

    var lastY: Int
        get() = prefs.getInt(KEY_LAST_Y, 300)
        set(value) = prefs.edit().putInt(KEY_LAST_Y, value).apply()
}
