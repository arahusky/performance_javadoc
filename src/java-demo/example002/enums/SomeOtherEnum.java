/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package example002.enums;

/**
 *
 * @author Jakub Naplava
 */
public enum SomeOtherEnum {
    ONE(1), 
    TWO(2), 
    FOUR(4)
    ;
    
    private final int numberOfMillisToWait;
    
    SomeOtherEnum(int numberOfMillisToWait) {
        this.numberOfMillisToWait = numberOfMillisToWait;
    }
    
    public int getNumberOfMillisToWait() {
        return numberOfMillisToWait;
    }
}
