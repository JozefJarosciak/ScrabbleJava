import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main {

    // declaring the components
    private JPanel panel1;
    private JTextField searchText;
    private JTable table;
    private JButton searchButton;
    private JScrollPane scroller;

    public static void main(String[] args) {
        JFrame frame = new JFrame("");
        frame.setContentPane(new Main().panel1);
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.pack();
        frame.setVisible(true);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
    }


    public Main() {

        // Listen to Search Button
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dbQuery();
            }
        });

        // Listen to Enter Button on the Text Entry Box
        searchText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dbQuery();
            }
        });
    }

    public int charScrabbleValue(String chr) {
        /*
        1 point: E ×12, A ×9, I ×9, O ×8, N ×6, R ×6, T ×6, L ×4, S ×4, U ×4
        2 points: D ×4, G ×3
        3 points: B ×2, C ×2, M ×2, P ×2
        4 points: F ×2, H ×2, V ×2, W ×2, Y ×2
        5 points: K ×1
        8 points: J ×1, X ×1
        10 points: Q ×1, Z ×1
        */
        int charValue = 0;

        if ((chr.contains("a"))||(chr.contains("e"))||(chr.contains("i"))||(chr.contains("o"))||(chr.contains("n"))
            ||(chr.contains("r"))||(chr.contains("t"))||(chr.contains("l"))||(chr.contains("s"))|| (chr.contains("u"))
            ) {charValue=1;}

        if ((chr.contains("d"))||(chr.contains("g")) ) {charValue=2;}

        if ((chr.contains("b"))||(chr.contains("c"))||(chr.contains("m"))||(chr.contains("p")) ) {charValue=3;}

        if ((chr.contains("f"))||(chr.contains("h"))||(chr.contains("v"))||(chr.contains("w"))||(chr.contains("y"))
            ) {charValue=4;}

        if ((chr.contains("k"))) {charValue=5;}

        if ((chr.contains("j"))||(chr.contains("x"))) {charValue=8;}

        if ((chr.contains("q"))||(chr.contains("z")) ) {charValue=10;}

        //System.out.println("Value is: " + charValue) ;
        return charValue;
    }


    public String createSqlQuery(String enteredWord) {
        String alphabet = "abcdefghijklmnopqrstuvwxyz";

        // split entered string into letters
        String[] array = enteredWord.split("(?!^)");
        int arrLen = array.length;
        String sqlQuery = "SELECT WORD FROM en_aspell WHERE (";
        for(int i=0; i<arrLen; i++){
            //sqlQuery = sqlQuery +  "word LIKE '%" + array[i] + "%' AND ";
            sqlQuery = sqlQuery +  "LOWER(WORD) LIKE '%" + array[i] + "%' OR ";
            // remove used letters from alphabet string
            alphabet = alphabet.replace(array[i], "");
        }
        sqlQuery = sqlQuery.replaceFirst(" OR $", "");

        sqlQuery = sqlQuery + " AND LENGTH(WORD)<="+arrLen+") AND (";

        for(int i=0; i<alphabet.length(); i++) {
            sqlQuery = sqlQuery + "LOWER(WORD) NOT LIKE '%" + alphabet.charAt(i)+"%' AND ";
            sqlQuery = sqlQuery + "LOWER(WORD) NOT LIKE '" + alphabet.charAt(i)+"%' AND ";
            sqlQuery = sqlQuery + "LOWER(WORD) NOT LIKE '%" + alphabet.charAt(i)+"' AND ";
        }
        sqlQuery = sqlQuery.replaceFirst(" AND $", "");
        sqlQuery = sqlQuery + ")";
        return sqlQuery;
    }


    public void dbQuery() {

        try {

            // Connect to DB

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con= DriverManager.getConnection(
                "jdbc:mysql://85.10.205.173:3307/englishwords?autoReconnect=true&useSSL=false","jozef","elvina");
            Statement stmt=con.createStatement();

            // Setup Table for Displaying Results
            DefaultTableModel model = new DefaultTableModel();
            table.setAutoCreateRowSorter(true);
            table.setFillsViewportHeight(true);
            scroller.setVisible(true);
            model.addColumn("#");
            model.addColumn("Word");
            model.addColumn("Scrabble Value");
            table.setModel(model);
            table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 18));

            // Display Results
            String searchLetters = searchText.getText().toLowerCase();

            // Build SQL Search Query based on the letters entered by the user
            String sqlQuery = createSqlQuery(searchLetters);
            System.out.println(sqlQuery);

            // Run SQL query
            ResultSet result = stmt.executeQuery(sqlQuery);


            int count = 0;
            while (result.next()) {
                count++;
                String foundWord = result.getString("word").toLowerCase();


                // reset scrabble value
                int scrabbleValue = 0;

                for(int i=0; i<foundWord.length(); i++){
                    //sqlQuery = sqlQuery +  "word LIKE '%" + array[i] + "%' AND ";
                    scrabbleValue = scrabbleValue + charScrabbleValue(Character.toString(foundWord.charAt(i)));
                }


                // add found row to table
                model.addRow(new Object[]{count, foundWord.toUpperCase(), scrabbleValue});
            }

            con.close();

        } catch (Exception ee) {
            ee.printStackTrace(System.out);
        }
    }


}


