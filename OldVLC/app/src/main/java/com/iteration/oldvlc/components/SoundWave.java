package com.iteration.oldvlc.components;


import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SoundWave extends SurfaceView implements SurfaceHolder.Callback{

	private SurfaceHolder holder;
	private SoundWaveThread  wavethread;
	private DataThread  datathread;
	private boolean hasSurface;
	
	final int showHeight=1200;  
	final int showWidth =1500;
	
	private ArrayList<short[]> inBuf = new ArrayList<short[]>(); 
	private ArrayList<short[]> audioBuf = new ArrayList<short[]>(); 
	
	private ArrayList<SoundBuf> audioBuf2 = new ArrayList<SoundBuf>(); 
	
    /** 
     * X����С�ı��� 
     */  
    public int rateX = 2;  
    /** 
     * Y����С�ı��� 
     */  
    public int rateY = 50;  
    /** 
     * Y����� 
     */  
    public int baseLine = showHeight/2;  
    
	
	public SoundWave(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initWave();
	}
	
	public SoundWave(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initWave();
	}
	
	public SoundWave(Context context, AttributeSet ats,int defaultStyle) {
	    super(context, ats, defaultStyle);
	    initWave();
	  }
	
	protected void initWave() {		
		// Create a new SurfaceHolder and assign this 
	    // class as its callback.
	    holder = getHolder();
	    holder.addCallback(this);
	    hasSurface = false;	
	    wavethread = new SoundWaveThread(); 
	    datathread = new DataThread(); 
	}
	
	public void resume() {
	    // Create and start the graphics update thread.
	    if (wavethread == null) {
	    	wavethread = new SoundWaveThread();

	      if (hasSurface == true)
	    	  wavethread.start();
	    }
	    if (datathread == null) {
	    	datathread = new DataThread();

	      if (hasSurface == true)
	    	  datathread.start();
	    }
	  }

	  public void pause() {
	    // Kill the graphics update thread
	    if (wavethread != null) {
	    	wavethread.requestExitAndWait();
	    	wavethread = null;
	    }
	    if (datathread != null) {
	    	datathread.requestExitAndWait();
	    	datathread = null;
	    }
	    
	  } 

	

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		hasSurface = true;
		
		if (datathread != null)
			datathread.start();
		
		if (wavethread != null)
			wavethread.start();
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// TODO Auto-generated method stub
		if (wavethread != null)
			wavethread.onWindowResize(w, h);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		pause();
	    hasSurface = false;
	}
	
	public class SoundBuf {
		
	    private short[] buffer;
	    private int bufsize; 
	    
	      
	    public SoundBuf(short[] _buffer, int _bufsize) {
			// TODO Auto-generated constructor stub
	    	super();
	    	this.buffer = _buffer;
	    	this.bufsize = _bufsize;
		}
		public int getBufsize() {  
	        return bufsize;  
	    }  
	    public void setBufsize(int _bufsize) {  
	        this.bufsize = _bufsize;  
	    }  
	    public short[] getBuf() {  
	        return buffer;  
	    }  
	    public void setBuf(short[] _buffer) {  
	        this.buffer = _buffer;  
	    }  
		
	}
	
	
//	public void sndAudioBuf(short[] _buffer) {
//		synchronized (audioBuf) {  
//			audioBuf.add(_buffer);// �������  
//        }
//	}
	public void sndAudioBuf(short[] _buffer,int bufsize) {
		SoundBuf sbuf = new SoundBuf(_buffer,bufsize);
		
//		synchronized (audioBuf) {  
//			audioBuf.add(_buffer);// �������  
//        }
		synchronized (audioBuf2) {  
			audioBuf2.add(sbuf);// �������  
        }
		
	}
	
	class DataThread extends Thread {
		private boolean done;

		DataThread() {
	      super();
	      done = false;
	    }

	    @SuppressWarnings("unchecked")
		@Override
	    public void run() {
	    	ArrayList<SoundBuf> buf = new ArrayList<SoundBuf>(); 
	    	
	    	while (!done) {
	    		try {
			    		synchronized (audioBuf2) {  
		                    if (audioBuf2.size() == 0)  
		                        continue;
		                    buf = (ArrayList<SoundBuf>) audioBuf2.clone();// ����  
		                 //   Log.v("buf", buf+"");
		                    audioBuf2.clear();// ���  
			    		}
			    		
			    		for(int k = 0; k < buf.size(); k++) { 
			    			short[] buffer = buf.get(k).getBuf();
			    			short[] tmpBuf = new short[buf.get(k).getBufsize() / rateX];  
		                    for (int i = 0, ii = 0; i < tmpBuf.length; i++, ii = i  
		                            * rateX) {  
		                        tmpBuf[i] = buffer[ii];  
		                    }  
		                    synchronized (inBuf) {//  
		                        inBuf.add(tmpBuf);// �������  
		                    }  
			    		}
			    		
			    		buf.clear();
	                    
	    		}catch(Exception e)  {}
	    	}
	    	
	    }
	    
	    public void requestExitAndWait() {
		      // Mark this thread as complete and combine into
		      // the main application thread.
		      done = true;
//		      try {
//		        join();
//		      } catch (InterruptedException ex) { }
		 }
	    
	}
	
