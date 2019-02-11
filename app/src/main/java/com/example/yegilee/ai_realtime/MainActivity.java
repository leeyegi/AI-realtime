package com.example.yegilee.ai_realtime;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Collections;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static java.security.AccessController.getContext;


public class MainActivity  extends BlunoLibrary {
    //ui구성에 필요한 인스턴스
    private TextView textState;
    private Button buttonScan;
    private Button buttonStart;
    private Button buttonEnd;
    private TextView timeCount;
    private ImageView toothImage;
    private TextView toothState;

    //자바내부 동작 인스턴스
    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L ;
    Handler handler;
    int Seconds, Minutes, MilliSeconds;
	boolean toothflag=false;
	boolean arr1save=true;
	boolean arr2save=false;

    //real-time에 필요한 인스턴스
	private static final int N_SAMPLES = 80;
	private static List<Float> ax;
	private static List<Float> ay;
	private static List<Float> az;
	private static List<Float> gx;
	private static List<Float> gy;
	private static List<Float> gz;
	private static List<Float> ax2;
	private static List<Float> ay2;
	private static List<Float> az2;
	private static List<Float> gx2;
	private static List<Float> gy2;
	private static List<Float> gz2;

	//prediction 한 결과를 담는 인스턴스
	private float[] results;
	//private float[] resultsTmp;

	private TensorFlowClassifier classifier;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		onCreateProcess();                                          //onCreate Process by BlunoLibrary

		serialBegin(115200);                                       //set the Uart Baudrate on BLE chip to 115200

        //타이머기능을 위한 handler
        handler = new Handler() ;

        //인스턴스와 layout id 연결
        textState=(TextView)findViewById(R.id.textState);
        buttonScan=(Button)findViewById(R.id.buttonScan);
		buttonStart = (Button)findViewById(R.id.buttonStart);
		buttonEnd=(Button)findViewById(R.id.buttonEnd);
        timeCount=(TextView)findViewById(R.id.timeCount);
        toothImage=(ImageView)findViewById(R.id.toothImage);
        toothState=(TextView) findViewById(R.id.toothState);

        //버튼 리스너
		buttonScan.setOnClickListener(buttonScanListener);
        buttonStart.setOnClickListener(buttonStartListener);
        buttonEnd.setOnClickListener(buttonEndListener);

        //센서 데이터 값 배열 생성
		ax = new ArrayList<>();
		ay = new ArrayList<>();
		az = new ArrayList<>();
		gx = new ArrayList<>();
		gy = new ArrayList<>();
		gz = new ArrayList<>();
		ax2 = new ArrayList<>();
		ay2 = new ArrayList<>();
		az2 = new ArrayList<>();
		gx2 = new ArrayList<>();
		gy2 = new ArrayList<>();
		gz2 = new ArrayList<>();

