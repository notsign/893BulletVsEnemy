package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

/**
 * Created by kesty on 4/7/2016.
 */
public class Bullet {
    Body bullet;
    World world;
    boolean hasContacted = false;

    protected Bullet(World world, Vector2 playerpos, boolean facingRight) {
        this.world = world;
        setBodyDef(playerpos);
        setFixtureDef();
        setVelocity(facingRight);
    }

    private void setBodyDef(Vector2 playerpos) {
        BodyDef bulletdef = new BodyDef();
        bulletdef.position.set(playerpos);
        bulletdef.type = BodyDef.BodyType.DynamicBody;
        bullet = world.createBody(bulletdef);
        bullet.setGravityScale(0);
        bullet.isBullet();
        bullet.setSleepingAllowed(false);
        bullet.setFixedRotation(true);
    }

    private void setFixtureDef() {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(2f, 2f);
        FixtureDef bulletfdef = new FixtureDef();
        bulletfdef.shape = shape;
        bulletfdef.filter.groupIndex = -1; // Don't collide with another bullet
        bulletfdef.filter.categoryBits = 4;
        bulletfdef.filter.maskBits = 1 | 8; // Ground, enemy
        bullet.setUserData("bullet");
        // group index explanation:
        // Collision groups let you specify an integral group index.
        // You can have all fixtures with the same group index always collide (positive index)
        // or never collide (negative index).

        // basically you put everything that you want together in a group, and decide whether or not you want
        // them all to collide or not by using a positive or negative hexadecimal or integer
        // works similar to a category bit but far more general
        bullet.createFixture(bulletfdef);
        shape.dispose();
    }

    private void setVelocity(boolean facingRight) {
        if (facingRight) {
            bullet.setLinearVelocity(500000, 0);
        } else {
            bullet.setLinearVelocity(-500000, 0);
        }
    }
}
