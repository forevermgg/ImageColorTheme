package com.mgg.imagecolortheme

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.LinearLayoutCompat.HORIZONTAL
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import com.mgg.quantcolor.ColorQuant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var t1: TextView? = null
    private var t2: TextView? = null
    private var t3: TextView? = null
    private var t4: TextView? = null
    private var t5: TextView? = null
    private var t6: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        t1 = findViewById(R.id.t1)
        t2 = findViewById(R.id.t2)
        t3 = findViewById(R.id.t3)
        t4 = findViewById(R.id.t4)
        t5 = findViewById(R.id.t5)
        t6 = findViewById(R.id.t6)
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val image = BitmapFactory.decodeResource(resources,
                    R.mipmap.test_image_color_theme)
                val themeColor = getMainThemeColor(image)
                if (themeColor != null) {
                    withContext(Dispatchers.Main) {
                        setThemeColor(themeColor)
                        getPalette()
                    }
                }
            }
        }
        val path = this@MainActivity.cacheDir.path + "test.png"
        if (!File(path).exists()) {
            copyFromAsset(this, "test.png", path)
        }
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val colors = ColorQuant.getColorQuant(path)
                withContext(Dispatchers.Main) {
                    val colorsList = colors.split(",")
                    Log.e("ColorQuant", colorsList.toString())
                }
            }
        }
    }

    private fun copyFromAsset(ct: Context, fileName: String, targetPath: String) {
        try {
            ct.assets.open(fileName).use { `in` ->
                FileOutputStream(targetPath).use { out ->
                    val buffer = ByteArray(1024)
                    var read: Int
                    while (`in`.read(buffer).also { read = it } != -1) {
                        out.write(buffer, 0, read)
                    }
                }
            }
        } catch (e: IOException) {
            Log.e("tag", "Failed to copy asset file: ", e)
        }
    }

    private fun getPalette() {
        val image = BitmapFactory.decodeResource(resources,
            R.mipmap.test_image_color_theme)
        Palette.from(image).generate { palette ->
            if (palette != null) {
                val layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                val llLinearLayoutCompat = LinearLayoutCompat(this@MainActivity)
                llLinearLayoutCompat.layoutParams = layoutParams
                llLinearLayoutCompat.orientation = HORIZONTAL
                run {
                    val layoutParams2 = LinearLayout.LayoutParams(50, 50)
                    val testView = TextView(this@MainActivity)
                    testView.layoutParams = layoutParams2
                    palette.getVibrantColor(Color.BLUE).let { testView.setBackgroundColor(it) }
                    llLinearLayoutCompat.addView(testView)
                }
                run {
                    val layoutParams2 = LinearLayout.LayoutParams(50, 50)
                    val testView = TextView(this@MainActivity)
                    testView.layoutParams = layoutParams2
                    palette.getDarkVibrantColor(Color.BLUE).let { testView.setBackgroundColor(it) }
                    llLinearLayoutCompat.addView(testView)
                }
                run {
                    val layoutParams2 = LinearLayout.LayoutParams(50, 50)
                    val testView = TextView(this@MainActivity)
                    testView.layoutParams = layoutParams2
                    palette.getLightVibrantColor(Color.BLUE).let { testView.setBackgroundColor(it) }
                    llLinearLayoutCompat.addView(testView)
                }
                run {
                    val layoutParams2 = LinearLayout.LayoutParams(50, 50)
                    val testView = TextView(this@MainActivity)
                    testView.layoutParams = layoutParams2
                    palette.getLightMutedColor(Color.BLUE).let { testView.setBackgroundColor(it) }
                    llLinearLayoutCompat.addView(testView)
                }
                run {
                    val layoutParams2 = LinearLayout.LayoutParams(50, 50)
                    val testView = TextView(this@MainActivity)
                    testView.layoutParams = layoutParams2
                    palette.getDarkMutedColor(Color.BLUE).let { testView.setBackgroundColor(it) }
                    llLinearLayoutCompat.addView(testView)
                }
                run {
                    val layoutParams2 = LinearLayout.LayoutParams(50, 50)
                    val testView = TextView(this@MainActivity)
                    testView.layoutParams = layoutParams2
                    palette.getMutedColor(Color.BLUE).let { testView.setBackgroundColor(it) }
                    llLinearLayoutCompat.addView(testView)
                }
                findViewById<LinearLayoutCompat>(R.id.llImageColorTheme).addView(llLinearLayoutCompat)
                run {
                    // ?????????????????????
                    val darkMutedColor = palette.getDarkMutedColor(Color.BLUE)
                    // ?????????????????????
                    val darkVibrantColor = palette.getDarkVibrantColor(Color.BLUE)
                    // ?????????????????????
                    val lightVibrantColor = palette.getLightVibrantColor(Color.BLUE)
                    // ?????????????????????
                    val lightMutedColor = palette.getLightMutedColor(Color.BLUE)
                    // ????????????
                    val mutedColor = palette.getMutedColor(Color.BLUE)
                    val vibrantColor = palette.getVibrantColor(Color.BLUE)

                    t1?.setBackgroundColor(darkMutedColor)
                    t2?.setBackgroundColor(darkVibrantColor)
                    t3?.setBackgroundColor(lightVibrantColor)
                    t4?.setBackgroundColor(lightMutedColor)
                    t5?.setBackgroundColor(mutedColor)
                    t6?.setBackgroundColor(vibrantColor)
                }
            }
        }
    }

    private suspend fun getMainThemeColor(bitmap: Bitmap): MMCQ.ThemeColor? {
        val themeColors: List<MMCQ.ThemeColor>
        withContext(Dispatchers.Default) {
            val mmcq = MMCQ(bitmap, 3)
            themeColors = timeIt("MMCQ:") {
                mmcq.quantize()
            }
            if (themeColors != null) {
                withContext(Dispatchers.Main) {
                    val layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                    val imageView = ImageView(this@MainActivity)
                    imageView.layoutParams = layoutParams
                    imageView.setBackgroundResource(R.mipmap.test_image_color_theme)
                    findViewById<LinearLayoutCompat>(R.id.llImageColorTheme).addView(imageView)
                    val layoutParams2 = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                    val testView = TextView(this@MainActivity)
                    testView.layoutParams = layoutParams2
                    testView.text = MMCQ::class.java.canonicalName
                    findViewById<LinearLayoutCompat>(R.id.llImageColorTheme).addView(testView)
                }
                for (themeColor in themeColors) {
                    withContext(Dispatchers.Main) {
                        val layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                        val testView1 = TextView(this@MainActivity)
                        testView1.layoutParams = layoutParams
                        testView1.gravity = Gravity.CENTER
                        testView1.setBackgroundColor(themeColor.color)
                        val testView2 = TextView(this@MainActivity)
                        testView2.layoutParams = layoutParams
                        testView2.gravity = Gravity.CENTER
                        testView2.setBackgroundColor(themeColor.titleTextColor)
                        val testView3 = TextView(this@MainActivity)
                        testView3.layoutParams = layoutParams
                        testView3.gravity = Gravity.CENTER
                        testView3.setBackgroundColor(themeColor.titleTextColor)
                        findViewById<LinearLayoutCompat>(R.id.llImageColorTheme).addView(testView1)
                        findViewById<LinearLayoutCompat>(R.id.llImageColorTheme).addView(testView2)
                        findViewById<LinearLayoutCompat>(R.id.llImageColorTheme).addView(testView3)
                    }
                    Log.e("themeColor", themeColor.toString())
                }
            }
        }
        return if (themeColors.isEmpty()) null else themeColors[0]
    }

    private fun Activity.setThemeColor(themeColor: MMCQ.ThemeColor?) {
        if (themeColor == null) return
        val toolbar = findViewById(R.id.action_bar) as? Toolbar ?: return
        toolbar.setBackgroundColor(themeColor.color)
        toolbar.setTitleTextColor(themeColor.titleTextColor)
        toolbar.setSubtitleTextColor(themeColor.titleTextColor)
        window.statusBarColor = themeColor.color
    }

    private fun <T> timeIt(message: String = "", block: () -> T): T {
        val start = System.currentTimeMillis()
        val r = block()
        val end = System.currentTimeMillis()
        Log.e("timeIt", "$message: ${end - start} ms")
        return r
    }
}
