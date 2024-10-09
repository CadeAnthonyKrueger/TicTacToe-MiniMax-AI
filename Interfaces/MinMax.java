// Cade Krueger
// This is used so that we can have one function that finds the max
// or min based on the method reference of type MinMax that we send in.

package Game_Files.Interfaces;

import Game_Files.SpotScore;

@FunctionalInterface
public interface MinMax {
    SpotScore get(SpotScore s1, SpotScore s2);
}
