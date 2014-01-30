package net.doublea.puzzlecreator;

import java.util.Vector;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class PuzzleSelectorView extends ListView implements OnItemSelectedListener
{
	private String[] fileList;
	
	public PuzzleSelectorView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		
		String[] allFileList = context.fileList();
		Vector<String> puzzleVector = new Vector<String>();
		for (int i = 0; i < allFileList.length; i++)
			if (allFileList[i].toUpperCase().endsWith(".PZL"))
				puzzleVector.add(allFileList[i]);
		fileList = puzzleVector.toArray(new String[] {});
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
				android.R.layout.simple_list_item_1, fileList);
		adapter.setNotifyOnChange(true);
		setAdapter(adapter);
		this.setFocusable(false);
		setOnItemSelectedListener(this);
	}

	@Override
	public void onItemSelected(AdapterView<?> adapter, View view, int pos,
			long id) 
	{
		System.out.println("|^^| Selector View: Loading...");
		PuzzleMat.loadMat(fileList[pos]);
		Puzzle.displayPuzzle();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) 
	{
		System.out.println("|^^| Selector View: Nothing selected");
	}
	
}
