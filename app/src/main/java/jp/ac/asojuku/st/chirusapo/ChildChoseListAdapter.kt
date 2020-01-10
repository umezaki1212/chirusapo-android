package jp.ac.asojuku.st.chirusapo

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import jp.ac.asojuku.st.chirusapo.adapters.ChildChoseListItem

class ChildChoseListAdapter(context: Context) : BaseAdapter() {
    private var layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private lateinit var item: ArrayList<ChildChoseListItem>

    override fun getCount(): Int {
        return try {
            item.size
        } catch (e: Exception) {
            0
        }
    }

    fun setSampleListItem(item: ArrayList<ChildChoseListItem>) {
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
        val view = layoutInflater.inflate(R.layout.layout_simple_list, parent, false)

        val item = item[position]

        view.findViewById<TextView>(R.id.playList).text = item.playList

        return view
}
}