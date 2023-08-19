package com.adarsh.memeshareapplication.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.room.Room
import com.adarsh.memeshareapplication.R
import com.adarsh.memeshareapplication.database.MemeDatabase
import com.adarsh.memeshareapplication.database.MemeEntity
import com.adarsh.memeshareapplication.util.ConnectionManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var btnShare: ImageButton
    private lateinit var btnDownload: ImageButton
    private lateinit var btnFavorite: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var imgMeme: ImageView
    private lateinit var progressBar: ProgressBar

    var currentImageUrl: String? = null
    var ups: Long = 0
    var flag2: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        imgMeme = findViewById(R.id.imgMeme)
        btnDownload = findViewById(R.id.btnDownload)
        btnShare = findViewById(R.id.btnShare)
        btnFavorite = findViewById(R.id.btnFavorite)
        btnNext = findViewById(R.id.btnNext)
        progressBar = findViewById(R.id.progressBar)


        if (intent != null) {

            flag2 = intent.getIntExtra("flag", flag2)

            if (flag2 == 1) {
                currentImageUrl = intent.getStringExtra("meme_image")
                ups = intent.getLongExtra("meme_ups", ups)
                loadCurrentMeme()
            }

        }

        if (flag2 == 0) {
            loadMeme()
        }


        btnShare.setOnClickListener {

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(
                Intent.EXTRA_TEXT,
                "Hey! Checkout this cool meme I got from Reddit $currentImageUrl"
            )

            val chooser = Intent.createChooser(intent, "Share this meme using...")
            startActivity(chooser)

        }

        btnFavorite.setOnLongClickListener {
            intent = Intent(this@MainActivity, FavoriteMemesActivity::class.java)
            startActivity(intent)
            true
        }

        btnNext.setOnClickListener {
            loadMeme()
        }


    }

    private fun loadCurrentMeme() {

        val memeEntity = MemeEntity(
            ups,
            currentImageUrl.toString()
        )
        val checkFav = DBAsyncTask(applicationContext, memeEntity, 1).execute()
        val isFav = checkFav.get()

        if (isFav) {
            btnFavorite.setImageResource(R.drawable.ic_favorite_added)
        } else {
            btnFavorite.setImageResource(R.drawable.ic_favorite)
        }



        Glide.with(this).load(currentImageUrl)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.visibility = View.GONE
                    return false
                }
            }).into(imgMeme)


        btnFavorite.setOnClickListener {


            if (!DBAsyncTask(applicationContext, memeEntity, 1).execute()
                    .get()
            ) {

                val async =
                    DBAsyncTask(applicationContext, memeEntity, 2).execute()
                val result = async.get()
                if (result) {
                    Toast.makeText(
                        this,
                        "Meme added to Favorites.\nLong-press \"♥\" icon to see Favorites",
                        Toast.LENGTH_SHORT
                    ).show()
                    btnFavorite.setImageResource(R.drawable.ic_favorite_added)
                } else {
                    Toast.makeText(
                        this,
                        "Some Error has Occurred",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                val async =
                    DBAsyncTask(applicationContext, memeEntity, 3).execute()
                val result = async.get()

                if (result) {
                    Toast.makeText(
                        this,
                        "Meme removed from Favorites",
                        Toast.LENGTH_SHORT
                    ).show()
                    btnFavorite.setImageResource(R.drawable.ic_favorite)
                } else {
                    Toast.makeText(
                        this,
                        "Some Error has Occurred",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

        }
        btnDownload.setOnClickListener {

            if (currentImageUrl?.contains(".gif") == true) {

                Toast.makeText(
                    this@MainActivity,
                    "Meme Downloaded & saved to gallery",
                    Toast.LENGTH_SHORT
                ).show()
                CoroutineScope(Dispatchers.IO).launch {
                    saveGif(
                        Glide.with(this@MainActivity)
                            .asGif()
                            .load(currentImageUrl) // sample image
                            .placeholder(android.R.drawable.progress_indeterminate_horizontal) // need placeholder to avoid issue like glide annotations
                            .error(android.R.drawable.stat_notify_error) // need error to avoid issue like glide annotations
                            .submit()
                            .get()
                    )
                }

            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Meme Downloaded & saved to gallery",
                    Toast.LENGTH_SHORT
                ).show()
                CoroutineScope(Dispatchers.IO).launch {
                    saveImage(
                        Glide.with(this@MainActivity)
                            .asBitmap()
                            .load(currentImageUrl) // sample image
                            .placeholder(android.R.drawable.progress_indeterminate_horizontal) // need placeholder to avoid issue like glide annotations
                            .error(android.R.drawable.stat_notify_error) // need error to avoid issue like glide annotations
                            .submit()
                            .get()
                    )
                }
            }
        }
    }

    private fun loadMeme() {
        if (ConnectionManager().checkConnectivity(this@MainActivity)) {
            progressBar.visibility = View.VISIBLE

            val url = "https://meme-api.com/gimme"

            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, url, null,
                { response ->
                    try {
                        currentImageUrl = response.getString("url")
                        ups = response.getLong("ups")

                        loadCurrentMeme()


                    } catch (e: Exception) {
                        Toast.makeText(this, "Some Error has Occurred!!!", Toast.LENGTH_SHORT)
                            .show()
                    }

                },
                {

                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()

                })

            // Add the request to the RequestQueue.
            MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
        } else {
            val dialog = AlertDialog.Builder(this@MainActivity)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection is not Found")
            dialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()
            }
            dialog.setNegativeButton("Cancel") { _, _ ->
                ActivityCompat.finishAffinity(this@MainActivity)
            }
            dialog.create()
            dialog.show()
        }

    }

    private fun saveImage(image: Bitmap): String? {

        var savedImagePath: String? = null
        val imageFileName = "Meme " + Random.nextInt() + ".jpg"
        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/Memes"
        )
        var success = true
        if (!storageDir.exists()) {
            success = storageDir.mkdirs()
        }
        if (success) {
            val imageFile = File(storageDir, imageFileName)
            savedImagePath = imageFile.absolutePath
            try {
                val fOut: OutputStream = FileOutputStream(imageFile)
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                fOut.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            //Adding image to system gallery
            galleryAddPic(savedImagePath)
        }
        return savedImagePath
    }

    private fun saveGif(image: GifDrawable): String? {

        var savedImagePath: String? = null
        val imageFileName = "Meme " + Random.nextInt() + ".gif"
        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/Memes"
        )
        var success = true
        if (!storageDir.exists()) {
            success = storageDir.mkdirs()
        }
        if (success) {
            val imageFile = File(storageDir, imageFileName)
            savedImagePath = imageFile.absolutePath
            try {
                val fOut: OutputStream = FileOutputStream(imageFile)
                val gifBuffer = image.buffer
                val bytes = ByteArray(gifBuffer.capacity())
                (gifBuffer.clear() as ByteBuffer).get(bytes)

                fOut.write(bytes, 0, bytes.size)
                fOut.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            //Adding gif to system gallery
            galleryAddPic(savedImagePath)
        }
        return savedImagePath
    }

    private fun galleryAddPic(imagePath: String?) {
        imagePath?.let { path ->
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val f = File(path)
            val contentUri: Uri = Uri.fromFile(f)
            mediaScanIntent.data = contentUri
            sendBroadcast(mediaScanIntent)
        }

    }


    class DBAsyncTask(
        val context: Context,
        private val memeEntity: MemeEntity,
        private val mode: Int
    ) :
        AsyncTask<Void, Void, Boolean>() {

        val db = Room.databaseBuilder(context, MemeDatabase::class.java, "memes-db").build()

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: Void?): Boolean {

            when (mode) {

                1 -> {
                    val meme: MemeEntity? = db.memeDao().getMemeById(memeEntity.ups.toString())
                    db.close()
                    return meme != null
                }

                2 -> {
                    db.memeDao().insertMeme(memeEntity)
                    db.close()
                    return true
                }

                3 -> {
                    db.memeDao().deleteMeme(memeEntity)
                    db.close()
                    return true
                }
            }
            return false
        }

    }

    override fun onStop() {
        super.onStop()
        finish()
    }
}