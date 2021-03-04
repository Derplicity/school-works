import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

/**
 * Parses, creates and prints the access control rights defined in various files, as well as allows
 * a user to query said rights
 */
public class RBAC {

  /** The role hierarchy for access control */
  private static Map<String, List<String>> roleHierarchy;

  /** The list containing all unique roles */
  private static List<String> allRoles;

  /** The list containing all unique objects */
  private static List<String> allObjects;

  /** The matrix containing all permissions for roles on objects */
  private static List<List<List<String>>> roleObjectMatrix;

  /** The constraints connected to a set of roles */
  private static Map<Integer, List<String>> roleConstraints;

  /** The list containing all unique users */
  private static List<String> allUsers;

  /** The matrix containing all connections between users and roles */
  private static List<List<String>> userRoleMatrix;

  /** Thc width of a cell (unpadded) in the formatted role object matrix table */
  private static Integer unpaddedRoleObjectTableCellWidth;

  /** Thc width of a cell (unpadded) in the formatted user role matrix table */
  private static Integer unpaddedUserRoleTableCellWidth;

  /** The scanner for reading file contents and user input */
  private static Scanner scanner;

  /**
   * Parses, creates and prints the access control rights defined in various files, as well as
   * allows a user to query said rights
   * 
   * @param args
   * @throws FileNotFoundException
   */
  public static void main(String[] args) throws FileNotFoundException {

    // Initialize width of unpadded role object table cell
    unpaddedRoleObjectTableCellWidth = 0;

    // Initialize width of unpadded user role table cell
    unpaddedUserRoleTableCellWidth = 0;

    parseRoleHierarchy();
    printRoleHierarchy();
    parseResourceObjects();
    createRoleObjectMatrix();
    printRoleObjectMatrix();
    addDefaultPermissions();
    parsePermissions();
    printRoleObjectMatrix();
    parseConstraints();
    printConstraints();
    parseUserRoles();
    printUserRoleMatrix();
    runQueryLoop();

  }

  /**
   * Parses the role hierarchy defined in a file named roleHierarchy.txt
   * 
   * @throws FileNotFoundException
   */
  private static void parseRoleHierarchy() throws FileNotFoundException {

    roleHierarchy = new TreeMap<String, List<String>>();
    scanner = new Scanner(new File("roleHierarchy.txt"));

    int longestRoleLabel = 0;

    // The set containing only unique roles
    Set<String> uniqueRoles = new HashSet<>();

    // Tracks the ascendant roles
    Set<String> ascendantRoles = new HashSet<>();

    // Tracks current line for error message
    int currentLine = 1;

    while (scanner.hasNextLine()) {

      String[] relatedRoles = scanner.nextLine().split("\t");
      String ascendantRole = relatedRoles[0];
      String descendantRole = relatedRoles[1];

      // Add roles to all roles if not already present
      uniqueRoles.add(ascendantRole);
      uniqueRoles.add(descendantRole);

      // Check if repeating ascendant role -> means invalid role hierarchy
      if (!ascendantRoles.add(ascendantRole)) {

        // Close file scanner
        scanner.close();

        // Print error
        System.out.println("invalid line is found in roleHierarchy.txt: line " + currentLine
            + ", enter any key to read it again");

        scanner = new Scanner(System.in);

        // Wait for any user input
        if (scanner.nextLine().length() >= 0) {
          // Reattempt parse
          parseRoleHierarchy();
        }

        scanner.close();

        return;
      }

      if (ascendantRole.length() > longestRoleLabel)
        longestRoleLabel = ascendantRole.length();

      if (descendantRole.length() > longestRoleLabel)
        longestRoleLabel = descendantRole.length();

      // Add role hierarchy mapping if not already present
      if (roleHierarchy.get(descendantRole) == null)
        roleHierarchy.put(descendantRole, new ArrayList<String>());

      // Append new ascendant role to descendant role hierarchy
      List<String> descendantRoleHierarchy = roleHierarchy.get(descendantRole);
      descendantRoleHierarchy.add(ascendantRole);

      // Replace old role hierarchy mapping
      roleHierarchy.replace(descendantRole, descendantRoleHierarchy);

      currentLine++;

    }

    if (longestRoleLabel > unpaddedRoleObjectTableCellWidth)
      unpaddedRoleObjectTableCellWidth = longestRoleLabel;

    if (longestRoleLabel > unpaddedUserRoleTableCellWidth)
      unpaddedUserRoleTableCellWidth = longestRoleLabel;

    allRoles = new ArrayList<>(uniqueRoles);

    scanner.close();

  }

