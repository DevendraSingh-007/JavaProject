/*
DigitalLibrarySystem.java
Single-file Java application (OOP + Swing + file-based persistence)

Features:
 - Login & Registration (Admin & Student)
 - Admin Panel: Manage Books, Users, Issued History
 - Student Panel: Browse, Search, Borrow, Return, Borrowed History
 - Persistent storage: books.data, users.data, history.data
 - Clean, semi-transparent Swing UI with background image

Compile & Run:
    javac DigitalLibrarySystem_Modified.java
    java Library2
*/

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/* ------------------------
   MAIN ENTRY POINT
   ------------------------ */
public class Library3 {
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new LoginFrame(new LibraryDatabase()));
    }
}

/* ------------------------
   MODEL CLASSES
   ------------------------ */
class Book implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id, title, author;
    private int totalCopies, availableCopies;

    public Book(String id, String title, String author, int copies) {
        this.id = id; this.title = title; this.author = author;
        this.totalCopies = copies; this.availableCopies = copies;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }

    public void setTitle(String t) { title = t; }
    public void setAuthor(String a) { author = a; }
    public void setTotalCopies(int c) {
        int diff = c - this.totalCopies;
        this.totalCopies = c;
        this.availableCopies += diff;
        if (this.availableCopies < 0) this.availableCopies = 0;
    }

    public boolean borrow() {
        if (availableCopies > 0) { availableCopies--; return true; }
        return false;
    }

    public boolean giveBack() {
        if (availableCopies < totalCopies) { availableCopies++; return true; }
        return false;
    }
}

abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String username, password, name;
    public User(String u, String p, String n) { username=u; password=p; name=n; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public void setPassword(String p) { password = p; }
    public void setName(String n) { name = n; }
}

class Student extends User {
    private static final long serialVersionUID = 1L;
    private List<String> borrowedBookIds = new ArrayList<>();
    public Student(String u, String p, String n) { super(u,p,n); }
    public List<String> getBorrowedBookIds() { return borrowedBookIds; }
    public void borrowBook(String id) { borrowedBookIds.add(id); }
    public void returnBook(String id) { borrowedBookIds.remove(id); }
}

class Admin extends User {
    private static final long serialVersionUID = 1L;
    public Admin(String u, String p, String n) { super(u,p,n); }
}

/* ------------------------
   TRANSACTION MODEL
   ------------------------ */
class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    String username, bookTitle, action, date;
    public Transaction(String username, String bookTitle, String action) {
        this.username = username;
        this.bookTitle = bookTitle;
        this.action = action;
        this.date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
    }
    public String getUsername() { return username; }
    public String getBookTitle() { return bookTitle; }
    public String getAction() { return action; }
    public String getDate() { return date; }
}

/* ------------------------
   DATABASE (file-based persistence)
   ------------------------ */
class LibraryDatabase {
    private static final String BOOKS_FILE = "books.data";
    private static final String USERS_FILE = "users.data";
    private static final String HISTORY_FILE = "history.data";

    private Map<String, Book> books = new LinkedHashMap<>();
    private Map<String, User> users = new LinkedHashMap<>();
    private List<Transaction> history = new ArrayList<>();

    public LibraryDatabase() { load(); }

