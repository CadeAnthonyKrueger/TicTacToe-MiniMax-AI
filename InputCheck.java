// Cade Krueger
// This is used to help check user input. The reason we used a functional interface
// is that we can have one generalized VerifyInput function that takes in a method ref
// as a parameter (among other things) of type InputCheck.

package Game_Files.Interfaces;

@FunctionalInterface
public interface InputCheck {
    boolean Check(String input);
}
