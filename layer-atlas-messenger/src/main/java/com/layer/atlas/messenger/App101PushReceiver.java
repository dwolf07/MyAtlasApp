package com.layer.atlas.messenger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.layer.atlas.messenger.App101.keys;

/**
 * @author Oleg Orlov
 */
public class App101PushReceiver extends BroadcastReceiver {
    
    private static final String TAG = App101PushReceiver.class.getSimpleName();
    private static final boolean debug = false;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (debug) Log.w(TAG, "onReceive() action: " + intent.getAction() + ", extras: " + App101.toString(intent.getExtras(), "\n", "\n"));
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            if (debug) Log.w(TAG, "onReceive() Waking Up! due to action: "  + intent.getAction());
            return;
        }
        
        NotificationManager notificationService = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        String msg = intent.getStringExtra("layer-push-message");
        Uri msgUri  = (Uri) intent.getExtras().get("layer-message-id");
        Uri convUri = (Uri) intent.getExtras().get("layer-conversation-id");
        
        int ntfId = msgUri != null ? msgUri.hashCode() : 0;
        Notification.Builder bld = new Notification.Builder(context);
        bld.setContentTitle("Atlas Messenger")
            .setContentInfo("Content Info!")
            .setContentText(msg)
            .setSmallIcon(R.drawable.ic_launcher)
            .setAutoCancel(true)
            .setLights(Color.rgb(0, 255, 0), 100, 1900)
            .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE)
            ;
        
        Intent chatIntent = new Intent(context, AtlasMessagesScreen.class);
        chatIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        chatIntent.putExtra(keys.CONVERSATION_URI, convUri.toString());

        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                context, 0, chatIntent, PendingIntent.FLAG_ONE_SHOT);
        
        bld.setContentIntent(resultPendingIntent);
        
        final Notification notification = bld.getNotification();
        
        try {
            notificationService.notify(ntfId, notification);
        } catch (SecurityException ignored) { 
            // 4.1.2 device required VIBRATE permission when in Vibrate mode. 
            // Fixed in 4.2.1 https://android.googlesource.com/platform/frameworks/base/+/cc2e849
        }
        
    }

}