    @SuppressWarnings("unchecked")
    private void load() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(BOOKS_FILE))) {
            Object o = ois.readObject();
            if (o instanceof Map) books = (Map<String, Book>) o;
        } catch (Exception e) { seedBooks(); saveBooks(); }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USERS_FILE))) {
            Object o = ois.readObject();
            if (o instanceof Map) users = (Map<String, User>) o;
        } catch (Exception e) { seedUsers(); saveUsers(); }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(HISTORY_FILE))) {
            Object o = ois.readObject();
            if (o instanceof List) history = (List<Transaction>) o;
        } catch (Exception e) { history = new ArrayList<>(); saveHistory(); }
    }

    public synchronized void save() { saveBooks(); saveUsers(); saveHistory(); }

    private void saveBooks() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BOOKS_FILE))) {
            oos.writeObject(books);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void saveHistory() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(HISTORY_FILE))) {
            oos.writeObject(history);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public synchronized void addTransaction(Transaction t) {
        history.add(t);
        saveHistory();
    }

    public List<Transaction> getAllTransactions() { return history; }

    // Books
    public Collection<Book> getAllBooks() { return books.values(); }
    public Book getBook(String id) { return books.get(id); }
    public void addOrUpdateBook(Book b) { books.put(b.getId(), b); saveBooks(); }
    public void removeBook(String id) { books.remove(id); saveBooks(); }

    // Users
    public Collection<User> getAllUsers() { return users.values(); }
    public User getUser(String username) { return users.get(username); }
    public void addUser(User u) { users.put(u.getUsername(), u); saveUsers(); }
    public void removeUser(String username) { users.remove(username); saveUsers(); }

    /* --- 20 Default Books --- */
    private void seedBooks() {
        addOrUpdateBook(new Book("B001", "Clean Code", "Robert C. Martin", 4));
        addOrUpdateBook(new Book("B002", "Effective Java", "Joshua Bloch", 3));
        addOrUpdateBook(new Book("B003", "Design Patterns", "Erich Gamma", 3));
        addOrUpdateBook(new Book("B004", "Introduction to Algorithms", "Thomas H. Cormen", 5));
        addOrUpdateBook(new Book("B005", "The Pragmatic Programmer", "Andrew Hunt", 4));
        addOrUpdateBook(new Book("B006", "Artificial Intelligence: A Modern Approach", "Stuart Russell", 3));
        addOrUpdateBook(new Book("B007", "Operating System Concepts", "Abraham Silberschatz", 4));
        addOrUpdateBook(new Book("B008", "Computer Networks", "Andrew S. Tanenbaum", 4));
        addOrUpdateBook(new Book("B009", "Database System Concepts", "Henry F. Korth", 4));
        addOrUpdateBook(new Book("B010", "Python Crash Course", "Eric Matthes", 5));
        addOrUpdateBook(new Book("B011", "Head First Java", "Kathy Sierra", 5));
        addOrUpdateBook(new Book("B012", "C Programming Language", "Brian W. Kernighan", 4));
        addOrUpdateBook(new Book("B013", "JavaScript: The Good Parts", "Douglas Crockford", 3));
        addOrUpdateBook(new Book("B014", "You Don’t Know JS", "Kyle Simpson", 3));
        addOrUpdateBook(new Book("B015", "Deep Learning", "Ian Goodfellow", 2));
        addOrUpdateBook(new Book("B016", "Machine Learning Yearning", "Andrew Ng", 3));
        addOrUpdateBook(new Book("B017", "Introduction to Machine Learning", "Ethem Alpaydin", 3));
        addOrUpdateBook(new Book("B018", "Data Structures & Algorithms Made Easy", "Narasimha Karumanchi", 5));
        addOrUpdateBook(new Book("B019", "Modern Operating Systems", "Andrew S. Tanenbaum", 3));
        addOrUpdateBook(new Book("B020", "System Design Interview", "Alex Xu", 4));
    }

    private void seedUsers() {
        addUser(new Admin("admin", "admin", "Library Admin"));
        addUser(new Student("student1", "pass", "Student One"));
    }
}

/* ------------------------
   BACKGROUND PANEL
   ------------------------ */
class BackgroundPanel extends JPanel {
    private Image backgroundImage;
    public BackgroundPanel(String imageUrl) {
        try { backgroundImage = new ImageIcon(new URL(imageUrl)).getImage(); }
        catch (Exception e) { backgroundImage = null; }
        setLayout(new GridBagLayout());
    }
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0, 0, 0, 60));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        } else setBackground(Color.LIGHT_GRAY);
    }
}

/* ------------------------
   LOGIN FRAME
   ------------------------ */
class LoginFrame extends JFrame {
    private final LibraryDatabase db;
    private static final String BG_URL = "https://images.unsplash.com/photo-1524995997946-a1c2e315a42f";

