package com.huawei.quickhandanalyzer.processor.hand

import android.content.Context
import android.util.Log
import com.huawei.hmf.tasks.Task
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.handkeypoint.*
import com.huawei.quickhandanalyzer.graphic.GraphicOverlay
import com.huawei.quickhandanalyzer.graphic.hand.HandKeypointGraphic
import com.huawei.quickhandanalyzer.processor.VisionProcessorBase
import java.util.*

/** Hand Detector Demo.  */
class HandDetectorProcessor(context: Context, detectorOptions: MLHandKeypointAnalyzerSetting?) :
    VisionProcessorBase<List<MLHandKeypoints>>(context) {

    private val detector: MLHandKeypointAnalyzer

    init {

        val options = detectorOptions
            ?: MLHandKeypointAnalyzerSetting.Factory()
                // MLHandKeypointAnalyzerSetting.TYPE_ALL indicates that all results are returned.
                // MLHandKeypointAnalyzerSetting.TYPE_KEYPOINT_ONLY indicates that only hand keypoint information is returned.
                // MLHandKeypointAnalyzerSetting.TYPE_RECT_ONLY indicates that only palm information is returned.
                .setSceneType(MLHandKeypointAnalyzerSetting.TYPE_ALL)
                // Set the maximum number of hand regions that can be detected in an image. By default, a maximum of 10 hand regions can be detected.
                .setMaxHandResults(1)
                .create()

        detector = MLHandKeypointAnalyzerFactory.getInstance().getHandKeypointAnalyzer(options)

        Log.v(MANUAL_TESTING_LOG, "Hand detector options: $options")
    }

    override fun stop() {
        super.stop()
        detector.stop()
    }

    override fun detectInImage(image: MLFrame): Task<List<MLHandKeypoints>> {
        return detector.asyncAnalyseFrame(image)
    }

    override fun onSuccess(hands: List<MLHandKeypoints>, graphicOverlay: GraphicOverlay) {
        for (hand in hands) {
            graphicOverlay.add(HandKeypointGraphic(graphicOverlay, hand))
            logExtrasForTesting(hand)
        }
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Face detection failed $e")
    }

    companion object {
        private const val TAG = "FaceDetectorProcessor"
        private fun logExtrasForTesting(hand: MLHandKeypoints?) {
            if (hand != null) {
                Log.v(
                    MANUAL_TESTING_LOG,
                    "face bounding box: " + hand.rect.flattenToString()
                )

                // All landmarks
                val landMarkTypes = intArrayOf(
                    MLHandKeypoint.TYPE_FOREFINGER_FIRST,
                    MLHandKeypoint.TYPE_FOREFINGER_SECOND,
                    MLHandKeypoint.TYPE_FOREFINGER_THIRD,
                    MLHandKeypoint.TYPE_FOREFINGER_FOURTH,
                    MLHandKeypoint.TYPE_LITTLE_FINGER_FIRST,
                    MLHandKeypoint.TYPE_LITTLE_FINGER_SECOND,
                    MLHandKeypoint.TYPE_LITTLE_FINGER_THIRD,
                    MLHandKeypoint.TYPE_LITTLE_FINGER_FOURTH,
                    MLHandKeypoint.TYPE_MIDDLE_FINGER_FIRST,
                    MLHandKeypoint.TYPE_MIDDLE_FINGER_SECOND,
                    MLHandKeypoint.TYPE_MIDDLE_FINGER_THIRD,
                    MLHandKeypoint.TYPE_MIDDLE_FINGER_FOURTH,
                    MLHandKeypoint.TYPE_RING_FINGER_FIRST,
                    MLHandKeypoint.TYPE_RING_FINGER_SECOND,
                    MLHandKeypoint.TYPE_RING_FINGER_THIRD,
                    MLHandKeypoint.TYPE_RING_FINGER_FOURTH,
                    MLHandKeypoint.TYPE_THUMB_FIRST,
                    MLHandKeypoint.TYPE_THUMB_SECOND,
                    MLHandKeypoint.TYPE_THUMB_THIRD,
                    MLHandKeypoint.TYPE_THUMB_FOURTH,
                    MLHandKeypoint.TYPE_WRIST
                )
                val landMarkTypesStrings = arrayOf(
                    "FOREFINGER_FIRST",
                    "FOREFINGER_SECOND",
                    "FOREFINGER_THIRD",
                    "FOREFINGER_FOURTH",
                    "LITTLE_FINGER_FIRST",
                    "LITTLE_FINGER_SECOND",
                    "LITTLE_FINGER_THIRD",
                    "LITTLE_FINGER_FOURTH",
                    "MIDDLE_FINGER_FIRST",
                    "MIDDLE_FINGER_SECOND",
                    "MIDDLE_FINGER_THIRD",
                    "MIDDLE_FINGER_FOURTH",
                    "RING_FINGER_FIRST",
                    "RING_FINGER_SECOND",
                    "RING_FINGER_THIRD",
                    "RING_FINGER_FOURTH",
                    "THUMB_FIRS",
                    "THUMB_SECON",
                    "THUMB_THIRD",
                    "THUMB_FOURTH",
                    "WRIST",
                )
                for (i in landMarkTypes.indices) {
                    val landmark = hand.getHandKeypoint(landMarkTypes[i])
                    if (landmark == null) {
                        Log.v(
                            MANUAL_TESTING_LOG,
                            "No landmark of type: " + landMarkTypesStrings[i] + " has been detected"
                        )
                    } else {
                        val landmarkPositionStr =
                            String.format(
                                Locale.US,
                                "x: %f , y: %f",
                                landmark.pointX,
                                landmark.pointY
                            )
                        Log.v(
                            MANUAL_TESTING_LOG,
                            "Position for face landmark: " +
                                    landMarkTypesStrings[i] +
                                    " is :" +
                                    landmarkPositionStr
                        )
                    }
                }
            }
        }
    }
}