  /**
   * Prints the formatted role hierarchy
   */
  private static void printRoleHierarchy() {

    System.out.println("Role Hierarchy:\n");

    for (Map.Entry<String, List<String>> entry : roleHierarchy.entrySet()) {

      System.out.print(entry.getKey() + " ---> ");

      for (int i = 0; i < entry.getValue().size(); i++) {

        if (i != 0)
          System.out.print(", ");

        System.out.print(entry.getValue().get(i));

      }

      System.out.println();

    }

    System.out.println();

  }

  /**
   * Parses the resource objects listed in a file name resourceObjects.txt
   * 
   * @throws FileNotFoundException
   */
  private static void parseResourceObjects() throws FileNotFoundException {

    scanner = new Scanner(new File("resourceObjects.txt"));

    int longestObjectLabel = 0;

    // The set containing only unique objects
    Set<String> uniqueObjects = new HashSet<>();

    while (scanner.hasNext()) {
      String object = scanner.next();

      if (!uniqueObjects.add(object)) {

        // Close file scanner
        scanner.close();

        // Print error
        System.out
            .println("duplicate object is found: " + object + ", enter any key to read it again");

        scanner = new Scanner(System.in);

        // Wait for any user input
        if (scanner.nextLine().length() >= 0) {
          // Reattempt parse
          parseResourceObjects();
        }

        scanner.close();

        return;

      }

      if (object.length() > longestObjectLabel)
        longestObjectLabel = object.length();

    }

    if (longestObjectLabel > unpaddedRoleObjectTableCellWidth)
      unpaddedRoleObjectTableCellWidth = longestObjectLabel;

    allObjects = new ArrayList<>(uniqueObjects);

    scanner.close();

  }

  /**
   * Creates an empty role object matrix
   */
  private static void createRoleObjectMatrix() {

    int numRows = allRoles.size();
    int numColumns = allRoles.size() + allObjects.size();

    List<List<List<String>>> rows = new ArrayList<>(numRows);

    // Add columns to each row
    for (int i = 0; i < numRows; i++) {

      List<List<String>> columns = new ArrayList<>(numColumns);

      // Add permissions list to each column
      for (int j = 0; j < numColumns; j++) {

        columns.add(j, new ArrayList<String>());

      }

      rows.add(i, columns);

    }

    roleObjectMatrix = rows;

  }

