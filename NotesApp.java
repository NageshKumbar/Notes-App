import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

class Note {
    private String title;
    private String content;
    private Date createdDate;
    private Date modifiedDate;
    
    public Note(String title, String content) {
        this.title = title;
        this.content = content;
        this.createdDate = new Date();
        this.modifiedDate = new Date();
    }
    
    public Note(String title, String content, Date createdDate, Date modifiedDate) {
        this.title = title;
        this.content = content;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
    }
    
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public Date getCreatedDate() { return createdDate; }
    public Date getModifiedDate() { return modifiedDate; }
    
    public void setTitle(String title) { 
        this.title = title; 
        updateModifiedDate();
    }
    
    public void setContent(String content) { 
        this.content = content; 
        updateModifiedDate();
    }
    
    private void updateModifiedDate() {
        this.modifiedDate = new Date();
    }
    
    public String toFileFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "TITLE:" + title + "\n" +
               "CREATED:" + sdf.format(createdDate) + "\n" +
               "MODIFIED:" + sdf.format(modifiedDate) + "\n" +
               "CONTENT:\n" + content + "\n" +
               "---END-NOTE---\n";
    }
    
    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return String.format("Title: %s\nCreated: %s | Modified: %s\nContent: %s\n", 
                           title, sdf.format(createdDate), sdf.format(modifiedDate), 
                           content.length() > 50 ? content.substring(0, 50) + "..." : content);
    }
}

public class NotesApp {
    private static final String NOTES_DIR = "notes";
    private static final String NOTES_FILE = NOTES_DIR + "/notes.txt";
    private static ArrayList<Note> notes = new ArrayList<Note>();
    private static Scanner scanner = new Scanner(System.in);
    
    public static void initializeNotesApp() {
        try {
            File notesDirectory = new File(NOTES_DIR);
            if (!notesDirectory.exists()) {
                notesDirectory.mkdirs();
                System.out.println("Created notes directory: " + NOTES_DIR);
            }
            
            File notesFile = new File(NOTES_FILE);
            if (!notesFile.exists()) {
                notesFile.createNewFile();
                System.out.println("Created notes file: " + NOTES_FILE);
            }
            
            loadNotesFromFile();
            
        } catch (IOException e) {
            System.out.println("Error initializing notes app: " + e.getMessage());
        }
    }
    
    public static void loadNotesFromFile() {
        notes.clear();
        
        try {
            FileReader fileReader = new FileReader(NOTES_FILE);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            
            String line;
            Note currentNote = null;
            StringBuilder contentBuilder = new StringBuilder();
            boolean readingContent = false;
            String title = "";
            Date createdDate = null;
            Date modifiedDate = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("TITLE:")) {
                    title = line.substring(6); 
                    readingContent = false;
                    contentBuilder.setLength(0); 
                    
                } else if (line.startsWith("CREATED:")) {
                    try {
                        createdDate = sdf.parse(line.substring(8));
                    } catch (Exception e) {
                        createdDate = new Date();
                    }
                    
                } else if (line.startsWith("MODIFIED:")) {
                    try {
                        modifiedDate = sdf.parse(line.substring(9));
                    } catch (Exception e) {
                        modifiedDate = new Date();
                    }
                    
                } else if (line.equals("CONTENT:")) {
                    readingContent = true;
                    
                } else if (line.equals("---END-NOTE---")) {
                    if (!title.isEmpty()) {
                        currentNote = new Note(title, contentBuilder.toString().trim(), createdDate, modifiedDate);
                        notes.add(currentNote);
                    }
                    readingContent = false;
                    contentBuilder.setLength(0);
                    title = "";
                    
                } else if (readingContent) {
                    if (contentBuilder.length() > 0) {
                        contentBuilder.append("\n");
                    }
                    contentBuilder.append(line);
                }
            }
            
            bufferedReader.close();
            fileReader.close();
            