		//tensorflow 분류기 선언
		classifier = new TensorFlowClassifier(getApplicationContext());
	}

	//====================================
    //블루투스 연결과 관련된 메소드들
    //====================================

    protected void onResume(){
		super.onResume();
		System.out.println("BlUNOActivity onResume");
		onResumeProcess();                                          //onResume Process by BlunoLibrary
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		onActivityResultProcess(requestCode, resultCode, data);               //onActivityResult Process by BlunoLibrary
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onPause() {
		super.onPause();
		onPauseProcess();                                          //onPause Process by BlunoLibrary
	}

	protected void onStop() {
		super.onStop();
		onStopProcess();                                          //onStop Process by BlunoLibrary
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		onDestroyProcess();                                          //onDestroy Process by BlunoLibrary
	}


	@Override
	public void onConectionStateChange(connectionStateEnum theConnectionState) {//Once connection state changes, this function will be called
		switch (theConnectionState) {                                 //Four connection state
			case isConnected:
				textState.setText("Connected");
				break;
			case isConnecting:
				textState.setText("Connecting");
				break;
         /*
      case isToScan:
         textState.setText("Scan");
         break;*/
			case isScanning:
				textState.setText("Scanning");
				break;
			case isDisconnecting:
				textState.setText("isDisconnecting");
				break;
			default:
				break;
		}
		if(textState.getText().equals("Connected")){
			buttonScan.setEnabled(false);
		}else{
			buttonScan.setEnabled(true);

		}
	}

    //====================================
    //블루투스데이터 처리 메소드
    //====================================

    //받아오는 데이터 처리하기 위한 인스턴스
	StringBuilder  bufferStr=new StringBuilder ();
	int index1=0;
	int index2=0;
	String saveStr;
	StringBuilder tmp;
	String save[];

	@Override
	public void onSerialReceived(String theString) {                     //Once connection data received, this function will be called
		// TODO Auto-generated method stub

		bufferStr.append(theString);

		if(bufferStr.length()>25 && bufferStr.lastIndexOf("\n")>1) {

			index2 = bufferStr.lastIndexOf("\n");

			//데이터 저장
			saveStr = bufferStr.substring(index1, index2);
			tmp = bufferStr.delete(0, index2);

			save=saveStr.split(" ");
			if (toothflag == true && save.length==6) {
				if(arr1save==true) {
					ax.add(Float.valueOf(save[0]));
					ay.add(Float.valueOf(save[1]));
					az.add(Float.valueOf(save[2]));
					gx.add(Float.valueOf(save[3]));
					gy.add(Float.valueOf(save[4]));
					gz.add(Float.valueOf(save[5]));
					}
				if(arr2save==true) {
					ax2.add(Float.valueOf(save[0]));
					ay2.add(Float.valueOf(save[1]));
					az2.add(Float.valueOf(save[2]));
					gx2.add(Float.valueOf(save[3]));
					gy2.add(Float.valueOf(save[4]));
					gz2.add(Float.valueOf(save[5]));
					Log.e("toothflag2 save", String.valueOf(ax2.size()));
				}
				}
			//}

			if(gz.size()==20){
				arr2save=true;
			}


			index1 = 1;
			//각 사이즈가 50개씩 쌓이면 아래 코드 실행
			if (gz.size() == N_SAMPLES) {
				Log.e("toothflag save", String.valueOf(gz.size()));

				activityPrediction(ax, ay, az, gx, gy, gz);
				ax.clear();
				ay.clear();
				az.clear();
				gx.clear();
				gy.clear();
				gz.clear();
			}
			if (gz2.size() == N_SAMPLES) {
				Log.e("toothflag save", String.valueOf(gz2.size()));

				activityPrediction(ax2, ay2, az2, gx2, gy2, gz2);
				ax2.clear();
				ay2.clear();
				az2.clear();
				gx2.clear();
				gy2.clear();
				gz2.clear();
			}
		}


		//The Serial data from the BLUNO may be sub-packaged, so using a buffer to hold the String is a good choice.
		//((ScrollView)toothState.getParent()).fullScroll(View.FOCUS_DOWN);
	}

    //====================================
    //데이터를 받아 50개씩 모은 후 신경망에서 평가하는 메소드
    //====================================
	private void activityPrediction(List<Float> arrax, List<Float> array, List<Float> arraz, List<Float> arrgx, List<Float> arrgy, List<Float> arrgz) {

		//리스트에 각 값을 저장
		List<Float> data = new ArrayList<>();
		data.addAll(arrax);
		data.addAll(array);
		data.addAll(arraz);

		data.addAll(arrgx);
		data.addAll(arrgy);
		data.addAll(arrgz);

		//prediction한 class의 probability를 배열에 담아줌
		results = classifier.predictProbabilities(toFloatArray(data));
		float[] resultsTmp=new float[results.length];
		System.arraycopy(results, 0,resultsTmp, 0,results.length);

		//prabability가 낮은 순으로 소팅 수행
		Arrays.sort(resultsTmp);

		toothState.setText(" ");
		for(int i=0;i<16;i+=2)
		{
			//toothState.append(String.valueOf(i+1 +" -> "+results[i])+" ");
			toothState.append(String.format("%2d -> %.10f \t", i + 1, results[i]));
			toothState.append(String.format("%2d -> %.10f \n", i + 2, results[i+1]));
		}

		if(resultsTmp[15]==results[0]) {
			toothImage.setImageResource(R.drawable.class1);
			toothState.append("\nclass 1 \n");
		}
		else if(resultsTmp[15]==results[1]) {
			toothImage.setImageResource(R.drawable.class2);
			toothState.append("\nclass 2 \n");
		}
		else if(resultsTmp[15]==results[2]) {
			toothImage.setImageResource(R.drawable.class3);
			toothState.append("\nclass 3 \n");
		}
		else if(resultsTmp[15]==results[3]) {
			toothImage.setImageResource(R.drawable.class4);
			toothState.append("\nclass 4 \n");
		}
		else if(resultsTmp[15]==results[4]) {
			toothImage.setImageResource(R.drawable.class5);
			toothState.append("\nclass 5 \n");
		}
		else if(resultsTmp[15]==results[5]) {
			toothImage.setImageResource(R.drawable.class6);
			toothState.append("\nclass 6 \n");
		}
		else if(resultsTmp[15]==results[6]) {
			toothImage.setImageResource(R.drawable.class7);
			toothState.append("\nclass 7 \n");
		}
		else if(resultsTmp[15]==results[7]) {
			toothImage.setImageResource(R.drawable.class8);
			toothState.append("\nclass 8 \n");
		}
		else if(resultsTmp[15]==results[8]) {
			toothImage.setImageResource(R.drawable.class9);
			toothState.append("\nclass 9 \n");
		}
		else if(resultsTmp[15]==results[9]) {
			toothImage.setImageResource(R.drawable.class10);
			toothState.append("\nclass 10 \n");
		}
		else if(resultsTmp[15]==results[10]) {
			toothImage.setImageResource(R.drawable.class11);
			toothState.append("\nclass 11 \n");
		}
		else if(resultsTmp[15]==results[11]) {
			toothImage.setImageResource(R.drawable.class12);
			toothState.append("\nclass 12 \n");
		}
		else if(resultsTmp[15]==results[12]) {
			toothImage.setImageResource(R.drawable.class13);
			toothState.append("\nclass 13 \n");
		}
		else if(resultsTmp[15]==results[13]) {
			toothImage.setImageResource(R.drawable.class14);
			toothState.append("\nclass 14 \n");
		}
		else if(resultsTmp[15]==results[14]) {
			toothImage.setImageResource(R.drawable.class15);
			toothState.append("\nclass 15 \n");
		}
		else if(resultsTmp[15]==results[15]) {
			toothImage.setImageResource(R.drawable.class16);
			toothState.append("\nclass 16 \n");
		}

	}
/*
	private void showToothImage(float[] results, float[] resultsTmp){

		for(int i=0;i<resultsTmp.length;i++){

			toothState.append(String.valueOf(results[i] +"  ->  "+resultsTmp[i])+" ");
				/*
				if(compareResults[i]==resultsTmp[15]){

					int rank=i+1;
					Log.e("rank", String.valueOf(rank));
					if(rank==1) toothImage.setImageResource(R.drawable.class1);
				 	else if(rank==2) toothImage.setImageResource(R.drawable.class2);
					else if(rank==3) toothImage.setImageResource(R.drawable.class3);
					else if(rank==4) toothImage.setImageResource(R.drawable.class4);
					else if(rank==5) toothImage.setImageResource(R.drawable.class5);
					else if(rank==6) toothImage.setImageResource(R.drawable.class6);
					else if(rank==7) toothImage.setImageResource(R.drawable.class7);
					else if(rank==8) toothImage.setImageResource(R.drawable.class8);
					else if(rank==9) toothImage.setImageResource(R.drawable.class9);
					else if(rank==10) toothImage.setImageResource(R.drawable.class10);
					else if(rank==11) toothImage.setImageResource(R.drawable.class11);
					else if(rank==12) toothImage.setImageResource(R.drawable.class12);
					else if(rank==13) toothImage.setImageResource(R.drawable.class13);
					else if(rank==14) toothImage.setImageResource(R.drawable.class14);
					else if(rank==15) toothImage.setImageResource(R.drawable.class15);
					else if(rank==16) toothImage.setImageResource(R.drawable.class16);
				}
		}

	}
*/
	private float[] toFloatArray(List<Float> list) {
		int i = 0;
		float[] array = new float[list.size()];

		for (Float f : list) {
			array[i++] = (f != null ? f : Float.NaN);
		}
		return array;
	}


    //====================================
    //버튼 리스너 메소드들
    //====================================

	View.OnClickListener buttonScanListener=new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			buttonScanOnClickProcess();                              //Alert Dialog for selecting the BLE device

		}
	};

    View.OnClickListener buttonStartListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	toothflag=true;
            buttonStart.setEnabled(false);
            buttonEnd.setEnabled(true);

            StartTime = SystemClock.uptimeMillis();
            handler.postDelayed(runnable, 0);
        }
    };

    View.OnClickListener buttonEndListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
			toothflag=false;

			buttonStart.setEnabled(true);
            buttonEnd.setEnabled(false);

            TimeBuff += MillisecondTime;
            handler.removeCallbacks(runnable);
        }
    };

    //양치 시간 계산하기 위한 메소드
    public Runnable runnable = new Runnable() {

        public void run() {

            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            Seconds = (int) (UpdateTime / 1000);

            Minutes = Seconds / 60;

            Seconds = Seconds % 60;

            MilliSeconds = (int) (UpdateTime % 1000);

            Minutes=Minutes%60;

            timeCount.setText("" + Minutes + ":"+String.format("%02d", Seconds));

            handler.postDelayed(this, 0);
        }

    };

}