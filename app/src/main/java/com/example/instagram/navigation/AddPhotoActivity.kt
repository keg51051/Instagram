package com.example.instagram.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.instagram.R
import com.example.instagram.navigation.model.ContentDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_GALLERY = 0
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_GALLERY)

        addphoto_btn_upload.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_GALLERY) {
            if(resultCode == Activity.RESULT_OK) {
                photoUri = data?.data
                addphoto_image.setImageURI(photoUri)
            } else {
                finish()
            }
        }
    }

    fun contentUpload() {
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"

        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        // Promise method
        storageRef?.putFile(photoUri!!)?.continueWithTask {
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            var contentDTO = ContentDTO()
            // Insert URL of image
            contentDTO.imageUrl = uri.toString()
            // Insert uid of users
            contentDTO.uid = auth?.currentUser?.uid
            // Insert userId
            contentDTO.userId = auth?.currentUser?.email
            // Insert explanation of content
            contentDTO.explanation = addphoto_edit_explain.text.toString()
            // Insert timestamp
            contentDTO.timestamp = System.currentTimeMillis()
            firestore?.collection("images")?.document()?.set(contentDTO)
            setResult(Activity.RESULT_OK)
            finish()
        }

        // Callback method
//        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
//            storageRef.downloadUrl.addOnSuccessListener { uri ->
//                var contentDTO = ContentDTO()
//                // Insert URL of image
//                contentDTO.imageUrl = uri.toString()
//                // Insert uid of users
//                contentDTO.uid = auth?.currentUser?.uid
//                // Insert userId
//                contentDTO.userId = auth?.currentUser?.email
//                // Insert explanation of content
//                contentDTO.explanation = addphoto_edit_explain.text.toString()
//                // Insert timestamp
//                contentDTO.timestamp = System.currentTimeMillis()
//                firestore?.collection("images")?.document()?.set(contentDTO)
//                setResult(Activity.RESULT_OK)
//                finish()
//            }
//        }
    }
}
