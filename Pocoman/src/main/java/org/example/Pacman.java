package org.example;

public class Pacman extends GameCharacter {
    public Pacman(Position position) {
        super(position);
    }

    @Override
    public void move() {
    }

    @Override
    public void run() {
        while(true) {
            move();
        }
    }
}