  /**
   * Prints the formatted role object matrix
   */
  private static void printRoleObjectMatrix() {

    System.out.println("Role Object Matrix:\n");

    int tableWidth = 100;
    int tableCellPadding = 5;
    int paddedTableCellWidth = unpaddedRoleObjectTableCellWidth + tableCellPadding;
    int columnsPerTableSplit = (tableWidth / paddedTableCellWidth) - 1;
    double numTableSplits =
        Math.ceil((allRoles.size() + allObjects.size()) / (columnsPerTableSplit + 0.0));

    for (int i = 0; i < numTableSplits; i++) {

      if (numTableSplits > 1)
        System.out.println("[ Part " + (i + 1) + " ]");

      System.out.print(String.format("%" + (paddedTableCellWidth - 2) + "s", "") + "| ");

      int numColumns = (columnsPerTableSplit * (i + 1)) < (allRoles.size() + allObjects.size())
          ? (columnsPerTableSplit * (i + 1))
          : (allRoles.size() + allObjects.size());

      for (int j = columnsPerTableSplit * i; j < numColumns; j++) {

        String objectLabel =
            j < allRoles.size() ? allRoles.get(j) : allObjects.get(j - allRoles.size());
        int objectLabelLength = objectLabel.length();

        System.out.print(objectLabel
            + String.format("%" + (paddedTableCellWidth - objectLabelLength - 2) + "s", "") + "| ");

      }

      System.out.println("\n" + repeatString("-",
          (((numColumns - columnsPerTableSplit * i) + 1) * paddedTableCellWidth) - 1));

      List<List<List<String>>> rows = roleObjectMatrix;

      for (int j = 0; j < rows.size(); j++) {

        String role = allRoles.get(j);
        int roleLabelLength = role.length();

        System.out.print("| " + role
            + String.format("%" + (paddedTableCellWidth - roleLabelLength - 4) + "s", "") + "| ");

        List<List<String>> columns = rows.get(j);

        boolean hasMorePermissions = false;
        int permissionNum = 0;

        do {

          hasMorePermissions = false;

          if (permissionNum > 0)
            System.out
                .print("|" + String.format("%" + (paddedTableCellWidth - 3) + "s", "") + "| ");


          for (int k = columnsPerTableSplit * i; k < numColumns; k++) {

            List<String> permissions = columns.get(k);

            if (permissionNum < permissions.size()) {

              String permission = permissions.get(permissionNum);

              int permissionLabelLength = permission.length();

              if (permissionNum + 1 < permissions.size()) {

                permission += ",";
                permissionLabelLength++;

              }

              System.out
                  .print(permission
                      + String.format(
                          "%" + (paddedTableCellWidth - permissionLabelLength - 2) + "s", "")
                      + "| ");

              if (permissionNum + 1 < permissions.size())
                hasMorePermissions = true;

            } else {

              System.out.print(String.format("%" + (paddedTableCellWidth - 2) + "s", "") + "| ");

            }

          }

          System.out.println();

          permissionNum++;

        } while (hasMorePermissions);

        System.out.println(repeatString("-",
            (((numColumns - columnsPerTableSplit * i) + 1) * paddedTableCellWidth) - 1));

      }

      System.out.println();

    }

  }

  /**
   * Gets a list of the descendants for the specified role
   * 
   * @param role
   * @return list of descendants
   */
  public static List<String> getDescendants(String role) {

    List<String> descendants = new ArrayList<>();

    for (Map.Entry<String, List<String>> entry : roleHierarchy.entrySet()) {

      if (entry.getValue().contains(role)) {

        descendants.add(entry.getKey());

        // Get the descendants of this descendant
        descendants.addAll(getDescendants(entry.getKey()));

      }

    }

    return descendants;

  }

  /**
   * Adds a permission to a role on an object in the role object matrix
   * 
   * @param role
   * @param object
   * @param permission
   */
  public static void addPermission(String role, String object, String permission) {

    int roleIndex = allRoles.indexOf(role);
    int objectIndex = allRoles.contains(object) ? allRoles.indexOf(object)
        : (allRoles.size() + allObjects.indexOf(object));

    List<List<List<String>>> rows = roleObjectMatrix;
    List<List<String>> columns = rows.get(roleIndex);
    List<String> permissions = columns.get(objectIndex);

    // Only add if not already present
    if (!permissions.contains(permission)) {

      permissions.add(permission);

      // Replace column
      columns.remove(objectIndex);
      columns.add(objectIndex, permissions);

      // Replace row
      rows.remove(roleIndex);
      rows.add(roleIndex, columns);
    }


    roleObjectMatrix = rows;

  }

  /**
   * Adds the defoult permissions to the role object matrix
   */
  private static void addDefaultPermissions() {

    if (unpaddedRoleObjectTableCellWidth < 7)
      unpaddedRoleObjectTableCellWidth = 7;

    for (String role : allRoles) {

      // Each role controls itself
      addPermission(role, role, "control");

      for (String descendant : getDescendants(role)) {

        // Each descendant controls and owns the ascendant
        addPermission(descendant, role, "control");
        addPermission(descendant, role, "own");

      }

    }

  }

  /**
   * Parses the permissions defined in a file named permissionsToRoles.txt
   * 
   * @throws FileNotFoundException
   */
  private static void parsePermissions() throws FileNotFoundException {

    scanner = new Scanner(new File("permissionsToRoles.txt"));

    int longestPermissionLabel = 0;

    while (scanner.hasNextLine()) {

      String[] permissionInfo = scanner.nextLine().split("\t");
      String role = permissionInfo[0];
      String permission = permissionInfo[1];
      String object = permissionInfo[2];

      if (permission.length() > longestPermissionLabel)
        longestPermissionLabel = permission.length();

      addPermission(role, object, permission);

      for (String descendant : getDescendants(role)) {

        addPermission(descendant, object, permission);

      }

    }

    if (longestPermissionLabel > unpaddedRoleObjectTableCellWidth)
      unpaddedRoleObjectTableCellWidth = longestPermissionLabel;

    scanner.close();

  }

