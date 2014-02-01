package net.doublea.puzzlecreator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;

public class PuzzleMat implements OnScaleGestureListener, OnTouchListener, Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8465298550310650226L;
	private static transient View myView;
	private static transient Context activeContext;
	private static transient AttributeSet activeAttrs;
	private static PuzzleMat activeMat;
	private String name;
	private String filename;
	private PuzzlePiece[] pieces;
	private float zoom, scaleStartZoom;
	private int xPan, yPan;
	private PuzzlePiece dragPiece;
	private FloatPoint dragDiff;
	private FloatPoint mapDrag;
	private float imageWidth, imageHeight;
	private FloatPoint numPieces;
	
	public PuzzleMat() 
	{
		this(activeContext, activeAttrs, myView);
	}
	
 	public PuzzleMat(Context context, AttributeSet attrs, View view) 
	{
		if (activeContext == null)
			activeContext = context;
		if (activeAttrs == null)
			activeAttrs = attrs;
		myView = view;
		
		activeMat = this;
		new AlertDialog.Builder(this)
			.setTitle("Debug entry")
			.setMessage("Context is " + context)
			.setIcon(R.drawable.ic_dialog_alert)
			.show();
		//new ScaleGestureDetector(context, this);
	}
 	
 	public static void newMatFromImage(int xPieces, int yPieces, String imageFilename, String saveFilename)
 	{
 		newMatFromImage(xPieces, yPieces, BitmapFactory.decodeResource(myView.getResources(),
 				R.drawable.tree_stream), saveFilename);
 	}
 	
 	public static void newMatFromImage(int xPieces, int yPieces, Bitmap image, String puzzleName)
 	{
 		if (activeMat != null)
 		{
 			activeMat.name = puzzleName;
 			activeMat.filename = puzzleName + ".PZL";
	 		activeMat.numPieces = new FloatPoint(xPieces, yPieces);
	 		activeMat.pieces = new PuzzlePiece[xPieces * yPieces];
	 		/*
			activeMat.makePuzzle(image, (int)activeMat.numPieces.getX(), 
	 				(int)activeMat.numPieces.getY());
	 		activeMat.imageWidth = image.getWidth();
	 		activeMat.imageHeight = image.getHeight();
			
	 		activeMat.scatter((int)activeMat.imageWidth, (int)activeMat.imageHeight);
			
	 		activeMat.zoom = 0.4f;
	 		activeMat.xPan = activeMat.yPan = 0;
	 	
	 		for (int i = 0; i < activeMat.pieces.length; i++)
	 			activeMat.pieces[i].saveImage();
			saveMatToFile(activeContext, puzzleName + ".PZL", activeMat);
			*/
			myView.invalidate();
 		}
 	}
 	
 	public static void loadMat(String filename)
 	{
 		if (activeContext != null && activeMat != null)
 			activeMat.loadMat(PuzzleMat.loadMatFromFile(activeContext, filename));
 	}
 	
 	private void loadMat(PuzzleMat mat)
 	{
 		if (activeMat != null)
 		{
 			activeMat.name = mat.name;
 			activeMat.filename = mat.filename;
 			activeMat.pieces = mat.pieces;
 			activeMat.zoom = mat.zoom;
 			activeMat.xPan = mat.xPan;
 			activeMat.yPan = mat.yPan;
 			activeMat.dragDiff = mat.dragDiff;
 			activeMat.mapDrag = mat.mapDrag;
 			activeMat.imageWidth = mat.imageWidth;
 			activeMat.imageHeight = mat.imageHeight;
 			activeMat.numPieces = mat.numPieces; 		
 		}
 	}
	
	private void makePuzzle(Bitmap image, int xPieces, int yPieces)
	{
		int pieceWidth = image.getWidth() / xPieces;
		int pieceHeight = image.getHeight() / yPieces;
		PuzzlePiece[][] pieceArray = new PuzzlePiece[xPieces][yPieces];
		
		for (int i = 0; i < pieceArray.length; i++)
			pieceArray[i] = new PuzzlePiece[yPieces];
		
		for (int x = 0; x < xPieces; x++)
			for (int y = 0; y < yPieces; y++)
			{
				PuzzlePiece upPiece, downPiece, leftPiece, rightPiece;
				upPiece = downPiece = leftPiece = rightPiece = null;
				if (y > 0)
					upPiece = pieceArray[x][y-1];
				if (y < yPieces - 1)
					downPiece = pieceArray[x][y+1];
				if (x > 0)
					leftPiece = pieceArray[x-1][y];
				if (x < xPieces - 1)
					rightPiece = pieceArray[x+1][y];
				
				PuzzlePiece thePiece = new PuzzlePiece(name, upPiece, downPiece,
						leftPiece, rightPiece, y * xPieces + x, activeContext);	
				
				pieceArray[x][y] = thePiece;
				activeMat.pieces[y * xPieces + x] = thePiece;		
			}
		for (int x = 0; x < xPieces; x++)
			for (int y = 0; y < yPieces; y++)
			{
				PuzzlePiece thePiece = pieceArray[x][y];
				thePiece.finalizeEdges();
				thePiece.paintPiece(image, x, y, pieceWidth, pieceHeight);
			}
	}
	
	private boolean isComplete()
	{
		for (int i = 0; i < pieces.length; i++)
			if (!pieces[i].isDone())
				return false;
		return true;
	}
	
	private void breakLinks()
	{
		for (int i = 0; i < pieces.length; i++)
			pieces[i].breakLinks();
	}
	
	private void scatter(int totalWidth, int totalHeight)
	{
		Random rand = new Random();
		for (int i = 0; i < pieces.length; i++)
			pieces[i].setPos(rand.nextInt(totalWidth), rand.nextInt(totalHeight));
	}

	public void onDraw(Canvas canvas) 
	{
		if (pieces != null)
			for (int i = 0; i < pieces.length; i++)
			{
				PuzzlePiece aPiece = pieces[i];
				aPiece.setZoom(zoom);
				aPiece.setPan(xPan, yPan);
				aPiece.onDraw(canvas);
			}
    }
	
	@Override
	public boolean onTouch(View view, MotionEvent event) 
	{        
		if (event.getAction() == MotionEvent.ACTION_DOWN) 
        {
        	for (int i = pieces.length - 1; i >= 0; i--)
        	{
        		if (pieces[i].contains(event.getX(), event.getY(), zoom, xPan, yPan))
        		{
        			dragPiece = pieces[i];
        			moveToEndOfPieces(i);
        			dragDiff = dragPiece.getDragDiff(event.getX(), event.getY(), zoom, xPan, yPan);
        			i = -1; // break out of for loop
        		}
        	}
        	if (dragPiece == null)
        	{
        		mapDrag = new FloatPoint(event.getX(), event.getY());
        	}
        }
        else if (event.getAction() == MotionEvent.ACTION_UP) 
        {
        	if (dragPiece != null)
        	{
        		tryAttach(dragPiece);
        		dragPiece = null;
        	}
        	mapDrag = null;
        	PuzzleMat.saveMatToFile(activeContext, filename, this);
        }
        else
        {
        	if (dragPiece != null)
        	{
        		dragPiece.setPos(event.getX(), event.getY(), zoom, xPan, yPan, dragDiff);
        	}
        	else if (mapDrag != null)
        	{
        		xPan += mapDrag.getX() - event.getX();
        		yPan += mapDrag.getY() - event.getY();
        		
        		mapDrag = new FloatPoint(event.getX(), event.getY());
        	}
        }
        
        view.invalidate();
        
        return view.onTouchEvent(event);
    }
	
	private void moveToEndOfPieces(int index)
	{
		PuzzlePiece movingPiece = pieces[index];
		
		while(index < pieces.length - 1)
		{
			pieces[index] = pieces[index + 1];
			index++;
		}
		pieces[index] = movingPiece;
	}
	
	public static void tryAttach(PuzzlePiece thePiece)
	{
		if (activeMat != null)
			for (int i = 0; i < activeMat.pieces.length; i++)
				thePiece.tryAttach(activeMat.pieces[i]);
	}
	
	@Override
	public boolean onScale(ScaleGestureDetector arg0) 
	{
		if (arg0.getPreviousSpan() > 0)
		{
			float minZoom = (float)myView.getWidth() / imageWidth / 2;
			int pieceAcrossCount = (int)numPieces.getX();
			if (numPieces.getY() > pieceAcrossCount)
				pieceAcrossCount = (int)numPieces.getY();
			float maxZoom = minZoom * pieceAcrossCount;
			
			zoom = scaleStartZoom * arg0.getScaleFactor();
			if (zoom < minZoom)
				zoom = minZoom;
			else if (zoom > maxZoom)
				zoom = maxZoom;
		}
		return true;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector arg0) 
	{
		scaleStartZoom = zoom;
		return false;
	}
	
	@Override
	public void onScaleEnd(ScaleGestureDetector arg0) {}

	// Static methods to save and load mats
	public static void saveMatToFile(Context context, String filename, PuzzleMat mat)
	{
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try 
		{
			fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
			out = new ObjectOutputStream(fos);
			out.writeObject(mat);
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		
		try 
		{
			out.close();
			fos.close();
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	public static PuzzleMat loadMatFromFile(Context context, String filename)
	{
		boolean exists = false;
		
		String[] fileList = context.fileList();
		for (int i = 0; i < fileList.length; i++)
			if (fileList[i].equalsIgnoreCase(filename))
				exists = true;
		PuzzleMat mat = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try 
		{
			fis = context.openFileInput(filename);
			in = new ObjectInputStream(fis);
			mat = (PuzzleMat)in.readObject();	
		} catch (FileNotFoundException e) { e.printStackTrace();
		} catch (StreamCorruptedException e) { e.printStackTrace();
		} catch (IOException e) { e.printStackTrace();
		} catch (ClassNotFoundException e) { e.printStackTrace();
		}
		
		try 
		{
			if (in != null)
				in.close();
			if (fis != null)
				fis.close();
		} catch (IOException e) { e.printStackTrace();
		}
		
		return mat;
	}
	
	public static Context getActiveContext()
	{
		if (activeMat != null)
			return activeMat.activeContext;
		else
			return null;
	}
}
