
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

// Create Limit on Number of Letters Typed, we need to add a new functionality into JTextField, as it's not there
public class ScrabbleTextLimit extends PlainDocument {
    private int limit;
    ScrabbleTextLimit(int limit) {
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