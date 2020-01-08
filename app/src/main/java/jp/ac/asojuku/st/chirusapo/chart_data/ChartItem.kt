package jp.ac.asojuku.st.chirusapo.chart_data

import android.content.Context
import android.view.View

import com.github.mikephil.charting.data.ChartData

/**
 * Base class of the Chart ListView items
 * @author philipp
 */
abstract class ChartItem internal constructor(internal var mChartData: ChartData<*>) {

    abstract fun getView(position: Int, convertView: View, c: Context): View

    companion object {

        internal val TYPE_BARCHART = 0
        internal val TYPE_LINECHART = 1
        internal val TYPE_PIECHART = 2
    }

    abstract fun getItemType(): Int
}
