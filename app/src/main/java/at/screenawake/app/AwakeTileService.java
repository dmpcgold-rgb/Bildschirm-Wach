package at.screenawake.app;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

public final class AwakeTileService extends TileService {
    @Override public void onStartListening() {
        super.onStartListening();
        refresh();
    }

    @Override public void onClick() {
        super.onClick();
        if (!AwakeManager.canWrite(this)) {
            Intent open = new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (Build.VERSION.SDK_INT >= 34) {
                PendingIntent pending = PendingIntent.getActivity(this, 0, open,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                startActivityAndCollapse(pending);
            } else {
                startActivityAndCollapse(open);
            }
            return;
        }
        boolean ok = AwakeManager.isActive(this) ?
                AwakeManager.deactivate(this) : AwakeManager.activate(this);
        if (!ok) Toast.makeText(this, "Berechtigung und Einstellungen prüfen",
            Toast.LENGTH_SHORT).show();
        refresh();
    }

    private void refresh() {
        Tile tile = getQsTile();
        if (tile == null) return;
        boolean active = AwakeManager.isActive(this);
        tile.setState(active ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.setLabel("Bildschirm Wach");
        if (Build.VERSION.SDK_INT >= 29) tile.setSubtitle(active ? "EIN" : "AUS");
        tile.updateTile();
    }
}
