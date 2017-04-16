package de.weis.camera2probe;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.hardware.camera2.CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE;
import static android.hardware.camera2.CameraMetadata.CONTROL_AE_MODE_OFF;
import static android.hardware.camera2.CameraMetadata.CONTROL_AE_MODE_ON;
import static android.hardware.camera2.CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH;
import static android.hardware.camera2.CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH;
import static android.hardware.camera2.CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_MODE_AUTO;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_MODE_EDOF;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_MODE_MACRO;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_MODE_OFF;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_AUTO;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_OFF;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_SHADE;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_TWILIGHT;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_WARM_FLUORESCENT;
import static android.hardware.camera2.CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_3;
import static android.hardware.camera2.CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL;
import static android.hardware.camera2.CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY;
import static android.hardware.camera2.CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED;

public class MainActivity extends AppCompatActivity {
    android.hardware.camera2.CameraManager manager;
    CameraCharacteristics characteristics = null;
    SharedPreferences prefs;

    WebView tv;
    Button btn_send;
    String result = "Probing...";
    String result_mail = "";

    String fpos = "<font style=\"color:#00aa00;\">";
    String fneg = "<font style=\"color:#990000;\">";
    String check = "<div style=\"float:left;width:20px;color:#00aa00;\">&#x2713;</div>";
    String cross = "<div style=\"float:left;width:20px;color:#990000;\">&#x2717;</div>";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_send = (Button) findViewById(R.id.btn_send);
        btn_send.setEnabled(false);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "weis.tobi+camera2@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Camera2 Supported Features");
                emailIntent.putExtra(Intent.EXTRA_TEXT, result_mail);
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });

        tv = (WebView) findViewById(R.id.textview_probe);
        tv.loadData(result, "text/html", "utf-8");

        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String cameraId = null;
        try {
            cameraId = manager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        try {
            characteristics = manager.getCameraCharacteristics(cameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        startProbe();
    }

    public void startProbe() {
        result = "";
        model();
        general();
        ae();
        af();
        awb();
        check_raw();

        tv.loadData(result, "text/html", "utf-8");
        btn_send.setEnabled(true);
    }

    public void model() {
        result += "<b>Model</b><br>";
        result += "Model: " + Build.MODEL + "<br>";
        result += "Manufacturer: " + Build.MANUFACTURER + "<br>";
        result += "Build version: " + android.os.Build.VERSION.RELEASE + "<br>";
        result += "SDK version: " + android.os.Build.VERSION.SDK_INT + "<br>";

        result_mail += "Model:" + Build.MODEL + "\n";
        result_mail += "Manufacturer:" + Build.MANUFACTURER + "\n";
        result_mail += "Build:" + android.os.Build.VERSION.RELEASE + "\n";
        result_mail += "SDK:" + android.os.Build.VERSION.SDK_INT + "\n";

    }

    private static boolean contains(int[] modes, int mode) {
        if (modes == null) {
            return false;
        }
        for (int i : modes) {
            if (i == mode) {
                return true;
            }
        }
        return false;
    }

    public void check_raw(){
        result += "<br><b>RAW capture</b><br>";
        if (contains(characteristics.get(
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES),
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW)){
            result += check + fpos + "RAW capture available</font><br style=\"clear:both;\">";
            result_mail += "RawCapture:" + 1 + "\n";
        }else{
            result += cross + fneg + "RAW capture NOT available</font><br style=\"clear:both;\">";
            result_mail += "RawCapture:" + 0 + "\n";
        }

    }

    public void general() {
        result += "<br><b>Hardware Level Support Category</b><br>";
        Integer mylevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        List<Pair> levels = new ArrayList<>();
        levels.add(new Pair<>(INFO_SUPPORTED_HARDWARE_LEVEL_3, "Level_3"));
        levels.add(new Pair<>(INFO_SUPPORTED_HARDWARE_LEVEL_FULL, "Full"));
        levels.add(new Pair<>(INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED, "Limited"));
        levels.add(new Pair<>(INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY, "Legacy"));

        /*
        Log.d("SL:", "Full:"+INFO_SUPPORTED_HARDWARE_LEVEL_FULL);
        Log.d("SL:", "Limited:"+INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED);
        Log.d("SL:", "Legacy:"+INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY);
        Log.d("SL:", "Level3:"+INFO_SUPPORTED_HARDWARE_LEVEL_3);
        */

        result_mail += "SupportLevel:" + mylevel + "\n";
        for (Pair<Integer, String> l : levels) {
            if (l.first == mylevel) {
                result += check + fpos + l.second + "</font><br style=\"clear:both;\">";
                result_mail += "SupportLevel HR:" + l.second + "\n";
            } else {
                result += cross + fneg + l.second + "</font><br style=\"clear:both;\">";
            }
        }

    }

    public void awb() {
        result += "<br><b>Whitebalance</b><br>";
        List<Pair> ml = new ArrayList<>();
        ml.add(new Pair<>(CONTROL_AWB_MODE_OFF, "Whitebalance off"));
        ml.add(new Pair<>(CONTROL_AWB_MODE_AUTO, "Automatic whitebalance"));
        ml.add(new Pair<>(CONTROL_AWB_MODE_CLOUDY_DAYLIGHT, "WB: cloudy day"));
        ml.add(new Pair<>(CONTROL_AWB_MODE_DAYLIGHT, "WB: day"));
        ml.add(new Pair<>(CONTROL_AWB_MODE_FLUORESCENT, "WB: fluorescent"));
        ml.add(new Pair<>(CONTROL_AWB_MODE_INCANDESCENT, "WB: incandescent"));
        ml.add(new Pair<>(CONTROL_AWB_MODE_SHADE, "WB: shade"));
        ml.add(new Pair<>(CONTROL_AWB_MODE_TWILIGHT, "WB: twilight"));
        ml.add(new Pair<>(CONTROL_AWB_MODE_WARM_FLUORESCENT, "WB: warm fluorescent"));

        int[] tmp = characteristics.get(CONTROL_AWB_AVAILABLE_MODES);
        List<Integer> aelist = new ArrayList<Integer>();
        for (int index = 0; index < tmp.length; index++) {
            aelist.add(tmp[index]);
        }

        for (Pair<Integer, String> kv : ml) {
            if (aelist.contains(kv.first)) {
                result += check + fpos + kv.second + "</font><br style=\"clear:both;\">";
                result_mail += kv.second + ":" + 1 + "\n";
            } else {
                result_mail += kv.second + ":" + 0 + "\n";
                result += cross + fneg + kv.second + "</font><br style=\"clear:both;\">";
            }
        }

        try {
            if (characteristics.get(CONTROL_AWB_LOCK_AVAILABLE)) {
                result += check + fpos + "AWB Lock" + "</font><br style=\"clear:both;\">";
                result_mail += "AWB Lock:" + 1 + "\n";
            } else {
                result_mail += "AWB Lock:" + 0 + "\n";
                result += cross + fneg + "AWB Lock" + "</font><br style=\"clear:both;\">";
            }
        }catch(Exception e){}
    }

    public void af() {
        result += "<br><b>Focus</b><br>";
        // not able to get the enum/key names from the ints,
        // so I am doing it myself
        List<Pair> ml = new ArrayList<>();
        ml.add(new Pair<>(CONTROL_AF_MODE_OFF, "Locked focus"));
        ml.add(new Pair<>(CONTROL_AF_MODE_AUTO, "Auto focus"));
        ml.add(new Pair<>(CONTROL_AF_MODE_MACRO, "Auto focus macro"));
        ml.add(new Pair<>(CONTROL_AF_MODE_CONTINUOUS_PICTURE, "Auto focus continuous picture"));
        ml.add(new Pair<>(CONTROL_AF_MODE_CONTINUOUS_VIDEO, "Auto focus continuous video"));
        ml.add(new Pair<>(CONTROL_AF_MODE_EDOF, "Auto focus EDOF"));

        int[] tmp = characteristics.get(CONTROL_AF_AVAILABLE_MODES);
        List<Integer> aelist = new ArrayList<Integer>();
        for (int index = 0; index < tmp.length; index++) {
            aelist.add(tmp[index]);
        }

        for (Pair<Integer, String> kv : ml) {
            if (aelist.contains(kv.first)) {
                result += check + fpos + kv.second + "</font><br style=\"clear:both;\">";
                result_mail += kv.second + ":" + 1 + "\n";
            } else {
                result += cross + fneg + kv.second + "</font><br style=\"clear:both;\">";
                result_mail += kv.second + ":" + 0 + "\n";
            }
        }
    }

    public void ae() {
        result += "<br><b>Exposure</b><br>";
        // not able to get the enum/key names from the ints,
        // so I am doing it myself
        List<Pair> ml = new ArrayList<>();
        ml.add(new Pair<>(CONTROL_AE_MODE_OFF, "Locked exposure"));
        ml.add(new Pair<>(CONTROL_AE_MODE_ON, "Auto exposure"));
        ml.add(new Pair<>(CONTROL_AE_MODE_ON_ALWAYS_FLASH, "Auto exposure, always flash"));
        ml.add(new Pair<>(CONTROL_AE_MODE_ON_AUTO_FLASH, "Auto exposure, auto flash"));
        ml.add(new Pair<>(CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE, "Auto exposure, auto flash redeye"));

        int[] tmp = characteristics.get(CONTROL_AE_AVAILABLE_MODES);
        List<Integer> aelist = new ArrayList<Integer>();
        for (int index = 0; index < tmp.length; index++) {
            aelist.add(tmp[index]);
        }

        for (Pair<Integer, String> kv : ml) {
            if (aelist.contains(kv.first)) {
                result += check + fpos + kv.second + "</font><br style=\"clear:both;\">";
                result_mail += kv.second + ":" + 1 + "\n";
            } else {
                result += cross + fneg + kv.second + "</font><br style=\"clear:both;\">";
                result_mail += kv.second + ":" + 0 + "\n";
            }
        }

        try {
            if (characteristics.get(CONTROL_AE_LOCK_AVAILABLE)) {
                result += check + fpos + "AE Lock" + "</font><br style=\"clear:both;\">";
                result_mail += "AF Lock:" + 1 + "\n";
            } else {
                result += cross + fneg + "AE Lock" + "</font><br style=\"clear:both;\">";
                result_mail += "AF Lock:" + 0 + "\n";
            }
        }catch(Exception e){}

    }
}
