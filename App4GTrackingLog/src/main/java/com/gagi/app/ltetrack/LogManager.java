package com.gagi.app.ltetrack;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Created by igaglioti on 12/02/14.
 */
public class LogManager
{
    //String filename = "MyLTETracking.log";
    String filename = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/MyLTETracking.log";
    FileOutputStream outputStream;
    private  static LogManager _logManager = null;
    private LogManager()
    {

    }

    public static LogManager GetInstance()
    {
        if(_logManager == null)
            _logManager = new LogManager();
        return  _logManager;
    }

    public synchronized void WriteToFile(String message)
    {
        try {
            File file = new File(filename);
            boolean fileOK = CreateLogFile(file);

            message = this.GetCurrentDateTimeFormatted() + " - " + message + "\r\n";
            if(fileOK)
            {
                FileOutputStream fOut = new FileOutputStream(file,true);
                OutputStreamWriter myOutWriter =
                        new OutputStreamWriter(fOut);
                myOutWriter.append(message);
                myOutWriter.close();
                fOut.close();
            }


        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private boolean CreateLogFile(File file) throws IOException {
        boolean fileOK = file.exists();
        if(!fileOK)
        {
            fileOK = file.createNewFile();
        }
        return fileOK;
    }

    public String GetCurrentDateTimeFormatted()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        return  dateFormat.format(cal.getTime());
    }
}
