import java.sql.*;
import java.util.*;

/**
 * Functions to Display information from the database
 * Add/Update Publisher
 * Insert a new book
 * Remove a book
 * @author Jimmy Chao
 * @author Alex Tol
 */
public class Functions 
{
    /**
     * This method is to get the selected columns that the user wants to see
     * @param in Scanner to collect console input
     * @param type the table the user wants to select columns from
     * type 1=WritingGroup
     * type 2= Publisher
     * type 3= Books
     * type 4= Display Books
     * @return Array list of user's selected columns
     */
    public static ArrayList getList(Scanner in,int type)
    {
        //Store all User's column selection in this temporary arrayList
        ArrayList<String> userSelect = new ArrayList<String>();
        //temporary storage if the user wants to continue adding things to the arrayList
        String contin=" ";
        //check to see if the user input is correct
        String check;
        boolean firstinput=true;
        //checks to see if the user wants to continue
        while(!contin.equals("n")){
            System.out.println("Type in the words you want to see");
            //User inputs column name
            check= in.next();
            //ALL OF THESE IF STATMENTS ARE HERE TO CHECK IF THE STRING IS A NAME OF AN ATTRIBUTE FROM EACH TABLE
            if((type==1||type==4)&&(check.equals("GroupName")||check.equals("HeadWriter")||check.equals("YearFormed")||check.equals("Subject")))
            {
                userSelect.add(check);
                System.out.println("Would you like to continue press 'n' to stop or any key to continue");
                contin=in.nextLine();
            }
            else if((type==2||type==4)&&(check.equals("PublisherName")||check.equals("PublisherAddress")||check.equals("PublisherPhone")||check.equals("PublisherEmail")))
            {
                userSelect.add(check);
                System.out.println("Would you like to continue press 'n' to stop or any key to continue");
                contin=in.nextLine();
            }
            else if((type==3||type==4)&&(check.equals("BookTitle")||check.equals("YearPublished")||check.equals("YearFormed")||check.equals("NumberPages")||check.equals("PublisherName")||check.equals("GroupName")))
            {
                userSelect.add(check);
                System.out.println("Would you like to continue press 'n' to stop or any key to continue");
                contin=in.nextLine();
            }
            else if((type==4)&&check.equals("*")||check.equals("all")&&firstinput)
            {
                userSelect.add("*");
                System.out.println("Displaying all variables...");
                break;
            }
            else{System.out.println("Invalid input");}
            //ask if user wants to continue
            contin=in.nextLine();
            firstinput=false;
        }   
        //return the arraylist of Column selections
        return userSelect;
    }
    /**
     * Display Selected information from what the user wants to see
     * @param stmt to send SQL statements to the database
     * @param inputs the columns the user wants to see
     * @param type which table the user wants to view
     * @param book the book title if they want to see certain information from a book
     */
    public static void DisplaySelected(Statement stmt, ArrayList<String> inputs,int type,String book)
    {
        //creating the SQL statement and choosing which table to view from
        String sql;
        sql="SELECT DISTINCT ";
        for(int i=0; i<inputs.size(); i++)
        {
            sql+= inputs.get(i);
            if((i+1)!=inputs.size()){sql+=",";}
            
        }
        sql+=" ";
        switch(type){
            case 1:  
                sql+="FROM WritingGroup";
                break;
            case 2: 
                sql+="FROM Publishers";
                break;
            case 3:
                sql+="FROM Book";
                break;     
            case 4:
                sql ="SELECT * FROM Book NATURAL JOIN PUBLISHERS NATURAL JOIN WRITINGGROUP WHERE BookTitle  = "+ "\'" +book+ "\'";
                break;
        }
        //executes the sql line and display the information
        try
        {
            ResultSet rs = stmt.executeQuery(sql);
            System.out.println("----------------------------------------------------------------------------------------------------");
            for(int i=0; i<inputs.size(); i++)
            {
                System.out.printf("%-30s", inputs.get(i));
            }
            System.out.println("\n----------------------------------------------------------------------------------------------------");
            while (rs.next())
            {
                for(int i=0; i<inputs.size(); i++){
                    String currentColumn = rs.getString(inputs.get(i));
                    System.out.printf("%-30s", JDBCProject.dispNull(currentColumn));
                }
                System.out.println();
            }
            rs.close();
        }
        catch (SQLException se) 
        {
             //Handle errors for JDBC
            se.printStackTrace();
        }
        System.out.println("----------------------------------------------------------------------------------------------------");
    
    }
    /**
     * Method to insert a certain book into the database
     * @param stmt 
     */
    public static void insertBook(Statement stmt)
    {
		//The first part of the query is prepared
        System.out.println("----------------------------------------------------------------------------------------------------");
        String query = "INSERT INTO Book VALUES(";
        Scanner in = new Scanner(System.in);
            
            try
            {
                //get writingGroup
                System.out.println("Which writing Group wrote this book?");
                String writingGroup = in.nextLine();

                //check if writing Group exists
                ResultSet rs = stmt.executeQuery("SELECT GROUPNAME FROM WRITINGGROUP WHERE GROUPNAME = '" + writingGroup + "'");
                if(rs.next())
                {//if writing Group exists, append query
                    query += "'" + writingGroup + "',";
                }
                else
                {   //if writting group doesn't exist, show list of valid groups and exit
                    //function
                    System.out.printf("The writing group you have entered does not exist\n "
                                    + "Here is a list of valid groups.\n\n");
                    rs = stmt.executeQuery("SELECT GROUPNAME FROM WRITINGGROUP");
                    
                        while(rs.next())
                            {System.out.println(rs.getObject("GROUPNAME"));}
                            System.out.printf("\n Press Any Key To Continue...\n");
                            in.nextLine();
                        
                    //exit out of function    
                    return;
                }
		//get title and append query
                System.out.println("What is the title of the book?");
                query += "'" + in.nextLine() + "',";
                //get publisher 
                System.out.println("Who published it?");
                String publisher = in.nextLine();
                //check if publisher exists
                rs = stmt.executeQuery("SELECT PUBLISHERNAME FROM PUBLISHERS WHERE PUBLISHERNAME = '" + publisher + "'");              
                if(rs.next())
                {
                    query += "'" + publisher + "',";
                }
                else
                {   //if publisher doesn't exist, show list of valid publishers, and exit function
                    System.out.printf("The publisher you have entered does not exist\n "
                                    + "Here is a list of valid publishers.\n\n");
                    rs = stmt.executeQuery("SELECT PUBLISHERNAME FROM PUBLISHERS");
                    
                        while(rs.next())
                            {System.out.println(rs.getObject("PUBLISHERNAME"));}
                            System.out.printf("\n Press Any Key To Continue...\n");
                            in.nextLine();
                        
                    //exit out of function    
                    return;
                }
            
            
		//get date published
                boolean valid = false;
                int day = 0,month = 0, year = 0;
                String date = "";
                while(!valid)
                {
                    try
                    {   //get month,day, and year seperately, check each one for correctness
                        System.out.println("What year was it published in?");
                        year = in.nextInt();

                        System.out.println("On which month? (enter as Integer)");
                        month = in.nextInt();
                            if(month > 12 || month <= 0)
                              {month = 12;}
                                
                        System.out.println("On what day? (enter as Integer)");
                           day = in.nextInt();
                            if(day > 31 || day <= 0)
                              {day = 31;}    
                        
                        valid = true;
                    }
                    
                        //this loop will continue until the user enters a valid date
                    
                        catch(InputMismatchException e)
                         {
                             System.out.println("Input Must be an int\n");
                             in.next();
                         }
                }    
                valid = false; //for later usage
                date = month + "/" + day + "/" + year; 
                query += "'" + date + "',";
                //get page number and append query
                System.out.println("How many pages does it have?");
                int pages = 0;
                while(!valid)
                {
                    try
                    {
                        pages = in.nextInt();
                        valid = true;
                    }

                    catch (InputMismatchException e)
                    {
                        System.out.println("Input MUST be an Integer");
                        in.next();
                    }
                }    
                
                query += pages + ")";

                //execute query
                stmt.executeUpdate(query);
            }            
            catch (SQLException se) 
            {
             //Handle errors for JDBC
            se.printStackTrace();
            }
            System.out.println("----------------------------------------------------------------------------------------------------");
    }
    /**
     * Removes a Book from the database
     * @param stmt 
     */
    public static void removeBook(Statement stmt)
    {
	//Prepare the first part of the query
        System.out.println("----------------------------------------------------------------------------------------------------");
        String query = "DELETE FROM BOOK WHERE BOOKTITLE = ";
        Scanner in = new Scanner(System.in);
        try
        {
        System.out.println("What book do you want to remove?");
		//get title, append query
        String bookTitle = in.nextLine();
        ResultSet rs = stmt.executeQuery("SELECT BOOKTITLE FROM BOOK WHERE BOOKTITLE = " +"'" + bookTitle + "'");
            //checks if books exists
            if(rs.next())
            {
            query += "'" + bookTitle + "'";
            }
            
            else
            {
                System.out.println("The book you specified does not exist\n");
                return;
            }
	//execute query
        stmt.executeUpdate(query);
        }
        
        catch (SQLException se) 
            {
             //Handle errors for JDBC
            System.out.println("The book you specified does not exist");
            }  
        System.out.println("----------------------------------------------------------------------------------------------------");
    }
    /**
     * Collects the Columns from the book table to display
     * @param stmt the Sql statement to be sent to be executed
     * @param getList if we are displaying certain information for a book or just book titles
     * @return ArrayList
     */
    public static ArrayList<String> DisplayBook(Statement stmt,Boolean getList)
    {
        //Holds all user input for a column selection
        ArrayList<String> bookList= new ArrayList<String>();
        try{
            String sql;
            sql="SELECT BookTitle FROM Book";
            ResultSet rs = stmt.executeQuery(sql);
            //display all book titles
            if(!getList){System.out.printf("%-20s\n","Book Titles");
            System.out.println("----------------------------------------------------------------------------------------------------");}
            
            while (rs.next())
            {
                if(!getList){
                    //Retrieve by column name
                    String BookTitle  = rs.getString("BookTitle");
                    //Display values
                    System.out.printf("%-20s\n",JDBCProject.dispNull(BookTitle));
                }
                else
                {
                    //add the column to the arraylist
                    bookList.add(rs.getString("BookTitle"));
                }
            }
            if(!getList){System.out.println("----------------------------------------------------------------------------------------------------");}
            rs.close();
        }
        catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }
        //return column selection
        return bookList;
    }
    /**
     * Display all Book Information
     * @param stmt 
     */
    public static void DisplayBookInformation(Statement stmt)
    {
        Scanner in=new Scanner(System.in);
        //finds the book title
        System.out.println("----------------------------------------------------------------------------------------------------");
        System.out.println("Please enter a book title");
        String input= in.nextLine();
        //collect user's column selection for the table
        ArrayList<String> bookList=DisplayBook(stmt,true);
        boolean bookfound=false;
        //CHECKS TO SEE IF BOOK EXIST
        for(int i=0; i<bookList.size(); i++)
        {
            String x=bookList.get(i);
            if((input.trim().equals(x.trim()))){bookfound=true;}
        }
        //IF BOOK EXIST
        if(bookfound)
        {
            //DISPLAY ALL INFORMATION IF THE ARRAY LIST CONTAIN ALL or *
            ArrayList<String> Select=getList(in,4);
            if((Select.get(0).equals("*"))){
                try{
                    String sql ="SELECT * FROM Book NATURAL JOIN PUBLISHERS NATURAL JOIN WRITINGGROUP WHERE BookTitle  = "+ "\'" +input+ "\'";
                    ResultSet rs = stmt.executeQuery(sql);
                    System.out.printf("%-30s%-20s%-20s%-20s%-20s%-20s%-20s%-30s%-30s%-30s%-20s\n","BookTitle","YearPublished","NumberPages","GroupName","HeadWriter","YearFormed","Subject","PublisherName","PublisherAddress","PublisherPhone","PublisherEmail");
                    while(rs.next())
                    {
                        String BookTitle=rs.getString("BookTitle");               
                        String YearPublished=rs.getString("YearPublished");         
                        String NumberPages=rs.getString("NumberPages"); 
                        String groupName = rs.getString("GroupName");
                        String HeadWriter = rs.getString("HeadWriter");
                        String YearFormed = rs.getString("YearFormed");
                        String Subject = rs.getString("Subject");
                        String PublisherName  = rs.getString("PublisherName");
                        String PublisherAddress  = rs.getString("PublisherAddress");
                        String PublisherPhone  = rs.getString("PublisherPhone");
                        String PublisherEmail = rs.getString("PublisherEmail");
                        System.out.printf("%-30s%-20s%-20s%-20s%-20s%-20s%-20s%-20s%-2s%-5s%-5s\n",JDBCProject.dispNull(BookTitle),JDBCProject.dispNull(YearPublished),JDBCProject.dispNull(NumberPages)
                                ,JDBCProject.dispNull(groupName),JDBCProject.dispNull(HeadWriter),JDBCProject.dispNull(YearFormed),JDBCProject.dispNull(Subject)
                                ,JDBCProject.dispNull(PublisherName),JDBCProject.dispNull(PublisherAddress),JDBCProject.dispNull(PublisherPhone),JDBCProject.dispNull(PublisherEmail));
                    }
                   System.out.println("----------------------------------------------------------------------------------------------------");
                    rs.close();
                }
                catch (SQLException se) {
                    //Handle errors for JDBC
                    se.printStackTrace();
                }
            }
            //OR ELSE DISPLAY INFORMATION THAT THE USER WANTS
            else{DisplaySelected(stmt, Select,4,input);}
        }
        else{System.out.println("Book not Found");}
    }
    /**
     * Updates a publisher or insert a new publisher
     * @param stmt 
     */
    public static void updatePublisher(Statement stmt)
    {
        //Ask if user wants to update a publisher or insert a new one
        System.out.println("----------------------------------------------------------------------------------------------------");
        System.out.println("Would you like to update a publisher(1) or add a new publisher(2)\n"
                              +"or enter anything else to exit");
        Scanner in = new Scanner(System.in);
            String answer = in.nextLine();
        //To update a publisher    
        if(answer.equals("1"))
         {
             boolean valid = false;
	    //prepare first part of query1
            String query1 = "SELECT PUBLISHERADDRESS,PUBLISHERPHONE,PUBLISHEREMAIL FROM PUBLISHERS WHERE PUBLISHERNAME = ";
            String target;
            ResultSet rs;
            try
            {
                //get publisher, append query 1
                System.out.println("What Publisher would you like to update?");
                target = in.nextLine();
                query1 += "'" + target + "'";
            
                rs = stmt.executeQuery(query1);
            
		//check if publisher exists, if yes execute below
                if(rs.next())
                {//prepare the attributes of the old publisher, so we can add them in with the new publisher
                  String pubAdress = rs.getString("PUBLISHERADDRESS");
                  String pubPhone = rs.getString("PUBLISHERPHONE");
                  String pubEmail = rs.getString("PUBLISHEREMAIL");
                    
                    //prepare first part of updates to and PUBLISHERS and BOOK tables
                    String update1 = "INSERT INTO PUBLISHERS VALUES (";
                    String update2 = "UPDATE BOOK SET PUBLISHERNAME = ";
                    String update3 = "DELETE FROM PUBLISHERS WHERE PUBLISHERNAME = " + "'" + target + "'"; 
                    //get new publisher 
                    System.out.println("What publisher would you like to replace them with?");
                    String target2 = in.nextLine();
                    //check to make sure user isn't trying to add an already existing publisher
                        while(!valid)
                        {
                            ResultSet rss = stmt.executeQuery("SELECT PUBLISHERNAME FROM PUBLISHERS WHERE PUBLISHERNAME = " + "'" + target2 + "'");
                            
                            if(rss.next())
                            {
                                System.out.println("This publisher already exists, please enter a valid Publisher");
                                target2 = in.nextLine();
                            }
                            
                            else
                            {
                                valid = true;
                            }
                            
                        }
				
				//append updates
                    update1 += "'" + target2 + "'," + "'" + pubAdress + "'," + "'" + pubPhone + "'," + "'" + pubEmail + "')";
                    update2 += "'" + target2 + "' WHERE PUBLISHERNAME = " + "'" + target + "'";                
                    //execute updates
                    stmt.executeUpdate(update1);
                    stmt.executeUpdate(update2);
                    stmt.executeUpdate(update3);                   
                }
                //if publisher does not exist, user is informed
                else
                {
                    System.out.println("This publisher does not exist");
                }
            }
            catch (SQLException se) 
            {
                //Handle errors for JDBC
                se.printStackTrace();
            }
        } 
        //to insert a new publisher
        else if(answer.equals("2"))
        {
            try
            {
                boolean valid = false;
                //creating the sql statement to insert a new publisher
                String query = "INSERT INTO PUBLISHERS VALUES(";
                
                System.out.println("What is the Publisher's name?");
                String pubName = in.nextLine();
                //check to make sure the user isn't trying to add an already existing publisher
                    while(!valid)
                        {
                            ResultSet rss = stmt.executeQuery("SELECT PUBLISHERNAME FROM PUBLISHERS WHERE PUBLISHERNAME = " + "'" + pubName + "'");
                            
                            if(rss.next())
                            {
                                System.out.println("This publisher already exists, please enter a valid Publisher");
                                pubName = in.nextLine();
                            }
                            
                            else
                            {
                                valid = true;
                            }
                            
                        }
                valid = false;
                query += "'" + pubName + "',";
                
                System.out.println("What is the Publisher's adress?");
                String pubAdress = in.nextLine();
	        query += "'" + pubAdress + "',";
                
                System.out.println("What is the Publisher's phone? (enter digits only)");
                String pubPhone = "";
                    while(!valid)
                    {
                        pubPhone = in.nextLine();
                        valid = true;
                        
                        if(!pubPhone.matches("[-+]?\\d*\\.?\\d+") || pubPhone.length() != 10)
                        {
                            System.out.println("Inputs can only be numeric and MUST have 10 digits");
                            valid = false;
                        }
                    }
                
	        query += "'" + pubPhone + "',";
                
               System.out.println("What is the Publisher's email?");
               String pubEmail = in.nextLine();
               
                    while(!pubEmail.contains("@gmail.com") && !pubEmail.contains("@yahoo.com") && !pubEmail.contains("@alexmail.com"))
                    {
                        System.out.println("make sure to add a valid suffix: @gmail.com or @yahoo.com or @alexmail.com");
                        pubEmail = in.nextLine();
                    }
                    
                query += "'" + pubEmail + "')";
                
                stmt.executeUpdate(query);
        
            }
            catch (SQLException se) 
                {
                //Handle errors for JDBC
                se.printStackTrace();
                }
        }
        System.out.println("----------------------------------------------------------------------------------------------------");
    }
    /**
     * Displays all information for the Writing Group
     * @param stmt 
     */
    public static void DisplayWritingGroup(Statement stmt)
    {               
        try
        {
            //creating SQL statement 
            String sql;
            sql="SELECT * FROM WritingGroup";
            ResultSet rs = stmt.executeQuery(sql);
            System.out.printf(JDBCProject.displayFormat, "Group Name", "HeadWriter", "YearFormed", "Subject");
            System.out.println("----------------------------------------------------------------------------------------------------");
            while (rs.next())
            {
                //Retrieve by column name
                String groupName = rs.getString("GroupName");
                String HeadWriter = rs.getString("HeadWriter");
                String YearFormed = rs.getString("YearFormed");
                String Subject = rs.getString("Subject");
                //Display values
                System.out.printf(JDBCProject.displayFormat, 
                JDBCProject.dispNull(groupName), JDBCProject.dispNull(HeadWriter), JDBCProject.dispNull(YearFormed), JDBCProject.dispNull(Subject));
            }
           System.out.println("----------------------------------------------------------------------------------------------------");
            rs.close();
        }
        catch (SQLException se) 
        {
             //Handle errors for JDBC
            se.printStackTrace();
        }
    }   
    /**
     * Display all Publisher Information
     * @param stmt 
     */
    public static void DisplayPublishers(Statement stmt)
    {
                
        try{
            //Creating sql statement 
            String sql;
            sql="SELECT * FROM Publishers";
            ResultSet rs = stmt.executeQuery(sql);
            System.out.printf("%-30s%-30s%-30s%-25s\n", "Publisher Name", "PublisherAddress", "PublisherPhone", "PublisherEmail");
            System.out.println("---------------------------------------------------------------------------------------------------------");
            while (rs.next())
            {
                //Retrieve by column name
                String PublisherName  = rs.getString("PublisherName");
                String PublisherAddress  = rs.getString("PublisherAddress");
                String PublisherPhone  = rs.getString("PublisherPhone");
                String PublisherEmail = rs.getString("PublisherEmail");
                //Display values
                System.out.printf("%-20s%-20s%-25s%-35s\n", 
                JDBCProject.dispNull(PublisherName), JDBCProject.dispNull(PublisherAddress), JDBCProject.dispNull(PublisherPhone), JDBCProject.dispNull(PublisherEmail));
            }
            System.out.println("---------------------------------------------------------------------------------------------------------");
            rs.close();
        }
        catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }
    }
}
