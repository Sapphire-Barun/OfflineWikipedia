import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Central management facade for the library system.
 * Coordinates catalog inventory, member registries, and executes validated lending transactions.
 */
public class Library {

    private List<Book> catalog;
    private List<Member> members;
    private List<LendingRecord> lendingHistory;
    private int nextRecordId = 1;

    // Default constructor to initialize an empty library with active tracking collections
    public Library() {
        this.catalog = new ArrayList<>();
        this.members = new ArrayList<>();
        this.lendingHistory = new ArrayList<>();
    }

    // Optional constructor allowing pre-populated collections
    public Library(List<Book> catalog, List<Member> members) {
        this.catalog = catalog != null ? catalog : new ArrayList<>();
        this.members = members != null ? members : new ArrayList<>();
        this.lendingHistory = new ArrayList<>();
    }

    public void addBook(Book book) {
        catalog.add(book);
    }

    public void registerMember(Member member) {
        members.add(member);
    }

    public Book searchBookByIsbn(String isbn) {
        for (Book item : catalog) {
            if (item.getIsbn().equals(isbn)) {
                return item;
            }
        }
        return null;
    }

    public Member findMemberById(int id) {
        for (Member item : members) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    /**
     * Executes a book checkout transaction after validating key business rules:
     * 1. Book must exist in catalog.
     * 2. Member must be registered in the system.
     * 3. Member account must not be CLOSED.
     * 4. Book must not already be checked out (BORROWED).
     */
    public void issueBook(String isbn, int memberId) {
        Book book = searchBookByIsbn(isbn);
        if (book == null) {
            System.out.println("Cannot issue: Book with ISBN " + isbn + " not found!");
            return;
        }

        Member member = findMemberById(memberId);
        if (member == null) {
            System.out.println("Cannot issue: Member with ID " + memberId + " not registered!");
            return;
        }

        if (member.getStatus() == AccountStatus.CLOSED) {
            System.out.println("Cannot issue book: Account for member " + member.getName() + " is closed.");
            return;
        }

        if (book.getStatus() == BookStatus.BORROWED) {
            System.out.println("Cannot issue book: '" + book.getTitle() + "' is already borrowed.");
            return;
        }

        // Delegate state changes to the member
        member.borrowBook(book);

        // Record the transaction in the central audit history
        LendingRecord record = new LendingRecord(nextRecordId++, book, member);
        lendingHistory.add(record);

        System.out.println("Success: '" + book.getTitle() + "' issued to " + member.getName() 
                + " (Record ID: " + record.getRecordId() + ", Due Date: " + record.getDueDate() + ").");
    }

    /**
     * Processes a book return transaction after verifying:
     * 1. Book and Member exist.
     * 2. The member actually has the book in their active borrowed list.
     */
    public void returnBook(String isbn, int memberId) {
        Book book = searchBookByIsbn(isbn);
        if (book == null) {
            System.out.println("Cannot return: Book with ISBN " + isbn + " not found!");
            return;
        }

        Member member = findMemberById(memberId);
        if (member == null) {
            System.out.println("Cannot return: Member with ID " + memberId + " not registered!");
            return;
        }

        if (!member.getBorrowedBooks().contains(book)) {
            System.out.println("Cannot return: " + member.getName() + " does not have '" + book.getTitle() + "' checked out.");
            return;
        }

        // Delegate state changes to the member
        member.returnBook(book);

        // Find the open lending record and stamp return timestamp
        for (LendingRecord record : lendingHistory) {
            if (record.getBook().getIsbn().equals(isbn) 
                    && record.getMember().getId() == memberId 
                    && record.getReturnDate() == null) {
                record.markReturned(LocalDate.now());
                break;
            }
        }

        System.out.println("Success: '" + book.getTitle() + "' returned by " + member.getName() + ".");
    }

    public List<Book> getCatalog() {
        return catalog;
    }

    public List<Member> getMembers() {
        return members;
    }

    public List<LendingRecord> getLendingHistory() {
        return lendingHistory;
    }
}
