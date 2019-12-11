package jp.ac.asojuku.st.chirusapo

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiGetTask
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import kotlinx.android.synthetic.main.fragment_calendar.*
import org.json.JSONArray
import java.util.*

class CalendarFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var calendarObject: JSONArray
    private lateinit var realm: Realm
    private lateinit var userToken: String
    private lateinit var groupId: String
    private lateinit var calendarView: CompactCalendarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onResume() {
        super.onResume()

        realm = Realm.getDefaultInstance()

        val account = realm.where(Account::class.java).findFirst()
        val group = realm.where(JoinGroup::class.java).equalTo("Rgroup_flag", 1.toInt()).findFirst()

        if (account != null && group != null) {
            userToken = account.Rtoken
            groupId = group.Rgroup_id
        }

        getCalendarSchedule()

        val calendarTitle = Calendar.getInstance()
        calendar_title.text = "%s年 %s月 %s日".format(
            calendarTitle.get(Calendar.YEAR),
            calendarTitle.get(Calendar.MONTH) + 1,
            calendarTitle.get(Calendar.DATE)
        )

        calendarView = (calendar_view as CompactCalendarView).apply {
            setCurrentDate(Calendar.getInstance().time)
            setFirstDayOfWeek(Calendar.SUNDAY)
            setUseThreeLetterAbbreviation(true)
            shouldDrawIndicatorsBelowSelectedDays(true)
            setLocale(TimeZone.getDefault(), Locale.JAPAN)
        }
        calendarView.setListener(object : CompactCalendarView.CompactCalendarViewListener {
            override fun onDayClick(date: Date) {
                val events = calendarView.getEvents(date)
                val calendar = Calendar.getInstance().apply {
                    time = date
                }
                calendar_title.text = "%s年 %s月 %s日".format(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DATE)
                )

                val eventList = arrayListOf<String>()
                for (event in events) {
                    eventList.add(event.data.toString())
                }
                schedule_list.adapter = ArrayAdapter(activity!!, android.R.layout.simple_list_item_1, eventList)
            }

            override fun onMonthScroll(date: Date) {
                val calendar = Calendar.getInstance().apply {
                    time = date
                }
                calendar_title.text = "%s年 %s月 %s日".format(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DATE)
                )
            }
        })

        button_schedule_add.setOnClickListener {
            val intent = Intent(activity, CalendarScheduleAddActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else throw RuntimeException("$context must implement OnFragmentInteractionListener")
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    private fun getCalendarSchedule() {
        Snackbar.make(root_view, "スケジュールデータを取得しています…", Snackbar.LENGTH_LONG).show()
        ApiGetTask { jsonObject ->
            if (jsonObject == null) {
                ApiError.showSnackBar(root_view, ApiError.CONNECTION_ERROR, Snackbar.LENGTH_SHORT)
            } else {
                when (jsonObject.getString("status")) {
                    "200" -> {
                        val scheduleObject =
                            jsonObject.getJSONObject("data").getJSONArray("calendar_list")
                        calendarObject = scheduleObject

                        setEvent()
                    }
                    "400" -> {
                        val errorArray = jsonObject.getJSONArray("message")
                        for (i in 0 until errorArray.length()) {
                            when (errorArray.getString(i)) {
                                ApiError.REQUIRED_PARAM,
                                ApiError.UNKNOWN_GROUP,
                                ApiError.UNREADY_BELONG_GROUP -> {
                                    ApiError.showSnackBar(
                                        root_view,
                                        errorArray.getString(i),
                                        Toast.LENGTH_SHORT
                                    )
                                }
                                ApiError.UNKNOWN_TOKEN -> {
                                    val intent =
                                        Intent(activity, SignInActivity::class.java).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                    startActivity(intent)
                                }
                            }
                        }
                    }
                }
            }
        }.execute(
            ApiParam(
                Api.SLIM + "calendar/get",
                hashMapOf("token" to userToken, "group_id" to groupId)
            )
        )
    }

    private fun setEvent() {
        if (calendarObject.length() == 1) {
            val data = calendarObject.getJSONObject(0)

            try {
                val year = data.keys().next()
                val yearObject = data.getJSONObject(year)
                val month = yearObject.keys().next()
                val monthObject = yearObject.getJSONObject(month)
                val day = monthObject.keys().next()
                val dayObject = monthObject.getJSONArray(day)

                (0 until dayObject.length()).forEach { index ->
                    val obj = dayObject.getJSONObject(index)
                    val time = Calendar.getInstance().apply {
                        val splitDate = obj.getString("date").split("-")
                        set(splitDate[0].toInt(), splitDate[1].toInt() - 1, splitDate[2].toInt())
                    }.timeInMillis
                    val event = Event(Color.GREEN, time, obj.getString("title"))
                    calendarView.addEvent(event)
                }
            } catch (e: Exception) {
                Snackbar.make(root_view, "スケジュールデータを処理できませんでした", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
