package com.example.instagram.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.instagram.R
import com.example.instagram.navigation.model.AlarmDTO
import com.example.instagram.navigation.model.ContentDTO
import com.example.instagram.navigation.model.FollowDTO
import com.facebook.all.All
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_datail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

class DetailViewFragment : Fragment() {
    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_datail, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.detailviewfragment_recyclerview.adapter = DetailViewRecyclerViewAdapter()
        view.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }

    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        private var contentUidList: ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("images")?.orderBy("timestamp", Query.Direction.DESCENDING)
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    contentDTOs.clear()
                    contentUidList.clear()
                    //Sometimes, this code returns null of querySnapshot when it sign out
                    if (querySnapshot == null) return@addSnapshotListener
                    for (snapshot in querySnapshot!!.documents) {
                        var item = snapshot.toObject(ContentDTO::class.java)
                        contentDTOs.add(item!!)
                        contentUidList.add(snapshot.id)
                        notifyDataSetChanged()
                    }
                }
        }

            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
                var view = LayoutInflater.from(p0.context).inflate(R.layout.item_detail, p0, false)
                return CustomViewHolder(view)
            }

            private inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

            override fun getItemCount(): Int {
                return contentDTOs.size
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                var viewHolder = (holder as CustomViewHolder).itemView

                // Profile Images
                firestore?.collection("profileImages")?.document(contentDTOs[position].uid!!)
                    ?.get()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val url = task.result?.get("image")
                            Glide.with(holder.itemView.context)
                                .load(url)
                                .apply(RequestOptions().circleCrop())
                                .into(viewHolder.detailviewitem_profile_image)
                        }
                    }
                //When the profile image is clicked
                viewHolder.detailviewitem_profile_image.setOnClickListener {
                    var fragment = UserFragment()
                    var bundle = Bundle()
                    bundle.putString("destinationUid", contentDTOs[position].uid)
                    bundle.putString("userId", contentDTOs[position].userId)
                    fragment.arguments = bundle
                    activity?.supportFragmentManager?.beginTransaction()
                        ?.replace(R.id.main_content, fragment)?.commit()
                }

                //UserId
                viewHolder.detailviewitem_profile_textview.text = contentDTOs!![position].userId

                //Image
                Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl)
                    .into(viewHolder.detailviewitem_imageview_content)

                //Explanation of content
                viewHolder.detailviewitem_explain_textview.text =
                    contentDTOs!![position].explanation

                //likes
                viewHolder.detailviewitem_favoritecounter_textview.text =
                    "Likes " + contentDTOs!![position].favoriteCount

                //When the button is clicked
                viewHolder.detailviewitem_favorite_imageview.setOnClickListener {
                    favoriteEvent(position)
                }

                //When the content image is long clicked, it works as Like button
                viewHolder.detailviewitem_imageview_content.setOnLongClickListener {
                    favoriteEvent(position)
                    true
                }

                //When the page is loaded
                if (contentDTOs!![position].favorites.containsKey(uid)) {
                    //Like status
                    viewHolder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite)
                } else {
                    //Unlike status
                    viewHolder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)
                }

                //When Comment icon is clicked
                viewHolder.detailviewitem_comment_imageview.setOnClickListener { v ->
                    var intent = Intent(v.context, CommentActivity::class.java)
                    intent.putExtra("contentUid", contentUidList[position])
                    intent.putExtra("destinationUid", contentDTOs[position].uid)
                    startActivity(intent)
                }
            }

            private fun favoriteEvent(position: Int) {
                var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
                firestore?.runTransaction { transaction ->

                    var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                    if (contentDTO!!.favorites.containsKey(uid)) {
                        //Like -> Unlike
                        contentDTO?.favoriteCount = contentDTO?.favoriteCount - 1
                        contentDTO?.favorites.remove(uid)
                    } else {
                        //Unlike -> Like
                        contentDTO?.favoriteCount = contentDTO?.favoriteCount + 1
                        contentDTO?.favorites[uid!!] = true
                        favoriteAlarm(contentDTOs[position].uid!!)
                    }
                    transaction.set(tsDoc, contentDTO)
                }
            }

            private fun favoriteAlarm(destinationUid: String) {
                var alarmDTO = AlarmDTO()
                alarmDTO.destinationUid = destinationUid
                alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
                alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
                alarmDTO.kind = 0
                alarmDTO.timestamp = System.currentTimeMillis()
                FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
            }

        }
    }