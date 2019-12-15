package jp.ac.asojuku.st.chirusapo

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import jp.ac.asojuku.st.chirusapo.apis.Api
import jp.ac.asojuku.st.chirusapo.apis.ApiError
import jp.ac.asojuku.st.chirusapo.apis.ApiParam
import jp.ac.asojuku.st.chirusapo.apis.ApiPostTask
import kotlinx.android.synthetic.main.activity_calendar_edit.*
import java.util.*

class CalendarEditActivity : AppCompatActivity() {
    private lateinit var realm: Realm
    private lateinit var userToken: String
    private lateinit var groupId: String
    private lateinit var calendarId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_edit)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
            title = "スケジュール編集"
        }

        realm = Realm.getDefaultInstance()

        val account = realm.where(Account::class.java).findFirst()
        val group = realm.where(JoinGroup::class.java).equalTo("Rgroup_flag", 1.toInt()).findFirst()

        if (account != null && group != null) {
            userToken = account.Rtoken
            groupId = group.Rgroup_id
        }

        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")
        val date = intent.getStringExtra("date")
        calendarId = intent.getStringExtra("calendar_id") as String
        check_box_remind_flg.isChecked = intent.getStringExtra("remind_flg") == "1"

        schedule_title.editText?.setText(title)
        schedule_detail.editText?.setText(content)
        schedule_date.editText?.setText(date)

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

        button_schedule_edit.setOnClickListener {
            onScheduleEdit()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()

        realm.close()
    }



    private fun validationTitle(): Boolean {
        val title = schedule_title.editText?.text.toString()

        return when {
            title.isEmpty() -> {
                schedule_title.error = "タイトルが入力されていません"
                false
            }
            title.count() > 30 -> {
                schedule_title.error = "30文字以下で入力してください"
                false
            }
            else -> {
                schedule_title.error = null
                true
            }
        }
    }

    private fun validationDetail(): Boolean {
        val detail = schedule_detail.editText?.text.toString()

        return when {
            detail.isEmpty() -> {
                schedule_detail.error = "内容が入力されていません"
                false
            }
            detail.count() > 200 -> {
                schedule_detail.error = "200文字以下で入力してください"
                false
            }
            else -> {
                schedule_detail.error = null
                true
            }
        }
    }

    private fun validationDate(): Boolean {
        val detail = schedule_date.editText?.text.toString()

        return when {
            detail.isEmpty() -> {
                schedule_date.error = "日付が入力されていません"
                false
            }
            else -> {
                schedule_date.error = null
                true
            }
        }
    }

    private fun onScheduleEdit() {
        var check = true
        if (!validationTitle()) check = false
        if (!validationDetail()) check = false
        if (!validationDate()) check = false

        if (!check) return

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
                        Snackbar.make(root_view, "スケジュールを変更しました", Snackbar.LENGTH_SHORT).show()
                    }
                    "400" -> {
                        val errorArray = jsonObject.getJSONArray("message")
                        for (i in 0 until errorArray.length()) {
                            when (errorArray.getString(i)) {
                                ApiError.REQUIRED_PARAM,
                                ApiError.UNKNOWN_GROUP,
                                ApiError.UNAUTHORIZED_OPERATION,
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
                Api.SLIM + "calendar/edit",
                hashMapOf(
                    "token" to userToken,
                    "group_id" to groupId,
                    "calendar_id" to calendarId,
                    "title" to title,
                    "content" to content,
                    "date" to date,
                    "remind_flg" to remindFlg
                )
            )
        )
    }
}