    public LoginFrame(LibraryDatabase db) {
        this.db = db;
        setTitle("Digital Library — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(780, 520);
        setLocationRelativeTo(null);

        BackgroundPanel bg = new BackgroundPanel(BG_URL);
        setContentPane(bg);

        JPanel card = new JPanel();
        card.setOpaque(true);
        card.setBackground(new Color(255,255,255,220));
        card.setBorder(new CompoundBorder(new LineBorder(new Color(200,200,200),1,true), new EmptyBorder(25,36,25,36)));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(520,420));

        JLabel title = new JLabel("📚 Digital Library", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(new Color(24,78,120));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JButton loginBtn = new JButton("Login");
        JButton regBtn = new JButton("Register");

        stylePrimaryButton(loginBtn);
        styleSecondaryButton(regBtn);

        JLabel info = new JLabel("Admin: admin/admin • Demo: student1/pass", SwingConstants.CENTER);
        info.setForeground(new Color(80,80,80));
        info.setFont(new Font("SansSerif", Font.PLAIN, 12));

        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0,18)));
        card.add(new JLabel("Username:"));
        card.add(userField);
        card.add(new JLabel("Password:"));
        card.add(passField);
        card.add(Box.createRigidArea(new Dimension(0,15)));
        card.add(loginBtn);
        card.add(Box.createRigidArea(new Dimension(0,8)));
        card.add(regBtn);
        card.add(Box.createRigidArea(new Dimension(0,10)));
        card.add(info);

        bg.add(card);

        loginBtn.addActionListener(e -> {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword());
            User user = db.getUser(u);
            if (user != null && user.getPassword().equals(p)) {
                dispose();
                if (user instanceof Admin) new AdminPanel(db, (Admin) user, BG_URL);
                else new StudentPanel(db, (Student) user, BG_URL);
            } else JOptionPane.showMessageDialog(this, "Invalid credentials!");
        });

        regBtn.addActionListener(e -> new RegisterDialog(this, db, BG_URL));

        setVisible(true);
    }

    private void stylePrimaryButton(JButton b) {
        b.setBackground(new Color(22,78,120));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 15));
        b.setFocusPainted(false);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setBorder(new EmptyBorder(8,16,8,16));
    }

    private void styleSecondaryButton(JButton b) {
        b.setBackground(Color.WHITE);
        b.setForeground(new Color(22,78,120));
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setBorder(new CompoundBorder(new LineBorder(new Color(22,78,120),1,true), new EmptyBorder(6,14,6,14)));
    }
}

/* ------------------------
   REGISTER DIALOG
   ------------------------ */
class RegisterDialog extends JDialog {
    public RegisterDialog(JFrame owner, LibraryDatabase db, String bgUrl) {
        super(owner, "Register User", true);
        setSize(580, 460);
        setLocationRelativeTo(owner);
        BackgroundPanel bg = new BackgroundPanel(bgUrl);
        setContentPane(bg);

        JPanel form = new JPanel();
        form.setOpaque(true);
        form.setBackground(new Color(255,255,255,230));
        form.setBorder(new CompoundBorder(new LineBorder(new Color(200,200,200),1,true), new EmptyBorder(22,30,22,30)));
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel head = new JLabel("📝 Register New User", SwingConstants.CENTER);
        head.setFont(new Font("SansSerif", Font.BOLD, 22));
        head.setForeground(new Color(22,78,120));
        head.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField nameF = new JTextField();
        JTextField userF = new JTextField();
        JPasswordField passF = new JPasswordField();
        JComboBox<String> role = new JComboBox<>(new String[]{"Student","Admin"});
        JButton regBtn = new JButton("Register");
        regBtn.setBackground(new Color(22,78,120)); regBtn.setForeground(Color.WHITE);

        form.add(head);
        form.add(Box.createRigidArea(new Dimension(0,20)));
        form.add(new JLabel("Full Name:")); form.add(nameF);
        form.add(new JLabel("Username:")); form.add(userF);
        form.add(new JLabel("Password:")); form.add(passF);
        form.add(new JLabel("Role:")); form.add(role);
        form.add(Box.createRigidArea(new Dimension(0,10)));
        form.add(regBtn);
        bg.add(form);

        regBtn.addActionListener(e -> {
            String n=nameF.getText().trim(), u=userF.getText().trim(), p=new String(passF.getPassword());
            if(n.isEmpty()||u.isEmpty()||p.isEmpty()){JOptionPane.showMessageDialog(this,"Fill all fields!");return;}
            if(db.getUser(u)!=null){JOptionPane.showMessageDialog(this,"Username exists!");return;}
            if(role.getSelectedItem().equals("Admin")) db.addUser(new Admin(u,p,n)); else db.addUser(new Student(u,p,n));
            JOptionPane.showMessageDialog(this,"Registered successfully!");
            dispose();
        });

        setVisible(true);
    }
}

/* ------------------------
   ADMIN PANEL
   ------------------------ */
class AdminPanel extends JFrame {
    private final LibraryDatabase db;
    private final Admin admin;
    private final String bgUrl;
    private DefaultTableModel booksModel, usersModel;

