package com.e.VoiceAssistant.ui.uiHelpers.touchListener

import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import com.e.VoiceAssistant.ui.uiHelpers.TouchHelper

class MultiTouchListener(val action:(TouchHelper)->Unit) : OnTouchListener {
    private val INVALID_POINTER_ID = -1
    protected var isRotateEnabled = false
    protected var isTranslateEnabled = true
    protected var isScaleEnabled = false
    private var minimumScale = 0.3f
    private var maximumScale = 10.0f
    private var mActivePointerId = INVALID_POINTER_ID
    private var mPrevX = 0f
    private var mPrevY = 0f
    private val mScaleGestureDetector: ScaleGestureDetector
    private var fingerOnView = true
    private fun adjustAngle(degrees: Float): Float {
        var degrees = degrees
        if (degrees > 180.0f) {
            degrees -= 360.0f
        } else if (degrees < -180.0f) {
            degrees += 360.0f
        }
        return degrees
    }

    private fun move(view: View, info: TransformInfo) {
        // computeRenderOffset(view, info.pivotX, info.pivotY);
        // adjustTranslation(view, info.deltaX, info.deltaY);

        // Assume that scaling still maintains aspect ratio.
        var scale = view.scaleX * info.deltaScale
        scale = Math.max(info.minimumScale, Math.min(info.maximumScale, scale))
        view.scaleX = scale
        view.scaleY = scale
        val rotation = adjustAngle(view.rotation + info.deltaAngle)
        view.rotation = rotation
    }

    private fun adjustTranslation(
        view: View,
        deltaX: Float,
        deltaY: Float
    ) {
        val deltaVector = floatArrayOf(deltaX, deltaY)
        view.matrix.mapVectors(deltaVector)
//        view.x = view.x + deltaVector[0]
//        view.y = view.y + deltaVector[1]
        val x=(view.translationX +deltaVector[0]).toInt()
        val y=(view.translationY + deltaVector[1]).toInt()
        action(TouchHelper.moveEvent(x,y))
    }


    override fun onTouch(view: View, event: MotionEvent): Boolean {
        mScaleGestureDetector.onTouchEvent(view, event)
        if (!isTranslateEnabled) {
            return true
        }
        val action = event.action
        when (action and event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mPrevX = event.x
                mPrevY = event.y
              //  trash.visibility = View.VISIBLE//show trash icon
                // Save the ID of this pointer.
                mActivePointerId = event.getPointerId(0)
                action(TouchHelper.downEvent)
            }
            MotionEvent.ACTION_MOVE -> {

                // Find the index of the active pointer and fetch its position.
                val pointerIndex = event.findPointerIndex(mActivePointerId)
                if (pointerIndex != -1) {
                    val currX = event.rawX
                    val currY = event.rawY

                    // Only move if the ScaleGestureDetector isn't processing a
                    // gesture.
                    if (!mScaleGestureDetector.isInProgress && event.pointerCount == 1 && calculateClickTime(event)) {
                        adjustTranslation(view, currX - mPrevX, currY - mPrevY)

                    }
                }
            }
            MotionEvent.ACTION_CANCEL -> mActivePointerId = INVALID_POINTER_ID
            MotionEvent.ACTION_UP -> onUp(event)
            MotionEvent.ACTION_POINTER_UP -> {

                // Extract the index of the pointer that left the touch sensor.
                val pointerIndex =
                    action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
                val pointerId = event.getPointerId(pointerIndex)
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    mPrevX = event.getX(newPointerIndex)
                    mPrevY = event.getY(newPointerIndex)
                    mActivePointerId = event.getPointerId(newPointerIndex)
                }
            }
        }
        return true
    }

    private inner class ScaleGestureListener :
        ScaleGestureDetector.SimpleOnScaleGestureListener() {
        private var mPivotX = 0f
        private var mPivotY = 0f
        private val mPrevSpanVector = Vector2D()
        override fun onScaleBegin(
            view: View,
            detector: ScaleGestureDetector
        ): Boolean {
            mPivotX = detector.focusX
            mPivotY = detector.focusY
            mPrevSpanVector.set(detector.currentSpanVector)
            return true
        }

        override fun onScaleEnd(
            view: View,
            detector: ScaleGestureDetector) {}

        override fun onScale(
            view: View,
            detector: ScaleGestureDetector
        ): Boolean {
            val info = TransformInfo()
            info.deltaScale = if (isScaleEnabled) detector.scaleFactor else 1.0f
            info.deltaAngle = if (isRotateEnabled) Vector2D.getAngle(
                mPrevSpanVector,
                detector.currentSpanVector
            ) else 0.0f
            info.deltaX = if (isTranslateEnabled) detector.focusX - mPivotX else 0.0f
            info.deltaY = if (isTranslateEnabled) detector.focusY - mPivotY else 0.0f
            info.pivotX = mPivotX
            info.pivotY = mPivotY
            info.minimumScale = minimumScale
            info.maximumScale = maximumScale
            move(view, info)
            return false
        }
    }


    private fun onUp(event: MotionEvent) {
        mActivePointerId = INVALID_POINTER_ID
//        if (fingerOnView)
//            action(TouchHelper.StopService)
        //if not deleted check for click event
        if (!calculateClickTime(event))
            action(TouchHelper.TalkOrStopClickEvent)
        else
            action(TouchHelper.upEvent)
         //   trash.visibility = View.GONE
    }

    private inner class TransformInfo {
        var deltaX = 0f
        var deltaY = 0f
        var deltaScale = 0f
        var deltaAngle = 0f
        var pivotX = 0f
        var pivotY = 0f
        var minimumScale = 0f
        var maximumScale = 0f
    }

    /**checks if a click is detected */
    private fun calculateClickTime(event: MotionEvent): Boolean {
        return event.eventTime - event.downTime >= 150
    }

    init { mScaleGestureDetector = ScaleGestureDetector(ScaleGestureListener())
           }
}