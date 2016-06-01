package com.example.downloadtest2;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener{
	
	private String TAG = "zhangjinqiang";
	private TextView myTxt;
	private ProgressBar mProgressBar;
	private Button mButton;
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			Bundle data = msg.getData();
			int downSize = data.getInt("size");
			Log.d(TAG,"downSize:"+downSize);
			mProgressBar.setProgress(downSize);
			float temp = (float)mProgressBar.getProgress()/(float)mProgressBar.getMax();
			int progress =(int)temp*100;
			Log.d(TAG,"progress:"+progress);
			if(progress ==100){
				Toast.makeText(getApplicationContext(), "下载完成", Toast.LENGTH_SHORT);
			}else{
				myTxt.setText("下载进度："+progress+"%");
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		myTxt = (TextView) findViewById(R.id.txt);
		mProgressBar = (ProgressBar) findViewById(R.id.progress);
		mButton =(Button)findViewById(R.id.button);
		mButton.setOnClickListener(this);
		Log.d(TAG,"onCreate");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		if(view.getId()==R.id.button){
			Log.d(TAG,"doDownLoad");
			doDownLoad();
		}
	}
	
	/**
	 * 下载准备工作
	 * 获取SD卡路径
	 * 开启线程
	 */
	private void doDownLoad(){
		//获取SD卡路径
		String path = Environment.getExternalStorageDirectory()+"/test/";
		Log.d(TAG,"path:"+path);
		
		File file = new File(path);
		//如果SD卡目录下不存在该目录则创建
		if(!file.exists()){
			Log.d(TAG,"file.mkdir");
			file.mkdir();
		}
		//初始化ProgressBar
		mProgressBar.setProgress(0);
		
		//简单起见，先把URL和文件名写死，其实这些都是可以通过HttpReader来获取到
		String downURL = "http://gdown.baidu.com/data/wisegame/91319a5a1dfae322/baidu_16785426.apk"; 
		String fileName = "baidu_16785426.apk";
		int threadNum = 5;
		String filePath = path+fileName;
		Log.d(TAG,"download file path: "+filePath);
		
		downLoadTask task = new downLoadTask(downURL, threadNum, filePath);
		task.start();
	}
	
	private class downLoadTask extends Thread{
		private String downloadURL;//下载链接地址
		private int threadNum;//开启的线程数
		private String filePath;//保存文件路径地址
		private int blockSize;//每个线程的下载量
		
		public downLoadTask(String downLoadURL,int threadNum,String filePath){
			this.downloadURL = downLoadURL;
			this.threadNum = threadNum;
			this.filePath = filePath;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			
			FileDownloadThread threads[] = new FileDownloadThread[threadNum];
			try{
				URL url = new URL(downloadURL);
				Log.d(TAG,"download file path:"+downloadURL);
				
				URLConnection conn = url.openConnection();
				//读取下载文件的总大小
				int fileSize = conn.getContentLength();
				if(fileSize < 0){
					Toast.makeText(getApplicationContext(), "读取文件失败", Toast.LENGTH_SHORT);
					return;
				}
				
				//设置ProgressBar的最大值为文件的size
				mProgressBar.setMax(fileSize);
				
				//计算每个线程对应的下载量
				blockSize = fileSize%threadNum==0 ? fileSize/threadNum : fileSize/threadNum+1;
				
				Log.d(TAG,"fileSize: "+fileSize +" blockSize: "+blockSize);
				
				File file  = new File(filePath);
				for(int i=0;i<threads.length;i++){
					//启动线程，分别下发每个线程需要下载的部分
					threads[i]= new FileDownloadThread(url, file, blockSize, (i+1));
					threads[i].setName("thread("+i+")");
					threads[i].start();
				}
				
				boolean isFinished = false;
				int downLoadAllSize = 0;
				while(!isFinished){
					isFinished = true;
					//当前所有线程下载总量
					downLoadAllSize= 0;
					for(int i=0;i<threads.length;i++){
						downLoadAllSize += threads[i].getDownLoadLength();
						if(!threads[i].isCompleted()){
							isFinished = false;
						}
					}
					
					//通过Handler去更新视图组件
					Message msg = new Message();
					Bundle bundle = new Bundle();
					bundle.putInt("size", downLoadAllSize);
					msg.setData(bundle);
					Log.d(TAG,"downLoadAllSize:"+downLoadAllSize);
					mHandler.sendMessage(msg);
					
					Thread.sleep(1000);
				}
			}catch (MalformedURLException e) {  
                e.printStackTrace();  
            } catch (IOException e) {  
                e.printStackTrace();  
            } catch (InterruptedException e) {  
                e.printStackTrace();  
            }  	
		}
	}
}