  /**
   * Parses the role constraints defined in a file name roleSetsSSD.txt
   * 
   * @throws FileNotFoundException
   */
  private static void parseConstraints() throws FileNotFoundException {

    roleConstraints = new HashMap<>();
    scanner = new Scanner(new File("roleSetsSSD.txt"));

    // Tracks current line for error message
    int currentLine = 1;

    while (scanner.hasNextLine()) {

      String[] constraintInfo = scanner.nextLine().split("\t");
      int n = Integer.parseInt(constraintInfo[0]);
      List<String> roles = new ArrayList<>(Arrays.asList(constraintInfo));
      roles.remove(0); // Remove n from role list

      if (n < 2) {

        // Close file scanner
        scanner.close();

        // Print error
        System.out.println("invalid line is found in roleSetsSSD.txt: line " + currentLine
            + ", enter any key to read it again");

        scanner = new Scanner(System.in);

        // Wait for any user input
        if (scanner.nextLine().length() >= 0) {
          // Reattempt parse
          parseConstraints();
        }

        scanner.close();

        return;

      }

      roleConstraints.put(n, roles);

      currentLine++;

    }

    scanner.close();

  }

  /**
   * Prints the formatted role constraints
   */
  private static void printConstraints() {

    System.out.println("Constraints:\n");

    int constraintNum = 1;

    for (Map.Entry<Integer, List<String>> entry : roleConstraints.entrySet()) {

      int n = entry.getKey();
      List<String> roles = entry.getValue();

      System.out.print("Constraint " + constraintNum + ", n = " + n + ", set of roles = {");

      for (int i = 0; i < roles.size(); i++) {

        if (i != 0)
          System.out.print(", ");

        System.out.print(roles.get(i));

      }

      System.out.println("}");

      constraintNum++;

    }

    System.out.println();

  }

  /**
   * Parses the user role relationships in a file named usersRoles.txt to the user role matrix
   * 
   * @throws FileNotFoundException
   */
  private static void parseUserRoles() throws FileNotFoundException {

    userRoleMatrix = new ArrayList<>();
    scanner = new Scanner(new File("usersRoles.txt"));

    Set<String> uniqueUsers = new HashSet<>();

    int longestUserLabel = 0;

    // Tracks current line for error message
    int currentLine = 1;

    while (scanner.hasNextLine()) {

      String[] userRoleInfo = scanner.nextLine().split("\t");
      String user = userRoleInfo[0];
      List<String> roles = new ArrayList<>(Arrays.asList(userRoleInfo));
      roles.remove(0); // Remove user from role list

      if (!uniqueUsers.add(user)) {

        // Close file scanner
        scanner.close();

        // Print error
        System.out.println("invalid line is found in usersRoles.txt: line " + currentLine
            + " due to duplicate user, enter any key to read it again");

        scanner = new Scanner(System.in);

        // Wait for any user input
        if (scanner.nextLine().length() >= 0) {
          // Reattempt parse
          parseUserRoles();
        }

        scanner.close();

        return;

      }

      if (user.length() > longestUserLabel)
        longestUserLabel = user.length();

      List<String> roleStatuses = new ArrayList<>(Collections.nCopies(allRoles.size(), ""));

      for (String role : roles) {

        int roleIndex = allRoles.indexOf(role);
        int constraintNum = 1;

        // Check for constraint errors
        for (Map.Entry<Integer, List<String>> entry : roleConstraints.entrySet()) {

          if (entry.getValue().contains(role)) {

            int n = entry.getKey();
            boolean hasConstraintError = false;

            if (n == 2) {

              int numMutuallyExclusive = 0;

              // Check if more than one mutually exclusive role is present
              for (String roleToCheck : roles) {

                if (entry.getValue().contains(roleToCheck))
                  numMutuallyExclusive++;

              }

              hasConstraintError = numMutuallyExclusive > 1;

            } else {

              int roleCardinality = 1;

              // Check if more than n instances of the current role are present
              for (int i = 0; i < userRoleMatrix.size(); i++) {

                if (userRoleMatrix.get(i).get(roleIndex).equals("+"))
                  roleCardinality++;

              }

              hasConstraintError = roleCardinality > n;

            }

            if (hasConstraintError) {

              // Close file scanner
              scanner.close();

              // Print error
              System.out.println("invalid line is found in usersRoles.txt: line " + currentLine
                  + " due to constraint " + constraintNum + ", enter any key to read it again");

              scanner = new Scanner(System.in);

              // Wait for any user input
              if (scanner.nextLine().length() >= 0) {
                // Reattempt parse
                parseUserRoles();
              }

              scanner.close();

              return;

            }

          }

          constraintNum++;

        }

        // Replace empty role status to active
        roleStatuses.remove(roleIndex);
        roleStatuses.add(roleIndex, "+");

      }

      userRoleMatrix.add(roleStatuses);

      currentLine++;

    }

    if (longestUserLabel > unpaddedUserRoleTableCellWidth)
      unpaddedUserRoleTableCellWidth = longestUserLabel;

    allUsers = new ArrayList<>(uniqueUsers);

    scanner.close();

  }

