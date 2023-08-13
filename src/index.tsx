//Com o modo estrito, você não pode, por exemplo, usar variáveis ​​não declaradas.
'use strict';

import {
  NativeModules,
  NativeAppEventEmitter,
  PermissionsAndroid,
  Platform
} from "react-native";

const LINKING_ERROR =
  `The package 'react-native-audio' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const Audio = NativeModules.Audio
  ? NativeModules.Audio
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

function multiply(a: number, b: number): Promise<number> {
  return Audio.multiply(a, b);
}
var AudioRecorder = {
  prepareRecordingAtPath: function(path, options) {
    if (this.progressSubscription) this.progressSubscription.remove();
    this.progressSubscription = NativeAppEventEmitter.addListener('recordingProgress',
      (data) => {
        if (this.onProgress) {
          this.onProgress(data);
        }
      }
    );

    if (this.finishedSubscription) this.finishedSubscription.remove();
    this.finishedSubscription = NativeAppEventEmitter.addListener('recordingFinished',
      (data) => {
        if (this.onFinished) {
          this.onFinished(data);
        }
      }
    );

    var defaultOptions = {
      SampleRate: 44100.0,
      Channels: 2,
      AudioQuality: 'High',
      AudioEncoding: 'ima4',
      OutputFormat: 'mpeg_4',
      MeteringEnabled: false,
      MeasurementMode: false,
      AudioEncodingBitRate: 32000,
      IncludeBase64: false,
      AudioSource: 0
    };

    var recordingOptions = {...defaultOptions, ...options};

    if (Platform.OS === 'ios') {
      Audio.prepareRecordingAtPath(
        path,
        recordingOptions.SampleRate,
        recordingOptions.Channels,
        recordingOptions.AudioQuality,
        recordingOptions.AudioEncoding,
        recordingOptions.MeteringEnabled,
        recordingOptions.MeasurementMode,
        recordingOptions.IncludeBase64
      );
    } else {
      return Audio.prepareRecordingAtPath(path, recordingOptions);
    }
  },
  startRecording: function() {
    return Audio.startRecording();
  },
  pauseRecording: function() {
    return Audio.pauseRecording();
  },
  resumeRecording: function() {
    return Audio.resumeRecording();
  },
  stopRecording: function() {
    return Audio.stopRecording();
  },
  checkAuthorizationStatus: Audio.checkAuthorizationStatus,
  requestAuthorization: () => {
    if (Platform.OS === 'ios')
      return Audio.requestAuthorization();
    else
      return new Promise((resolve, reject) => {
        PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.RECORD_AUDIO
        ).then(result => {
          if (result == PermissionsAndroid.RESULTS.GRANTED || result == true)
            resolve(true);
          else
            resolve(false)
        })
      });
  },
  removeListeners: function() {
    if (this.progressSubscription) this.progressSubscription.remove();
    if (this.finishedSubscription) this.finishedSubscription.remove();
  },
};

let AudioUtils = {};
let AudioSource = {};

if (Platform.OS === 'ios') {
  AudioUtils = {
    MainBundlePath: Audio.MainBundlePath,
    CachesDirectoryPath: Audio.NSCachesDirectoryPath,
    DocumentDirectoryPath: Audio.NSDocumentDirectoryPath,
    LibraryDirectoryPath: Audio.NSLibraryDirectoryPath,
  };
} else if (Platform.OS === 'android') {
  AudioUtils = {
    MainBundlePath: Audio.MainBundlePath,
    CachesDirectoryPath: Audio.CachesDirectoryPath,
    DocumentDirectoryPath: Audio.DocumentDirectoryPath,
    LibraryDirectoryPath: Audio.LibraryDirectoryPath,
    PicturesDirectoryPath: Audio.PicturesDirectoryPath,
    MusicDirectoryPath: Audio.MusicDirectoryPath,
    DownloadsDirectoryPath: Audio.DownloadsDirectoryPath
  };
  AudioSource = {
    DEFAULT: 0,
    MIC: 1,
    VOICE_UPLINK: 2,
    VOICE_DOWNLINK: 3,
    VOICE_CALL: 4,
    CAMCORDER: 5,
    VOICE_RECOGNITION: 6,
    VOICE_COMMUNICATION: 7,
    REMOTE_SUBMIX: 8, // added in API 19
    UNPROCESSED: 9, // added in API 24
  };
}

export  { multiply, AudioRecorder, AudioUtils, AudioSource };
