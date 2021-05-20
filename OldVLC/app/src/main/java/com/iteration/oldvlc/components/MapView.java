package com.iteration.oldvlc.components;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.iteration.oldvlc.R;

// Matrix setXXX��������Ĭ�ϵ���reset()���о��������

public class MapView extends SurfaceView implements SurfaceHolder.Callback{
	
	private SurfaceHolder holder;
	private boolean hasSurface;

	
	private Bitmap mapBitmap = null;
	private Bitmap compassBitmap = null;
	
	Matrix compassMatrix = new Matrix();
	Matrix mapMatrix = new Matrix();
	
	private MapViewThread  mapviewthread;
	int reqCompassWidth = 80;
	int reqCompassHeight = 80;
//	float location[][] = new float[4][2];
	float location[][] = {{200,200},{1500,200},{200,2000},{1500,2000}};
	float currentlocation[] = {900,1530};
	float compassScaleR = 0;
	float mapScaleR = 0;
	private float bearing=0;
	
	int screenWidth = 1600;
	int screenHeight = 2237;
	
	DisplayMetrics dm;
	
	private static final int LAYER_FLAGS = Canvas.ALL_SAVE_FLAG;
//			Canvas.MATRIX_SAVE_FLAG
//            | Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
//            | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
//            | Canvas.CLIP_TO_LAYER_SAVE_FLAG;
	
	
	int touchMode = 0;//���ڱ��ģʽ 
	static final int NONE = 0;// ��ʼ״̬
    static final int DRAG = 1;// �϶�
    static final int ZOOM = 2;// ����
	PointF prev = new PointF();
    PointF mid = new PointF();
    float dist = 1f;
    float scale = 1;
	
    long currentVlcId = 0;
    
	public MapView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initMap();
	}
	public MapView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    initMap();
	  }

	public MapView(Context context, AttributeSet ats,int defaultStyle) {
	    super(context, ats, defaultStyle);
	    initMap();
	  }
	
	protected void initMap() {		
		// Create a new SurfaceHolder and assign this 
	    // class as its callback.
	    holder = getHolder();
	    holder.addCallback(this);
	    hasSurface = false;	
	    mapviewthread = new MapViewThread();          
	    
	}
	
	public void resume() {
	    // Create and start the graphics update thread.
	    if (mapviewthread == null) {
	    	mapviewthread = new MapViewThread();

	      if (hasSurface == true)
	    	  mapviewthread.start();
	    }
	  }

	  public void pause() {
	    // Kill the graphics update thread
	    if (mapviewthread != null) {
	    	mapviewthread.requestExitAndWait();
	    	mapviewthread = null;
	    }
	  } 

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		hasSurface = true;
		
