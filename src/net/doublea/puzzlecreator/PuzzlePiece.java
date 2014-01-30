package net.doublea.puzzlecreator;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

public class PuzzlePiece implements Serializable
{
	/**
	 * 
	 */
	private static transient final long serialVersionUID = 4879194898296514087L;
	private static transient final int NO_COLOR = Color.TRANSPARENT;
	private static transient final int SNAP_TO_RANGE = 20;
	private static transient Random rand;
	
	private int ID;
	private transient PuzzlePiece upPiece, downPiece, leftPiece, rightPiece;
	private int upPieceID, downPieceID, leftPieceID, rightPieceID;
	private transient boolean upAttached, downAttached, leftAttached, rightAttached;
	private float x, y;
	private int width, height;
	private int tabWidth, tabHeight;
	
	private transient Bitmap image;
	private String puzzleName;
	private String imageFilename;
	
	private float zoom;
	private int xPan, yPan;
	private transient Paint paint;
	private transient Matrix scaleMatrix, translateMatrix, matrix;
	
	private transient PuzzlePiecePeg upPeg, downPeg, leftPeg, rightPeg;
	
	public PuzzlePiece() { }
	
	// Create puzzle piece
	public PuzzlePiece(String puzzleName, PuzzlePiece upPiece, PuzzlePiece downPiece, 
			PuzzlePiece leftPiece, PuzzlePiece rightPiece, int id, Context context)
	{
		this.puzzleName = puzzleName;
		ID = id;
		leftAttached = rightAttached = upAttached = downAttached = false;

		if (rand == null)
			rand = new Random();
		if (paint == null)
		{
			paint = new Paint();
			paint.setAntiAlias(true);
			paint.setFilterBitmap(true);
			paint.setDither(true);
			paint.setColor(Color.WHITE);
			paint.setStrokeWidth(0);
		}
		setUpPiece(upPiece);
		setDownPiece(downPiece);
		setLeftPiece(leftPiece);
		setRightPiece(rightPiece);
		
		scaleMatrix = new Matrix();
		translateMatrix = new Matrix();
		matrix = new Matrix();
	}
	
	private void setUpPiece(PuzzlePiece upPiece)
	{
		if (upPiece == null)
		{
			upPeg = new PuzzlePiecePeg(rand);
		}
		else
		{
			this.upPiece = upPiece;
			this.upPieceID = upPiece.ID;
			upPiece.downPiece = this;
			upPiece.downPieceID = ID;
			if (upPiece.downPeg != null)
				upPeg = upPiece.downPeg.getInverse();
		}
	}
	
	private void setDownPiece(PuzzlePiece downPiece)
	{
		if (downPiece == null)
		{
			downPeg = new PuzzlePiecePeg(rand);
		}
		else
		{
			this.downPiece = downPiece;
			this.downPieceID = downPiece.ID;
			downPiece.upPiece = this;
			downPiece.upPieceID = ID;
			if (downPiece.upPeg != null)
				downPeg = downPiece.upPeg.getInverse();
		}
	}
	
	private void setLeftPiece(PuzzlePiece leftPiece)
	{
		if (leftPiece == null)
		{
			leftPeg = new PuzzlePiecePeg(rand);
		}
		else
		{
			this.leftPiece = leftPiece;
			this.leftPieceID = leftPiece.ID;
			leftPiece.rightPiece = this;
			leftPiece.rightPieceID = ID;
			if (leftPiece.rightPeg != null)
				leftPeg = leftPiece.rightPeg.getInverse();
		}
	}
	
	private void setRightPiece(PuzzlePiece rightPiece)
	{
		if (rightPiece == null)
		{
			rightPeg = new PuzzlePiecePeg(rand);
		}
		else
		{
			this.rightPiece = rightPiece;
			this.rightPieceID = rightPiece.ID;
			rightPiece.leftPiece = this;
			rightPiece.leftPieceID = ID;
			if (rightPiece.leftPeg != null)
				rightPeg = rightPiece.leftPeg.getInverse();
		}
	}
	
