package Client;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by NoobLevler on 16/05/2016.
 */
public class FileDB {

    ArrayList<String> fileList;

    public FileDB(ArrayList<String> fileList) {
        this.fileList = fileList;
    }

    public FileDB() {
        fileList = new ArrayList<>();
        updateDB();
    }
    void updateDB()
    {
        fileList = new ArrayList<>();
        updateDBRec("./Music/", fileList);
    }
    void updateDBRec(String dirName, ArrayList<String> l)
    {
        File dir = new File(dirName);

        for(File f : dir.listFiles())
        {
            if(f.isDirectory())
            {
                updateDBRec(f.getAbsolutePath(),l);
            }else{
                l.add(f.getName());
            }
        }
    }
}