  /**
   * Prints the formatted user role matrix
   */
  private static void printUserRoleMatrix() {

    System.out.println("User Role Matrix:\n");

    int tableWidth = 100;
    int tableCellPadding = 5;
    int paddedTableCellWidth = unpaddedUserRoleTableCellWidth + tableCellPadding;
    int columnsPerTableSplit = (tableWidth / paddedTableCellWidth) - 1;
    double numTableSplits = Math.ceil(allRoles.size() / (columnsPerTableSplit + 0.0));

    for (int i = 0; i < numTableSplits; i++) {

      if (numTableSplits > 1)
        System.out.println("[ Part " + (i + 1) + " ]");

      System.out.print(String.format("%" + (paddedTableCellWidth - 2) + "s", "") + "| ");

      int numColumns =
          (columnsPerTableSplit * (i + 1)) < allRoles.size() ? (columnsPerTableSplit * (i + 1))
              : allRoles.size();

      for (int j = columnsPerTableSplit * i; j < numColumns; j++) {

        String roleLabel = allRoles.get(j);
        int roleLabelLength = roleLabel.length();

        System.out.print(roleLabel
            + String.format("%" + (paddedTableCellWidth - roleLabelLength - 2) + "s", "") + "| ");

      }

      System.out.println("\n" + repeatString("-",
          (((numColumns - columnsPerTableSplit * i) + 1) * paddedTableCellWidth) - 1));

      List<List<String>> rows = userRoleMatrix;

      for (int j = 0; j < rows.size(); j++) {

        String user = allUsers.get(j);
        int userLabelLength = user.length();

        System.out.print("| " + user
            + String.format("%" + (paddedTableCellWidth - userLabelLength - 4) + "s", "") + "| ");

        List<String> columns = rows.get(j);

        for (int k = columnsPerTableSplit * i; k < numColumns; k++) {

          String status = columns.get(k);

          if (status.equals("+")) {

            System.out
                .print(status + String.format("%" + (paddedTableCellWidth - 3) + "s", "") + "| ");

          } else {

            System.out.print(String.format("%" + (paddedTableCellWidth - 2) + "s", "") + "| ");

          }

        }

        System.out.println("\n" + repeatString("-",
            (((numColumns - columnsPerTableSplit * i) + 1) * paddedTableCellWidth) - 1));

      }

      System.out.println();

    }

  }

