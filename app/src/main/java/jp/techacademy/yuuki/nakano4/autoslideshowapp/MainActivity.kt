package jp.techacademy.yuuki.nakano4.autoslideshowapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.database.Cursor
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Handler
import java.util.*

class MainActivity : AppCompatActivity(),View.OnClickListener {

    private val PERMISSION_REQUEST_CODE = 100
    private var cursor :Cursor? = null

    private var mTimer: Timer? = null
    private var mTimerSec = 0.0

    private var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getContentsInfo()
                nextButton.setOnClickListener(this)
                prevButton.setOnClickListener(this)
                playButton.setOnClickListener(this)
            } else {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),PERMISSION_REQUEST_CODE)
            }
        } else {
            getContentsInfo()
        }
    }

    override fun onDetachedFromWindow() {
        cursor!!.close()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode){
            PERMISSION_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    override fun onClick(v: View){
        when(v.id){
            R.id.nextButton -> {
                nextImage()
            }
            R.id.prevButton -> {
                prevImage()
            }
            R.id.playButton -> {
                playSlideshow()
            }
        }
    }

    private fun getContentsInfo() {
        val resolver = contentResolver
        cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )
        if (cursor!!.moveToFirst()) {
            setimageview()
        }
    }

    private fun nextImage(){
        if (!cursor!!.moveToNext()) {
            cursor!!.moveToFirst()
        }
        setimageview()

    }

    private fun prevImage(){
        if (!cursor!!.moveToPrevious()) {
            cursor!!.moveToLast()
        }
        setimageview()
    }

    private fun playSlideshow(){
        if (mTimer == null) {
            mTimer = Timer()

            mTimer!!.schedule(object : TimerTask() {
                override fun run() {
                    mTimerSec += 0.1
                    mHandler.post {
                        if((mTimerSec * 10).toInt() % 20 == 0){
                            nextImage()
                        }
                        timer.text = String.format("%.1f", mTimerSec)
                    }
                }
            }, 100, 100)
            playButton.text = "停止"
            nextButton.isClickable = false
            prevButton.isClickable = false
        }else{
            mTimer!!.cancel()
            mTimer = null
            playButton.text = "再生"
            nextButton.isClickable = true
            prevButton.isClickable = true
        }
    }

    private fun setimageview() {
        val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
        val id = cursor!!.getLong(fieldIndex)
        val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

        imageView.setImageURI(imageUri)
    }
}
