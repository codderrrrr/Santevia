package com.example.medilink.BookedAppointment;

import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medilink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    private String otherUserID; // doctor or patient id (the remote user)

    private boolean isMuted = false;
    private boolean usingFrontCamera = true;

    private static final long APP_ID = 537370811L;
    private static final String APP_SIGN =
            "d2fa45396530d5318f578e057e8a014551f8868c09b992cc0e14bb3bb11ac8d4";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        localContainer = findViewById(R.id.local_container);
        remoteContainer = findViewById(R.id.remote_container);

        btnEndCall = findViewById(R.id.btnEndCall);
        btnMute = findViewById(R.id.btnMute);
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera);

        localView = new SurfaceView(this);
        remoteView = new SurfaceView(this);

        localContainer.addView(localView);
        remoteContainer.addView(remoteView);

        initZegoEngine();
        engine = ZegoExpressEngine.getEngine();

        roomID = getIntent().getStringExtra("roomID");
        otherUserID = getIntent().getStringExtra("otherUserID");

        if (roomID == null) roomID = "defaultRoom";

        String localUserID = FirebaseAuth.getInstance().getUid();
        if (localUserID == null) localUserID = "unknown_user";
        localStreamID = localUserID + "_stream";
        remoteStreamID = otherUserID + "_stream";

        setupZegoEventHandler();

        // Start preview and publish
        ZegoCanvas localCanvas = new ZegoCanvas(localView);
        localCanvas.viewMode = ZegoViewMode.ASPECT_FILL;
        engine.startPreview(localCanvas);

        ZegoUser zegoUser = new ZegoUser(localUserID, "User");
        engine.loginRoom(roomID, zegoUser);
        engine.startPublishingStream(localStreamID);

        btnEndCall.setOnClickListener(v -> endCallAndFinish());

        btnMute.setOnClickListener(v -> {
            isMuted = !isMuted;
            engine.muteMicrophone(isMuted);
            Toast.makeText(this, isMuted ? "Muted" : "Unmuted", Toast.LENGTH_SHORT).show();
        });

        btnSwitchCamera.setOnClickListener(v -> {
            usingFrontCamera = !usingFrontCamera;
            engine.useFrontCamera(usingFrontCamera, ZegoPublishChannel.MAIN);
            Toast.makeText(this, usingFrontCamera ? "Front Camera" : "Back Camera",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void setupZegoEventHandler() {
        if (engine == null) return;

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
                } else if (updateType == ZegoUpdateType.DELETE) {
                    engine.stopPlayingStream(remoteStreamID);
                    Toast.makeText(VideoCall.this, "User left call", Toast.LENGTH_SHORT).show();
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

    private void endCallAndFinish() {
        // 1) Update Firestore signaling: mark ended and remove the incomingCall doc (if present)
        if (otherUserID != null) {
            // otherUserID is the remote user (doctor or patient). If we started the call, the doc is under doctor's doc.
            // We attempt both: update / delete to be safe.
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Try delete the incomingCall doc under doctor -> incomingCall/call
            db.collection("Calls")
                    .document(otherUserID)
                    .collection("incomingCall")
                    .document("call")
                    .delete()
                    .addOnCompleteListener(task -> {
                        // ignore result; just ensure cleanup
                    });

            // also set a status document under Calls/{otherUserID} to mark ended (optional)
            Map<String, Object> statusMap = new HashMap<>();
            statusMap.put("status", "ended");
            statusMap.put("endedBy", FirebaseAuth.getInstance().getUid());
            statusMap.put("timestamp", System.currentTimeMillis());
            db.collection("Calls")
                    .document(otherUserID)
                    .set(statusMap, com.google.firebase.firestore.SetOptions.merge());
        }

        // 2) Stop and cleanup Zego resources robustly
        try {
            if (engine != null) {
                engine.stopPreview();
                engine.stopPublishingStream();
                engine.stopPlayingStream(remoteStreamID);
                engine.logoutRoom(roomID);

                // destroy engine to allow new calls to initialize properly
                ZegoExpressEngine.destroyEngine(null);
                engine = null;
            }
        } catch (Exception ignored) {
        }

        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // We don't auto-destroy here because activity may be backgrounded temporarily,
        // but if finishing we will destroy in onDestroy/endCall.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // final cleanup to ensure next call works
        try {
            if (engine != null) {
                engine.stopPreview();
                engine.stopPublishingStream();
                engine.stopPlayingStream(remoteStreamID);
                engine.logoutRoom(roomID);
                ZegoExpressEngine.destroyEngine(null);
                engine = null;
            }
        } catch (Exception ignored) {}
    }
}
