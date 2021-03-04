#include <pthread.h>
#include <stdio.h>

// Define Bool type
typedef enum {
  false,
  true
} Bool;

// Global variables
int g_puzzle[9][9];
Bool g_columnValidity[9];
Bool g_rowValidity[9];
Bool g_subgridValidity[9];

// Define Parameters structure
typedef struct {
  int topRow;
  int bottomRow;
  int leftColumn;
  int rightColumn;
} Parameters;

// Check if the puzzle section (column/row/subgrid) is valid
void *checkPuzzleSectionIsValid(void *p_args) {
  Parameters *p_sectionParams = (Parameters *)p_args;

  // Destructure given parameters
  int topRow = p_sectionParams->topRow;
  int bottomRow = p_sectionParams->bottomRow;
  int leftColumn = p_sectionParams->leftColumn;
  int rightColumn = p_sectionParams->rightColumn;

  // Initialize counters for each puzzle number (1-9)
  int numCounters[] = {0, 0, 0, 0, 0, 0, 0, 0, 0};

  // Iterate over puzzle section and increment the counter for each number found.
  // i.e. if a 5 is found, the 5th counter (index 4) would be incremented.
  int num;
  for (int row = topRow; row <= bottomRow; row++) {
    for (int column = leftColumn; column <= rightColumn; column++) {
      num = g_puzzle[row][column];
      numCounters[num - 1]++;
    }
  }

  // Check that all counters have a value of 1.
  // Less than 1 -> number not found in section
  // Greater than 1 -> number was found more than once in section
  for (int i = 0; i < 9; i++) {
    if (numCounters[i] != 1) {
      return (void *)false;
    }
  }

  return (void *)true;
}

// Read in a Sudoku puzzle from file SudokuPuzzle.txt, then print whether the
// puzzle is valid or invalid
int main() {
  // Open puzzle file with read permission
  FILE *p_puzzleFile = fopen("SudokuPuzzle.txt", "r");

  // Read each digit from file, store in puzzle matrix and print the puzzle matrix
  int numRead;
  for (int row = 0; row < 9; row++) {
    for (int column = 0; column < 9; column++) {
      fscanf(p_puzzleFile, "%d", &numRead);
      g_puzzle[row][column] = numRead;
      printf("%d\t", numRead);
    }
    printf("\n");
  }

  fclose(p_puzzleFile);

  // Create section parameters
  Parameters columnParams[9];
  Parameters rowParams[9];
  Parameters subgridParams[9];
  for (int i = 0; i < 9; i++) {
    // Create column parameters
    columnParams[i] = (Parameters){0, 8, i, i};

    // Create row parameters
    rowParams[i] = (Parameters){i, i, 0, 8};

    // Calculate subgrid dimensions
    int topRow = (i / 3) * 3;
    int bottomRow = topRow + 2;
    int leftColumn = (i % 3) * 3;
    int rightColumn = leftColumn + 2;

    // Create subgrid parameters
    subgridParams[i] = (Parameters){topRow, bottomRow, leftColumn, rightColumn};
  }

  // Start worker threads
  pthread_t tIdColumn[9];
  pthread_t tIdRow[9];
  pthread_t tIdSubgrid[9];
  for (int i = 0; i < 9; i++) {
    // Start column worker thread
    pthread_create(&tIdColumn[i],
                   NULL,
                   checkPuzzleSectionIsValid,
                   (void *)&columnParams[i]);

    // Start row worker thread
    pthread_create(&tIdRow[i],
                   NULL,
                   checkPuzzleSectionIsValid,
                   (void *)&rowParams[i]);

    // Start subgrid worker thread
    pthread_create(&tIdSubgrid[i],
                   NULL,
                   checkPuzzleSectionIsValid,
                   (void *)&subgridParams[i]);
  }

  // Wait for threads, then print valid or invalid messages
  for (int i = 0; i < 9; i++) {
    void *p_isValid;

    // Join location for column thread
    pthread_join(tIdColumn[i], &p_isValid);

    // Set validity for column
    g_columnValidity[i] = (Bool)p_isValid;

    // Print valid or invalid message for column
    printf("%X TRow: %d, BRow: %d, LCol: %d, RCol: %d %s!\n",
           (unsigned int)tIdColumn[i],
           columnParams[i].topRow,
           columnParams[i].bottomRow,
           columnParams[i].leftColumn,
           columnParams[i].rightColumn,
           !!(Bool)p_isValid ? "valid" : "invalid");

    // Join location for row thread
    pthread_join(tIdRow[i], &p_isValid);

    // Set validity for row
    g_rowValidity[i] = (Bool)p_isValid;

    // Print valid or invalid message for row
    printf("%X TRow: %d, BRow: %d, LCol: %d, RCol: %d %s!\n",
           (unsigned int)tIdRow[i],
           rowParams[i].topRow,
           rowParams[i].bottomRow,
           rowParams[i].leftColumn,
           rowParams[i].rightColumn,
           !!(Bool)p_isValid ? "valid" : "invalid");

    // Join location for subgrid thread
    pthread_join(tIdSubgrid[i], &p_isValid);

    // Set validity for subgrid
    g_subgridValidity[i] = (Bool)p_isValid;

    // Print valid or invalid message for subgrid
    printf("%X TRow: %d, BRow: %d, LCol: %d, RCol: %d %s!\n",
           (unsigned int)tIdSubgrid[i],
           subgridParams[i].topRow,
           subgridParams[i].bottomRow,
           subgridParams[i].leftColumn,
           subgridParams[i].rightColumn,
           !!(Bool)p_isValid ? "valid" : "invalid");
  }

  // Print validity of each section checked
  for (int i = 0; i < 9; i++) {
    // Print validity of column
    printf("Column: %X %s\n",
           (unsigned int)tIdColumn[i],
           !!g_columnValidity[i] ? "valid" : "invalid");

    // Print validity of row
    printf("Row: %X %s\n",
           (unsigned int)tIdRow[i],
           !!g_rowValidity[i] ? "valid" : "invalid");

    // Print validity of subgrid
    printf("Subgrid: %X %s\n",
           (unsigned int)tIdSubgrid[i],
           !!g_subgridValidity[i] ? "valid" : "invalid");
  }

  // Check all validity arrays to determine whether the puzzle is valid
  Bool puzzleIsValid = true;
  for (int i = 0; i < 9; i++) {
    if (!g_columnValidity[i] || !g_rowValidity[i] || !g_subgridValidity[i]) {
      puzzleIsValid = false;
    }
  }

  // Print validity of puzzle
  printf("Sudoku Puzzle: %s\n", !!puzzleIsValid ? "valid" : "invalid");

  return 0;
}