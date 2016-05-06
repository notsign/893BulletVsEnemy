package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Created by k9sty on 2016-03-12.
 */


public class Player {
    Body mainBody;
    BodyDef bdefMain;
    FixtureDef fdefPlayer, fdefFoot;
    PolygonShape shape;
    TextureAtlas taIdle = new TextureAtlas(Gdx.files.internal("player/idle/idle.pack"));
    TextureAtlas taRun = new TextureAtlas(Gdx.files.internal("player/run/run.pack"));
    Animation aniIdle, aniRun;
    float elapsedTime = 0;
    float playerwidth = taIdle.findRegion("idle (1)").getRegionWidth();
    float playerheight = taIdle.findRegion("idle (1)").getRegionHeight();
    World world;
    int bulletCooldown = 0;

    boolean bRight = true, isIdle = true;

    Player(World world, Vector2 spawnpoint) {
        this.world = world;
        createMainBody(spawnpoint);
        createFootSensor();
    }

    private void createMainBody(Vector2 spawnpoint) {
        aniIdle = new Animation(10, taIdle.getRegions());
        aniRun = new Animation(10, taRun.getRegions());
        bdefMain = new BodyDef();
        shape = new PolygonShape();

        bdefMain.position.set(new Vector2(spawnpoint.x / 2, spawnpoint.y / 2));
        bdefMain.type = BodyDef.BodyType.DynamicBody;
        mainBody = world.createBody(bdefMain);
        mainBody.setFixedRotation(true);

        shape.setAsBox(playerwidth / 4, playerheight / 4);
        fdefPlayer = new FixtureDef();
        fdefPlayer.shape = shape;
        fdefPlayer.friction = 1;
        fdefPlayer.filter.categoryBits = 2;
        fdefPlayer.filter.maskBits = 1 | 8; // Terrain, enemy
        mainBody.setSleepingAllowed(false);
        mainBody.createFixture(fdefPlayer);
        mainBody.setUserData("Player");
        shape.dispose();
    }

    private void createFootSensor() {
        shape = new PolygonShape();

        shape.setAsBox(playerwidth / 4, 0.2f, new Vector2(mainBody.getLocalCenter().x, mainBody.getLocalCenter().y - (playerheight / 4) - 0.1f), 0);
        fdefFoot = new FixtureDef();
        fdefFoot.shape = shape;
        mainBody.createFixture(fdefFoot);
        shape.dispose();
    }

    Vector3 getPosition() {
        return new Vector3(mainBody.getPosition().x, mainBody.getPosition().y, 0);
    }

    void render(SpriteBatch sb) {
        if (bulletCooldown != 0) {
            bulletCooldown--;
        }
        elapsedTime++;
        if (isIdle) {
            TextureRegion trIdle = aniIdle.getKeyFrame(elapsedTime, true);
            playerwidth = taIdle.findRegion("idle (1)").getRegionWidth();
            playerheight = taIdle.findRegion("idle (1)").getRegionHeight();
            if (bRight) {
                //sb.draw(trIdle, mainBody.getPosition().x - playerwidth / 2, mainBody.getPosition().y - playerheight / 2, playerwidth / 2, playerheight / 2);
                sb.draw(trIdle, mainBody.getWorldCenter().x - playerwidth / 4, mainBody.getWorldCenter().y - playerwidth / 4, playerwidth / 2, playerheight / 2);
            } else {
                sb.draw(trIdle, mainBody.getWorldCenter().x + playerwidth / 4, mainBody.getWorldCenter().y - playerwidth / 4, -playerwidth / 2, playerheight / 2);
            }
        } else {
            TextureRegion trRun = aniRun.getKeyFrame(elapsedTime, true);
            playerwidth = taRun.findRegion("run (1)").getRegionWidth();
            playerheight = taRun.findRegion("run (1)").getRegionHeight();
            if (bRight) {
                //sb.draw(trIdle, mainBody.getPosition().x - playerwidth / 2, mainBody.getPosition().y - playerheight / 2, playerwidth / 2, playerheight / 2);
                sb.draw(trRun, mainBody.getWorldCenter().x - playerwidth / 4, mainBody.getWorldCenter().y - playerwidth / 4, playerwidth / 2, playerheight / 2);
            } else {
                sb.draw(trRun, mainBody.getWorldCenter().x + playerwidth / 4, mainBody.getWorldCenter().y - playerwidth / 4, -playerwidth / 2, playerheight / 2);
            }
        }
    }

    void move() {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            isIdle = false;
            bRight = false;
            mainBody.setLinearVelocity(mainBody.getMass() * -100, mainBody.getLinearVelocity().y);
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            isIdle = false;
            bRight = true;
            mainBody.setLinearVelocity(mainBody.getMass() * 100, mainBody.getLinearVelocity().y);
        }
    }

    void stop() {
        isIdle = true;
        mainBody.setLinearVelocity(0, mainBody.getLinearVelocity().y);
    }

    boolean isGrounded = true;

    void jump() {
        isGrounded = false;
        mainBody.setLinearVelocity(new Vector2(mainBody.getLinearVelocity().x, mainBody.getMass() * 500));
    }

    Vector2 getLinearVelocity() {
        return mainBody.getLinearVelocity();
    }

    void setLinearVelocity(Vector2 velocity) {
        mainBody.setLinearVelocity(velocity);
    }

    void setGravityScale(float scale) {
        mainBody.setGravityScale(0);
    }

    boolean facingRight() {
        return bRight;
    }
}