    public AdminPanel(LibraryDatabase db, Admin admin, String bgUrl) {
        this.db=db; this.admin=admin; this.bgUrl=bgUrl;
        setTitle("Admin Dashboard — "+admin.getName());
        setSize(1000,620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BackgroundPanel bg = new BackgroundPanel(bgUrl);
        setContentPane(bg);

        JPanel overlay=new JPanel(new BorderLayout());
        overlay.setOpaque(false);
        overlay.setBorder(new EmptyBorder(16,16,16,16));
        JPanel top=new JPanel(new BorderLayout()); top.setOpaque(false);

        JLabel head=new JLabel("🛠️ Admin Dashboard",SwingConstants.LEFT);
        head.setFont(new Font("SansSerif",Font.BOLD,22)); head.setForeground(Color.WHITE);

        JPanel btnPanel=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,4));
        btnPanel.setOpaque(false);
        JButton add=btn("➕ Add Book"),edit=btn("✏️ Edit Book"),del=btn("🗑 Delete"),refresh=btn("⟳ Refresh"),save=btn("💾 Save"),users=btn("👥 View Users"),history=btn("📜 Issued History");
        btnPanel.add(add);btnPanel.add(edit);btnPanel.add(del);btnPanel.add(users);btnPanel.add(refresh);btnPanel.add(save);btnPanel.add(history);
        top.add(head,BorderLayout.WEST);top.add(btnPanel,BorderLayout.EAST);
        overlay.add(top,BorderLayout.NORTH);

        JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);split.setResizeWeight(0.6);
        booksModel=new DefaultTableModel(new Object[]{"ID","Title","Author","Avail","Total"},0){public boolean isCellEditable(int r,int c){return false;}};
        JTable booksTable=new JTable(booksModel);
        JPanel left=createCard("Books"); left.add(new JScrollPane(booksTable)); split.setLeftComponent(left);

        usersModel=new DefaultTableModel(new Object[]{"Username","Name","Role"},0){public boolean isCellEditable(int r,int c){return false;}};
        JTable usersTable=new JTable(usersModel);
        JPanel right=createCard("Users"); right.add(new JScrollPane(usersTable)); split.setRightComponent(right);
        overlay.add(split,BorderLayout.CENTER);

        JButton delUser=btn("🗑 Delete User");
        JPanel uPanel=new JPanel(new FlowLayout(FlowLayout.RIGHT)); uPanel.setOpaque(false); uPanel.add(delUser);
        right.add(uPanel,BorderLayout.SOUTH);

        bg.add(overlay);
        refreshTables();

        add.addActionListener(e->editBook(null));
        edit.addActionListener(e->{int r=booksTable.getSelectedRow();if(r<0)return;String id=(String)booksModel.getValueAt(r,0);editBook(db.getBook(id));});
        del.addActionListener(e->{int r=booksTable.getSelectedRow();if(r<0)return;String id=(String)booksModel.getValueAt(r,0);if(JOptionPane.showConfirmDialog(this,"Delete "+id+"?","Confirm",2)==0){db.removeBook(id);refreshTables();}});
        delUser.addActionListener(e->{int r=usersTable.getSelectedRow();if(r<0)return;String u=(String)usersModel.getValueAt(r,0);if("admin".equals(u)){JOptionPane.showMessageDialog(this,"Cannot delete admin!");return;}if(JOptionPane.showConfirmDialog(this,"Delete "+u+"?","Confirm",2)==0){db.removeUser(u);refreshTables();}});
        refresh.addActionListener(e->refreshTables());
        save.addActionListener(e->{db.save();JOptionPane.showMessageDialog(this,"Saved!");});
        history.addActionListener(e->new HistoryFrame(db,null,true));

        JButton logout=btn("🚪 Logout"); logout.setBackground(new Color(220,53,69)); JPanel lp=new JPanel(new FlowLayout(FlowLayout.RIGHT)); lp.setOpaque(false); lp.add(logout); overlay.add(lp,BorderLayout.SOUTH);
        logout.addActionListener(e->{if(JOptionPane.showConfirmDialog(this,"Logout?","Confirm",2)==0){dispose();new LoginFrame(db);}});
        setVisible(true);
    }

    private JButton btn(String t){JButton b=new JButton(t);b.setBackground(new Color(22,78,120));b.setForeground(Color.WHITE);b.setFont(new Font("SansSerif",Font.BOLD,13));b.setFocusPainted(false);return b;}
    private JPanel createCard(String t){JPanel c=new JPanel(new BorderLayout());c.setOpaque(true);c.setBackground(new Color(255,255,255,235));c.setBorder(new CompoundBorder(new LineBorder(new Color(220,220,220),1,true),new EmptyBorder(8,8,8,8)));JLabel l=new JLabel(" "+t);l.setFont(new Font("SansSerif",Font.BOLD,14));c.add(l,BorderLayout.NORTH);return c;}
    private void refreshTables(){booksModel.setRowCount(0);for(Book b:db.getAllBooks())booksModel.addRow(new Object[]{b.getId(),b.getTitle(),b.getAuthor(),b.getAvailableCopies(),b.getTotalCopies()});usersModel.setRowCount(0);for(User u:db.getAllUsers())usersModel.addRow(new Object[]{u.getUsername(),u.getName(),(u instanceof Admin)?"Admin":"Student"});}
    private void editBook(Book ex){JPanel p=new JPanel(new GridLayout(0,2,6,6));JTextField id=new JTextField(ex==null?"":ex.getId()),t=new JTextField(ex==null?"":ex.getTitle()),a=new JTextField(ex==null?"":ex.getAuthor());JSpinner c=new JSpinner(new SpinnerNumberModel(ex==null?1:ex.getTotalCopies(),1,1000,1));if(ex!=null)id.setEnabled(false);p.add(new JLabel("ID"));p.add(id);p.add(new JLabel("Title"));p.add(t);p.add(new JLabel("Author"));p.add(a);p.add(new JLabel("Copies"));p.add(c);if(JOptionPane.showConfirmDialog(this,p,ex==null?"Add Book":"Edit Book",2)==0){if(id.getText().trim().isEmpty())return;if(ex==null){if(db.getBook(id.getText())!=null){JOptionPane.showMessageDialog(this,"Book exists!");return;}db.addOrUpdateBook(new Book(id.getText(),t.getText(),a.getText(),(Integer)c.getValue()));}else{ex.setTitle(t.getText());ex.setAuthor(a.getText());ex.setTotalCopies((Integer)c.getValue());db.addOrUpdateBook(ex);}refreshTables();}}
}

