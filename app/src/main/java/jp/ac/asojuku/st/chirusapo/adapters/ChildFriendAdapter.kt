package jp.ac.asojuku.st.chirusapo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import jp.ac.asojuku.st.chirusapo.R

class ChildFriendAdapter(context: Context) : BaseAdapter() {

    private var layoutInflater: LayoutInflater =
        context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private lateinit var item: ArrayList<ChildFriendList>

    override fun getCount(): Int {
        return try {
            item.size
        } catch (e: Exception) {
            0
        }
    }

    fun setSampleListItem(item: ArrayList<ChildFriendList>) {
        this.item = item
    }

    override fun getItem(position: Int): Any {
        return item[position]
    }

    override fun getItemId(position: Int): Long {
        return item[position].id
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = layoutInflater.inflate(R.layout.layout_child_frend_list, parent, false)

        val item = item[position]

        view.findViewById<TextView>(R.id.user_name).text = item.userName

        if (!item.userIcon.isNullOrEmpty()) {
            Picasso.get().load(item.userIcon).into(view.findViewById<ImageView>(R.id.user_icon))
        }

        return view
    }
}