import java.util.Scanner;

/**
 * Interactive menu-driven console application for the Library Management System.
 * Accepts user input via console and executes transactions, catalog updates,
 * member registrations, and audit inspections using a switch-case control structure.
 */
public class App {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Library library = new Library();

        // Pre-load seed data so the system is immediately usable for testing
        seedInitialData(library);

        boolean running = true;

        System.out.println("==================================================");
        System.out.println("   WELCOME TO THE LIBRARY MANAGEMENT SYSTEM       ");
        System.out.println("==================================================");

        while (running) {
            displayMenu();
            System.out.print("Enter your choice (1-9): ");
            String choice = scanner.nextLine().trim();
            System.out.println();

            switch (choice) {
                case "1":
                    viewAllBooks(library);
                    break;

                case "2":
                    searchBookByIsbn(scanner, library);
                    break;

                case "3":
                    issueBook(scanner, library);
                    break;

                case "4":
                    returnBook(scanner, library);
                    break;

                case "5":
                    viewMemberDetails(scanner, library);
                    break;

                case "6":
                    registerNewMember(scanner, library);
                    break;

                case "7":
                    addNewBook(scanner, library);
                    break;

                case "8":
                    viewLendingHistory(library);
                    break;

                case "9":
                    System.out.println("Thank you for using the Library Management System. Goodbye!");
                    running = false;
                    break;

                default:
                    System.out.println("Invalid option! Please enter a number from 1 to 9.");
                    break;
            }

            System.out.println();
        }

