package com.adarsh.memeshareapplication.activity

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.adarsh.memeshareapplication.R
import com.adarsh.memeshareapplication.adapter.FavoriteRecyclerAdapter
import com.adarsh.memeshareapplication.database.MemeDatabase
import com.adarsh.memeshareapplication.database.MemeEntity

class FavoriteMemesActivity : AppCompatActivity() {

    lateinit var recyclerFavorites: RecyclerView
    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: FavoriteRecyclerAdapter
    var dbMemeList = listOf<MemeEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_memes)

        recyclerFavorites = findViewById(R.id.recyclerFavourite)
        progressLayout = findViewById(R.id.progressLayout)
        progressBar = findViewById(R.id.progressBar)

        layoutManager = GridLayoutManager(this@FavoriteMemesActivity, 2)

        dbMemeList = RetrieveFavorites(this@FavoriteMemesActivity).execute().get()

        progressLayout.visibility = View.GONE
        recyclerAdapter = FavoriteRecyclerAdapter(this@FavoriteMemesActivity, dbMemeList)
        recyclerFavorites.adapter = recyclerAdapter
        recyclerFavorites.layoutManager = layoutManager

    }

    class RetrieveFavorites(val context: Context) : AsyncTask<Void, Void, List<MemeEntity>>() {

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: Void?): List<MemeEntity> {
            val db = Room.databaseBuilder(context, MemeDatabase::class.java, "memes-db").build()

            return db.memeDao().getAllMemes()
        }

    }

    override fun onStop() {
        super.onStop()
        finish()
    }
}
