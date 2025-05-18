package com.finals.kotlin_androidswiftshare

import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private var isReversed = false
    private lateinit var transitionDrawable: TransitionDrawable

    /**
     * ON CREATE-- Most things initialize here.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        // Start animated gradient background
        val layout = findViewById<ConstraintLayout>(R.id.main)
        transitionDrawable = layout.background as TransitionDrawable
        animateBackgroundLoop()

        val fileSenderButton = findViewById<Button>(R.id.SelectFileBTN)

        fileSenderButton.setOnClickListener {
            val intent = Intent(this, FileSender::class.java)
            intent.putExtra("AUTO_LOAD_FILES", true)
            startActivity(intent)
        }


        // Metallic gradient text effect
        applyMetallicShader()
    }



    private fun animateBackgroundLoop() {
        val handler = Handler(Looper.getMainLooper())

        val loopRunnable = object : Runnable {
            override fun run() {
                if (isReversed) {
                    transitionDrawable.reverseTransition(3000)
                } else {
                    transitionDrawable.startTransition(3000)
                }
                isReversed = !isReversed
                handler.postDelayed(this, 6000) // 3s for transition, 3s pause
            }
        }

        handler.post(loopRunnable)
    }



    private fun applyMetallicShader() {
        val textView = findViewById<TextView>(R.id.appheaderTV)
        textView.post {
            val paint = textView.paint
            val width = paint.measureText(textView.text.toString())

            val shader = LinearGradient(
                0f, 0f, width, textView.textSize,
                intArrayOf(
                    Color.parseColor("#1E3A5A"),
                    Color.parseColor("#285480"),
                    Color.parseColor("#A7BED3"),
                    Color.parseColor("#285480"),
                    Color.parseColor("#1E3A5A")
                ),
                floatArrayOf(0f, 0.25f, 0.5f, 0.75f, 1f),
                Shader.TileMode.CLAMP
            )
            textView.paint.shader = shader
            textView.invalidate()
        }
    }
    }