package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by kesty on 3/21/2016.
 */
public class ScrBulletVsEnemy implements Screen, InputProcessor {
	Game game;
	World world;
	Map map;
	OrthographicCamera camera;
	Box2DDebugRenderer b2dr;
	TiledMapRenderer tiledMapRenderer;
	Player player;
	SpriteBatch batch = new SpriteBatch();
	ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	ArrayList<Enemy> enemies = new ArrayList<Enemy>();

	ScrBulletVsEnemy(Game game) {
		this.game = game;

		initializeWorld();
		initializeCamera();
		initializePlayer();
		initializeEnemy();
	}

	private void initializeWorld() {
		world = new World(new Vector2(0, -200), true);
		world.setContactListener(new ContactListener() {
			@Override
			public void beginContact(Contact c) {

			}

			@Override
			public void endContact(Contact c) {
				Fixture fa = c.getFixtureA();
				Fixture fb = c.getFixtureB();
				if (fa.getFilterData().categoryBits == 1) {
					player.isGrounded = false;
				} else if (fb.getFilterData().categoryBits == 1) {
					player.isGrounded = false;
				}
			}

			@Override
			public void preSolve(Contact c, Manifold oldManifold) {
				Fixture fa = c.getFixtureA();
				Fixture fb = c.getFixtureB();

				if (fa.getFilterData().categoryBits == 1) {
					player.isGrounded = true;
				} else if (fb.getFilterData().categoryBits == 1) {
					player.isGrounded = true;
				}

			}

			@Override
			public void postSolve(Contact c, ContactImpulse impulse) {
				Fixture fa = c.getFixtureA();
				Fixture fb = c.getFixtureB();

				Fixture enemyFixture = (fa.getFilterData().groupIndex == -2) ? fa : (fb.getFilterData().groupIndex == -2) ? fb : null;
				Fixture bulletFixture = (fa.getFilterData().groupIndex == -1) ? fa : (fb.getFilterData().groupIndex == -1) ? fb : null;

				if(!c.isTouching())
					return; //lol who cares

				if(enemyFixture != null && bulletFixture != null){ // An enemy and a bullet contacted
					// Find the enemy that owns this fixture
					for(Enemy enemy : enemies){
						if(enemy.enemyMainBody.equals(enemyFixture.getBody())){
							enemy.health = -999;
							break;
						}
					}
				}

				if(bulletFixture != null){ // A bullet hit something
					// Find the bullet that owns the fixture
					for(Bullet bullet : bullets){
						if(bullet.bullet.equals(bulletFixture.getBody())){
							bullet.hasContacted = true;
							break;
						}
					}
				}
			}
		});

		map = new Map(world, "debugroom");

	}

	private void initializeCamera() {
		b2dr = new Box2DDebugRenderer();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 64 * (19 / 2), 64 * (10 / 2));

		camera.update();
		tiledMapRenderer = new OrthogonalTiledMapRenderer(map.getMap(), map.getUnitScale());
	}

	private void initializePlayer() {
		player = new Player(world, map.getSpawnpoint());
	}

	private void initializeEnemy() {
		enemies.add(new Enemy(world, map.getEnemySpawn()));
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		world.step(1 / 60f, 6, 2);

		//if (bullets.size() > 0)
			clean(); // Remove stuff that shouldn't exist

		camera.position.set(player.getPosition());
		camera.update();
		tiledMapRenderer.setView(camera);
		tiledMapRenderer.render();
		b2dr.render(world, camera.combined);

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		for(Enemy e : enemies)
			e.draw(batch);
		player.render(batch);
		batch.end();


		for(Enemy e : enemies)
			e.move(player.mainBody.getPosition().x);
		player.move();

	}

	private void clean() {
		// We have to remove stuff here instead of in the contact listener because it will crash
		// because (my guess) of a ConcurrentModificationException.

		Iterator<Bullet> bulletIterator = bullets.iterator();
		while (bulletIterator.hasNext()) {
			Bullet b = bulletIterator.next();
			if (b.hasContacted) {
				world.destroyBody(b.bullet);
				bulletIterator.remove();
			}
		}

		Iterator<Enemy> enemyIterator = enemies.iterator();
		while(enemyIterator.hasNext()){
			Enemy enemy = enemyIterator.next();
			if(enemy.health <= 0) {
				world.destroyBody(enemy.enemyMainBody);
				enemyIterator.remove();
			}
		}
	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {

	}

	@Override
	public void dispose() {

	}

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Input.Keys.Z && player.isGrounded) {
			player.jump();
		}
		if (keycode == Input.Keys.X && player.bulletCooldown == 0) {
			bullets.add(new Bullet(world, new Vector2(player.getPosition().x, player.getPosition().y), player.facingRight()));
			player.bulletCooldown = 30;
			// set a "cooling" period so bullets can't be massfired
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (keycode == com.badlogic.gdx.Input.Keys.LEFT || keycode == com.badlogic.gdx.Input.Keys.RIGHT) {
			player.stop();
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}
}

