package jp.ac.asojuku.st.chirusapo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso

class DressModelViewerFragment : Fragment() {
    private var list: java.util.ArrayList<String>? = arrayListOf()
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            list = it.getStringArrayList("list")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dress_model_viewer, container, false)

        class GridAdapter(
            private val context: Context,
            private val list: java.util.ArrayList<String>?
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

        val gridView = view.findViewById<GridView>(R.id.grid_view)
        gridView.adapter = GridAdapter(activity!!, list)

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener

    companion object {
        @JvmStatic
        fun newInstance(list: ArrayList<String>) =
            DressModelViewerFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList("list", list)
                }
            }
    }
}