	// Paint image
	public void paintPiece(Bitmap fullImage, int xPuzzlePiece, int yPuzzlePiece, int width, int height)
	{
		calcDimensions(width, height);
		width = this.width;
		height = this.height;
		
		int[] centerPixels = new int[width * height];
		int[] upperPixels = new int[width * tabHeight];
		int[] lowerPixels = new int[width * tabHeight];
		int[] leftPixels = new int[tabWidth * height];
		int[] rightPixels = new int[tabWidth * height];
		
		// Fill center pixels
		fullImage.getPixels(centerPixels,//save array
				0,						//offset
				width,					//stride
				xPuzzlePiece * width,	//x
				yPuzzlePiece * height,	//y
				width,					//width
				height);				//height
		// Fill upper pixels
		try
		{
			fullImage.getPixels(upperPixels,			//save array
					0,									//offset
					width,								//stride
					xPuzzlePiece * width,				//x
					yPuzzlePiece * height - tabHeight,	//y
					width,								//width
					tabHeight);						//height
		}
		catch (Exception e)
		{ 
			upperPixels = null;
		};
		// Fill lower pixels
		try
		{
			fullImage.getPixels(lowerPixels,			//save array
					0,									//offset
					width,								//stride
					xPuzzlePiece * width,				//x
					yPuzzlePiece * height + height,		//y
					width,								//width
					tabHeight);						//height
		}
		catch (Exception e)
		{ 
			lowerPixels = null;
		};
		// Fill left pixels
		try
		{
			fullImage.getPixels(leftPixels,				//save array
					0,									//offset
					tabWidth,						//stride
					xPuzzlePiece * width - tabWidth,//x
					yPuzzlePiece * height,				//y
					tabWidth,						//width
					height);							//height
		}
		catch (Exception e)
		{ 
			leftPixels = null;
		};
		// Fill right pixels
		try
		{
			fullImage.getPixels(rightPixels,			//save array
					0,									//offset
					tabWidth,						//stride
					xPuzzlePiece * width + width,		//x
					yPuzzlePiece * height,				//y
					tabWidth,						//width
					height);							//height
		}
		catch (Exception e)
		{ 
			rightPixels = null;
		};
		
		int[] allPixels = combinePixels(centerPixels, upperPixels, lowerPixels, leftPixels, rightPixels);
		image = cutOutPiece(makeImageFromColorArray(allPixels), getPieceShape());		
	}
	
