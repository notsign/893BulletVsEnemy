package com.mygdx.game;

import com.badlogic.gdx.Game;

public class BulletVsEnemy extends Game {

    @Override
    public void create() {
        setScreen(new ScrBulletVsEnemy(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
