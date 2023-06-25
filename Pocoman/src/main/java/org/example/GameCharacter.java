package org.example;

public abstract class GameCharacter implements Runnable {
    private Position position;

    public GameCharacter(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public abstract void move();

    public abstract void run();
}