package com.example.yegilee.ai_realtime;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MainActivity  extends BlunoLibrary {
	private Button buttonScan;
	private TextView textState;
	private TextView textSerialReceived;

	boolean flagSave=false;

	private static final int N_SAMPLES = 50;
	private static List<Float> ax;
	private static List<Float> ay;
	private static List<Float> az;
	private static List<Float> gx;
	private static List<Float> gy;
	private static List<Float> gz;

	private float[] results;
	private TensorFlowClassifier classifier;

	private String[] labels = {"1", "2", "3", "4", "5", "6","7","8", "9", "10", "11", "12", "13","14", "15","16"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		onCreateProcess();                                          //onCreate Process by BlunoLibrary

		serialBegin(115200);                                       //set the Uart Baudrate on BLE chip to 115200

		buttonScan=(Button)findViewById(R.id.buttonScan);
		//textAdress=(TextView)findViewById(R.id.textAddress);
		textState=(TextView)findViewById(R.id.textState);
		//editLableNum=(EditText)findViewById(R.id.editLableNum);
		//buttonStart=(Button)findViewById(R.id.buttonStart);
		//buttonEnd=(Button)findViewById(R.id.buttonEnd);
		textSerialReceived=(TextView)findViewById(R.id.textSerialReceived);

		buttonScan.setOnClickListener(buttonScanListener);
		//buttonStart.setOnClickListener(buttonStartListener);
		//buttonEnd.setOnClickListener(buttonEndListener);

		ax = new ArrayList<>();
		ay = new ArrayList<>();
		az = new ArrayList<>();
		gx = new ArrayList<>();
		gy = new ArrayList<>();
		gz = new ArrayList<>();

		classifier = new TensorFlowClassifier(getApplicationContext());




	}

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

	Queue<String> queueSave=new LinkedList<String>();
	StringBuilder  bufferStr=new StringBuilder ();
	int index1=0;
	int index2=0;
	String drop;
	StringBuilder tmp;
	String tmp_str;
	String save[];

	@Override
	public void onSerialReceived(String theString) {                     //Once connection data received, this function will be called
		// TODO Auto-generated method stub
		//textSerialReceived.append(theString);                     //append the text into the EditText
		bufferStr.append(theString);

		//Log.e("datalog", String.valueOf(bufferStr));
		//Log.e("datalog length", String.valueOf(bufferStr.length()));
		if(bufferStr.length()>20 && bufferStr.lastIndexOf("\n")>1) {
			//Thread threadOne = new Thread1();
			//threadOne.start();
			index2 = bufferStr.lastIndexOf("\n");

			if(bufferStr.lastIndexOf("\n")<45) {

				//Log.e("index length", String.valueOf(index1) + " " + String.valueOf(index2));

				/*
					String save= bufferStr.substring(index1, index2);
					tmp  = bufferStr.delete(0, index2);
					textSerialReceived.append(save + "\n");
				*/
				save=bufferStr.substring(index1, index2).split(" ");
				//addAllData(save);
				if(save.length>=6) {
					for (int i = 0; i < 6; i++) {
						try {
							Float.parseFloat(save[i]);

					}catch (NumberFormatException e) {
						save[i]="0";
							textSerialReceived.append("data -> 0");
						}
					}
					ax.add(Float.valueOf(save[0]));
					ay.add(Float.valueOf(save[1]));
					az.add(Float.valueOf(save[2]));
					gx.add(Float.valueOf(save[3]));
					gy.add(Float.valueOf(save[4]));
					gz.add(Float.valueOf(save[5]));
					activityPrediction();
				}
			}else{
				drop=bufferStr.substring(index1, index2);
				tmp  = bufferStr.delete(0, index2);
			}


			index1 = 1;
		}
		//The Serial data from the BLUNO may be sub-packaged, so using a buffer to hold the String is a good choice.
		((ScrollView)textSerialReceived.getParent()).fullScroll(View.FOCUS_DOWN);
	}

	private void addAllData(String[] save){

	}


	private void activityPrediction() {
		if (ax.size() == N_SAMPLES && ay.size() == N_SAMPLES && az.size() == N_SAMPLES) {

			Log.e("method", "method");
			textSerialReceived.append("method \n");

			List<Float> data = new ArrayList<>();
			data.addAll(ax);
			data.addAll(ay);
			data.addAll(az);

			data.addAll(gx);
			data.addAll(gy);
			data.addAll(gz);

			results = classifier.predictProbabilities(toFloatArray(data));
			textSerialReceived.append("results" + results + "\n");
			for(int i=0;i<results.length;i++)
			{
				textSerialReceived.append(results[i]+" ");
			}
			textSerialReceived.append("\n");

			ax.clear();
			ay.clear();
			az.clear();
			gx.clear();
			gy.clear();
			gz.clear();
		}
	}

	private float[] toFloatArray(List<Float> list) {
		int i = 0;
		float[] array = new float[list.size()];

		for (Float f : list) {
			array[i++] = (f != null ? f : Float.NaN);
		}
		return array;
	}

		//===================================================================

	View.OnClickListener buttonScanListener=new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			buttonScanOnClickProcess();                              //Alert Dialog for selecting the BLE device

		}
	};
	/*
	View.OnClickListener buttonStartListener=new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			flagSave=true;
			//Log.e("button status", "start");
			buttonStart.setEnabled(false);
			buttonEnd.setEnabled(true);

			labelValue= String.valueOf(editLableNum.getText());
			textSerialReceived.append(labelValue+"번 수집시작"+"\n");

			queueStr.clear();
			//bufferStr.delete(0,bufferStr.length());

		}
	};

	View.OnClickListener buttonEndListener=new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			flagSave=false;
			//Log.e("button status", "end");
			buttonStart.setEnabled(true);
			buttonEnd.setEnabled(false);

			textSerialReceived.append(labelValue+"번 수집종료"+"\n");
			editLableNum.setText("");

			saveFile();
		}
	};
*/
	/*
	String buf;
	String[] bufSize;
	public void saveFile() {
		try {
			textSerialReceived.append(labelValue+"번 클래스 저장중\n");

			Log.e("saveFile()","파일을 생성하여 저장합니다.\n");
			File dir = new File (foldername);

			if(!dir.exists()){
				dir.mkdir();
			}

			fos = new FileOutputStream(foldername+"/"+filename, true);
			fos2 = new FileOutputStream(foldername+"/"+filename2, true);

			fw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
			fw2 = new BufferedWriter(new OutputStreamWriter(fos2,"UTF-8"));

			int idx=0;
			//globalIdx2+=idx;
			for(int i=0;i<queueSave.size()-1;){
				buf=String.valueOf(queueSave.poll());
				if(buf.split(" ").length==6) {

					fw.write(String.valueOf(idx++));
					fw.write(" ");

					//fw.write(String.valueOf(queueSave.poll()));       fw.write(" ");
					//fw.write(String.valueOf(queueSave.poll()));        fw.write(" ");
					//fw.write(String.valueOf(queueSave.poll()));          fw.write(" ");
					//fw.write(String.valueOf(queueSave.poll()));        fw.write(" ");
					//fw.write(String.valueOf(queueSave.poll()));        fw.write(" ");
					fw.write(buf);
					fw.write("\r\n");

				}
			}
			Log.e("queueSave 남은 수",String.valueOf(queueSave.size()));
			queueSave.clear();
			Log.e("queueSave 남은 수",String.valueOf(queueSave.size()));

			fw2.write(labelValue);       fw2.write(" ");
			fw2.write(String.valueOf(globalIdx));       fw2.write(" ");
			globalIdx+=idx;
			fw2.write(String.valueOf(globalIdx-1));       fw2.write("\r\n");
			Log.e("saveFile()","저장완료");
			textSerialReceived.append(labelValue+"번 클래스 저장완료\n");

			fw.close();
			//fw.flush();
			fw2.close();
			//fw2.flush();
		}catch (Exception e) {
			e.printStackTrace() ;
		}
	}
*/
}