// Cade Krueger
// This is used within the Minimax algorithm to keep track of individual spots
// along with corresponding scores. Having the compareTo method allows us to
// find the max/min value that we are looking for in a list of SpotScores.

package Game_Files;

public class SpotScore implements Comparable<SpotScore> {

    public int[] spot;
    public Integer score;
    public SpotScore(int[] spot, Integer score) {
        this.spot = spot;
        this.score = score;
    }

    @Override
    public int compareTo(SpotScore spotScore) {
        return this.score.compareTo(spotScore.score);
    }

    public static SpotScore max(SpotScore s1, SpotScore s2) {
        if (s1.compareTo(s2) > 0) { return s1; } else return s2;
    }

    public static SpotScore min(SpotScore s1, SpotScore s2) {
        if (s1.compareTo(s2) < 0) { return s1; } else return s2;
    }

}
