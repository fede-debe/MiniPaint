package com.example.minipaint

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import kotlin.math.abs

/**define a constant for the stroke width. */
private const val STROKE_WIDTH = 12f // has to be float

/** Create a custom view. Make the MyCanvasView class extend the View class and pass in the context: Context.
 * To display what we will draw in MyCanvasView, we have to set it as the ContentView of the MainActivity */
class MyCanvasView(context: Context) : View(context) {

    /** Define member variables for a canvas and a bitmap. Call them extraCanvas and extraBitmap.
     * These are your bitmap and canvas for caching what has been drawn before.
     * */
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap

    /** Define a class level variable backgroundColor, for the background color of the canvas
     * and initialize it to the colorBackground you defined earlier
     * */
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorBackground, null)

    /** define a variable drawColor for holding the color to draw with and initialize it with
     * the colorPaint resource you defined earlier.
     * */
    private val drawColor = ResourcesCompat.getColor(resources,  R.color.colorPaint, null)

    // set up the paint with which  to draw.
    private val paint = Paint().apply {
        color = drawColor
        // smooths out edges of what is drawn without affecting shape
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled
        isDither = true
        style = Paint.Style.STROKE // default: FILL the object to which the paints is applied.
        strokeJoin = Paint.Join.ROUND // default: MITER -> specifies how lines and curve segments join a stroked path.
        strokeCap = Paint.Cap.ROUND // default: BUTT -> sets the shape of the end of the line to be a cap. Cap specifies how to the begging and ending of stroked lines and path look.
        strokeWidth  = STROKE_WIDTH // default: Hairline-width (really thin) -> specifies the width of the stroke in pixels.
    }

    /** In MyCanvasView, add a variable path and initialize it with a Path object to store the
     * path that is being drawn when following the user's touch on the screen. Import
     * android.graphics.Path for the Path.
     * */
    private var path = Path()

    /** add the motionTouchEventX and motionTouchEventY variables for caching the x and y
     * coordinates of the current touch event (the MotionEvent coordinates). Initialize
     * them to 0f.
     * */
    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f

    /**  add variables to cache the latest x and y values. After the user stops moving
     * and lifts their touch, these are the starting point for the next path (the next
     * segment of the line to draw).
     * */
    private var currentX = 0f
    private var currentY =0f

    /** Using a path, there is no need to draw every pixel, and each time you request a refresh after display.
     * Instead, you  can and will interpolate a path between points for much better performance. If the finger
     * has barely moved, there is no need to draw.If the finger has moved less than a touchTolerance distance,
     * don't draw. "scaledTouchSlop" returns the distance in pixels, a touch can wander before the system
     * thinks the user is scrolling. */
    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    /** In this exercise you are going to draw a frame around the sketch. Add a variable called frame that
     * holds a Rectangle object.
     * */
    private lateinit var frame: Rect

    /** This callback method is called by the Android system with the changed screen dimensions,
     * that is, with a new width and height (to change to) and the old width and height (to
     * change from)
     *
     * Inside onSizeChanged(), create an instance of Bitmap with the new width and height,
     * which are the screen size, and assign it to extraBitmap. The third argument is the
     * bitmap color configuration. ARGB_8888 stores each color in 4 bytes and is
     * recommended.
     *
     * Create a Canvas instance from extraBitmap and assign it to extraCanvas.
     *
     * Specify the background color in which to fill extraCanvas.
     *
     * Looking at onSizeChanged(), a new bitmap and canvas are created every time the function executes.
     * You need a new bitmap, because the size has changed. However, this is a memory leak, leaving
     * the old bitmaps around. To fix this, recycle extraBitmap before creating the next one.
     * */
    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        if (::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(backgroundColor)

        // calculate a rectangular frame around the picture. add code to create the Rect that will be used for the frame, using the new dimensions and the inset.
        val inset = 40
        frame = Rect(inset, inset, width - inset, height -  inset)
    }

    /** Override onDraw() and draw the contents of the cached extraBitmap on the canvas associated with
     * the view. The drawBitmap() Canvas method comes in several versions. In this code, you provide
     * the bitmap, the x and y coordinates (in pixels) of the top left corner, and null for the Paint,
     * as you'll set that later.
     *
     * Note: The 2D coordinate system used for drawing on a Canvas is in pixels, and the origin (0,0)
     * is at the top left corner of the Canvas
     * */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // drawing the bitmap
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
        // draw a frame around the canvas
        canvas.drawRect(frame, paint)
    }


    /** call when the user touches the screen. Reset the path, move to the x-y coordinates of the touch
     * event (motionTouchEventX and motionTouchEventY) and assign currentX and currentY to that value.
     * */
    private fun touchStart() {
        path.reset()
        path.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY =motionTouchEventY
    }

    /** Calculate the traveled distance (dx, dy), create a curve between the two points and
     * store it in path, update the running currentX and currentY tally, and draw the path.
     * Then call invalidate() to force redrawing of the screen with the updated path.
     *
     * */
    private fun touchMove() {
        val dx = abs(motionTouchEventX - currentX) // Calculate the distance that has been moved (dx, dy)
        val dy = abs(motionTouchEventY - currentY)
        if (dx >= touchTolerance || dy >= touchTolerance) { // If the movement was further than the touch tolerance, add a segment to the path (next  line).
            path.quadTo(currentX, currentY, (motionTouchEventX + currentX) / 2, (motionTouchEventY + currentY) / 2) // Using quadTo() instead of lineTo() create a smoothly drawn line without corners.
            currentX = motionTouchEventX // Set the starting point for the next segment to the endpoint of this segment.
            currentY = motionTouchEventY
            extraCanvas.drawPath(path, paint) // draw the path
        }
        invalidate() // Call invalidate() to (eventually call onDraw() and) redraw the view.
    }

    /** when a user lifts their touch, all that is needed to is to reset the path, so it
     * doesn't get drawn again. Nothing is drawn here, so no invalidation is needed.
     *
     * Run your code and use your finger to draw on the screen. Notice that if you rotate
     * the device, the screen is cleared, because the drawing state is not saved. For this
     * sample app, this is by design, to give the user a simple way to clear the screen
     * */
    private fun touchUp() {
        // reset the path so it doesn't get drawn again
        path.reset()
    }

    /** Override the onTouchEvent() method to cache the x and y coordinates of the passed
     *  in event. Then use a when expression to handle motion events for touching down on
     *  the screen, moving on the screen, and releasing touch on the screen. These are the
     *  events of interest for drawing a line on the screen. For each event type, call a
     *  utility method, as shown in the code below.
     *  */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart()
            MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }

}