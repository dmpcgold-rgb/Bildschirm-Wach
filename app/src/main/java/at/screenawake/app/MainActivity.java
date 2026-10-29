package at.screenawake.app;

import android.app.Activity;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public final class MainActivity extends Activity {
    private static final int BG = Color.rgb(7,18,27);
    private static final int SURFACE = Color.rgb(17,34,45);
    private static final int GREEN = Color.rgb(96,229,183);
    private static final int WHITE = Color.rgb(244,249,250);
    private static final int MUTED = Color.rgb(155,174,188);
    private TextView badge, status, detail;
    private Button mainButton, permission;
    private final Button[] durations = new Button[3];

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        build();
    }
    @Override protected void onResume() {
        super.onResume();
        if (status != null) refresh();
    }
    private int dp(float value) {
        return (int)(getResources().getDisplayMetrics().density * value + .5f);
    }
    private GradientDrawable bg(int color, int radius, boolean outline) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        if (outline) g.setStroke(dp(1), Color.rgb(46,68,78));
        return g;
    }
    private TextView text(String value, int sp, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(value); v.setTextColor(color); v.setTextSize(sp);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }
    private void add(LinearLayout target, View v, int top, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.topMargin = dp(top); p.bottomMargin = dp(bottom);
        target.addView(v, p);
    }
    private void build() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true); scroll.setBackgroundColor(BG);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(30), dp(24), dp(30));
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            int l=0,t=0,r=0,b=0;
            if (Build.VERSION.SDK_INT >= 30) {
                Insets i = insets.getInsets(WindowInsets.Type.systemBars());
                l=i.left; t=i.top; r=i.right; b=i.bottom;
            } else {
                l=insets.getSystemWindowInsetLeft(); t=insets.getSystemWindowInsetTop();
                r=insets.getSystemWindowInsetRight(); b=insets.getSystemWindowInsetBottom();
            }
            v.setPadding(dp(24)+l,dp(30)+t,dp(24)+r,dp(30)+b);
            return insets;
        });
        scroll.addView(root);
        TextView brand = text("◉  BILDSCHIRM WACH",12,GREEN,true);
        brand.setLetterSpacing(.1f);
        root.addView(brand);
        add(root,text("Dein Display.\nDeine Kontrolle.",32,WHITE,true),18,8);
        add(root,text("Serien und Videos ansehen, ohne dass das Display automatisch ausgeht.",15,MUTED,false),0,25);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(22),dp(26),dp(22),dp(24));
        card.setBackground(bg(SURFACE,24,false));
        add(root,card,0,16);
        badge = text("● AUS",13,MUTED,true);
        card.addView(badge);
        status = text("Display läuft normal",23,WHITE,true);
        add(card,status,12,7);
        detail = text("Deine bisherige Timeout-Einstellung ist aktiv.",14,MUTED,false);
        add(card,detail,0,22);
        mainButton = new Button(this);
        mainButton.setAllCaps(false); mainButton.setTextSize(17);
        mainButton.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        mainButton.setMinHeight(dp(62));
        mainButton.setOnClickListener(v -> toggle());
        add(card,mainButton,0,0);

        add(root,text("AUTOMATISCH AUSSCHALTEN",12,MUTED,true),15,10);
        LinearLayout options = new LinearLayout(this);
        options.setOrientation(LinearLayout.HORIZONTAL);
        String[] labels = {"2 Stunden","4 Stunden","Manuell"};
        int[] values = {120,240,0};
        for (int n=0;n<3;n++) {
            final int duration=values[n];
            Button b=new Button(this);
            b.setAllCaps(false); b.setText(labels[n]); b.setTextSize(12);
            b.setMinHeight(dp(48)); b.setMinWidth(0);
            b.setPadding(dp(2),0,dp(2),0);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,dp(48),1);
            if (n>0) p.leftMargin=dp(7);
            options.addView(b,p);
            b.setOnClickListener(v -> {
                AwakeManager.setDuration(this,duration);
                if (AwakeManager.isActive(this) && !AwakeManager.activate(this))
                    Toast.makeText(this,"Zeit konnte nicht geändert werden",Toast.LENGTH_LONG).show();
                refresh();
            });
            durations[n]=b;
        }
        root.addView(options);
        add(root,text("Im manuellen Modus bleibt das Display bis zum Ausschalten wach. Android kann Zeitbegrenzungen leicht verzögern.",12,MUTED,false),12,16);

        permission=new Button(this);
        permission.setAllCaps(false); permission.setTextSize(14);
        permission.setText("Berechtigung erteilen");
        permission.setTextColor(BG); permission.setBackground(bg(GREEN,15,false));
        permission.setOnClickListener(v -> askPermission());
        add(root,permission,0,5);

        Button quick=new Button(this);
        quick.setAllCaps(false); quick.setTextSize(13);
        quick.setText("＋  Schnellschalter hinzufügen");
        quick.setTextColor(WHITE); quick.setBackground(bg(SURFACE,15,true));
        quick.setOnClickListener(v -> addTile());
        add(root,quick,14,14);
        add(root,text("✓ Ohne Werbung  ·  Kein Internetzugriff\n\nDie Seitentaste sperrt weiterhin normal. Der Energiesparmodus und die automatische Helligkeit bleiben aktiv.",12,MUTED,false),0,0);
        setContentView(scroll);
        refresh();
    }
    private void toggle() {
        if (!AwakeManager.canWrite(this)) { askPermission(); return; }
        boolean ok = AwakeManager.isActive(this) ?
            AwakeManager.deactivate(this) : AwakeManager.activate(this);
        if (!ok) Toast.makeText(this,"Android hat die Änderung nicht erlaubt. Bitte Berechtigung prüfen.",Toast.LENGTH_LONG).show();
        refresh();
    }
    private void refresh() {
        boolean active=AwakeManager.isActive(this);
        badge.setText(active?"● AKTIV":"● AUS");
        badge.setTextColor(active?GREEN:MUTED);
        status.setText(active?"Bildschirm bleibt wach":"Display läuft normal");
        detail.setText(active ? "Der Bildschirm-Timeout ist verlängert – auch in anderen Apps." :
            "Deine ursprüngliche Timeout-Einstellung wird verwendet.");
        mainButton.setText(active?"Wachhalten ausschalten":"Bildschirm wach halten");
        mainButton.setTextColor(active?WHITE:BG);
        mainButton.setBackground(bg(active?Color.rgb(44,66,77):GREEN,18,false));
        permission.setVisibility(AwakeManager.canWrite(this)?View.GONE:View.VISIBLE);
        int selected=AwakeManager.duration(this);
        int[] values={120,240,0};
        for (int n=0;n<3;n++) {
            boolean chosen=selected==values[n];
            durations[n].setBackground(bg(chosen?GREEN:SURFACE,13,!chosen));
            durations[n].setTextColor(chosen?BG:WHITE);
        }
    }
    private void askPermission() {
        Toast.makeText(this,"Bitte 'Systemeinstellungen ändern' zulassen",Toast.LENGTH_LONG).show();
        Intent intent=new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
            Uri.parse("package:"+getPackageName()));
        try { startActivity(intent); }
        catch (Exception e) { startActivity(new Intent(Settings.ACTION_SETTINGS)); }
    }
    private void addTile() {
        if (Build.VERSION.SDK_INT>=33) {
            StatusBarManager manager=getSystemService(StatusBarManager.class);
            if (manager==null) return;
            manager.requestAddTileService(
                new ComponentName(this,AwakeTileService.class),
                getString(R.string.quick_tile_label),
                Icon.createWithResource(this,R.drawable.ic_launcher),
                getMainExecutor(),
                result -> Toast.makeText(this,
                    result==StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED?
                        "Schnellschalter hinzugefügt":"Schnellpanel öffnen und Schalter manuell hinzufügen",
                    Toast.LENGTH_LONG).show());
        } else {
            Toast.makeText(this,"Schnellpanel → Bearbeiten → Bildschirm Wach hinzufügen",Toast.LENGTH_LONG).show();
        }
    }
}
