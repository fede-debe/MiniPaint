package com.example.minipaint

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_main)

        /** delete setContentView(R.layout.activity_main) and create an instance of MyCanvasView.
         * Below that, request the full screen for the layout of myCanvasView. Do this by setting the
         * SYSTEM_UI_FLAG_FULLSCREEN flag on myCanvasView. In this way, the view completely fills the
         * screen.
         * Add a content description.
         * Below that, set the content view to myCanvasView.
         *
         * You will see a completely white screen, because the canvas has no size, and you have not drawn anything yet.
         * Note: You will need to know the size of the view for drawing, but you cannot get the size of the view in the
         * onCreate() method, because the size has not been determined at this point.
         * */
        val myCanvasView = MyCanvasView(this)
        myCanvasView.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN
        myCanvasView.contentDescription = getString(R.string.canvasContentDescription)
        setContentView(myCanvasView)

    }
}