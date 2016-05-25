package Server;

public class Question 
{
    private String question;
    private String songFileName;
    private String imageFileName;
    private String q1, q2, q3;
    private int numAns;
    
    Question(String q, String s, String i, String q1x, String q2x, String q3x, int n)
    {
        question = q;
        songFileName = s;
        imageFileName = i;
        q1 = q1x;
        q2 = q2x;
        q3 = q3x;
        numAns = n;
    }

    public String getQuestion() 
    { return question; }
    public String getSongFileName() 
    { return songFileName; }
    public String getImageFileName() 
    { return imageFileName; }
    public String getQ1() 
    { return q1; }
    public String getQ2() 
    { return q2; }
    public String getQ3() 
    { return q3; }
    public int getNumAns() 
    { return numAns; }

    public void setQuestion(String question) 
    { this.question = question; }
    public void setSongFileName(String songFileName) 
    { this.songFileName = songFileName; }
    public void setImageFileName(String imageFileName) 
    { this.imageFileName = imageFileName; }
    public void setQ1(String q1) 
    { this.q1 = q1; }
    public void setQ2(String q2) 
    { this.q2 = q2; }
    public void setQ3(String q3) 
    { this.q3 = q3; }
    public void setNumAns(int numAns) 
    { this.numAns = numAns; }
}
