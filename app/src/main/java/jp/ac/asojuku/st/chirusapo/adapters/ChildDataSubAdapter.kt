package jp.ac.asojuku.st.chirusapo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import jp.ac.asojuku.st.chirusapo.R

class ChildDataSubAdapter(context: Context):BaseAdapter() {

    private var layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private lateinit var item: ArrayList<ChildDataListSub>

    override fun getCount(): Int {
        return try {
            item.size
        } catch (e: Exception) {
            0
        }
    }

    fun setChildDataSubAdapter(item: ArrayList<ChildDataListSub>) {
        this.item = item
    }

    override fun getItem(position: Int): Any {
        return item[position]
    }

    override fun getItemId(position: Int): Long {
        return item[position].id
    }

    override fun isEnabled(position: Int): Boolean {
        return false
    }

    @SuppressLint("ViewHolder", "ResourceType")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = layoutInflater.inflate(R.layout.child_list_item_sub, parent, false)

        val item = item[position]

        view.findViewById<TextView>(R.id.child_data_title_sub).text = item.dataTitle

        return view
    }

}