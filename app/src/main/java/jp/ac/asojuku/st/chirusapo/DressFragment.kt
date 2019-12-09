package jp.ac.asojuku.st.chirusapo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class DressFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_dress, container, false)

        val arrayAdapter = ArrayAdapter<String>(activity!!, android.R.layout.simple_list_item_1).apply {
            add("子供モデルを登録")
            add("試着")
            add("試着後一覧")
        }
        val listView = view.findViewById<ListView>(R.id.ARListView)
        listView.adapter = arrayAdapter
        listView.setOnItemClickListener {parent, view, position, id ->
            // 項目の TextView を取得
            val itemTextView : TextView = view.findViewById(android.R.id.text1)

            // 項目のラベルテキストをログに表示
            Log.i("debug", itemTextView.text.toString())

            // それぞれの画面に遷移
            //RegistrationChildModelは子供モデル登録の画面
            when(itemTextView.text.toString()){
                "子供モデルを登録" -> {
                    val intent = Intent(activity,RegistrationChildModel::class.java)
                    startActivity(intent)
                }
                "試着" -> {
                    val intent = Intent(activity,TryonActivity::class.java)
                    startActivity(intent)
                }
                "試着後一覧" -> {
                    val intent = Intent(activity,MyActivity::class.java)
                    startActivity(intent)
                }
            }
        }
        return view
    }

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
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

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DressFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
