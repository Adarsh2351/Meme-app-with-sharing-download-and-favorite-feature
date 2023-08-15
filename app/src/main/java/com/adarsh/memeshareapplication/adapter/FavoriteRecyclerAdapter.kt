package com.adarsh.memeshareapplication.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.adarsh.memeshareapplication.R
import com.adarsh.memeshareapplication.activity.MainActivity
import com.adarsh.memeshareapplication.database.MemeEntity
import com.squareup.picasso.Picasso

class FavoriteRecyclerAdapter(val context: Context, val memeList: List<MemeEntity>) :
    RecyclerView.Adapter<FavoriteRecyclerAdapter.FavoriteViewHolder>() {

    class FavoriteViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val imgFavMeme: ImageButton = view.findViewById(R.id.imgFavMeme)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_favorite_single_row, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {

        val meme = memeList[position]
        Picasso.get().load(meme.memeImage).into(holder.imgFavMeme)


        holder.imgFavMeme.setOnClickListener {

            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("meme_image", meme.memeImage)
            intent.putExtra("meme_ups", meme.ups)
            val flag = 1
            intent.putExtra("flag", flag)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return memeList.size
    }
}