//		dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);// ��ȡ�ֱ���
//		System.out.println("###width,higth = " + dm.widthPixels +"," + dm.heightPixels);
//		
		final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(getResources(), R.drawable.map,options);
		// ����ѹ������,��inSampleSize=4ʱ,ͼƬ��ѹ����ԭͼ��1/4
	    options.inSampleSize = calculateInSampleSize(options, screenWidth, screenHeight);

	    // ��inJustDecodeBounds��Ϊfalseʱ,BitmapFactory.decode...�ͻ᷵��ͼƬ������
	    options.inJustDecodeBounds = false;
	    // ���ü���ı���ֵ��ȡѹ�����ͼƬ����
	    mapBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map,options);
	    mapScaleR = Math.max(
                (float) screenWidth / (float) mapBitmap.getWidth(),
                (float) screenHeight / (float) mapBitmap.getHeight());
        if (mapScaleR < 1.0) {
        	mapMatrix.setScale(mapScaleR, mapScaleR);
        }	   
        
		if (mapviewthread != null)
			mapviewthread.start();
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// TODO Auto-generated method stub
		if (mapviewthread != null)
			mapviewthread.onWindowResize(w, h);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub		
	    pause();
	    hasSurface = false;
	}
	
	
	 public void setBearing(float _bearing) {
	    bearing = _bearing;
	  //  sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
	  }

	  public float getBearing() {
	    return bearing;
	  }
	  
	  public void sendVlcId(long _vlcid, boolean recording) {
		  long vlcid = _vlcid;
		//  System.out.println("###vlcid = " + vlcid);
		  if(vlcid!=0 && currentVlcId!=vlcid) {
			  
				if(compassBitmap == null) {
					compassBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.compass);
					//	System.out.println("###width,higth = " + compassBitmap.getWidth() +"," + compassBitmap.getHeight());	      
					compassScaleR = Math.min(
					        (float) reqCompassWidth / (float) compassBitmap.getWidth(),
					        (float) reqCompassHeight / (float) compassBitmap.getHeight());
					if (compassScaleR < 1.0) {
						compassMatrix.setScale(compassScaleR, compassScaleR);
					}
				}
		        
				if(vlcid == 1408){
					currentlocation[0] = location[0][0];
					currentlocation[1] = location[0][1];
				}
				else if(vlcid == 2410){
					currentlocation[0] = location[1][0];
					currentlocation[1] = location[1][1];
				}
				else if(vlcid == 3408){
					currentlocation[0] = location[2][0];
					currentlocation[1] = location[2][1];        	
				}
				else if(vlcid == 4410){
					currentlocation[0] = location[3][0];
					currentlocation[1] = location[3][1];
				}
				else if(vlcid == 0x123456){
					currentlocation[0] = 900;
					currentlocation[1] = 1500;
					mapMatrix.setScale(mapScaleR, mapScaleR);
				}
				else {
					
				}
		        
				currentVlcId=vlcid;
		  }
	  }
		
		/**
		* ����ѹ������ֵ
		* @param options       ����ͼƬ��������Ϣ
		* @param reqWidth            ����ͼƬѹ���ߴ���С���
		* @param reqHeight           ����ͼƬѹ���ߴ���С�߶�
		* @return
		*/
		public static int calculateInSampleSize(BitmapFactory.Options options,
		             int reqWidth, int reqHeight) {
		       // ����ͼƬԭ���ֵ
		       final int height = options.outHeight;
		       final int width = options.outWidth;
		       
		       System.out.println("###map size : " + width +"," + height );
		       // ��ʼ��ѹ������Ϊ1
		       int inSampleSize = 1;

		       // ��ͼƬ���ֵ�κ�һ����������ѹ��ͼƬ���ֵʱ,����ѭ������ϵͳ
		       if (height > reqHeight || width > reqWidth) {

		             final int halfHeight = height / 2;
		             final int halfWidth = width / 2;

		             // ѹ������ֵÿ��ѭ����������,
		             // ֱ��ԭͼ���ֵ��һ�����ѹ��ֵ��~����������ֵΪֹ
		             while ((halfHeight / inSampleSize) >= reqHeight
		                        && (halfWidth / inSampleSize) >= reqWidth) {
		                  inSampleSize *= 2;
		            }
		      }

		       return inSampleSize;
		}
	
	class MapViewThread extends Thread {
	    private boolean done;

	    MapViewThread() {
	      super();
	      done = false;
	    }

	    @Override
	    public void run() {
	      SurfaceHolder surfaceHolder = holder;

	      // Repeat the drawing loop until the thread is stopped.
			while (!done) {
				try {
					// Lock the surface and return the canvas to draw onto.
					Canvas canvas = surfaceHolder.lockCanvas();
			        // TODO: Draw on the canvas!
			         canvas.drawColor(Color.BLACK);
			         canvas.drawBitmap(mapBitmap, mapMatrix, null); 	         
			        // Unlock the canvas and render the current image.
			         
			         if(compassBitmap != null) {
				         canvas.saveLayerAlpha(0,0,screenWidth,screenHeight,0xff,LAYER_FLAGS);
				         compassMatrix.setScale(compassScaleR, compassScaleR);		         
				         compassMatrix.postRotate(-1*(bearing),reqCompassWidth/2,reqCompassHeight/2);
				         compassMatrix.postTranslate(currentlocation[0], currentlocation[1]);
				         canvas.drawBitmap(compassBitmap, compassMatrix, null);
					     canvas.restore();
			         }
						
			        surfaceHolder.unlockCanvasAndPost(canvas);
				} 
				catch(Exception e)  {}
			}
			
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
	
	
		
		/**
	     * ����ľ���
	     */
	    private float spacing(MotionEvent event) {
	        float x = event.getX(0) - event.getX(1);
	        float y = event.getY(0) - event.getY(1);
//	        return FloatMath.sqrt(x * x + y * y);
			return (float) Math.sqrt(x*x + y*y);
	    }
	
	    /**
	     * ������е�
	     */
	    private void midPoint(PointF point, MotionEvent event) {
	        float x = event.getX(0) + event.getX(1);
	        float y = event.getY(0) + event.getY(1);
	        point.set(x / 2, y / 2);
	    }
    
	 @SuppressLint("ClickableViewAccessibility") 
	 @Override
		public boolean onTouchEvent(MotionEvent event){
		
		 switch (event.getAction() & MotionEvent.ACTION_MASK) {
		 	
		 	case MotionEvent.ACTION_DOWN: {
	    		prev.set(event.getX(), event.getY());
	    		touchMode = DRAG; 
	    		System.out.println("###ACTION_DOWN : " +event.getX() + "," + event.getY());
	    	//	return super.onTouchEvent(event);
	    		return true;
	    	}
		 	case MotionEvent.ACTION_MOVE: {
		 		if (touchMode == DRAG) {//ͼƬ�϶��¼�
		    		//System.out.println("###MOVE ");		 			
		 			float deltaX = 0, deltaY = 0;					
					
					RectF rect = new RectF(0, 0, mapBitmap.getWidth(), mapBitmap.getHeight());
					mapMatrix.mapRect(rect);
				//	System.out.println("###���ϣ�����:"+"("+rect.left+","+rect.top+")"+";;;"+"("+rect.right+","+rect.bottom+")");
					if((rect.left+event.getX()-prev.x) >0 || (rect.right+event.getX()-prev.x) < screenWidth){
						deltaX = 0;
					}
					else {
						deltaX = event.getX()- prev.x;
					}
					if((rect.top+event.getY()-prev.y) >0 || (rect.bottom+event.getY()-prev.y) < screenHeight){
						deltaY = 0;
					}
					else {
						deltaY = event.getY()- prev.y;;
					}
					currentlocation[0] += deltaX;
					currentlocation[1] += deltaY;
					mapMatrix.postTranslate(deltaX, deltaY);
					prev.set(event.getX(), event.getY());
					
		 		}
		 		else if(touchMode == ZOOM) {//ͼƬ�Ŵ��¼� 
	    			//System.out.println("###POINTER DOWN ");
		 			float newDist = spacing(event);	    			
	    			float tScale = 0;
	    			RectF rect = new RectF(0, 0, mapBitmap.getWidth(), mapBitmap.getHeight());
					Matrix m = new Matrix();
					m.set(mapMatrix);
					m.postScale(newDist/dist, newDist/dist,mid.x,mid.y);
					m.mapRect(rect);
					if((rect.left>0) || (rect.right<screenWidth) || (rect.top>0) || (rect.bottom<screenHeight)) {
				//	if(((rect.right-rect.left)<screenWidth) ||  ((rect.bottom-rect.top)<screenHeight)) {
						tScale = 1;
					//	mapMatrix.setScale(mapScaleR, mapScaleR);
					//	Toast.makeText(this.getContext(), "����������С����",Toast.LENGTH_SHORT).show();
					//	Toast.makeText(this.getContext(), "����������С����",0).show();
					}
					else {
						tScale = newDist / dist;
					}
					
					mapMatrix.postScale(tScale, tScale,mid.x,mid.y);
	    			currentlocation[0] = currentlocation[0]*tScale-(tScale-1)*mid.x;
	    			currentlocation[1] = currentlocation[1]*tScale-(tScale-1)*mid.y;
	    			dist = newDist;	
	    			
		 		}
		 		return true;
		 	}
		 	case MotionEvent.ACTION_POINTER_DOWN: {
	    		midPoint(mid, event);
	    		dist = spacing(event);
	    		touchMode = ZOOM;
	    	//	System.out.println("###ACTION_POINTER_DOWN : " + dist);
	    	//	return super.onTouchEvent(event);
	    		return true;
	    	}
	    	default : {
	    		touchMode = 0;
	    		//return super.onTouchEvent(event);
	    		return true;
	    	}
		 
		 }
		 
		// return false;		 
	 }
	

}
