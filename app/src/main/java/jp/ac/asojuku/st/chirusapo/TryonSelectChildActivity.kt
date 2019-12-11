package jp.ac.asojuku.st.chirusapo

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_tryon_select_child.*

class TryonSelectChildActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tryon_select_child)

        val intent = intent
        val model_child: ArrayList<String> = intent.getStringArrayListExtra("MODEL_LIST")

        class GridAdapter(
            private val context: Context,
            private val list: ArrayList<String>?
        ) : BaseAdapter() {

            override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View {
                val imageView = ImageView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(500, 500)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setPadding(10, 10, 10, 10)
                }

                Picasso.get().load(list!![i]).into(imageView)

                return imageView
            }

            override fun getItem(i: Int): Any {
                return list!![i]
            }

            override fun getItemId(i: Int): Long {
                return i.toLong()
            }

            override fun getCount(): Int {
                return list!!.size
            }
        }

        val gridView = child_grid_view
        gridView.adapter = GridAdapter(this, model_child)

        gridView.setOnItemClickListener { adapterView, view, i, l ->
            intent.putExtra("MODEL_STRING", model_child[i].split("file://").last())
            setResult(Activity.RESULT_OK,intent)
            finish()
        }
    }
}