//	class DataThread extends Thread {
//		private boolean done;
//
//		DataThread() {
//	      super();
//	      done = false;
//	    }
//
//	    @SuppressWarnings("unchecked")
//		@Override
//	    public void run() {
//	    	ArrayList<short[]> buf = new ArrayList<short[]>(); 
//	    	
//	    	while (!done) {
//	    		try {
//			    		synchronized (audioBuf) {  
//		                    if (audioBuf.size() == 0)  
//		                        continue;
//		                    buf = (ArrayList<short[]>) audioBuf.clone();// ����  
//		                 //   Log.v("buf", buf+"");
//		                    audioBuf.clear();// ���  
//			    		}
//			    		
//			    		for(int k = 0; k < buf.size(); k++) { 
//			    			short[] buffer = buf.get(k);
//			    			short[] tmpBuf = new short[buffer.length / rateX];  
//		                    for (int i = 0, ii = 0; i < tmpBuf.length; i++, ii = i  
//		                            * rateX) {  
//		                        tmpBuf[i] = (short) buffer[ii];  
//		                    }  
//		                    synchronized (inBuf) {//  
//		                        inBuf.add(tmpBuf);// �������  
//		                    }  
//			    		}
//			    		
//			    		buf.clear();
//	                    
//	    		}catch(Exception e)  {}
//	    	}
//	    	
//	    }
//	    
//	    public void requestExitAndWait() {
//		      // Mark this thread as complete and combine into
//		      // the main application thread.
//		      done = true;
////		      try {
////		        join();
////		      } catch (InterruptedException ex) { }
//		 }
//	    
//	}
    
	class SoundWaveThread extends Thread {
	    private boolean done;
	    SurfaceHolder surfaceHolder = holder;
	    Paint mPaint;  
	    
	    private int oldX = 0;// �ϴλ��Ƶ�X����  
        private int oldY = 0;// �ϴλ��Ƶ�Y����          
        private int X_index = 0;// ��ǰ��ͼ������ĻX�������  
        

	    SoundWaveThread() {
	      super();
	      done = false;
	    }

	    @SuppressWarnings("unchecked")
		@Override
	    public void run() {
	    	
	    	 mPaint = new Paint();    
	     //    mPaint.setColor(Color.BLUE);// ����Ϊ��ɫ    
	         mPaint.setStrokeWidth(2);// ���û��ʴ�ϸ   	    	    	
	    	
	    	while (!done) {
	    		try {
	    			 ArrayList<short[]> buf = new ArrayList<short[]>();  
	                 synchronized (inBuf) {  
	                     if (inBuf.size() == 0)  
	                         continue;  
	                     buf = (ArrayList<short[]>) inBuf.clone();// ����  
	                  //   Log.v("buf", buf+"");
	                     inBuf.clear();// ���  
	                 }  
	                 for (int i = 0; i < buf.size(); i++) {  
	                     short[] tmpBuf = buf.get(i);  
	                     SimpleDraw(X_index, tmpBuf, rateY, baseLine);// �ѻ��������ݻ�����  
	                     X_index = X_index + tmpBuf.length;  
	                     if (X_index > showWidth) {  
	                         X_index = 0;  
	                     }  
	                 }  
				} 
				catch(Exception e)  {}
	    		
	    	}
	    	
	    }
	    
	    /** 
	     * ����ָ������ 
	     *  
	     * @param start 
	     *            X�Ὺʼ��λ��(ȫ��) 
	     * @param buffer 
	     *            ������ 
	     * @param rate 
	     *            Y��������С�ı��� 
	     * @param baseLine 
	     *            Y����� 
	     */  
	    void SimpleDraw(int start, short[] buffer, int rate, int baseLine) {  
	        if (start == 0)  
	            oldX = 0; 
	        Canvas canvas = surfaceHolder.lockCanvas(  
	                new Rect(start, 0, start + buffer.length, showHeight));// �ؼ�:��ȡ����  
	        canvas.drawColor(Color.WHITE);// �������  
	        mPaint.setColor(Color.GREEN);
	        mPaint.setStrokeWidth(10);
	        canvas.drawLine(0, showHeight/2, showWidth, showHeight/2, mPaint);
	        mPaint.setStrokeWidth(1);
	        mPaint.setColor(Color.BLUE);
	        
	        int y;  
	        for (int i = 0; i < buffer.length; i++) {// �ж��ٻ�����  
	            int x = i + start;  
	            y = buffer[i] / rate + baseLine;// ������С���������ڻ�׼��  
	            canvas.drawLine(oldX, oldY, x, y, mPaint);  
	            oldX = x;  
	            oldY = y;  
	        }  
	        surfaceHolder.unlockCanvasAndPost(canvas);// �����������ύ���õ�ͼ��  
	        
	    }  
	    
	    
	    
	    public void requestExitAndWait() {
		      // Mark this thread as complete and combine into
		      // the main application thread.
		      done = true;
		      try {
		        join();
		      } catch (InterruptedException ex) { }
		    }

		    public void onWindowResize(int w, int h) {
		      // Deal with a change in the available surface size.
		    }
	    
	}   

	
}
