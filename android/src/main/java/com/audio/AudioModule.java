package com.audio;

import androidx.annotation.NonNull;
import android.content.Context;
import android.media.MediaRecorder;

import java.lang.reflect.Method;

import java.util.Timer;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;


@ReactModule(name = AudioModule.NAME)
public class AudioModule extends ReactContextBaseJavaModule {
  public static final String NAME = "Audio";

  private static final String DocumentDirectoryPath = "DocumentDirectoryPath";
  private static final String PicturesDirectoryPath = "PicturesDirectoryPath";
  private static final String MainBundlePath = "MainBundlePath";
  private static final String CachesDirectoryPath = "CachesDirectoryPath";
  private static final String LibraryDirectoryPath = "LibraryDirectoryPath";
  private static final String MusicDirectoryPath = "MusicDirectoryPath";
  private static final String DownloadsDirectoryPath = "DownloadsDirectoryPath";

  private Context context;
  private MediaRecorder recorder;
  private String currentOutputFile;
  private boolean isRecording = false;
  private boolean isPaused = false;
  private boolean includeBase64 = false;
  private Timer timer;
  private StopWatch stopWatch;

  private boolean isPauseResumeCapable = false;
  private Method pauseMethod = null;
  private Method resumeMethod = null;

  public AudioModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }


  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  public void multiply(double a, double b, Promise promise) {
    promise.resolve(a * b);
  }
}
