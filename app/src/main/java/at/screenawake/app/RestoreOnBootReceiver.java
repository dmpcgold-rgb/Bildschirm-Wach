package at.screenawake.app;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
public final class RestoreOnBootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context ctx, Intent intent) {
        AwakeManager.deactivate(ctx);
    }
}
