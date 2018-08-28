/*
 * Copyright (c) 2018.
 * João Paulo Sena <joaopaulo761@gmail.com>
 *
 * This file is part of the UNES Open Source Project.
 *
 * UNES is licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.forcetower.unes.feature.siecomp.day

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.text.Layout.Alignment.ALIGN_CENTER
import android.graphics.Typeface.BOLD
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import androidx.core.content.res.*
import androidx.core.graphics.withTranslation
import androidx.core.text.inSpans
import androidx.core.view.get
import androidx.core.view.isEmpty
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.State
import com.forcetower.unes.R
import com.forcetower.unes.core.storage.database.accessors.SessionWithData
import com.forcetower.unes.feature.siecomp.ETimeUtils
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber

class ScheduleItemHeaderDecoration(
    context: Context,
    sessions: List<SessionWithData>,
    zoneId: ZoneId
): RecyclerView.ItemDecoration() {
    private val paint: TextPaint
    private val width: Int
    private val paddingTop: Int
    private val hourMinTextSize: Int
    private val meridiemTextSize: Int
    private val hourFormatter = DateTimeFormatter.ofPattern("h")
    private val hourMinFormatter = DateTimeFormatter.ofPattern("h:m")
    private val meridiemFormatter = DateTimeFormatter.ofPattern("a")

    init {
        val attrs = context.obtainStyledAttributes(
            R.style.Widget_Schedule_TimeHeaders,
            R.styleable.TimeHeader
        )

        paint = TextPaint(ANTI_ALIAS_FLAG).apply {
            color = attrs.getColorOrThrow(R.styleable.TimeHeader_android_textColor)
            textSize = attrs.getDimensionOrThrow(R.styleable.TimeHeader_hourTextSize)
            try {
                typeface = ResourcesCompat.getFont(
                        context,
                        attrs.getResourceIdOrThrow(R.styleable.TimeHeader_android_fontFamily)
                )
            } catch (_: Exception) {
                // ignore
            }
        }

        width = attrs.getDimensionPixelSizeOrThrow(R.styleable.TimeHeader_android_width)
        paddingTop = attrs.getDimensionPixelSizeOrThrow(R.styleable.TimeHeader_android_paddingTop)
        hourMinTextSize = attrs.getDimensionPixelSizeOrThrow(R.styleable.TimeHeader_hourMinTextSize)
        meridiemTextSize = attrs.getDimensionPixelSizeOrThrow(R.styleable.TimeHeader_meridiemTextSize)
        attrs.recycle()
    }

    private val timeSlots: Map<Int, StaticLayout> =
            indexSessionHeaders(sessions, zoneId).map {
                it.first to createHeader(it.second)
            }.toMap()


    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: State) {
        if (timeSlots.isEmpty() || parent.isEmpty()) return

        var earliestFoundHeaderPos = -1
        var prevHeaderTop = Int.MAX_VALUE

        for (i in parent.childCount - 1 downTo 0) {
            val view = parent.getChildAt(i)
            if (view == null) {
                Timber.w(
                        """View is null. Index: $i, childCount: ${parent.childCount},
                        |RecyclerView.State: $state""".trimMargin()
                )
                continue
            }
            val viewTop = view.top + view.translationY.toInt()
            if (view.bottom > 0 && viewTop < parent.height) {
                val position = parent.getChildAdapterPosition(view)
                timeSlots[position]?.let { layout ->
                    paint.alpha = (view.alpha * 255).toInt()
                    val top = (viewTop + paddingTop)
                            .coerceAtLeast(paddingTop)
                            .coerceAtMost(prevHeaderTop - layout.height)
                    c.withTranslation(y = top.toFloat()) {
                        layout.draw(c)
                    }
                    earliestFoundHeaderPos = position
                    prevHeaderTop = viewTop
                }
            }
        }

        if (earliestFoundHeaderPos < 0) {
            earliestFoundHeaderPos = parent.getChildAdapterPosition(parent[0]) + 1
        }

        for (headerPos in timeSlots.keys.reversed()) {
            if (headerPos < earliestFoundHeaderPos) {
                timeSlots[headerPos]?.let {
                    val top = (prevHeaderTop - it.height).coerceAtMost(paddingTop)
                    c.withTranslation(y = top.toFloat()) {
                        it.draw(c)
                    }
                }
                break
            }
        }
    }

    private fun createHeader(startTime: ZonedDateTime): StaticLayout {
        val text = if (startTime.minute == 0) {
            SpannableStringBuilder(hourFormatter.format(startTime))
        } else {
            // Use a smaller text size and different pattern if event does not start on the hour
            SpannableStringBuilder().apply {
                inSpans(AbsoluteSizeSpan(hourMinTextSize)) {
                    append(hourMinFormatter.format(startTime))
                }
            }
        }.apply {
            append(System.lineSeparator())
            inSpans(AbsoluteSizeSpan(meridiemTextSize), StyleSpan(BOLD)) {
                append(meridiemFormatter.format(startTime).toUpperCase())
            }
        }
        return StaticLayout(text, paint, width, ALIGN_CENTER, 1f, 0f, false)
    }
}

fun indexSessionHeaders(sessions: List<SessionWithData>, zoneId: ZoneId): List<Pair<Int, ZonedDateTime>> {
    return sessions
            .mapIndexed { index, session ->
                index to ETimeUtils.zonedTime(session.session.startTime, zoneId)
            }
            .distinctBy { it.second.hour to it.second.minute }
}