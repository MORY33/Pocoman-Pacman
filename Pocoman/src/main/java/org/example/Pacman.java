package org.example;

public class Pacman extends GameCharacter {
    public Pacman(Position position) {
        super(position);
    }

    @Override
    public void move() {
        // logic to move the pacman
    }

    @Override
    public void run() {
        while(true) {
            move();
            // Sleep or wait for keyboard input
        }
    }
}


