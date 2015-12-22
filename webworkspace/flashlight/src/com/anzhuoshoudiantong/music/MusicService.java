package com.anzhuoshoudiantong.music;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import com.anzhuoshoudiantong.MainActivity;
import com.anzhuoshoudiantong.R;

public class MusicService extends Service implements OnCompletionListener,
        OnPreparedListener, OnErrorListener, MusicFocusable,
        PrepareMusicRetrieverTask.MusicRetrieverPreparedListener {

    final static String TAG = "RandomMusicPlayer";

    public static final String ACTION_TOGGLE_PLAYBACK = "com.anzhuoshoudiantong.TOGGLE_PLAYBACK";
    public static final String ACTION_PLAY = "com.anzhuoshoudiantong.PLAY";
    public static final String ACTION_PAUSE = "com.anzhuoshoudiantong.PAUSE";
    public static final String ACTION_STOP = "com.anzhuoshoudiantong.STOP";
    public static final String ACTION_SKIP = "com.anzhuoshoudiantong.SKIP";
    public static final String ACTION_REWIND = "com.anzhuoshoudiantong.REWIND";
    public static final String ACTION_URL = "com.anzhuoshoudiantong.URL";

    public static final float DUCK_VOLUME = 0.1f;

    MediaPlayer mPlayer = null;
    Visualizer mVisualizer = null;
    AudioFocusHelper mAudioFocusHelper = null;

    enum State {
        Retrieving, Stopped, Preparing, Playing, Paused
    };

    State mState = State.Retrieving;
    boolean mStartPlayingAfterRetrieve = false;
    Uri mWhatToPlayAfterRetrieve = null;

    enum PauseReason {
        UserRequest, FocusLoss,
    };

    PauseReason mPauseReason = PauseReason.UserRequest;

    enum AudioFocus {
        NoFocusNoDuck, // we don't have audio focus, and can't duck
        NoFocusCanDuck, // we don't have focus, but can play at a low volume
                        // ("ducking")
        Focused // we have full audio focus
    }

    AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;

    String mSongTitle = "";

    final int NOTIFICATION_ID = 1;

    MusicRetriever mRetriever;

    Bitmap mDummyAlbumArt;

    ComponentName mMediaButtonReceiverComponent;

    AudioManager mAudioManager;
    NotificationManager mNotificationManager;

    Notification mNotification = null;

    void createMediaPlayerIfNeeded() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            mPlayer.setWakeMode(getApplicationContext(),
                    PowerManager.PARTIAL_WAKE_LOCK);
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
            
//            mVisualizer = new Visualizer(mPlayer.getAudioSessionId());
//            mVisualizer.setEnabled(false);
//            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
//            mVisualizer.setDataCaptureListener(
//                    new Visualizer.OnDataCaptureListener() {
//                        public void onWaveFormDataCapture(
//                                Visualizer visualizer, byte[] bytes,
//                                int samplingRate) {
//                        }
//
//                        public void onFftDataCapture(Visualizer visualizer,
//                                byte[] bytes, int samplingRate) {
//                            updateVisualizer(bytes);
//                        }
//                    }, Visualizer.getMaxCaptureRate() / 2, true, false);
//            mVisualizer.setEnabled(true);
        } else {
            mPlayer.reset();
        }

    }

    @Override
    public void onCreate() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        mRetriever = new MusicRetriever(getContentResolver());
        (new PrepareMusicRetrieverTask(mRetriever, this)).execute();

        if (android.os.Build.VERSION.SDK_INT >= 8)
            mAudioFocusHelper = new AudioFocusHelper(getApplicationContext(),
                    this);
        else
            mAudioFocus = AudioFocus.Focused;

        mDummyAlbumArt = BitmapFactory.decodeResource(getResources(),
                R.drawable.dummy_album_art);

        mMediaButtonReceiverComponent = new ComponentName(this,
                MusicIntentReceiver.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action.equals(ACTION_TOGGLE_PLAYBACK))
            processTogglePlaybackRequest();
        else if (action.equals(ACTION_PLAY))
            processPlayRequest();
        else if (action.equals(ACTION_PAUSE))
            processPauseRequest();
        else if (action.equals(ACTION_SKIP))
            processSkipRequest();
        else if (action.equals(ACTION_STOP))
            processStopRequest();
        else if (action.equals(ACTION_REWIND))
            processRewindRequest();
        else if (action.equals(ACTION_URL)) processAddRequest(intent);

        return START_NOT_STICKY;
    }

    void processTogglePlaybackRequest() {
        if (mState == State.Paused || mState == State.Stopped) {
            processPlayRequest();
        } else {
            processPauseRequest();
        }
    }

    void processPlayRequest() {
        if (mState == State.Retrieving) {
            mWhatToPlayAfterRetrieve = null;
            mStartPlayingAfterRetrieve = true;
            return;
        }

        tryToGetAudioFocus();

        if (mState == State.Stopped) {
            playNextSong(null);
        } else if (mState == State.Paused) {
            mState = State.Playing;
            setUpAsForeground(getString(R.string.playing) + mSongTitle);
            configAndStartMediaPlayer();
        }

    }

    void updateVisualizer(byte[] fft) {
        if(mVisualizer != null) {
            int freq = mVisualizer.getFft(fft);
            Log.d("freq", String.valueOf(freq));
        }
    }

    void processPauseRequest() {
        if (mState == State.Retrieving) {
            mStartPlayingAfterRetrieve = false;
            return;
        }

        if (mState == State.Playing) {
            mState = State.Paused;
            mPlayer.pause();
            relaxResources(false);
        }

    }

    void processRewindRequest() {
        if (mState == State.Playing || mState == State.Paused)
            mPlayer.seekTo(0);
    }

    void processSkipRequest() {
        if (mState == State.Playing || mState == State.Paused) {
            tryToGetAudioFocus();
            playNextSong(null);
        }
    }

    void processStopRequest() {
        processStopRequest(false);
    }

    void processStopRequest(boolean force) {
        if (mState == State.Playing || mState == State.Paused || force) {
            mState = State.Stopped;

            relaxResources(true);
            giveUpAudioFocus();

            stopSelf();
        }
    }

    void relaxResources(boolean releaseMediaPlayer) {
        stopForeground(true);

        if (releaseMediaPlayer && mPlayer != null) {
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }

    }

    void giveUpAudioFocus() {
        if (mAudioFocus == AudioFocus.Focused && mAudioFocusHelper != null
                && mAudioFocusHelper.abandonFocus())
            mAudioFocus = AudioFocus.NoFocusNoDuck;
    }

    void configAndStartMediaPlayer() {
        if (mAudioFocus == AudioFocus.NoFocusNoDuck) {
            if (mPlayer.isPlaying()) mPlayer.pause();
            return;
        } else if (mAudioFocus == AudioFocus.NoFocusCanDuck)
            mPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME); // we'll be relatively
                                                         // quiet
        else
            mPlayer.setVolume(1.0f, 1.0f); // we can be loud

        if (!mPlayer.isPlaying()) mPlayer.start();
    }

    void processAddRequest(Intent intent) {
        if (mState == State.Retrieving) {
            mWhatToPlayAfterRetrieve = intent.getData();
            mStartPlayingAfterRetrieve = true;
        } else if (mState == State.Playing || mState == State.Paused
                || mState == State.Stopped) {
            tryToGetAudioFocus();
            playNextSong(intent.getData().toString());
        }
    }

    void tryToGetAudioFocus() {
        if (mAudioFocus != AudioFocus.Focused && mAudioFocusHelper != null
                && mAudioFocusHelper.requestFocus())
            mAudioFocus = AudioFocus.Focused;
    }

    void playNextSong(String manualUrl) {
        mState = State.Stopped;
        relaxResources(false);
        try {
            MusicRetriever.Item playingItem = null;
            if (manualUrl != null) {
                createMediaPlayerIfNeeded();
                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mPlayer.setDataSource(manualUrl);

                playingItem = new MusicRetriever.Item(0, null, manualUrl, null,
                        0);
            } else {

                playingItem = mRetriever.getRandomItem();
                if (playingItem == null) {
                    Toast.makeText(
                            this,R.string.no_music,
                            Toast.LENGTH_LONG).show();
                    processStopRequest(true); // stop everything!
                    return;
                }

                // set the source of the media player a a content URI
                createMediaPlayerIfNeeded();
                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mPlayer.setDataSource(getApplicationContext(), playingItem
                        .getURI());
            }

            
            mSongTitle = playingItem.getTitle();

            mState = State.Preparing;
            setUpAsForeground(getString(R.string.loading) + mSongTitle);

            MediaButtonHelper.registerMediaButtonEventReceiverCompat(
                    mAudioManager, mMediaButtonReceiverComponent);

            mPlayer.prepareAsync();
        } catch (IOException ex) {
            Log.e("MusicService", "IOException playing next song: "
                    + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void onCompletion(MediaPlayer player) {
        playNextSong(null);
    }

    public void onPrepared(MediaPlayer player) {
        mState = State.Playing;
        updateNotification(getString(R.string.playing) + mSongTitle);
        configAndStartMediaPlayer();
    }

    void updateNotification(String text) {
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(),
                0, new Intent(getApplicationContext(), MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        mNotification.setLatestEventInfo(getApplicationContext(),
                getString(R.string.running), text, pi);
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    void setUpAsForeground(String text) {
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(),
                0, new Intent(getApplicationContext(), MainActivity.class)
                        .setAction(ACTION_STOP),
                PendingIntent.FLAG_UPDATE_CURRENT);
        mNotification = new Notification();
        mNotification.tickerText = text;
        mNotification.icon = R.drawable.ic_stat_playing;
        mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
        mNotification.setLatestEventInfo(getApplicationContext(),
                getString(R.string.running), text, pi);
        startForeground(NOTIFICATION_ID, mNotification);
    }

    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(getApplicationContext(),
                "Media player error! Resetting.", Toast.LENGTH_SHORT).show();

        mState = State.Stopped;
        relaxResources(true);
        giveUpAudioFocus();
        return true; // true indicates we handled the error
    }

    public void onGainedAudioFocus() {
        Toast.makeText(getApplicationContext(), "gained audio focus.",
                Toast.LENGTH_SHORT).show();
        mAudioFocus = AudioFocus.Focused;

        if (mState == State.Playing) configAndStartMediaPlayer();
    }

    public void onLostAudioFocus(boolean canDuck) {
        Toast.makeText(getApplicationContext(),
                "lost audio focus." + (canDuck ? "can duck" : "no duck"),
                Toast.LENGTH_SHORT).show();
        mAudioFocus = canDuck ? AudioFocus.NoFocusCanDuck
                : AudioFocus.NoFocusNoDuck;

        if (mPlayer != null && mPlayer.isPlaying())
            configAndStartMediaPlayer();
    }

    public void onMusicRetrieverPrepared() {
        mState = State.Stopped;

        if (mStartPlayingAfterRetrieve) {
            tryToGetAudioFocus();
            playNextSong(mWhatToPlayAfterRetrieve == null ? null
                    : mWhatToPlayAfterRetrieve.toString());
        }
    }

    @Override
    public void onDestroy() {
        if (mVisualizer != null) {
            mVisualizer.release();
        }
        mState = State.Stopped;
        relaxResources(true);
        giveUpAudioFocus();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
