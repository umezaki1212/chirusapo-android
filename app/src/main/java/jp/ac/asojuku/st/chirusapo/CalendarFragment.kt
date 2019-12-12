package jp.ac.asojuku.st.chirusapo

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
import kotlin.collections.ArrayList

class CalendarFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var calendarObject: JSONArray
    private lateinit var realm: Realm
    private lateinit var userToken: String
    private lateinit var userId: String
    private lateinit var groupId: String
    private lateinit var calendarView: CompactCalendarView
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        mSwipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener)
        mSwipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)

        return view
    }

    override fun onResume() {
        super.onResume()

        realm = Realm.getDefaultInstance()

        val account = realm.where(Account::class.java).findFirst()
        val group = realm.where(JoinGroup::class.java).equalTo("Rgroup_flag", 1.toInt()).findFirst()

        if (account != null && group != null) {
            userToken = account.Rtoken
            userId = account.Ruser_id
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

                val eventList = ArrayList<HashMap<String, String>>()
                for (event in events) {
                    val map = hashMapOf<String, String>()
                    val ec = try {
                        event.data as EventContent
                    } catch (e: Exception) {
                        return
                    }
                    map["id"] = ec.id.toString()
                    map["user_id"] = ec.userId
                    map["user_name"] = ec.userName
                    map["title"] = ec.title
                    map["content"] = ec.content
                    map["date"] = ec.date
                    eventList.add(map)
                }
                schedule_list.adapter = SimpleAdapter(
                    activity!!,
                    eventList,
                    android.R.layout.simple_list_item_2,
                    arrayOf("title", "content"),
                    intArrayOf(android.R.id.text1, android.R.id.text2)
                )
                schedule_list.setOnItemClickListener { _, _, i, _ ->
                    val viewEvent = eventList[i]
                    val intent = Intent(activity, CalendarViewActivity::class.java).apply {
                        putExtra("user_name", viewEvent["user_name"])
                        putExtra("title", viewEvent["title"])
                        putExtra("content", viewEvent["content"])
                        putExtra("date", viewEvent["date"])
                    }
                    startActivity(intent)
                }
                schedule_list.setOnItemLongClickListener { _, _, i, _ ->
                    if (Objects.equals(eventList[i]["user_id"], userId)) {
                        AlertDialog.Builder(activity!!).apply {
                            val menuItems =
                                resources.getStringArray(R.array.calendar_fragment_menu_dialog)
                            setItems(menuItems) { _: DialogInterface?, i: Int ->
                                when (menuItems[i]) {
                                    resources.getString(R.string.fragment_calendar_menu_edit) -> {
                                        Toast.makeText(activity, "編集", Toast.LENGTH_SHORT).show()
                                    }
                                    resources.getString(R.string.fragment_calendar_menu_delete) -> {
                                        Toast.makeText(activity, "削除", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            create()
                            show()
                        }
                    }
                    return@setOnItemLongClickListener true
                }
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
                val eventList = arrayListOf<String>()
                schedule_list.adapter =
                    ArrayAdapter(activity!!, android.R.layout.simple_list_item_1, eventList)
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

    private val mOnRefreshListener = SwipeRefreshLayout.OnRefreshListener {
        getCalendarSchedule()
    }

    private fun getCalendarSchedule() {
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

            mSwipeRefreshLayout.isRefreshing = false
        }.execute(
            ApiParam(
                Api.SLIM + "calendar/get",
                hashMapOf("token" to userToken, "group_id" to groupId)
            )
        )
    }

    private fun setEvent() {
        try {
            calendarView.removeAllEvents()
            val data = calendarObject

            (0 until data.length()).forEach { index ->
                val obj = data.getJSONObject(index)
                val time = Calendar.getInstance().apply {
                    set(obj.getInt("year"), obj.getInt("month") - 1, obj.getInt("day"))
                }.timeInMillis
                val content = EventContent(
                    obj.getInt("id"),
                    obj.getString("user_id"),
                    obj.getString("user_name"),
                    obj.getString("title"),
                    obj.getString("content"),
                    obj.getString("date")
                )
                val event = Event(Color.GREEN, time, content)
                calendarView.addEvent(event)
            }
        } catch (e: Exception) {
            Snackbar.make(root_view, "スケジュールデータを処理できませんでした", Snackbar.LENGTH_SHORT).show()
        }
    }

    data class EventContent(
        val id: Int,
        val userId: String,
        val userName: String,
        val title: String,
        val content: String,
        val date: String
    )
}
