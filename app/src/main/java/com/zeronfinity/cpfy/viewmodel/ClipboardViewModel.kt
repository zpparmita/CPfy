package com.zeronfinity.cpfy.viewmodel

import android.app.Application
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.URLSpan
import androidx.core.content.ContextCompat.getColor
import androidx.core.text.bold
import androidx.core.text.color
import androidx.core.text.italic
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zeronfinity.core.entity.Contest
import com.zeronfinity.core.logger.logD
import com.zeronfinity.core.usecase.GetFilteredContestListUseCase
import com.zeronfinity.core.usecase.GetPlatformUseCase
import com.zeronfinity.cpfy.R
import com.zeronfinity.cpfy.common.makeDurationText
import com.zeronfinity.cpfy.viewmodel.helpers.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Locale

class ClipboardViewModel @ViewModelInject constructor(
    private val application: Application,
    private val getFilteredContestListUseCase: GetFilteredContestListUseCase,
    private val getPlatformUseCase: GetPlatformUseCase
) : ViewModel() {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val simpleDateFormat = SimpleDateFormat(
        "hh:mm a, dd-MMM-yy",
        Locale.getDefault()
    )

    private val selectedContests = ArrayList<Contest>()

    private val _clipboardTextLiveData = MutableLiveData<CharSequence>()
    val clipboardTextLiveData: LiveData<CharSequence>
        get() = _clipboardTextLiveData

    private val _isProgressVisibleLiveDataEv = MutableLiveData<Event<Boolean>>()
    val isProgressVisibleLiveDataEv: LiveData<Event<Boolean>>
        get() = _isProgressVisibleLiveDataEv

    fun fetchClipboardText() {
        logD("fetchClipboardText() started")
        _isProgressVisibleLiveDataEv.postValue(Event(true))

        coroutineScope.launch {
            val contestList = when (selectedContests.isEmpty()) {
                true -> getFilteredContestListUseCase()
                false -> selectedContests
            }

            var clipboardText: CharSequence = ""

            for ((index, contest) in contestList.withIndex()) {

                if (clipboardText.isNotEmpty()) {
                    clipboardText = TextUtils.concat(clipboardText, "\n")
                }

                val spannableString = SpannableStringBuilder()
                    .bold {
                        getPlatformUseCase(contest.platformId)?.let{
                            append("${index + 1}. ${it.shortName}:")
                        }
                    }
                    .append(" ${contest.name}\n")
                    .italic { append(application.getString(R.string.starts_at_colon_tv_label)) }
                    .append(
                        " ${
                            simpleDateFormat.format(
                                contest.startTime
                            )
                        }\n"
                    )
                    .italic { append(application.getString(R.string.duration_colon_tv_label)) }
                    .append(
                        " ${
                            makeDurationText(
                                contest.duration
                            )
                        }\n"
                    )
                    .italic { append(application.getString(R.string.link_colon)) }
                    .append(" ")
                    .color(getColor(application, R.color.primaryDarkColor)) {
                        append(contest.url, URLSpan(contest.url), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    .append("\n")

                clipboardText = TextUtils.concat(clipboardText, spannableString)
            }

            if (clipboardText.isNotEmpty()) {
                clipboardText = TextUtils.concat(clipboardText, "\n" + application.getString(R.string.generated_by_cpfy))
            }

            _clipboardTextLiveData.postValue(clipboardText)
            _isProgressVisibleLiveDataEv.postValue(Event(false))
        }
    }

    fun setSelectedContests(list: List<Contest>) {
        selectedContests.clear()
        selectedContests.addAll(list)
    }
}
