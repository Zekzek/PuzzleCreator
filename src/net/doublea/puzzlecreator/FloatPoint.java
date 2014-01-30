package net.doublea.puzzlecreator;

import java.io.Serializable;

public class FloatPoint implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6631319224049897300L;
	private float x, y;
	
	public FloatPoint()
	{
		x = 0;
		y = 0;
	}
	
	public FloatPoint(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	public float getX()
	{
		return x;
	}
	
	public float getY()
	{
		return y;
	}
	
	public void setX(float value)
	{
		x = value;
	}
	
	public void setY(float value)
	{
		y = value;
	}
}
