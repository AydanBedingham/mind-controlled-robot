package com.awesomefacialrecognition;

import android.app.Activity;
import android.app.Application;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.emotiv.insight.IEmoStateDLL;

import com.emotiv.dateget.EngineConnector;
import com.emotiv.dateget.EngineInterface;
import com.emotiv.insight.FacialExpressionDetection;
import com.usbhosttest.command.Command;
import com.usbhosttest.managers.SupremeUsbManager;
import com.usbhosttest.managers.SupremeUsbManagerListener;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements EngineInterface, SupremeUsbManagerListener {

    private boolean canSendCommands;

    private Toast toast;

    private SupremeUsbManager usbManager;

    final MainActivity me = this;

    private static final double DEFAULT_SMILE_THRESHOLD = 0.8;
    private double smileThreshold;

    private boolean leftEyeDetectionPriority;

    @Override
    public void onSupremeUsbManagerConnect(SupremeUsbManager sender, UsbDevice device) {
        ((TextView)this.findViewById(R.id.textViewUsbEnabled)).setText("Usb Status: Connected");

        Command command = new Command("MOVE_LEFT");

        try {
            usbManager.sendCommand(command);
        } catch(Exception ex){
            showToast(ex.getLocalizedMessage());
        }
    }

    public void btnPrintLog_Clicked(View view) throws IOException {
        Command cmd = new Command("MOVE_FORWARD");
        trySendCommand(cmd);
        try {
            Command command = new Command("DEBUG", new String[]{});
            usbManager.sendCommand(command);
        } catch(Exception e) {
            showToast(e.getLocalizedMessage());
        }
    }

    @Override
    public void onSupremeUsbManagerDisconnect(SupremeUsbManager sender, UsbDevice device) {
        ((TextView)this.findViewById(R.id.textViewUsbEnabled)).setText("Usb Status: Disconnected");
    }

    @Override
    public void onSupremeUsbManagerReceiveData(SupremeUsbManager sender, UsbDevice device, Command command) {
        String str = command.getProcedure() + "\r\n";
        for(String param : command.getParameters()){
            str += param + "\r\n";
        }
        showToast(str);
    }

    @Override
    public void onSupremeUsbManagerError(SupremeUsbManager sender, Throwable error) {
        showToast("Usb Error: " + error);
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    private void showToast(String message){
        if (toast!=null)  {toast.cancel();}
        this.toast = Toast.makeText(this.getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }



    @Override
    protected void onStart()
    {
        super.onStart();
        usbManager = new SupremeUsbManager(this);
        usbManager.setProductId(3);
        usbManager.setVendorId(9914);
        usbManager.addListener(this);
        usbManager.start();
    }

    @Override
    public void onPause(){
        usbManager.removeListener(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        usbManager.addListener(this);
    }


    @Override
    protected void onDestroy(){
        usbManager.removeListener(this);
        usbManager.stop();
        super.onDestroy();
    }


    private FaceState nextFaceState;


    private static int face_neutral = R.drawable.face_smile;
    private static int face_wink_left = R.drawable.face_wink_left;
    private static int face_wink_right = R.drawable.face_wink_right;
    private static int face_smile = R.drawable.face_grin;

    private EngineConnector engineConnector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        leftEyeDetectionPriority = true;

        canSendCommands = false;

        setFaceState(face_neutral);

        EngineConnector.setContext(this);
        engineConnector = EngineConnector.shareInstance();
        engineConnector.delegate = this;

        Timer timerListenAction = new Timer();
        timerListenAction.scheduleAtFixedRate(new TimerTask() {
                                                  @Override
                                                  public void run() {
                                                      mHandlerUpdateUI.sendEmptyMessage(1);
                                                  }
                                              },
                0, timerInterval);

        updateUiSmileThreshold();

        SeekBar seekBarSmileThreshold = (SeekBar) this.findViewById(R.id.seekBarSmileThreshold);
        seekBarSmileThreshold.setOnSeekBarChangeListener(new YourListener());

    }


    private void updateUiSmileThreshold(){
        smileThreshold = DEFAULT_SMILE_THRESHOLD;
        int progress = (int)Math.round((smileThreshold / 1) * 100);
        ((SeekBar)this.findViewById(R.id.seekBarSmileThreshold)).setProgress(progress);

        ((TextView)this.findViewById(R.id.textViewSmileThresholdValue)).setText(String.format("%.2f", smileThreshold));
    }

    private void setFaceState(int faceState){
        ImageView imageView = (ImageView)this.findViewById(R.id.imageView);
        imageView.setImageResource(faceState);
    }



    @Override
    public void detectedActionLowerFace(int typeAction, float power) {

        String typeActionStr = "" + typeAction;
        while (typeActionStr.length()<3){
            typeActionStr="0"+typeActionStr;
        }

        ((TextView)this.findViewById(R.id.textViewSmileValue)).setText(typeActionStr + " - " + String.format("%.2f", power));


        if (typeAction==512){
            //((TextView)this.findViewById(R.id.textViewSmileValue)).setText(typeAction + " - " + String.format("%.2f", power));
            if (power>=smileThreshold) {
                this.nextFaceState = FaceState.SMILE;
            }
        }
    }

    @Override
    public void detectedEyeLidMovement(float[] eyeLidState) {
        final int LEFT_EYE = 0;
        final int RIGHT_EYE = 1;

        double leftEyeValue = eyeLidState[LEFT_EYE];
        double rightEyeValue = eyeLidState[RIGHT_EYE];

        if (this.nextFaceState!=FaceState.SMILE) {
            if (leftEyeDetectionPriority) {
                //Check left eye first
                if (leftEyeValue == 1) {
                    this.nextFaceState = FaceState.LEFT_WINK;
                } else if (rightEyeValue == 1) {
                    this.nextFaceState = FaceState.RIGHT_WINK;
                }
            } else {
                //Check right eye first
                if (rightEyeValue == 1) {
                    this.nextFaceState = FaceState.RIGHT_WINK;
                } else if (leftEyeValue == 1) {
                    this.nextFaceState = FaceState.LEFT_WINK;
                }
            }
        }

        ((TextView) me.findViewById(R.id.textViewLWinkValue)).setText(String.format("%.2f", leftEyeValue));
        ((TextView) me.findViewById(R.id.textViewRWinkValue)).setText(String.format("%.2f", rightEyeValue));
    }



    private void trySendCommand(Command command){
        try{
            usbManager.sendCommand(command);
        } catch(Exception ex){
            showToast(ex.getLocalizedMessage());
        }
    }

    ////
    private final int initialWait = 500;
    private boolean countingDown = false;
    private final int timerInterval = 100;
    private final int timerMax = 2000;
    private int timerCountDown = timerMax;
    private boolean displayingFace = false;
    private void updater(){

        if (!countingDown) {
            if (me.nextFaceState!=null) {
                countingDown = true;
                displayingFace = false;
            }
        } else {
            timerCountDown -= timerInterval;

            if (!displayingFace){
                if ((timerMax-timerCountDown)>initialWait) {
                    switch (me.nextFaceState) {
                        case SMILE:
                            me.setFaceState(face_smile);
                            if (canSendCommands) {
                                Command cmd = new Command("MOVE_FORWARD");
                                trySendCommand(cmd);
                            }
                            break;
                        case LEFT_WINK:
                            me.setFaceState(face_wink_left);
                            if (canSendCommands) {
                                Command cmd = new Command("TURN_LEFT");
                                trySendCommand(cmd);
                            }
                            break;
                        case RIGHT_WINK:
                            me.setFaceState(face_wink_right);
                            if (canSendCommands) {
                                Command cmd = new Command("TURN_RIGHT");
                                trySendCommand(cmd);
                            }
                            break;
                        default:
                            break;
                    }
                    displayingFace = true;
                }
            }

            if (timerCountDown<=0) {
                countingDown = false;
                timerCountDown = timerMax;
                displayingFace = false;
                me.nextFaceState = null;
                me.setFaceState(face_neutral);
            }

        }

    }

    public Handler mHandlerUpdateUI = new Handler() {
        public void handleMessage(Message msg) {
            updater();
        }
    };

    @Override public void trainStarted() {}
    @Override public void trainSucceed() {}
    @Override public void trainCompleted() {}
    @Override public void trainRejected() {}
    @Override public void trainErased() {}
    @Override public void trainReset() {}
    @Override public void userAdded(int userId) {}
    @Override public void userRemove() {}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) { return true; }
        return super.onOptionsItemSelected(item);
    }



    //-------------------------------------------------
    public class YourListener implements SeekBar.OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            smileThreshold = (1*(progress/100.00));
            ((TextView) me.findViewById(R.id.textViewSmileThresholdValue)).setText(String.format("%.2f", smileThreshold));
        }
        public void onStartTrackingTouch(SeekBar seekBar) {}
        public void onStopTrackingTouch(SeekBar seekBar) {}
    }


    public void onLeftEyeDetectionPriorityClicked(View v)
    {
        leftEyeDetectionPriority = true;
        ((RadioButton)this.findViewById(R.id.radioButtonRWinkPriority)).setChecked(false);
    }

    public void onRightEyeDetectionPriorityClicked(View v){
        leftEyeDetectionPriority = false;
        ((RadioButton)this.findViewById(R.id.radioButtonLWinkPriority)).setChecked(false);
    }


    public void onSwitchSendCommandsClicked(View v)
    {
        canSendCommands = ((Switch)this.findViewById(R.id.switchSendCommands)).isChecked();
        showToast("Send commands " + (canSendCommands ? "enabled" : "disabled") );
    }

}
