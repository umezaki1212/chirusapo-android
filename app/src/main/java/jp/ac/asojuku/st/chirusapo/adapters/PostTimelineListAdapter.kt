package jp.ac.asojuku.st.chirusapo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.setPadding
import com.squareup.picasso.Picasso
import jp.ac.asojuku.st.chirusapo.R

class PostTimelineListAdapter(context: Context) : BaseAdapter() {
    private var _context: Context = context

    private var layoutInflater: LayoutInflater =
        context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private lateinit var item: ArrayList<PostTimelineListItem>

    override fun getCount(): Int {
        return try {
            item.size
        } catch (e: Exception) {
            0
        }
    }

    fun setPostTimelineList(item: ArrayList<PostTimelineListItem>) {
        this.item = item
    }

    override fun getItem(position: Int): Any {
        return item[position]
    }

    override fun getItemId(position: Int): Long {
        return item[position].id
    }

    @SuppressLint("ViewHolder", "ResourceType")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = layoutInflater.inflate(R.layout.layout_timeline_item, parent, false)

        val item = item[position]

        view.findViewById<TextView>(R.id.user_id).text = item.userId
        view.findViewById<TextView>(R.id.user_name).text = item.userName

        if (!item.userIcon.isNullOrEmpty()) {
            Picasso.get().load(item.userIcon).into(view.findViewById<ImageView>(R.id.user_icon))
        }

        if (!item.text.isNullOrEmpty()) {
            view.findViewById<TextView>(R.id.content).visibility = View.VISIBLE
            view.findViewById<TextView>(R.id.content).text = item.text
        }

        if (!item.image01.isNullOrEmpty()) {
            val linerLayout = view.findViewById<LinearLayout>(R.id.linear_layout)
            val layoutParams = ViewGroup.LayoutParams(500, 500)

            view.findViewById<HorizontalScrollView>(R.id.horizontal_scroll_view).visibility =
                View.VISIBLE

            val imageView01 = ImageView(_context)
            imageView01.id = 1
            imageView01.setPadding(10)
            imageView01.layoutParams = layoutParams
            imageView01.scaleType = ImageView.ScaleType.CENTER_CROP

            Picasso.get().load(item.image01).into(imageView01)
            linerLayout.addView(imageView01)

            if (!item.image02.isNullOrEmpty()) {
                val imageView02 = ImageView(_context)
                imageView02.id = 2
                imageView02.setPadding(10)
                imageView02.layoutParams = layoutParams
                imageView02.scaleType = ImageView.ScaleType.CENTER_CROP
                Picasso.get().load(item.image02).into(imageView02)
                linerLayout.addView(imageView02)

                if (!item.image03.isNullOrEmpty()) {
                    val imageView03 = ImageView(_context)
                    imageView03.id = 3
                    imageView03.setPadding(10)
                    imageView03.layoutParams = layoutParams
                    imageView03.scaleType = ImageView.ScaleType.CENTER_CROP
                    Picasso.get().load(item.image03).into(imageView03)
                    linerLayout.addView(imageView03)

                    if (!item.image04.isNullOrEmpty()) {
                        val imageView04 = ImageView(_context)
                        imageView04.id = 4
                        imageView04.setPadding(10)
                        imageView04.layoutParams = layoutParams
                        imageView04.scaleType = ImageView.ScaleType.CENTER_CROP
                        Picasso.get().load(item.image04).into(imageView04)
                        linerLayout.addView(imageView04)
                    }
                }
            }
        }

        return view
    }
}