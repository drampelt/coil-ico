package com.danielrampelt.coil.ico.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import coil.load
import com.danielrampelt.coil.ico.IcoDecoder

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val icoDecoder = IcoDecoder(this)

        findViewById<ImageView>(R.id.iconHnSystem).load("https://news.ycombinator.com/favicon.ico")
        findViewById<ImageView>(R.id.iconHn).load("https://news.ycombinator.com/favicon.ico") {
            decoder(icoDecoder)
        }

        findViewById<ImageView>(R.id.iconGoogleSystem).load("https://www.google.com/favicon.ico")
        findViewById<ImageView>(R.id.iconGoogle).load("https://www.google.com/favicon.ico") {
            decoder(icoDecoder)
        }
    }
}