            System.out.println("Loaded " + notes.size() + " notes from file.");
            
        } catch (FileNotFoundException e) {
            System.out.println("Notes file not found. Starting with empty notes list.");
        } catch (IOException e) {
            System.out.println("Error reading notes file: " + e.getMessage());
        }
    }
    
    public static void saveNotesToFile() {
        try {
            FileWriter fileWriter = new FileWriter(NOTES_FILE);
            
            for (Note note : notes) {
                fileWriter.write(note.toFileFormat());
            }
            
            fileWriter.close();
            System.out.println("Notes saved successfully to " + NOTES_FILE);
            
        } catch (IOException e) {
            System.out.println("Error saving notes: " + e.getMessage());
        }
    }
    
    public static void displayMainMenu() {
        System.out.println("\n" + repeatString("=", 50));
        System.out.println("              NOTES MANAGER");
        System.out.println(repeatString("=", 50));
        System.out.println("1.  Create New Note");
        System.out.println("2.  View All Notes");
        System.out.println("3.  View Note Details");
        System.out.println("4.  Edit Note");
        System.out.println("5.  Delete Note");
        System.out.println("6.  Search Notes");
        System.out.println("7.  Export Note to File");
        System.out.println("8.  Import Note from File");
        System.out.println("9.  Notes Statistics");
        System.out.println("10. Save & Exit");
        System.out.println(repeatString("=", 50));
        System.out.printf("Total Notes: %d | File: %s\n", notes.size(), NOTES_FILE);
        System.out.println(repeatString("=", 50));
        System.out.print("Choose an option (1-10): ");
    }
    
    public static void createNewNote() {
        System.out.println("\n--- CREATE NEW NOTE ---");
        
        System.out.print("Enter note title: ");
        String title = scanner.nextLine().trim();
        
        if (title.isEmpty()) {
            System.out.println("Error: Title cannot be empty!");
            return;
        }
        
        for (Note note : notes) {
            if (note.getTitle().equalsIgnoreCase(title)) {
                System.out.println("Error: A note with this title already exists!");
                return;
            }
        }
        
        System.out.println("Enter note content (type 'END' on a new line to finish):");
        StringBuilder contentBuilder = new StringBuilder();
        String line;
        
        while (!(line = scanner.nextLine()).equals("END")) {
            if (contentBuilder.length() > 0) {
                contentBuilder.append("\n");
            }
            contentBuilder.append(line);
        }
        
        String content = contentBuilder.toString().trim();
        
        if (content.isEmpty()) {
            System.out.println("Error: Content cannot be empty!");
            return;
        }
        
        Note newNote = new Note(title, content);
        notes.add(newNote);
        
        System.out.println("\nNote created successfully!");
        System.out.println("Title: " + title);
        System.out.println("Content length: " + content.length() + " characters");
        
        saveNotesToFile();
    }
    
    public static void viewAllNotes() {
        if (notes.isEmpty()) {
            System.out.println("\nNo notes found! Create your first note.");
            return;
        }
        
        System.out.println("\n--- ALL NOTES ---");
        System.out.println(repeatString("-", 80));
        
        for (int i = 0; i < notes.size(); i++) {
            Note note = notes.get(i);
            System.out.printf("%d. %s\n", (i + 1), note.getTitle());
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            System.out.printf("   Created: %s | Modified: %s\n", 
                            sdf.format(note.getCreatedDate()), sdf.format(note.getModifiedDate()));
            
            String preview = note.getContent().length() > 100 ? 
                           note.getContent().substring(0, 100) + "..." : note.getContent();
            System.out.printf("   Preview: %s\n", preview.replace("\n", " "));
            System.out.println();
        }
        
        System.out.println(repeatString("-", 80));
        System.out.println("Total Notes: " + notes.size());
    }
    
    public static void viewNoteDetails() {
        if (notes.isEmpty()) {
            System.out.println("\nNo notes available!");
            return;
        }
        
        System.out.println("\n--- VIEW NOTE DETAILS ---");
        viewAllNotes();
        
        try {
            System.out.print("Enter note number to view: ");
            int noteIndex = scanner.nextInt() - 1;
            scanner.nextLine(); 
            
            if (noteIndex >= 0 && noteIndex < notes.size()) {
                Note note = notes.get(noteIndex);
                System.out.println("\n" + repeatString("=", 60));
                System.out.println("TITLE: " + note.getTitle());
                System.out.println(repeatString("=", 60));
                
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                System.out.println("Created: " + sdf.format(note.getCreatedDate()));
                System.out.println("Modified: " + sdf.format(note.getModifiedDate()));
                System.out.println("Content Length: " + note.getContent().length() + " characters");
                System.out.println(repeatString("-", 60));
                System.out.println("CONTENT:");
                System.out.println(note.getContent());
                System.out.println(repeatString("=", 60));
            } else {
                System.out.println("Invalid note number!");
            }
        } catch (Exception e) {
            System.out.println("Invalid input! Please enter a number.");
            scanner.nextLine();
        }
    }
    
    public static void editNote() {
        if (notes.isEmpty()) {
            System.out.println("\nNo notes available to edit!");
            return;
        }
        
        System.out.println("\n--- EDIT NOTE ---");
        viewAllNotes();
        
        try {
            System.out.print("Enter note number to edit: ");
            int noteIndex = scanner.nextInt() - 1;
            scanner.nextLine(); 
            
            if (noteIndex >= 0 && noteIndex < notes.size()) {
                Note note = notes.get(noteIndex);
                
                System.out.println("\nCurrent Note:");
                System.out.println("Title: " + note.getTitle());
                System.out.println("Content: " + (note.getContent().length() > 100 ? 
                                 note.getContent().substring(0, 100) + "..." : note.getContent()));
                
                System.out.println("\nWhat would you like to edit?");
                System.out.println("1. Title only");
                System.out.println("2. Content only");
                System.out.println("3. Both title and content");
                System.out.print("Choose option (1-3): ");
                
                int choice = scanner.nextInt();
                scanner.nextLine(); 
                
                switch (choice) {
                    case 1:
                        System.out.print("Enter new title: ");
                        String newTitle = scanner.nextLine().trim();
                        if (!newTitle.isEmpty()) {
                            boolean titleExists = false;
                            for (int i = 0; i < notes.size(); i++) {
                                if (i != noteIndex && notes.get(i).getTitle().equalsIgnoreCase(newTitle)) {
                                    titleExists = true;
                                    break;
                                }
                            }
                            
                            if (!titleExists) {
                                note.setTitle(newTitle);
                                System.out.println("Title updated successfully!");
                            } else {
                                System.out.println("Error: A note with this title already exists!");
                                return;
                            }
                        } else {
                            System.out.println("Error: Title cannot be empty!");
                            return;
                        }
                        break;
                        
                    case 2:
                        System.out.println("Enter new content (type 'END' on a new line to finish):");
                        StringBuilder contentBuilder = new StringBuilder();
                        String line;
                        
                        while (!(line = scanner.nextLine()).equals("END")) {
                            if (contentBuilder.length() > 0) {
                                contentBuilder.append("\n");
                            }
                            contentBuilder.append(line);
                        }
                        
                        String newContent = contentBuilder.toString().trim();
                        if (!newContent.isEmpty()) {
                            note.setContent(newContent);
                            System.out.println("Content updated successfully!");
                        } else {
                            System.out.println("Error: Content cannot be empty!");
                            return;
                        }
                        break;
                        
                    case 3:
                        System.out.print("Enter new title: ");
                        String title = scanner.nextLine().trim();
                        if (title.isEmpty()) {
                            System.out.println("Error: Title cannot be empty!");
                            return;
                        }
                        
                        boolean titleExists = false;
                        for (int i = 0; i < notes.size(); i++) {
                            if (i != noteIndex && notes.get(i).getTitle().equalsIgnoreCase(title)) {
                                titleExists = true;
                                break;
                            }
                        }
                        
                        if (titleExists) {
                            System.out.println("Error: A note with this title already exists!");
                            return;
                        }
                        
                        System.out.println("Enter new content (type 'END' on a new line to finish):");
                        StringBuilder builder = new StringBuilder();
                        String inputLine;
                        
                        while (!(inputLine = scanner.nextLine()).equals("END")) {
                            if (builder.length() > 0) {
                                builder.append("\n");
                            }
                            builder.append(inputLine);
                        }
                        
                        String content = builder.toString().trim();
                        if (content.isEmpty()) {
                            System.out.println("Error: Content cannot be empty!");
                            return;
                        }
                        
                        note.setTitle(title);
                        note.setContent(content);
                        System.out.println("Both title and content updated successfully!");
                        break;
                        
                    default:
                        System.out.println("Invalid option!");
                        return;
                }
                
                saveNotesToFile();
                
            } else {
                System.out.println("Invalid note number!");
            }
        } catch (Exception e) {
            System.out.println("Invalid input! Please enter a number.");
            scanner.nextLine();
        }
    }
    
    public static void deleteNote() {
        if (notes.isEmpty()) {
            System.out.println("\nNo notes available to delete!");
            return;
        }
        
        System.out.println("\n--- DELETE NOTE ---");
        viewAllNotes();
        
        try {
            System.out.print("Enter note number to delete: ");
            int noteIndex = scanner.nextInt() - 1;
            scanner.nextLine(); 
            
            if (noteIndex >= 0 && noteIndex < notes.size()) {
                Note note = notes.get(noteIndex);
                
                System.out.println("\nNote to delete:");
                System.out.println("Title: " + note.getTitle());
                System.out.println("Created: " + note.getCreatedDate());
                
                System.out.print("Are you sure you want to delete this note? (y/n): ");
                String confirmation = scanner.nextLine().trim().toLowerCase();
                
                if (confirmation.equals("y") || confirmation.equals("yes")) {
                    notes.remove(noteIndex);
                    System.out.println("Note deleted successfully!");
                    
                    saveNotesToFile();
                } else {
                    System.out.println("Delete cancelled.");
                }
            } else {
                System.out.println("Invalid note number!");
            }
        } catch (Exception e) {
            System.out.println("Invalid input! Please enter a number.");
            scanner.nextLine();
        }
    }
    
    public static void searchNotes() {
        if (notes.isEmpty()) {
            System.out.println("\nNo notes available to search!");
            return;
        }
        
        System.out.println("\n--- SEARCH NOTES ---");
        System.out.println("1. Search by title");
        System.out.println("2. Search by content");
        System.out.println("3. Search in both title and content");
        System.out.print("Choose search option (1-3): ");
        
        try {
            int choice = scanner.nextInt();
            scanner.nextLine(); 
            
            System.out.print("Enter search term: ");
            String searchTerm = scanner.nextLine().trim().toLowerCase();
            
            if (searchTerm.isEmpty()) {
                System.out.println("Search term cannot be empty!");
                return;
            }
            
            ArrayList<Note> results = new ArrayList<Note>();
            
            for (Note note : notes) {
                boolean match = false;
                
                switch (choice) {
                    case 1:
                        match = note.getTitle().toLowerCase().contains(searchTerm);
                        break;
                    case 2:
                        match = note.getContent().toLowerCase().contains(searchTerm);
                        break;
                    case 3:
                        match = note.getTitle().toLowerCase().contains(searchTerm) ||
                               note.getContent().toLowerCase().contains(searchTerm);
                        break;
                    default:
                        System.out.println("Invalid search option!");
                        return;
                }
                
                if (match) {
                    results.add(note);
                }
            }
            
            if (results.isEmpty()) {
                System.out.println("No notes found matching '" + searchTerm + "'");
            } else {
                System.out.println("\nSearch Results for '" + searchTerm + "':");
                System.out.println(repeatString("-", 60));
                
                for (int i = 0; i < results.size(); i++) {
                    Note note = results.get(i);
                    System.out.printf("%d. %s\n", (i + 1), note.getTitle());
                    
                    String preview = note.getContent().length() > 80 ?
                                   note.getContent().substring(0, 80) + "..." : note.getContent();
                    System.out.printf("   Preview: %s\n", preview.replace("\n", " "));
                    System.out.println();
                }
                
                System.out.println(repeatString("-", 60));
                System.out.println("Found " + results.size() + " matching notes.");
            }
            
        } catch (Exception e) {
            System.out.println("Invalid input!");
            scanner.nextLine();
        }
    }
    
    public static void exportNoteToFile() {
        if (notes.isEmpty()) {
            System.out.println("\nNo notes available to export!");
            return;
        }
        
        System.out.println("\n--- EXPORT NOTE TO FILE ---");
        viewAllNotes();
        
        try {
            System.out.print("Enter note number to export: ");
            int noteIndex = scanner.nextInt() - 1;
            scanner.nextLine(); 
            
            if (noteIndex >= 0 && noteIndex < notes.size()) {
                Note note = notes.get(noteIndex);
                
                String filename = note.getTitle().replaceAll("[^a-zA-Z0-9\\s]", "").replace(" ", "_") + ".txt";
                String filepath = NOTES_DIR + "/" + filename;
                
                try {
                    FileWriter fileWriter = new FileWriter(filepath);
                    fileWriter.write("Title: " + note.getTitle() + "\n");
                    
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    fileWriter.write("Created: " + sdf.format(note.getCreatedDate()) + "\n");
                    fileWriter.write("Modified: " + sdf.format(note.getModifiedDate()) + "\n");
                    fileWriter.write(repeatString("-", 50) + "\n\n");
                    fileWriter.write(note.getContent());
                    
                    fileWriter.close();
                    
                    System.out.println("Note exported successfully to: " + filepath);
                    
                } catch (IOException e) {
                    System.out.println("Error exporting note: " + e.getMessage());
                }
            } else {
                System.out.println("Invalid note number!");
            }
        } catch (Exception e) {
            System.out.println("Invalid input! Please enter a number.");
            scanner.nextLine();
        }
    }
    
    public static void importNoteFromFile() {
        System.out.println("\n--- IMPORT NOTE FROM FILE ---");
        System.out.print("Enter the full file path to import: ");
        String filepath = scanner.nextLine().trim();
        
        try {
            File file = new File(filepath);
            if (!file.exists()) {
                System.out.println("File not found: " + filepath);
                return;
            }
            
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            
            StringBuilder contentBuilder = new StringBuilder();
            String line;
            
            while ((line = bufferedReader.readLine()) != null) {
                if (contentBuilder.length() > 0) {
                    contentBuilder.append("\n");
                }
                contentBuilder.append(line);
            }
            
            bufferedReader.close();
            fileReader.close();
            
            String content = contentBuilder.toString().trim();
            if (content.isEmpty()) {
                System.out.println("File is empty!");
                return;
            }
            
            System.out.print("Enter title for the imported note: ");
            String title = scanner.nextLine().trim();
            
            if (title.isEmpty()) {
                title = "Imported_Note_" + (notes.size() + 1);
            }
            
            for (Note note : notes) {
                if (note.getTitle().equalsIgnoreCase(title)) {
                    title = title + "_" + System.currentTimeMillis();
                    break;
                }
            }
            
            Note importedNote = new Note(title, content);
            notes.add(importedNote);
            
            System.out.println("Note imported successfully!");
            System.out.println("Title: " + title);
            System.out.println("Content length: " + content.length() + " characters");
            
            saveNotesToFile();
            
        } catch (IOException e) {
            System.out.println("Error importing file: " + e.getMessage());
        }
    }
    
    public static void displayNotesStatistics() {
        System.out.println("\n--- NOTES STATISTICS ---");
        System.out.println(repeatString("=", 40));
        
        if (notes.isEmpty()) {
            System.out.println("No notes available for statistics!");
            return;
        }
        
        int totalNotes = notes.size();
        int totalCharacters = 0;
        int totalWords = 0;
        Note oldestNote = notes.get(0);
        Note newestNote = notes.get(0);
        Note longestNote = notes.get(0);
        Note shortestNote = notes.get(0);
        
        for (Note note : notes) {
            int contentLength = note.getContent().length();
            totalCharacters += contentLength;
            totalWords += note.getContent().split("\\s+").length;
            
            if (note.getCreatedDate().before(oldestNote.getCreatedDate())) {
                oldestNote = note;
            }
            
            if (note.getCreatedDate().after(newestNote.getCreatedDate())) {
                newestNote = note;
            }
            
            if (contentLength > longestNote.getContent().length()) {
                longestNote = note;
            }
            
            if (contentLength < shortestNote.getContent().length()) {
                shortestNote = note;
            }
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        System.out.println("Total Notes: " + totalNotes);
        System.out.println("Total Characters: " + totalCharacters);
        System.out.println("Total Words: " + totalWords);
        System.out.println("Average Characters per Note: " + (totalCharacters / totalNotes));
        System.out.println("Average Words per Note: " + (totalWords / totalNotes));
        System.out.println();
        System.out.println("Oldest Note: " + oldestNote.getTitle() + " (" + sdf.format(oldestNote.getCreatedDate()) + ")");
        System.out.println("Newest Note: " + newestNote.getTitle() + " (" + sdf.format(newestNote.getCreatedDate()) + ")");
        System.out.println("Longest Note: " + longestNote.getTitle() + " (" + longestNote.getContent().length() + " characters)");
        System.out.println("Shortest Note: " + shortestNote.getTitle() + " (" + shortestNote.getContent().length() + " characters)");
        System.out.println();
        System.out.println("Storage Location: " + NOTES_FILE);
        
        // File size
        File notesFile = new File(NOTES_FILE);
        if (notesFile.exists()) {
            long fileSize = notesFile.length();
            System.out.println("File Size: " + fileSize + " bytes");
        }
        
        System.out.println(repeatString("=", 40));
    }
    
    private static String repeatString(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    public static void main(String[] args) {
        System.out.println("Welcome to Java Notes Manager!");
        System.out.println("==============================");
        
        initializeNotesApp();
        
        int choice;
        
        do {
            displayMainMenu();
            
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); 
                
                switch (choice) {
                    case 1:
                        createNewNote();
                        break;
                    case 2:
                        viewAllNotes();
                        break;
                    case 3:
                        viewNoteDetails();
                        break;
                    case 4:
                        editNote();
                        break;
                    case 5:
                        deleteNote();
                        break;
                    case 6:
                        searchNotes();
                        break;
                    case 7:
                        exportNoteToFile();
                        break;
                    case 8:
                        importNoteFromFile();
                        break;
                    case 9:
                        displayNotesStatistics();
                        break;
                    case 10:
                        System.out.println("\nSaving notes...");
                        saveNotesToFile();
                        System.out.println("Thank you for using Notes Manager!");
                        System.out.println("Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice! Please select 1-10.");
                }
                
                if (choice != 10 && choice >= 1 && choice <= 9) {
                    System.out.println("\nPress Enter to continue...");
                    scanner.nextLine();
                }
                
            } catch (Exception e) {
                System.out.println("Error: Invalid input! Please enter a number (1-10).");
                scanner.nextLine(); 
                choice = 0; 
            }
            
        } while (choice != 10);
        
        scanner.close();
    }
}