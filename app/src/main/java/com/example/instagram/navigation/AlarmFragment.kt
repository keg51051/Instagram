package com.example.instagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R
import com.example.instagram.navigation.model.AlarmDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.item_comment.view.*

class AlarmFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_alarm, container, false)
        var recyclerview = view.findViewById<RecyclerView>(R.id.alarmfragment_recyclerview)
        recyclerview.adapter = AlarmRecyclerViewAdapter()
        recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }

    inner class AlarmRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val alarmDTOList = ArrayList<AlarmDTO>()

        init {
            var uid = FirebaseAuth.getInstance().currentUser!!.uid
            FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid", uid).orderBy("timestamp").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                alarmDTOList.clear()
                if(querySnapshot == null) return@addSnapshotListener
                for(snapshot in querySnapshot.documents!!) {
                    alarmDTOList.add(snapshot.toObject(AlarmDTO::class.java)!!)
                }
                alarmDTOList.sortBy { it.timestamp }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View?) : RecyclerView.ViewHolder(view!!)

        override fun getItemCount(): Int {
            return alarmDTOList.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val commentTextView = holder.itemView.commentviewitem_textview_profile

            when (alarmDTOList[position].kind) {
                0 -> {
                    val str_0 = alarmDTOList[position].userId + getString(R.string.alarm_favorite)
                    commentTextView.text = str_0
                }
                1 -> {
                    var str_1 = alarmDTOList[position].userId + getString(R.string.alarm_comment) +
                            alarmDTOList[position].message
                    commentTextView.text = str_1
                }
                2 -> {
                    val str_2 = alarmDTOList[position].userId + getString(R.string.alarm_follow)
                    commentTextView.text = str_2
                }
            }

        }

    }
}