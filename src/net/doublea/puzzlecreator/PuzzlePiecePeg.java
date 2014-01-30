package net.doublea.puzzlecreator;

import java.io.Serializable;
import java.util.Random;

public class PuzzlePiecePeg implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1492843118630634324L;
	public static final float MIN_HEAD_HEIGHT = 0.15f;
	public static final float MAX_HEAD_HEIGHT = 0.5f;
	public static final float MIN_HEAD_WIDTH = 0.35f;
	public static final float MAX_HEAD_WIDTH = 0.5f;
	public static final float MIN_NECK_HEIGHT = 0.2f;
	public static final float MAX_NECK_HEIGHT = 0.5f;
	public static final float MIN_NECK_WIDTH = 0.1f;
	public static final float MAX_NECK_WIDTH = 0.25f;
	
	public enum PegDirection {IN, OUT, FLAT};
	
	private PegDirection pegDirection;
	private float pegHeadHeight;
	private float pegHeadWidth;
	private float pegNeckHeight;
	private float pegNeckWidth;

	public PuzzlePiecePeg()
	{
		this(new Random());
	}
	
	public PuzzlePiecePeg(Random rand)
	{
		pegDirection = rand.nextBoolean() ? PegDirection.IN : PegDirection.OUT;
		pegHeadHeight = MIN_HEAD_HEIGHT + rand.nextFloat() * (MAX_HEAD_HEIGHT - MIN_HEAD_HEIGHT);
		pegHeadWidth = MIN_HEAD_WIDTH + rand.nextFloat() * (MAX_HEAD_WIDTH - MIN_HEAD_WIDTH);
		pegNeckHeight = MIN_NECK_HEIGHT + rand.nextFloat() * (MAX_NECK_HEIGHT - MIN_NECK_HEIGHT);
		pegNeckWidth = MIN_NECK_WIDTH + rand.nextFloat() * (MAX_NECK_WIDTH - MIN_NECK_WIDTH);
	}
	
	public PuzzlePiecePeg(PegDirection pegDirection, float pegHeadHeight, float pegHeadWidth,
			float pegNeckHeight, float pegNeckWidth)
	{
		this.pegDirection = pegDirection;
		this.pegHeadHeight = pegHeadHeight;
		this.pegHeadWidth = pegHeadWidth;
		this.pegNeckHeight = pegNeckHeight;
		this.pegNeckWidth = pegNeckWidth;
	}
	
	public float getPegHeadHeight(float maxHeight)
	{
		return pegHeadHeight * maxHeight;
	}
	
	public float getPegHeadWidth(float maxWidth)
	{
		return pegHeadWidth * maxWidth;
	}
	
	public float getPegNeckHeight(float maxHeight)
	{
		return pegNeckHeight * maxHeight;
	}
	
	public float getPegNeckWidth(float maxWidth)
	{
		return pegNeckWidth * maxWidth;
	}
	
	public int getPegDirection()
	{
		if (pegDirection.equals(PegDirection.OUT))
			return 1;
		else if (pegDirection.equals(PegDirection.IN))
			return -1;
		else //(pegDirection.equals(PegDirection.FLAT))
			return 0;
	}

	public void setPegFlat()
	{
		pegDirection = PegDirection.FLAT;
	}
	
	public PuzzlePiecePeg getInverse()
	{
		PuzzlePiecePeg inverse = deepCopy();
		if (inverse.pegDirection == PegDirection.IN)
			inverse.pegDirection = PegDirection.OUT;
		else if (inverse.pegDirection == PegDirection.OUT)
			inverse.pegDirection = PegDirection.IN;
		return inverse;
	}
	
	public PuzzlePiecePeg deepCopy()
	{
		return new PuzzlePiecePeg(pegDirection, pegHeadHeight, pegHeadWidth,
				pegNeckHeight, pegNeckWidth);
	}
}