  /**
   * Prints all the permissions that a user has on objects
   * 
   * @param user
   */
  private static void printUserPermissions(String user) {

    int userIndex = allUsers.indexOf(user);
    List<String> roleStatuses = userRoleMatrix.get(userIndex);

    for (int i = 0; i < roleStatuses.size(); i++) {

      if (roleStatuses.get(i).equals("+")) {

        List<List<String>> objects = roleObjectMatrix.get(i);

        for (int j = 0; j < objects.size(); j++) {

          List<String> objectPermissions = objects.get(j);

          if (objectPermissions.size() > 0) {

            System.out.print((j < roleObjectMatrix.size() ? allRoles.get(j)
                : allObjects.get(j - roleObjectMatrix.size())) + "\t");

            for (int k = 0; k < objectPermissions.size(); k++) {

              if (k != 0)
                System.out.print(", ");

              System.out.print(objectPermissions.get(k));

            }

            System.out.println();

          }

        }

      }

    }

  }

  /**
   * Prints the permissions a user has on a specified object
   * 
   * @param user
   * @param object
   */
  private static void printUserObjectPermissions(String user, String object) {

    int userIndex = allUsers.indexOf(user);
    int objectIndex = allRoles.contains(object) ? allRoles.indexOf(object)
        : (allRoles.size() + allObjects.indexOf(object));
    List<String> roleStatuses = userRoleMatrix.get(userIndex);

    for (int i = 0; i < roleStatuses.size(); i++) {

      if (roleStatuses.get(i).equals("+")) {

        List<List<String>> objects = roleObjectMatrix.get(i);
        List<String> objectPermissions = objects.get(objectIndex);

        if (objectPermissions.size() > 0) {

          System.out.print(object + "\t");

          for (int k = 0; k < objectPermissions.size(); k++) {

            if (k != 0)
              System.out.print(", ");

            System.out.print(objectPermissions.get(k));

          }

          System.out.println();

        }

      }

    }

  }

  /**
   * Checks whether the user has the given permission on a specified object
   * 
   * @param user
   * @param object
   * @param permission
   * @return
   */
  private static boolean hasUserObjectPermission(String user, String object, String permission) {

    int userIndex = allUsers.indexOf(user);
    int objectIndex = allRoles.contains(object) ? allRoles.indexOf(object)
        : (allRoles.size() + allObjects.indexOf(object));
    List<String> roleStatuses = userRoleMatrix.get(userIndex);
    boolean hasPermission = false;

    for (int i = 0; i < roleStatuses.size(); i++) {

      if (roleStatuses.get(i).equals("+")) {

        List<List<String>> objects = roleObjectMatrix.get(i);
        List<String> objectPermissions = objects.get(objectIndex);

        if (objectPermissions.contains(permission)) {
          hasPermission = true;
        }

      }

    }

    return hasPermission;

  }

  /**
   * A loop that lets the user query the access control rights.
   */
  private static void runQueryLoop() {

    scanner = new Scanner(System.in);
    boolean quit = false;

    while (!quit) {

      System.out.print("Please enter the user in your query: ");
      String user = scanner.nextLine();

      if (user.isEmpty() || !allUsers.contains(user)) {

        System.out.println("invalid user, try again.\n");
        continue;

      }

      System.out.print("Please enter the object in your query (hit enter if it's for any): ");
      String object = scanner.nextLine();

      if (!object.isEmpty() && (!allObjects.contains(object) && !allRoles.contains(object))) {

        System.out.println("invalid object, try again\n");
        continue;

      }

      System.out.print("Please enter the access right in your query (hit enter if it's for any): ");
      String permission = scanner.nextLine();

      System.out.println("\nQuery Results:\n");

      if (object.isEmpty() && permission.isEmpty()) {

        printUserPermissions(user);

      } else if (permission.isEmpty()) {

        printUserObjectPermissions(user, object);

      } else {

        if (hasUserObjectPermission(user, object, permission)) {

          System.out.println("authorized");

        } else {

          System.out.println("rejected");

        }

      }

      System.out.print("\nWould you like to continue for the next query? ");

      if (!scanner.nextLine().toLowerCase().equals("yes")) {

        quit = true;

      } else {

        System.out.println();

      }

    }

    scanner.close();

  }

  /**
   * Helper function to do String.repeat(Int) in lower versions of Java
   * 
   * @param stringToRepeat
   * @param numRepeats
   * @return
   */
  private static String repeatString(String stringToRepeat, int numRepeats) {

    String repeatedString = "";

    for (int i = 0; i < numRepeats; i++) {

      repeatedString += stringToRepeat;

    }

    return repeatedString;

  }

}