	public void saveImage()
	{
		if (imageFilename == null)
		{
			FileOutputStream fos = null;
			
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			image.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] byteArray = stream.toByteArray();
			try { stream.close(); } 
			catch (IOException e) { e.printStackTrace(); }
			
			imageFilename = "Piece" + ID + ".PNG";
			try 
			{
				fos = PuzzleMat.getActiveContext().openFileOutput(
						puzzleName + "__" + imageFilename, Context.MODE_PRIVATE);
				fos.write(byteArray);
			}
			catch (FileNotFoundException e) { e.printStackTrace(); }
			catch (IOException e) { e.printStackTrace(); }
			
			try { fos.close(); } 
			catch (IOException e) { e.printStackTrace(); }
		}
	}
	
	private void loadImage()
	{
		FileInputStream fis = null;
		
		try 
		{
			fis = PuzzleMat.getActiveContext().openFileInput(puzzleName + "__" + imageFilename);	
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		
		image = BitmapFactory.decodeStream(fis);
		
		try { fis.close(); } 
		catch (IOException e) { e.printStackTrace(); }
		
		
	}
	
	private Bitmap makeImageFromColorArray(int[] pixels)
	{
		Bitmap graphic = Bitmap.createBitmap(width + tabWidth * 2, height + tabWidth * 2, Bitmap.Config.ARGB_8888);
		graphic.setPixels(pixels, 
				0, 					//offset
				width + tabWidth * 2, 		//stride
				0, 					//x
				0, 					//y
				width + tabWidth * 2, 		//width
				height + tabWidth * 2); 	//height
		return graphic;
	}
	
	private Bitmap getPieceShape()
	{	
		Bitmap pieceShape = Bitmap.createBitmap(width + tabWidth * 2, height + tabWidth * 2, Bitmap.Config.ARGB_8888);
		pieceShape.eraseColor(Color.BLACK);
		Canvas canvas = new Canvas(pieceShape);
		Paint transferPaint = new Paint();
		transferPaint.setFilterBitmap(false);
		transferPaint.setStyle(Style.FILL);
		transferPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
		canvas.drawPath(getCurvePath(), transferPaint);
		transferPaint.setXfermode(null);
		
		return pieceShape;
	}
	
	private Path getCurvePath()
	{
		float[][] controlPoints = getControlPoints();
		
		Path path = new Path();
		path.setLastPoint(controlPoints[0][0], controlPoints[0][1]);
		for (int i = 1; i < controlPoints.length; i += 3)
		{
			// try arcTo
			path.cubicTo(controlPoints[i][0], controlPoints[i][1],//first control point 
					controlPoints[i+1][0], controlPoints[i+1][1],//	second control point
					controlPoints[i+2][0], controlPoints[i+2][1]); // End point
		}
		path.close();

		return path;
	}
	
	private Path getBlockPath()
	{
		float[][] controlPoints = getControlPoints();
		
		Path path = new Path();
		path.setLastPoint(controlPoints[0][0], controlPoints[0][1]);
		for (int i = 1; i < controlPoints.length; i++)
			path.lineTo(controlPoints[i][0], controlPoints[i][1]);
		path.close();

		return path;
	}
	
	private float[][] getControlPoints()
	{
		float[][] controlPoints = new float[37][2];
		
		// Static upper-left
		controlPoints[0][0] = tabWidth;
		controlPoints[0][1] = tabHeight;
		// Top side
		
		controlPoints[1][0] = tabWidth + (width - upPeg.getPegNeckWidth(width)) / 2;
		controlPoints[1][1] = tabHeight;
		controlPoints[2][0] = tabWidth + (width - upPeg.getPegNeckWidth(width)) / 2;
		controlPoints[2][1] = tabHeight - upPeg.getPegNeckHeight(tabHeight) * upPeg.getPegDirection();
		controlPoints[3][0] = tabWidth + (width - upPeg.getPegHeadWidth(width)) / 2;
		controlPoints[3][1] = tabHeight - upPeg.getPegNeckHeight(tabHeight) * upPeg.getPegDirection();
		controlPoints[4][0] = tabWidth + (width - upPeg.getPegHeadWidth(width)) / 2;
		controlPoints[4][1] = tabHeight - (upPeg.getPegNeckHeight(tabHeight) 
						+ upPeg.getPegHeadHeight(tabHeight)) * upPeg.getPegDirection();
		controlPoints[5][0] = tabWidth + (width + upPeg.getPegHeadWidth(width)) / 2;
		controlPoints[5][1] = tabHeight - (upPeg.getPegNeckHeight(tabHeight) 
						+ upPeg.getPegHeadHeight(tabHeight)) * upPeg.getPegDirection();
		controlPoints[6][0] = tabWidth + (width + upPeg.getPegHeadWidth(width)) / 2;
		controlPoints[6][1] = tabHeight - upPeg.getPegNeckHeight(tabHeight) * upPeg.getPegDirection();
		controlPoints[7][0] = tabWidth + (width + upPeg.getPegNeckWidth(width)) / 2;
		controlPoints[7][1] = tabHeight - upPeg.getPegNeckHeight(tabHeight) * upPeg.getPegDirection();
		controlPoints[8][0] = tabWidth + (width + upPeg.getPegNeckWidth(width)) / 2;
		controlPoints[8][1] = tabHeight;
		// Static upper-right
		controlPoints[9][0] = tabWidth + width;
		controlPoints[9][1] = tabHeight;
		// Right side
		controlPoints[10][0] = tabWidth + width;
		controlPoints[10][1] = tabHeight + (height - rightPeg.getPegNeckWidth(height)) / 2;
		controlPoints[11][0] = tabWidth + width + rightPeg.getPegNeckHeight(tabWidth) * rightPeg.getPegDirection();
		controlPoints[11][1] = tabHeight + (height - rightPeg.getPegNeckWidth(height)) / 2;
		controlPoints[12][0] = tabWidth + width + rightPeg.getPegNeckHeight(tabWidth) * rightPeg.getPegDirection();
		controlPoints[12][1] = tabHeight + (height - rightPeg.getPegHeadWidth(height)) / 2;
		controlPoints[13][0] = tabWidth + width + (rightPeg.getPegNeckHeight(tabWidth) 
						+ rightPeg.getPegHeadHeight(tabWidth)) * rightPeg.getPegDirection();
		controlPoints[13][1] = tabHeight + (height - rightPeg.getPegHeadWidth(height)) / 2;
		controlPoints[14][0] = tabWidth + width + (rightPeg.getPegNeckHeight(tabWidth) 
						+ rightPeg.getPegHeadHeight(tabWidth)) * rightPeg.getPegDirection();
		controlPoints[14][1] = tabHeight + (height + rightPeg.getPegHeadWidth(height)) / 2;
		controlPoints[15][0] = tabWidth + width + rightPeg.getPegNeckHeight(tabWidth) * rightPeg.getPegDirection();
		controlPoints[15][1] = tabHeight + (height + rightPeg.getPegHeadWidth(height)) / 2;
		controlPoints[16][0] = tabWidth + width + rightPeg.getPegNeckHeight(tabWidth) * rightPeg.getPegDirection();
		controlPoints[16][1] = tabHeight + (height + rightPeg.getPegNeckWidth(height)) / 2;
		controlPoints[17][0] = tabWidth + width;
		controlPoints[17][1] = tabHeight + (height + rightPeg.getPegNeckWidth(height)) / 2;
		// Static lower-right
		controlPoints[18][0] = tabWidth + width;
		controlPoints[18][1] = tabHeight + height;
		// Bottom side
		controlPoints[19][0] = tabWidth + (width + downPeg.getPegNeckWidth(width)) / 2;
		controlPoints[19][1] = tabHeight + height;
		controlPoints[20][0] = tabWidth + (width + downPeg.getPegNeckWidth(width)) / 2;
		controlPoints[20][1] = tabHeight + height + downPeg.getPegNeckHeight(tabHeight) * downPeg.getPegDirection();
		controlPoints[21][0] = tabWidth + (width + downPeg.getPegHeadWidth(width)) / 2;
		controlPoints[21][1] = tabHeight + height + downPeg.getPegNeckHeight(tabHeight) * downPeg.getPegDirection();
		controlPoints[22][0] = tabWidth + (width + downPeg.getPegHeadWidth(width)) / 2;
		controlPoints[22][1] = tabHeight + height + (downPeg.getPegNeckHeight(tabHeight) 
						+ downPeg.getPegHeadHeight(tabHeight)) * downPeg.getPegDirection();
		controlPoints[23][0] = tabWidth + (width - downPeg.getPegHeadWidth(width)) / 2;
		controlPoints[23][1] = tabHeight + height + (downPeg.getPegNeckHeight(tabHeight) 
						+ downPeg.getPegHeadHeight(tabHeight)) * downPeg.getPegDirection();
		controlPoints[24][0] = tabWidth + (width - downPeg.getPegHeadWidth(width)) / 2;
		controlPoints[24][1] = tabHeight + height + downPeg.getPegNeckHeight(tabHeight) * downPeg.getPegDirection();
		controlPoints[25][0] = tabWidth + (width - downPeg.getPegNeckWidth(width)) / 2;
		controlPoints[25][1] = tabHeight + height + downPeg.getPegNeckHeight(tabHeight) * downPeg.getPegDirection();
		controlPoints[26][0] = tabWidth + (width - downPeg.getPegNeckWidth(width)) / 2;
		controlPoints[26][1] = tabHeight + height;
		// Static lower-left
		controlPoints[27][0] = tabWidth;
		controlPoints[27][1] = tabHeight + height;
		// Left side
		controlPoints[28][0] = tabWidth;
		controlPoints[28][1] = tabHeight + (height + leftPeg.getPegNeckWidth(height)) / 2;
		controlPoints[29][0] = tabWidth - leftPeg.getPegNeckHeight(tabWidth) * leftPeg.getPegDirection();
		controlPoints[29][1] = tabHeight + (height + leftPeg.getPegNeckWidth(height)) / 2;
		controlPoints[30][0] = tabWidth - leftPeg.getPegNeckHeight(tabWidth) * leftPeg.getPegDirection();
		controlPoints[30][1] = tabHeight + (height + leftPeg.getPegHeadWidth(height)) / 2;
		controlPoints[31][0] = tabWidth - (leftPeg.getPegNeckHeight(tabWidth) 
						+ leftPeg.getPegHeadHeight(tabWidth)) * leftPeg.getPegDirection();
		controlPoints[31][1] = tabHeight + (height + leftPeg.getPegHeadWidth(height)) / 2;
		controlPoints[32][0] = tabWidth - (leftPeg.getPegNeckHeight(tabWidth) 
						+ leftPeg.getPegHeadHeight(tabWidth)) * leftPeg.getPegDirection();
		controlPoints[32][1] = tabHeight + (height - leftPeg.getPegHeadWidth(height)) / 2;
		controlPoints[33][0] = tabWidth - leftPeg.getPegNeckHeight(tabWidth) * leftPeg.getPegDirection();
		controlPoints[33][1] = tabHeight + (height - leftPeg.getPegHeadWidth(height)) / 2;
		controlPoints[34][0] = tabWidth - leftPeg.getPegNeckHeight(tabWidth) * leftPeg.getPegDirection();
		controlPoints[34][1] = tabHeight + (height - leftPeg.getPegNeckWidth(height)) / 2;
		controlPoints[35][0] = tabWidth;
		controlPoints[35][1] = tabHeight + (height - leftPeg.getPegNeckWidth(height)) / 2;
		// Static upper-left
		controlPoints[36][0] = tabWidth;
		controlPoints[36][1] = tabHeight;
	
		return controlPoints;
	}
	
	private Bitmap cutOutPiece(Bitmap graphic, Bitmap pieceShape)
	{
		Bitmap finishedPiece = Bitmap.createBitmap(width + tabWidth * 2, height + tabWidth * 2, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(finishedPiece);
		Paint transferPaint = new Paint();
		transferPaint.setFilterBitmap(false);
		canvas.drawBitmap(graphic, 0, 0, paint);
		transferPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
		canvas.drawBitmap(pieceShape, 0, 0, transferPaint);
		transferPaint.setXfermode(null);
		
		return finishedPiece;
	}
	
	private void calcDimensions(int width, int height)
	{
		tabWidth = width / 3;
		tabHeight = height / 3;
		this.width = tabWidth * 3;
		this.height = tabHeight * 3;
	}
	
	private int[] combinePixels(int[] centerPixels, int[] upperPixels, 
			int[] lowerPixels, int[] leftPixels, int[] rightPixels)
	{
		int[] combinedPixels = new int[(width + tabWidth * 2) * (height + tabWidth * 2)];
		for (int i = 0; i < combinedPixels.length; i++)
			combinedPixels[i] = NO_COLOR;
		
		for (int x = 0; x < width + tabWidth * 2; x++)
			for (int y = 0; y < height + tabHeight * 2; y++)
			{
				if (x < tabWidth)
				{
					if (y < tabHeight || y >= (height + tabHeight) || leftPixels == null)
						combinedPixels[y * (width + tabWidth * 2) + x] = NO_COLOR;
					else
						combinedPixels[y * (width + tabWidth * 2) + x] 
						       = leftPixels[(y - tabHeight) * tabWidth + x];
				}
				else if (x < (width + tabWidth))
				{
					if (y < tabHeight)
					{
						if (upperPixels == null)
							combinedPixels[y * (width + tabWidth * 2) + x] = NO_COLOR;
						else
							combinedPixels[y * (width + tabWidth * 2) + x] 
							       = upperPixels[y * width + (x - tabWidth)];
					}
					else if (y < (height + tabHeight))
					{
						if (centerPixels == null)
							combinedPixels[y * (width + tabWidth * 2) + x] = NO_COLOR;
						else				
							combinedPixels[y * (width + tabWidth * 2) + x] 
							       = centerPixels[(y - tabHeight) * width + (x - tabWidth)];
					}
					else
					{
						if (lowerPixels == null)
							combinedPixels[y * (width + tabWidth * 2) + x] = NO_COLOR;
						else
							combinedPixels[y * (width + tabWidth * 2) + x] 
							       = lowerPixels[(y - (height + tabHeight)) * width + (x - tabWidth)];
					}
				}
				else // if (x >= (width + quarterWidth))
				{
					if (y < tabHeight || y >= (height + tabHeight) || rightPixels == null)
						combinedPixels[y * (width + tabWidth * 2) + x] = NO_COLOR;
					else
						combinedPixels[y * (width + tabWidth * 2) + x] 
						       = rightPixels[(y - tabHeight) * tabWidth + x - (width + tabWidth)];
				}
			}
		return combinedPixels;
	}
	
	public void setPos (float x, float y)
	{
		if (this.x != x || this.y != y)
		{
			this.x = x;
			this.y = y;
			translateMatrix.reset();
			translateMatrix.postTranslate(x - xPan, y - yPan);
			notifyAttachedOfPos();
		}
	}
	
	public void setPos (float screenX, float screenY, float zoom, int panX, int panY, FloatPoint dragDiff)
	{
		FloatPoint localPoint = screenToPannedZoomedCoordinates(screenX, screenY, zoom, panX, panY);
		localPoint.setX(localPoint.getX() - dragDiff.getX());
		localPoint.setY(localPoint.getY() - dragDiff.getY());
		setPos(localPoint.getX(), localPoint.getY());
	}
	
	private void notifyAttachedOfPos()
	{
		if (upAttached && upPiece != null)
			upPiece.setPos(x, y-height);
		if (downAttached && downPiece != null)
			downPiece.setPos(x, y+height);
		if (leftAttached && leftPiece != null)
			leftPiece.setPos(x-width, y);
		if (rightAttached && rightPiece != null)
			rightPiece.setPos(x+width, y);
	}
	
	public void setZoom (float zoom)
	{
		this.zoom = zoom;
		if (scaleMatrix == null)
			scaleMatrix = new Matrix();
		else
			scaleMatrix.reset();
		scaleMatrix.postScale(zoom, zoom);
	}
	
	public void setPan (int x, int y)
	{
		xPan = x;
		yPan = y;
		if (translateMatrix == null)
			translateMatrix = new Matrix();
		else
			translateMatrix.reset();
		translateMatrix.postTranslate(this.x - xPan, this.y - yPan);
	}
	
	public void finalizeEdges()
	{
		if (upPiece == null)
			upPeg.setPegFlat();
		if (downPiece == null)
			downPeg.setPegFlat();
		if (leftPiece == null)
			leftPeg.setPegFlat();
		if (rightPiece == null)
			rightPeg.setPegFlat();
	}
	
	// Draw image
	public void onDraw(Canvas canvas)
	{
		if (matrix == null)
			matrix = new Matrix();
		else
			matrix.reset();
		matrix.postConcat(translateMatrix);
		matrix.postConcat(scaleMatrix);
		
		canvas.drawBitmap(image, matrix, paint);
	}
	
	public boolean contains(float xScreen, float yScreen, float zoom, int xPan, int yPan)
	{
		FloatPoint localPoint = screenToPannedZoomedCoordinates(xScreen, yScreen,
				zoom, xPan, yPan);
		return (localPoint.getX() > x + tabWidth && localPoint.getX() < x + width + tabWidth
				&& localPoint.getY() > y + tabHeight && localPoint.getY() < y + height + tabHeight);
	}
	
	public FloatPoint getDragDiff(float xScreen, float yScreen, float zoom, int xPan, int yPan)
	{
		FloatPoint localPoint = screenToPannedZoomedCoordinates(xScreen, yScreen,
				zoom, xPan, yPan);
		return new FloatPoint(localPoint.getX() - x, localPoint.getY() - y);
	}
	
	private FloatPoint screenToPannedZoomedCoordinates(float xScreen, float yScreen,
			float zoom, int xPan, int yPan)
	{
		return new FloatPoint(xScreen / zoom + xPan, yScreen / zoom + yPan);
	}

	public void tryAttach(PuzzlePiece otherPiece)
	{
		if (otherPiece.ID == upPieceID && !upAttached
				&& x > otherPiece.x - SNAP_TO_RANGE 
				&& x < otherPiece.x + SNAP_TO_RANGE
				&& y - height > otherPiece.y - SNAP_TO_RANGE 
				&& y - height < otherPiece.y + SNAP_TO_RANGE)
		{
			upAttached = true;
			otherPiece.downAttached = true;
			setUpPiece(otherPiece);
			otherPiece.notifyAttachedOfPos();
			PuzzleMat.tryAttach(otherPiece);
		}
		if (otherPiece.ID == downPieceID && !downAttached 
				&& x > otherPiece.x - SNAP_TO_RANGE 
				&& x < otherPiece.x + SNAP_TO_RANGE
				&& y + height > otherPiece.y - SNAP_TO_RANGE 
				&& y + height < otherPiece.y + SNAP_TO_RANGE)
		{
			downAttached = true;
			otherPiece.upAttached = true;
			setDownPiece(otherPiece);
			otherPiece.notifyAttachedOfPos();
			PuzzleMat.tryAttach(otherPiece);
		}
		if (otherPiece.ID == leftPieceID && !leftAttached
				&& x - width > otherPiece.x - SNAP_TO_RANGE 
				&& x - width < otherPiece.x + SNAP_TO_RANGE
				&& y > otherPiece.y - SNAP_TO_RANGE 
				&& y < otherPiece.y + SNAP_TO_RANGE)
		{
			leftAttached = true;
			otherPiece.rightAttached = true;
			setLeftPiece(otherPiece);
			otherPiece.notifyAttachedOfPos();
			PuzzleMat.tryAttach(otherPiece);
		}
		if (otherPiece.ID == rightPieceID && !rightAttached
				&& x + width > otherPiece.x - SNAP_TO_RANGE 
				&& x + width < otherPiece.x + SNAP_TO_RANGE
				&& y > otherPiece.y - SNAP_TO_RANGE 
				&& y < otherPiece.y + SNAP_TO_RANGE)
		{
			rightAttached = true;
			otherPiece.leftAttached = true;
			setRightPiece(otherPiece);
			otherPiece.notifyAttachedOfPos();
			PuzzleMat.tryAttach(otherPiece);
		}
	}

	public void breakLinks()
	{
		upAttached = false;
		downAttached = false;
		leftAttached = false;
		rightAttached = false;
	}
	
	public boolean isDone()
	{
		return (upAttached || upPiece == null) && (downAttached || downPiece == null)
			&& (leftAttached || leftPiece == null) && (rightAttached || rightPiece == null);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		
		loadImage();
		setZoom(zoom);
		setPan(xPan, yPan);
		upAttached = downAttached = leftAttached = rightAttached = false;
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(0);
	}
}
