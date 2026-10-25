package com.example.service

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.os.VibratorManager
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.example.model.MediaTrackState
import com.example.preferences.BubblePreferences
import kotlin.math.hypot

/**
 * Manages the floating WindowManager views (both compact Bubble and expanded Mini Player card)
 * with real touch drag physics, edge snapping, live media updates, and transparency controls.
 */
class BubbleOverlayManager(
    private val context: Context,
    private val onCloseRequested: () -> Unit
) {
    companion object {
        private const val TAG = "BubbleOverlayManager"
    }

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val preferences = BubblePreferences(context)
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val mainHandler = Handler(Looper.getMainLooper())

    private var rootContainer: FrameLayout? = null
    private var bubbleContainer: FrameLayout? = null
    private var panelContainer: FrameLayout? = null

    // Bubble elements
    private var bubbleArtImageView: ImageView? = null
    private var bubblePlayBadge: ImageView? = null

    // Panel elements
    private var panelArtImageView: ImageView? = null
    private var titleTextView: TextView? = null
    private var artistTextView: TextView? = null
    private var sourceBadgeTextView: TextView? = null
    private var playPauseBtn: ImageView? = null
    private var prevBtn: ImageView? = null
    private var nextBtn: ImageView? = null
    private var alphaSeekBar: SeekBar? = null
    private var alphaValueTextView: TextView? = null

    private var isExpanded = false
    private var currentTrackState = MediaTrackState()

    private val windowParams: WindowManager.LayoutParams = WindowManager.LayoutParams().apply {
        type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        format = PixelFormat.TRANSLUCENT
        flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        gravity = Gravity.TOP or Gravity.START
        x = preferences.lastX
        y = preferences.lastY
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
    }

    fun show() {
        if (rootContainer != null) return

        try {
            buildViews()
            windowManager.addView(rootContainer, windowParams)
            updateMediaState(MediaSessionHub.mediaState.value)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add overlay window", e)
        }
    }

    fun hide() {
        if (rootContainer != null) {
            try {
                preferences.lastX = windowParams.x
                preferences.lastY = windowParams.y
                windowManager.removeView(rootContainer)
            } catch (e: Exception) {
                Log.w(TAG, "Error removing overlay view", e)
            } finally {
                rootContainer = null
                bubbleContainer = null
                panelContainer = null
            }
        }
    }

    fun updateMediaState(state: MediaTrackState) {
        currentTrackState = state
        mainHandler.post {
            applyMediaStateToViews(state)
        }
    }

    private fun buildViews() {
        val density = context.resources.displayMetrics.density
        val bubbleSizePx = (preferences.bubbleSizeDp * density).toInt()

        rootContainer = FrameLayout(context).apply {
            clipChildren = false
            clipToPadding = false
        }

        // 1. Create Compact Bubble
        bubbleContainer = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(bubbleSizePx, bubbleSizePx)
            alpha = preferences.bubbleAlpha
            elevation = 16f * density
            clipToOutline = true

            // Glowing Dark Burgundy Background with Crimson Border
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                colors = intArrayOf(Color.parseColor("#4A0E22"), Color.parseColor("#1F050D"))
                gradientType = GradientDrawable.RADIAL_GRADIENT
                gradientRadius = bubbleSizePx / 1.2f
                setStroke((2.5f * density).toInt(), Color.parseColor("#E50914"))
            }
            background = bg

            // Album art or musical note icon inside
            bubbleArtImageView = ImageView(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    val pad = (6 * density).toInt()
                    setMargins(pad, pad, pad, pad)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageResource(android.R.drawable.ic_media_play)
                setColorFilter(Color.parseColor("#FF4D6D"))
            }
            addView(bubbleArtImageView)

            // Small Play/Pause Indicator Badge in bottom-right corner
            bubblePlayBadge = ImageView(context).apply {
                val badgeSize = (20 * density).toInt()
                layoutParams = FrameLayout.LayoutParams(badgeSize, badgeSize).apply {
                    gravity = Gravity.BOTTOM or Gravity.END
                    setMargins(0, 0, (2 * density).toInt(), (2 * density).toInt())
                }
                val badgeBg = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#E50914"))
                    setStroke((1.5f * density).toInt(), Color.parseColor("#0A0709"))
                }
                background = badgeBg
                val iconPad = (4 * density).toInt()
                setPadding(iconPad, iconPad, iconPad, iconPad)
                scaleType = ImageView.ScaleType.FIT_CENTER
                setImageResource(android.R.drawable.ic_media_play)
                setColorFilter(Color.WHITE)
            }
            addView(bubblePlayBadge)

            setupBubbleTouchListener(this)
        }
        rootContainer?.addView(bubbleContainer)

        // 2. Create Expanded Mini Player Card
        panelContainer = buildMiniPlayerPanel(density)
        panelContainer?.visibility = View.GONE
        rootContainer?.addView(panelContainer)
    }

    private fun buildMiniPlayerPanel(density: Float): FrameLayout {
        val cardWidth = (300 * density).toInt()

        return FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(cardWidth, FrameLayout.LayoutParams.WRAP_CONTENT)
            alpha = preferences.panelAlpha
            elevation = 24f * density

            // Pitch Black & Burgundy Gradient with Neon Crimson stroke
            val cardBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 20f * density
                colors = intArrayOf(Color.parseColor("#250A14"), Color.parseColor("#0D0B0E"))
                gradientType = GradientDrawable.LINEAR_GRADIENT
                orientation = GradientDrawable.Orientation.TOP_BOTTOM
                setStroke((1.5f * density).toInt(), Color.parseColor("#E50914"))
            }
            background = cardBg

            val mainColumn = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                val p = (16 * density).toInt()
                setPadding(p, p, p, p)
            }

            // Top Header: App Title + Source badge + Minimize & Close buttons
            val headerRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val appLabel = TextView(context).apply {
                text = "MUSIC BUBBLE"
                setTextColor(Color.parseColor("#FF4D6D"))
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            headerRow.addView(appLabel)

            sourceBadgeTextView = TextView(context).apply {
                text = "STANDBY"
                setTextColor(Color.parseColor("#B8A4AC"))
                textSize = 9f
                val badgePadH = (6 * density).toInt()
                val badgePadV = (2 * density).toInt()
                setPadding(badgePadH, badgePadV, badgePadH, badgePadV)
                val badgeBg = GradientDrawable().apply {
                    cornerRadius = 8f * density
                    setColor(Color.parseColor("#381525"))
                }
                background = badgeBg
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, (8 * density).toInt(), 0)
                }
            }
            headerRow.addView(sourceBadgeTextView)

            // Minimize button
            val minimizeBtn = ImageView(context).apply {
                val btnSize = (28 * density).toInt()
                layoutParams = LinearLayout.LayoutParams(btnSize, btnSize).apply {
                    setMargins(0, 0, (6 * density).toInt(), 0)
                }
                val pad = (6 * density).toInt()
                setPadding(pad, pad, pad, pad)
                setImageResource(android.R.drawable.arrow_down_float)
                setColorFilter(Color.parseColor("#B8A4AC"))
                setOnClickListener {
                    collapseToBubble()
                }
            }
            headerRow.addView(minimizeBtn)

            // Close button
            val closeBtn = ImageView(context).apply {
                val btnSize = (28 * density).toInt()
                layoutParams = LinearLayout.LayoutParams(btnSize, btnSize)
                val pad = (6 * density).toInt()
                setPadding(pad, pad, pad, pad)
                setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                setColorFilter(Color.parseColor("#FF4D6D"))
                setOnClickListener {
                    performHapticFeedback()
                    onCloseRequested()
                }
            }
            headerRow.addView(closeBtn)
            mainColumn.addView(headerRow)

            // Track Information Row (Album Art + Title + Artist)
            val trackRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, (12 * density).toInt(), 0, (12 * density).toInt())
                }
            }

            val artSize = (54 * density).toInt()
            panelArtImageView = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(artSize, artSize).apply {
                    setMargins(0, 0, (12 * density).toInt(), 0)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                val artBg = GradientDrawable().apply {
                    cornerRadius = 12f * density
                    setColor(Color.parseColor("#1F050D"))
                    setStroke((1f * density).toInt(), Color.parseColor("#4A0E22"))
                }
                background = artBg
                clipToOutline = true
                setImageResource(android.R.drawable.ic_media_play)
                setColorFilter(Color.parseColor("#FF4D6D"))
            }
            trackRow.addView(panelArtImageView)

            val textColumn = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            titleTextView = TextView(context).apply {
                text = "No Active Song"
                setTextColor(Color.WHITE)
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
            }
            textColumn.addView(titleTextView)

            artistTextView = TextView(context).apply {
                text = "Play music on any player"
                setTextColor(Color.parseColor("#B8A4AC"))
                textSize = 12f
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, (2 * density).toInt(), 0, 0)
                }
            }
            textColumn.addView(artistTextView)
            trackRow.addView(textColumn)
            mainColumn.addView(trackRow)

            // Playback Controls Row (Previous, Play/Pause, Next)
            val controlsRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, (4 * density).toInt(), 0, (8 * density).toInt())
                }
            }

            prevBtn = ImageView(context).apply {
                val btnSize = (40 * density).toInt()
                layoutParams = LinearLayout.LayoutParams(btnSize, btnSize).apply {
                    setMargins(0, 0, (16 * density).toInt(), 0)
                }
                val pad = (10 * density).toInt()
                setPadding(pad, pad, pad, pad)
                setImageResource(android.R.drawable.ic_media_previous)
                setColorFilter(Color.WHITE)
                val btnBg = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#22111A"))
                }
                background = btnBg
                setOnClickListener {
                    performHapticFeedback()
                    MediaSessionHub.skipToPrevious()
                }
            }
            controlsRow.addView(prevBtn)

            playPauseBtn = ImageView(context).apply {
                val btnSize = (52 * density).toInt()
                layoutParams = LinearLayout.LayoutParams(btnSize, btnSize).apply {
                    setMargins(0, 0, (16 * density).toInt(), 0)
                }
                val pad = (14 * density).toInt()
                setPadding(pad, pad, pad, pad)
                setImageResource(android.R.drawable.ic_media_play)
                setColorFilter(Color.WHITE)
                val btnBg = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#E50914"))
                }
                background = btnBg
                elevation = 8f * density
                setOnClickListener {
                    performHapticFeedback()
                    MediaSessionHub.togglePlayPause()
                }
            }
            controlsRow.addView(playPauseBtn)

            nextBtn = ImageView(context).apply {
                val btnSize = (40 * density).toInt()
                layoutParams = LinearLayout.LayoutParams(btnSize, btnSize)
                val pad = (10 * density).toInt()
                setPadding(pad, pad, pad, pad)
                setImageResource(android.R.drawable.ic_media_next)
                setColorFilter(Color.WHITE)
                val btnBg = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#22111A"))
                }
                background = btnBg
                setOnClickListener {
                    performHapticFeedback()
                    MediaSessionHub.skipToNext()
                }
            }
            controlsRow.addView(nextBtn)
            mainColumn.addView(controlsRow)

            // Transparency Bar
            val alphaRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, (6 * density).toInt(), 0, 0)
                }
            }

            val alphaLabel = TextView(context).apply {
                text = "Opacity"
                setTextColor(Color.parseColor("#756269"))
                textSize = 10f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, (8 * density).toInt(), 0)
                }
            }
            alphaRow.addView(alphaLabel)

            alphaSeekBar = SeekBar(context).apply {
                max = 60 // 40% to 100%
                progress = ((preferences.panelAlpha - 0.4f) * 100).toInt().coerceIn(0, 60)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            val alpha = 0.4f + (progress / 100f)
                            preferences.panelAlpha = alpha
                            panelContainer?.alpha = alpha
                            alphaValueTextView?.text = "${(alpha * 100).toInt()}%"
                        }
                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
            }
            alphaRow.addView(alphaSeekBar)

            alphaValueTextView = TextView(context).apply {
                text = "${(preferences.panelAlpha * 100).toInt()}%"
                setTextColor(Color.parseColor("#756269"))
                textSize = 10f
                layoutParams = LinearLayout.LayoutParams(
                    (32 * density).toInt(),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            alphaRow.addView(alphaValueTextView)
            mainColumn.addView(alphaRow)

            addView(mainColumn)

            // Allow dragging the panel as well by touching the header
            setupPanelTouchListener(headerRow)
        }
    }

    private fun setupBubbleTouchListener(view: View) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var touchStartTime = 0L
        var isDragging = false

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = windowParams.x
                    initialY = windowParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    touchStartTime = System.currentTimeMillis()
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (!isDragging && hypot(dx, dy) > touchSlop) {
                        isDragging = true
                    }
                    if (isDragging) {
                        windowParams.x = initialX + dx.toInt()
                        windowParams.y = initialY + dy.toInt()
                        safeUpdateViewLayout()
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val duration = System.currentTimeMillis() - touchStartTime
                    val dist = hypot(event.rawX - initialTouchX, event.rawY - initialTouchY)
                    if (!isDragging && dist < touchSlop && duration < 350) {
                        // Click detected!
                        performHapticFeedback()
                        expandToMiniPlayer()
                    } else if (preferences.autoSnap) {
                        snapToEdge()
                    } else {
                        preferences.lastX = windowParams.x
                        preferences.lastY = windowParams.y
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun setupPanelTouchListener(touchHandle: View) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false

        touchHandle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = windowParams.x
                    initialY = windowParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (!isDragging && hypot(dx, dy) > touchSlop) {
                        isDragging = true
                    }
                    if (isDragging) {
                        windowParams.x = initialX + dx.toInt()
                        windowParams.y = initialY + dy.toInt()
                        safeUpdateViewLayout()
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    preferences.lastX = windowParams.x
                    preferences.lastY = windowParams.y
                    true
                }
                else -> false
            }
        }
    }

    private fun expandToMiniPlayer() {
        if (isExpanded) return
        isExpanded = true

        bubbleContainer?.visibility = View.GONE
        panelContainer?.visibility = View.VISIBLE
        panelContainer?.alpha = preferences.panelAlpha

        // Clamp coordinates within screen boundaries
        val metrics = context.resources.displayMetrics
        val density = metrics.density
        val cardWidth = (300 * density).toInt()
        val maxX = metrics.widthPixels - cardWidth - 16
        if (windowParams.x > maxX) {
            windowParams.x = maxX.coerceAtLeast(16)
        }
        safeUpdateViewLayout()
    }

    private fun collapseToBubble() {
        if (!isExpanded) return
        isExpanded = false

        panelContainer?.visibility = View.GONE
        bubbleContainer?.visibility = View.VISIBLE
        bubbleContainer?.alpha = preferences.bubbleAlpha

        if (preferences.autoSnap) {
            snapToEdge()
        } else {
            safeUpdateViewLayout()
        }
    }

    private fun snapToEdge() {
        val metrics = context.resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val density = metrics.density
        val bubbleSize = (preferences.bubbleSizeDp * density).toInt()

        val currentCenterX = windowParams.x + bubbleSize / 2
        val targetX = if (currentCenterX < screenWidth / 2) {
            (12 * density).toInt()
        } else {
            screenWidth - bubbleSize - (12 * density).toInt()
        }

        val startX = windowParams.x
        val animator = ValueAnimator.ofInt(startX, targetX).apply {
            duration = 220
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                windowParams.x = anim.animatedValue as Int
                safeUpdateViewLayout()
            }
        }
        animator.start()
        preferences.lastX = targetX
        preferences.lastY = windowParams.y
    }

    private fun applyMediaStateToViews(state: MediaTrackState) {
        // 1. Update Bubble art
        if (state.albumArt != null) {
            bubbleArtImageView?.clearColorFilter()
            bubbleArtImageView?.setImageBitmap(state.albumArt)
        } else {
            bubbleArtImageView?.setImageResource(android.R.drawable.ic_media_play)
            bubbleArtImageView?.setColorFilter(Color.parseColor("#FF4D6D"))
        }

        // 2. Update Bubble play badge
        if (state.isPlaying) {
            bubblePlayBadge?.setImageResource(android.R.drawable.ic_media_pause)
        } else {
            bubblePlayBadge?.setImageResource(android.R.drawable.ic_media_play)
        }

        // 3. Update Panel Art
        if (state.albumArt != null) {
            panelArtImageView?.clearColorFilter()
            panelArtImageView?.setImageBitmap(state.albumArt)
        } else {
            panelArtImageView?.setImageResource(android.R.drawable.ic_media_play)
            panelArtImageView?.setColorFilter(Color.parseColor("#FF4D6D"))
        }

        // 4. Update Titles
        if (state.hasActiveSession) {
            titleTextView?.text = state.title.ifBlank { "Playing Track" }
            artistTextView?.text = state.artist.ifBlank { state.appName.ifBlank { "Unknown Artist" } }
            sourceBadgeTextView?.text = state.appName.ifBlank { "ACTIVE" }.uppercase()
        } else {
            titleTextView?.text = "No Active Song"
            artistTextView?.text = "Play music on any player"
            sourceBadgeTextView?.text = "STANDBY"
        }

        // 5. Update Play/Pause Button
        if (state.isPlaying) {
            playPauseBtn?.setImageResource(android.R.drawable.ic_media_pause)
        } else {
            playPauseBtn?.setImageResource(android.R.drawable.ic_media_play)
        }
    }

    private fun safeUpdateViewLayout() {
        if (rootContainer != null) {
            try {
                windowManager.updateViewLayout(rootContainer, windowParams)
            } catch (e: Exception) {
                Log.w(TAG, "Error updating overlay layout", e)
            }
        }
    }

    private fun performHapticFeedback() {
        if (!preferences.vibrateOnTap) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    android.os.VibrationEffect.createPredefined(android.os.VibrationEffect.EFFECT_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (_: Exception) {}
    }
}
