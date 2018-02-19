import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main extends JFrame {

    // declaring the SWING components
    private JPanel panel1;
    private JTextField searchText;
    private JTable table;
    private JButton searchButton;
    private JScrollPane scroller;

    public Main() {
        // Allow maximum 8 characters in the search field
        searchText.setDocument(new JTextFieldLimit(8));

        // Listen to Search Button and execute query
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

    public static void main(String[] args) {
        // Setup the form details
        JFrame frame = new JFrame("Java Scrabble Solver by Jozef & Elvina (UOL)");
        frame.setContentPane(new Main().panel1);
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
    }

    // Calculate Scrabble value of each character
    public int charScrabbleValue(String chr) {
        /* ENGLISH SCRABBLE VALUES
        1 point: E ×12, A ×9, I ×9, O ×8, N ×6, R ×6, T ×6, L ×4, S ×4, U ×4
        2 points: D ×4, G ×3
        3 points: B ×2, C ×2, M ×2, P ×2
        4 points: F ×2, H ×2, V ×2, W ×2, Y ×2
        5 points: K ×1
        8 points: J ×1, X ×1
        10 points: Q ×1, Z ×1
        */
        int charValue = 0;

        if ((chr.contains("a")) || (chr.contains("e")) || (chr.contains("i")) || (chr.contains("o")) || (chr.contains("n"))
            || (chr.contains("r")) || (chr.contains("t")) || (chr.contains("l")) || (chr.contains("s")) || (chr.contains("u"))
            ) {
            charValue = 1;
        }
        if ((chr.contains("d")) || (chr.contains("g"))) {
            charValue = 2;
        }
        if ((chr.contains("b")) || (chr.contains("c")) || (chr.contains("m")) || (chr.contains("p"))) {
            charValue = 3;
        }
        if ((chr.contains("f")) || (chr.contains("h")) || (chr.contains("v")) || (chr.contains("w")) || (chr.contains("y"))
            ) {
            charValue = 4;
        }
        if ((chr.contains("k"))) {
            charValue = 5;
        }
        if ((chr.contains("j")) || (chr.contains("x"))) {
            charValue = 8;
        }
        if ((chr.contains("q")) || (chr.contains("z"))) {
            charValue = 10;
        }

        return charValue;
    }

    // Build the SQL query based on entered characters
    public String createSqlQuery(String enteredWord) {
        String alphabet = "abcdefghijklmnopqrstuvwxyz";

        // split entered string into letters
        String[] array = enteredWord.split("(?!^)");
        // get lenght of entered letters
        int arrLen = array.length;

        String sqlQuery = "SELECT DISTINCT WORD,LENGTH(WORD) FROM en_aspell WHERE (";
        //this SQL query file
        //word must be more than 2
        //word entered less than one results to a numeric exception
        sqlQuery = sqlQuery + "  LENGTH(WORD) BETWEEN 2  AND 8 AND ";
        for (int i = 0; i < arrLen; i++) {
            sqlQuery = sqlQuery + "LOWER(WORD) LIKE '%" + array[i] + "%' AND ";
            sqlQuery = sqlQuery + "LOWER(WORD) LIKE '%" + array[i] + "%' OR ";
            // remove used letters from alphabet string
            alphabet = alphabet.replace(array[i], "");
        }
        sqlQuery = sqlQuery.replaceFirst(" OR $", "");
        sqlQuery = sqlQuery + " AND LENGTH(WORD)<=" + arrLen + ") AND (";
        for (int i = 0; i < alphabet.length(); i++) {
            sqlQuery = sqlQuery + "  LENGTH(WORD) BETWEEN 2 AND " + arrLen + " AND ";
            sqlQuery = sqlQuery + "  LENGTH(WORD) > 1 and LENGTH(WORD)<=8 AND ";
            sqlQuery = sqlQuery + "LOWER(WORD) NOT LIKE '%" + alphabet.charAt(i) + "%' AND ";
            sqlQuery = sqlQuery + "LOWER(WORD) NOT LIKE '" + alphabet.charAt(i) + "%' AND ";
            sqlQuery = sqlQuery + "LOWER(WORD) NOT LIKE '%" + alphabet.charAt(i) + "' AND ";
            sqlQuery = sqlQuery + "LOWER(WORD) NOT LIKE '%''%' AND ";
        }
        sqlQuery = sqlQuery.replaceFirst(" AND $", "");

        // make sure repeating letters are included with their exact count
        for (int i = 0; i < arrLen; i++) {
            int counter = enteredWord.split(array[i], -1).length - 1;
            sqlQuery = sqlQuery + " AND Char_length(LOWER(WORD)) - Char_length(REPLACE(LOWER(WORD), '" + array[i] + "', '')) <= " + counter;
        }

        // order results by longest key (not really necessary, but it looks better)
        sqlQuery = sqlQuery + ") GROUP BY WORD ORDER BY LENGTH(WORD) DESC";
        return sqlQuery;
    }

    public void dbQuery() {

        try {
            // Connect to DB
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(
                "jdbc:mysql://85.10.205.173:3307/englishwords?autoReconnect=true&useSSL=false", "jozef", "elvina");
            Statement stmt = con.createStatement();

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

            // Run SQL query
            ResultSet result = stmt.executeQuery(sqlQuery);

            int count = 0;
            while (result.next()) {
                count++;
                String foundWord = result.getString("word").toLowerCase();

                // reset scrabble value
                int scrabbleValue = 0;

                // calculate scrabble value for each letter in the word found in the db
                for (int i = 0; i < foundWord.length(); i++) {
                    scrabbleValue = scrabbleValue + charScrabbleValue(Character.toString(foundWord.charAt(i)));
                }

                // add found row to table
                model.addRow(new Object[]{count, foundWord.toUpperCase(), scrabbleValue});
            }

            con.close();

            //database exception
        } catch (Exception ee) {
            ee.printStackTrace(System.out);
        }
    }

    // Create Limit on Number of Letters Typed, we need to add a new functionality into JTextField, as it's not there
    public class JTextFieldLimit extends PlainDocument {
        private int limit;
        JTextFieldLimit(int limit) {
            super();
            this.limit = limit;
        }
        public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
            if (str == null) return;
            if ((getLength() + str.length()) <= limit) {
                super.insertString(offset, str, attr);
            }
        }
    }


}


