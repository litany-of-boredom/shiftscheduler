# shiftscheduler

This program uses a greedy algorithm to assign gamemastering shifts to members in an escape room club.

### Input:

The program requires two input files: one that contains the required shifts, and one that contains every person's availability. In each, one line represents one shift/person. The format each shift is as follows:

> [day number] [shift number within that day] [# of people required for that shift]

The format for each person's availability is:

> [name] [whether or not they were on design team (boolean)] [availability for shift #1 (0 or 1)] [availability for shift #2 (0 or 1)] ...

Upon running the program, the user can select how many extra shifts, if any, non-design team members should be given.
