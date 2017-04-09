package com.musiri.musiri;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;

public class DirectoryChooserDialog
{
    private Context _activityContext;
    private DirectoryChooserInterface _directoryChooserInterface;
    private AlertDialog.Builder _builder;
    private File _rootDir, _currDir;
    private String _currPath = "";
    private ArrayAdapter<String> _listAdapter = null;
    private TextView _titleView;

    public interface DirectoryChooserInterface
    {
        void onChosenDir(String path);
        void onCancelClicked();
    }

    public DirectoryChooserDialog(Context activityContext, DirectoryChooserInterface directoryChooserInterface)
    {
        // constructor initialization
        _activityContext = activityContext;
        _directoryChooserInterface = directoryChooserInterface;

        // initializing to the default values
        _listAdapter = new ArrayAdapter<String>(_activityContext, android.R.layout.select_dialog_item);

        _rootDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        _currDir = new File(_rootDir.getAbsolutePath());
        _currPath = _rootDir.getAbsolutePath();

        // building the dialog functionality
        buildDialogBuilder();
    }

    public void ChooseDirectory()
    {
        _builder.show();
    }

    private void buildDialogBuilder()
    {
        // initializing the builder
        _builder = new AlertDialog.Builder(_activityContext);
        _builder.setCancelable(false);

        // initializing everything with its default value
        buildTitleLayout();
        updateDialog();

        // every time the user clicking an item
        _builder.setSingleChoiceItems(_listAdapter, -1, new itemClickListener());

        // when the user presses the back button
        _builder.setOnKeyListener(new backPressedListener());

        _builder.setPositiveButton("OK", new onOKClick());
        _builder.setNegativeButton("Cancel", new onCancelClick());
    }

    // updates the dialog everytime the list changes(every time the user clicked a directory)
    private void updateDialog()
    {
        _listAdapter.clear();
        updateList(_currDir);

        _titleView.setText(_currDir.getAbsolutePath());
    }

    // update the items in the list to be the current directory
    private void updateList(File dir)
    {
        File[] files = dir.listFiles();
        int index = 0;

        if(files == null)
        {
            Toast.makeText(_activityContext, "FILES IS NULL", Toast.LENGTH_LONG).show();
            return;
        }

        for(int i = 0; i < files.length; i++)
        {
            if(files[i].isDirectory())
                _listAdapter.insert(files[i].getName(), index++);
        }

        _listAdapter.notifyDataSetChanged();
    }

    // building the title layout for the dialog
    private void buildTitleLayout()
    {
        _titleView = new TextView(_activityContext);
        _titleView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        _titleView.setTextAppearance(_activityContext, android.R.style.TextAppearance_Large);
        _titleView.setTextColor( _activityContext.getResources().getColor(android.R.color.black) );
        _titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        _titleView.setText(_rootDir.getAbsolutePath());

        _builder.setCustomTitle(_titleView);
    }

    // every time the user clicking an item
    private class itemClickListener implements DialogInterface.OnClickListener
    {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            _currPath += "/" + ((AlertDialog) dialog).getListView().getAdapter().getItem(which);
            _currDir = new File(_currPath);
            updateDialog();
        }
    }

    // when the user presses the back button
    private class backPressedListener implements DialogInterface.OnKeyListener
    {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)

        {
            // Back button pressed
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
            {
                // The very top level directory, do nothing
                if (_currDir.getAbsolutePath().equals(_rootDir.getAbsolutePath()))
                    return false;
                else
                {
                    // Navigate back to the parent directory
                    _currDir = _currDir.getParentFile();
                    _currPath = _currDir.getAbsolutePath();
                    updateDialog();
                }

                return true;
            }
            else
            {
                return false;
            }
        }
    }

    // when the OK button is clicked
    private class onOKClick implements DialogInterface.OnClickListener
    {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            // send the path to the onChosenDir callback
            if(_directoryChooserInterface != null)
                _directoryChooserInterface.onChosenDir(_currPath);
        }
    }

    // when the Cancel button is clicked
    private class onCancelClick implements  DialogInterface.OnClickListener
    {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            if(_directoryChooserInterface != null)
                _directoryChooserInterface.onCancelClicked();
        }
    }
}
