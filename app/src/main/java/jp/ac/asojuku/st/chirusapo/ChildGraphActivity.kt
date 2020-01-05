package jp.ac.asojuku.st.chirusapo

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
//import jp.ac.asojuku.st.chirusapo.chart_data.BarChartItem
import jp.ac.asojuku.st.chirusapo.chart_data.ChartItem
import kotlinx.android.synthetic.main.activity_child_graff.*
import java.util.*

class ChildGraphActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_child_graff)

        title = "ListViewMultiChartActivity"

        val lv = listView1

        val list = ArrayList<ChartItem>()

//        list.add(
//            BarChartItem(
//                generateDataBar(1),
//                applicationContext
//            )
//        )
//        list.add(
//            BarChartItem(
//                generateDataBar(2),
//                applicationContext
//            )
//        )
//        list.add(
//            BarChartItem(
//                generateDataBar(3),
//                applicationContext
//            )
//        )
//        list.add(
//            BarChartItem(
//                generateDataBar(4),
//                applicationContext
//            )
//        )

        val cda = ChartDataAdapter(applicationContext, list)
        lv.adapter = cda
    }

    /** adapter that supports 3 different item types  */
    private inner class ChartDataAdapter internal constructor(
        context: Context,
        objects: List<ChartItem>
    ) :
        ArrayAdapter<ChartItem>(context, 0, objects) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

            return getItem(position)!!.getView(position, convertView!!, context)
        }

        override fun getItemViewType(position: Int): Int {
            // return the views type
            val ci = getItem(position)
            return ci?.getItemType() ?: 0
        }

        override fun getViewTypeCount(): Int {
            return 3 // we have 3 different item-types
        }
    }

    private fun generateDataBar(cnt: Int): BarData {

        val entries = ArrayList<BarEntry>()

        for (i in 0..11) {
            entries.add(BarEntry(i.toFloat(), ((Math.random() * 70).toInt() + 30).toFloat()))
        }

        val d = BarDataSet(entries, "New DataSet $cnt")
        d.setColors(*ColorTemplate.VORDIPLOM_COLORS)
        d.highLightAlpha = 255

        val cd = BarData(d)
        cd.barWidth = 0.9f
        return cd
    }


    fun saveToGallery() { /* Intentionally left empty */
    }
}