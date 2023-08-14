package com.audio;

import android.content.Context;
import android.content.pm.PackageManager;

import android.media.MediaRecorder;
import android.media.MediaRecorder;
import android.media.AudioManager;

import android.Manifest;

import android.os.Build;
import android.os.Environment;

import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.lang.reflect.Method;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.IllegalAccessException;
import java.lang.NoSuchMethodException;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;


@ReactModule(name = AudioModule.NAME)
class AudioModule extends ReactContextBaseJavaModule {

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
  private Timer timer;
  private int recorderSecondsElapsed;


  public AudioModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.context = reactContext;
  }

  @Override
  public Map<String, Object> getConstants() {
    Map<String, Object> constants = new HashMap<>();
    constants.put(DocumentDirectoryPath, this.getReactApplicationContext().getFilesDir().getAbsolutePath());
    constants.put(PicturesDirectoryPath, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath());
    constants.put(MainBundlePath, "");
    constants.put(CachesDirectoryPath, this.getReactApplicationContext().getCacheDir().getAbsolutePath());
    constants.put(LibraryDirectoryPath, "");
    constants.put(MusicDirectoryPath, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath());
    constants.put(DownloadsDirectoryPath, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
    return constants;
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  @ReactMethod
  public void checkAuthorizationStatus(Promise promise) {
    try {
          int permissionCheck = ContextCompat.checkSelfPermission(getCurrentActivity(),
            Manifest.permission.RECORD_AUDIO);
          boolean permissionGranted = permissionCheck == PackageManager.PERMISSION_GRANTED;
          promise.resolve(permissionGranted);
    } catch (final Exception e) {

    }

  }

  @ReactMethod
  public void prepareRecordingAtPath(String recordingPath, ReadableMap recordingSettings, Promise promise) {
    try {
          if (isRecording){
            logAndRejectPromise(promise, "INVALID_STATE", "Please call stopRecording before starting recording");
          }

          recorder = new MediaRecorder();
          try {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            int outputFormat = getOutputFormatFromString(recordingSettings.getString("OutputFormat"));
            recorder.setOutputFormat(outputFormat);
            int audioEncoder = getAudioEncoderFromString(recordingSettings.getString("AudioEncoding"));
            recorder.setAudioEncoder(audioEncoder);
            recorder.setAudioSamplingRate(recordingSettings.getInt("SampleRate"));
            recorder.setAudioChannels(recordingSettings.getInt("Channels"));
            recorder.setAudioEncodingBitRate(recordingSettings.getInt("AudioEncodingBitRate"));
            recorder.setOutputFile(recordingPath);
          }
          catch(final Exception e) {
            logAndRejectPromise(promise, "COULDNT_CONFIGURE_MEDIA_RECORDER" , "Make sure you've added RECORD_AUDIO permission to your AndroidManifest.xml file "+e.getMessage());
            return;
          }

          currentOutputFile = recordingPath;
          try {
            recorder.prepare();
            promise.resolve(currentOutputFile);
          } catch (final Exception e) {
            logAndRejectPromise(promise, "COULDNT_PREPARE_RECORDING_AT_PATH "+recordingPath, e.getMessage());
          }
    } catch (final Exception e) {

    }

  }

  private int getAudioEncoderFromString(String audioEncoder) {
   switch (audioEncoder) {
     case "aac":
       return MediaRecorder.AudioEncoder.AAC;
     case "aac_eld":
       return MediaRecorder.AudioEncoder.AAC_ELD;
     case "amr_nb":
       return MediaRecorder.AudioEncoder.AMR_NB;
     case "amr_wb":
       return MediaRecorder.AudioEncoder.AMR_WB;
     case "he_aac":
       return MediaRecorder.AudioEncoder.HE_AAC;
     case "vorbis":
      return MediaRecorder.AudioEncoder.VORBIS;
     default:
       Log.d("INVALID_AUDIO_ENCODER", "USING MediaRecorder.AudioEncoder.DEFAULT instead of "+audioEncoder+": "+MediaRecorder.AudioEncoder.DEFAULT);
       return MediaRecorder.AudioEncoder.DEFAULT;
   }
  }

  private int getOutputFormatFromString(String outputFormat) {
    switch (outputFormat) {
      case "mpeg_4":
        return MediaRecorder.OutputFormat.MPEG_4;
      case "aac_adts":
        return MediaRecorder.OutputFormat.AAC_ADTS;
      case "amr_nb":
        return MediaRecorder.OutputFormat.AMR_NB;
      case "amr_wb":
        return MediaRecorder.OutputFormat.AMR_WB;
      case "three_gpp":
        return MediaRecorder.OutputFormat.THREE_GPP;
      case "webm":
        return MediaRecorder.OutputFormat.WEBM;
      default:
        Log.d("INVALID_OUPUT_FORMAT", "USING MediaRecorder.OutputFormat.DEFAULT : "+MediaRecorder.OutputFormat.DEFAULT);
        return MediaRecorder.OutputFormat.DEFAULT;

    }
  }

  @ReactMethod
  public void startRecording(Promise promise){
    try {
      if (recorder == null){
        logAndRejectPromise(promise, "RECORDING_NOT_PREPARED", "Please call prepareRecordingAtPath before starting recording");
        return;
      }
      if (isRecording){
        logAndRejectPromise(promise, "INVALID_STATE", "Please call stopRecording before starting recording");
        return;
      }
      recorder.start();
      isRecording = true;
      startTimer();
      promise.resolve(currentOutputFile);
    } catch (Exception e) {

    }
  }

  @ReactMethod
  public void stopRecording(Promise promise){

    try {
          if (!isRecording){
            logAndRejectPromise(promise, "INVALID_STATE", "Please call startRecording before stopping recording");
            return;
          }

          stopTimer();
          isRecording = false;

          try {
            recorder.stop();
            recorder.release();
          }
          catch (final RuntimeException e) {
            // https://developer.android.com/reference/android/media/MediaRecorder.html#stop()
            logAndRejectPromise(promise, "RUNTIME_EXCEPTION", "No valid audio data received. You may be using a device that can't record audio.");
            return;
          }
          finally {
            recorder = null;
          }

          promise.resolve(currentOutputFile);
          sendEvent("recordingFinished", null);
    } catch (Exception e) {

    }


  }

  @ReactMethod
  public void pauseRecording(Promise promise){
        try {
            // Added this function to have the same api for android and iOS, stops recording now
            stopRecording(promise);
        } catch (Exception e) {

        }
  }

  private void startTimer(){
     try {
            stopTimer();
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
              @Override
              public void run() {
                AudioModule.this.getReactApplicationContext().runOnNativeModulesQueueThread(new Runnable() {
                  @Override
                  public void run() {
                    recorderSecondsElapsed++;
                    WritableMap body = Arguments.createMap();
                    body.putInt("currentTime", recorderSecondsElapsed/4);
                    int maxAmplitude = 0;
                    if (recorder != null) {
                      maxAmplitude = recorder.getMaxAmplitude();
                    }
                    body.putInt("currentMetering", maxAmplitude);
                    sendEvent("recordingProgress", body);
                  }
                });
              }
            }, 0, 250);
        } catch (Exception e) {

        }

  }

  private void stopTimer() {
    try {
        recorderSecondsElapsed = 0;
        if (timer != null) {
          timer.cancel();
          timer.purge();
          timer = null;
        }
    } catch (Exception e) {

    }
  }

  private void sendEvent(String eventName, Object params) {
    try {
        getReactApplicationContext()
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, params);
    } catch (Exception e) {

    }

  }

  private void logAndRejectPromise(Promise promise, String errorCode, String errorMessage) {
     try {
        Log.e(NAME, errorMessage);
        promise.reject(errorCode, errorMessage);
    } catch (Exception e) {

    }

  }
}
