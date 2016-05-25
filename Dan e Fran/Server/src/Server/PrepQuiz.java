package Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class PrepQuiz 
{
    private File file;
    private Scanner scan;
    private ArrayList<Question> quiz;
    
    PrepQuiz(String filePath)
    {
        file = new File(filePath);
        try
        { scan = new Scanner(new BufferedReader(new FileReader(file))); }
        catch(FileNotFoundException fnfe)
        { System.err.println("Oh Darn! File not found, can't load Quiz! :'("); }
        quiz = new ArrayList<>();
    }
    
    public ArrayList<Question> getQuiz()
    {
        loadQuiz();
        return quiz;
    }
    
    public void loadQuiz()
    {
        for(int i=0; i<3; i++)
        { scan.nextLine(); }
        while (scan.hasNextLine())
        {
            String line = scan.nextLine();
            StringTokenizer st = new StringTokenizer(line, ",");
            
            String q, s, i ,q1, q2, q3;
            int n;
            
            s = (String)st.nextElement();
            i = (String)st.nextElement();
            q = (String)st.nextElement();
            q1 = (String)st.nextElement();
            q2 = (String)st.nextElement();
            q3 = (String)st.nextElement();
            n = Integer.parseInt((String)st.nextElement());
            
            Question qs = new Question(q, s, i, q1, q2, q3, n);
            
            quiz.add(qs);
        }
    }
}
