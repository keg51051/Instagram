package com.example.instagram.navigation.model

data class ContentDTO(var explanation : String? = null,
                      var imageUrl : String? = null,
                      var uid : String? = null,
                      var userId : String? = null,
                      var timestamp : Long? = null,
                      var favoriteCount : Int = 0,
                      var favorites : Map<String, Boolean> = HashMap()) {
    data class Comment(var uid: String? = null,
                       var userId: String? = null,
                       var comment : String? = null,
                       var timestamp: Long? = null)
}