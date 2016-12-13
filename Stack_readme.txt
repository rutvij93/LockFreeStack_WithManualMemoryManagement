java EliminationStack p d n t e

where p > 1 represents the number of threads to use, d > 0 represents
the upper bound for the random delay between each thread operation, n the
total number of operations each thread attempts to do, t  0 represents the
timeout factor used in the elimination stack, and e > 0 is the size of the
elimination array. All times are in milliseconds. Choose an n > 1000 and a
relatively brief d, such that execution takes at least several seconds for t = 0.