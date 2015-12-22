package com.anzhuoshoudiantong.music;

import com.anzhuoshoudiantong.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.widget.Toast;

public class MusicIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(
                android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            Toast.makeText(context, R.string.headphone_disconnect,
                    Toast.LENGTH_SHORT).show();

            context.startService(new Intent(MusicService.ACTION_PAUSE));

        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(
                    Intent.EXTRA_KEY_EVENT);
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) return;

            switch (keyEvent.getKeyCode()) {
            case KeyEvent.KEYCODE_HEADSETHOOK:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                context.startService(new Intent(
                        MusicService.ACTION_TOGGLE_PLAYBACK));
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                context.startService(new Intent(MusicService.ACTION_PLAY));
                break;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                context.startService(new Intent(MusicService.ACTION_PAUSE));
                break;
            case KeyEvent.KEYCODE_MEDIA_STOP:
                context.startService(new Intent(MusicService.ACTION_STOP));
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                context.startService(new Intent(MusicService.ACTION_SKIP));
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                context.startService(new Intent(MusicService.ACTION_REWIND));
                break;
            }
        }
    }
}
