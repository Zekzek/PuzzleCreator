package net.doublea.puzzlecreator;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class Puzzle extends Activity 
{
	private static Puzzle me;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        me = this;
        setContentView(R.layout.activity_puzzle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
    	getMenuInflater().inflate(R.menu.activity_puzzle, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	//check selected menu item
    	// R.id.exit is @+id/exit
    	if(item.getItemId() == R.id.exit)
    	{
    		//close the Activity
    		this.finish();
    		return true;
    	}
    	else if(item.getItemId() == R.id.make_new_puzzle)
    	{
    		setContentView(R.layout.activity_puzzle);
            
    		// TODO testing only, purge all files before creating new ones
    		//String[] allFileList = fileList();
    		//for (int i = allFileList.length - 1; i >= 0; i--)
    		//	deleteFile(allFileList[i]);
    		
    		PuzzleMat.newMatFromImage(2, 2, "", "Test");
    		return true;
    	}
    	else if(item.getItemId() == R.id.load_puzzle)
    	{
    		setContentView(R.layout.load_puzzle);
            return true;
    	}
    	
    	return false;
    }
    
    public static void displayPuzzle()
    {
    	if (me != null)
    		me.setContentView(R.layout.activity_puzzle);
    }
}
