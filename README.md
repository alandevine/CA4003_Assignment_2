# CA4003 - Assignment 2

## Spec

The aim of this assignment is to add semantic analysis checks and intermediate representation generation to the lexical and syntax analyser you have implement in Assignment 1. The generated intermediate code should be a 3-address code and stored in a file with the ".ir" extension.

You will need to extend your submission for Assignment 1 to:

- Generate an Abstract Syntax Tree.
- Add a Symbol Table that can handle scope.
- Perform a set of semtantic checks. This following is a list of typical sematic checks
    - Is every identifier declared within scope before its is used?
    - Is no identifier declared more than once in the same scope?
    - Is the left-hand side of an assignment a variable of the correct type?
    - Are the arguments of an arithmetic operator the integer variables or integer constants?
    - Are the arguments of a boolean operator boolean variables or boolean constants?
    - Is there a function for every invoked identifier?
    - Does every function call have the correct number of arguments?
    - Is every variable both written to and read from?
    - Is every function called?
    - Generate an Intermediate Representation using 3-address code.

Feel free to add any additional semantic checks you can think of!

The .jar file for a 3-Address Code Interpreter is available at: here. It is decribed at https://www.computing.dcu.ie/~davids/courses/CA4003/taci.pdf.

### Due
10am on Monday 14th December 2020.

### Submission

You should submit, by email, all the source files in a Winzip.
- Antlr4 grammar file,
- Java files,
- a report
    - description of your abstract syntax tree structure and symbol table,
    - how you implemented them
    - how you implemented the semantic checking and intermediate code generation

Submissions without the declaration of that the assignment is the student's own work will not be assessed. The assignment carries 15 marks and late submissions will incur a 1.5 mark penalty for each 24 hours after the submission.

Please click here to review the School's policy on plagarism. All submissions will be checked for plagarism and severe penalties will apply.
