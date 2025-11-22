package com.example.medilink.BookedAppointment;

import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medilink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.util.ArrayList;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.constants.ZegoViewMode;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

public class VideoCall extends AppCompatActivity {

    FrameLayout localContainer, remoteContainer;
    ImageButton btnEndCall, btnMute, btnSwitchCamera;

    SurfaceView localView, remoteView;

    private ZegoExpressEngine engine;
    private String localStreamID, remoteStreamID, roomID;

    private boolean isMuted = false;
    private boolean usingFrontCamera = true;

    private static final long APP_ID = 537370811;
    private static final String APP_SIGN =
            "d2fa45396530d5318f578e057e8a014551f8868c09b992cc0e14bb3bb11ac8d4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        localContainer = findViewById(R.id.local_container);
        remoteContainer = findViewById(R.id.remote_container);

        btnEndCall = findViewById(R.id.btnEndCall);
        btnMute = findViewById(R.id.btnMute);
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera);

        // Create SurfaceViews
        localView = new SurfaceView(this);
        remoteView = new SurfaceView(this);

        localContainer.addView(localView);
        remoteContainer.addView(remoteView);

        // Initialize Zego Engine
        initZegoEngine();
        engine = ZegoExpressEngine.getEngine();

        // Get call details
        roomID = getIntent().getStringExtra("roomID");
        String otherUserID = getIntent().getStringExtra("otherUserID");

        if (roomID == null) roomID = "defaultRoom";

        String localUserID = FirebaseAuth.getInstance().getUid();
        localStreamID = localUserID + "_stream";
        remoteStreamID = otherUserID + "_stream";

        setupZegoEventHandler();

        // Start local preview
        ZegoCanvas localCanvas = new ZegoCanvas(localView);
        localCanvas.viewMode = ZegoViewMode.ASPECT_FILL;
        engine.startPreview(localCanvas);

        // Login + publish
        ZegoUser zegoUser = new ZegoUser(localUserID, "User");
        engine.loginRoom(roomID, zegoUser);
        engine.startPublishingStream(localStreamID);

        // Button: End Call
        btnEndCall.setOnClickListener(v -> {
            String roomID = getIntent().getStringExtra("roomID");
            if (roomID != null) {
                FirebaseFirestore.getInstance()
                        .collection("calls")
                        .document(roomID)
                        .update("status", "ended");
            }
            finish();

        });

        // Button: Mute
        btnMute.setOnClickListener(v -> {
            isMuted = !isMuted;
            engine.muteMicrophone(isMuted);
            Toast.makeText(this, isMuted ? "Muted" : "Unmuted", Toast.LENGTH_SHORT).show();
        });

        // Button: Switch Camera
        btnSwitchCamera.setOnClickListener(v -> {
            usingFrontCamera = !usingFrontCamera;
            engine.useFrontCamera(usingFrontCamera, ZegoPublishChannel.MAIN);
            Toast.makeText(this, usingFrontCamera ? "Front Camera" : "Back Camera",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void setupZegoEventHandler() {
        engine.setEventHandler(new IZegoEventHandler() {

            @Override
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType,
                                           ArrayList<ZegoStream> streamList,
                                           JSONObject extendedData) {

                if (updateType == ZegoUpdateType.ADD) {
                    for (ZegoStream stream : streamList) {
                        if (stream.streamID.equals(remoteStreamID)) {

                            ZegoCanvas remoteCanvas = new ZegoCanvas(remoteView);
                            remoteCanvas.viewMode = ZegoViewMode.ASPECT_FILL;

                            engine.startPlayingStream(remoteStreamID, remoteCanvas);
                        }
                    }
                }

                else if (updateType == ZegoUpdateType.DELETE) {
                    engine.stopPlayingStream(remoteStreamID);
                    Toast.makeText(VideoCall.this, "User left call",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initZegoEngine() {
        if (ZegoExpressEngine.getEngine() == null) {
            ZegoEngineProfile profile = new ZegoEngineProfile();
            profile.appID = APP_ID;
            profile.appSign = APP_SIGN;
            profile.scenario = ZegoScenario.GENERAL;
            profile.application = this.getApplication();

            ZegoExpressEngine.createEngine(profile, null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        engine.stopPreview();
        engine.stopPublishingStream();
        engine.stopPlayingStream(remoteStreamID);
        engine.logoutRoom(roomID);
    }
}
