# AfyaLink keep rules (only needed when minifyEnabled is switched on).
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keep class com.afyalink.app.ui.views.** { *; }
-keep class com.afyalink.app.reminder.** { *; }
-keepclassmembers class * implements java.io.Serializable { *; }
