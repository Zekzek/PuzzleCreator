package net.doublea.puzzlecreator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ScaleGestureDetector;
import android.view.View;

public class MatView extends View
{
	private PuzzleMat mat;
	
	public MatView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		mat = new PuzzleMat(context, attrs, this);
		
		setBackgroundColor(Color.TRANSPARENT);
		new ScaleGestureDetector(context, mat);
		setOnTouchListener(mat);
	}
	
	public void onDraw(Canvas canvas) 
	{
		mat.onDraw(canvas);
    }
}
