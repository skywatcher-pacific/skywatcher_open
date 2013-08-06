package com.skywatcher.PanoramaApp;

import java.util.List;

import com.skywatcher.extend.CroodAxis1Axis2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;

public class PreviewChartView extends SurfaceView implements Callback {
	private PreviewChartThread thread;

	private List<CroodAxis1Axis2> points; // 所有要拍攝的點
	private CroodAxis1Axis2 targetPoint; // 下一個要拍攝的點
	private CroodAxis1Axis2 camera; // 目前相機位置
	private int completeIndex = -1; // 目前已完成的位置

	public PreviewChartView(Context context) {
		super(context);
		initialize(context);
	}

	public PreviewChartView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context);
	}

	public PreviewChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
	}

	private void initialize(Context context) {
		// register our interest in hearing about changes to our surface
		SurfaceHolder surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setFixedSize(getWidth(), getHeight());

		// create thread only; it's started in surfaceCreated()
		thread = new PreviewChartThread(surfaceHolder, context);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// start the thread here so that we don't busy-wait in run()
		// waiting for the surface to be created
		thread.setRunning(true);
		thread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// we have to tell thread to shut down & wait for it to finish, or else
		// it might touch the Surface after we return and explode
		boolean retry = true;
		thread.setRunning(false);
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
	}

	public PreviewChartThread getThread() {
		return thread;
	}

	public void setShootPoints(List<CroodAxis1Axis2> points) {
		this.points = points;
	}

	public void setTargetPoint(CroodAxis1Axis2 targetPoint) {
		this.targetPoint = targetPoint;
	}

	public void setCamera(CroodAxis1Axis2 camera) {
		this.camera = camera;
	}

	public void setCompleteIndex(int completeIndex) {
		this.completeIndex = completeIndex;
	}

	class PreviewChartThread extends Thread {
		private SurfaceHolder surfaceHolder;
		private Context context;

		/** Indicate whether the surface has been created & is ready to draw */
		private boolean mRun = false;
		private boolean pauseFlag = false;

		private final static int PADDING = 10;
		private final static double X_AXIS_MAX = 180;
		private final static double X_AXIS_MIN = -180;
		private final static double Y_AXIS_MAX = 90;
		private final static double Y_AXIS_MIN = -50;

		private double xAxisScale;
		private double yAxisScale;

		public PreviewChartThread(SurfaceHolder surfaceHolder, Context context) {
			this.surfaceHolder = surfaceHolder;
			this.context = context;
		}

		// 計算原始座標與 preview chart上座標的比例
		private void calculateScale() {
			xAxisScale = (getWidth() - PADDING * 2) / (X_AXIS_MAX - X_AXIS_MIN);
			yAxisScale = (getHeight() - PADDING * 2)
					/ (Y_AXIS_MAX - Y_AXIS_MIN);
		}

		// 將 point 計算成在 preview chart 上的座標
		private PointF axisTranslate(double x, double y) {
			float x_trans = (float) ((x - X_AXIS_MIN) * xAxisScale + PADDING);
			float y_trans = (float) ((Y_AXIS_MAX - y) * yAxisScale + PADDING);
			return new PointF(x_trans, y_trans);
		}

		@Override
		public void run() {
			calculateScale();

			while (mRun) {
				Canvas c = null;
				try {
					c = surfaceHolder.lockCanvas(null);
					synchronized (surfaceHolder) {
						if (!pauseFlag)
							doDraw(c);
					}
				} finally {
					// do this in a finally so that if an exception is thrown
					// during the above, we don't leave the Surface in an
					// inconsistent state
					if (c != null) {
						surfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}
		}

		/**
		 * Used to signal the thread whether it should be running or not.
		 * Passing true allows the thread to run; passing false will shut it
		 * down if it's already running. Calling start() after this was most
		 * recently called with false will result in an immediate shutdown.
		 * 
		 * @param b
		 *            true to run, false to shut down
		 */
		public void setRunning(boolean b) {
			mRun = b;
		}

		public void pause(boolean b) {
			synchronized (surfaceHolder) {
				pauseFlag = b;
			}
		}

		private void doDraw(Canvas canvas) {
			canvas.drawColor(Color.WHITE); // 設底色
			drawPoints(canvas);
			drawTargetPoint(canvas);
			drawXAxis(canvas);
			drawCameraPosition(canvas);

			canvas.restore();
		}

		// 畫所有已完成(灰色) 和 未完成(淡黃色)的點
		private void drawPoints(Canvas canvas) {
			if (points == null)
				return;
			Paint paint = new Paint();
			paint.setAntiAlias(true);

			PointF point;
			for (int i = 0; i < points.size(); i++) {
				CroodAxis1Axis2 c = points.get(i);
				point = axisTranslate(c.Axis1.getDegree(), c.Axis2.getDegree());

				paint.setStyle(Paint.Style.FILL);
				if (i <= completeIndex)
					paint.setColor(Color.rgb(202, 202, 202));
				else
					paint.setColor(Color.rgb(253, 242, 202));
				canvas.drawCircle(point.x, point.y, 10, paint);

				paint.setStyle(Paint.Style.STROKE);
				paint.setColor(Color.BLACK);
				canvas.drawCircle(point.x, point.y, 10, paint);
			}
			canvas.save();
		}

		// 畫 X軸
		private void drawXAxis(Canvas canvas) {
			Paint paint = new Paint();
			paint.setColor(Color.BLUE);
			paint.setStrokeWidth(2);

			PointF startPoint = axisTranslate(X_AXIS_MIN, 0);
			PointF endPoint = axisTranslate(X_AXIS_MAX, 0);

			canvas.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y,
					paint);
		}

		// 畫下一個要拍攝的點 (紅色)
		private void drawTargetPoint(Canvas canvas) {
			if (targetPoint == null)
				return;
			PointF point = axisTranslate(targetPoint.Axis1.getDegree(),
					targetPoint.Axis2.getDegree());

			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(Color.RED);
			canvas.drawCircle(point.x, point.y, 10, paint);
			paint.setStyle(Paint.Style.STROKE);
			paint.setColor(Color.BLACK);
			canvas.drawCircle(point.x, point.y, 10, paint);

			canvas.save();
		}

		// 畫目前相機的位置 (十字)
		private void drawCameraPosition(Canvas canvas) {
			if (camera == null)
				return;
			PointF point = axisTranslate(camera.Axis1.getDegree(),
					camera.Axis2.getDegree());

			Paint paint = new Paint();
			paint.setColor(Color.BLACK);
			paint.setStrokeWidth(2);
			canvas.drawLine(point.x - 5, point.y, point.x + 5, point.y, paint);
			canvas.drawLine(point.x, point.y - 5, point.x, point.y + 5, paint);
			canvas.save();
		}
	}
}