/* ------------------------
   STUDENT PANEL (modified)
   - Removed the visible "My Borrowed Books" list from main UI (as requested)
   - Added Return and Logout buttons to the top control bar (so they appear beside Search/Borrow/etc.)
   - Return opens a dialog listing the student's borrowed books to pick and return
   ------------------------ */
class StudentPanel extends JFrame {
    private final LibraryDatabase db; private final Student s;
    private DefaultTableModel allModel;
    public StudentPanel(LibraryDatabase db, Student s, String bgUrl){
        this.db=db;this.s=s;
        setTitle("Student Portal — "+s.getName());
        setSize(900,600);setLocationRelativeTo(null);setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BackgroundPanel bg=new BackgroundPanel(bgUrl);setContentPane(bg);
        JPanel overlay=new JPanel(new BorderLayout());overlay.setOpaque(false);overlay.setBorder(new EmptyBorder(12,12,12,12));
        JLabel h=new JLabel("👋 Welcome, "+s.getName());h.setFont(new Font("SansSerif",Font.BOLD,22));h.setForeground(Color.WHITE);overlay.add(h,BorderLayout.NORTH);

        JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);split.setResizeWeight(1.0); // take full width with left component

        allModel=new DefaultTableModel(new Object[]{"ID","Title","Author","Avail","Total"},0){public boolean isCellEditable(int r,int c){return false;}};JTable all=new JTable(allModel);
        JPanel left=createCard("All Books"); JPanel sp=new JPanel(new FlowLayout(FlowLayout.LEFT,6,6));sp.setOpaque(false);
        JTextField q=new JTextField(20);
        JButton search=acc("Search"), borrow=pri("📘 Borrow"), refresh=acc("⟳ Refresh"), history=acc("📜 Borrowed History");
        JButton ret=pri("📗 Return"), logout=pri("🚪 Logout");
        // Move Return and Logout to the top control bar (different place than before)
        sp.add(new JLabel("Search:"));sp.add(q);sp.add(search);sp.add(borrow);sp.add(refresh);sp.add(history);sp.add(ret);sp.add(logout);
        left.add(sp,BorderLayout.NORTH);left.add(new JScrollPane(all)); split.setLeftComponent(left);

        // Right side intentionally left empty / collapsed to "delete my borrowed books" from visible panel
        JPanel empty=new JPanel(); empty.setOpaque(false); split.setRightComponent(empty);

        overlay.add(split);bg.add(overlay);
        refreshAll();

