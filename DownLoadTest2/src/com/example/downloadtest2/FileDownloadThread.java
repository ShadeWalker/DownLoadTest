package com.example.downloadtest2;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

import android.util.Log;

public class FileDownloadThread extends Thread{
	private String TAG = "zhangjinqiang";
	
	/**
	 * 当前下载是否完成
	 */
	private boolean isCompleted = false;
	
	/**当前下载文件长度 **/
	private int downloadLength = 0;
	
	/**
	 * 文件保存路径
	 */
	private File file;
	
	/**
	 * 文件下载路径
	 */
	private URL downloadURL;
	
	/**
	 * 当前下载线程ID
	 */
	private int threadID;
	
	/**
	 * 线程下载数据长度
	 */
	private int blockSize;
	
	/**
	 * @param url:文件下载地址
	 * @param file:文件保存路径
	 * @param blockSize:下载数据长度
	 * @param threadID:线程ID
	 */
	public FileDownloadThread(URL downloadURL,File file,int blockSize,int threadID){
		this.file = file;
		this.downloadURL = downloadURL;
		this.blockSize = blockSize;
		this.threadID = threadID;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		
		BufferedInputStream bis = null;
		RandomAccessFile raf = null;
		
		try {
			URLConnection conn = downloadURL.openConnection();
			conn.setAllowUserInteraction(true);
			int startPos = blockSize*(threadID-1);//开始位置
			int endPos = blockSize*threadID-1;//结束位置
			Log.d(TAG,"startPos:"+startPos);
			Log.d(TAG,"endPos:"+endPos);
			//设置当前线程下载的起点和终点
			conn.setRequestProperty("Range", "bytes="+startPos+"-"+endPos);
			
			System.out.println(Thread.currentThread().getName()+" bytes= "+startPos+"-"+endPos);
			
			byte[] buffer = new byte[1024];
			bis = new BufferedInputStream(conn.getInputStream());
			
			raf = new RandomAccessFile(file, "rwd");
			raf.seek(startPos);
			
			int len=-1;
			while((len=bis.read(buffer, 0, 1024))!=-1){
				raf.write(buffer, 0, len);
				downloadLength += len;
			}
			
			isCompleted = true;
			Log.d(TAG,"current thread task has finished,all size:"+downloadLength);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d(TAG,"IOException1");
		}finally{
			if(bis!=null){
				try {
					bis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.d(TAG,"IOException2");
				}
			}
			
			if(raf!=null){
				try {
					raf.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.d(TAG,"IOException3");
				}
			}
		}
		
	}
	
	public boolean isCompleted(){
		return isCompleted;
	}
	
	public int getDownLoadLength(){
		return downloadLength;
	}
	
	
	
}
