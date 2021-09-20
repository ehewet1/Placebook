package com.raywenderlich.placebook.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.raywenderlich.placebook.model.Bookmark
import com.raywenderlich.placebook.repository.BookmarkRepo
import com.raywenderlich.placebook.util.ImageUtils

class MapsViewModel(application: Application): AndroidViewModel(application){

    private val TAG = "MapsViewModel"

    private val bookmarkRepo:BookmarkRepo= BookmarkRepo(
        getApplication())
    private var bookmarks:LiveData<List<BookmarkView>>?
        = null

    fun addBookmarkFromPlace(place: Place,image:Bitmap?){

        val bookmark=bookmarkRepo.createBookmark()
        bookmark.placeId=place.id
        bookmark.name=place.name.toString()
        bookmark.longitude=place.latLng?.longitude?:0.0
        bookmark.latitude=place.latLng?.latitude?:0.0
        bookmark.phone=place.phoneNumber.toString()
        bookmark.address=place.address.toString()
        bookmark.category=getPlaceCategory(place)

        val newId=bookmarkRepo.addBookmark(bookmark)
        image?.let{bookmark.setImage(it, getApplication())}

        Log.i(TAG,"New bookmark $newId added to the database.")
    }

    fun getBookmarkViews():
            LiveData<List<BookmarkView>>?{
        if (bookmarks==null){
            mapBookmarksToBookMarkView()
        }
        return bookmarks
    }

    private fun mapBookmarksToBookMarkView(){
        bookmarks = Transformations.map(bookmarkRepo.allBookmarks) {repoBookmarks->
            repoBookmarks.map{bookmark->
                bookmarkToBookmarkView(bookmark)
            }
        }
    }

    private fun bookmarkToBookmarkView(bookmark: Bookmark): MapsViewModel.BookmarkView{
        return MapsViewModel.BookmarkView(
            bookmark.id,
            LatLng(bookmark.latitude, bookmark.longitude),
            bookmark.name,
            bookmark.phone,
            bookmarkRepo.getCategoryResourceId(bookmark.category))
    }

    private fun getPlaceCategory(place: Place): String{
        var category = "Other"
        val types = place.types

        types?.let{placeTypes->

            if (placeTypes.size>0){
                val placeType = placeTypes[0]
                category=bookmarkRepo.placeTypeToCategory(placeType)
            }
        }
        return category
    }

    fun addBookmark(latLng: LatLng): Long? {
        val bookmark = bookmarkRepo.createBookmark()
        bookmark.name="Untitled"
        bookmark.longitude=latLng.longitude
        bookmark.latitude=latLng.latitude
        bookmark.category="Other"
        return bookmarkRepo.addBookmark(bookmark)
    }

    data class BookmarkView(
        val id: Long?=null,
        val location: LatLng = LatLng(0.0,0.0),
        val name: String="",
        val phone: String="",
        val categoryResourceId: Int?=null){

        fun getImage(context: Context)=id?.let{
            ImageUtils.loadBitmapFromFile(context,
                Bookmark.generateImageFilename(it))
        }
    }
}