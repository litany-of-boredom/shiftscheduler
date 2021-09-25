# shiftscheduler

This program uses a greedy algorithm to assign gamemastering shifts to members in an escape room club.

### Input:

The program requires two input files: one that contains the required shifts, and one that contains every person's availability. In each, one line represents one shift/person. The format each shift is as follows:

> [day number] [shift number within that day] [# of people required for that shift]

The format for each person's availability is:

> [name] [whether or not they were on design team (boolean)] [availability for shift #1 (0 or 1)] [availability for shift #2 (0 or 1)] ...

Upon running the program, the user can select how many extra shifts, if any, non-design team members should be given.

### Algorithm:

The program produces a shift schedule that aims to assign x shifts to every design team member and x + y shifts to every non-design team member, where x is a baseline number of shifts computed arithmetically and y is the user-selected number of extra shifts for non-design team members. It accomplishes this by first assigning every person to every shift for which they are available (the locally optimal decision). It then removes shifts from people one by one until they are at their required shift count, dictated by the above expressions. It prioritizes not scheduling people with gaps between shifts on the same day.

However, it is unlikely that the number of shifts availability is evenly divisble by the number of people. (That is, (x)(# of design team members) + (x+y)(# of non-design team members) typically exceeds the number of shifts actually required). To remove these extraneous shifts, the program simply sorts the list of people in order of how many extra shifts they have, then removes shifts one by one.

### Output:

The program outputs to console both a list of people with their assigned shifts and a list of shifts with their assigned people.
