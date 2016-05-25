package client;

import java.io.Serializable;

public class ObjectFile implements Serializable 
{
    private long fileSize;
    private byte[] fileData;
    
    public ObjectFile() {}
    
    public ObjectFile(long fs, byte[] fd)
    {
        fileSize = fs;
        fileData = fd;
    }
    
    public long getFileSize() 
    { return fileSize; }
    public byte[] getFileData() 
    { return fileData; }
    
    public void setFileSize(long fileSize) 
    { this.fileSize = fileSize; }
    public void setFileData(byte[] fileData) 
    { this.fileData = fileData; }
}
