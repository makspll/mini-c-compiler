# Mini-C Compiler
## License
The code is mine, however it was based on the repository given to students taking compiling techniques at UoE 
(2019-20, no longer running, i did not take it but i did watch the lectures and try and stick to the coursework instructions)

link to the mentioned repo: [gitlab](https://git.ecdf.ed.ac.uk/cdubach/ct-19-20/tree/master/desc/part1)

those parts of code which are identical to exerpts from the repo are not under my license.


## Requirements
- Java 11
- Maven

## features

this version of mini c supports (items with * signify those features are not present in the original coursework):

- int,char,void, struct, pointer and array types
- pointer arithmetic*
- casting (char to int, array to ptr, void ptr to other ptr)
- some standard library functions: read_i, read_c, print_i, print_c, print_s, mcmalloc
- heap allocation using mcmalloc(bytes)
- recursion
- if/else and while control flow statements

the program structure enforces that the declarations always precede any statements in each scope,
and that in the global scope struct type declarations come before global variable declarations and those come before
function declarations

have a look at the example txt files with programs to see what I mean.

To compile a file run: `make run IN="<inputfile>" OUT="<outputfile>"`

this will create the output file as well as an abstract syntax tree in png format (among other dump files) in the same directory

To launch the program run it in MARS using: `java -jar Mars4_5.jar`
which will open up the MARS GUI in which you can open and simulate the output file.

## notes

this compiler has been developed within the span of 3 weeks (just before summer break ended).
The course normally runs over 11 weeks and so I very much did not aim for perfect code (you'll find some bolognese in the repo \/(^_^)\/ ) or a fast/efficient compiler.
My goal was to quickstart my understanding of compilers and write one ASAP. I was desperate since the course was cancelled and I was looking forward to taking it since day 1!
I definitely achieved this goal and I am very happy to have a working (even if crappy) compiler of my own!

## Development

To build:
`make build`

To run tests:
`make test`

To run jar after build:
`make run IN="<filename>" OUT="<filename>"`

To watch an input file and recompile on change:
`make watch IN="<filename>" OUT="<filename>"`


