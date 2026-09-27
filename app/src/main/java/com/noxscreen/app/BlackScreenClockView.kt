package com.noxscreen.app

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class BlackScreenClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var timeText: String = "12:23"
    private var dateText: String = "Axd, Seb 27"
    private var batteryPercentage: Int = 87
    private var showBattery: Boolean = true
    private var clockStyle: String = "default"
    private var themeColor: String = "white"

    private val density = resources.displayMetrics.density
    private fun dp(value: Float): Float = value * density

    data class NeonPalette(
        val ambientCenter: Int,
        val ambientMid: Int,
        val arcStart: Int,
        val arcMid: Int,
        val arcHighlight: Int,
        val arcEnd: Int,
        val subArcStart: Int,
        val subArcMid: Int,
        val subArcEnd: Int,
        val hoursBottomMid: Int,
        val hoursBottomEnd: Int,
        val colonTop: Int,
        val colonBottom: Int,
        val minTop1: Int,
        val minTop2: Int,
        val minTop3: Int,
        val minTop4: Int,
        val minBot1: Int,
        val minBot2: Int,
        val minBot3: Int,
        val pillStart: Int,
        val pillMid: Int,
        val pillEnd: Int,
        val glowPrimary: Int,
        val glowSecondary: Int
    )

    private fun resolvePalette(theme: String): NeonPalette {
        return when (theme.lowercase()) {
            "green" -> NeonPalette(
                ambientCenter = Color.argb(28, 0, 230, 118),
                ambientMid = Color.argb(12, 0, 150, 80),
                arcStart = Color.parseColor("#00897B"),
                arcMid = Color.parseColor("#00E676"),
                arcHighlight = Color.parseColor("#B9F6CA"),
                arcEnd = Color.parseColor("#00BFA5"),
                subArcStart = Color.parseColor("#00A859"),
                subArcMid = Color.parseColor("#3DFF92"),
                subArcEnd = Color.parseColor("#00796B"),
                hoursBottomMid = Color.parseColor("#A7FFEB"),
                hoursBottomEnd = Color.parseColor("#69F0AE"),
                colonTop = Color.parseColor("#69F0AE"),
                colonBottom = Color.parseColor("#00C853"),
                minTop1 = Color.parseColor("#69F0AE"),
                minTop2 = Color.parseColor("#00E676"),
                minTop3 = Color.parseColor("#00C853"),
                minTop4 = Color.parseColor("#009624"),
                minBot1 = Color.parseColor("#3DFF92"),
                minBot2 = Color.parseColor("#00B248"),
                minBot3 = Color.parseColor("#00E676"),
                pillStart = Color.parseColor("#009624"),
                pillMid = Color.parseColor("#00E676"),
                pillEnd = Color.parseColor("#69F0AE"),
                glowPrimary = Color.rgb(0, 230, 118),
                glowSecondary = Color.rgb(0, 160, 80)
            )
            "yellow" -> NeonPalette(
                ambientCenter = Color.argb(28, 255, 196, 0),
                ambientMid = Color.argb(12, 255, 111, 0),
                arcStart = Color.parseColor("#FF8F00"),
                arcMid = Color.parseColor("#FFD54F"),
                arcHighlight = Color.parseColor("#FFF9C4"),
                arcEnd = Color.parseColor("#FFA000"),
                subArcStart = Color.parseColor("#FF6F00"),
                subArcMid = Color.parseColor("#FFD740"),
                subArcEnd = Color.parseColor("#E65100"),
                hoursBottomMid = Color.parseColor("#FFE57F"),
                hoursBottomEnd = Color.parseColor("#FFD740"),
                colonTop = Color.parseColor("#FFE57F"),
                colonBottom = Color.parseColor("#FF8F00"),
                minTop1 = Color.parseColor("#FFF176"),
                minTop2 = Color.parseColor("#FFD54F"),
                minTop3 = Color.parseColor("#FFA000"),
                minTop4 = Color.parseColor("#FF6F00"),
                minBot1 = Color.parseColor("#FFD740"),
                minBot2 = Color.parseColor("#FF8F00"),
                minBot3 = Color.parseColor("#FFA000"),
                pillStart = Color.parseColor("#FF6F00"),
                pillMid = Color.parseColor("#FFB300"),
                pillEnd = Color.parseColor("#FFE57F"),
                glowPrimary = Color.rgb(255, 213, 79),
                glowSecondary = Color.rgb(255, 111, 0)
            )
            "pink" -> NeonPalette(
                ambientCenter = Color.argb(28, 255, 64, 129),
                ambientMid = Color.argb(12, 197, 17, 98),
                arcStart = Color.parseColor("#AD1457"),
                arcMid = Color.parseColor("#FF4081"),
                arcHighlight = Color.parseColor("#FFCDD2"),
                arcEnd = Color.parseColor("#F50057"),
                subArcStart = Color.parseColor("#C51162"),
                subArcMid = Color.parseColor("#FF80AB"),
                subArcEnd = Color.parseColor("#880E4F"),
                hoursBottomMid = Color.parseColor("#FF9EBB"),
                hoursBottomEnd = Color.parseColor("#FF80AB"),
                colonTop = Color.parseColor("#FF80AB"),
                colonBottom = Color.parseColor("#F50057"),
                minTop1 = Color.parseColor("#FF80AB"),
                minTop2 = Color.parseColor("#FF4081"),
                minTop3 = Color.parseColor("#F50057"),
                minTop4 = Color.parseColor("#C51162"),
                minBot1 = Color.parseColor("#FF5252"),
                minBot2 = Color.parseColor("#C51162"),
                minBot3 = Color.parseColor("#F50057"),
                pillStart = Color.parseColor("#C51162"),
                pillMid = Color.parseColor("#FF4081"),
                pillEnd = Color.parseColor("#FF80AB"),
                glowPrimary = Color.rgb(255, 64, 129),
                glowSecondary = Color.rgb(197, 17, 98)
            )
            "purple" -> NeonPalette(
                ambientCenter = Color.argb(28, 124, 77, 255),
                ambientMid = Color.argb(12, 98, 0, 234),
                arcStart = Color.parseColor("#4527A0"),
                arcMid = Color.parseColor("#7C4DFF"),
                arcHighlight = Color.parseColor("#EDE7F6"),
                arcEnd = Color.parseColor("#651FFF"),
                subArcStart = Color.parseColor("#6200EA"),
                subArcMid = Color.parseColor("#B388FF"),
                subArcEnd = Color.parseColor("#4A148C"),
                hoursBottomMid = Color.parseColor("#D1C4E9"),
                hoursBottomEnd = Color.parseColor("#B388FF"),
                colonTop = Color.parseColor("#B388FF"),
                colonBottom = Color.parseColor("#651FFF"),
                minTop1 = Color.parseColor("#D1C4E9"),
                minTop2 = Color.parseColor("#B388FF"),
                minTop3 = Color.parseColor("#7C4DFF"),
                minTop4 = Color.parseColor("#6200EA"),
                minBot1 = Color.parseColor("#9575CD"),
                minBot2 = Color.parseColor("#651FFF"),
                minBot3 = Color.parseColor("#7C4DFF"),
                pillStart = Color.parseColor("#6200EA"),
                pillMid = Color.parseColor("#7C4DFF"),
                pillEnd = Color.parseColor("#B388FF"),
                glowPrimary = Color.rgb(179, 136, 255),
                glowSecondary = Color.rgb(101, 31, 255)
            )
            "blue" -> NeonPalette(
                ambientCenter = Color.argb(30, 41, 121, 255),
                ambientMid = Color.argb(14, 13, 71, 161),
                arcStart = Color.parseColor("#1565C0"),
                arcMid = Color.parseColor("#448AFF"),
                arcHighlight = Color.parseColor("#E3F2FD"),
                arcEnd = Color.parseColor("#2979FF"),
                subArcStart = Color.parseColor("#1E88E5"),
                subArcMid = Color.parseColor("#82B1FF"),
                subArcEnd = Color.parseColor("#0D47A1"),
                hoursBottomMid = Color.parseColor("#BBDEFB"),
                hoursBottomEnd = Color.parseColor("#82B1FF"),
                colonTop = Color.parseColor("#82B1FF"),
                colonBottom = Color.parseColor("#2962FF"),
                minTop1 = Color.parseColor("#82B1FF"),
                minTop2 = Color.parseColor("#448AFF"),
                minTop3 = Color.parseColor("#2979FF"),
                minTop4 = Color.parseColor("#2962FF"),
                minBot1 = Color.parseColor("#448AFF"),
                minBot2 = Color.parseColor("#1565C0"),
                minBot3 = Color.parseColor("#2962FF"),
                pillStart = Color.parseColor("#1565C0"),
                pillMid = Color.parseColor("#2979FF"),
                pillEnd = Color.parseColor("#82B1FF"),
                glowPrimary = Color.rgb(68, 138, 255),
                glowSecondary = Color.rgb(41, 98, 255)
            )
            else -> NeonPalette( // "white" / "cyan" (Signature Reference Design)
                ambientCenter = Color.argb(26, 0, 145, 255),
                ambientMid = Color.argb(12, 0, 80, 220),
                arcStart = Color.parseColor("#1976D2"),
                arcMid = Color.parseColor("#00E5FF"),
                arcHighlight = Color.parseColor("#A8FFFF"),
                arcEnd = Color.parseColor("#00B0FF"),
                subArcStart = Color.parseColor("#0088FF"),
                subArcMid = Color.parseColor("#40C4FF"),
                subArcEnd = Color.parseColor("#0066FF"),
                hoursBottomMid = Color.parseColor("#90ECFF"),
                hoursBottomEnd = Color.parseColor("#5CDFFF"),
                colonTop = Color.parseColor("#18E8FF"),
                colonBottom = Color.parseColor("#0095FF"),
                minTop1 = Color.parseColor("#00FBFF"),
                minTop2 = Color.parseColor("#00C4FF"),
                minTop3 = Color.parseColor("#006BFF"),
                minTop4 = Color.parseColor("#0048FF"),
                minBot1 = Color.parseColor("#2496FF"),
                minBot2 = Color.parseColor("#0066FF"),
                minBot3 = Color.parseColor("#1478FF"),
                pillStart = Color.parseColor("#0051FF"),
                pillMid = Color.parseColor("#00B8FF"),
                pillEnd = Color.parseColor("#00F5FF"),
                glowPrimary = Color.rgb(0, 235, 255),
                glowSecondary = Color.rgb(0, 110, 255)
            )
        }
    }

    private val ambientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val arcOuterGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        maskFilter = BlurMaskFilter(dp(15f), BlurMaskFilter.Blur.NORMAL)
    }

    private val arcMidGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        maskFilter = BlurMaskFilter(dp(5f), BlurMaskFilter.Blur.NORMAL)
    }

    private val arcCorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val hoursGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif", Typeface.BOLD)
        letterSpacing = -0.025f
        maskFilter = BlurMaskFilter(dp(13f), BlurMaskFilter.Blur.NORMAL)
    }

    private val hoursPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif", Typeface.BOLD)
        letterSpacing = -0.025f
    }

    private val minutesGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif", Typeface.BOLD)
        letterSpacing = -0.025f
        maskFilter = BlurMaskFilter(dp(15f), BlurMaskFilter.Blur.NORMAL)
    }

    private val minutesPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif", Typeface.BOLD)
        letterSpacing = -0.025f
    }

    private val colonGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(dp(7f), BlurMaskFilter.Blur.NORMAL)
    }

    private val colonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val pillBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#030812")
    }

    private val pillGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(3.5f)
        maskFilter = BlurMaskFilter(dp(5f), BlurMaskFilter.Blur.NORMAL)
    }

    private val pillBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1.35f)
    }

    private val dateTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
        letterSpacing = 0.015f
    }

    private val batteryGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(145, 0, 230, 118)
        maskFilter = BlurMaskFilter(dp(7f), BlurMaskFilter.Blur.NORMAL)
    }

    private val batteryStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1.65f)
        color = Color.WHITE
    }

    private val batteryCapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val batteryFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val batteryTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        letterSpacing = 0.01f
    }

    private val auxLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val auxGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        maskFilter = BlurMaskFilter(dp(6f), BlurMaskFilter.Blur.NORMAL)
    }

    private val miniDigitalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        letterSpacing = 0.06f
    }

    private val arcRect = RectF()
    private val innerArcRect = RectF()
    private val pillRect = RectF()
    private val batteryBodyRect = RectF()
    private val batteryFillRect = RectF()
    private val batteryCapRect = RectF()
    private val batterySideCapRect = RectF()
    private val crescentPath = Path()
    private val clipAbovePath = Path()
    private val clipBelowPath = Path()
    private val maskBackdropPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLACK
        maskFilter = BlurMaskFilter(dp(10f), BlurMaskFilter.Blur.NORMAL)
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun updateState(
        time: String,
        date: String,
        battery: Int,
        showBattery: Boolean,
        clockStyle: String = this.clockStyle,
        themeColor: String = this.themeColor
    ) {
        val cleanedTime = time.trim().split(" ").firstOrNull() ?: time
        var changed = false
        if (this.timeText != cleanedTime) {
            this.timeText = cleanedTime
            changed = true
        }
        if (this.dateText != date) {
            this.dateText = date
            changed = true
        }
        if (this.batteryPercentage != battery) {
            this.batteryPercentage = battery
            changed = true
        }
        if (this.showBattery != showBattery) {
            this.showBattery = showBattery
            changed = true
        }
        if (this.clockStyle != clockStyle) {
            this.clockStyle = clockStyle
            changed = true
        }
        if (this.themeColor != themeColor) {
            this.themeColor = themeColor
            changed = true
        }
        if (changed) {
            invalidate()
        }
    }

    private fun withAlpha(rgb: Int, alpha: Int): Int {
        return Color.argb(alpha.coerceIn(0, 255), Color.red(rgb), Color.green(rgb), Color.blue(rgb))
    }

    private fun buildTaperedCrescentPath(
        path: Path,
        cx: Float,
        cy: Float,
        radius: Float,
        startDeg: Float,
        endDeg: Float,
        maxHalfThickness: Float,
        skewPower: Double = 1.25
    ) {
        path.reset()
        val steps = 48
        val sweep = endDeg - startDeg
        for (i in 0..steps) {
            val t = i.toFloat() / steps
            val deg = startDeg + sweep * t
            val rad = Math.toRadians(deg.toDouble())
            val profile = sin(t * Math.PI).pow(skewPower).toFloat()
            val r = radius + maxHalfThickness * profile
            val x = cx + r * cos(rad).toFloat()
            val y = cy + r * sin(rad).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        for (i in steps downTo 0) {
            val t = i.toFloat() / steps
            val deg = startDeg + sweep * t
            val rad = Math.toRadians(deg.toDouble())
            val profile = sin(t * Math.PI).pow(skewPower).toFloat()
            val r = radius - maxHalfThickness * profile
            val x = cx + r * cos(rad).toFloat()
            val y = cy + r * sin(rad).toFloat()
            path.lineTo(x, y)
        }
        path.close()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val designHeight = dp(265f)
        val scale = (h / designHeight).coerceIn(0.35f, 1.25f)

        val cx = w / 2f
        canvas.save()
        if (kotlin.math.abs(scale - 1f) > 0.01f) {
            canvas.scale(scale, scale, cx, 0f)
        }

        val palette = resolvePalette(themeColor)
        val ringCy = dp(116f)
        val ringRadius = dp(96f)

        // 1. Orbital Ring & Ambient Glow (Shared signature across all styles)
        drawOrbitalNeonRing(canvas, cx, ringCy, ringRadius, palette)

        // 2. Style-Specific Time / Dial Centerpiece
        val timeBaseline = dp(142f)
        when (clockStyle) {
            "huge" -> drawNeonOutlineTime(canvas, cx, timeBaseline, palette)
            "analog" -> drawOrbitalChronoAnalog(canvas, cx, ringCy, ringRadius, palette)
            "dino" -> drawNeonPulsePortalTime(canvas, cx, ringCy, ringRadius, timeBaseline, palette)
            else -> drawSignatureOrbitalTime(canvas, cx, timeBaseline, palette)
        }

        // 3. Signature Glowing Date Pill ("Axd, Seb 27")
        val pillBottom = drawDatePill(canvas, cx, timeBaseline, palette)

        // 4. Signature Glowing Battery Indicator ("[🔋] 87%")
        if (showBattery) {
            drawBatteryIndicator(canvas, cx, pillBottom)
        }

        canvas.restore()
    }

    private fun drawOrbitalNeonRing(
        canvas: Canvas,
        cx: Float,
        ringCy: Float,
        ringRadius: Float,
        palette: NeonPalette
    ) {
        ambientPaint.shader = RadialGradient(
            cx,
            ringCy,
            ringRadius * 1.18f,
            intArrayOf(
                palette.ambientCenter,
                palette.ambientMid,
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.65f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, ringCy, ringRadius * 1.18f, ambientPaint)

        arcRect.set(
            cx - ringRadius,
            ringCy - ringRadius,
            cx + ringRadius,
            ringCy + ringRadius
        )

        // Top Orbital Glowing Crescent Arc (207° -> 333°)
        arcOuterGlowPaint.strokeWidth = dp(10f)
        arcOuterGlowPaint.shader = LinearGradient(
            cx - ringRadius * 0.8f,
            ringCy - ringRadius,
            cx + ringRadius * 0.8f,
            ringCy - ringRadius * 0.3f,
            intArrayOf(
                withAlpha(palette.glowSecondary, 0),
                withAlpha(palette.glowSecondary, 165),
                withAlpha(palette.glowPrimary, 200),
                withAlpha(palette.glowSecondary, 0)
            ),
            floatArrayOf(0f, 0.42f, 0.78f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawArc(arcRect, 214f, 112f, false, arcOuterGlowPaint)

        arcMidGlowPaint.strokeWidth = dp(4.2f)
        arcMidGlowPaint.shader = LinearGradient(
            cx - ringRadius * 0.85f,
            ringCy - ringRadius,
            cx + ringRadius * 0.85f,
            ringCy - ringRadius * 0.3f,
            intArrayOf(
                withAlpha(palette.arcStart, 20),
                withAlpha(palette.arcMid, 235),
                withAlpha(palette.arcHighlight, 255),
                withAlpha(palette.arcEnd, 30)
            ),
            floatArrayOf(0f, 0.48f, 0.78f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawArc(arcRect, 210f, 120f, false, arcMidGlowPaint)

        buildTaperedCrescentPath(
            path = crescentPath,
            cx = cx,
            cy = ringCy,
            radius = ringRadius,
            startDeg = 207f,
            endDeg = 333f,
            maxHalfThickness = dp(1.75f),
            skewPower = 1.2
        )
        arcCorePaint.shader = LinearGradient(
            cx - ringRadius * 0.85f,
            ringCy - ringRadius,
            cx + ringRadius * 0.85f,
            ringCy - ringRadius * 0.35f,
            intArrayOf(
                palette.arcStart,
                palette.arcMid,
                palette.arcHighlight,
                palette.arcEnd
            ),
            floatArrayOf(0f, 0.45f, 0.76f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(crescentPath, arcCorePaint)

        // Bottom-Left Orbital Glowing Crescent Arc (76° -> 154°)
        arcOuterGlowPaint.strokeWidth = dp(8.5f)
        arcOuterGlowPaint.shader = LinearGradient(
            cx - ringRadius,
            ringCy + ringRadius * 0.35f,
            cx + dp(10f),
            ringCy + ringRadius,
            intArrayOf(
                withAlpha(palette.glowSecondary, 145),
                withAlpha(palette.glowPrimary, 170),
                withAlpha(palette.glowPrimary, 0)
            ),
            floatArrayOf(0f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawArc(arcRect, 82f, 66f, false, arcOuterGlowPaint)

        buildTaperedCrescentPath(
            path = crescentPath,
            cx = cx,
            cy = ringCy,
            radius = ringRadius,
            startDeg = 76f,
            endDeg = 154f,
            maxHalfThickness = dp(1.35f),
            skewPower = 1.25
        )
        arcCorePaint.shader = LinearGradient(
            cx - ringRadius * 0.9f,
            ringCy + ringRadius * 0.4f,
            cx + dp(8f),
            ringCy + ringRadius,
            intArrayOf(
                palette.subArcStart,
                palette.subArcMid,
                palette.subArcEnd
            ),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(crescentPath, arcCorePaint)

        // Bottom-Right Delicate Orbital Arc (26° -> 64°)
        buildTaperedCrescentPath(
            path = crescentPath,
            cx = cx,
            cy = ringCy,
            radius = ringRadius,
            startDeg = 26f,
            endDeg = 64f,
            maxHalfThickness = dp(0.75f),
            skewPower = 1.3
        )
        arcCorePaint.shader = LinearGradient(
            cx + ringRadius * 0.4f,
            ringCy + ringRadius * 0.85f,
            cx + ringRadius * 0.9f,
            ringCy + ringRadius * 0.4f,
            palette.arcEnd,
            palette.subArcEnd,
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(crescentPath, arcCorePaint)
    }

    // STYLE 1: "default" — Signature Orbital Neon (1:1 Reference Design)
    private fun drawSignatureOrbitalTime(
        canvas: Canvas,
        cx: Float,
        timeBaseline: Float,
        palette: NeonPalette
    ) {
        val parts = timeText.split(":")
        val hoursStr = parts.getOrNull(0) ?: "12"
        val minutesStr = parts.getOrNull(1) ?: "23"

        val timeTextSize = dp(94f)
        hoursPaint.style = Paint.Style.FILL
        hoursGlowPaint.style = Paint.Style.FILL
        minutesPaint.style = Paint.Style.FILL
        minutesGlowPaint.style = Paint.Style.FILL

        hoursPaint.textSize = timeTextSize
        hoursGlowPaint.textSize = timeTextSize
        minutesPaint.textSize = timeTextSize
        minutesGlowPaint.textSize = timeTextSize

        val hoursWidth = hoursPaint.measureText(hoursStr)
        val minutesWidth = minutesPaint.measureText(minutesStr)
        val colonGap = dp(24f)
        val totalTimeWidth = hoursWidth + colonGap + minutesWidth

        val startX = cx - totalTimeWidth / 2f
        val hoursX = startX
        val colonCenterX = startX + hoursWidth + colonGap / 2f
        val minutesX = startX + hoursWidth + colonGap

        val fontMetrics = hoursPaint.fontMetrics
        val capHeight = -fontMetrics.ascent * 0.72f
        val timeTopY = timeBaseline - capHeight
        val timeCapCenterY = timeBaseline - capHeight / 2f

        canvas.drawRoundRect(
            startX - dp(12f),
            timeTopY - dp(4f),
            startX + totalTimeWidth + dp(12f),
            timeBaseline + dp(6f),
            dp(18f),
            dp(18f),
            maskBackdropPaint
        )

        val diagLeftX = startX - dp(10f)
        val diagLeftY = timeBaseline + dp(4f)
        val diagRightX = startX + totalTimeWidth + dp(10f)
        val diagRightY = timeBaseline - dp(26f)

        clipAbovePath.reset()
        clipAbovePath.moveTo(diagLeftX, timeTopY - dp(30f))
        clipAbovePath.lineTo(diagRightX, timeTopY - dp(30f))
        clipAbovePath.lineTo(diagRightX, diagRightY)
        clipAbovePath.lineTo(diagLeftX, diagLeftY)
        clipAbovePath.close()

        clipBelowPath.reset()
        clipBelowPath.moveTo(diagLeftX, diagLeftY)
        clipBelowPath.lineTo(diagRightX, diagRightY)
        clipBelowPath.lineTo(diagRightX, timeBaseline + dp(30f))
        clipBelowPath.lineTo(diagLeftX, timeBaseline + dp(30f))
        clipBelowPath.close()

        hoursGlowPaint.shader = null
        hoursGlowPaint.color = withAlpha(palette.glowPrimary, 85)
        canvas.drawText(hoursStr, hoursX, timeBaseline, hoursGlowPaint)

        canvas.save()
        canvas.clipPath(clipAbovePath)
        hoursPaint.shader = LinearGradient(
            hoursX,
            timeTopY,
            hoursX,
            timeBaseline,
            Color.WHITE,
            Color.parseColor("#F2FAFF"),
            Shader.TileMode.CLAMP
        )
        canvas.drawText(hoursStr, hoursX, timeBaseline, hoursPaint)
        canvas.restore()

        canvas.save()
        canvas.clipPath(clipBelowPath)
        hoursPaint.shader = LinearGradient(
            hoursX,
            timeBaseline - dp(24f),
            hoursX + hoursWidth,
            timeBaseline,
            intArrayOf(
                Color.WHITE,
                palette.hoursBottomMid,
                palette.hoursBottomEnd
            ),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawText(hoursStr, hoursX, timeBaseline, hoursPaint)
        canvas.restore()

        // Colon Dots
        val dotRadius = dp(6.2f)
        val dotOffset = dp(13.5f)
        val topDotY = timeCapCenterY - dotOffset
        val bottomDotY = timeCapCenterY + dotOffset

        colonGlowPaint.color = withAlpha(palette.glowPrimary, 135)
        canvas.drawCircle(colonCenterX, topDotY, dotRadius * 1.3f, colonGlowPaint)
        canvas.drawCircle(colonCenterX, bottomDotY, dotRadius * 1.3f, colonGlowPaint)

        colonPaint.style = Paint.Style.FILL
        colonPaint.shader = LinearGradient(
            colonCenterX,
            topDotY - dotRadius,
            colonCenterX,
            bottomDotY + dotRadius,
            palette.colonTop,
            palette.colonBottom,
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(colonCenterX, topDotY, dotRadius, colonPaint)
        canvas.drawCircle(colonCenterX, bottomDotY, dotRadius, colonPaint)

        // Minutes
        minutesGlowPaint.shader = LinearGradient(
            minutesX,
            timeTopY,
            minutesX,
            timeBaseline,
            intArrayOf(
                withAlpha(palette.glowPrimary, 120),
                withAlpha(palette.glowSecondary, 135)
            ),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawText(minutesStr, minutesX, timeBaseline, minutesGlowPaint)

        canvas.save()
        canvas.clipPath(clipAbovePath)
        minutesPaint.shader = LinearGradient(
            minutesX,
            timeTopY,
            minutesX,
            timeBaseline,
            intArrayOf(
                palette.minTop1,
                palette.minTop2,
                palette.minTop3,
                palette.minTop4
            ),
            floatArrayOf(0f, 0.42f, 0.82f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawText(minutesStr, minutesX, timeBaseline, minutesPaint)
        canvas.restore()

        canvas.save()
        canvas.clipPath(clipBelowPath)
        minutesPaint.shader = LinearGradient(
            minutesX,
            diagRightY,
            minutesX,
            timeBaseline,
            intArrayOf(
                palette.minBot1,
                palette.minBot2,
                palette.minBot3
            ),
            floatArrayOf(0f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawText(minutesStr, minutesX, timeBaseline, minutesPaint)
        canvas.restore()
    }

    // STYLE 2: "huge" — Neon Outline (Hollow Cyber Dual-Stroke + Horizon Core)
    private fun drawNeonOutlineTime(
        canvas: Canvas,
        cx: Float,
        timeBaseline: Float,
        palette: NeonPalette
    ) {
        val parts = timeText.split(":")
        val hoursStr = parts.getOrNull(0) ?: "12"
        val minutesStr = parts.getOrNull(1) ?: "23"

        val timeTextSize = dp(98f)
        hoursPaint.textSize = timeTextSize
        hoursGlowPaint.textSize = timeTextSize
        minutesPaint.textSize = timeTextSize
        minutesGlowPaint.textSize = timeTextSize

        val hoursWidth = hoursPaint.measureText(hoursStr)
        val minutesWidth = minutesPaint.measureText(minutesStr)
        val colonGap = dp(24f)
        val totalTimeWidth = hoursWidth + colonGap + minutesWidth

        val startX = cx - totalTimeWidth / 2f
        val hoursX = startX
        val colonCenterX = startX + hoursWidth + colonGap / 2f
        val minutesX = startX + hoursWidth + colonGap

        val fontMetrics = hoursPaint.fontMetrics
        val capHeight = -fontMetrics.ascent * 0.72f
        val timeTopY = timeBaseline - capHeight
        val timeCapCenterY = timeBaseline - capHeight / 2f

        canvas.drawRoundRect(
            startX - dp(14f),
            timeTopY - dp(6f),
            startX + totalTimeWidth + dp(14f),
            timeBaseline + dp(8f),
            dp(18f),
            dp(18f),
            maskBackdropPaint
        )

        // Diagonal horizon cut for subtle inner glass reflection in bottom half
        val diagLeftX = startX - dp(10f)
        val diagLeftY = timeBaseline + dp(2f)
        val diagRightX = startX + totalTimeWidth + dp(10f)
        val diagRightY = timeBaseline - dp(28f)

        clipBelowPath.reset()
        clipBelowPath.moveTo(diagLeftX, diagLeftY)
        clipBelowPath.lineTo(diagRightX, diagRightY)
        clipBelowPath.lineTo(diagRightX, timeBaseline + dp(30f))
        clipBelowPath.lineTo(diagLeftX, timeBaseline + dp(30f))
        clipBelowPath.close()

        // Inner horizon glass fill below diagonal
        canvas.save()
        canvas.clipPath(clipBelowPath)
        hoursPaint.style = Paint.Style.FILL
        hoursPaint.shader = LinearGradient(
            hoursX,
            diagRightY,
            hoursX,
            timeBaseline,
            withAlpha(Color.WHITE, 45),
            withAlpha(palette.hoursBottomMid, 95),
            Shader.TileMode.CLAMP
        )
        canvas.drawText(hoursStr, hoursX, timeBaseline, hoursPaint)

        minutesPaint.style = Paint.Style.FILL
        minutesPaint.shader = LinearGradient(
            minutesX,
            diagRightY,
            minutesX,
            timeBaseline,
            withAlpha(palette.minTop1, 55),
            withAlpha(palette.minTop3, 115),
            Shader.TileMode.CLAMP
        )
        canvas.drawText(minutesStr, minutesX, timeBaseline, minutesPaint)
        canvas.restore()

        // Hours Neon Hollow Outline
        hoursGlowPaint.style = Paint.Style.STROKE
        hoursGlowPaint.strokeWidth = dp(5f)
        hoursGlowPaint.color = withAlpha(palette.glowPrimary, 110)
        canvas.drawText(hoursStr, hoursX, timeBaseline, hoursGlowPaint)

        hoursPaint.style = Paint.Style.STROKE
        hoursPaint.strokeWidth = dp(2.4f)
        hoursPaint.shader = LinearGradient(
            hoursX,
            timeTopY,
            hoursX,
            timeBaseline,
            intArrayOf(Color.WHITE, Color.parseColor("#E0F7FA"), palette.hoursBottomMid),
            floatArrayOf(0f, 0.65f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawText(hoursStr, hoursX, timeBaseline, hoursPaint)

        // Hollow Neon Colon Rings
        val dotRadius = dp(5.8f)
        val dotOffset = dp(13.5f)
        val topDotY = timeCapCenterY - dotOffset
        val bottomDotY = timeCapCenterY + dotOffset

        colonGlowPaint.color = withAlpha(palette.glowPrimary, 140)
        canvas.drawCircle(colonCenterX, topDotY, dotRadius * 1.35f, colonGlowPaint)
        canvas.drawCircle(colonCenterX, bottomDotY, dotRadius * 1.35f, colonGlowPaint)

        colonPaint.style = Paint.Style.STROKE
        colonPaint.strokeWidth = dp(2.2f)
        colonPaint.shader = LinearGradient(
            colonCenterX,
            topDotY - dotRadius,
            colonCenterX,
            bottomDotY + dotRadius,
            palette.colonTop,
            palette.colonBottom,
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(colonCenterX, topDotY, dotRadius, colonPaint)
        canvas.drawCircle(colonCenterX, bottomDotY, dotRadius, colonPaint)

        // Minutes Neon Hollow Outline
        minutesGlowPaint.style = Paint.Style.STROKE
        minutesGlowPaint.strokeWidth = dp(5.5f)
        minutesGlowPaint.shader = LinearGradient(
            minutesX,
            timeTopY,
            minutesX,
            timeBaseline,
            intArrayOf(
                withAlpha(palette.glowPrimary, 160),
                withAlpha(palette.glowSecondary, 160)
            ),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawText(minutesStr, minutesX, timeBaseline, minutesGlowPaint)

        minutesPaint.style = Paint.Style.STROKE
        minutesPaint.strokeWidth = dp(2.5f)
        minutesPaint.shader = LinearGradient(
            minutesX,
            timeTopY,
            minutesX,
            timeBaseline,
            intArrayOf(
                palette.minTop1,
                palette.minTop2,
                palette.minTop3,
                palette.minTop4
            ),
            floatArrayOf(0f, 0.45f, 0.82f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawText(minutesStr, minutesX, timeBaseline, minutesPaint)
    }

    // STYLE 3: "analog" — Orbital Chrono (Luxury Neon Analog + Mini Digital Hybrid inside Orbital Ring)
    private fun drawOrbitalChronoAnalog(
        canvas: Canvas,
        cx: Float,
        ringCy: Float,
        ringRadius: Float,
        palette: NeonPalette
    ) {
        val parts = timeText.split(":")
        val rawHours = parts.getOrNull(0)?.toIntOrNull() ?: 12
        val rawMinutes = parts.getOrNull(1)?.toIntOrNull() ?: 23

        val dialRadius = ringRadius * 0.80f

        // Subtle inner chrono track ring
        auxLinePaint.style = Paint.Style.STROKE
        auxLinePaint.strokeWidth = dp(1f)
        auxLinePaint.pathEffect = null
        auxLinePaint.shader = null
        auxLinePaint.color = withAlpha(palette.arcMid, 48)
        canvas.drawCircle(cx, ringCy, dialRadius, auxLinePaint)

        // 12 precision hour tick marks inside the orbital ring
        for (i in 0 until 12) {
            val angleDeg = i * 30f - 90f
            val rad = Math.toRadians(angleDeg.toDouble())
            val isCardinal = (i % 3 == 0)
            val outerR = dialRadius - dp(2f)
            val innerR = if (isCardinal) outerR - dp(10f) else outerR - dp(5f)

            val x1 = cx + outerR * cos(rad).toFloat()
            val y1 = ringCy + outerR * sin(rad).toFloat()
            val x2 = cx + innerR * cos(rad).toFloat()
            val y2 = ringCy + innerR * sin(rad).toFloat()

            auxLinePaint.strokeWidth = if (isCardinal) dp(2.4f) else dp(1.3f)
            auxLinePaint.color = if (isCardinal) Color.WHITE else withAlpha(palette.arcMid, 160)
            canvas.drawLine(x1, y1, x2, y2, auxLinePaint)
        }

        // Mini digital readout cleanly positioned inside lower half of dial
        miniDigitalPaint.textSize = dp(15f)
        miniDigitalPaint.color = withAlpha(palette.arcHighlight, 230)
        canvas.drawText(timeText, cx, ringCy + dialRadius * 0.52f, miniDigitalPaint)

        // Calculate real angles for hour and minute hands
        val minuteAngle = (rawMinutes % 60) * 6f - 90f
        val hourAngle = ((rawHours % 12) + (rawMinutes % 60) / 60f) * 30f - 90f

        val hourRad = Math.toRadians(hourAngle.toDouble())
        val minRad = Math.toRadians(minuteAngle.toDouble())

        // Minute Hand (Neon Gradient + Glow)
        val minHandLen = dialRadius * 0.74f
        val minEndX = cx + minHandLen * cos(minRad).toFloat()
        val minEndY = ringCy + minHandLen * sin(minRad).toFloat()

        auxGlowPaint.strokeWidth = dp(6f)
        auxGlowPaint.shader = null
        auxGlowPaint.color = withAlpha(palette.glowPrimary, 150)
        canvas.drawLine(cx, ringCy, minEndX, minEndY, auxGlowPaint)

        auxLinePaint.strokeWidth = dp(3.2f)
        auxLinePaint.shader = LinearGradient(
            cx,
            ringCy,
            minEndX,
            minEndY,
            palette.minTop1,
            palette.minTop3,
            Shader.TileMode.CLAMP
        )
        canvas.drawLine(cx, ringCy, minEndX, minEndY, auxLinePaint)

        // Hour Hand (Crisp White + Subtle Glow)
        val hourHandLen = dialRadius * 0.50f
        val hourEndX = cx + hourHandLen * cos(hourRad).toFloat()
        val hourEndY = ringCy + hourHandLen * sin(hourRad).toFloat()

        auxGlowPaint.strokeWidth = dp(6.5f)
        auxGlowPaint.color = withAlpha(Color.WHITE, 110)
        canvas.drawLine(cx, ringCy, hourEndX, hourEndY, auxGlowPaint)

        auxLinePaint.shader = null
        auxLinePaint.strokeWidth = dp(4.2f)
        auxLinePaint.color = Color.WHITE
        canvas.drawLine(cx, ringCy, hourEndX, hourEndY, auxLinePaint)

        // Center Orbital Jewel Cap
        colonGlowPaint.color = withAlpha(palette.glowPrimary, 180)
        canvas.drawCircle(cx, ringCy, dp(7f), colonGlowPaint)

        colonPaint.style = Paint.Style.FILL
        colonPaint.shader = null
        colonPaint.color = Color.parseColor("#030812")
        canvas.drawCircle(cx, ringCy, dp(4.8f), colonPaint)

        auxLinePaint.strokeWidth = dp(2f)
        auxLinePaint.color = palette.arcMid
        canvas.drawCircle(cx, ringCy, dp(4.8f), auxLinePaint)
    }

    // STYLE 4: "dino" — Neon Pulse (Dual-Orbit Cyber Portal + Laser Horizon Split)
    private fun drawNeonPulsePortalTime(
        canvas: Canvas,
        cx: Float,
        ringCy: Float,
        ringRadius: Float,
        timeBaseline: Float,
        palette: NeonPalette
    ) {
        // Inner segmented cyber-orbit ring inside the main orbital crescent ring
        val innerR = ringRadius * 0.84f
        innerArcRect.set(cx - innerR, ringCy - innerR, cx + innerR, ringCy + innerR)

        auxLinePaint.style = Paint.Style.STROKE
        auxLinePaint.strokeWidth = dp(1.5f)
        auxLinePaint.pathEffect = DashPathEffect(floatArrayOf(dp(6f), dp(5f)), 0f)
        auxLinePaint.shader = null
        auxLinePaint.color = withAlpha(palette.arcMid, 125)
        canvas.drawArc(innerArcRect, 200f, 140f, false, auxLinePaint)
        canvas.drawArc(innerArcRect, 40f, 100f, false, auxLinePaint)
        auxLinePaint.pathEffect = null

        // Draw signature digital time first
        drawSignatureOrbitalTime(canvas, cx, timeBaseline, palette)

        // Add glowing horizontal cyber-horizon laser line across the lower third of the digits
        val laserY = timeBaseline - dp(14f)
        val laserHalfW = dp(112f)

        auxGlowPaint.strokeWidth = dp(4.5f)
        auxGlowPaint.shader = LinearGradient(
            cx - laserHalfW,
            laserY,
            cx + laserHalfW,
            laserY,
            intArrayOf(
                Color.TRANSPARENT,
                withAlpha(palette.glowPrimary, 180),
                withAlpha(palette.arcHighlight, 220),
                withAlpha(palette.glowPrimary, 180),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.25f, 0.5f, 0.75f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawLine(cx - laserHalfW, laserY, cx + laserHalfW, laserY, auxGlowPaint)

        auxLinePaint.strokeWidth = dp(1.3f)
        auxLinePaint.shader = LinearGradient(
            cx - laserHalfW,
            laserY,
            cx + laserHalfW,
            laserY,
            intArrayOf(
                Color.TRANSPARENT,
                palette.arcMid,
                Color.WHITE,
                palette.arcMid,
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.25f, 0.5f, 0.75f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawLine(cx - laserHalfW, laserY, cx + laserHalfW, laserY, auxLinePaint)
        auxLinePaint.shader = null
    }

    private fun drawDatePill(
        canvas: Canvas,
        cx: Float,
        timeBaseline: Float,
        palette: NeonPalette
    ): Float {
        dateTextPaint.textSize = dp(15f)
        val dateTextWidth = dateTextPaint.measureText(dateText)
        val pillWidth = (dateTextWidth + dp(42f)).coerceAtLeast(dp(134f))
        val pillHeight = dp(33f)
        val pillTop = timeBaseline + dp(24f)
        val pillBottom = pillTop + pillHeight
        val pillLeft = cx - pillWidth / 2f
        val pillRight = cx + pillWidth / 2f
        val pillRadius = pillHeight / 2f

        pillRect.set(pillLeft, pillTop, pillRight, pillBottom)

        canvas.drawRoundRect(pillRect, pillRadius, pillRadius, pillBgPaint)

        val pillShader = LinearGradient(
            pillLeft,
            pillTop,
            pillRight,
            pillBottom,
            intArrayOf(
                palette.pillStart,
                palette.pillMid,
                palette.pillEnd
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        pillGlowPaint.shader = LinearGradient(
            pillLeft,
            pillTop,
            pillRight,
            pillBottom,
            intArrayOf(
                withAlpha(palette.pillStart, 110),
                withAlpha(palette.pillEnd, 155)
            ),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(pillRect, pillRadius, pillRadius, pillGlowPaint)

        pillBorderPaint.shader = pillShader
        canvas.drawRoundRect(pillRect, pillRadius, pillRadius, pillBorderPaint)

        val dateFontMetrics = dateTextPaint.fontMetrics
        val dateBaseline = pillRect.centerY() - (dateFontMetrics.ascent + dateFontMetrics.descent) / 2f
        canvas.drawText(dateText, cx, dateBaseline, dateTextPaint)

        return pillBottom
    }

    private fun drawBatteryIndicator(
        canvas: Canvas,
        cx: Float,
        pillBottom: Float
    ) {
        batteryTextPaint.textSize = dp(15.5f)
        val pctString = "${batteryPercentage.coerceIn(0, 100)}%"
        val pctWidth = batteryTextPaint.measureText(pctString)

        val iconW = dp(13.5f)
        val iconH = dp(19.5f)
        val gap = dp(8.5f)
        val totalBatWidth = iconW + gap + pctWidth

        val batCenterY = pillBottom + dp(26f)
        val iconLeft = cx - totalBatWidth / 2f
        val iconRight = iconLeft + iconW
        val iconTop = batCenterY - iconH / 2f + dp(1f)
        val iconBottom = iconTop + iconH

        batteryBodyRect.set(iconLeft, iconTop, iconRight, iconBottom)

        canvas.drawRoundRect(
            iconLeft - dp(1f),
            iconTop - dp(1f),
            iconRight + dp(1f),
            iconBottom + dp(1f),
            dp(4f),
            dp(4f),
            batteryGlowPaint
        )

        val capW = dp(6f)
        val capH = dp(2.2f)
        batteryCapRect.set(
            iconLeft + (iconW - capW) / 2f,
            iconTop - capH + dp(0.4f),
            iconLeft + (iconW + capW) / 2f,
            iconTop + dp(0.5f)
        )
        canvas.drawRoundRect(batteryCapRect, dp(1.2f), dp(1.2f), batteryCapPaint)

        val sideCapW = dp(1.5f)
        val sideCapH = dp(6.2f)
        batterySideCapRect.set(
            iconRight + dp(0.6f),
            batteryBodyRect.centerY() - sideCapH / 2f,
            iconRight + dp(0.6f) + sideCapW,
            batteryBodyRect.centerY() + sideCapH / 2f
        )
        canvas.drawRoundRect(batterySideCapRect, dp(1f), dp(1f), batteryCapPaint)

        val inset = dp(2.1f)
        val maxFillH = (iconH - inset * 2f).coerceAtLeast(dp(2f))
        val fillFraction = (batteryPercentage.coerceIn(12, 100)) / 100f
        val fillH = maxFillH * fillFraction
        batteryFillRect.set(
            iconLeft + inset,
            iconBottom - inset - fillH,
            iconRight - inset,
            iconBottom - inset
        )
        batteryFillPaint.shader = LinearGradient(
            batteryFillRect.left,
            batteryFillRect.top,
            batteryFillRect.left,
            batteryFillRect.bottom,
            Color.parseColor("#3DFF92"),
            Color.parseColor("#00E676"),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(batteryFillRect, dp(1.8f), dp(1.8f), batteryFillPaint)

        canvas.drawRoundRect(batteryBodyRect, dp(3.2f), dp(3.2f), batteryStrokePaint)

        val batFontMetrics = batteryTextPaint.fontMetrics
        val batTextBaseline = batCenterY - (batFontMetrics.ascent + batFontMetrics.descent) / 2f + dp(0.5f)
        canvas.drawText(pctString, iconRight + gap, batTextBaseline, batteryTextPaint)
    }
}
