# QuickHandAnalyzer

This project is a sample Android Application to show the power of HMS MLKit Hand recognition feature
integrated with Android CameraX

## Important bits

In the [AndroidManifest.xml](https://github.com/joaobiriba/QuickHandAnalyzer/blob/master/app/src/main/AndroidManifest.xml) is enabled the dependency of the hand model for HMS ML Kit with

```xml
 <meta-data
        android:name="com.huawei.hms.ml.DEPENDENCY"
        android:value="handkeypoint" />
 ```

In the [HandDetectorProcessor.kt](https://github.com/joaobiriba/QuickHandAnalyzer/blob/master/app/src/main/java/com/huawei/quickhandanalyzer/processor/face/HandDetectorProcessor.kt) there is the creation of the Hand Analyzer responsible to detect the hands in an image

```kotlin
        detector = MLHandKeypointAnalyzerFactory.getInstance().getHandKeypointAnalyzer(options)
 ```

The option passed as argument can have multiple statements
```kotlin
val handDetectorOptions: MLHandKeypointAnalyzerSetting =
    MLHandKeypointAnalyzerSetting.Factory() // MLHandKeypointAnalyzerSetting.TYPE_ALL indicates that all results are returned.
        // MLHandKeypointAnalyzerSetting.TYPE_KEYPOINT_ONLY indicates that only hand keypoint information is returned.
        // MLHandKeypointAnalyzerSetting.TYPE_RECT_ONLY indicates that only palm information is returned.
        .setSceneType(MLHandKeypointAnalyzerSetting.TYPE_ALL) // Set the maximum number of hand regions that can be detected in an image. By default, a maximum of 10 hand regions can be detected.
        .setMaxHandResults(1)
        .create()
 ```
feel free to check the [documentation](https://developer.huawei.com/consumer/en/doc/development/HMSCore-References-V5/mlhandkeypointanalyzersetting-0000001051621265-V5)
for more options

Every time the detector detects one or more hands these will be returned in the onSuccess
```kotlin
  override fun onSuccess(hands: List<MLHandKeypoints>, graphicOverlay: GraphicOverlay) {
        for (hand in hands) {
            graphicOverlay.add(HandKeypointGraphic(graphicOverlay, hand))
            logExtrasForTesting(hand)
        }
    }
```
From a MLHandKeypoints object You will be able to get 

[Hands Key Points](https://developer.huawei.com/consumer/en/doc/development/HMSCore-References-V5/mlhandkeypoint-0000001052421228-V5)

Also check [HandKeypointGraphic.kt](https://github.com/joaobiriba/QuickHandAnalyzer/blob/master/app/src/main/java/com/huawei/quickhandanalyzer/graphic/face/HandKeypointGraphic.kt) for a sample to check how to display the info you want on the canvas

In the [OnSwipeTouchListener.kt](https://github.com/joaobiriba/QuickFaceAnalyzer/blob/master/app/src/main/java/com/huawei/quickhandanalyzer/utils/OnSwipeTouchListener.kt) there is the code to handle the Top to Bottom swype down gesture to change the camera
## Usage

Open the project with Android Studio 4.0 and run it or alternatively use gradle directly

Enable the permissions asked by the system

Point the camera to an hand 

Swype down from top to bottom on the screen to change camera

## Use it!

You are encouraged to try it out and study and hack every parts to achieve your own target