        scanner.close();
    }

    /**
     * Prints the interactive menu options to the console.
     */
    private static void displayMenu() {
        System.out.println("----------------- MAIN MENU -----------------");
        System.out.println("1. View All Books in Catalog");
        System.out.println("2. Search Book by ISBN");
        System.out.println("3. Borrow / Issue a Book");
        System.out.println("4. Return a Book");
        System.out.println("5. View Member Details & Borrowed Books");
        System.out.println("6. Register a New Member");
        System.out.println("7. Add a New Book to Catalog");
        System.out.println("8. View Lending History (Audit Log)");
        System.out.println("9. Exit");
        System.out.println("---------------------------------------------");
    }

    /**
     * Displays all books currently cataloged in the library.
     */
    private static void viewAllBooks(Library library) {
        System.out.println("--- Library Catalog ---");
        if (library.getCatalog().isEmpty()) {
            System.out.println("The catalog is currently empty.");
            return;
        }
        for (Book book : library.getCatalog()) {
            System.out.println("- " + book);
        }
    }

    /**
     * Searches the catalog for a specific book using an ISBN entered by the user.
     */
    private static void searchBookByIsbn(Scanner scanner, Library library) {
        System.out.print("Enter Book ISBN to search: ");
        String isbn = scanner.nextLine().trim();

        Book found = library.searchBookByIsbn(isbn);
        if (found != null) {
            System.out.println("\n[Found Book Details]");
            System.out.println("Title: " + found.getTitle());
            System.out.println("Author: " + found.getAuthor());
            System.out.println("ISBN: " + found.getIsbn());
            System.out.println("Current Status: " + found.getStatus());
        } else {
            System.out.println("No book matching ISBN '" + isbn + "' exists in the catalog.");
        }
    }

    /**
     * Prompts for book ISBN and Member ID, then executes the issue transaction.
     */
    private static void issueBook(Scanner scanner, Library library) {
        System.out.print("Enter Book ISBN to issue: ");
        String isbn = scanner.nextLine().trim();

        int memberId = parseIntegerInput(scanner, "Enter Member ID: ");
        if (memberId == -1) return;

        library.issueBook(isbn, memberId);
    }

    /**
     * Prompts for book ISBN and Member ID, then processes the return transaction.
     */
    private static void returnBook(Scanner scanner, Library library) {
        System.out.print("Enter Book ISBN to return: ");
        String isbn = scanner.nextLine().trim();

        int memberId = parseIntegerInput(scanner, "Enter Member ID: ");
        if (memberId == -1) return;

        library.returnBook(isbn, memberId);
    }

    /**
     * Displays personal details, account standing, and checked-out books for a specific member.
     */
    private static void viewMemberDetails(Scanner scanner, Library library) {
        int memberId = parseIntegerInput(scanner, "Enter Member ID to inspect: ");
        if (memberId == -1) return;

        Member member = library.findMemberById(memberId);
        if (member != null) {
            System.out.println("\n[Member Details]");
            System.out.println("ID: " + member.getId());
            System.out.println("Name: " + member.getName());
            System.out.println("Email: " + member.getEmail());
            System.out.println("Account Status: " + member.getStatus());
            System.out.println("Books Currently Borrowed (" + member.getBorrowedBooks().size() + "):");
            if (member.getBorrowedBooks().isEmpty()) {
                System.out.println("  (None)");
            } else {
                for (Book b : member.getBorrowedBooks()) {
                    System.out.println("  - " + b.getTitle() + " [ISBN: " + b.getIsbn() + "]");
                }
            }
        } else {
            System.out.println("Member with ID " + memberId + " not found.");
        }
    }

    /**
     * Prompts for member details and registers the new member with the library.
     */
    private static void registerNewMember(Scanner scanner, Library library) {
        int id = parseIntegerInput(scanner, "Enter New Member ID: ");
        if (id == -1) return;

        if (library.findMemberById(id) != null) {
            System.out.println("A member with ID " + id + " already exists!");
            return;
        }

        System.out.print("Enter Member Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Member Email: ");
        String email = scanner.nextLine().trim();

        Member newMember = new Member(id, name, email);
        library.registerMember(newMember);
        System.out.println("Member " + name + " registered successfully.");
    }

    /**
     * Prompts for book details and enters the book into the library catalog.
     */
    private static void addNewBook(Scanner scanner, Library library) {
        System.out.print("Enter Book ISBN: ");
        String isbn = scanner.nextLine().trim();

        if (library.searchBookByIsbn(isbn) != null) {
            System.out.println("A book with ISBN " + isbn + " already exists in the catalog!");
            return;
        }

        System.out.print("Enter Book Title: ");
        String title = scanner.nextLine().trim();

        System.out.print("Enter Book Author: ");
        String author = scanner.nextLine().trim();

        Book newBook = new Book(isbn, title, author);
        library.addBook(newBook);
        System.out.println("Book '" + title + "' added to catalog successfully.");
    }

    /**
     * Displays all recorded lending events and shows active vs returned status.
     */
    private static void viewLendingHistory(Library library) {
        System.out.println("--- Complete Lending History (Audit Trail) ---");
        if (library.getLendingHistory().isEmpty()) {
            System.out.println("No lending transactions have occurred yet.");
            return;
        }
        for (LendingRecord record : library.getLendingHistory()) {
            System.out.println(record);
            System.out.println("  -> Status: " + (record.isOverdue() ? "OVERDUE!" : "On time / Active"));
        }
    }

    /**
     * Helper to safely parse integer input from console without crashing on non-numeric entries.
     */
    private static int parseIntegerInput(Scanner scanner, String prompt) {
        System.out.print(prompt);
        String rawInput = scanner.nextLine().trim();
        try {
            return Integer.parseInt(rawInput);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Expected a numeric ID.");
            return -1;
        }
    }

    /**
     * Seeds initial books and members so the application is ready for immediate interaction.
     */
    private static void seedInitialData(Library library) {
        library.addBook(new Book("978-0134685991", "Effective Java", "Joshua Bloch"));
        library.addBook(new Book("978-0596009205", "Head First Java", "Kathy Sierra"));
        library.addBook(new Book("978-0132350884", "Clean Code", "Robert C. Martin"));

        library.registerMember(new Member(101, "Alice Smith", "alice@example.com"));
        library.registerMember(new Member(102, "Bob Jones", "bob@example.com"));
    }
}