        borrow.addActionListener(e->{int r=all.getSelectedRow();if(r<0){JOptionPane.showMessageDialog(this,"Select a book to borrow");return;}String id=(String)allModel.getValueAt(r,0);Book b=db.getBook(id);if(b==null){JOptionPane.showMessageDialog(this,"Book not found");return;}if(b.borrow()){s.borrowBook(id);db.addTransaction(new Transaction(s.getUsername(),b.getTitle(),"Borrowed"));db.save();refreshAll();JOptionPane.showMessageDialog(this,"Borrowed!");}else{JOptionPane.showMessageDialog(this,"No copies available");}});

        // Return button now opens a dialog listing borrowed books for selection
        ret.addActionListener(e->{
            List<String> ids = s.getBorrowedBookIds();
            if(ids.isEmpty()){ JOptionPane.showMessageDialog(this, "You have no borrowed books."); return; }
            // build display array
            String[] options = new String[ids.size()];
            Map<String,String> idMap = new HashMap<>();
            for(int i=0;i<ids.size();i++){ Book b = db.getBook(ids.get(i)); String disp = ids.get(i) + " - " + (b!=null?b.getTitle():"(unknown)"); options[i]=disp; idMap.put(disp, ids.get(i)); }
            String sel = (String) JOptionPane.showInputDialog(this, "Select a book to return:", "Return Book", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if(sel!=null){ String id = idMap.get(sel); Book b = db.getBook(id); if(b!=null && b.giveBack()){ s.returnBook(id); db.addTransaction(new Transaction(s.getUsername(), b.getTitle(), "Returned")); db.save(); refreshAll(); JOptionPane.showMessageDialog(this, "Returned!"); } else { JOptionPane.showMessageDialog(this, "Unable to return (book record missing or max copies reached)."); } }
        });

        search.addActionListener(e->filter(q.getText().trim().toLowerCase()));
        refresh.addActionListener(e->refreshAll());
        history.addActionListener(e->new HistoryFrame(db,s.getUsername(),false));

        logout.addActionListener(e->{ if(JOptionPane.showConfirmDialog(this,"Logout?","Confirm",2)==0){ dispose(); new LoginFrame(db); } });

        setVisible(true);
    }
    private JPanel createCard(String t){JPanel c=new JPanel(new BorderLayout());c.setOpaque(true);c.setBackground(new Color(255,255,255,235));c.setBorder(new CompoundBorder(new LineBorder(new Color(220,220,220),1,true),new EmptyBorder(8,8,8,8)));JLabel l=new JLabel(" "+t);l.setFont(new Font("SansSerif",Font.BOLD,14));c.add(l,BorderLayout.NORTH);return c;}
    private JButton pri(String t){JButton b=new JButton(t);b.setBackground(new Color(22,78,120));b.setForeground(Color.WHITE);b.setFont(new Font("SansSerif",Font.BOLD,13));b.setFocusPainted(false);return b;}
    private JButton acc(String t){JButton b=new JButton(t);b.setBackground(new Color(245,245,245));b.setForeground(new Color(22,78,120));b.setFocusPainted(false);b.setBorder(new CompoundBorder(new LineBorder(new Color(220,220,220),1,true),new EmptyBorder(4,8,4,8)));return b;}
    private void refreshAll(){ allModel.setRowCount(0); for(Book b:db.getAllBooks()) allModel.addRow(new Object[]{b.getId(),b.getTitle(),b.getAuthor(),b.getAvailableCopies(),b.getTotalCopies()}); }
    private void filter(String q){ allModel.setRowCount(0); for(Book b:db.getAllBooks()){ if(b.getTitle().toLowerCase().contains(q)||b.getAuthor().toLowerCase().contains(q)||b.getId().toLowerCase().contains(q)) allModel.addRow(new Object[]{b.getId(),b.getTitle(),b.getAuthor(),b.getAvailableCopies(),b.getTotalCopies()}); } }
}

/* ------------------------
   HISTORY FRAME
   ------------------------ */
class HistoryFrame extends JFrame {
    public HistoryFrame(LibraryDatabase db, String user, boolean admin) {
        setTitle(admin?"📜 Issued History":"📜 Borrowed History");
        setSize(700,400);
        setLocationRelativeTo(null);
        DefaultTableModel m=new DefaultTableModel(new Object[]{"Username","Book Title","Action","Date"},0){public boolean isCellEditable(int r,int c){return false;}};
        JTable t=new JTable(m);t.setRowHeight(24);
        for(Transaction tr:db.getAllTransactions()) if(admin||tr.getUsername().equals(user)) m.addRow(new Object[]{tr.getUsername(),tr.getBookTitle(),tr.getAction(),tr.getDate()});
        add(new JScrollPane(t));setVisible(true);
    }
}
