import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

import exceptions.BarbieNoDeadlineException;
import exceptions.BarbieNoDescException;
import exceptions.BarbieNoKeywordException;
import exceptions.BarbieNoSuchCommandException;
import exceptions.BarbieNoTimingException;
import exceptions.BarbieTaskNumberException;
import types.Deadlines;
import types.Party;
import types.Task;
import types.Todo;



/**
 * Implements the main Barbie chatbot logic.
 */
public class Barbie {

    private enum Command {
        MARK,
        UNMARK,
        DEL,
        TODO,
        DEADLINE,
        PARTY,
        LIST,
        BYE,
        FIND
    }

    /**
     * Main wrapper method for the command logic of Barbie chatbot.
     * @param args to leave empty
     */
    public static void main(String[] args) {
        // CONSTANTS
        Scanner scanner = new Scanner(System.in);
        ArrayList<Task> list = Storage.getLastList();
        Path path = Paths.get("barbie.txt");
        int indexNumber = list.size(); // Starting from 1 reduces the need to subtract and add 1 for usability.


        // Intro
        Ui.intro(Utils.getDateList(LocalDate.now(), list));

        loop:
            while (true) {
                try {

                    String input = scanner.nextLine();
                    String[] parts = input.split(" ", 2);
                    Command command = Command.valueOf(parts[0].toUpperCase());

                    Ui.barbie();

                    switch (command) {
                    case MARK:
                    case UNMARK:
                    case DEL:

                        String desc = parts[1];
                        int taskNumber;
                        try {
                            taskNumber = Integer.parseInt(desc) - 1;
                        } catch (NumberFormatException e) {
                            throw new BarbieTaskNumberException();
                        }
                        switch (command) {
                        case MARK:
                            // Editing variables
                            list.get(taskNumber).mark();
                            Storage.changeLineStatus(path, "1", taskNumber);

                            // Output
                            Ui.mark(list.get(taskNumber));
                            break;

                        case UNMARK:
                            // Editing variables
                            list.get(taskNumber).unmark();
                            Storage.changeLineStatus(path, "2", taskNumber);

                            // Output
                            Ui.unmark(list.get(taskNumber));

                            break;

                        case DEL:
                            // Editing variables
                            list.remove(taskNumber);
                            indexNumber -= 1;
                            Storage.deleteLine(path, taskNumber);

                            // Output
                            Ui.del();
                            break;

                        default:
                            break;
                        }
                        break;

                    case TODO:
                    case DEADLINE:
                    case PARTY:
                        if (parts.length < 2) {
                            throw new BarbieNoDescException();
                        }
                        desc = parts[1];
                        String[] parts2 = parts[1].split("/");

                        switch (command) {
                        case DEADLINE:
                            if (parts2.length < 2) {
                                throw new BarbieNoDeadlineException();
                            }
                            desc = parts2[0];
                            LocalDate by = LocalDate.parse(parts2[1]);
                            list.add(indexNumber, new Deadlines(desc, by));
                            Storage.addToList(path, desc, by);

                            break;

                        case PARTY:
                            if (parts2.length < 3) {
                                throw new BarbieNoTimingException();
                            }
                            desc = parts2[0];
                            LocalDate from = LocalDate.parse(parts2[1]);
                            LocalDate to = LocalDate.parse(parts2[2]);
                            list.add(indexNumber, new Party(desc, from, to));
                            Storage.addToList(path, desc, from, to);

                            break;

                        default:
                            list.add(indexNumber, new Todo(desc));
                            Storage.addToList(path, desc);
                            break;
                        }

                        Ui.taskAdded(list.get(indexNumber));
                        indexNumber++;
                        break;

                    case FIND:
                        if (parts.length < 2) {
                            throw new BarbieNoKeywordException();
                        }
                        String keyword = parts[1];

                        Ui.findTasks(list, indexNumber, keyword);
                        break;

                    case LIST:
                        // No variables to edit, only output (refer to listTasks func)
                        Ui.listTasks(list, indexNumber);
                        break;

                    case BYE:
                        break loop; // break out of the while loop, not switch statement

                    default:
                        throw new BarbieNoSuchCommandException();

                    }

                } catch (DateTimeParseException e) {
                    System.out.println("Hey Barbie,, make sure to give dates in the format YYYY-MM-DD alright! ");
                    System.out.println(e.getMessage());

                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }

                System.out.println("[you]:");

            }
        Ui.exit();

    }


}
