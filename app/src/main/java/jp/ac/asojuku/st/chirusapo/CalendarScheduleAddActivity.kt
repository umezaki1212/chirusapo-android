package jp.ac.asojuku.st.chirusapo

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import jp.ac.asojuku.st.chirusapo.apis.ApiPostTask
import kotlinx.android.synthetic.main.activity_calendar_schedule_add.*
import java.util.*

class CalendarScheduleAddActivity : AppCompatActivity() {
    private lateinit var realm: Realm
    private lateinit var userToken: String
    private lateinit var groupId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_schedule_add)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
            title = "スケジュール追加"
        }
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

        schedule_date.editText?.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { _, y, m, d ->
                    val year = y.toString()
                    var month = (m + 1).toString()
                    var day = d.toString()
                    if (m < 9) {
                        month = "0$m"
                    }
                    if (d < 10) {
                        day = "0$d"
                    }
                    schedule_date.editText?.setText("%s-%s-%s".format(year, month, day))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        button_schedule_add.setOnClickListener {
            onScheduleAdd()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        realm.close()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun onScheduleAdd() {
        val title = schedule_title.editText?.text.toString()
        val content = schedule_detail.editText?.text.toString()
        val date = schedule_date.editText?.text.toString()
        val remindFlg = if (check_box_remind_flg.isChecked) {
            "1"
        } else {
            "0"
        }

        ApiPostTask { jsonObject ->
            if (jsonObject == null) {
                ApiError.showSnackBar(root_view, ApiError.CONNECTION_ERROR, Snackbar.LENGTH_SHORT)
            } else {
                when (jsonObject.getString("status")) {
                    "200" -> {
                        Toast.makeText(this, "スケジュールを追加しました", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    "400" -> {
                        val errorArray = jsonObject.getJSONArray("message")
                        for (i in 0 until errorArray.length()) {
                            when (errorArray.getString(i)) {
                                ApiError.REQUIRED_PARAM,
                                ApiError.UNKNOWN_GROUP,
                                ApiError.UNREADY_BELONG_GROUP,
                                "VALIDATION_TITLE",
                                "VALIDATION_CONTENT",
                                "VALIDATION_DATE",
                                "VALIDATION_REMIND_FLG" -> {
                                    ApiError.showSnackBar(
                                        root_view,
                                        errorArray.getString(i),
                                        Toast.LENGTH_SHORT
                                    )
                                }
                                ApiError.UNKNOWN_TOKEN -> {
                                    val intent =
                                        Intent(this, SignInActivity::class.java).apply {
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
                Api.SLIM + "calendar/add",
                hashMapOf(
                    "token" to userToken,
                    "group_id" to groupId,
                    "title" to title,
                    "content" to content,
                    "date" to date,
                    "remind_flg" to remindFlg
                )
            )
        )
    }
}
